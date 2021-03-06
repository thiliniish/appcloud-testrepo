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

package org.wso2.carbon.cloud.billing.commons.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Config element that represent the ZuoraAPI xml object
 */
@XmlRootElement(name = "Zuora") public class ZuoraConfig {

    private String user;
    private String password;
    private String currency;
    private long sessionExpired;
    private String termType;
    private String signatureExpired;
    private String sslEnabledProtocols;
    private HttpClientConfig httpClientConfig;
    private String serviceUrlHost;
    private HostedPageConfig hostedPageConfig;
    private Subscription[] subscriptions;
    private UsageConfig usageConfig;
    private SubscriptionCleanUp subscriptionCleanUp;

    @XmlElement(name = "User", nillable = false) public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @XmlElement(name = "Password", nillable = false) public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @XmlElement(name = "Currency", nillable = false) public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @XmlElement(name = "SessionExpired", nillable = false) public long getSessionExpired() {
        return sessionExpired;
    }

    public void setSessionExpired(long sessionExpired) {
        this.sessionExpired = sessionExpired;
    }

    @XmlElement(name = "TermType", nillable = false) public String getTermType() {
        return termType;
    }

    public void setTermType(String termType) {
        this.termType = termType;
    }

    @XmlElement(name = "SignatureExpired", nillable = false) public String getSignatureExpired() {
        return signatureExpired;
    }

    public void setSignatureExpired(String signatureExpired) {
        this.signatureExpired = signatureExpired;
    }

    @XmlElement(name = "EnabledProtocols", nillable = false) public String getEnabledProtocols() {
        return sslEnabledProtocols;
    }

    public void setEnabledProtocols(String enabledProtocols) {
        this.sslEnabledProtocols = enabledProtocols;
    }

    @XmlElement(name = "HttpClientConfig", nillable = false) public HttpClientConfig getHttpClientConfig() {
        return httpClientConfig;
    }

    public void setHttpClientConfig(HttpClientConfig httpClientConfig) {
        this.httpClientConfig = httpClientConfig;
    }

    @XmlElement(name = "HostedPage", nillable = false) public HostedPageConfig getHostedPageConfig() {
        return hostedPageConfig;
    }

    public void setHostedPageConfig(HostedPageConfig hostedPageConfig) {
        this.hostedPageConfig = hostedPageConfig;
    }

    @XmlElement(name = "ServiceURLHost", nillable = false) public String getServiceUrlHost() {
        return serviceUrlHost;
    }

    public void setServiceUrlHost(String serviceUrlHost) {
        this.serviceUrlHost = serviceUrlHost;
    }

    @XmlElementWrapper(name = "Subscriptions", nillable = false, required = true) @XmlElement(name = "Subscription",
            nillable = false, required = true) public Subscription[] getSubscriptions() {
        Subscription[] subs = null;
        if (subscriptions != null) {
            subs = subscriptions.clone();
        }
        return subs;
    }

    public void setSubscriptions(Subscription[] subscriptions) {
        this.subscriptions = subscriptions.clone();
    }

    @XmlElement(name = "Usage", nillable = false, required = true) public UsageConfig getUsageConfig() {
        return usageConfig;
    }

    public void setUsageConfig(UsageConfig usageConfig) {
        this.usageConfig = usageConfig;
    }

    @XmlElement(name = "SubscriptionCleanUp", nillable = false, required = true) public SubscriptionCleanUp
    getSubscriptionCleanUp() {
        return subscriptionCleanUp;
    }

    public void setSubscriptionCleanUp(SubscriptionCleanUp subscriptionCleanUp) {
        this.subscriptionCleanUp = subscriptionCleanUp;
    }
}
