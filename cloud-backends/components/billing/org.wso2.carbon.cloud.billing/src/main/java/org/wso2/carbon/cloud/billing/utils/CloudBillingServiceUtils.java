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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.billing.beans.usage.AccountUsage;
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.commons.MonetizationConstants;
import org.wso2.carbon.cloud.billing.commons.config.Plan;
import org.wso2.carbon.cloud.billing.commons.config.Subscription;
import org.wso2.carbon.cloud.billing.commons.notifications.EmailNotifications;
import org.wso2.carbon.cloud.billing.commons.utils.BillingConfigUtils;
import org.wso2.carbon.cloud.billing.commons.utils.CloudBillingUtils;
import org.wso2.carbon.cloud.billing.commons.zuora.client.ZuoraAccountClient;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingZuoraException;
import org.wso2.carbon.cloud.billing.processor.BillingRequestProcessor;
import org.wso2.carbon.cloud.billing.processor.BillingRequestProcessorFactory;
import org.wso2.carbon.cloud.billing.usage.apiusage.APICloudUsageManager;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

/**
 * Model to represent Utilities for Cloud Billing module
 */

public final class CloudBillingServiceUtils {

    /*Zuora REST custom response identifiers*/
    private static final String ZUORA_CUSTOM_CREATE_CHILD_RES = "createChildResponse";
    private static final String ZUORA_CUSTOM_ADD_PARENT_RES = "addParentResponse";

    private static final Log LOGGER = LogFactory.getLog(CloudBillingServiceUtils.class);
    private static volatile String configObj;
    private static BillingRequestProcessor dsBRProcessor = BillingRequestProcessorFactory.getBillingRequestProcessor
            (BillingRequestProcessorFactory.ProcessorType.DATA_SERVICE,
                    BillingConfigUtils.getBillingConfiguration()
                            .getDSConfig()
                            .getHttpClientConfig());
    private static BillingRequestProcessor zuoraReqProcessor = BillingRequestProcessorFactory
            .getBillingRequestProcessor(BillingRequestProcessorFactory.ProcessorType.ZUORA, BillingConfigUtils
                    .getBillingConfiguration().getZuoraConfig().getHttpClientConfig());
    private static String billingServiceURI = BillingConfigUtils.getBillingConfiguration().getDSConfig().getCloudBillingServiceUrl();
    private static String monetizationServiceURI = BillingConfigUtils.getBillingConfiguration().getDSConfig()
            .getCloudMonetizationServiceUrl();

    private CloudBillingServiceUtils() {
    }

    /**
     * Retrieve zuora account id for tenant
     *
     * @param tenantDomain tenant domain
     * @return account id
     * @throws CloudBillingException
     */
    public static String getAccountIdForTenant(String tenantDomain) throws CloudBillingException {

        String dsAccountURL = billingServiceURI + BillingConstants.DS_API_URI_TENANT_ACCOUNT;

        String response = dsBRProcessor.doGet(dsAccountURL.replace(BillingConstants.TENANT_DOMAIN_PARAM,
                tenantDomain), null, null);
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
     * Get configuration on json
     *
     * @return json object
     */
    public static String getConfigInJson() {
        if (configObj == null) {
            synchronized (CloudBillingServiceUtils.class) {
                if (configObj == null) {
                    Gson gson = new Gson();
                    configObj = gson.toJson(BillingConfigUtils.getBillingConfiguration());
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Configuration read to json: " + configObj);
                    }
                }
            }
        }
        return configObj;
    }

    /**
     * Validate rate plan id
     *
     * @param serviceId         service Id
     * @param productRatePlanId rate plan id
     * @return validation boolean
     */
    public static boolean validateRatePlanId(String serviceId, String productRatePlanId) {
        Plan[] plans = getSubscriptions(serviceId);
        for (Plan plan : plans) {
            if (plan.getId().equals(productRatePlanId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validate service Id
     *
     * @param serviceId service id
     * @return validation boolean
     */
    public static boolean validateServiceId(String serviceId) {
        Subscription[] subscriptions = BillingConfigUtils.getBillingConfiguration().getZuoraConfig().getSubscriptions();
        for (Subscription subscription : subscriptions) {
            if (serviceId.equals(subscription.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get rate plan for rate planId
     *
     * @param subscriptionId subscription id (ex:api_cloud)
     * @param ratePlanId     rate plan id
     * @return rate plan
     * @throws CloudBillingException
     */
    public static Plan getSubscriptionForId(String subscriptionId, String ratePlanId)
            throws CloudBillingException, XMLStreamException {
        Plan plan = getSubscriptionInfo(subscriptionId, ratePlanId);
        OMElement subscriptionElements;
        if (plan == null) {
            String subscriptionMappingResponse;
            subscriptionMappingResponse = getSubscriptionMapping(ratePlanId);
            if (subscriptionMappingResponse != null) {
                subscriptionElements = AXIOMUtil.stringToOM(subscriptionMappingResponse);
                Iterator<?> iterator =
                        subscriptionElements.getChildrenWithName(new QName(BillingConstants.PRODUCT_RATE_PLAN_ID));
                while (iterator.hasNext()) {
                    OMElement ratePlanIdElement = (OMElement) iterator.next();
                    String productRatePlanId = ratePlanIdElement.getText();
                    plan = getSubscriptionInfo(subscriptionId, productRatePlanId);
                }
            }
        }
        return plan;
    }


    private static Plan getSubscriptionInfo(String subscriptionId, String id) throws CloudBillingException {
        Subscription[] subscriptions = BillingConfigUtils.getBillingConfiguration().getZuoraConfig().getSubscriptions();
        for (Subscription sub : subscriptions) {
            if (subscriptionId.equalsIgnoreCase(sub.getId())) {
                for (Plan plan : sub.getPlans()) {
                    if ((plan.getId()).equals(id)) {
                        return plan;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get plans for Id
     *
     * @param subscriptionId subscription id(ex:api_cloud)
     * @return rate plans in billing.xml
     */
    public static Plan[] getSubscriptions(String subscriptionId) {
        Subscription[] subscriptions = BillingConfigUtils.getBillingConfiguration().getZuoraConfig().getSubscriptions();
        Plan[] plans = null;
        for (Subscription subscription : subscriptions) {
            if (subscriptionId.equalsIgnoreCase(subscription.getId())) {
                plans = subscription.getPlans();
            }
        }
        return plans;
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


    public static String getZuoraProductIdForServiceId(String serviceId) throws CloudBillingException {
        Subscription[] subscriptions = BillingConfigUtils.getBillingConfiguration().getZuoraConfig().getSubscriptions();
        for (Subscription subscription : subscriptions) {
            if (subscription.getId().equals(serviceId)) {
                return subscription.getProductId();
            }
        }
        throw new CloudBillingException("No zuora product id found for serviceId: " + serviceId);
    }

    /**
     * Method to get that the billing functionality enable/disable status
     *
     * @return billing enable/disable status
     */
    public static boolean isBillingEnabled() {
        return BillingConfigUtils.getBillingConfiguration().isBillingEnabled();
    }

    public static boolean isMonetizationEnabled(String tenantDomain, String application) throws CloudBillingException {
        String monetizationStatusUrl = monetizationServiceURI + MonetizationConstants.DS_API_URI_MONETIZATION_STATUS;
        String response = null;
        try {
            String url = monetizationStatusUrl.replace(MonetizationConstants.RESOURCE_IDENTIFIER_TENANT,
                    CloudBillingUtils.encodeUrlParam(tenantDomain)).replace(MonetizationConstants
                    .RESOURCE_IDENTIFIER_CLOUD_TYPE, CloudBillingUtils.encodeUrlParam(application));
            response = dsBRProcessor.doGet(url, null, null);
            OMElement elements = AXIOMUtil.stringToOM(response);

            OMElement status = elements.getFirstChildWithName(new QName(BillingConstants.DS_NAMESPACE_URI,
                    BillingConstants.STATUS));
            //Since the tenants' who are not enabled monetization. will not have an entry in the rdbms.
            return status != null && StringUtils.isNotBlank(status.getText()) && Integer.valueOf(status.getText()) == 1;

        } catch (XMLStreamException | UnsupportedEncodingException e) {
            throw new CloudBillingException("Error occurred while parsing response: " + response, e);
        }
    }

    public static String getRatePlanId(String tenantDomain, String zuoraProductName, String ratePlanName) throws
            CloudBillingException {
        String ratePlanUrl = monetizationServiceURI + MonetizationConstants.DS_API_URI_MONETIZATION_TENANT_RATE_PLAN;
        String response = null;
        try {
            String url = ratePlanUrl.replace(MonetizationConstants.RESOURCE_IDENTIFIER_TENANT,
                    CloudBillingUtils.encodeUrlParam(tenantDomain)).replace(MonetizationConstants
                    .RESOURCE_IDENTIFIER_ZUORA_PRODUCT_NAME, CloudBillingUtils.encodeUrlParam(zuoraProductName))
                    .replace(MonetizationConstants.RESOURCE_IDENTIFIER_RATE_PLAN_NAME, CloudBillingUtils
                            .encodeUrlParam(ratePlanName));
            response = dsBRProcessor.doGet(url, null, null);
            OMElement elements = AXIOMUtil.stringToOM(response);

            OMElement ratePlanId = elements.getFirstChildWithName(new QName(BillingConstants.DS_NAMESPACE_URI,
                    MonetizationConstants.RATE_PLAN_ID));
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
     * Add child account parent
     *
     * @param childAccountNo  child account number
     * @param parentAccountNo parent account number
     * @return success json object
     * @throws CloudBillingZuoraException
     */
    public static JsonObject addAccountParent(String childAccountNo, String parentAccountNo)
            throws CloudBillingZuoraException {
        ZuoraAccountClient client = new ZuoraAccountClient();
        return client.addAccountParent(childAccountNo, parentAccountNo);
    }

    /**
     * @param tenantDomain    tenant domain
     * @param accountInfoJson account information in json
     * @return return json object
     * @throws CloudBillingException
     */
    public static JsonObject createChildAccount(String tenantDomain, String accountInfoJson) throws CloudBillingException {

        String zuoraAccountURI = BillingConfigUtils.getBillingConfiguration().getZuoraConfig()
                .getServiceUrl() + BillingConstants.ZUORA_REST_API_URI_ACCOUNTS;

        String response;
        JsonParser parser = new JsonParser();
        ZuoraAccountClient client;
        try {
            client = new ZuoraAccountClient();
            JsonObject templateAccount = getTemplateAccount(tenantDomain, client);
            JsonObject childAccountObj = parser.parse(accountInfoJson).getAsJsonObject();

            addProfilePropertyIfExists(templateAccount, childAccountObj, BillingConstants.ZUORA_INVOICE_TEMPLATE_ID);
            addProfilePropertyIfExists(templateAccount, childAccountObj, BillingConstants.ZUORA_COMMUNICATION_PROFILE_ID);
            response = zuoraReqProcessor.doPost(zuoraAccountURI, null, childAccountObj.toString());
        } catch (CloudBillingException e) {
            throw new CloudBillingException("Creating child account failed. ", e);
        }

        JsonObject resultObj = new JsonObject();
        try {
            JsonObject createChildResponse = parser.parse(response).getAsJsonObject();
            resultObj.add(ZUORA_CUSTOM_CREATE_CHILD_RES, createChildResponse);
            if (createChildResponse.get(BillingConstants.ZUORA_RESPONSE_SUCCESS).getAsBoolean()) {
                String childAccountNo = createChildResponse.get(BillingConstants.ZUORA_ACCOUNT_NUMBER).getAsString();
                String parentAccountNo = getAccountIdForTenant(tenantDomain);
                JsonObject addParentResponse = client.addAccountParent(childAccountNo, parentAccountNo);
                resultObj.add(ZUORA_CUSTOM_ADD_PARENT_RES, addParentResponse);
            }
            return resultObj;
        } catch (CloudBillingException e) {
            throw new CloudBillingException("Adding parent account details to child account failed. ", e);
        }
    }

    /**
     * Delete zuora account by name
     *
     * @param accountName account name
     * @return success json string
     * @throws CloudBillingZuoraException
     */
    public static JsonObject deleteAccount(String accountName) throws CloudBillingZuoraException {
        ZuoraAccountClient client = new ZuoraAccountClient();
        return client.deleteAccount(accountName);
    }


    /**
     * This is to send notification mails to cloud. Receiver mail
     * address will be set as cloud
     *
     * @param messageBody    message body
     * @param messageSubject message subject
     */
    public static void sendNotificationToCloud(String messageBody, String messageSubject) {
        String receiver = BillingConfigUtils.getBillingConfiguration().getUtilsConfig()
                .getNotifications().getEmailNotification().getSender();
        EmailNotifications.getInstance().sendMail(messageBody, messageSubject, receiver);
    }

    private static JsonObject getTemplateAccount(String tenantDomain, ZuoraAccountClient client) throws CloudBillingZuoraException {

        JsonObject templateAccount = client.queryAccountByName(tenantDomain + BillingConstants.ZUORA_TEMPLATE_ACCOUNT_SUFFIX);
        if (!templateAccount.get("nameSpecified").getAsBoolean()) {
            templateAccount = client.queryAccountByName(BillingConstants.ZUORA_DEFAULT_TEMPLATE_ACCOUNT_SUFFIX);
        }
        return templateAccount;
    }

    private static void addProfilePropertyIfExists(JsonObject templateAccount, JsonObject childAccountObj, String property)
            throws CloudBillingException {
        String propertySpecifiedSuffix = "Specified";
        if (templateAccount.get(property + propertySpecifiedSuffix).getAsBoolean()) {
            childAccountObj.addProperty(property, templateAccount.get(property).getAsJsonObject().get("id").getAsString());
        }
    }

    private static String getSubscriptionMapping(String ratePlanId) throws CloudBillingException {
        String url = BillingConfigUtils.getBillingConfiguration().getDSConfig().getCloudBillingServiceUrl()
                + BillingConstants.DS_API_URI_MAPPING_FOR_SUBSCRIPTION;
        NameValuePair[] nameValuePairs = new NameValuePair[]{new NameValuePair("NEW_SUBSCRIPTION_ID", ratePlanId)};
        return dsBRProcessor.doGet(url, null, nameValuePairs);
    }
}
