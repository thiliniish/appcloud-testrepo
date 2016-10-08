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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.cloud.billing.core.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.core.commons.CloudBillingServiceProvider;
import org.wso2.carbon.cloud.billing.core.commons.MonetizationConstants;
import org.wso2.carbon.cloud.billing.core.commons.config.BillingConfig;
import org.wso2.carbon.cloud.billing.core.commons.config.DataServiceConfig;
import org.wso2.carbon.cloud.billing.core.commons.config.Plan;
import org.wso2.carbon.cloud.billing.core.commons.notifications.EmailNotifications;
import org.wso2.carbon.cloud.billing.core.commons.utils.BillingConfigUtils;
import org.wso2.carbon.cloud.billing.core.commons.utils.CloudBillingUtils;
import org.wso2.carbon.cloud.billing.core.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.core.utils.BillingVendorInvoker;
import org.wso2.carbon.cloud.billing.core.utils.CloudBillingServiceUtils;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

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
     * @param ratePlanInfoJson rate-plan details
     * @return success json string
     */
    @Override public String createProductRatePlan(String ratePlanInfoJson) throws CloudBillingException {
        try {
            return init().createProductRatePlan(ratePlanInfoJson);
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
    @Override public String updateProductRatePlan(String planId, String ratePlanInfoJson) throws
                                                                                          CloudBillingException {
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
     * @return success Json string
     */
    @Override public String updateSubscription(String subscriptionId, String subscriptionInfoJson)
            throws CloudBillingException {
        try {
            return init().updateSubscription(subscriptionId, subscriptionInfoJson);
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
    @Override public String updatePaymentMethod(String customerId, String paymentMethodId, String
            paymentMethodInfoJson)
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
     * Load and return the billing vendor instance
     *
     * @return billing vendor
     */
    private CloudBillingServiceProvider init() throws CloudBillingException {
        return BillingVendorInvoker.loadBillingVendor();
    }

    /**
     * Load and return the billing vendor instance for monetization
     * Here we have to pass parameters to billing vendor for monetization
     *
     * @return billing vendor for monetization
     */
    public CloudBillingServiceProvider init(String consArg) throws CloudBillingException {
        return BillingVendorInvoker.loadBillingVendorForMonetization(consArg);
    }

    /**
     * Invoke cloud billing vendor method with no argument
     *
     * @return the result string object
     */
    public String callVendorNoArgsUtilMethod(String methodName) throws CloudBillingException {
        return (String) BillingVendorInvoker.invokeMethod(methodName);
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
     * @return billing enable/disable status
     */
    public boolean isBillingEnabled() {
        return CloudBillingServiceUtils.isBillingEnabled();
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
    public boolean enableMonetization(String tenantDomain, String customerId, String monetizationAccountInfoJson,
                                      String tenantPassword, String tenantDisplayName) throws CloudBillingException {
        boolean status = false;
        String testAccountCreationEmailFileName = MonetizationConstants.TEST_ACCOUNT_CREATION_EMAIL_FILE_NAME;
        String testAccountDeletionEmailFileName = MonetizationConstants.TEST_ACCOUNT_DELETION_EMAIL_FILE_NAME;
        String subscriptionNotificationEmailFileName = MonetizationConstants.SUBSCRIPTION_NOTIFICATION_EMAIL_FILE_NAME;
        String textContentType = BillingConstants.TEXT_PLAIN_CONTENT_TYPE;

        try {
            // Create the monetization Account
            JsonNode accountCreationResponseList = CloudBillingServiceUtils.getJsonList(createMonetizationAccount(
                    customerId, monetizationAccountInfoJson));

            if (null != accountCreationResponseList && null != accountCreationResponseList.get("id")) {
                //add secret key and publishable key to database.

                //   Add subscriptionCreation element to workflowExtension.xml in registry
                status = true;
                if (!updateWorkFlow(tenantPassword, tenantDisplayName, tenantDomain)) {
                    LOGGER.error(
                            "Registry WorkflowExtension.xml update failed while enabling Monetization.");
                }
                /*if (!createEmailFileInRegsitry(tenantDomain, subscriptionNotificationEmailFileName,
                                               textContentType)) {
                    LOGGER.error("Creating the registry file " + subscriptionNotificationEmailFileName +
                                 " failed.");
                }
                if (!createEmailFileInRegsitry(tenantDomain, testAccountCreationEmailFileName,
                                               textContentType)) {
                    LOGGER.error("Creating the registry file " + testAccountCreationEmailFileName +
                                 " failed.");
                }
                if (!createEmailFileInRegsitry(tenantDomain, testAccountDeletionEmailFileName,
                                               textContentType)) {
                    LOGGER.error("Creating the registry file " + testAccountDeletionEmailFileName +
                                 " failed.");
                }*/
            }
        } catch (IOException e) {
            throw new CloudBillingException("Error occurred while creating monetization account.", e);
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
    public boolean updateWorkFlow(String tenantPassword, String tenantUsername, String tenantDomain)
            throws CloudBillingException {
        try {
            // Get the workflow resource url
            String workflowUrl = MonetizationConstants.WORKFLOW_EXTENSION_URL;
            Resource workflowResource = CloudBillingUtils.getRegistryResource(tenantDomain, workflowUrl);

            // Get the resource content
            byte[] bytesContent = (byte[]) workflowResource.getContent();
            String content = new String(bytesContent, "UTF-8");
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            InputSource inputSource = new InputSource();
            inputSource.setCharacterStream(new StringReader(content));
            Document doc = documentBuilder.parse(inputSource);
            Node workFlowExtension = doc.getElementsByTagName(MonetizationConstants.TAG_WORKFLOWEXTENSION).item(0);
            // Loop the WorkFlowExtensions child node
            NodeList extensionList = workFlowExtension.getChildNodes();
            for (int i = 0; i < extensionList.getLength(); i++) {
                Node node = extensionList.item(i);
                // Remove the already existing SubscriptionCreation tag
                if (MonetizationConstants.TAG_SUSCRIPTION_CREATION.equals(node.getNodeName())) {
                    workFlowExtension.removeChild(node);
                }
            }
            // Add the new SubscriptionCreation tag
            Element subscriptionCreation = doc.createElement(MonetizationConstants.TAG_SUSCRIPTION_CREATION);
            // Set the Attribute executor
            subscriptionCreation.setAttribute(MonetizationConstants.ATTRIBUTE_EXECUTOR,
                                              MonetizationConstants.SUBSCRIPTION_CREATION_EXECUTOR);
            // Add ServiceUrl Property tag
            Element propertyServiceUrl = doc.createElement(MonetizationConstants.ATTRIBUTE_PROPERTY);
            propertyServiceUrl
                    .setAttribute(MonetizationConstants.ATTRIBUTE_NAME,
                                  MonetizationConstants.PROPERTY_SERVICE_END_POINT);
            // Get ServiceUrl from config files
            BillingConfig billingConfig = BillingConfigUtils.getBillingConfiguration();
            DataServiceConfig dataServiceConfig = billingConfig.getDSConfig();
            String serviceUrlHost = dataServiceConfig.getHttpClientConfig().getHostname();
            int serviceUrlPort = dataServiceConfig.getHttpClientConfig().getPort();
            String serviceURL = BillingConstants.HTTPS + serviceUrlHost + BillingConstants.COLON + serviceUrlPort +
                                MonetizationConstants.PROPERTY_MONETIZATION_SERVICE_VALUE;
            propertyServiceUrl.setTextContent(serviceURL);
            subscriptionCreation.appendChild(propertyServiceUrl);
            // Add Username Property tag
            Element propertyName = doc.createElement(MonetizationConstants.ATTRIBUTE_PROPERTY);
            propertyName
                    .setAttribute(MonetizationConstants.ATTRIBUTE_NAME, MonetizationConstants.PROPERTY_USERNAME_NAME);
            propertyName.setTextContent(tenantUsername);
            subscriptionCreation.appendChild(propertyName);
            // Add Password Property tag
            Element propertyPassword = doc.createElement(MonetizationConstants.ATTRIBUTE_PROPERTY);
            propertyPassword
                    .setAttribute(MonetizationConstants.ATTRIBUTE_NAME, MonetizationConstants.PROPERTY_PASSWORD_NAME);
            propertyPassword.setTextContent(tenantPassword);
            subscriptionCreation.appendChild(propertyPassword);
            workFlowExtension.appendChild(subscriptionCreation);
            // Writing xml file
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, MonetizationConstants.YES);
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(doc);
            // Creating output stream
            transformer.transform(source, result);
            content = result.getWriter().toString();
            // Update the workflow resource
            workflowResource.setContent(content.getBytes("UTF-8"));
            return CloudBillingUtils.putRegistryResource(tenantDomain, workflowUrl, workflowResource);
        } catch (RegistryException | ParserConfigurationException | SAXException | IOException | TransformerException
                e) {
            throw new CloudBillingException("Error occurred while updating the Registry workflowExtensionContent ", e);
        }
    }

}
