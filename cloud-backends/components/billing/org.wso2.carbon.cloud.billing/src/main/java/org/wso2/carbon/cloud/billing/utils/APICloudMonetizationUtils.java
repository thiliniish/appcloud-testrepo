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

    private static final String IS_TEST_ACCOUNT = "testAccount";
    private static final String ACCOUNT_NUMBER = "accountNumber";
    private static BillingRequestProcessor dsBRProcessor = BillingRequestProcessorFactory
            .getBillingRequestProcessor(BillingRequestProcessorFactory.ProcessorType.DATA_SERVICE,
                    BillingConfigUtils.getBillingConfiguration().getDSConfig().getHttpClientConfig());
    private static String subscribersUrl =
            BillingConfigUtils.getBillingConfiguration().getDSConfig().getApiCloudMonetizationServiceUrl()
                    + MonetizationConstants.DS_API_URI_MON_APIC_SUBSCRIBER;

    private APICloudMonetizationUtils() {
    }

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
}
