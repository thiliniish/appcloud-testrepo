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
import org.json.JSONException;
import org.json.JSONObject;
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
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;

import org.wso2.carbon.cloud.billing.internal.ServiceDataHolder;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.stream.XMLStreamException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
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
    private static String usageOfApiUrl;
    private static String usageOfSubscriberUrl;
    private static String usageOfApiBySubscriberUrl;
    private static String usageOfTenantUrl;
    private static String usageOfApiByApplicationBySubscriberUrl;
    private static String userAPIsUri;
    private static String userAPIApplicationsUri;
    private static String apiSubscriptionRemovalUri;

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
        usageOfApiUrl = BillingConfigUtils.getBillingConfiguration().getDSConfig().getApiCloudMonetizationServiceUrl()
                + MonetizationConstants.DS_API_URI_API_USAGE;
        usageOfSubscriberUrl = BillingConfigUtils.getBillingConfiguration().getDSConfig()
                .getApiCloudMonetizationServiceUrl() + MonetizationConstants.DS_API_URI_SUBSCRIBER_USAGE;
        usageOfApiBySubscriberUrl = BillingConfigUtils.getBillingConfiguration().getDSConfig()
                .getApiCloudMonetizationServiceUrl() + MonetizationConstants.DS_API_URI_SUBSCRIBER_API_USAGE;
        usageOfTenantUrl = BillingConfigUtils.getBillingConfiguration().getDSConfig()
                .getApiCloudMonetizationServiceUrl() + MonetizationConstants.DS_API_URI_TENANT_USAGE;
        usageOfApiByApplicationBySubscriberUrl = BillingConfigUtils.getBillingConfiguration().getDSConfig()
                .getApiCloudMonetizationServiceUrl() + MonetizationConstants
                .DS_API_URI_SUBSCRIBER_API_USAGE_BY_APPLICATION;
        userAPIsUri = BillingConfigUtils.getBillingConfiguration().getDSConfig().getApiCloudMonetizationServiceUrl()
                + MonetizationConstants.DS_API_URI_USER_APIS;
        userAPIApplicationsUri =
                BillingConfigUtils.getBillingConfiguration().getDSConfig().getApiCloudMonetizationServiceUrl()
                        + MonetizationConstants.DS_API_URI_USER_API_APPLICATIONS;
        apiSubscriptionRemovalUri =
                BillingConfigUtils.getBillingConfiguration().getDSConfig().getApiCloudMonetizationServiceUrl()
                        + MonetizationConstants.DS_API_URI_REMOVE_API_SUBSCRIPTION;
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

            return DataServiceBillingRequestProcessor.isXMLResponseSuccess(response);
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
     * Removes paid api subscriptions of the user
     *
     * @param subscriberId subscriber id
     * @param tenantDomain tenant domain
     * @return boolean status of the db transaction
     * @throws CloudMonetizationException
     */
    public static boolean removePaidApiSubscriptionsOfUser(String subscriberId, String tenantDomain)
            throws CloudMonetizationException {
        try {
            //TODO use an api from api manager to access apim databases instead of using a data service.
            TenantManager tenantManager = ServiceDataHolder.getInstance().getRealmService().getTenantManager();
            int tenantId = tenantManager.getTenantId(tenantDomain);
            if (tenantId != MultitenantConstants.SUPER_TENANT_ID && tenantId != -1) {
                String url = apiSubscriptionRemovalUri
                        .replace(MonetizationConstants.RESOURCE_IDENTIFIER_TENANT_ID, String.valueOf(tenantId))
                        .replace(MonetizationConstants.RESOURCE_IDENTIFIER_SUBSCRIBER_ID,
                                CloudBillingUtils.encodeUrlParam(subscriberId).trim());
                List<String> freeTiersList = getFreeTiersOfTenant(tenantDomain);
                List<NameValuePair> nameValuePairs = new ArrayList<>();
                for (String freeTier : freeTiersList) {
                    NameValuePair tmpFreeTierNVP = new NameValuePair(MonetizationConstants.FREE_TIER, freeTier.trim());
                    nameValuePairs.add(tmpFreeTierNVP);
                }
                String response = dsBRProcessor.doDelete(url, BillingConstants.HTTP_TYPE_APPLICATION_XML,
                        nameValuePairs.toArray(new NameValuePair[nameValuePairs.size()]));
                return DataServiceBillingRequestProcessor.isXMLResponseSuccess(response);
            } else {
                throw new CloudMonetizationException(
                        "Error while retrieving tenant id for tenant domain: " + tenantDomain);
            }
        } catch (CloudBillingException | UserStoreException | UnsupportedEncodingException | XMLStreamException e) {
            throw new CloudMonetizationException(
                    "Error while sending remove api subscriptions request to data service for" +
                            " user: " + subscriberId + " tenant domain: " + tenantDomain, e);
        }
    }

    /**
     * Retrieve a list of free tiers of a given tenant
     *
     * @param tenantDomain tenant domain
     * @return list of free tier IDs
     * @throws CloudMonetizationException
     */
    public static List<String> getFreeTiersOfTenant(String tenantDomain) throws CloudMonetizationException {
        try {
            Resource tiersXmlResource = CloudBillingUtils
                    .getRegistryResource(tenantDomain, MonetizationConstants.tiersXmlUrl);
            if (tiersXmlResource != null) {
                String content = new String((byte[]) tiersXmlResource.getContent());
                List<String> freeTiers = new ArrayList<String>();
                OMElement element = AXIOMUtil.stringToOM(content);
                OMElement assertion = element.getFirstChildWithName(MonetizationConstants.ASSERTION_ELEMENT);
                Iterator policies = assertion.getChildrenWithName(MonetizationConstants.POLICY_ELEMENT);
                while (policies.hasNext()) {
                    OMElement attributes = null;
                    OMElement policy = (OMElement) policies.next();
                    OMElement id = policy.getFirstChildWithName(MonetizationConstants.THROTTLE_ID_ELEMENT);
                    String tierName = id.getText();
                    if (MonetizationConstants.UNAUTHENTICATED.equalsIgnoreCase(tierName)) {
                        continue;
                    }
                    OMElement tier = policy.getFirstChildWithName(MonetizationConstants.POLICY_ELEMENT)
                            .getFirstChildWithName(MonetizationConstants.THROTTLE_CONTROL_ELEMENT)
                            .getFirstChildWithName(MonetizationConstants.POLICY_ELEMENT)
                            .getFirstChildWithName(MonetizationConstants.POLICY_ELEMENT);
                    if (tier != null) {
                        attributes = tier.getFirstChildWithName(MonetizationConstants.THROTTLE_ATTRIBUTES_ELEMENT);
                    }
                    if (attributes != null) {
                        OMElement billingPlan = attributes
                                .getFirstChildWithName(MonetizationConstants.THROTTLE_ATTRIBUTES_BILLING_PLAN_ELEMENT);
                        if (billingPlan != null && MonetizationConstants.FREE.equals(billingPlan.getText())) {
                            freeTiers.add(tierName);
                        }
                    }
                }
                return freeTiers;
            } else {
                throw new CloudMonetizationException(
                        "tiers.xml file could not be loaded for tenant " + tenantDomain + ".");
            }
        } catch (RegistryException | XMLStreamException e) {
            throw new CloudMonetizationException("Error occurred while getting free tiers of tenant: " + tenantDomain + ".", e);
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
                    new NameValuePair(MonetizationConstants.SOAP_API_PROVIDER, apiDataObj
                            .get(MonetizationConstants.SOAP_API_PROVIDER).getAsString()), new NameValuePair(BillingConstants.PARAM_RATE_PLAN_ID, apiDataObj.get(BillingConstants
                            .PARAM_RATE_PLAN_ID).getAsString()),
                    new NameValuePair(BillingConstants.PARAM_SUBSCRIPTION_NUMBER, apiDataObj.get(BillingConstants
                            .PARAM_SUBSCRIPTION_NUMBER).getAsString()),
                    new NameValuePair(BillingConstants.PARAM_START_DATE, effectiveDate.trim())
            };

            String response = dsBRProcessor.doPost(url, BillingConstants.HTTP_TYPE_APPLICATION_XML, nameValuePairs);
            return DataServiceBillingRequestProcessor.isXMLResponseSuccess(response);
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
            String url = subscriptionUri.replace(MonetizationConstants.RESOURCE_IDENTIFIER_ACCOUNT_NO, CloudBillingUtils.encodeUrlParam(accountNumber))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_APP_NAME, CloudBillingUtils.encodeUrlParam(appName))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_API_NAME, CloudBillingUtils.encodeUrlParam(apiName))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_API_VERSION,
                            CloudBillingUtils.encodeUrlParam(apiVersion));

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
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_ACCOUNT_NO,
                            CloudBillingUtils.encodeUrlParam(accountNumber))
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
     *
     * When subscription data not available on databases
     * it would be
     *
     * {
     *  "subscriptionInfoNotAvailable":true
     * }
     *
     * @throws CloudMonetizationException
     */
    public static String cancelSubscription(String accountNumber, String appName, String apiName, String apiVersion)
            throws CloudMonetizationException {
        String subscriptionInfo = getSubscriptionInfo(accountNumber, appName, apiName, apiVersion);

        JsonElement subscriptionInfoElement = new JsonParser().parse(subscriptionInfo);

        if (subscriptionInfoElement == null || subscriptionInfoElement.isJsonPrimitive()) {
            throw new CloudMonetizationException("Error while cancelling the subscription. Subscription data " +
                    "unavailable. Account no: " + accountNumber + ", Application name: " + appName + ", Api name: " +
                    apiName + ", Api " + "version: " + apiVersion);
        }

        if (subscriptionInfoElement.isJsonObject() && subscriptionInfoElement.getAsJsonObject().get(MonetizationConstants.SUBSCRIPTION)
                .isJsonPrimitive()) {
            LOGGER.warn("Subscription information not available. Proceeding with the subscription cancellation. Account no: " +
                    accountNumber + ", Application name: " + appName + ", Api name: " + apiName + ", Api " +
                    "version: " + apiVersion);
            JsonObject responseObj = new JsonObject();
            //This is added since some times there may be instances without subscription information on zuora side
            responseObj.addProperty("subscriptionInfoNotAvailable", true);
            return responseObj.toString();
        }

        JsonObject subscriptionObj = subscriptionInfoElement.getAsJsonObject().get(MonetizationConstants.SUBSCRIPTION)
                .getAsJsonObject();

        JsonObject zuoraResponseObj = new JsonParser().parse(removeZuoraSubscription(accountNumber, subscriptionObj))
                .getAsJsonObject();
        if (zuoraResponseObj.get(BillingConstants.ZUORA_RESPONSE_SUCCESS).getAsBoolean()) {
            return removeSubscriptionsInMonDb(accountNumber, appName, apiName, apiVersion, zuoraResponseObj);
        } else {
            LOGGER.error("Error while cancelling the subscription. response code: " + zuoraResponseObj.toString());
            zuoraResponseObj.addProperty(BillingConstants.MONETIZATION_DB_UPDATED, false);
            return zuoraResponseObj.toString();
        }
    }

    /**
     * Removes the subscription information from the monetization tables
     *
     * @param accountNumber accountNumber
     * @param appName application name
     * @param apiName api name
     * @param apiVersion api version
     * @param zuoraResponseObj zuora response object
     * @return zuora response object with "monetizationDbUpdated" attribute
     * @throws CloudMonetizationException
     */
    private static String removeSubscriptionsInMonDb(String accountNumber, String appName, String apiName,
            String apiVersion, JsonObject zuoraResponseObj)
            throws CloudMonetizationException {
        //TODO do following with box carring
        try {
            String subscriptionHistoryUrl = subscriptionHistoryUri
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_ACCOUNT_NO, CloudBillingUtils.encodeUrlParam(accountNumber))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_APP_NAME, CloudBillingUtils.encodeUrlParam(appName))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_API_NAME, CloudBillingUtils.encodeUrlParam(apiName))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_API_VERSION, CloudBillingUtils.encodeUrlParam(apiVersion));

            boolean response = DataServiceBillingRequestProcessor.isJsonResponseSuccess(dsBRProcessor.doPost
                    (subscriptionHistoryUrl, null, new NameValuePair[]{}));
            //TODO do following with box carring
            if (response) {
                String subscriptionUrl = subscriptionUri
                        .replace(MonetizationConstants.RESOURCE_IDENTIFIER_ACCOUNT_NO, CloudBillingUtils.encodeUrlParam(accountNumber))
                        .replace(MonetizationConstants.RESOURCE_IDENTIFIER_APP_NAME, CloudBillingUtils.encodeUrlParam(appName))
                        .replace(MonetizationConstants.RESOURCE_IDENTIFIER_API_NAME, CloudBillingUtils.encodeUrlParam(apiName))
                        .replace(MonetizationConstants.RESOURCE_IDENTIFIER_API_VERSION, CloudBillingUtils.encodeUrlParam(apiVersion));

                response = DataServiceBillingRequestProcessor
                        .isJsonResponseSuccess(dsBRProcessor.doDelete(subscriptionUrl, null, new NameValuePair[]{}));
                if (response) {
                    zuoraResponseObj.addProperty(BillingConstants.MONETIZATION_DB_UPDATED, true);
                    return zuoraResponseObj.toString();
                } else {
                    LOGGER.warn("Cancelling subscription: monetization tables update failure. Account no: " +
                            accountNumber + ", Application name: " + appName + ", Api name: " + apiName + ", Api " +
                            "version: " + apiVersion);
                    zuoraResponseObj.addProperty(BillingConstants.MONETIZATION_DB_UPDATED, false);
                    return zuoraResponseObj.toString();
                }
            } else {
                LOGGER.warn("Cancelling subscription: monetization tables update failure. Account no: " +
                        accountNumber + ", Application name: " + appName + ", Api name: " + apiName + ", Api " +
                        "version: " + apiVersion);
                zuoraResponseObj.addProperty(BillingConstants.MONETIZATION_DB_UPDATED, false);
                return zuoraResponseObj.toString();
            }

        } catch (UnsupportedEncodingException | CloudBillingException e) {
            throw new CloudMonetizationException("Error while updating api subscription history tables. ", e);
        }
    }

    /**
     * Remove all the api subscriptions of an Application
     *
     * @param accountNumber account number
     * @param appName application name
     * @return
     * {
     *     "removedSubscriptions": [
     *         {
     *             "AccountNumber": "A00000657",
     *             "ApiName": "CalculatorAPI",
     *             "ApiProvider": "rajith.siriw.ardana.gmail.com-AT-mustanggt350",
     *             "ApiVersion": "1.0",
     *             "AppName": "TESTAAA1",
     *             "RatePlanId": "2c92c0f8516cc19e0151854814d367ff",
     *             "StartDate": "2016-01-06T14:37:30.000+05:30",
     *             "SubscriptionNumber": "A-S00000699"
     *         },
     *         {
     *             "AccountNumber": "A00000657",
     *             "ApiName": "PhoneVerify",
     *             "ApiProvider": "criachae.fakeinbox.com -AT-mustanggt350",
     *             "ApiVersion": "1.0.0",
     *             "AppName": "TESTAAA1",
     *             "RatePlanId": "2c92c0f8516cc19e0151854814d367ff",
     *             "StartDate": "2016-01-06T14:43:38.000+05:30",
     *             "SubscriptionNumber": "A-S00000700"
     *         }
     *     ],
     *     "success": true
     * }
     *
     * If one of the subscriptions in the application isn't removed, the "success" attribute will be set to false
     * @throws CloudMonetizationException
     */
    public static String removeAppSubscriptions(String accountNumber, String appName) throws CloudMonetizationException {
        String subscriptionsInfo = getAppSubscriptionsInfo(accountNumber, appName);

        JsonElement element = new JsonParser().parse(subscriptionsInfo);

        if (!element.isJsonObject()) {
            throw new CloudMonetizationException("Error while cancelling the subscriptions for Account: " +
                    accountNumber + ", Application name: " + appName + ". Subscription details not available.");
        }
        JsonElement subscriptionsElement = element.getAsJsonObject().get(MonetizationConstants.SUBSCRIPTIONS);

        JsonObject cancellationObj = new JsonObject();
        cancellationObj.addProperty(BillingConstants.ZUORA_RESPONSE_SUCCESS, true);
        //Application only has free apis.
        if (subscriptionsElement.isJsonPrimitive() && subscriptionsElement.getAsString().isEmpty()) {
            return cancellationObj.toString();
        }

        if (!subscriptionsElement.isJsonObject()) {
            throw new CloudMonetizationException("Error while cancelling the subscriptions for Account: " +
                    accountNumber + ", Application name: " + appName + ". Subscription details not available.");
        }

        JsonElement subscriptionsJElement = subscriptionsElement.getAsJsonObject().get(MonetizationConstants.SUBSCRIPTION);
        JsonArray subscriptions;
        if (subscriptionsJElement.isJsonObject()) {
            subscriptions = new JsonArray();
            subscriptions.add(subscriptionsJElement.getAsJsonObject());
        } else if (subscriptionsJElement.isJsonArray()) {
            subscriptions = subscriptionsJElement.getAsJsonArray();
        } else {
            throw new CloudMonetizationException("Error while cancelling the subscriptions for Account: " +
                    accountNumber + ", Application name: " + appName + ". Subscription element should be either " +
                    "json  array or json object. ");
        }

        JsonArray removedSubscriptions = new JsonArray();

        for (JsonElement subscription : subscriptions) {
            JsonObject subscriptionObj = subscription.getAsJsonObject();
            String response = removeZuoraSubscription(accountNumber, subscriptionObj);
            JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
            if (!responseObj.isJsonObject() || responseObj.get(BillingConstants.ZUORA_RESPONSE_SUCCESS)
                    ==null || !responseObj.get(BillingConstants.ZUORA_RESPONSE_SUCCESS).getAsBoolean()) {
                LOGGER.error("Subscription cancellation failure. Account number: " + accountNumber + " , " +
                        "subscription no: " + subscriptionObj.get(MonetizationConstants.SUBSCRIPTION_NUMBER)
                        .getAsString() + ". Reasons: " + responseObj.get("reasons"));
                //Check already the success value set to false
                if (cancellationObj.get(BillingConstants.ZUORA_RESPONSE_SUCCESS).getAsBoolean()) {
                    cancellationObj.addProperty(BillingConstants.ZUORA_RESPONSE_SUCCESS, false);
                }
            } else {
                removeSubscriptionsInMonDb(accountNumber, subscriptionObj.get(MonetizationConstants.ATTRIB_APP_NAME)
                                .getAsString(), subscriptionObj.get(MonetizationConstants.ATTRIB_API_NAME).getAsString(),
                        subscriptionObj.get(MonetizationConstants.ATTRIB_API_VERSION).getAsString(), responseObj);
                removedSubscriptions.add(subscriptionObj);
            }
        }
        cancellationObj.add("removedSubscriptions", removedSubscriptions);
        return cancellationObj.toString();
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

    /**
     * @param tenantDomain    tenant domain
     * @param subscriberId    subscriber id
     * @param api             api name
     * @param version         api version
     * @param applicationName application name
     * @param startDate       date range - start date
     * @param endDate         date range - end date
     * @return JSON object of usage data
     * @throws CloudMonetizationException
     */
    public static JSONObject getTenantMonetizationUsageDataForGivenDateRange(String tenantDomain, String subscriberId,
            String api, String version, String applicationName, String startDate, String endDate)
            throws CloudMonetizationException {
        String url;
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        try {
            // "*" represents the selection of "all" option
            if (MonetizationConstants.ASTERISK_SYMBOL.equals(subscriberId)) {
                if (MonetizationConstants.ASTERISK_SYMBOL.equals(api)) {
                    //Usage of tenant
                    url = usageOfTenantUrl.replace(MonetizationConstants.RESOURCE_IDENTIFIER_TENANT,
                            CloudBillingUtils.encodeUrlParam("%@" + tenantDomain));
                } else {
                    //Usage of api A1 by all users
                    url = usageOfApiUrl.replace(MonetizationConstants.RESOURCE_IDENTIFIER_API_NAME,
                            CloudBillingUtils.encodeUrlParam(api))
                            .replace(MonetizationConstants.RESOURCE_IDENTIFIER_VERSION,
                                    CloudBillingUtils.encodeUrlParam(version));
                    NameValuePair tenantDomainNVP = new NameValuePair(MonetizationConstants.TENANT,
                            "%@" + tenantDomain);
                    nameValuePairs.add(tenantDomainNVP);
                }
            } else {
                if (MonetizationConstants.ASTERISK_SYMBOL.equals(api)) {
                    //Usage of all apis by subscriber S1
                    url = usageOfSubscriberUrl.replace(MonetizationConstants.RESOURCE_IDENTIFIER_SUBSCRIBER_ID,
                            CloudBillingUtils.encodeUrlParam(subscriberId));
                } else {
                    if (MonetizationConstants.ASTERISK_SYMBOL.equals(applicationName)) {
                        //Usage of api A1 by Subscriber S1 for all applications
                        String encodedSubId = CloudBillingUtils.encodeUrlParam(subscriberId);
                        String encodedApi = CloudBillingUtils.encodeUrlParam(api);
                        String encodedVersion = CloudBillingUtils.encodeUrlParam(version);
                        url = usageOfApiBySubscriberUrl
                                .replace(MonetizationConstants.RESOURCE_IDENTIFIER_SUBSCRIBER_ID, encodedSubId)
                                .replace(MonetizationConstants.RESOURCE_IDENTIFIER_API_NAME, encodedApi)
                                .replace(MonetizationConstants.RESOURCE_IDENTIFIER_VERSION, encodedVersion);
                    } else {
                        //Usage of api A1 by Subscriber S1 for application App1
                        url = usageOfApiByApplicationBySubscriberUrl
                                .replace(MonetizationConstants.RESOURCE_IDENTIFIER_SUBSCRIBER_ID,
                                        CloudBillingUtils.encodeUrlParam(subscriberId))
                                .replace(MonetizationConstants.RESOURCE_IDENTIFIER_API_NAME,
                                        CloudBillingUtils.encodeUrlParam(api))
                                .replace(MonetizationConstants.RESOURCE_IDENTIFIER_VERSION,
                                        CloudBillingUtils.encodeUrlParam(version))
                                .replace(MonetizationConstants.RESOURCE_IDENTIFIER_APP_NAME,
                                        CloudBillingUtils.encodeUrlParam(applicationName));
                    }
                }
            }
            NameValuePair startDateNVP = new NameValuePair(MonetizationConstants.START_DATE, startDate);
            NameValuePair endDateNVP = new NameValuePair(MonetizationConstants.END_DATE, endDate);
            nameValuePairs.add(startDateNVP);
            nameValuePairs.add(endDateNVP);
            String response = dsBRProcessor.doGet(url, BillingConstants.HTTP_TYPE_APPLICATION_JSON,
                    nameValuePairs.toArray(new NameValuePair[nameValuePairs.size()]));
            return new JSONObject(response);
        } catch (CloudBillingException | UnsupportedEncodingException | JSONException e) {
            throw new CloudMonetizationException(
                    "Error while getting monetization usage data of tenant: " + tenantDomain, e);
        }
    }

    /**
     * Retrieve APIs for a given user
     *
     * @param username subscriber username
     * @return String xml response
     * @throws CloudMonetizationException
     */
    public static String getUserAPIs(String username) throws CloudMonetizationException {
        try {
            String url = userAPIsUri.replace(MonetizationConstants.RESOURCE_IDENTIFIER_USERNAME,
                    CloudBillingUtils.encodeUrlParam(username));
            return dsBRProcessor.doGet(url, BillingConstants.HTTP_TYPE_APPLICATION_JSON, null);
        } catch (CloudBillingException | UnsupportedEncodingException e) {
            throw new CloudMonetizationException("Error while retrieving APIs for user: " + username, e);
        }
    }

    /**
     * Retrieve Application Names for a given API for a given user
     *
     * @param username subscriber username
     * @return String xml response
     * @throws CloudMonetizationException
     */
    public static String getUserAPIApplications(String username, String apiName) throws CloudMonetizationException {
        try {
            String url = userAPIApplicationsUri.replace(MonetizationConstants.RESOURCE_IDENTIFIER_USERNAME,
                    CloudBillingUtils.encodeUrlParam(username))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_API_NAME,
                            CloudBillingUtils.encodeUrlParam(apiName));
            return dsBRProcessor.doGet(url, BillingConstants.HTTP_TYPE_APPLICATION_JSON, null);
        } catch (CloudBillingException | UnsupportedEncodingException e) {
            throw new CloudMonetizationException("Error while retrieving applications  for user: " + username, e);
        }
    }
}
