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

package org.wso2.carbon.cloud.billing.core.commons.config;

import java.util.Arrays;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Config element that represent the CloudBilling xml object
 */
@XmlRootElement(name = "CloudBilling")
public class BillingConfig {

    private DataServiceConfig dsConfig;
    private SSLConfig sslConfig;
    private UtilsConfig utilsConfig;
    private int trialPeriod;
    private boolean billingEnabled;
    private boolean mgtModeEnabled;
    private Subscription[] subscriptions;
    private String billingVendorClass;

    @XmlElement(name = "BillingEnabled", nillable = false, required = true)
    public boolean isBillingEnabled() {
        return billingEnabled;
    }

    public void setBillingEnabled(boolean billingEnabled) {
        this.billingEnabled = billingEnabled;
    }

    @XmlElement(name = "BillingVendorClass", nillable = false, required = true)
    public String getBillingVendorClass() {
        return billingVendorClass;
    }

    public void setBillingVendorClass(String billingVendorClass) {
        this.billingVendorClass = billingVendorClass;
    }

    @XmlElement(name = "DataServiceAPI", nillable = false, required = true)
    public DataServiceConfig getDSConfig() {
        return dsConfig;
    }

    public void setDSConfig(DataServiceConfig dsConfig) {
        this.dsConfig = dsConfig;
    }

    @XmlElement(name = "TrialPeriod", nillable = false, required = true)
    public int getTrialPeriod() {
        return trialPeriod;
    }

    public void setTrialPeriod(int trialPeriod) {
        this.trialPeriod = trialPeriod;
    }

    @XmlElement(name = "Utils", nillable = false, required = true)
    public UtilsConfig getUtilsConfig() {
        return utilsConfig;
    }

    public void setUtilsConfig(UtilsConfig utilsConfig) {
        this.utilsConfig = utilsConfig;
    }

    @XmlElement(name = "MgtModeEnabled", nillable = false, required = true)
    public boolean isMgtModeEnabled() {
        return mgtModeEnabled;
    }

    public void setMgtModeEnabled(boolean mgtModeEnabled) {
        this.mgtModeEnabled = mgtModeEnabled;
    }

    @XmlElement(name = "SSLRelyingParty", nillable = false, required = true)
    public SSLConfig getSslConfig() {
        return sslConfig;
    }

    public void setSslConfig(SSLConfig sslConfig) {
        this.sslConfig = sslConfig;
    }

    @XmlElementWrapper(name = "Subscriptions", nillable = false, required = true)
    @XmlElement(name = "Subscription", nillable = false, required = true)
    public Subscription[] getSubscriptions() {
        return Arrays.copyOf(subscriptions, subscriptions.length);
    }

    public void setSubscriptions(Subscription[] subscriptions) {
        this.subscriptions = Arrays.copyOf(subscriptions, subscriptions.length);
    }

}
