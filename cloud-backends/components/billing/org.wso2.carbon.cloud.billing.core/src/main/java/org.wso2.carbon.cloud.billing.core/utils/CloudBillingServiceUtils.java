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

package org.wso2.carbon.cloud.billing.core.utils;

import com.google.gson.Gson;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.wso2.carbon.cloud.billing.core.beans.usage.AccountUsage;
import org.wso2.carbon.cloud.billing.core.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.core.commons.MonetizationConstants;
import org.wso2.carbon.cloud.billing.core.commons.config.BillingConfigManager;
import org.wso2.carbon.cloud.billing.core.commons.config.model.CloudType;
import org.wso2.carbon.cloud.billing.core.commons.config.model.Plan;
import org.wso2.carbon.cloud.billing.core.commons.notifications.EmailNotifications;
import org.wso2.carbon.cloud.billing.core.commons.utils.CloudBillingUtils;
import org.wso2.carbon.cloud.billing.core.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.core.processor.BillingRequestProcessor;
import org.wso2.carbon.cloud.billing.core.processor.BillingRequestProcessorFactory;
import org.wso2.carbon.cloud.billing.core.usage.apiusage.APICloudUsageManager;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * Model to represent Utilities for Cloud Billing core module
 */
public final class CloudBillingServiceUtils {

    private static final Log LOGGER = LogFactory.getLog(CloudBillingServiceUtils.class);
    private static volatile String configObj;
    private static String billingServiceURI;
    private static BillingRequestProcessor dsBRProcessor;
    private static String monetizationServiceURI;

    static {
        monetizationServiceURI =
                BillingConfigManager.getBillingConfiguration().getDataServiceConfig().getCloudMonetizationServiceUri();
        dsBRProcessor = BillingRequestProcessorFactory.getInstance().getBillingRequestProcessor(
                BillingRequestProcessorFactory.ProcessorType.DATA_SERVICE);
        billingServiceURI =
                BillingConfigManager.getBillingConfiguration().getDataServiceConfig().getCloudBillingServiceUri();
    }

    private CloudBillingServiceUtils() {
    }

    /**
     * Get configuration on json
     *
     * @return json object
     */
    public static String getConfigInJson() {
        if (configObj == null) {
            synchronized (CloudBillingServiceUtils.class) {
                if (configObj == null) {
                    Gson gson = new Gson();
                    configObj = gson.toJson(BillingConfigManager.getBillingConfiguration());
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Configuration read to json is successfully completed.");
                    }
                }
            }
        }
        return configObj;
    }

    /**
     * Get plans for Id
     *
     * @param cloudId Unique ID for the cloud (i.e api_cloud)
     * @return rate plans in billing.xml
     */
    public static Plan[] getSubscriptions(String cloudId) {
        CloudType cloudType = BillingConfigManager.getBillingConfiguration().getCloudTypeById(cloudId);
        return cloudType.getSubscription().getPlans();
    }

    /**
     * Retrieve the billing vendor class name
     *
     * @return Billing Vendor service class name
     */
    public static String getBillingVendorServiceUtilClass() {
        return BillingConfigManager.getBillingConfiguration().getBillingVendorClass();
    }

    /**
     * Retrieve the billing vendor monetization class name
     *
     * @return Billing Vendor service monetization class name
     */
    public static String getBillingVendorMonetizationServiceUtilClass() {
        return BillingConfigManager.getBillingConfiguration().getBillingVendorMonetizationClass();
    }

    /**
     * Validate rate plan id
     *
     * @param serviceId         service Id
     * @param productRatePlanId rate plan id
     * @return validation boolean
     */
    public static boolean validateRatePlanId(String serviceId, String productRatePlanId) {
        if (serviceId != null && productRatePlanId != null && !serviceId.trim().isEmpty() &&
            !productRatePlanId.trim().isEmpty()) {
            Plan[] plans = getSubscriptions(serviceId);
            for (Plan plan : plans) {
                if (plan.getId().equals(productRatePlanId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Validate service Id
     *
     * @param cloudId service id
     * @return validation boolean
     */
    public static boolean validateServiceId(String cloudId) {
        CloudType[] cloudTypes = BillingConfigManager.getBillingConfiguration().getCloudTypes();
        for (CloudType cloudType : cloudTypes) {
            if (cloudId.equals(cloudType.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method to get that the billing functionality enable/disable status
     *
     * @param cloudId Unique ID for the cloud (i.e api_cloud)
     * @return billing enable/disable status
     */
    public static boolean isBillingEnabled(String cloudId) {
        return BillingConfigManager.getBillingConfiguration().getCloudTypeById(cloudId).isBillingEnabled();
    }

    /**
     * Method to get that the monetization functionality enable/disable status
     *
     * @param tenantDomain tenant domain
     * @param cloudId      Unique ID for the cloud (i.e api_cloud)
     * @return monetization enable/disable status
     */
    public static boolean isMonetizationEnabled(String tenantDomain, String cloudId) throws CloudBillingException {
        String monetizationStatusUrl =
                monetizationServiceURI.concat(MonetizationConstants.DS_API_URI_MONETIZATION_STATUS);
        String response = null;
        try {
            String url = monetizationStatusUrl.replace(MonetizationConstants.RESOURCE_IDENTIFIER_TENANT,
                                                       CloudBillingUtils.encodeUrlParam(tenantDomain))
                                              .replace(MonetizationConstants.RESOURCE_IDENTIFIER_CLOUD_TYPE,
                                                       CloudBillingUtils.encodeUrlParam(cloudId));
            response = dsBRProcessor.doGet(url, null, null);
            OMElement elements = AXIOMUtil.stringToOM(response);

            OMElement status = elements.getFirstChildWithName(
                    new QName(BillingConstants.DS_NAMESPACE_URI, BillingConstants.STATUS));
            //Since the tenants' who are not enabled monetization. will not have an entry in the rdbms.
            return status != null && StringUtils.isNotBlank(status.getText()) &&
                   Integer.parseInt(status.getText()) == 1;

        } catch (XMLStreamException | UnsupportedEncodingException e) {
            throw new CloudBillingException("Error occurred while parsing response: " + response, e);
        }
    }

    /**
     * This is to send notification mails to cloud. Receiver mail
     * address will be set as cloud
     *
     * @param messageBody    message body
     * @param messageSubject message subject
     */
    public static void sendNotificationToCloud(String messageBody, String messageSubject) {
        String receiver = BillingConfigManager.getBillingConfiguration().getNotificationsConfig().getEmailNotification()
                                              .getSender();
        EmailNotifications.getInstance()
                          .sendMail(messageBody, messageSubject, receiver, BillingConstants.TEXT_PLAIN_CONTENT_TYPE);
    }

    /**
     * Get the Json List of the response string
     *
     * @param responseObject jsonString response
     * @return Json node list
     */
    public static JsonNode getJsonList(String responseObject) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        // convert JSON string to JsonNode list
        return mapper.readTree(responseObject);
    }

    /**
     * Encrypt texts using the default Crypto utility. and base 64 encode
     *
     * @param text text need to be encrypt
     * @return base64encoded encrypted string
     * @throws org.wso2.carbon.core.util.CryptoException
     */
    public static String getEncryptionInfo(String text) throws CryptoException {
        return CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(text.getBytes(Charset.defaultCharset()));
    }

    /**
     * Decrypt texts using the default Crypto utility. and base 64 decode
     *
     * @param base64CyperText base64 Cyper Text need to be decrypt
     * @return base64decoded decrypted string
     * @throws org.wso2.carbon.core.util.CryptoException
     */
    public static String getDecryptedInfo(String base64CyperText) throws CryptoException, IOException {
        byte[] decriptedByteArray = CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(base64CyperText);
        return new String(decriptedByteArray, BillingConstants.ENCODING);
    }

    /**
     * Retrieve Payment account id for tenant
     *
     * @param tenantDomain tenant domain
     * @return account id
     * @throws CloudBillingException
     */
    public static String getAccountIdForTenant(String tenantDomain) throws CloudBillingException {

        String dsAccountURL = billingServiceURI.concat(BillingConstants.DS_API_URI_TENANT_ACCOUNT);

        String response = dsBRProcessor
                .doGet(dsAccountURL.replace(BillingConstants.TENANT_DOMAIN_PARAM, tenantDomain), null, null);
        try {
            if (response != null && !response.isEmpty()) {
                OMElement elements = AXIOMUtil.stringToOM(response);
                if (elements.getFirstElement() == null || elements.getFirstElement().getFirstElement() == null) {
                    return null;
                } else {
                    return elements.getFirstElement().getFirstElement().getText();
                }
            } else {
                return null;
            }

        } catch (XMLStreamException e) {
            throw new CloudBillingException("Unable to get the OMElement from " + response, e);
        }
    }

    /**
     * Method to get that the billing trial period
     *
     * @param cloudId Unique ID for the cloud (i.e api_cloud)
     * @return billing trial period
     */
    public static String getTrialPeriod(String cloudId) {
        return Integer
                .toString(BillingConfigManager.getBillingConfiguration().getCloudTypeById(cloudId).getTrialPeriod());
    }

    /**
     * @param tenantDomain tenant domain
     * @param productName  product name
     * @param ratePlanName rate plan name
     * @return rate plan id
     * @throws CloudBillingException
     */
    public static String getRatePlanId(String tenantDomain, String productName, String ratePlanName)
            throws CloudBillingException {
        String ratePlanUrl =
                monetizationServiceURI.concat(MonetizationConstants.DS_API_URI_MONETIZATION_TENANT_RATE_PLAN);
        String response = null;
        try {
            String url = ratePlanUrl.replace(MonetizationConstants.RESOURCE_IDENTIFIER_TENANT,
                                             CloudBillingUtils.encodeUrlParam(tenantDomain))
                                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_PRODUCT_NAME,
                                             CloudBillingUtils.encodeUrlParam(productName))
                                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_RATE_PLAN_NAME,
                                             CloudBillingUtils.encodeUrlParam(ratePlanName));
            response = dsBRProcessor.doGet(url, null, null);
            OMElement elements = AXIOMUtil.stringToOM(response);

            OMElement ratePlanId = elements.getFirstChildWithName(
                    new QName(BillingConstants.DS_NAMESPACE_URI, MonetizationConstants.RATE_PLAN_ID));
            if (ratePlanId != null && StringUtils.isNotBlank(ratePlanId.getText())) {
                return ratePlanId.getText().trim();
            } else {
                return BillingConstants.EMPTY_STRING;
            }
        } catch (XMLStreamException | UnsupportedEncodingException e) {
            throw new CloudBillingException("Error occurred while parsing response: " + response, e);
        }
    }

    /**
     * Method to get that the billing usage display period
     *
     * @param cloudId Unique ID for the cloud (i.e api_cloud)
     * @return billing usage display period
     */
    public static String usageDisplayPeriod(String cloudId) {
        return Integer.toString(
                BillingConfigManager.getBillingConfiguration().getCloudTypeById(cloudId).getUsageDisplayPeriod());
    }

    /**
     * Retrieve account usage
     *
     * @param tenantDomain tenant domain
     * @param productName  product name
     * @param startDate    start date (date range for usage)
     * @param endDate      end data (date range for usage)
     * @return account usage array
     * @throws CloudBillingException
     */
    public static AccountUsage[] getTenantUsageDataForGivenDateRange(String tenantDomain, String productName,
                                                                     String startDate, String endDate)
            throws CloudBillingException {
        APICloudUsageManager usageManager = new APICloudUsageManager();
        return usageManager.getTenantUsageDataForGivenDateRange(tenantDomain, productName, startDate, endDate);
    }

}
