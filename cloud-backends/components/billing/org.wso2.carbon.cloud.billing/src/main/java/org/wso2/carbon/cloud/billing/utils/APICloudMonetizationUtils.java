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

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.commons.MonetizationConstants;
import org.wso2.carbon.cloud.billing.commons.utils.BillingConfigUtils;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.exceptions.CloudMonetizationException;
import org.wso2.carbon.cloud.billing.processor.BillingRequestProcessor;
import org.wso2.carbon.cloud.billing.processor.BillingRequestProcessorFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Model to represent Utilities for Cloud monetization service
 */
public class APICloudMonetizationUtils {

    private static final String IS_TEST_ACCOUNT = "isTestAccount";
    private static final String ACCOUNT_NUMBER = "accountNumber";
    private static BillingRequestProcessor dsBRProcessor = BillingRequestProcessorFactory
            .getBillingRequestProcessor(BillingRequestProcessorFactory.ProcessorType.DATA_SERVICE,
                    BillingConfigUtils.getBillingConfiguration().getDSConfig().getHttpClientConfig());
    private static String subscribersUrl =
            BillingConfigUtils.getBillingConfiguration().getDSConfig().getApiCloudMonetizationServiceUrl()
                    + MonetizationConstants.DS_API_URI_MON_APIC_SUBSCRIBER;
    private static String updateApiSubscriptionUrl =
            BillingConfigUtils.getBillingConfiguration().getDSConfig().getApiCloudMonetizationServiceUrl()
                    + MonetizationConstants.DS_API_URI_UPDATE_API_SUBSCRIPTION;

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
            String url = subscribersUrl.replace(MonetizationConstants.RESOURCE_IDENTIFIER_TENANT,
                    URLEncoder.encode(tenantDomain.trim(), BillingConstants.ENCODING))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_USERNAME,
                            URLEncoder.encode(username.trim(), BillingConstants.ENCODING));
            return dsBRProcessor.doGet(url, null);
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
    public static void updateAPISubscriberInfo(String username, String tenantDomain, boolean isTestAccount,
                                               String accountNumber, boolean isExistingUser) throws CloudMonetizationException {
        try {
            String url = subscribersUrl.replace(MonetizationConstants.RESOURCE_IDENTIFIER_TENANT,
                    URLEncoder.encode(tenantDomain.trim(), BillingConstants.ENCODING))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_USERNAME,
                            URLEncoder.encode(username.trim(), BillingConstants.ENCODING));
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            NameValuePair testAccountNVP = new NameValuePair(IS_TEST_ACCOUNT, String.valueOf(isTestAccount));
            nameValuePairs.add(testAccountNVP);

            if (StringUtils.isNotBlank(accountNumber)) {
                NameValuePair accountNumberNVP = new NameValuePair(ACCOUNT_NUMBER, accountNumber.trim());
                nameValuePairs.add(accountNumberNVP);
            }
            if (!isExistingUser) {
                dsBRProcessor.doPost(url, nameValuePairs.toArray(new NameValuePair[nameValuePairs.size()]));
            } else {
                dsBRProcessor.doPut(url, nameValuePairs.toArray(new NameValuePair[nameValuePairs.size()]));
            }
        } catch (CloudBillingException | UnsupportedEncodingException e) {
            throw new CloudMonetizationException(
                    "Error while retrieving API subscribers for user: " + username + " tenant domain: " + tenantDomain,
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
            String url = updateApiSubscriptionUrl.replace(MonetizationConstants.RESOURCE_IDENTIFIER_TENANT_ID,
                    URLEncoder.encode(tenantId.trim(), BillingConstants.ENCODING));
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            NameValuePair userIdNVP = new NameValuePair(MonetizationConstants.USER_ID, userId.trim());
            NameValuePair statusNVP = new NameValuePair(MonetizationConstants.API_SUBSCRIPTION_STATUS,
                    MonetizationConstants.API_SUBSCRIPTION_BLOCKED_STATUS);
            nameValuePairs.add(userIdNVP);
            nameValuePairs.add(statusNVP);
            dsBRProcessor.doPut(url, nameValuePairs.toArray(new NameValuePair[nameValuePairs.size()]));
        } catch (CloudBillingException | UnsupportedEncodingException e) {
            throw new CloudMonetizationException(
                    "Error while sending block subscriptions request to data service for user :" + userId
                            + " tenant Id :" + tenantId, e);
        }
    }
}
