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

package org.wso2.carbon.cloud.billing.service;


import org.json.simple.JSONArray;
import org.wso2.carbon.cloud.billing.beans.usage.AccountUsage;
import org.wso2.carbon.cloud.billing.commons.config.Plan;
import org.wso2.carbon.cloud.billing.commons.zuora.ZuoraRESTUtils;
import org.wso2.carbon.cloud.billing.commons.zuora.security.ZuoraHPMUtils;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.utils.CloudBillingUtils;
import org.wso2.carbon.core.AbstractAdmin;

/**
 * Represents cloud billing related services.
 */

public class CloudBillingService extends AbstractAdmin {

    /**
     * Retrieve cloud billing configuration as a json string
     *
     * @return json string
     * @throws CloudBillingException
     */
    public static String getConfigInJson() throws CloudBillingException {
        try {
            return CloudBillingUtils.getConfigInJson();
        } catch (Exception ex) {
            throw new CloudBillingException("Error occurred while getting the configuration in JSON ", ex);
        }
    }

    /**
     * Retrieve payment rate plans associated with a service subscription id
     *
     * @param serviceSubscriptionId subscriptionId (api_cloud/app_cloud)
     * @return Rate plans
     * @throws CloudBillingException
     */
    public Plan[] getPaymentPlansForServiceId(String serviceSubscriptionId) throws CloudBillingException {
        try {
            return CloudBillingUtils.getSubscriptions(serviceSubscriptionId);
        } catch (Exception ex) {
            throw new CloudBillingException("Error occurred while retrieving subscriptions for Id: " +
                                            serviceSubscriptionId, ex);
        }
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
            throw ex;
        } catch (Exception ex) {
            throw new CloudBillingException("Error occurred while retrieving the account summary for Account Id: "
                                            + accountId, ex);
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
            throw ex;
        } catch (Exception ex) {
            throw new CloudBillingException("Error occurred while retrieving invoices for Account Id: " + accountId,
                                            ex);
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
            throw ex;
        } catch (Exception ex) {
            throw new CloudBillingException("Error occurred while retrieving payments for Account Id: " + accountId,
                                            ex);
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
            return CloudBillingUtils.getTenantUsageDataForGivenDateRange(tenantDomain, productName, startDate, endDate);
        } catch (CloudBillingException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CloudBillingException("Error occurred while retrieving usage data of tenant: " + tenantDomain
                                            + "for product: " + productName);
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
            return CloudBillingUtils.getAccountIdForTenant(tenantDomain);
        } catch (CloudBillingException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CloudBillingException("Error occurred while retrieving account Id tenant: " + tenantDomain);
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
            String accountId = CloudBillingUtils.getAccountIdForTenant(tenantDomain);
            return ZuoraRESTUtils.getSubscriptionIdForAccount(accountId, serviceId);
        } catch (CloudBillingException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CloudBillingException("Error occurred while retrieving subscription id for tenant: " +
                                            tenantDomain);
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
            String accountId = CloudBillingUtils.getAccountIdForTenant(tenantDomain);
            return (accountId != null && !accountId.isEmpty()) ?
                   ZuoraRESTUtils.getCurrentRatePlan(productName, accountId) : null;
        } catch (CloudBillingException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CloudBillingException("Error occurred while retrieving the current rate plan of the tenant: "
                                            + tenantDomain + " for subscription: " + productName);
        }
    }

    /**
     * Prepare access parameters required for client to query iframe
     *
     * @return json string of parameters
     * @throws CloudBillingException
     */
    public String prepareParams() throws CloudBillingException {
        return ZuoraHPMUtils.prepareParams();
    }

    /**
     * validate the signature which returned from zuora
     *
     * @param signature      signature
     * @param expirationTime expiration time
     * @throws CloudBillingException
     */
    public void validateSignature(String signature, String expirationTime) throws CloudBillingException {
        ZuoraHPMUtils.validateSignature(signature, expirationTime);
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
        } catch (Exception e) {
            throw new CloudBillingException("Error occurred while generating hash value: ");
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
        return ZuoraHPMUtils.validateHash(data, hash, mdAlgorithm);
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
            throw ex;
        } catch (Exception ex) {
            throw new CloudBillingException("Error occurred while retrieving product rate plans for product: "
                                            + productName);
        }
    }

    /**
     * Validate rate plan id
     *
     * @param serviceId         serviceId
     * @param productRatePlanId rate plan id
     * @return success boolean
     * @throws CloudBillingException
     */
    public boolean validateRatePlanId(String serviceId, String productRatePlanId) throws CloudBillingException {
        try {
            return CloudBillingUtils.validateRatePlanId(serviceId, productRatePlanId);
        } catch (Exception ex) {
            throw new CloudBillingException("Error occurred while validating the rate plan: " + productRatePlanId
                                            + " for service: " + serviceId);
        }
    }

    /**
     * Validate service id
     *
     * @param serviceId service Id
     * @return success boolean
     * @throws CloudBillingException
     */
    public boolean validateServiceId(String serviceId) throws CloudBillingException {
        try {
            return CloudBillingUtils.validateServiceId(serviceId);
        } catch (Exception ex) {
            throw new CloudBillingException("Error occurred while validating the service id: " + serviceId);
        }
    }

    /**
     * Method to get that the billing functionality enable/disable status
     *
     * @return billing enable/disable status
     */
    public boolean isBillingEnabled() {
        return CloudBillingUtils.isBillingEnabled();
    }
}
