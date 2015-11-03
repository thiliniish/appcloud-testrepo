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
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Config element that represent the CloudBilling xml object
 */
@XmlRootElement(name = "CloudBilling")
public class BillingConfig {

    private DataServiceConfig dsConfig;
    private ZuoraConfig zuoraConfig;
    private SSOConfig ssoConfig;
    private UtilsConfig utilsConfig;
    private int trialPeriod;
    private boolean billingEnabled;
    private boolean mgtModeEnabled;

    @XmlElement(name = "BillingEnabled", nillable = false, required = true)
    public boolean isBillingEnabled() {
        return billingEnabled;
    }

    public void setBillingEnabled(boolean billingEnabled) {
        this.billingEnabled = billingEnabled;
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

    @XmlElement(name = "Zoura", nillable = false, required = true)
    public ZuoraConfig getZuoraConfig() {
        return zuoraConfig;
    }

    public void setZuoraConfig(ZuoraConfig zuoraConfig) {
        this.zuoraConfig = zuoraConfig;
    }

    @XmlElement(name = "SSORelyingParty", nillable = false, required = true)
    public SSOConfig getSSOConfig() {
        return ssoConfig;
    }

    public void setSSOConfig(SSOConfig ssoConfig) {
        this.ssoConfig = ssoConfig;
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
}
