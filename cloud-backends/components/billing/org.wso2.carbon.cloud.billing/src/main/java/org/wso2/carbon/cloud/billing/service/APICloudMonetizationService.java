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

import org.json.simple.JSONArray;
import org.wso2.carbon.cloud.billing.commons.zuora.ZuoraRESTUtils;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.exceptions.CloudMonetizationException;
import org.wso2.carbon.cloud.billing.utils.APICloudMonetizationUtils;

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
     * <subscriber>
     * <tenant xmlns="http://ws.wso2.org/dataservice">fordmustang</tenant>
     * <username xmlns="http://ws.wso2.org/dataservice">methiapr.fakeinbox.com@fordmustang</username>
     * <testAccount xmlns="http://ws.wso2.org/dataservice">false</testAccount>
     * <accountNumber xmlns="http://ws.wso2.org/dataservice" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     * xsi:nil="true"/>
     * </subscriber>
     * </subscribers>
     * @throws CloudMonetizationException
     */
    public String getAPISubscriberInfo(String username, String tenantDomain) throws CloudMonetizationException {
        return APICloudMonetizationUtils.getAPISubscriberInfo(username, tenantDomain);
    }

    /**
     * Insert into subscriber information
     *
     * @param username subscriber user name
     * @param tenantDomain tenant domain
     * @param isTestAccount boolean test account or not
     * @param accountNumber account number. this would be null for non paid subscribers
     * @throws CloudMonetizationException
     */
    public void updateAPISubscriberInfo(String username, String tenantDomain, boolean isTestAccount,
            String accountNumber) throws CloudMonetizationException {
        APICloudMonetizationUtils.updateAPISubscriberInfo(username, tenantDomain, isTestAccount, accountNumber);
    }

    /**
     * Retrieve active subscriptions for a given account id
     *
     * @param accountId customer accountId
     * @param serviceName cloud service name
     * @return Json string of active subscription ids
     * @throws CloudBillingException
     */
    public JSONArray getActiveSubscriptionIdsForAccountId(String accountId, String serviceName)
            throws CloudBillingException {
        try {
            return ZuoraRESTUtils.getActiveSubscriptionIdsForAccountId(accountId, serviceName);
        } catch (CloudBillingException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CloudBillingException(
                    "Error occurred while retrieving active subscription ids for account ID: " + accountId);
        }
    }

    /**
     * Block api subscriptions of a given user
     *
     * @param userId user id of the user
     * @param tenantId tenant id
     * @throws CloudMonetizationException
     */
    public void blockApiSubscriptionsOfUser(String userId, String tenantId) throws CloudMonetizationException {
        try {
            APICloudMonetizationUtils.blockApiSubscriptionsOfUser(userId, tenantId);
        } catch (CloudMonetizationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CloudMonetizationException("Error occurred while blocking api subscriptions of user : " + userId);
        }
    }
}
