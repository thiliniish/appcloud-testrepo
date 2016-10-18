/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.cloud.billing.vendor.stripe.utils;

import com.google.gson.JsonObject;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.wso2.carbon.cloud.billing.core.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.core.commons.MonetizationConstants;
import org.wso2.carbon.cloud.billing.core.commons.config.BillingConfigManager;
import org.wso2.carbon.cloud.billing.core.commons.utils.CloudBillingUtils;
import org.wso2.carbon.cloud.billing.core.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.core.processor.BillingRequestProcessor;
import org.wso2.carbon.cloud.billing.core.processor.BillingRequestProcessorFactory;
import org.wso2.carbon.cloud.billing.core.processor.DataServiceBillingRequestProcessor;
import org.wso2.carbon.cloud.billing.core.utils.CloudBillingServiceUtils;
import org.wso2.carbon.cloud.billing.vendor.commons.BillingVendorConstants;
import org.wso2.carbon.cloud.billing.vendor.commons.processor.VendorRequestProcessor;
import org.wso2.carbon.cloud.billing.vendor.commons.utils.BillingVendorConfigUtils;
import org.wso2.carbon.cloud.billing.vendor.stripe.exceptions.CloudBillingVendorException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import javax.xml.stream.XMLStreamException;

/**
 * Model to represent Utilities for Cloud monetization service
 */
public final class APICloudMonetizationUtils {

    private static final Log LOGGER = LogFactory.getLog(APICloudMonetizationUtils.class);

    /* Database http request processor */
    private static BillingRequestProcessor dsBRProcessor;
    private static VendorRequestProcessor vendorRequestProcessor;

    /* Data service URIs */
    private static String monetizationAccountUri;
    private static String accountInfoUri;

    private static String clientSecret;

    static {
        dsBRProcessor = BillingRequestProcessorFactory.getInstance().getBillingRequestProcessor(
                BillingRequestProcessorFactory.ProcessorType.DATA_SERVICE);
        vendorRequestProcessor = new VendorRequestProcessor(
                BillingVendorConfigUtils.getBillingVendorConfiguration().getOAuthEndpointConfig()
                                        .getHttpClientConfig());
        String apiCloudMonUri = BillingConfigManager.getBillingConfiguration().getDataServiceConfig()
                                                    .getApiCloudMonetizationServiceUri();

        monetizationAccountUri = apiCloudMonUri.concat(MonetizationConstants.DS_API_URI_ADD_MONETIZATION_ACCOUNT);
        accountInfoUri = apiCloudMonUri.concat(BillingVendorConstants.DS_API_URI_VENDOR_ACCOUNT_INFO);

        clientSecret =
                BillingVendorConfigUtils.getBillingVendorConfiguration().getAuthenticationApiKeys().getSecretKey();
    }

    private APICloudMonetizationUtils() {
    }

    /**
     * Add account access information of partner accounts
     *
     * @param customerId     customer id
     * @param stripeAuthCode auth code to retrieve the access token of the customer account
     * @return success information
     * @throws CloudBillingVendorException
     */
    public static boolean addMonetizationAccount(String customerId, String stripeAuthCode)
            throws CloudBillingVendorException {
        try {
            String tokenEndpointUrl = BillingVendorConstants.TOKEN_ENDPOINT;
            NameValuePair[] stripeNameValuePairs =
                    new NameValuePair[] { new NameValuePair(BillingVendorConstants.CLIENT_SECRET, clientSecret),
                                          new NameValuePair(BillingVendorConstants.CODE, stripeAuthCode),
                                          new NameValuePair(BillingVendorConstants.GRANT_TYPE,
                                                            BillingVendorConstants.AUTHORIZATION_CODE) };
            String responseFromStripeTE = vendorRequestProcessor
                    .doPost(tokenEndpointUrl, BillingConstants.HTTP_TYPE_APPLICATION_JSON, stripeNameValuePairs);
            if (StringUtils.isBlank(responseFromStripeTE)) {
                throw new CloudBillingVendorException(
                        "Error while retrieving the access token form the stripe token endpoint for customer account:" +
                        " " +
                        customerId);
            }
            LOGGER.info("Successfully retrieved access token information for customer account: " + customerId);
            JsonNode accountCreationResponseList;
            accountCreationResponseList = APICloudMonetizationUtils.getJsonList(responseFromStripeTE);
            String addAccountUrl = monetizationAccountUri.replace(MonetizationConstants.RESOURCE_IDENTIFIER_ACCOUNT_NO,
                                                                  CloudBillingUtils.encodeUrlParam(customerId));
            if (null != accountCreationResponseList) {
                String stripeUserId = accountCreationResponseList.get(BillingVendorConstants.STRIPE_USER_ID).asText();
                String accessToken = accountCreationResponseList.get(BillingVendorConstants.ACCESS_TOKEN).asText();
                String liveMode = "0";
                if (accountCreationResponseList.get(BillingVendorConstants.LIVEMODE).asBoolean()) {
                    liveMode = "1";
                }
                String refreshToken = accountCreationResponseList.get(BillingVendorConstants.REFRESH_TOKEN).asText();
                String tokenType = accountCreationResponseList.get(BillingVendorConstants.TOKEN_TYPE).asText();
                String stripePublishableKey =
                        accountCreationResponseList.get(BillingVendorConstants.STRIPE_PUBLISHABLE_KEY).asText();
                String scope = accountCreationResponseList.get(BillingVendorConstants.SCOPE).asText();
                DateFormat dateFormat = new SimpleDateFormat(BillingVendorConstants.DATE_FORMAT);
                Date date = new Date();
                String accountCreationDate = (dateFormat.format(date));
                NameValuePair[] dsNameValuePairs =
                        new NameValuePair[] { new NameValuePair(BillingVendorConstants.STRIPE_USER_ID, stripeUserId),
                                              new NameValuePair(BillingVendorConstants.ACCESS_TOKEN, accessToken),
                                              new NameValuePair(BillingVendorConstants.LIVEMODE, liveMode),
                                              new NameValuePair(BillingVendorConstants.REFRESH_TOKEN, refreshToken),
                                              new NameValuePair(BillingVendorConstants.TOKEN_TYPE, tokenType),
                                              new NameValuePair(BillingVendorConstants.STRIPE_PUBLISHABLE_KEY,
                                                                stripePublishableKey),
                                              new NameValuePair(BillingVendorConstants.SCOPE, scope),
                                              new NameValuePair(BillingVendorConstants.ACCOUNT_CREATION_DATE,
                                                                accountCreationDate) };
                String dsResponse = dsBRProcessor
                        .doPost(addAccountUrl, BillingConstants.HTTP_TYPE_APPLICATION_XML, dsNameValuePairs);
                return DataServiceBillingRequestProcessor.isXMLResponseSuccess(dsResponse);
            }
        } catch (IOException | CloudBillingException | XMLStreamException e) {
            throw new CloudBillingVendorException(
                    "Error while adding monetization Account information to the database for the customer : " +
                    customerId, e);
        }
        return false;
    }

    public static String getTenantAccountInformation(String accountId) throws CloudBillingVendorException {
        try {
            String url = accountInfoUri.replace(BillingVendorConstants.RESOURCE_IDENTIFIER_CUSTOMER_ID,
                                                CloudBillingUtils.encodeUrlParam(accountId));
            String response = dsBRProcessor.doGet(url, null, null);
            if (response != null && !response.isEmpty()) {
                return response;
            } else {
                return null;
            }
        } catch (CloudBillingException | UnsupportedEncodingException e) {
            throw new CloudBillingVendorException("Error while getting Tenant Account Information for : " + accountId,
                                                  e);
        }
    }

    public static String getPublishableKeyForTenant(String tenantDomain) throws CloudBillingVendorException {
        try {
            String accountId = CloudBillingServiceUtils.getAccountIdForTenant(tenantDomain);
            String accountInfo = getTenantAccountInformation(accountId);
            String publishableKey;
            if (accountInfo != null && !accountInfo.isEmpty()) {
                OMElement elements = AXIOMUtil.stringToOM(accountInfo);
                if (elements.getFirstElement() == null || elements.getFirstElement().getFirstElement() == null) {
                    return "No account information for " + accountId;
                } else {
                    Iterator iterator = elements.getFirstElement().getChildElements();
                    while (iterator.hasNext()) {
                        OMElement AccountInfo = (OMElement) iterator.next();
                        if (BillingVendorConstants.STRIPE_PUBLISHABLE_KEY.equals(AccountInfo.getLocalName())) {
                            publishableKey = AccountInfo.getText();
                            JsonObject params = new JsonObject();
                            params.addProperty("token", publishableKey);
                            return params.toString();
                        }
                    }
                    return "Publishable key Error";
                }
            } else {
                return "No account information for " + accountId;
            }
        } catch (XMLStreamException | CloudBillingException e) {
            throw new CloudBillingVendorException(
                    "Error while getting Tenant Account Information for : " + tenantDomain, e);
        }
    }

    /**
     * Get the Json List of the response string
     *
     * @param responseObject jsonString response
     * @return Json node list
     */
    public static JsonNode getJsonList(String responseObject) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(responseObject);
    }

}
