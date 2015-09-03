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
package org.wso2.carbon.cloud.billing.beans.usage;

/**
 * Bean that represent the account usage for a tenant
 */
public class AccountUsage {

    private String tenantDomain;
    private String accountId;
    private String productName;
    private String date;
    private int users;
    private int maxDailyUsage; //500 max api call
    private int usage; // actual usage
    private float overage; // how much this account is over charged ie $10
    private String ratePlan; //$5 -> 1k
    private boolean isPaidAccount;

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getUsers() {
        return users;
    }

    public void setUsers(int users) {
        this.users = users;
    }

    public int getMaxDailyUsage() {
        return maxDailyUsage;
    }

    public void setMaxDailyUsage(int maxDailyUsage) {
        this.maxDailyUsage = maxDailyUsage;
    }

    public int getUsage() {
        return usage;
    }

    public void setUsage(int usage) {
        this.usage = usage;
    }

    public float getOverage() {
        return overage;
    }

    public void setOverage(float overage) {
        this.overage = overage;
    }

    public String getRatePlan() {
        return ratePlan;
    }

    public void setRatePlan(String ratePlan) {
        this.ratePlan = ratePlan;
    }

    public boolean isPaidAccount() {
        return isPaidAccount;
    }

    public void setPaidAccount(boolean isPaidAccount) {
        this.isPaidAccount = isPaidAccount;
    }


    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

}
