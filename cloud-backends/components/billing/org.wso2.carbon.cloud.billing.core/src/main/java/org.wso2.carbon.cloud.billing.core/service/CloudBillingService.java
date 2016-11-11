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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.cloud.billing.core.beans.usage.AccountUsage;
import org.wso2.carbon.cloud.billing.core.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.core.commons.CloudBillingServiceProvider;
import org.wso2.carbon.cloud.billing.core.commons.MonetizationConstants;
import org.wso2.carbon.cloud.billing.core.commons.config.BillingConfigManager;
import org.wso2.carbon.cloud.billing.core.commons.config.model.BillingConfig;
import org.wso2.carbon.cloud.billing.core.commons.config.model.DataServiceConfig;
import org.wso2.carbon.cloud.billing.core.commons.config.model.Plan;
import org.wso2.carbon.cloud.billing.core.commons.fileprocessor.FileContentReader;
import org.wso2.carbon.cloud.billing.core.commons.notifications.EmailNotifications;
import org.wso2.carbon.cloud.billing.core.commons.utils.CloudBillingUtils;
import org.wso2.carbon.cloud.billing.core.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.core.security.CloudBillingSecurity;
import org.wso2.carbon.cloud.billing.core.utils.BillingVendorInvoker;
import org.wso2.carbon.cloud.billing.core.utils.CloudBillingServiceUtils;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Represents cloud billing related services.
 */
public class CloudBillingService extends AbstractAdmin implements CloudBillingServiceProvider {
    /**
     * We have enforce logging and throwing exceptions in service methods as this service class is intended to be used
     * as a web service and also invoked by jaggery code via osgi service. For jaggery code in the module layer to
     * generate the error or success message, its important to catch the exception.
     */

    private static final Log LOGGER = LogFactory.getLog(CloudBillingService.class);

    /**
     * Retrieve cloud billing configuration as a json string
     *
     * @return json string
     */
    public static String getConfigInJson() {
        return CloudBillingServiceUtils.getConfigInJson();
    }

    /**
     * Retrieve payment rate plans associated with a service subscription id
     *
     * @param serviceSubscriptionId subscriptionId (api_cloud/app_cloud)
     * @return Rate plans
     */
    public Plan[] getPaymentPlansForServiceId(String serviceSubscriptionId) {
        return CloudBillingServiceUtils.getSubscriptions(serviceSubscriptionId);
    }

    /**
     * Create the Customer
     *
     * @param customerInfoJson customer details
     * @return success Json string
     */
    @Override public String createCustomer(String customerInfoJson) throws CloudBillingException {
        try {
            return init().createCustomer(customerInfoJson);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while creating the customer";
            LOGGER.error(message, ex);
            throw new CloudBillingException(message, ex);
        }
    }

    /**
     * Retrieve customer details
     *
     * @param customerId customer id
     * @return json string of customer information
     */
    @Override public String getCustomerDetails(String customerId) throws CloudBillingException {
        try {
            return init().getCustomerDetails(customerId);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while retrieving the customer details for customer id : " + customerId;
            LOGGER.error(message, ex);
            throw new CloudBillingException(message, ex);
        }
    }

    /**
     * Update Customer Info
     *
     * @param customerId       customerId Id
     * @param customerInfoJson customer details
     * @return success Json string
     */
    @Override public String updateCustomer(String customerId, String customerInfoJson) throws CloudBillingException {
        try {
            return init().updateCustomer(customerId, customerInfoJson);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while updating the customer with customer id : " + customerId;
            LOGGER.error(message, ex);
            throw new CloudBillingException(message, ex);
        }
    }

    /**
     * Delete customer
     *
     * @param customerId customer Id
     * @return success json string
     */
    @Override public String deleteCustomer(String customerId) throws CloudBillingException {
        try {
            return init().deleteCustomer(customerId);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while deleting the customer with customer id : " + customerId;
            LOGGER.error(message, ex);
            throw new CloudBillingException(message, ex);
        }
    }

    /**
     * Create rate plan for the Product
     *
     * @param tenantDomain     tenant domain
     * @param ratePlanInfoJson rate-plan details
     * @return success json string
     */
    @Override public String createProductRatePlan(String tenantDomain, String ratePlanInfoJson)
            throws CloudBillingException {
        try {
            return init().createProductRatePlan(tenantDomain, ratePlanInfoJson);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while creating the the product rate plan.";
            LOGGER.error(message, ex);
            throw new CloudBillingException(message, ex);
        }
    }

    /**
     * retrieve a specific rate plan
     *
     * @param ratePlanId rate-plan Id
     * @return success json string
     */
    @Override public String getProductRatePlan(String ratePlanId) throws CloudBillingException {
        try {
            return init().getProductRatePlan(ratePlanId);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while retrieving the the product rate plan with id : " + ratePlanId;
            LOGGER.error(message, ex);
            throw new CloudBillingException(message, ex);
        }
    }

    /**
     * Method to update Product rate plan
     *
     * @param planId           rate plan ID
     * @param ratePlanInfoJson rate-plan details
     * @return success json string
     */
    @Override public String updateProductRatePlan(String planId, String ratePlanInfoJson) throws CloudBillingException {
        try {
            return init().updateProductRatePlan(planId, ratePlanInfoJson);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while updating the product rate plan with id : " + planId;
            LOGGER.error(message, ex);
            throw new CloudBillingException(message, ex);
        }
    }

    /**
     * Method to delete a specific Product rate plan
     *
     * @param ratePlanId rate-plan Id
     * @return success json string
     */
    @Override public String deleteProductRatePlan(String ratePlanId) throws CloudBillingException {
        try {
            return init().deleteProductRatePlan(ratePlanId);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while deleting the the product rate plan with id : " + ratePlanId;
            LOGGER.error(message, ex);
            throw new CloudBillingException(message, ex);
        }
    }

    /**
     * Method to retrieve all the product rate-plans
     *
     * @param ratePlanInfoJson rate-plan details
     * @return a list of rate plans
     */
    @Override public String getAllProductRatePlans(String ratePlanInfoJson) throws CloudBillingException {
        try {
            return init().getAllProductRatePlans(ratePlanInfoJson);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while retrieving all product rate plans.";
            LOGGER.error(message, ex);
            throw new CloudBillingException(message, ex);
        }
    }

    /**
     * Get current plan subscribed to a service
     *
     * @param customerId customer id
     * @return current active rate plan
     * @throws CloudBillingException
     */
    @Override public String getCurrentRatePlan(String customerId) throws CloudBillingException {
        try {
            return init().getCurrentRatePlan(customerId);
        } catch (CloudBillingException ex) {
            LOGGER.error("Error occurred while retrieving the current rate plan of the customer : " + customerId, ex);
            throw ex;
        }
    }

    /**
     * Get customer coupons
     *
     * @param customerId customer id
     * @return current coupons
     * @throws CloudBillingException
     */
    @Override public String getCustomerCoupons(String customerId) throws CloudBillingException {
        try {
            return init().getCustomerCoupons(customerId);
        } catch (CloudBillingException ex) {
            LOGGER.error("Error occurred while retrieving the coupons of the customer : " + customerId, ex);
            throw ex;
        }
    }

    /**
     * Get a specific coupon details
     *
     * @param couponID coupon id
     * @return coupon data
     * @throws CloudBillingException
     */
    @Override public String retrieveCouponInfo(String couponID) throws CloudBillingException {
        try {
            return init().retrieveCouponInfo(couponID);
        } catch (CloudBillingException ex) {
            LOGGER.error("Error occurred while retrieving the coupons information of the coupon : " + couponID, ex);
            throw ex;
        }
    }

    /**
     * Create a subscription
     *
     * @param subscriptionInfoJson subscription details. This includes customer id and the product rate-plan id
     * @return success Json string
     */
    @Override public String createSubscription(String subscriptionInfoJson) throws CloudBillingException {
        try {
            return init().createSubscription(subscriptionInfoJson);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while creating the subscription.";
            LOGGER.error(message, ex);
            throw new CloudBillingException(message, ex);
        }
    }

    /**
     * Retrieve a subscription
     *
     * @param subscriptionId subscription Id
     * @return success Json string
     */
    @Override public String getSubscription(String subscriptionId) throws CloudBillingException {
        try {
            return init().getSubscription(subscriptionId);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while retrieving the subscription with id : " + subscriptionId;
            LOGGER.error(message, ex);
            throw new CloudBillingException(message, ex);
        }
    }

    /**
     * Method to retrieve all the subscriptions
     *
     * @param subscriptionInfoJson subscription details.
     * @return a list of subscriptions
     */
    @Override public String getAllSubscriptions(String subscriptionInfoJson) throws CloudBillingException {
        try {
            return init().getAllSubscriptions(subscriptionInfoJson);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while retrieving all subscriptions.";
            LOGGER.error(message, ex);
            throw new CloudBillingException(message, ex);
        }
    }

    /**
     * Update subscription
     *
     * @param subscriptionId       subscription Id
     * @param subscriptionInfoJson subscription details for downgrade or upgrade. This includes customer id and the
     *                             product rate-plan id
     * @param isUpgrade            is Upgrade subscription
     * @return success Json string
     */
    @Override public String updateSubscription(String subscriptionId, String subscriptionInfoJson, boolean isUpgrade)
            throws CloudBillingException {
        try {
            return init().updateSubscription(subscriptionId, subscriptionInfoJson, isUpgrade);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while updating the subscription with id : " + subscriptionId;
            LOGGER.error(message, ex);
            throw new CloudBillingException(message, ex);
        }
    }

    /**
     * Cancel subscription by the subscription id
     *
     * @param subscriptionId       subscription id
     * @param subscriptionInfoJson subscription information in json
     * @return success jason string
     */
    @Override public String cancelSubscription(String subscriptionId, String subscriptionInfoJson)
            throws CloudBillingException {
        try {
            return init().cancelSubscription(subscriptionId, subscriptionInfoJson);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while cancelling the subscription with id : " + subscriptionId;
            LOGGER.error(message, ex);
            throw new CloudBillingException(message, ex);
        }
    }

    /**
     * Add a payment method to a specific customer
     *
     * @param customerId            customer Id
     * @param paymentMethodInfoJson payment method details
     * @return success Json string
     */
    @Override public String addPaymentMethod(String customerId, String paymentMethodInfoJson)
            throws CloudBillingException {
        try {
            return init().addPaymentMethod(customerId, paymentMethodInfoJson);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while adding the payment method.";
            LOGGER.error(message, ex);
            throw new CloudBillingException(message, ex);
        }
    }

    /**
     * Set specific payment method of a specific customer as default
     *
     * @param customerId            customer Id
     * @param paymentMethodInfoJson payment method details
     * @return success Json string
     */
    @Override public String setDefaultPaymentMethod(String customerId, String paymentMethodInfoJson)
            throws CloudBillingException {
        try {
            return init().setDefaultPaymentMethod(customerId, paymentMethodInfoJson);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while setting the default payment method of customer " + customerId;
            LOGGER.error(message, ex);
            throw new CloudBillingException(message, ex);
        }
    }

    /**
     * Update default payment method
     *
     * @param customerId            customer Id
     * @param paymentMethodId       payment method id
     * @param paymentMethodInfoJson payment method details
     * @return success Json string
     */
    @Override public String updatePaymentMethod(String customerId, String paymentMethodId, String paymentMethodInfoJson)
            throws CloudBillingException {
        try {
            return init().updatePaymentMethod(customerId, paymentMethodId, paymentMethodInfoJson);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while updating the payment method of customer " + customerId;
            LOGGER.error(message, ex);
            throw new CloudBillingException(message, ex);
        }
    }

    /**
     * Retrieve all payment methods of a specific customer
     *
     * @param customerId            customer Id
     * @param paymentMethodInfoJson payment method details
     * @return success Json string
     */
    @Override public String getAllPaymentMethods(String customerId, String paymentMethodInfoJson)
            throws CloudBillingException {
        try {
            return init().getAllPaymentMethods(customerId, paymentMethodInfoJson);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while retrieving all payment method of customer " + customerId;
            LOGGER.error(message, ex);
            throw new CloudBillingException(message, ex);
        }
    }

    /**
     * Remove payment method/ card info
     *
     * @param customerId      customer Id
     * @param paymentMethodId payment method id
     * @return success jason string
     */
    @Override public String removePaymentMethod(String customerId, String paymentMethodId)
            throws CloudBillingException {
        try {
            return init().removePaymentMethod(customerId, paymentMethodId);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while deleting the payment method " + paymentMethodId + " , of customer" +
                             " " +
                             customerId;
            LOGGER.error(message, ex);
            throw new CloudBillingException(message, ex);
        }
    }

    /**
     * Method to create Monetization account
     *
     * @param customerId                  monetization customer id
     * @param monetizationAccountInfoJson monetization account info
     * @return success jason string
     */
    @Override public String createMonetizationAccount(String customerId, String monetizationAccountInfoJson)
            throws CloudBillingException {
        try {
            return init().createMonetizationAccount(customerId, monetizationAccountInfoJson);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while creating the monetization account for customer " + customerId;
            LOGGER.error(message, ex);
            throw new CloudBillingException(message, ex);
        }
    }

    /**
     * Retrieve invoices associated with a customer
     *
     * @param invoiceInfoJson invoice retrieval info for a specific customer
     * @return Json String of invoices
     */
    @Override public String getInvoices(String invoiceInfoJson) throws CloudBillingException {
        try {
            return init().getInvoices(invoiceInfoJson);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while retrieving invoices.";
            LOGGER.error(message, ex);
            throw new CloudBillingException(message, ex);
        }
    }

    /**
     * Retrieve invoice details
     *
     * @param invoiceId invoice id
     * @return json string of invoice information
     */
    @Override public String getInvoiceDetails(String invoiceId) throws CloudBillingException {
        try {
            return init().getInvoiceDetails(invoiceId);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while retrieving invoice details.";
            LOGGER.error(message, ex);
            throw new CloudBillingException(message, ex);
        }
    }

    /**
     * Retrieve invoices associated with a customer
     *
     * @param invoiceInfoJson invoice creation info
     * @return String of invoices id
     */
    @Override public String createInvoice(String invoiceInfoJson) throws CloudBillingException {
        try {
            return init().createInvoice(invoiceInfoJson);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while creating the invoice";
            LOGGER.error(message, ex);
            throw new CloudBillingException(message, ex);
        }
    }

    /**
     * Retrieve invoices associated with a customer
     *
     * @param invoiceId invoice id
     * @return String of invoices id
     */
    @Override public String chargeInvoice(String invoiceId) throws CloudBillingException {
        try {
            return init().chargeInvoice(invoiceId);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while charging for invoice";
            LOGGER.error(message, ex);
            throw new CloudBillingException(message, ex);
        }
    }

    /**
     * Load and return the billing vendor instance
     *
     * @return billing vendor
     */
    private CloudBillingServiceProvider init() throws CloudBillingException {
        return BillingVendorInvoker.loadBillingVendor();
    }

    /**
     * Invoke cloud billing vendor method
     *
     * @return the result string object
     */
    public String callVendorMethod(String methodName, String params) throws CloudBillingException {
        return (String) BillingVendorInvoker.invokeMethod(methodName, params);
    }

    /**
     * Call Vendor Method with Monetization Parameters
     *
     * @param methodName    Method to be invoked from Vendor Class
     * @param tenantDomain  Tenant Domain to be parsed
     * @param params        Parameters to be used
     * @return              String response from vendor end
     * @throws CloudBillingException
     */
    public String callVendorMethodForMonetization(String methodName, String tenantDomain, String params)
            throws CloudBillingException {
        return (String) BillingVendorInvoker.invokeMethodForMonetization(methodName, tenantDomain, params);
    }

    /**
     * Validate rate plan id
     *
     * @param serviceId         serviceId
     * @param productRatePlanId rate plan id
     * @return success boolean
     */
    public boolean validateRatePlanId(String serviceId, String productRatePlanId) {
        return CloudBillingServiceUtils.validateRatePlanId(serviceId, productRatePlanId);
    }

    /**
     * Validate service id
     *
     * @param serviceId service Id
     * @return success boolean
     */
    public boolean validateServiceId(String serviceId) {
        return CloudBillingServiceUtils.validateServiceId(serviceId);
    }

    /**
     * Method to get that the billing functionality enable/disable status
     *
     * @param cloudId Unique ID for the cloud (i.e api_cloud)
     * @return billing enable/disable status
     */
    public boolean isBillingEnabled(String cloudId) {
        return CloudBillingServiceUtils.isBillingEnabled(cloudId);
    }

    /**
     * Send notification emails for billing service
     *
     * @param receiver receiver email address
     * @param subject  email subject
     * @param msgBody  email body
     */
    public void sendEmailNotification(String receiver, String subject, String msgBody, String contentType) {
        EmailNotifications.getInstance().sendMail(msgBody, subject, receiver, contentType);
    }

    /**
     * Send notification emails to cloud alerts
     *
     * @param subject subject of the mail
     * @param msgBody mail body
     */
    public void sendEmailToCloud(String subject, String msgBody) {
        CloudBillingServiceUtils.sendNotificationToCloud(msgBody, subject);
    }

    /**
     * Method to enable monetization
     *
     * @param tenantDomain      tenantDomain
     * @param tenantPassword    tenant password
     * @param tenantDisplayName tenant display name
     * @return status of product creation
     * @throws CloudBillingException
     */
    public boolean enableMonetization(String tenantDomain, String customerId, String stripeAuthCode,
                                      String tenantPassword, String tenantDisplayName) throws CloudBillingException {
        boolean status = false;
        String testAccountCreationEmailFileName = MonetizationConstants.TEST_ACCOUNT_CREATION_EMAIL_FILE_NAME;
        String testAccountDeletionEmailFileName = MonetizationConstants.TEST_ACCOUNT_DELETION_EMAIL_FILE_NAME;
        String subscriptionNotificationEmailFileName = MonetizationConstants.SUBSCRIPTION_NOTIFICATION_EMAIL_FILE_NAME;
        String textContentType = BillingConstants.TEXT_PLAIN_CONTENT_TYPE;
        // Create the monetization Account
        boolean accountCreationResponse = Boolean.parseBoolean(createMonetizationAccount(customerId, stripeAuthCode));
        if (accountCreationResponse) {
            status = true;
            // Add subscriptionCreation element to workflowExtension.xml in registry
            if (!updateWorkFlow(MonetizationConstants.TAG_SUBSCRIPTION_CREATION,
                                MonetizationConstants.SUBSCRIPTION_CREATION_EXECUTOR, tenantPassword, tenantDisplayName,
                                tenantDomain)) {
                LOGGER.error("Registry WorkflowExtension.xml update failed for subscription creation while enabling " +
                             "Monetization.");
            }
            // Add subscriptionDeletion element to workflowExtension.xml in registry
            if (!updateWorkFlow(MonetizationConstants.TAG_SUBSCRIPTION_DELETION,
                                MonetizationConstants.SUBSCRIPTION_DELETION_EXECUTOR, tenantPassword, tenantDisplayName,
                                tenantDomain)) {
                LOGGER.error("Registry WorkflowExtension.xml update failed for subscription deletion while enabling " +
                             "Monetization.");
            }
            // Add applicationDeletion element to workflowExtension.xml in registry
            if (!updateWorkFlow(MonetizationConstants.TAG_APPLICATION_DELETION,
                                MonetizationConstants.APLLICATION_DELETION_EXECUTOR, tenantPassword, tenantDisplayName,
                                tenantDomain)) {
                LOGGER.error("Registry WorkflowExtension.xml update failed for application deletion while enabling " +
                             "Monetization.");
            }
            if (!createEmailFileInRegistry(tenantDomain, subscriptionNotificationEmailFileName, textContentType)) {
                LOGGER.error("Creating the registry file " + subscriptionNotificationEmailFileName + " failed.");
            }
            if (!createEmailFileInRegistry(tenantDomain, testAccountCreationEmailFileName, textContentType)) {
                LOGGER.error("Creating the registry file " + testAccountCreationEmailFileName + " failed.");
            }
            if (!createEmailFileInRegistry(tenantDomain, testAccountDeletionEmailFileName, textContentType)) {
                LOGGER.error("Creating the registry file " + testAccountDeletionEmailFileName + " failed.");
            }
        }
        return status;
    }

    /**
     * Method to update the registry WorkflowExtension.xml to add SubscriptionCreation element
     *
     * @param tenantPassword tenant password
     * @param tenantUsername tenant username
     * @return status of the update
     * @throws CloudBillingException
     */
    public boolean updateWorkFlow(String workflow, String executor, String tenantPassword, String tenantUsername,
                                  String tenantDomain) throws CloudBillingException {
        try {
            // Get the workflow resource url
            String workflowUrl = MonetizationConstants.WORKFLOW_EXTENSION_URL;
            Resource workflowResource = CloudBillingUtils.getRegistryResource(tenantDomain, workflowUrl);

            // Get the resource content
            byte[] bytesContent = (byte[]) workflowResource.getContent();
            String content = new String(bytesContent, BillingConstants.ENCODING);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            InputSource inputSource = new InputSource();
            inputSource.setCharacterStream(new StringReader(content));
            Document doc = documentBuilder.parse(inputSource);
            Node workFlowExtension = doc.getElementsByTagName(MonetizationConstants.TAG_WORKFLOW_EXTENSION).item(0);
            // Loop the WorkFlowExtensions child node
            NodeList extensionList = workFlowExtension.getChildNodes();
            for (int i = 0; i < extensionList.getLength(); i++) {
                Node node = extensionList.item(i);
                // Remove the already existing element tag
                if (workflow.equals(node.getNodeName())) {
                    workFlowExtension.removeChild(node);
                }
            }
            // Add the new tag
            Element newElement = doc.createElement(workflow);
            // Set the Attribute executor
            newElement.setAttribute(MonetizationConstants.ATTRIBUTE_EXECUTOR, executor);
            // Add ServiceUrl Property tag
            Element propertyServiceUrl = doc.createElement(MonetizationConstants.ATTRIBUTE_PROPERTY);
            propertyServiceUrl.setAttribute(MonetizationConstants.ATTRIBUTE_NAME,
                                            MonetizationConstants.PROPERTY_SERVICE_END_POINT);
            // Get ServiceUrl from config files
            BillingConfig billingConfig = BillingConfigManager.getBillingConfiguration();
            DataServiceConfig dataServiceConfig = billingConfig.getDataServiceConfig();
            String serviceUrlHost = dataServiceConfig.getHttpClientConfig().getHostname();
            int serviceUrlPort = dataServiceConfig.getHttpClientConfig().getPort();
            String serviceURL = BillingConstants.HTTPS + serviceUrlHost + BillingConstants.COLON + serviceUrlPort +
                                MonetizationConstants.PROPERTY_MONETIZATION_SERVICE_VALUE;
            propertyServiceUrl.setTextContent(serviceURL);
            newElement.appendChild(propertyServiceUrl);
            // Add Username Property tag
            Element propertyName = doc.createElement(MonetizationConstants.ATTRIBUTE_PROPERTY);
            propertyName
                    .setAttribute(MonetizationConstants.ATTRIBUTE_NAME, MonetizationConstants.PROPERTY_USERNAME_NAME);
            propertyName.setTextContent(tenantUsername);
            newElement.appendChild(propertyName);
            // Add Password Property tag
            Element propertyPassword = doc.createElement(MonetizationConstants.ATTRIBUTE_PROPERTY);
            propertyPassword
                    .setAttribute(MonetizationConstants.ATTRIBUTE_NAME, MonetizationConstants.PROPERTY_PASSWORD_NAME);
            propertyPassword.setTextContent(tenantPassword);
            newElement.appendChild(propertyPassword);
            workFlowExtension.appendChild(newElement);
            // Writing xml file
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, MonetizationConstants.YES);
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(doc);
            // Creating output stream
            transformer.transform(source, result);
            content = result.getWriter().toString();
            // Update the workflow resource
            workflowResource.setContent(content.getBytes(BillingConstants.ENCODING));
            return CloudBillingUtils.putRegistryResource(tenantDomain, workflowUrl, workflowResource);
        } catch (RegistryException | ParserConfigurationException | SAXException | IOException | TransformerException
                e) {
            throw new CloudBillingException("Error occurred while updating the Registry workflowExtensionContent ", e);
        }
    }

    /**
     * This method will create the email notification files sent to subscribers in  the tenant space of the registry.
     *
     * @param tenantDomain     tenant domain
     * @param emailFileName    email file name
     * @param emailContentType email content type
     * @return the status of the file creation in the tenant space.
     * @throws CloudBillingException
     */
    private boolean createEmailFileInRegistry(String tenantDomain, String emailFileName, String emailContentType)
            throws CloudBillingException {
        FileContentReader fileContentReader = new FileContentReader();
        String fileBasePath =
                CarbonUtils.getCarbonHome() + File.separator + MonetizationConstants.EMAIL_RESOURCES_FOLDER +
                File.separator;
        String emailFilePath = fileBasePath + emailFileName;
        String registryPath = MonetizationConstants.EMAIL_FILE_BASE_URL + emailFileName;
        try {
            String emailContent = fileContentReader.fileReader(emailFilePath);
            return CloudBillingUtils.createRegistryResource(tenantDomain, registryPath, emailContent, emailContentType,
                                                            BillingConstants.GOVERNANCE_REGISTRY);
        } catch (Exception e) {
            throw new CloudBillingException(
                    "Error occurred while creating the registry file " + emailFileName + " error: ", e);
        }
    }

    /**
     * Method to update the config registry, monetization status in tenant-conf.json
     *
     * @param tenantDomain tenant domain
     * @return status of the update
     */
    public boolean updateMonetizationStatus(String tenantDomain) throws CloudBillingException {

        // Get the tenant-conf resource url
        String tenantConfUrl = MonetizationConstants.TENANT_CONF_URL;
        Resource tenantConfResource =
                CloudBillingUtils.getRegistryResource(tenantDomain, tenantConfUrl, BillingConstants.CONFIG_REGISTRY);

        // Get the resource content
        try {
            String content = new String((byte[]) tenantConfResource.getContent(), BillingConstants.ENCODING);
            JsonObject jsonRegistryObject = (JsonObject) new JsonParser().parse(content);
            jsonRegistryObject.addProperty(BillingConstants.ENABLE_MONETIZATION_REGISTRY_PROPERTY, true);
            tenantConfResource.setContent(jsonRegistryObject.toString().getBytes(BillingConstants.ENCODING));
            return CloudBillingUtils.putRegistryResource(tenantDomain, tenantConfUrl, tenantConfResource,
                                                         BillingConstants.CONFIG_REGISTRY);

        } catch (RegistryException e) {
            throw new CloudBillingException("Error occurred while updating the monetization status in registry ", e);
        } catch (UnsupportedEncodingException e) {
            throw new CloudBillingException("Error occurred during encoding bytes ", e);
        }
    }

    /**
     * Encrypt texts using the default Crypto utility. and base 64 encode
     *
     * @param text text need to be encrypt
     * @return base64encoded encrypted string
     * @throws CloudBillingException
     */
    public String getEncryptionInfo(String text) throws CloudBillingException {
        try {
            return CloudBillingServiceUtils.getEncryptionInfo(text);
        } catch (CryptoException e) {
            throw new CloudBillingException("Error occurred while encrypting ", e);
        }
    }

    /**
     * Decrypt texts using the default Crypto utility. and base 64 decode
     *
     * @param base64CyperText base64 Cyper Text need to be decrypt
     * @return base64decoded decrypted string
     * @throws CloudBillingException
     */
    public String getDecryptedInfo(String base64CyperText) throws CloudBillingException {
        try {
            return CloudBillingServiceUtils.getDecryptedInfo(base64CyperText);
        } catch (CryptoException | IOException e) {
            throw new CloudBillingException("Error occurred while decrypting ", e);
        }
    }

    /**
     * Retrieve customer Id for tenant domain
     *
     * @param tenantDomain tenant domain
     * @return string customer Id
     * @throws CloudBillingException
     */
    public String getAccountId(String tenantDomain) throws CloudBillingException {
        try {
            return CloudBillingServiceUtils.getAccountIdForTenant(tenantDomain);
        } catch (CloudBillingException ex) {
            LOGGER.error("Error occurred while retrieving account Id for tenant: " + tenantDomain, ex);
            throw ex;
        }
    }

    /**
     * Generate a MDA hash
     *
     * @param data        data which need a hash
     * @param mdAlgorithm mda algorithm
     * @return hashed data
     * @throws CloudBillingException
     */
    public String generateHash(String data, String mdAlgorithm) throws CloudBillingException {
        try {
            return CloudBillingSecurity.generateHash(data, mdAlgorithm);
        } catch (CloudBillingException ex) {
            throw ex;
        }
    }

    /**
     * Validate the encrypted value against the provided encryption algorithm
     * @param token         original token
     * @param tokenHash     encrypted token parsed
     * @param mdAlgorithm   encryption Algorithm
     * @return
     * @throws CloudBillingException
     */
    public boolean validateHash(String token, String tokenHash, String mdAlgorithm) throws CloudBillingException {
        try {
            return CloudBillingSecurity.validateHash(token, tokenHash, mdAlgorithm);
        } catch (CloudBillingException ex) {
            throw ex;
        }
    }

    /**
     * Get a specific account details
     *
     * @param customerId customer id
     * @return coupon data
     * @throws CloudBillingException
     */
    @Override public String retrieveAccountInfo(String customerId) throws CloudBillingException {
        try {
            return init().retrieveAccountInfo(customerId);
        } catch (CloudBillingException ex) {
            LOGGER.error("Error occurred while retrieving the account information of the customer : " + customerId, ex);
            throw ex;
        }
    }

    /**
     * Get currency used from Vendor
     *
     * @return { "success" : "true",
     * "data" : {
     * "currency" : "USD",
     * "conversion" : "CENTS"
     * }
     * }
     * @throws CloudBillingException
     */
    @Override public String getCurrencyUsed() throws CloudBillingException {
        try {
            return init().getCurrencyUsed();
        } catch (CloudBillingException ex) {
            LOGGER.error("Error occurred while retrieving the currency used");
            throw ex;
        }
    }

    /**
     * Method to get that the billing trial period
     *
     * @param cloudId Unique ID for the cloud (i.e api_cloud)
     * @return billing trial period
     */
    public static String getTrialPeriod(String cloudId) {
        return CloudBillingServiceUtils.getTrialPeriod(cloudId);
    }

    /**
     * Method to get that the billing usage display period
     *
     * @param cloudId Unique ID for the cloud (i.e api_cloud)
     * @return billing usage display period
     */
    public static String usageDisplayPeriod(String cloudId) {
        return CloudBillingServiceUtils.usageDisplayPeriod(cloudId);
    }

    /**
     * Retrieve usage data for a tenant
     *
     * @param tenantDomain Tenant Domain
     * @param productName  Subscribed product
     * @param startDate    date range - start date
     * @param endDate      data range - end date
     * @return Account Usage array
     * @throws CloudBillingException
     */
    public AccountUsage[] getTenantUsageDataForGivenDateRange(String tenantDomain, String productName, String startDate,
                                                              String endDate) throws CloudBillingException {
        try {
            return CloudBillingServiceUtils
                    .getTenantUsageDataForGivenDateRange(tenantDomain, productName, startDate, endDate);
        } catch (CloudBillingException ex) {
            LOGGER.error("Error occurred while retrieving usage data of tenant: " + tenantDomain + "for product: " +
                         productName, ex);
            throw ex;
        }
    }
}
