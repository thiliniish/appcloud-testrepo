/*
 *  Copyright (c) 2015-2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.cloud.billing.service;

import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.json.simple.JSONObject;
import org.wso2.carbon.cloud.billing.beans.usage.AccountUsage;
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.commons.MonetizationConstants;
import org.wso2.carbon.cloud.billing.commons.config.BillingConfig;
import org.wso2.carbon.cloud.billing.commons.config.DataServiceConfig;
import org.wso2.carbon.cloud.billing.commons.config.Plan;
import org.wso2.carbon.cloud.billing.commons.notifications.EmailNotifications;
import org.wso2.carbon.cloud.billing.commons.utils.BillingConfigUtils;
import org.wso2.carbon.cloud.billing.commons.utils.CloudBillingUtils;
import org.wso2.carbon.cloud.billing.commons.zuora.ZuoraRESTUtils;
import org.wso2.carbon.cloud.billing.commons.zuora.security.ZuoraHPMUtils;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingZuoraException;
import org.wso2.carbon.cloud.billing.exceptions.CloudMonetizationException;
import org.wso2.carbon.cloud.billing.utils.APICloudMonetizationUtils;
import org.wso2.carbon.cloud.billing.utils.CloudBillingServiceUtils;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Represents cloud billing related services.
 */

public class CloudBillingService extends AbstractAdmin {
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
     * Retrieve billing account summary
     *
     * @param accountId Account Id of the customer
     * @return Json string of account summary
     * @throws CloudBillingException
     */
    public String getAccountSummary(String accountId) throws CloudBillingException {
        try {
            return ZuoraRESTUtils.getAccountSummary(accountId);
        } catch (CloudBillingException ex) {
            LOGGER.error("Error occurred while retrieving the account summary for Account Id: " + accountId, ex);
            throw ex;
        }
    }

    /**
     * Update Account Info
     *
     * @param accountId       Account Id
     * @param accountInfoJson account details
     * @return success Json string
     * @throws CloudBillingException
     */
    public String updateAccount(String accountId, String accountInfoJson) throws CloudBillingException {
        try {
            return ZuoraRESTUtils.updateAccount(accountId, accountInfoJson);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while updating the account for Account Id: " + accountId;
            LOGGER.error(message, ex);
            throw new CloudBillingException(message + ex);
        }
    }

    /**
     * Create the Account
     *
     * @param accountInfoJson account details
     * @return success Json string
     * @throws CloudBillingException
     */
    public String createAccount(String accountInfoJson) throws CloudBillingException {
        try {
            return ZuoraRESTUtils.createAccount(accountInfoJson);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while creating the account";
            LOGGER.error(message, ex);
            throw new CloudBillingException(message + ex);
        }
    }

    /**
     * Update subscription
     *
     * @param subscriptionId       subscription id
     * @param subscriptionInfoJson subscription details
     * @return success Json string
     * @throws CloudBillingException
     */
    public String updateSubscription(String subscriptionId, String subscriptionInfoJson) throws CloudBillingException {
        try {
            return ZuoraRESTUtils.updateSubscription(subscriptionId, subscriptionInfoJson);
        } catch (CloudBillingException ex) {
            String message =
                    "Error occurred while updating the subscription details for Subscription Id: " + subscriptionId;
            LOGGER.error(message, ex);
            throw new CloudBillingException(message + ex);
        }
    }

    /**
     * Retrieve billing account details
     *
     * @param accountId Account Id
     * @return success Json string
     * @throws CloudBillingException
     */
    public String getAccountDetails(String accountId) throws CloudBillingException {
        try {
            return ZuoraRESTUtils.getAccountDetails(accountId);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while retrieving the account details for Account Id: " + accountId;
            LOGGER.error(message, ex);
            throw new CloudBillingException(message + ex);
        }
    }

    /**
     * Remove payment method
     *
     * @param methodId payment method id
     * @return Json string of account
     * @throws CloudBillingException
     */
    public String removePaymentMethod(String methodId) throws CloudBillingException {
        try {
            return ZuoraRESTUtils.removePaymentMethod(methodId);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while removing the payment method for Method Id: " + methodId;
            LOGGER.error(message, ex);
            throw new CloudBillingException(message + ex);
        }
    }

    /**
     * Retrieve all payment methods
     *
     * @param accountId account id
     * @return success Json string
     * @throws CloudBillingException
     */
    public String getAllPaymentMethods(String accountId) throws CloudBillingException {
        try {
            return ZuoraRESTUtils.getAllPaymentMethods(accountId);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while retrieving all payment methods for Account Id: " + accountId;
            LOGGER.error(message, ex);
            throw new CloudBillingException(message + ex);
        }
    }

    /**
     * Update default payment method
     *
     * @param methodId              method id
     * @param paymentMethodInfoJson payment method details
     * @return success Json string
     * @throws CloudBillingException
     */
    public String updateDefaultPaymentMethod(String methodId, String paymentMethodInfoJson)
            throws CloudBillingException {
        try {
            return ZuoraRESTUtils.updateDefaultPaymentMethod(methodId, paymentMethodInfoJson);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while updating the default payment methods for Method Id: " + methodId;
            LOGGER.error(message, ex);
            throw new CloudBillingException(message + ex);
        }
    }

    /**
     * Retrieve invoices associated with a accountId
     *
     * @param accountId customer accountId
     * @return Json String of invoices
     * @throws CloudBillingException
     */
    public String getInvoices(String accountId) throws CloudBillingException {
        try {
            return ZuoraRESTUtils.getInvoices(accountId);
        } catch (CloudBillingException ex) {
            LOGGER.error("Error occurred while retrieving invoices for Account Id: " + accountId, ex);
            throw ex;
        }
    }

    /**
     * Retrieve payments done
     *
     * @param accountId account Id of the customer
     * @return Json String of payments
     * @throws CloudBillingException
     */
    public String getPayments(String accountId) throws CloudBillingException {
        try {
            return ZuoraRESTUtils.getPayments(accountId);
        } catch (CloudBillingException ex) {
            LOGGER.error("Error occurred while retrieving payments for Account Id: " + accountId, ex);
            throw ex;
        }
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
            return CloudBillingServiceUtils.getTenantUsageDataForGivenDateRange(tenantDomain, productName, startDate,
                    endDate);
        } catch (CloudBillingException ex) {
            LOGGER.error("Error occurred while retrieving usage data of tenant: " + tenantDomain + "for product: " +
                    productName, ex);
            throw ex;
        }
    }

    /**
     * Retrieve zuora accountId for tenant domain
     *
     * @param tenantDomain tenant domain
     * @return string zuora accountId
     * @throws CloudBillingException
     */
    public String getAccountId(String tenantDomain) throws CloudBillingException {
        try {
            return CloudBillingServiceUtils.getAccountIdForTenant(tenantDomain);
        } catch (CloudBillingException ex) {
            LOGGER.error("Error occurred while retrieving account Id tenant: " + tenantDomain, ex);
            throw ex;
        }
    }

    /**
     * Get zuora subscription id for a service subscription
     *
     * @param tenantDomain tenant domain
     * @return subscription id
     * @throws CloudBillingException
     */
    public String getSubscriptionId(String tenantDomain, String serviceId) throws CloudBillingException {
        try {
            String accountId = CloudBillingServiceUtils.getAccountIdForTenant(tenantDomain);
            return ZuoraRESTUtils.getSubscriptionIdForAccount(accountId, serviceId);
        } catch (CloudBillingException ex) {
            LOGGER.error("Error occurred while retrieving subscription id for tenant: " + tenantDomain, ex);
            throw ex;
        }
    }

    /**
     * Get current plan subscribed to a service
     *
     * @param tenantDomain tenant domain
     * @param productName  subscribed service
     * @return current rate plan list (this is a list because it contains coupon plans as well)
     * @throws CloudBillingException
     */
    public JSONArray getCurrentRatePlan(String tenantDomain, String productName) throws CloudBillingException {
        try {
            String accountId = CloudBillingServiceUtils.getAccountIdForTenant(tenantDomain);
            return (accountId != null && !accountId.isEmpty()) ?
                    ZuoraRESTUtils.getCurrentRatePlan(productName, accountId) : null;
        } catch (CloudBillingException ex) {
            LOGGER.error("Error occurred while retrieving the current rate plan of the tenant: " + tenantDomain + " " +
                    "for subscription: " + productName, ex);
            throw ex;
        }
    }

    /**
     * Prepare access parameters required for client to query iframe
     *
     * @return json string of parameters
     * @throws CloudBillingException
     */
    public String prepareParams() throws CloudBillingException {
        try {
            return ZuoraHPMUtils.prepareParams();
        } catch (CloudBillingException ex) {
            LOGGER.error("Error while preparing access parameters", ex);
            throw ex;
        }
    }

    /**
     * validate the signature which returned from zuora
     *
     * @param signature      signature
     * @param expirationTime expiration time
     * @throws CloudBillingException
     */
    public void validateSignature(String signature, String expirationTime) throws CloudBillingException {
        try {
            ZuoraHPMUtils.validateSignature(signature, expirationTime);
        } catch (CloudBillingException ex) {
            LOGGER.error("Error occurred while validating the signature. ", ex);
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
            return ZuoraHPMUtils.generateHash(data, mdAlgorithm);
        } catch (CloudBillingException ex) {
            LOGGER.error("Error occurred while generating hash value ", ex);
            throw ex;
        }
    }

    /**
     * Validate hash
     *
     * @param data        data
     * @param hash        hash
     * @param mdAlgorithm MDA algorithm
     * @return success boolean
     * @throws CloudBillingException
     */
    public boolean validateHash(String data, String hash, String mdAlgorithm) throws CloudBillingException {
        try {
            return ZuoraHPMUtils.validateHash(data, hash, mdAlgorithm);
        } catch (CloudBillingException ex) {
            LOGGER.error("Error while validating the hash value. ", ex);
            throw ex;
        }
    }

    /**
     * Retrieve product rate plans from zuora associated for product name
     *
     * @param productName product name
     * @return Json array of product rate plans retrieved from zuora
     * @throws CloudBillingException
     */
    public JSONArray getProductRatePlans(String productName) throws CloudBillingException {
        try {
            return ZuoraRESTUtils.getProductRatePlans(productName);
        } catch (CloudBillingException ex) {
            String message = "Error occurred while retrieving product rate plans for product: " + productName;
            LOGGER.error(message, ex);
            throw new CloudBillingException(message, ex);
        }
    }

    /**
     * Get product rate plan object from zuora for rate plan name
     *
     * @param productName product name
     * @param ratePlanName rate plan name
     * @return Json object of product rate plan for given product rate plan name
     * @throws CloudBillingException
     */
    public JSONObject getProductRatePlanObject(String productName, String ratePlanName) throws CloudBillingException {
        return ZuoraRESTUtils.getProductRatePlanObject(productName, ratePlanName);
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
     * Add parent to the account.
     *
     * @param childAccountNo  child account name
     * @param parentAccountNo parent account no
     * @return json object in String
     * {
     *    "errors": null,
     *    "errorsSpecified": false,
     *    "id": {
     *        "id": "2c92c0fb5133f6380151439c0980718d"
     *    },
     *   "idSpecified": true,
     *    "success": true,
     *    "successSpecified": true
     * }
     * <p/>
     * or
     * <p/>
     * {
     *    "errors": [
     *        {
     *            "code": {
     *                "value": "INVALID_VALUE"
     *            },
     *            "codeSpecified": true,
     *            "field": null,
     *            "fieldSpecified": false,
     *            "message": "The account number T-1444288585567 is invalid.",
     *            "messageSpecified": true
     *        }
     *    ],
     *    "errorsSpecified": true,
     *    "id": null,
     *    "idSpecified": false,
     *    "success": false,
     *    "successSpecified": true
     * }
     * @throws CloudBillingException
     */
    public String addAccountParent(String childAccountNo, String parentAccountNo) throws CloudBillingException {
        try {
            JsonObject result = CloudBillingServiceUtils.addAccountParent(childAccountNo, parentAccountNo);
            return result.toString();
        } catch (CloudBillingException ex) {
            LOGGER.error("Error occurred while adding parent to the account : ", ex);
            throw ex;
        }
    }

    /**
     * Create child account
     *
     * @param tenantDomain    tenant domain
     * @param accountInfoJson child account information in json
     * {
     *     "autoPay": true,
     *     "billToContact": {
     *         "address1": "1967",
     *         "address2": "",
     *         "city": "Seattle",
     *         "country": "US",
     *         "firstName": "Chevy",
     *         "lastName": "Impala",
     *         "state": "WA",
     *         "workEmail": "rajith.siri.wardana@gmail.com",
     *         "zipCode": "98057"
     *     },
     *     "currency": "USD",
     *     "hpmCreditCardPaymentMethodId": "2c92c0f8516cc1a501517aea10e138f2",
     *     "invoiceCollect": true,
     *     "invoiceTargetDate": "2015-12-07",
     *     "name": "Chevy Impala",
     *     "subscription": {
     *         "autoRenew": true,
     *         "contractEffectiveDate": "2015-12-07",
     *         "subscribeToRatePlans": [
     *             {
     *                 "productRatePlanId": "2c92c0f84b0795b8014b0b1ac63e6713"
     *             }
     *         ],
     *         "termType": "EVERGREEN"
     *     }
     * }
     * @return Json string as follows
     * {
     *     "addParentResponse": {
     *         "errors": null,
     *         "errorsSpecified": false,
     *         "id": {
     *             "id": "2c92c0fa516cc1fc01517c125eae08c7"
     *         },
     *         "idSpecified": true,
     *         "success": true,
     *         "successSpecified": true
     *     },
     *     "createChildResponse": {
     *         "accountId": "2c92c0fa516cc1fc01517c125eae08c7",
     *         "accountNumber": "A00000579",
     *         "contractedMrr": 700.0,
     *         "invoiceId": "2c92c0fa516cc1fc01517c125fab08db",
     *         "paidAmount": 700.0,
     *         "paymentId": "2c92c0fa516cc1fc01517c125ff308e5",
     *         "paymentMethodId": "2c92c0f9516ccc6c01517c11f35051ec",
     *         "subscriptionId": "2c92c0fa516cc1fc01517c125f0008d2",
     *         "subscriptionNumber": "A-S00000588",
     *         "success": true,
     *         "totalContractedValue": null
     *     }
     * }
     * @throws CloudBillingException
     */
    public String createChildAccount(String tenantDomain, String accountInfoJson) throws CloudBillingException {
        try {
            JsonObject result = CloudBillingServiceUtils.createChildAccount(tenantDomain, accountInfoJson);
            return result.toString();
        } catch (CloudBillingException ex) {
            LOGGER.error("Error occurred while creating child account under tenant: " + tenantDomain, ex);
            throw ex;
        }
    }

    /**
     * Delete zuora account by name
     *
     * @param accountName account name
     * @return success json string
     * {
     *     "errors": null,
     *     "errorsSpecified": false,
     *     "id": {
     *         "id": "2c92c0f8501d4405015046de02cf0542"
     *     },
     *     "idSpecified": true,
     *     "success": true,
     *     "successSpecified": true
     * }
     * @throws CloudBillingZuoraException
     */
    public String deleteAccount(String accountName) throws CloudBillingException {
        try {
            JsonObject result = CloudBillingServiceUtils.deleteAccount(accountName);
            return result.toString();
        } catch (CloudBillingException ex) {
            LOGGER.error("Error occurred while deleting account: " + accountName, ex);
            throw ex;
        }
    }

    /**
     * Cancel subscription by the subscription number
     *
     * @param subscriptionNumber   subscription number
     * @param subscriptionInfoJson subscription information in json
     * @return
     * @throws CloudBillingException
     */
    public String cancelSubscription(String subscriptionNumber, String subscriptionInfoJson) throws CloudBillingException {
        try {
            return ZuoraRESTUtils.cancelSubscription(subscriptionNumber, subscriptionInfoJson);
        } catch (CloudBillingException ex) {
            LOGGER.error("Error while cancelling the subscription. subscription no: " + subscriptionNumber, ex);
            throw ex;
        }
    }

    /**
     * Send notification emails for billing service
     *
     * @param receiver receiver email address
     * @param subject email subject
     * @param msgBody email body
     */
    public void sendEmailNotification(String receiver, String subject, String msgBody) {
        EmailNotifications.getInstance().sendMail(msgBody, subject, receiver);
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
    public boolean enableMonetization(String tenantDomain, String tenantPassword, String tenantDisplayName)
            throws CloudBillingException {
        boolean status = false;
        // Create the zuoara Product
        boolean createProductStatus = createProduct(tenantDomain);
        if (createProductStatus) {
            //   Add subscriptionCreation element to workflowExtension.xml in registry
            status = true;
            if (!updateWorkFlow(tenantPassword, tenantDisplayName,tenantDomain)) {
                LOGGER.error("Registry WorkflowExtension.xml update failed while enabling Monetization.");
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
    public boolean updateWorkFlow(String tenantPassword, String tenantUsername, String tenantDomain) throws CloudBillingException {
        try {
            // Get the workflow resource url
            String workflowUrl = MonetizationConstants.WORKFLOW_EXTENSION_URL;
            Resource workflowResource = CloudBillingUtils
                    .getRegistryResource(tenantDomain, workflowUrl);

            // Get the resource content
            String content = new String((byte[]) workflowResource.getContent());
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
                    .setAttribute(MonetizationConstants.ATTRIBUTE_NAME, MonetizationConstants.PROPERTY_SERVICE_END_POINT);
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
            workflowResource.setContent(content.getBytes());
            return CloudBillingUtils.putRegistryResource(tenantDomain, workflowUrl, workflowResource);
        } catch (RegistryException | ParserConfigurationException | SAXException | IOException | TransformerException e) {
            throw new CloudBillingException("Error occurred while updating the Registry workflowExtensionContent " + e);
        }
    }

    /**
     * Method to create Product
     *
     * @param tenantDomain tenant domain
     * @return status of the product creation task
     * @throws CloudBillingException
     */
    public boolean createProduct(String tenantDomain) throws CloudBillingException {
        return CloudBillingServiceUtils.createProduct(tenantDomain);
    }

    /**
     * Method to create Product rate plan for a Product
     *
     * @param tenantDomain tenantDomain
     * @param ratePlanName ratePlanName
     * @return status of product creation
     * @throws CloudBillingException
     */
    public JsonObject createProductRatePlan(String tenantDomain, String ratePlanName, String price,
                                            String overageCharge, String description)
            throws CloudBillingException {
        try {
            return CloudBillingServiceUtils.createProductRatePlan(tenantDomain, ratePlanName, price, overageCharge,
                                                                  description);
        } catch (CloudBillingException e) {
            throw new CloudBillingException("Error occurred while getting the Registry throttling tiers " + e);
        }
    }

    /**
     * Method to get the throttling tiers of the tenant
     *
     * @param tenantDomain tenant domain
     * @return JSONArray of throttling tiers
     * {
     *     Gold, Silver, Bronze ......
     * }
     * @throws CloudMonetizationException
     */
    public JSONArray getThrottlingTiersOfTenant(String tenantDomain) throws CloudMonetizationException {
        return APICloudMonetizationUtils.getThrottlingTiersOfTenant(tenantDomain);
    }

    /**
     * Get the Product Catalogue information
     *
     * @param productName  product name
     * @param tenantDomain tenantDomain
     * @return product
     * @throws CloudBillingException
     */
    public JsonObject queryProduct(String tenantDomain, String productName) throws CloudBillingException {
        return CloudBillingServiceUtils.queryProduct(tenantDomain, productName);
    }

    /**
     * Encrypt texts using the default Crypto utility. and base 64 encode
     *
     * @param text text need to be encrypt
     * @return base64encoded encrypted string
     * @throws org.wso2.carbon.core.util.CryptoException
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
     * @throws org.wso2.carbon.core.util.CryptoException
     */
    public String getDecryptedInfo(String base64CyperText) throws CloudBillingException {
        try {
            return CloudBillingServiceUtils.getDecryptedInfo(base64CyperText);
        } catch (CryptoException | IOException e) {
            throw new CloudBillingException("Error occurred while decrypting ", e);
        }
    }

}
