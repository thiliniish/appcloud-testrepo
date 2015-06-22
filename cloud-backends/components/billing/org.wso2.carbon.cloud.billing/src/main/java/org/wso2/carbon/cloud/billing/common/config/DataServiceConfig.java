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
package org.wso2.carbon.cloud.billing.common.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Config element that represent the DataServiceAPI xml object
 */
@XmlRootElement(name = "DataServiceAPI")
public class DataServiceConfig {

    private String requestCount;
    private String usage;
    private String amendments;
    private String tenantAccount;
    private String pendingDisableTenants;
    private String disableTenant;
    private String subscriptionStatus;
    private String billingHistory;
    private String user;
    private String password;
    private HttpClientConfig httpClientConfig;

    @XmlElement(name = "RequestCount", nillable = false, required = true)
    public String getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(String requestCount) {
        this.requestCount = requestCount;
    }

    @XmlElement(name = "Usage", nillable = false, required = true)
    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    @XmlElement(name = "Amendments", nillable = false, required = true)
    public String getAmendments() {
        return amendments;
    }

    public void setAmendments(String amendments) {
        this.amendments = amendments;
    }

    @XmlElement(name = "TenantAccount", nillable = false, required = true)
    public String getTenantAccount() {
        return tenantAccount;
    }

    public void setTenantAccount(String tenantAccount) {
        this.tenantAccount = tenantAccount;
    }

    @XmlElement(name = "User", nillable = false, required = true)
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @XmlElement(name = "Password", nillable = false, required = true)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @XmlElement(name = "HttpClientConfig", nillable = false)
    public HttpClientConfig getHttpClientConfig() {
        return httpClientConfig;
    }

    public void setHttpClientConfig(HttpClientConfig httpClientConfig) {
        this.httpClientConfig = httpClientConfig;
    }

    @XmlElement(name = "PendingDisableTenants", nillable = false, required = true)
    public String getPendingDisableTenants() {
        return pendingDisableTenants;
    }

    public void setPendingDisableTenants(String pendingDisableTenants) {
        this.pendingDisableTenants = pendingDisableTenants;
    }

    @XmlElement(name = "DisableTenant", nillable = false, required = true)
    public String getDisableTenant() {
        return disableTenant;
    }

    public void setDisableTenant(String disableTenant) {
        this.disableTenant = disableTenant;
    }

    @XmlElement(name = "SubscriptionStatus", nillable = false, required = true)
    public String getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(String subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    @XmlElement(name = "BillingHistory", nillable = false, required = true)
    public String getBillingHistory() {
        return billingHistory;
    }

    public void setBillingHistory(String billingHistory) {
        this.billingHistory = billingHistory;
    }
}
