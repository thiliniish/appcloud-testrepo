/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.cloud.billing.core.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.billing.core.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.core.commons.CloudBillingServiceProvider;
import org.wso2.carbon.cloud.billing.core.commons.MonetizationConstants;
import org.wso2.carbon.cloud.billing.core.commons.config.BillingConfigManager;
import org.wso2.carbon.cloud.billing.core.commons.utils.CloudBillingUtils;
import org.wso2.carbon.cloud.billing.core.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.core.exceptions.CloudMonetizationException;
import org.wso2.carbon.cloud.billing.core.processor.BillingRequestProcessor;
import org.wso2.carbon.cloud.billing.core.processor.BillingRequestProcessorFactory;
import org.wso2.carbon.cloud.billing.core.processor.DataServiceBillingRequestProcessor;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * Model to represent Utilities for Cloud monetization service
 */
public final class APICloudMonetizationUtils {

    private static final Log LOGGER = LogFactory.getLog(APICloudMonetizationUtils.class);

    /*Database http request processor*/
    private static BillingRequestProcessor dsBRProcessor;
    private static BillingRequestProcessor apimRestRequestProcessor;

    /*Data service URIs*/
    private static String subscribersUri;
    private static String subscriptionUri;
    private static String subscriptionHistoryUri;
    private static String appSubscriptionsUri;
    private static String ratePlanUrl;

    /*APIM Rest API URIs*/
    private static String tiersOfTenantUri;

    static {
        dsBRProcessor = BillingRequestProcessorFactory.getInstance()
                .getBillingRequestProcessor(BillingRequestProcessorFactory.ProcessorType.DATA_SERVICE);
        apimRestRequestProcessor = BillingRequestProcessorFactory.getInstance()
                .getBillingRequestProcessor(BillingRequestProcessorFactory.ProcessorType.APIM_REST);

        String cloudMonUri = BillingConfigManager.getBillingConfiguration().getDataServiceConfig()
                .getCloudMonetizationServiceUri();
        String apiCloudMonUri = BillingConfigManager.getBillingConfiguration().getDataServiceConfig()
                .getApiCloudMonetizationServiceUri();
        String apimRestUri = BillingConfigManager.getBillingConfiguration().getApimRestAPIConfig().getRestServiceUri();

        subscribersUri = apiCloudMonUri.concat(MonetizationConstants.DS_API_URI_MON_APIC_SUBSCRIBER);
        subscriptionUri = apiCloudMonUri.concat(MonetizationConstants.DS_API_URI_MON_APIC_SUBSCRIPTION);
        subscriptionHistoryUri = apiCloudMonUri.concat(MonetizationConstants.DS_API_URI_API_SUBSCRIPTION_HISTORY);
        appSubscriptionsUri = apiCloudMonUri.concat(MonetizationConstants.DS_API_URI_APP_SUBSCRIPTIONS);
        ratePlanUrl = cloudMonUri.concat(MonetizationConstants.DS_API_URI_MONETIZATION_TENANT_RATE_PLAN);

        tiersOfTenantUri = apimRestUri.concat(BillingConstants.APIM_ADMIN_REST_URI_TENANT_THROTLING_TIERS);
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

        JsonObject responseObj = new JsonObject();
        JsonObject dataObj = new JsonObject();

        try {
            String url = subscribersUri.replace(MonetizationConstants.RESOURCE_IDENTIFIER_TENANT,
                    CloudBillingUtils.encodeUrlParam(tenantDomain))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_USERNAME,
                            CloudBillingUtils.encodeUrlParam(username));
            String dbResponse = dsBRProcessor.doGet(url, BillingConstants.HTTP_TYPE_APPLICATION_JSON, null);
            JsonElement dbResponseElement = new JsonParser().parse(dbResponse);

            if (dbResponseElement == null || dbResponseElement.isJsonPrimitive()) {
                throw new CloudMonetizationException(
                        "API Subscriber information unavailable for the User : " + username + ", For Tenant : "
                                + tenantDomain);
            }

            if (dbResponseElement.isJsonObject() && dbResponseElement.getAsJsonObject()
                    .get(MonetizationConstants.SUBSCRIBERS).isJsonPrimitive()) {
                String msg = "API Subscriber information unavailable for the User : " + username + ", For Tenant : "
                        + tenantDomain;
                LOGGER.warn(msg);
                responseObj.addProperty(MonetizationConstants.RESPONSE_SUCCESS, false);
                responseObj.addProperty(MonetizationConstants.RESPONSE_MESSAGE, msg);
                dataObj.addProperty("subscriptionInfoNotAvailable", true);
                responseObj.add(MonetizationConstants.RESPONSE_DATA, dataObj);
                return responseObj.toString();
            }

            dataObj = dbResponseElement.getAsJsonObject().get(MonetizationConstants.SUBSCRIBERS).getAsJsonObject();
            responseObj.addProperty(MonetizationConstants.RESPONSE_SUCCESS, true);
            responseObj.add(MonetizationConstants.RESPONSE_DATA, dataObj);

            return responseObj.toString();

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
     * @param accountNumber  account number
     * @param isExistingUser boolean existence of the user
     * @throws CloudMonetizationException
     */
    public static boolean updateAPISubscriberInfo(String username, String tenantDomain, boolean isTestAccount,
            String accountNumber, boolean isExistingUser) throws CloudMonetizationException {
        try {
            String url = subscribersUri.replace(MonetizationConstants.RESOURCE_IDENTIFIER_TENANT,
                    CloudBillingUtils.encodeUrlParam(tenantDomain))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_USERNAME,
                            CloudBillingUtils.encodeUrlParam(username));
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
     * Cancelling a paid API Subscription
     *
     * @param accountNumber account number
     * @param appName       application name
     * @param apiName       api name
     * @param apiVersion    api version
     * @return in success
     * {
     * "success": true,
     * "subscriptionId": "2c92c0fb51b054350151b382f3635c6f",
     * "cancelledDate": "2013-07-01",
     * "totalDeltaMrr": 0,
     * "totalDeltaTcv": null
     * }
     * <p>
     * failure
     * <p>
     * {
     * "success": false,
     * "data": "Data sent from Vendor"
     * }
     * <p>
     * When subscription data not available on databases
     * it would be
     * <p>
     * {
     * "subscriptionInfoNotAvailable":true
     * }
     * @throws CloudMonetizationException
     */
    public static String cancelSubscription(String accountNumber, String appName, String apiName, String apiVersion)
            throws CloudMonetizationException {
        JsonObject responseObj = new JsonObject();
        JsonObject dataObj = new JsonObject();

        String subscriptionInfo = getSubscriptionInfo(accountNumber, appName, apiName, apiVersion);

        JsonElement subscriptionInfoElement = new JsonParser().parse(subscriptionInfo);

        if (subscriptionInfoElement == null || subscriptionInfoElement.isJsonPrimitive()) {
            throw new CloudMonetizationException(
                    "Error while cancelling the subscription. Subscription data unavailable. Account no: "
                            + accountNumber + ", Application name: " + appName + ", Api name: " + apiName + ", Api "
                            + "version: " + apiVersion);
        }

        if (subscriptionInfoElement.isJsonObject() && subscriptionInfoElement.getAsJsonObject()
                .get(MonetizationConstants.SUBSCRIPTION).isJsonPrimitive()) {
            String msg = "Subscription information not available.";
            LOGGER.warn(msg + " Account no: " + accountNumber + ", Application name: " + appName + ", " + "Api name: "
                    + apiName + ", Api " + "version: " + apiVersion);

            responseObj.addProperty(MonetizationConstants.RESPONSE_SUCCESS, true);
            responseObj.addProperty(MonetizationConstants.RESPONSE_MESSAGE, msg);
            dataObj.addProperty("subscriptionInfoNotAvailable", true);
            responseObj.add(MonetizationConstants.RESPONSE_DATA, dataObj);
            return responseObj.toString();
        }

        JsonObject subscriptionObj = subscriptionInfoElement.getAsJsonObject().get(MonetizationConstants.SUBSCRIPTION)
                .getAsJsonObject();

        try {
            responseObj = cancelVendorSubscription(subscriptionObj);

            if (responseObj.get(MonetizationConstants.RESPONSE_SUCCESS).getAsBoolean()) {
                boolean success = removeSubscriptionsInMonDb(accountNumber, appName, apiName, apiVersion);
                responseObj.get(MonetizationConstants.RESPONSE_DATA).getAsJsonObject()
                        .addProperty(BillingConstants.MONETIZATION_DB_UPDATED, success);

            } else {
                LOGGER.error(
                        "Error while cancelling the subscription. Response From Vendor: " + responseObj.toString());
                responseObj.get(MonetizationConstants.RESPONSE_DATA).getAsJsonObject()
                        .addProperty(BillingConstants.MONETIZATION_DB_UPDATED, false);
            }
        } catch (CloudBillingException e) {
            throw new CloudMonetizationException("Error while canceling Subscription for account : " + accountNumber,
                    e);
        }
        return responseObj.toString();
    }

    private static JsonObject cancelVendorSubscription(JsonObject subscriptionObj) throws CloudBillingException {

        CloudBillingServiceProvider provider = BillingVendorInvoker.loadBillingVendorForMonetization();

        String response = provider
                .cancelSubscription(subscriptionObj.get(MonetizationConstants.SUBSCRIPTION_NUMBER).getAsString(),
                        subscriptionObj.toString());
        return new JsonParser().parse(response).getAsJsonObject();
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
            String url = subscriptionUri.replace(MonetizationConstants.RESOURCE_IDENTIFIER_ACCOUNT_NO,
                    CloudBillingUtils.encodeUrlParam(accountNumber))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_APP_NAME,
                            CloudBillingUtils.encodeUrlParam(appName))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_API_NAME,
                            CloudBillingUtils.encodeUrlParam(apiName))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_API_VERSION,
                            CloudBillingUtils.encodeUrlParam(apiVersion));

            return dsBRProcessor.doGet(url, BillingConstants.HTTP_TYPE_APPLICATION_JSON, null);
        } catch (UnsupportedEncodingException | CloudBillingException e) {
            throw new CloudMonetizationException(
                    "Error while retrieving subscription information for child account: " + accountNumber, e);
        }
    }

    /**
     * Removes the subscription information from the monetization tables
     *
     * @param accountNumber accountNumber
     * @param appName       application name
     * @param apiName       api name
     * @param apiVersion    api version
     * @return response object with "monetizationDbUpdated" attribute
     * @throws CloudMonetizationException
     */
    private static boolean removeSubscriptionsInMonDb(String accountNumber, String appName, String apiName,
            String apiVersion) throws CloudMonetizationException {

        boolean success = false;
        //TODO do following with box carring
        try {
            String subscriptionHistoryUrl = subscriptionHistoryUri
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_ACCOUNT_NO,
                            CloudBillingUtils.encodeUrlParam(accountNumber))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_APP_NAME,
                            CloudBillingUtils.encodeUrlParam(appName))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_API_NAME,
                            CloudBillingUtils.encodeUrlParam(apiName))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_API_VERSION,
                            CloudBillingUtils.encodeUrlParam(apiVersion));

            boolean response = DataServiceBillingRequestProcessor
                    .isJsonResponseSuccess(dsBRProcessor.doPost(subscriptionHistoryUrl, null, new NameValuePair[] {}));
            //TODO do following with box carring
            if (response) {
                String subscriptionUrl = subscriptionUri.replace(MonetizationConstants.RESOURCE_IDENTIFIER_ACCOUNT_NO,
                        CloudBillingUtils.encodeUrlParam(accountNumber))
                        .replace(MonetizationConstants.RESOURCE_IDENTIFIER_APP_NAME,
                                CloudBillingUtils.encodeUrlParam(appName))
                        .replace(MonetizationConstants.RESOURCE_IDENTIFIER_API_NAME,
                                CloudBillingUtils.encodeUrlParam(apiName))
                        .replace(MonetizationConstants.RESOURCE_IDENTIFIER_API_VERSION,
                                CloudBillingUtils.encodeUrlParam(apiVersion));

                response = DataServiceBillingRequestProcessor
                        .isJsonResponseSuccess(dsBRProcessor.doDelete(subscriptionUrl, null, new NameValuePair[] {}));
                if (response) {
                    success = true;
                } else {
                    //ToDo This email and manual recovering process is a temporary fix for the MVP
                    String msgBody = "Error while removing subscriptions operation failed: Updating subscription "
                            + "information in the database. Please delete the entry from "
                            + "subscriptions table.  Account no: " + accountNumber + ", Application name: " + appName
                            + ", " + "API name: " + apiName + ", API Version: " + apiVersion;
                    String msgSubject = "[Monetization][API Cloud][ALERT] Subscription removal db update failure";
                    //sending as a notification for cloud
                    CloudBillingServiceUtils.sendNotificationToCloud(msgBody, msgSubject);

                    LOGGER.error("Cancelling subscription: monetization tables update failure. Removing "
                            + "subscription from subscriptions table. Account no: " + accountNumber + ", Application "
                            + "name: " + appName + ", Api name: " + apiName + ", Api " + "version: " + apiVersion);
                }
            } else {
                //ToDo This email and manual recovering process is a temporary fix for the MVP
                String msgBody = "Error while removing subscriptions operation failed: Updating subscription "
                        + "information in the database. Please add the entry to history table and delete it from "
                        + "subscriptions table.  Account no: " + accountNumber + ", Application name: " + appName + ", "
                        + "API name: " + apiName + ", API Version: " + apiVersion;
                String msgSubject = "[Monetization][API Cloud][ALERT] Subscription removal db update failure.";
                //sending as a notification for cloud
                CloudBillingServiceUtils.sendNotificationToCloud(msgBody, msgSubject);

                LOGGER.error("Cancelling subscription: monetization tables update failure. adding to history "
                        + "and removing from subscriptions. Account no: " + accountNumber + ", Application name: "
                        + appName + ", Api name: " + apiName + ", Api " + "version: " + apiVersion);

            }

        } catch (UnsupportedEncodingException | CloudBillingException e) {
            throw new CloudMonetizationException("Error while updating api subscription history tables. ", e);
        }
        return success;
    }

    /**
     * Remove all the api subscriptions of an Application
     *
     * @param accountNumber account number
     * @param appName       application name
     * @return {
     * "removedSubscriptions": [
     * {
     * "AccountNumber": "A00000657",
     * "ApiName": "CalculatorAPI",
     * "ApiProvider": "rajith.siriw.ardana.gmail.com-AT-mustanggt350",
     * "ApiVersion": "1.0",
     * "AppName": "TESTAAA1",
     * "RatePlanId": "2c92c0f8516cc19e0151854814d367ff",
     * "StartDate": "2016-01-06T14:37:30.000+05:30",
     * "SubscriptionNumber": "A-S00000699"
     * },
     * {
     * "AccountNumber": "A00000657",
     * "ApiName": "PhoneVerify",
     * "ApiProvider": "criachae.fakeinbox.com -AT-mustanggt350",
     * "ApiVersion": "1.0.0",
     * "AppName": "TESTAAA1",
     * "RatePlanId": "2c92c0f8516cc19e0151854814d367ff",
     * "StartDate": "2016-01-06T14:43:38.000+05:30",
     * "SubscriptionNumber": "A-S00000700"
     * }
     * ],
     * "success": true
     * }
     * <p>
     * If one of the subscriptions in the application isn't removed, the "success" attribute will be set to false
     * @throws CloudMonetizationException
     */
    public static String removeAppSubscriptions(String accountNumber, String appName)
            throws CloudMonetizationException {
        JsonObject responseObj = new JsonObject();
        JsonObject dataObj = new JsonObject();

        String subscriptionsInfo = getAppSubscriptionsInfo(accountNumber, appName);

        JsonElement element = new JsonParser().parse(subscriptionsInfo);

        if (!element.isJsonObject()) {
            throw new CloudMonetizationException(
                    "Error while cancelling the subscriptions for Account: " + accountNumber + ", Application name: "
                            + appName + ". Subscription details not available.");
        }
        JsonElement subscriptionsElement = element.getAsJsonObject().get(MonetizationConstants.SUBSCRIPTIONS);

        responseObj.addProperty(MonetizationConstants.RESPONSE_SUCCESS, true);
        //Application only has free apis.
        if (subscriptionsElement.isJsonPrimitive() && subscriptionsElement.getAsString().isEmpty()) {
            return responseObj.toString();
        }

        if (!subscriptionsElement.isJsonObject()) {
            throw new CloudMonetizationException(
                    "Error while cancelling the subscriptions for Account: " + accountNumber + ", Application name: "
                            + appName + ". Subscription details not available.");
        }

        JsonElement subscriptionsJElement = subscriptionsElement.getAsJsonObject()
                .get(MonetizationConstants.SUBSCRIPTION);
        JsonArray subscriptions;
        if (subscriptionsJElement.isJsonObject()) {
            subscriptions = new JsonArray();
            subscriptions.add(subscriptionsJElement.getAsJsonObject());
        } else if (subscriptionsJElement.isJsonArray()) {
            subscriptions = subscriptionsJElement.getAsJsonArray();
        } else {
            throw new CloudMonetizationException(
                    "Error while cancelling the subscriptions for Account: " + accountNumber + ", Application name: "
                            + appName + ". Subscription element should be either " + "json  array or json object. ");
        }

        JsonArray removedSubscriptions = new JsonArray();
        JsonArray removalFailedSubscriptions = new JsonArray();

        for (JsonElement subscription : subscriptions) {
            try {
                JsonObject subscriptionObj = subscription.getAsJsonObject();
                JsonObject vendorResponseObj = cancelVendorSubscription(subscriptionObj);
                if (vendorResponseObj.get(MonetizationConstants.RESPONSE_SUCCESS).getAsBoolean()) {
                    boolean success = removeSubscriptionsInMonDb(accountNumber,
                            subscriptionObj.get(MonetizationConstants.ATTRIB_APP_NAME).getAsString(),
                            subscriptionObj.get(MonetizationConstants.ATTRIB_API_NAME).getAsString(),
                            subscriptionObj.get(MonetizationConstants.ATTRIB_API_VERSION).getAsString());
                    subscriptionObj.addProperty(BillingConstants.MONETIZATION_DB_UPDATED, success);
                    removedSubscriptions.add(subscriptionObj);
                } else {
                    LOGGER.error("Vendor Subscription cancellation failure. Account number: " + accountNumber + " , "
                            + "Subscription Id: " + subscriptionObj.get(MonetizationConstants.SUBSCRIPTION_NUMBER)
                            .getAsString() + ". Reasons: " + vendorResponseObj.get("message"));
                    subscriptionObj.addProperty(BillingConstants.MONETIZATION_DB_UPDATED, false);
                    removalFailedSubscriptions.add(subscriptionObj);
                    //Check already the success value set to false
                    if (responseObj.get(MonetizationConstants.RESPONSE_SUCCESS).getAsBoolean()) {
                        responseObj.addProperty(MonetizationConstants.RESPONSE_SUCCESS, false);
                    }
                }
            } catch (CloudBillingException e) {
                throw new CloudMonetizationException(
                        "Error while canceling Subscription for account : " + accountNumber, e);
            }
        }
        dataObj.add("removedSubscriptions", removedSubscriptions);
        if (removalFailedSubscriptions.size() > 0) {
            dataObj.add("removalFailedSubscriptions", removalFailedSubscriptions);
        }

        responseObj.add(MonetizationConstants.RESPONSE_DATA, dataObj);

        return responseObj.toString();
    }

    /**
     * Retrieve App's all api subscriptions
     *
     * @param accountNumber account number
     * @param appName       app name
     * @return json object array of subscriptions
     * @throws CloudMonetizationException
     */
    public static String getAppSubscriptionsInfo(String accountNumber, String appName)
            throws CloudMonetizationException {
        try {
            String url = appSubscriptionsUri.replace(MonetizationConstants.RESOURCE_IDENTIFIER_ACCOUNT_NO,
                    CloudBillingUtils.encodeUrlParam(accountNumber))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_APP_NAME,
                            CloudBillingUtils.encodeUrlParam(appName));

            return dsBRProcessor.doGet(url, BillingConstants.HTTP_TYPE_APPLICATION_JSON, null);
        } catch (UnsupportedEncodingException | CloudBillingException e) {
            throw new CloudMonetizationException(
                    "Error while retrieving app subscriptions information for child " + "account: " + accountNumber, e);
        }
    }

    public static String createAPISubscription(String accountNumber, String tenantDomain, String tierName,
            String appName, String apiName, String apiVersion, String apiProvider) throws CloudMonetizationException {

        Date planEffectiveDate = new Date();
        String ratePlanId = getRatePlanId(tenantDomain, tierName);
        if (StringUtils.isBlank(ratePlanId)) {
            throw new CloudMonetizationException(
                    "Tier is either not commercial or invalid; tier: " + tierName + " " + "for tenant:  "
                            + tenantDomain);
        }

        try {
            JsonObject subscriptionObj = new JsonObject();
            subscriptionObj.addProperty("plan", ratePlanId);
            subscriptionObj.addProperty("customer", accountNumber);

            CloudBillingServiceProvider provider = BillingVendorInvoker.loadBillingVendorForMonetization();
            String vendorResponse = provider.createSubscription(subscriptionObj.toString());

            JsonObject vendorResponseObj = new JsonParser().parse(vendorResponse).getAsJsonObject();

            if (vendorResponseObj != null && vendorResponseObj.isJsonObject()) {
                String mysqlFormatDate = new SimpleDateFormat(BillingConstants.DATE_TIME_FORMAT)
                        .format(planEffectiveDate);

                if (vendorResponseObj.get(MonetizationConstants.RESPONSE_SUCCESS).getAsBoolean()) {
                    JsonObject vendorDataObj = vendorResponseObj.getAsJsonObject(MonetizationConstants.RESPONSE_DATA);
                    JsonObject apiDataObj = new JsonObject();
                    apiDataObj.addProperty(MonetizationConstants.SOAP_APP_NAME, appName);
                    apiDataObj.addProperty(MonetizationConstants.SOAP_API_NAME, apiName);
                    apiDataObj.addProperty(MonetizationConstants.SOAP_API_VERSION, apiVersion);
                    apiDataObj.addProperty(MonetizationConstants.SOAP_API_PROVIDER, apiProvider);
                    apiDataObj.addProperty(BillingConstants.PARAM_RATE_PLAN_ID, ratePlanId);
                    apiDataObj.addProperty(BillingConstants.PARAM_SUBSCRIPTION_NUMBER,
                            vendorDataObj.get(BillingConstants.PARAM_SUBSCRIPTION_NUMBER).getAsString());
                    boolean addSubscriptionStatus = APICloudMonetizationUtils
                            .addSubscriptionInformation(tenantDomain, accountNumber, apiDataObj, mysqlFormatDate);

                    //ToDo This email and manual recovering process is a temporary fix for the MVP
                    if (!addSubscriptionStatus) {
                        String msgBody = "Error while updating subscription information in the database. Please update "
                                + "the subscriptions table. Tenant: " + tenantDomain + ", Account number: "
                                + accountNumber + ", Effective date: " + mysqlFormatDate + ", API data: " + apiDataObj
                                .toString();
                        String msgSubject = "[Monetization][API Cloud][ALERT] Subscription db update failure";
                        //sending as a notification for cloud
                        CloudBillingServiceUtils.sendNotificationToCloud(msgBody, msgSubject);
                        addSubscriptionStatus = false;
                    }

                    //ignore the warning this is only until the continuous delivery of messages is guaranteed
                    vendorResponseObj.getAsJsonObject(MonetizationConstants.RESPONSE_DATA)
                            .addProperty(BillingConstants.MONETIZATION_DB_UPDATED, addSubscriptionStatus);
                    return vendorResponseObj.toString();

                } else {
                    //ToDo This email and manual recovering process is a temporary fix for the MVP
                    String errorMsg = "Vendor subscription creation failure. response: {" + vendorResponseObj
                            .get(MonetizationConstants.RESPONSE_MESSAGE) + " } Tenant: " + tenantDomain
                            + ", Account number: " + accountNumber + ". User may have "
                            + "retried later. No action required unless this is a recurring issue. Please see the "
                            + "response details";
                    String msgSubject = "[Monetization][API Cloud][WARN] Subscription failure";

                    //sending as a notification for cloud
                    CloudBillingServiceUtils.sendNotificationToCloud(errorMsg, msgSubject);

                    LOGGER.error(errorMsg);
                    throw new CloudMonetizationException(errorMsg);
                }

            } else {
                String errorMsg = "Unexpected Error while creating subscription. response empty";
                LOGGER.error(errorMsg);
                throw new CloudMonetizationException(errorMsg);
            }
        } catch (CloudBillingException e) {
            String errorMsg = "Error while creating subscription." + e.getMessage();
            LOGGER.error(errorMsg, e);
            throw new CloudMonetizationException(errorMsg, e);
        }
    }

    /**
     * Get rate plan id for rate plan name
     *
     * @param tenantDomain tenant domain
     * @param ratePlanName rate plan name
     * @return rate plan id
     */
    public static String getRatePlanId(String tenantDomain, String ratePlanName) throws CloudMonetizationException {
        try {
            String productName = BillingConstants.API_CLOUD_ID;
            String response = null;
            try {
                String url = ratePlanUrl.replace(MonetizationConstants.RESOURCE_IDENTIFIER_TENANT,
                        CloudBillingUtils.encodeUrlParam(tenantDomain))
                        .replace(MonetizationConstants.PRODUCT_NAME, CloudBillingUtils.encodeUrlParam(productName))
                        .replace(MonetizationConstants.RESOURCE_IDENTIFIER_RATE_PLAN_NAME,
                                CloudBillingUtils.encodeUrlParam(ratePlanName));
                response = dsBRProcessor.doGet(url, null, null);
                OMElement elements = AXIOMUtil.stringToOM(response);

                OMElement ratePlanId = elements.getFirstChildWithName(
                        new QName(BillingConstants.DS_NAMESPACE_URI, MonetizationConstants.RATE_PLAN_ID));
                if (ratePlanId != null && StringUtils.isNotBlank(ratePlanId.getText())) {
                    return ratePlanId.getText().trim();
                } else {
                    return BillingConstants.EMPTY_STRING;
                }

            } catch (XMLStreamException | UnsupportedEncodingException e) {
                throw new CloudBillingException("Error occurred while parsing response: " + response, e);
            }
        } catch (CloudBillingException e) {
            String errorMsg =
                    "Error while getting rate plan id for tenant: " + tenantDomain + " Rate Plan name: " + ratePlanName;
            LOGGER.error(errorMsg, e);
            throw new CloudMonetizationException(errorMsg, e);
        }
    }

    /**
     * Add subscription information for child account
     *
     * @param tenantDomain  tenant domain
     * @param accountNumber account number
     * @param apiDataObj    api data json object
     * @param effectiveDate effective date
     * @return success information
     * @throws CloudMonetizationException
     */
    public static boolean addSubscriptionInformation(String tenantDomain, String accountNumber, JsonObject apiDataObj,
            String effectiveDate) throws CloudMonetizationException {
        try {
            String url = subscriptionUri.replace(MonetizationConstants.RESOURCE_IDENTIFIER_ACCOUNT_NO,
                    CloudBillingUtils.encodeUrlParam(accountNumber))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_APP_NAME, CloudBillingUtils
                            .encodeUrlParam(apiDataObj.get(MonetizationConstants.SOAP_APP_NAME).getAsString()))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_API_NAME, CloudBillingUtils
                            .encodeUrlParam(apiDataObj.get(MonetizationConstants.SOAP_API_NAME).getAsString()))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_API_VERSION, CloudBillingUtils
                            .encodeUrlParam(apiDataObj.get(MonetizationConstants.SOAP_API_VERSION).getAsString()));

            NameValuePair[] nameValuePairs = new NameValuePair[] {
                    new NameValuePair(MonetizationConstants.SOAP_API_PROVIDER,
                            apiDataObj.get(MonetizationConstants.SOAP_API_PROVIDER).getAsString()),
                    new NameValuePair(BillingConstants.PARAM_RATE_PLAN_ID,
                            apiDataObj.get(BillingConstants.PARAM_RATE_PLAN_ID).getAsString()),
                    new NameValuePair(BillingConstants.PARAM_SUBSCRIPTION_NUMBER,
                            apiDataObj.get(BillingConstants.PARAM_SUBSCRIPTION_NUMBER).getAsString()),
                    new NameValuePair(BillingConstants.PARAM_START_DATE, effectiveDate.trim()) };

            String response = dsBRProcessor.doPost(url, BillingConstants.HTTP_TYPE_APPLICATION_XML, nameValuePairs);
            return DataServiceBillingRequestProcessor.isXMLResponseSuccess(response);
        } catch (CloudBillingException | UnsupportedEncodingException | XMLStreamException e) {
            throw new CloudMonetizationException(
                    "Error while adding subscription information for child account: " + accountNumber
                            + " of the parent tenant: " + tenantDomain, e);
        }
    }

    /**
     * Retrieve the list of tiers of a given tenant
     *
     * @param tenantDomain
     * @return json string of tiers
     * @throws CloudMonetizationException
     */
    public static String getTiersOfTenant(String tenantDomain) throws CloudMonetizationException {
        try {
            Map<String, String> customHeaders = new HashMap<String, String>();
            customHeaders.put(BillingConstants.HTTP_REQ_HEADER_X_WSO2_TENANT, tenantDomain);
            return apimRestRequestProcessor
                    .doGet(tiersOfTenantUri, BillingConstants.HTTP_TYPE_APPLICATION_JSON, customHeaders, null);
        } catch (CloudBillingException e) {
            throw new CloudMonetizationException(
                    "Error occurred while calling the APIM Store Rest API for retrieving throttling tiers of tenant: "
                            + tenantDomain, e);
        }
    }

}
