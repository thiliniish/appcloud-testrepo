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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
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
     * @param username subscriber username
     * @param tenantDomain tenant domain
     * @return String xml response
     * @throws CloudMonetizationException
     */
    public static String getAPISubscriberInfo(String username, String tenantDomain) throws CloudMonetizationException {

        try {
            String url = subscribersUrl.replace(MonetizationConstants.RESOURCE_IDENTIFIER_TENANT,
                    URLEncoder.encode(tenantDomain, BillingConstants.ENCODING))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_USERNAME,
                            URLEncoder.encode(username, BillingConstants.ENCODING));
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
     * @param username subscriber username
     * @param tenantDomain tenant domain
     * @param isTestAccount boolean test account or not
     * @param accountNumber zuora account number
     * @throws CloudMonetizationException
     */
    public static void updateAPISubscriberInfo(String username, String tenantDomain, boolean isTestAccount,
            String accountNumber) throws CloudMonetizationException {
        try {
            String url = subscribersUrl.replace(MonetizationConstants.RESOURCE_IDENTIFIER_TENANT,
                    URLEncoder.encode(tenantDomain, BillingConstants.ENCODING))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_USERNAME,
                            URLEncoder.encode(username, BillingConstants.ENCODING));
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            NameValuePair testAccountNVP = new NameValuePair(IS_TEST_ACCOUNT, String.valueOf(isTestAccount));
            nameValuePairs.add(testAccountNVP);

            if (StringUtils.isNotBlank(accountNumber)) {
                NameValuePair accountNumberNVP = new NameValuePair(ACCOUNT_NUMBER, accountNumber);
                nameValuePairs.add(accountNumberNVP);
            }
            dsBRProcessor.doPost(url, nameValuePairs.toArray(new NameValuePair[nameValuePairs.size()]));
        } catch (CloudBillingException | UnsupportedEncodingException e) {
            throw new CloudMonetizationException(
                    "Error while retrieving API subscribers for user: " + username + " tenant domain: " + tenantDomain,
                    e);
        }
    }

    public static void blockApiSubscriptionsOfUser(String userId, String tenantId) throws CloudBillingException {
        JSONObject payLoad = new JSONObject();
        JSONObject params = new JSONObject();
        try {
            //TODO use an api to update apim databases instead of using a data service.
            params.put(MonetizationConstants.USER_ID, userId);
            params.put(MonetizationConstants.API_SUBSCRIPTION_STATUS,
                    MonetizationConstants.API_SUBSCRIPTION_BLOCKED_STATUS);
            params.put(MonetizationConstants.TENANT_ID, tenantId);
            payLoad.put("putv1_apim_subscription_subscriptions", params);
            String jsonResponse = dsBRProcessor.doPut(updateApiSubscriptionUrl, payLoad.toString());
            String response = new JSONObject(jsonResponse).get(MonetizationConstants.DS_API_UPDATE_SUBSCRIPTION_STATUS)
                    .toString();
            if (!StringUtils.equals(response, MonetizationConstants.SUCCESSFUL)) {
                throw new CloudBillingException("Blocking api subscriptions of user :" + userId + " is unsuccessful.");
            }
        } catch (JSONException e) {
            throw new CloudBillingException("Error while sending update request to data service.", e);
        }
    }
}
