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

import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.commons.zuora.ZuoraRESTUtils;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.exceptions.CloudMonetizationException;
import org.wso2.carbon.cloud.billing.utils.APICloudMonetizationUtils;
import org.wso2.carbon.cloud.billing.utils.CloudBillingServiceUtils;

/**
 * API Cloud monetization service.
 */
public class APICloudMonetizationService {

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
     *
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
        return APICloudMonetizationUtils.getAPISubscriberInfo(username, tenantDomain);
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
        APICloudMonetizationUtils.updateAPISubscriberInfo(username, tenantDomain, isTestAccount, accountNumber, false);
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
        return APICloudMonetizationUtils.updateAPISubscriberInfo(username, tenantDomain, isTestAccount, accountNumber, true);
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
            throw new CloudMonetizationException("Error while checking monetization status for tenant: " +
                    tenantDomain, e);
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
            return CloudBillingServiceUtils.getRatePlanId(tenantDomain, ratePlanName);
        } catch (CloudBillingException e) {
            throw new CloudMonetizationException("Error while getting rate plan id for tenant: " + tenantDomain, e);
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
            throw new CloudBillingException(
                    "Error occurred while retrieving active subscription ids for account ID: " + accountId);
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
            throw new CloudMonetizationException("Error occurred while blocking api subscriptions of user : " + userId);
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
        return APICloudMonetizationUtils.addSubscriptionInformation(tenantDomain, accountNumber, apiData, effectiveDate);
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
}
