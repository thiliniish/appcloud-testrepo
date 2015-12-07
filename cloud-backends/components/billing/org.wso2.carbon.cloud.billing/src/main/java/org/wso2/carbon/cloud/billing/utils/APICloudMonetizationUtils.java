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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.commons.MonetizationConstants;
import org.wso2.carbon.cloud.billing.commons.utils.BillingConfigUtils;
import org.wso2.carbon.cloud.billing.commons.utils.CloudBillingUtils;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.exceptions.CloudMonetizationException;
import org.wso2.carbon.cloud.billing.processor.BillingRequestProcessor;
import org.wso2.carbon.cloud.billing.processor.BillingRequestProcessorFactory;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Model to represent Utilities for Cloud monetization service
 */
public final class APICloudMonetizationUtils {

    /*body constants*/
    private static final String APP_NAME = "appName";
    private static final String API_NAME = "apiName";
    private static final String API_VERSION = "apiVersion";

    /*Database http request processor*/
    private static BillingRequestProcessor dsBRProcessor = BillingRequestProcessorFactory
            .getBillingRequestProcessor(BillingRequestProcessorFactory.ProcessorType.DATA_SERVICE,
                    BillingConfigUtils.getBillingConfiguration().getDSConfig().getHttpClientConfig());


    /*Data service URIs*/
    private static String subscribersUri =
            BillingConfigUtils.getBillingConfiguration().getDSConfig().getApiCloudMonetizationServiceUrl()
                    + MonetizationConstants.DS_API_URI_MON_APIC_SUBSCRIBER;
    private static String apiSubscriptionUri =
            BillingConfigUtils.getBillingConfiguration().getDSConfig().getApiCloudMonetizationServiceUrl()
                    + MonetizationConstants.DS_API_URI_UPDATE_API_SUBSCRIPTION;
    private static String subscriptionUri = BillingConfigUtils.getBillingConfiguration().getDSConfig()
            .getApiCloudMonetizationServiceUrl() + MonetizationConstants.DS_API_URI_MON_APIC_SUBSCRIPTION;

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
            String url = apiSubscriptionUri.replace(MonetizationConstants.RESOURCE_IDENTIFIER_TENANT_ID,
                    CloudBillingUtils.encodeUrlParam(tenantId));
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            NameValuePair userIdNVP = new NameValuePair(MonetizationConstants.USER_ID, userId.trim());
            NameValuePair statusNVP = new NameValuePair(MonetizationConstants.API_SUBSCRIPTION_STATUS,
                    MonetizationConstants.API_SUBSCRIPTION_BLOCKED_STATUS);
            nameValuePairs.add(userIdNVP);
            nameValuePairs.add(statusNVP);
            dsBRProcessor.doPut(url, nameValuePairs.toArray(new NameValuePair[nameValuePairs.size()]));
        } catch (CloudBillingException | UnsupportedEncodingException e) {
            throw new CloudMonetizationException("Error while sending block subscriptions request to data service for" +
                    " user :" + userId + " tenant Id :" + tenantId, e);
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
    public static String addSubscriptionInformation(String tenantDomain, String accountNumber, String apiData,
                                                    String effectiveDate) throws CloudMonetizationException {
        JsonObject apiDataObj = new JsonParser().parse(apiData).getAsJsonObject();
        try {
            String url = subscriptionUri
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_ACCOUNT_NO, CloudBillingUtils
                            .encodeUrlParam(accountNumber))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_APP_NAME, CloudBillingUtils
                            .encodeUrlParam(apiDataObj.get(APP_NAME).getAsString()))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_API_NAME, CloudBillingUtils
                            .encodeUrlParam(apiDataObj.get(API_NAME).getAsString()))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_API_VERSION, CloudBillingUtils
                            .encodeUrlParam(apiDataObj.get(API_VERSION).getAsString()));

            NameValuePair[] nameValuePairs = new NameValuePair[]{
                    new NameValuePair(BillingConstants.PARAM_RATE_PLAN_ID, apiDataObj.get(BillingConstants
                            .PARAM_RATE_PLAN_ID).getAsString()),
                    new NameValuePair(BillingConstants.PARAM_SUBSCRIPTION_ID, apiDataObj.get(BillingConstants
                            .PARAM_SUBSCRIPTION_ID).getAsString()),
                    new NameValuePair(BillingConstants.PARAM_START_DATE, effectiveDate.trim())
            };

            return dsBRProcessor.doPost(url, nameValuePairs);
        } catch (CloudBillingException | UnsupportedEncodingException e) {
            throw new CloudMonetizationException("Error while adding subscription information for child account: " +
                    accountNumber + " of the parent tenant: " + tenantDomain, e);
        }
    }
}