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


import org.json.JSONException;
import org.json.simple.JSONArray;
import org.wso2.carbon.cloud.billing.beans.AccountUsage;
import org.wso2.carbon.cloud.billing.common.CloudBillingException;
import org.wso2.carbon.cloud.billing.common.config.Plan;
import org.wso2.carbon.cloud.billing.common.zuora.ZuoraUtils;
import org.wso2.carbon.cloud.billing.common.zuora.security.ZuoraHPMUtils;
import org.wso2.carbon.cloud.billing.usage.CloudUsageManager;
import org.wso2.carbon.cloud.billing.utils.CloudBillingUtils;
import org.wso2.carbon.core.AbstractAdmin;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * Represents cloud billing related services.
 */

public class CloudBillingService extends AbstractAdmin {


    private static CloudUsageManager usageManager = new CloudUsageManager();

    public Plan[] getAllSubscriptions(String subscriptionId) throws CloudBillingException {
        return CloudBillingUtils.getSubscriptions(subscriptionId);
    }

    public String getAccountSummary(String accountId) throws CloudBillingException {
        return ZuoraUtils.getAccountSummary(accountId);
    }

    public String getInvoices(String accountId) throws CloudBillingException {
        return ZuoraUtils.getInvoices(accountId);
    }

    public String getPayments(String accountId) throws CloudBillingException {
        return ZuoraUtils.getPayments(accountId);
    }

    public AccountUsage[] getTenantUsageDataForGivenDateRange(String tenantDomain, String productName, String startDate,
                                                              String endDate) throws CloudBillingException {
        return usageManager.getTenantUsageDataForGivenDateRange(
                tenantDomain, productName, startDate, endDate);
    }

    public String getAccountId(String tenantDomain) throws CloudBillingException {
        return CloudBillingUtils.getAccountIdForTenant(tenantDomain);
    }

    public String getSubscriptionId(String tenantDomain) throws CloudBillingException {
        String accountId = CloudBillingUtils.getAccountIdForTenant(tenantDomain);
        return ZuoraUtils.getSubscriptionIdForAccount(accountId);
    }

    public JSONArray getCurrentRatePlan(String tenantDomain, String ProductName) throws CloudBillingException {
        String accountId = CloudBillingUtils.getAccountIdForTenant(tenantDomain);
        return (accountId != null && !"".equals(accountId)) ?
               ZuoraUtils.getCurrentRatePlan(ProductName, accountId) : null;
    }

    public void loadConfig(String username, String password, String configs) throws CloudBillingException {
        try {
            ZuoraHPMUtils.loadConfig(username, password, configs);
        } catch (IOException e) {
            throw new CloudBillingException(e);
        } catch (JSONException e) {
            throw new CloudBillingException(e);
        }
    }

    public String prepareParams() throws CloudBillingException {
        try {
            return ZuoraHPMUtils.prepareParams();
        } catch (Exception e) {
            throw new CloudBillingException(e);
        }
    }

    public void validSignature(String signature, String expirationTime) throws CloudBillingException {
        try {
            ZuoraHPMUtils.validSignature(signature, expirationTime);
        } catch (Exception e) {
            throw new CloudBillingException(e);
        }
    }

    public String generateHash(String data, String mdAlgorithm) throws CloudBillingException {
        try {
            return ZuoraHPMUtils.generateHash(data, mdAlgorithm);
        } catch (Exception e) {
            throw new CloudBillingException(e);
        }
    }

    public boolean validateHash(String data, String hash, String mdAlgorithm) throws CloudBillingException {
        try {
            return ZuoraHPMUtils.validateHash(data, hash, mdAlgorithm);
        } catch (NoSuchProviderException e) {
            throw new CloudBillingException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new CloudBillingException(e);
        }
    }

    public JSONArray getProductRatePlans(String productName) throws CloudBillingException {
        try {
            return ZuoraUtils.getProductRatePlans(productName);
        } catch (CloudBillingException e) {
            throw new CloudBillingException(e);
        }

    }
}
