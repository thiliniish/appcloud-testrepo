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

package org.wso2.carbon.cloud.billing.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.commons.MonetizationConstants;
import org.wso2.carbon.cloud.billing.commons.zuora.ZuoraRESTUtils;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.exceptions.CloudMonetizationException;
import org.wso2.carbon.cloud.billing.utils.APICloudMonetizationUtils;
import org.wso2.carbon.cloud.billing.utils.CloudBillingServiceUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * API Cloud monetization service.
 */
public class APICloudMonetizationService {

    private static final Log LOGGER = LogFactory.getLog(CloudBillingService.class);

    /**
     * Retrieve subscriber information from
     *
     * @param username     username of the subscriber
     * @param tenantDomain tenant domain
     * @return
     * {
     * 	"Subscribers": {
     * 		"Subscriber": {
     * 		    "Tenant": "chargerhellcat"
     * 		    "Username": "kaiphaes.fakeinbox.com"
     * 		    "TestAccount": "false"
     * 		    "AccountNumber": "A00000622"
     * 		}
     * 	}
     * }
     *
     * if AccountNumber is null then
     * <p/>
     * {
     * 	"Subscribers": {
     * 		"Subscriber": {
     * 		    "Tenant": "chargerhellcat"
     * 		    "Username": "kaiphaes.fakeinbox.com"
     * 		    "TestAccount": "false"
     * 		    "AccountNumber": {"@nil": "true"}
     * 		}
     * 	}
     * }
     * @throws CloudMonetizationException
     */
    public String getAPISubscriberInfo(String username, String tenantDomain) throws CloudMonetizationException {
        try {
            return APICloudMonetizationUtils.getAPISubscriberInfo(username, tenantDomain);
        } catch (CloudMonetizationException ex) {
            LOGGER.error("Error while getting subscriber information. Tenant: " + tenantDomain + " subscriber: " +
                    username, ex);
            throw ex;
        }
    }

    /**
     * Insert into subscriber information
     *
     * @param username      subscriber user name
     * @param tenantDomain  tenant domain
     * @param isTestAccount boolean test account or not
     * @param accountNumber account number. this would be null for non paid subscribers
     * @throws CloudMonetizationException
     */
    public void addAPISubscriberInfo(String username, String tenantDomain, boolean isTestAccount,
                                     String accountNumber) throws CloudMonetizationException {
        try {
            APICloudMonetizationUtils.updateAPISubscriberInfo(username, tenantDomain, isTestAccount, accountNumber, false);
        } catch (CloudMonetizationException ex) {
            LOGGER.error("Error while adding subscriber information. Tenant: " + tenantDomain + " Subscriber: " +
                    username + " Account number: " + accountNumber, ex);
            throw ex;
        }
    }

    /**
     * update subscriber information
     *
     * @param username      subscriber user name
     * @param tenantDomain  tenant domain
     * @param isTestAccount boolean test account or not
     * @param accountNumber account number. this would be null for non paid subscribers
     * @throws CloudMonetizationException
     */
    public boolean updateAPISubscriberInfo(String username, String tenantDomain, boolean isTestAccount,
                                           String accountNumber) throws CloudMonetizationException {
        try {
            return APICloudMonetizationUtils.updateAPISubscriberInfo(username, tenantDomain, isTestAccount, accountNumber, true);
        } catch (CloudMonetizationException ex) {
            LOGGER.error("Error while updating subscriber information. Tenant: " + tenantDomain + " Subscriber: " +
                    username + " Account No: " + accountNumber, ex);
            throw ex;
        }
    }

    /**
     * Check whether monetization enabled for API Cloud
     *
     * @param tenantDomain tenant domain
     * @return boolean enabled status
     */
    public boolean isMonetizationEnabled(String tenantDomain) throws CloudMonetizationException {
        try {
            return CloudBillingServiceUtils.isMonetizationEnabled(tenantDomain, BillingConstants.API_CLOUD_SUBSCRIPTION_ID);
        } catch (CloudBillingException e) {
            String errorMsg = "Error while checking monetization status for tenant: " + tenantDomain;
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
    public String getRatePlanId(String tenantDomain, String ratePlanName) throws CloudMonetizationException {
        try {
            String zuoraProductName = tenantDomain + "_" + BillingConstants.API_CLOUD_SUBSCRIPTION_ID;
            return CloudBillingServiceUtils.getRatePlanId(tenantDomain, zuoraProductName, ratePlanName);
        } catch (CloudBillingException e) {
            String errorMsg = "Error while getting rate plan id for tenant: " + tenantDomain + " Rate Plan name: " +
                    ratePlanName;
            LOGGER.error(errorMsg, e);
            throw new CloudMonetizationException(errorMsg, e);
        }
    }

    /**
     * Retrieve active subscriptions for a given account id
     *
     * @param accountId   customer accountId
     * @param serviceName cloud service name
     * @return Json string of active subscription ids
     * @throws CloudBillingException
     */
    public JSONArray getActiveSubscriptionIdsForAccountId(String accountId, String serviceName)
            throws CloudBillingException {
        try {
            return ZuoraRESTUtils.getActiveSubscriptionIdsForAccountId(accountId, serviceName);
        } catch (CloudBillingException ex) {
            LOGGER.error("Error occurred while retrieving active subscription ids for account ID: " + accountId, ex);
            throw ex;
        }
    }

    /**
     * Block api subscriptions of a given user
     *
     * @param userId   user id of the user
     * @param tenantId tenant id
     * @throws CloudMonetizationException
     */
    public void blockApiSubscriptionsOfUser(String userId, String tenantId) throws CloudMonetizationException {
        try {
            APICloudMonetizationUtils.blockApiSubscriptionsOfUser(userId, tenantId);
        } catch (CloudMonetizationException ex) {
            LOGGER.error("Error occurred while blocking api subscriptions of user: " + userId, ex);
            throw ex;
        }
    }

    /**
     * @param tenantDomain  tenant domain
     * @param accountNumber account number
     * @param apiData       api data json object
     * @param effectiveDate effective date
     * @return success information
     * @throws CloudMonetizationException
     */
    public boolean addSubscriptionInformation(String tenantDomain, String accountNumber, String apiData,
                                              String effectiveDate) throws CloudMonetizationException {
        try {
            JsonObject apiDataObj = new JsonParser().parse(apiData).getAsJsonObject();
            return APICloudMonetizationUtils.addSubscriptionInformation(tenantDomain, accountNumber, apiDataObj, effectiveDate);
        } catch (CloudMonetizationException ex) {
            LOGGER.error("Error while adding subscription information. Tenant: " + tenantDomain + " Account no: " +
                    accountNumber + " Api data: " + apiData, ex);
            throw ex;
        }
    }

    /**
     * Get api subscription information
     *
     * @param accountNumber account number
     * @param appName       application name
     * @param apiName       api name
     * @param apiVersion    api version
     * @return response json string
     */
    public String getSubscriptionInfo(String accountNumber, String appName, String apiName, String apiVersion)
            throws CloudMonetizationException {
        try {
            return APICloudMonetizationUtils.getSubscriptionInfo(accountNumber, appName, apiName, apiVersion);
        } catch (CloudMonetizationException ex) {
            LOGGER.error("Error while retrieving subscription information. Account no: " + accountNumber + " " +
                    "Application name: " + appName + " Api name: " + apiName + " Api version: " + apiVersion, ex);
            throw ex;
        }
    }

    /**
     * Cancel API paid subscription
     *
     * @param accountNumber account number
     * @param appName       application name
     * @param apiName       api name
     * @param apiVersion    api version
     * @return cancel subscription status
     * In success
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
    public String cancelSubscription(String accountNumber, String appName, String apiName, String apiVersion)
            throws CloudMonetizationException {
        try {
            return APICloudMonetizationUtils.cancelSubscription(accountNumber, appName, apiName, apiVersion);
        } catch (CloudMonetizationException ex) {
            LOGGER.error("Error while cancelling the subscription. Account no: " + accountNumber + " Application " +
                    "name: " + appName + " Api name: " + apiName + " Api version: " + apiVersion, ex);
            throw ex;
        }
    }

    /**
     * Remove Application related api subscriptions
     *
     * @param accountNumber subscriber account number
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
     *
     * @throws CloudMonetizationException
     */
    public String removeAppSubscriptions(String accountNumber, String appName) throws CloudMonetizationException {
        try {
            return APICloudMonetizationUtils.removeAppSubscriptions(accountNumber, appName);
        } catch (CloudMonetizationException ex) {
            LOGGER.error("Error while removing application subscription. Account no: " + accountNumber + " " +
                    "Application name: " + appName, ex);
            throw ex;
        }
    }

    /**
     * Handles the commercial API subscription flows of subscribers who already has paid accounts.
     *
     * @param accountNumber subscriber account number
     * @param tenantDomain  tenant domain
     * @param tierName      subscribing tier name
     * @param appName       application name
     * @param apiName       api name
     * @param apiVersion    api version
     * @param apiProvider   api provider
     * @return response json object.
     * @throws CloudMonetizationException
     */
    public String createAPISubscription(String accountNumber, String tenantDomain, String tierName, String appName,
                                        String apiName, String apiVersion, String apiProvider) throws
            CloudMonetizationException {
        Date planEffectiveDate = new Date();
        String ratePlanId = getRatePlanId(tenantDomain, tierName);
        if (StringUtils.isBlank(ratePlanId)) {
            throw new CloudMonetizationException("Tier is either not commercial or invalid; tier: " + tierName + " " +
                    "for tenant:  " + tenantDomain);
        }
        try {
            String zuoraResponse = ZuoraRESTUtils.createSubscription(accountNumber, ratePlanId, planEffectiveDate);
            JsonObject zuoraResObj = new JsonParser().parse(zuoraResponse).getAsJsonObject();
            if (zuoraResObj != null && zuoraResObj.isJsonObject()) {
                if (zuoraResObj.get(BillingConstants.ZUORA_RESPONSE_SUCCESS).getAsBoolean()) {
                    JsonObject apiDataObj = new JsonObject();
                    apiDataObj.addProperty(MonetizationConstants.SOAP_APP_NAME, appName);
                    apiDataObj.addProperty(MonetizationConstants.SOAP_API_NAME, apiName);
                    apiDataObj.addProperty(MonetizationConstants.SOAP_API_VERSION, apiVersion);
                    apiDataObj.addProperty(MonetizationConstants.SOAP_API_PROVIDER, apiProvider);
                    apiDataObj.addProperty(BillingConstants.PARAM_RATE_PLAN_ID, ratePlanId);
                    apiDataObj.addProperty(BillingConstants.PARAM_SUBSCRIPTION_NUMBER, zuoraResObj.get
                            (BillingConstants.PARAM_SUBSCRIPTION_NUMBER).getAsString());
                    boolean addSubscriptionStatus = APICloudMonetizationUtils.addSubscriptionInformation(tenantDomain,
                            accountNumber, apiDataObj, (new SimpleDateFormat(BillingConstants.DATE_TIME_FORMAT))
                                    .format(planEffectiveDate));
                    zuoraResObj.addProperty(BillingConstants.MONETIZATION_DB_UPDATED, addSubscriptionStatus);
                    return zuoraResObj.toString();

                } else {
                    String errorMsg = "Zuora subscription creation failure. response: " + zuoraResObj.get("reasons");
                    LOGGER.error(errorMsg);
                    throw new CloudMonetizationException(errorMsg);
                }

            } else {
                String errorMsg = "Unexpected Error while creating zuora subscription. response empty";
                LOGGER.error(errorMsg);
                throw new CloudMonetizationException(errorMsg);
            }
        } catch (CloudBillingException e) {
            String errorMsg = "Error while creating zuora subscription.";
            LOGGER.error(errorMsg, e);
            throw new CloudMonetizationException(errorMsg, e);
        }
    }

    /**
     * @param tenantDomain    tenant domain
     * @param userId          user id
     * @param api             api name with version
     * @param version         api version
     * @param applicationName application name
     * @param startDate       date range - start date
     * @param endDate         date range - end date
     * @return JSON object of usage data
     * @throws CloudMonetizationException
     */
    public JSONObject getTenantMonetizationUsageDataForGivenDateRange(String tenantDomain, String userId, String api,
                                                                      String version, String applicationName, String startDate, String endDate)
            throws CloudMonetizationException {
        try {
            return APICloudMonetizationUtils
                    .getTenantMonetizationUsageDataForGivenDateRange(tenantDomain, userId, api, version,
                            applicationName, startDate, endDate);
        } catch (CloudMonetizationException ex) {
            throw new CloudMonetizationException(
                    "Error occurred while retrieving monetization usage data of tenant: " + tenantDomain);
        }
    }

    /**
     * Get APIs for a given user from API Stat tables.
     *
     * @param username
     * @return JSON object of api names
     * @throws CloudMonetizationException
     */
    public String getUserAPIs(String username) throws CloudMonetizationException {
        try {
            return APICloudMonetizationUtils.getUserAPIs(username);
        } catch (CloudMonetizationException ex) {
            LOGGER.error("Error while getting API list for the user: " + username, ex);
            throw ex;
        }
    }

    /**
     * Get the list of applications by looking at the user name and api name from API Stat tables
     *
     * @param username
     * @param apiName API Name
     * @return JSON object of Application names
     * @throws CloudMonetizationException
     */
    public String getUserAPIApplications(String username, String apiName) throws CloudMonetizationException {
        try {
            return APICloudMonetizationUtils.getUserAPIApplications(username, apiName);
        } catch (CloudMonetizationException ex) {
            LOGGER.error("Error while getting Application list for the user: " + username, ex);
            throw ex;
        }
    }

    /**
     * Retrieve rate plans information for api cloud for tenant
     *
     * @param tenantDomain tenant
     * @return rate plan information in json
     *[
     *    {
     *        "MaxDailyUsage": "10000",
     *        "MonthlyRental": "5000.0",
     *        "OverUsageUnits": "1000",
     *        "OverUsageUnitsPrice": "5.0",
     *        "RatePlanName": "Gold"
     *    },
     *    {
     *        "MaxDailyUsage": "10000",
     *        "MonthlyRental": "7000.0",
     *        "OverUsageUnits": "1000",
     *        "OverUsageUnitsPrice": "5.0",
     *        "RatePlanName": "Platinum"
     *    }
     *]
     * @throws CloudMonetizationException
     */
    public String getRatePlansInfo(String tenantDomain) throws CloudMonetizationException {
        try {
            String ratePlans = APICloudMonetizationUtils.getRatePlanInfo(tenantDomain);
            if (StringUtils.isBlank(ratePlans)) {
                return new JsonArray().toString();
            }

            JsonElement jsonElement = new JsonParser().parse(ratePlans);

            if (!jsonElement.isJsonObject()) {
                return new JsonArray().toString();
            }
            JsonElement entries = jsonElement.getAsJsonObject().get(MonetizationConstants.ENTRY);

            if (entries == null || !entries.isJsonObject()) {
                return new JsonArray().toString();
            }

            JsonElement ratePlanElements = entries.getAsJsonObject().get(MonetizationConstants.RATE_PLANS);

            if (ratePlanElements == null) {
                return new JsonArray().toString();
            }

            if (ratePlanElements.isJsonObject()) {
                JsonArray ratePlansArray = new JsonArray();
                ratePlansArray.add(ratePlanElements.getAsJsonObject());
                return ratePlansArray.toString();
            } else if (ratePlanElements.isJsonArray()) {
                return ratePlanElements.getAsJsonArray().toString();
            } else {
                return new JsonArray().toString();
            }

        } catch (CloudMonetizationException ex) {
            LOGGER.error("Error while getting Rate plans for tenant: " + tenantDomain, ex);
            throw ex;
        }
    }
}
