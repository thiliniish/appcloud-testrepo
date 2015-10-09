/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

    public static String getConfigInJson() throws CloudBillingException {
        try {
            return CloudBillingUtils.getConfigInJson();
        } catch (Exception ex) {
            throw new CloudBillingException("Error occurred while getting the configuration in JSON ", ex);
        }
    }

    public Plan[] getAllSubscriptions(String subscriptionId) throws CloudBillingException {
        try {
            return CloudBillingUtils.getSubscriptions(subscriptionId);
        } catch (Exception ex) {
            throw new CloudBillingException("Error occurred while retrieving subscriptions for Id: " +
                                            subscriptionId, ex);
        }
    }

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

    public String getAccountId(String tenantDomain) throws CloudBillingException {
        try {
            return CloudBillingUtils.getAccountIdForTenant(tenantDomain);
        } catch (CloudBillingException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CloudBillingException("Error occurred while retrieving account Id tenant: " + tenantDomain);
        }
    }

    public String getSubscriptionId(String tenantDomain) throws CloudBillingException {
        try {
            String accountId = CloudBillingUtils.getAccountIdForTenant(tenantDomain);
            return ZuoraRESTUtils.getSubscriptionIdForAccount(accountId);
        } catch (CloudBillingException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CloudBillingException("Error occurred while retrieving subscription id for tenant: " +
                                            tenantDomain);
        }
    }

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

    public String prepareParams() throws CloudBillingException {
        return ZuoraHPMUtils.prepareParams();
    }

    public void validSignature(String signature, String expirationTime) throws CloudBillingException {
        ZuoraHPMUtils.validSignature(signature, expirationTime);
    }

    public String generateHash(String data, String mdAlgorithm) throws CloudBillingException {
        try {
            return ZuoraHPMUtils.generateHash(data, mdAlgorithm);
        } catch (Exception e) {
            throw new CloudBillingException("Error occurred while generating hash value: ");
        }
    }

    public boolean validateHash(String data, String hash, String mdAlgorithm) throws CloudBillingException {
        return ZuoraHPMUtils.validateHash(data, hash, mdAlgorithm);
    }

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

    public boolean validateRatePlanId(String serviceId, String productRatePlanId) throws CloudBillingException {
        try {
            return CloudBillingUtils.validateRatePlanId(serviceId, productRatePlanId);
        } catch (Exception ex) {
            throw new CloudBillingException("Error occurred while validating the rate plan: " + productRatePlanId
                                            + " for service: " + serviceId);
        }
    }

    public boolean validateServiceId(String serviceId) throws CloudBillingException {
        try {
            return CloudBillingUtils.validateServiceId(serviceId);
        } catch (Exception ex) {
            throw new CloudBillingException("Error occurred while validating the service id: " + serviceId);
        }
    }

    public boolean isBillingEnable() {
        return CloudBillingUtils.isBillingEnable();
    }
}
