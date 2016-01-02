/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.cloud.billing.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.commons.MonetizationConstants;
import org.wso2.carbon.cloud.billing.commons.utils.BillingConfigUtils;
import org.wso2.carbon.cloud.billing.commons.utils.CloudBillingUtils;
import org.wso2.carbon.cloud.billing.commons.zuora.ZuoraRESTUtils;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.exceptions.CloudMonetizationException;
import org.wso2.carbon.cloud.billing.processor.BillingRequestProcessor;
import org.wso2.carbon.cloud.billing.processor.BillingRequestProcessorFactory;
import org.wso2.carbon.cloud.billing.processor.DataServiceBillingRequestProcessor;

import javax.xml.stream.XMLStreamException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Model to represent Utilities for Cloud monetization service
 */
public final class APICloudMonetizationUtils {

    private static final Log LOGGER = LogFactory.getLog(CloudBillingServiceUtils.class);

    /*Database http request processor*/
    private static BillingRequestProcessor dsBRProcessor;

    /*Data service URIs*/
    private static String subscribersUri;
    private static String apiSubscriptionUri;
    private static String subscriptionUri;
    private static String appSubscriptionsUri;
    private static String subscriptionHistoryUri;

    static {
        dsBRProcessor = BillingRequestProcessorFactory
                .getBillingRequestProcessor(BillingRequestProcessorFactory.ProcessorType.DATA_SERVICE,
                        BillingConfigUtils.getBillingConfiguration().getDSConfig().getHttpClientConfig());

        subscribersUri =
                BillingConfigUtils.getBillingConfiguration().getDSConfig().getApiCloudMonetizationServiceUrl()
                        + MonetizationConstants.DS_API_URI_MON_APIC_SUBSCRIBER;
        apiSubscriptionUri =
                BillingConfigUtils.getBillingConfiguration().getDSConfig().getApiCloudMonetizationServiceUrl()
                        + MonetizationConstants.DS_API_URI_UPDATE_API_SUBSCRIPTION;
        subscriptionUri = BillingConfigUtils.getBillingConfiguration().getDSConfig()
                .getApiCloudMonetizationServiceUrl() + MonetizationConstants.DS_API_URI_MON_APIC_SUBSCRIPTION;
        appSubscriptionsUri = BillingConfigUtils.getBillingConfiguration().getDSConfig()
                .getApiCloudMonetizationServiceUrl() + MonetizationConstants.DS_API_URI_APP_SUBSCRIPTIONS;
        subscriptionHistoryUri = BillingConfigUtils.getBillingConfiguration().getDSConfig()
                .getApiCloudMonetizationServiceUrl() + MonetizationConstants.DS_API_URI_API_SUBSCRIPTION_HISTORY;
    }

    private APICloudMonetizationUtils() {
    }

    /**
     * Retrieve subscriber information
     *
     * @param username     subscriber username
     * @param tenantDomain tenant domain
     * @return String xml response
     * @throws CloudMonetizationException
     */
    public static String getAPISubscriberInfo(String username, String tenantDomain) throws CloudMonetizationException {

        try {
            String url = subscribersUri
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_TENANT, CloudBillingUtils.encodeUrlParam(tenantDomain))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_USERNAME, CloudBillingUtils.encodeUrlParam(username));
            return dsBRProcessor.doGet(url, BillingConstants.HTTP_TYPE_APPLICATION_JSON, null);

        } catch (CloudBillingException | UnsupportedEncodingException e) {
            throw new CloudMonetizationException(
                    "Error while retrieving API subscribers for user: " + username + " tenant domain: " + tenantDomain,
                    e);
        }
    }

    /**
     * Update subscriber information table
     *
     * @param username       subscriber username
     * @param tenantDomain   tenant domain
     * @param isTestAccount  boolean test account or not
     * @param accountNumber  zuora account number
     * @param isExistingUser boolean existence of the user
     * @throws CloudMonetizationException
     */
    public static boolean updateAPISubscriberInfo(String username, String tenantDomain, boolean isTestAccount,
                                                  String accountNumber, boolean isExistingUser) throws CloudMonetizationException {
        try {
            String url = subscribersUri
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_TENANT, CloudBillingUtils.encodeUrlParam(tenantDomain))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_USERNAME, CloudBillingUtils.encodeUrlParam(username));
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            NameValuePair testAccountNVP = new NameValuePair(MonetizationConstants.PARAM_IS_TEST_ACCOUNT,
                    String.valueOf(isTestAccount));
            nameValuePairs.add(testAccountNVP);

            if (StringUtils.isNotBlank(accountNumber)) {
                NameValuePair accountNumberNVP = new NameValuePair(BillingConstants.PARAM_ACCOUNT_NUMBER,
                        accountNumber.trim());
                nameValuePairs.add(accountNumberNVP);
            }
            String response;
            if (!isExistingUser) {
                response = dsBRProcessor.doPost(url, BillingConstants.HTTP_TYPE_APPLICATION_XML,
                        nameValuePairs.toArray(new NameValuePair[nameValuePairs.size()]));
            } else {
                response = dsBRProcessor.doPut(url, BillingConstants.HTTP_TYPE_APPLICATION_XML,
                        nameValuePairs.toArray(new NameValuePair[nameValuePairs.size()]));
            }

            return DataServiceBillingRequestProcessor.isRequestSuccess(response);
        } catch (CloudBillingException | UnsupportedEncodingException | XMLStreamException e) {
            throw new CloudMonetizationException(
                    "Error while updating API subscribers. For user: " + username + " tenant domain: " + tenantDomain,
                    e);
        }
    }

    /**
     * Block api subscriptions of the user
     *
     * @param userId   user id of the user
     * @param tenantId tenant id
     * @throws CloudMonetizationException
     */
    public static void blockApiSubscriptionsOfUser(String userId, String tenantId) throws CloudMonetizationException {
        try {
            //TODO use an api to update apim databases instead of using a data service.
            String url = apiSubscriptionUri.replace(MonetizationConstants.RESOURCE_IDENTIFIER_TENANT_ID,
                    CloudBillingUtils.encodeUrlParam(tenantId));
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            NameValuePair userIdNVP = new NameValuePair(MonetizationConstants.USER_ID, userId.trim());
            NameValuePair statusNVP = new NameValuePair(MonetizationConstants.API_SUBSCRIPTION_STATUS,
                    MonetizationConstants.API_SUBSCRIPTION_BLOCKED_STATUS);
            nameValuePairs.add(userIdNVP);
            nameValuePairs.add(statusNVP);
            dsBRProcessor.doPut(url, null, nameValuePairs.toArray(new NameValuePair[nameValuePairs.size()]));
        } catch (CloudBillingException | UnsupportedEncodingException e) {
            throw new CloudMonetizationException("Error while sending block subscriptions request to data service for" +
                    " user :" + userId + " tenant Id :" + tenantId, e);
        }
    }

    /**
     * @param tenantDomain  tenant domain
     * @param accountNumber account number
     * @param apiDataObj       api data json object
     * @param effectiveDate effective date
     * @return success information
     * @throws CloudMonetizationException
     */
    public static boolean addSubscriptionInformation(String tenantDomain, String accountNumber, JsonObject apiDataObj,
                                                     String effectiveDate) throws CloudMonetizationException {
        try {
            String url = subscriptionUri
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_ACCOUNT_NO, CloudBillingUtils
                            .encodeUrlParam(accountNumber))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_APP_NAME, CloudBillingUtils
                            .encodeUrlParam(apiDataObj.get(MonetizationConstants.SOAP_APP_NAME).getAsString()))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_API_NAME, CloudBillingUtils
                            .encodeUrlParam(apiDataObj.get(MonetizationConstants.SOAP_API_NAME).getAsString()))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_API_VERSION, CloudBillingUtils
                            .encodeUrlParam(apiDataObj.get(MonetizationConstants.SOAP_API_VERSION).getAsString()));

            NameValuePair[] nameValuePairs = new NameValuePair[]{
                    new NameValuePair(BillingConstants.PARAM_RATE_PLAN_ID, apiDataObj.get(BillingConstants
                            .PARAM_RATE_PLAN_ID).getAsString()),
                    new NameValuePair(BillingConstants.PARAM_SUBSCRIPTION_NUMBER, apiDataObj.get(BillingConstants
                            .PARAM_SUBSCRIPTION_NUMBER).getAsString()),
                    new NameValuePair(BillingConstants.PARAM_START_DATE, effectiveDate.trim())
            };

            String response = dsBRProcessor.doPost(url, BillingConstants.HTTP_TYPE_APPLICATION_XML, nameValuePairs);
            return DataServiceBillingRequestProcessor.isRequestSuccess(response);
        } catch (CloudBillingException | UnsupportedEncodingException | XMLStreamException e) {
            throw new CloudMonetizationException("Error while adding subscription information for child account: " +
                    accountNumber + " of the parent tenant: " + tenantDomain, e);
        }
    }

    /**
     * Get subscriber information
     *
     * @param accountNumber account number
     * @param appName       application name
     * @param apiName       api name
     * @param apiVersion    api version
     * @return response json String
     */
    public static String getSubscriptionInfo(String accountNumber, String appName, String apiName, String apiVersion)
            throws CloudMonetizationException {
        try {
            String url = subscriptionUri
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_ACCOUNT_NO, CloudBillingUtils.encodeUrlParam(accountNumber))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_APP_NAME, CloudBillingUtils.encodeUrlParam(appName))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_API_NAME, CloudBillingUtils.encodeUrlParam(apiName))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_API_VERSION, CloudBillingUtils.encodeUrlParam(apiVersion));

            return dsBRProcessor.doGet(url, BillingConstants.HTTP_TYPE_APPLICATION_JSON, null);
        } catch (UnsupportedEncodingException | CloudBillingException e) {
            throw new CloudMonetizationException("Error while retrieving subscription information for child account: " +
                    accountNumber, e);
        }
    }

    /**
     * Retrieve App's all api subscriptions
     *
     * @param accountNumber account number
     * @param appName app name
     * @return json object array of subscriptions
     * @throws CloudMonetizationException
     */
    public static String getAppSubscriptionsInfo(String accountNumber, String appName) throws CloudMonetizationException {
        try {
            String url = appSubscriptionsUri
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_ACCOUNT_NO, CloudBillingUtils.encodeUrlParam(accountNumber))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_APP_NAME, CloudBillingUtils.encodeUrlParam(appName));

            return dsBRProcessor.doGet(url, BillingConstants.HTTP_TYPE_APPLICATION_JSON, null);
        } catch (UnsupportedEncodingException | CloudBillingException e) {
            throw new CloudMonetizationException("Error while retrieving app subscriptions information for child " +
                    "account: " + accountNumber, e);
        }
    }

    /**
     * Cancelling a paid API Subscription
     *
     * @param accountNumber account number
     * @param appName       application name
     * @param apiName       api name
     * @param apiVersion    api version
     * @return in success
     * {
     *  "success": true,
     *  "subscriptionId": "2c92c0fb51b054350151b382f3635c6f",
     *  "cancelledDate": "2013-07-01",
     *  "totalDeltaMrr": 0,
     *  "totalDeltaTcv": null
     * }
     *
     * failure
     *
     * {
     *  "success": false,
     *  "processId": "2E185B5582D256E7",
     *  "reasons": [
     *      {
     *          "code": 53200020,
     *          "message": "Only activated subscription can be cancelled."
     *      }
     *  ]
     * }
     * @throws CloudMonetizationException
     */
    public static String cancelSubscription(String accountNumber, String appName, String apiName, String apiVersion)
            throws CloudMonetizationException {
        String subscriptionInfo = getSubscriptionInfo(accountNumber, appName, apiName, apiVersion);
        JsonObject subscriptionObj = new JsonParser().parse(subscriptionInfo).getAsJsonObject().get
                (MonetizationConstants.SUBSCRIPTION).getAsJsonObject();

        JsonObject zuoraResponseObj = new JsonParser().parse(removeZuoraSubscription(accountNumber, subscriptionObj)).getAsJsonObject();
        if (zuoraResponseObj.get(BillingConstants.ZUORA_RESPONSE_SUCCESS).getAsBoolean()){
            //TODO do following with box carring
            try {
                String subscriptionHistoryUrl = subscriptionHistoryUri
                        .replace(MonetizationConstants.RESOURCE_IDENTIFIER_ACCOUNT_NO, CloudBillingUtils.encodeUrlParam(accountNumber))
                        .replace(MonetizationConstants.RESOURCE_IDENTIFIER_APP_NAME, CloudBillingUtils.encodeUrlParam(appName))
                        .replace(MonetizationConstants.RESOURCE_IDENTIFIER_API_NAME, CloudBillingUtils.encodeUrlParam(apiName))
                        .replace(MonetizationConstants.RESOURCE_IDENTIFIER_API_VERSION, CloudBillingUtils.encodeUrlParam(apiVersion));

                boolean response = Boolean.valueOf(dsBRProcessor.doPost(subscriptionHistoryUrl, null, new
                        NameValuePair[]{}));
                //TODO do following with box carring
                if (response) {
                    String subscriptionUrl = subscriptionUri
                            .replace(MonetizationConstants.RESOURCE_IDENTIFIER_ACCOUNT_NO, CloudBillingUtils.encodeUrlParam(accountNumber))
                            .replace(MonetizationConstants.RESOURCE_IDENTIFIER_APP_NAME, CloudBillingUtils.encodeUrlParam(appName))
                            .replace(MonetizationConstants.RESOURCE_IDENTIFIER_API_NAME, CloudBillingUtils.encodeUrlParam(apiName))
                            .replace(MonetizationConstants.RESOURCE_IDENTIFIER_API_VERSION, CloudBillingUtils.encodeUrlParam(apiVersion));

                    response = Boolean.valueOf(dsBRProcessor.doDelete(subscriptionUrl, null, new NameValuePair[]{}));
                    if (response) {
                        zuoraResponseObj.addProperty(BillingConstants.DB_TABLES_UPDATED, true);
                        return zuoraResponseObj.toString();
                    } else {
                        zuoraResponseObj.addProperty(BillingConstants.DB_TABLES_UPDATED, false);
                        return zuoraResponseObj.toString();
                    }
                } else {
                    zuoraResponseObj.addProperty(BillingConstants.DB_TABLES_UPDATED, false);
                    return zuoraResponseObj.toString();
                }

            } catch (UnsupportedEncodingException | CloudBillingException e) {
                throw new CloudMonetizationException("Error while updating api subscription history tables. ", e);
            }
        } else {
            LOGGER.error("Error while cancelling the subscription. response code: " + zuoraResponseObj.toString());
            zuoraResponseObj.addProperty(BillingConstants.DB_TABLES_UPDATED, false);
            return zuoraResponseObj.toString();
        }
    }

    /**
     * Remove all the api subscriptions of an Application
     *
     * @param accountNumber account number
     * @param appName application name
     * @throws CloudMonetizationException
     */
    public static String removeAppSubscriptions(String accountNumber, String appName) throws CloudMonetizationException {
        String subscriptionsInfo = getAppSubscriptionsInfo(accountNumber, appName);
        JsonObject subscriptionsObj = new JsonParser().parse(subscriptionsInfo).getAsJsonObject();
        if (subscriptionsObj.isJsonObject()) {
            JsonArray subscriptions = subscriptionsObj.getAsJsonArray(MonetizationConstants.SUBSCRIPTION);
            JsonObject results = new JsonObject();

            for (JsonElement subscription : subscriptions) {
                JsonObject subscriptionObj = subscription.getAsJsonObject();
                String subscriptionNumber = subscriptionObj.get(BillingConstants.PARAM_SUBSCRIPTION_NUMBER).getAsString();
                String response = removeZuoraSubscription(accountNumber, subscriptionObj);
                JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
                /*if (!responseObj.isJsonObject() || !responseObj.get(BillingConstants.ZUORA_RESPONSE_SUCCESS)
                        ==null || !responseObj.get(BillingConstants.ZUORA_RESPONSE_SUCCESS).getAsBoolean()) {
                    LOGGER.error("Subscription cancellation failure. Account number: " + accountNumber + " , " +
                            "subscription no: " + subscriptionObj.get(MonetizationConstants.SUBSCRIPTION_NUMBER).getAsString());
                }*/
                results.add(subscriptionNumber, responseObj);
            }
            return results.toString();
        }  else {
            throw new CloudMonetizationException("Error while cancelling the subscription for account: " +
                    accountNumber + " Subscription details not available");
        }
    }

    /**
     *
     * @param accountNumber account number
     * @param subscriptionObj subscription details
     * @return in success
     * {
     *  "success": true,
     *  "subscriptionId": "2c92c0fb51b054350151b382f3635c6f",
     *  "cancelledDate": "2013-07-01",
     *  "totalDeltaMrr": 0,
     *  "totalDeltaTcv": null
     * }
     *
     * failure
     *
     * {
     *  "success": false,
     *  "processId": "2E185B5582D256E7",
     *  "reasons": [
     *      {
     *          "code": 53200020,
     *          "message": "Only activated subscription can be cancelled."
     *      }
     *  ]
     * }
     *
     * @throws CloudMonetizationException
     */
    private static String removeZuoraSubscription(String accountNumber, JsonObject subscriptionObj) throws
            CloudMonetizationException {
        if (subscriptionObj.isJsonObject()) {
            String subscriptionNumber = subscriptionObj.get(MonetizationConstants.SUBSCRIPTION_NUMBER).getAsString();
            try {
                JsonObject payload = new JsonObject();
                SimpleDateFormat simpleDateFormatter = new SimpleDateFormat(BillingConstants.DATE_FORMAT);
                String effectiveDate = simpleDateFormatter.format(new Date());
                payload.addProperty(BillingConstants.CANCELLATION_POLICY, BillingConstants.CANCELLATION_POLICY_SPECIFIC_DATE);
                payload.addProperty(BillingConstants.CANCELLATION_EFFECTIVE_DATE, effectiveDate);
                payload.addProperty(BillingConstants.INVOICE_COLLECT, true);
                return ZuoraRESTUtils.cancelSubscription(subscriptionNumber, payload.toString());

            } catch (CloudBillingException e) {
                throw new CloudMonetizationException("Error while cancelling the subscription for account: " +
                        accountNumber + " subscription number: " + subscriptionNumber, e);
            }
        } else {
            throw new CloudMonetizationException("Error while cancelling the subscription for account: " +
                    accountNumber + " Subscription details not available");
        }
    }
}
