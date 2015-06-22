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
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Config element that represent the ZuoraAPI xml object
 */
@XmlRootElement(name = "Zuora")
public class ZuoraConfig {

    private int trialPeriod;
    private String user;
    private String password;
    private String currency;
    private String termType;
    private String signatureExpired;
    private HttpClientConfig httpClientConfig;
    private HostedPageConfig hostedPageConfig;
    private APIConfigs apiConfigs;
    private Subscription[] subscriptions;
    private UsageConfig usageConfig;
    private SubscriptionCleanUp subscriptionCleanUp;
    private boolean sslCertificateValid;

    @XmlElement(name = "TrialPeriod", nillable = false)
    public int getTrialPeriod() {
        return trialPeriod;
    }

    public void setTrialPeriod(int trialPeriod) {
        this.trialPeriod = trialPeriod;
    }

    @XmlElement(name = "User", nillable = false)
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @XmlElement(name = "Password", nillable = false)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @XmlElement(name = "Currency", nillable = false)
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @XmlElement(name = "TermType", nillable = false)
    public String getTermType() {
        return termType;
    }

    public void setTermType(String termType) {
        this.termType = termType;
    }

    @XmlElement(name = "SignatureExpired", nillable = false)
    public String getSignatureExpired() {
        return signatureExpired;
    }

    public void setSignatureExpired(String signatureExpired) {
        this.signatureExpired = signatureExpired;
    }

    @XmlElement(name = "HttpClientConfig", nillable = false)
    public HttpClientConfig getHttpClientConfig() {
        return httpClientConfig;
    }

    public void setHttpClientConfig(HttpClientConfig httpClientConfig) {
        this.httpClientConfig = httpClientConfig;
    }

    @XmlElement(name = "HostedPageConfig", nillable = false)
    public HostedPageConfig getHostedPageConfig() {
        return hostedPageConfig;
    }

    public void setHostedPageConfig(HostedPageConfig hostedPageConfig) {
        this.hostedPageConfig = hostedPageConfig;
    }

    @XmlElement(name = "APIs", nillable = false)
    public APIConfigs getApiConfigs() {
        return apiConfigs;
    }

    public void setApiConfigs(APIConfigs apiConfigs) {
        this.apiConfigs = apiConfigs;
    }

    @XmlElementWrapper(name = "Subscriptions", nillable = false, required = true)
    @XmlElement(name = "Subscription", nillable = false, required = true)
    public Subscription[] getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Subscription[] subscriptions) {
        this.subscriptions = subscriptions;
    }

    @XmlElement(name = "Usage", nillable = false, required = true)
    public UsageConfig getUsageConfig() {
        return usageConfig;
    }

    public void setUsageConfig(UsageConfig usageConfig) {
        this.usageConfig = usageConfig;
    }

    @XmlElement(name = "SSLCertificateValid", nillable = false, required = true)
    public boolean isSslCertificateValid() {
        return sslCertificateValid;
    }

    public void setSslCertificateValid(boolean sslCertificateValid) {
        this.sslCertificateValid = sslCertificateValid;
    }

    @XmlElement(name = "SubscriptionCleanUp", nillable = false, required = true)
    public SubscriptionCleanUp getSubscriptionCleanUp() {
        return subscriptionCleanUp;
    }

    public void setSubscriptionCleanUp(SubscriptionCleanUp subscriptionCleanUp) {
        this.subscriptionCleanUp = subscriptionCleanUp;
    }
}
