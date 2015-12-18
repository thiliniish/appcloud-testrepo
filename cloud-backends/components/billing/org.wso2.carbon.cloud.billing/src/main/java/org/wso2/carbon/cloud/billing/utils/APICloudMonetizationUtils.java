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
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.commons.MonetizationConstants;
import org.wso2.carbon.cloud.billing.commons.utils.BillingConfigUtils;
import org.wso2.carbon.cloud.billing.commons.utils.CloudBillingUtils;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.exceptions.CloudMonetizationException;
import org.wso2.carbon.cloud.billing.processor.BillingRequestProcessor;
import org.wso2.carbon.cloud.billing.processor.BillingRequestProcessorFactory;
import org.wso2.carbon.cloud.billing.processor.DataServiceBillingRequestProcessor;

import javax.xml.stream.XMLStreamException;
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
    private static String usageOfApiUrl =
            BillingConfigUtils.getBillingConfiguration().getDSConfig().getApiCloudMonetizationServiceUrl()
                    + MonetizationConstants.DS_API_URI_API_USAGE;
    private static String usageOfSubscriberUrl =
            BillingConfigUtils.getBillingConfiguration().getDSConfig().getApiCloudMonetizationServiceUrl()
                    + MonetizationConstants.DS_API_URI_SUBSCRIBER_USAGE;
    private static String usageOfApiBySubscriberUrl =
            BillingConfigUtils.getBillingConfiguration().getDSConfig().getApiCloudMonetizationServiceUrl()
                    + MonetizationConstants.DS_API_URI_SUBSCRIBER_API_USAGE;
    private static String usageOfTenantUrl =
            BillingConfigUtils.getBillingConfiguration().getDSConfig().getApiCloudMonetizationServiceUrl()
                    + MonetizationConstants.DS_API_URI_TENANT_USAGE;
    private static String usageOfApiByApplicationBySubscriberUrl =
            BillingConfigUtils.getBillingConfiguration().getDSConfig().getApiCloudMonetizationServiceUrl()
                    + MonetizationConstants.DS_API_URI_SUBSCRIBER_API_USAGE_BY_APPLICATION;

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

            return DataServiceBillingRequestProcessor.isRequestSuccess(response);
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
     * @param tenantDomain  tenant domain
     * @param accountNumber account number
     * @param apiData       api data json object
     * @param effectiveDate effective date
     * @return success information
     * @throws CloudMonetizationException
     */
    public static boolean addSubscriptionInformation(String tenantDomain, String accountNumber, String apiData,
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
                    new NameValuePair(BillingConstants.PARAM_SUBSCRIPTION_NUMBER, apiDataObj.get(BillingConstants
                            .PARAM_SUBSCRIPTION_NUMBER).getAsString()),
                    new NameValuePair(BillingConstants.PARAM_START_DATE, effectiveDate.trim())
            };

            String response = dsBRProcessor.doPost(url, BillingConstants.HTTP_TYPE_APPLICATION_XML, nameValuePairs);
            return DataServiceBillingRequestProcessor.isRequestSuccess(response);
        } catch (CloudBillingException | UnsupportedEncodingException | XMLStreamException e) {
            throw new CloudMonetizationException("Error while adding subscription information for child account: " +
                    accountNumber + " of the parent tenant: " + tenantDomain, e);
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
}
