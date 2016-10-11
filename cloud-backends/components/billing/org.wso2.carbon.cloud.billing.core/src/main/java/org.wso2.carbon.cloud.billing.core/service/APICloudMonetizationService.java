/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.cloud.billing.core.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.billing.core.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.core.exceptions.CloudMonetizationException;
import org.wso2.carbon.cloud.billing.core.utils.APICloudMonetizationUtils;

/**
 * API Cloud monetization service.
 */
public class APICloudMonetizationService {

    /**
     * We have enforce logging and throwing exceptions in service methods as this service class is intended to be used
     * as a web service and also invoked by jaggery code via osgi service. For jaggery code in the module layer to
     * generate the error or success message, its important to catch the exception.
     */

    private static final Log LOGGER = LogFactory.getLog(CloudBillingService.class);

    /**
     * Retrieve subscriber information from
     *
     * @param username     username of the subscriber
     * @param tenantDomain tenant domain
     * @return
     * {
     *  "Subscribers": {
     *      "Subscriber": {
     *          "Tenant": "chargerhellcat"
     *          "Username": "kaiphaes.fakeinbox.com"
     *          "TestAccount": "false"
     *          "AccountNumber": "xxxxxxxxxxxxxx"
     *      }
     *  }
     * }
     * <p>
     * if AccountNumber is null then
     * <p/>
     * {
     *  "Subscribers": {
     *      "Subscriber": {
     *          "Tenant": "chargerhellcat"
     *          "Username": "kaiphaes.fakeinbox.com"
     *          "TestAccount": "false"
     *          "AccountNumber": {"@nil": "true"}
     *      }
     *  }
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
    public void addAPISubscriberInfo(String username, String tenantDomain, boolean isTestAccount, String accountNumber)
            throws CloudMonetizationException {
        try {
            APICloudMonetizationUtils
                    .updateAPISubscriberInfo(username, tenantDomain, isTestAccount, accountNumber, false);
        } catch (CloudMonetizationException ex) {
            LOGGER.error("Error while adding subscriber information. Tenant: " + tenantDomain + " Subscriber: " +
                    username + " Account number: " + accountNumber, ex);
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
     *  "data":  { Vendor Data },
     *  "monetizationDbUpdated" : true
     * }
     * <p>
     * failure
     * <p>
     * {
     *  "success": false,
     *  "message": "Error Message",
     *  "data": { Vendor Data },
     *  "monetizationDbUpdated" : true
     * }
     * <p>
     * When subscription data not available on databases
     * it would be
     * <p>
     * {
     *  "subscriptionInfoNotAvailable":true
     * }
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
     * @param appName       application name
     * @return {
     *      "removedSubscriptions": [
     *          {
     *              "AccountNumber": "A00000657",
     *              "ApiName": "CalculatorAPI",
     *              "ApiProvider": "rajith.siriw.ardana.gmail.com-AT-mustanggt350",
     *              "ApiVersion": "1.0",
     *              "AppName": "TESTAAA1",
     *              "RatePlanId": "2c92c0f8516cc19e0151854814d367ff",
     *              "StartDate": "2016-01-06T14:37:30.000+05:30",
     *              "SubscriptionNumber": "A-S00000699"
     *          },
     *          {
     *              "AccountNumber": "A00000657",
     *              "ApiName": "PhoneVerify",
     *              "ApiProvider": "criachae.fakeinbox.com -AT-mustanggt350",
     *              "ApiVersion": "1.0.0",
     *              "AppName": "TESTAAA1",
     *              "RatePlanId": "2c92c0f8516cc19e0151854814d367ff",
     *              "StartDate": "2016-01-06T14:43:38.000+05:30",
     *              "SubscriptionNumber": "A-S00000700"
     *          }
     *      ],
     *      "success": true
     *  }
     * <p>
     * If one of the subscriptions in the application isn't removed, the "success" attribute will be set to false
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
     * Retrieve available tiers of the tenant
     *
     * @param tenantDomain tenant
     * @return Tiers information in a json string
     * @throws CloudMonetizationException
     */
    public String getTiersOfTenant(String tenantDomain) throws CloudMonetizationException {
        try {
            return APICloudMonetizationUtils.getTiersOfTenant(tenantDomain);
        } catch (CloudMonetizationException ex) {
            LOGGER.error("Error occurred while retrieving throttling tiers of tenant: " + tenantDomain, ex);
            throw ex;
        }
    }
}
