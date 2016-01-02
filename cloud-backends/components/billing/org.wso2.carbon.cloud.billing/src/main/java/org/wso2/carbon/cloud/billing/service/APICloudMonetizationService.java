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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
     * @return <subscribers xmlns="http://ws.wso2.org/dataservice">
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
            String errorMsg = "Error while getting rate plan id for tenant: " + tenantDomain  + " Rate Plan name: " +
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
     * @param accountNumber account number
     * @param appName       application name
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
     * @return response json object.
     * @throws CloudMonetizationException
     */
    public String createAPISubscription(String accountNumber, String tenantDomain, String tierName, String appName,
                                        String apiName, String apiVersion) throws CloudMonetizationException {
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
                    apiDataObj.addProperty(BillingConstants.PARAM_RATE_PLAN_ID, ratePlanId);
                    apiDataObj.addProperty(BillingConstants.PARAM_SUBSCRIPTION_NUMBER, zuoraResObj.get
                            (BillingConstants.PARAM_SUBSCRIPTION_NUMBER).getAsString());
                    boolean addSubscriptionStatus = APICloudMonetizationUtils.addSubscriptionInformation(tenantDomain,
                            accountNumber, apiDataObj, (new SimpleDateFormat(BillingConstants.DATE_TIME_FORMAT))
                                    .format(planEffectiveDate));
                    zuoraResObj.addProperty(BillingConstants.DB_TABLES_UPDATED, addSubscriptionStatus);
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
}
