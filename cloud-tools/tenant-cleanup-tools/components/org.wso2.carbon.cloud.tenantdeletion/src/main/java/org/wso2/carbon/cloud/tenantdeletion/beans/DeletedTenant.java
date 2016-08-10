/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.cloud.tenantdeletion.beans;

/**
 * Bean that  represent the Deleted Tenant with deletion flags
 */
public class DeletedTenant {

    private String tenantDomain;
    private int appFlag;
    private int apiFlag;
    private int configPubstore;
    private int configBps;
    private int configCloudMgt;
    private int configIs;
    private int configSs;
    private int configDas;
    private int configAf;
    private int governanceFlag;
    private int userMgtFlag;
    private int cloudMgtFlag;
    private int ldapFlag;

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public int getAppFlag() {
        return appFlag;
    }

    public void setAppFlag(int appFlag) {
        this.appFlag = appFlag;
    }

    public int getApiFlag() {
        return apiFlag;
    }

    public void setApiFlag(int apiFlag) {
        this.apiFlag = apiFlag;
    }

    public int getConfigPubstore() {
        return configPubstore;
    }

    public void setConfigPubstore(int configPubstore) {
        this.configPubstore = configPubstore;
    }

    public int getConfigBps() {
        return configBps;
    }

    public void setConfigBps(int configBps) {
        this.configBps = configBps;
    }

    public int getConfigCloudMgt() {
        return configCloudMgt;
    }

    public void setConfigCloudMgt(int configCloudMgt) {
        this.configCloudMgt = configCloudMgt;
    }

    public int getConfigIs() {
        return configIs;
    }

    public void setConfigIs(int configIs) {
        this.configIs = configIs;
    }

    public int getConfigSs() {
        return configSs;
    }

    public void setConfigSs(int configSs) {
        this.configSs = configSs;
    }

    public int getConfigDas() {
        return configDas;
    }

    public void setConfigDas(int configDas) {
        this.configDas = configDas;
    }

    public int getConfigAf() {
        return configAf;
    }

    public void setConfigAf(int configAf) {
        this.configAf = configAf;
    }

    public int getGovernanceFlag() {
        return governanceFlag;
    }

    public void setGovernanceFlag(int governanceFlag) {
        this.governanceFlag = governanceFlag;
    }

    public int getUserMgtFlag() {
        return userMgtFlag;
    }

    public void setUserMgtFlag(int userMgtFlag) {
        this.userMgtFlag = userMgtFlag;
    }

    public int getCloudMgtFlag() {
        return cloudMgtFlag;
    }

    public void setCloudMgtFlag(int cloudMgtFlag) {
        this.cloudMgtFlag = cloudMgtFlag;
    }

    public int getLdapFlag() {
        return ldapFlag;
    }

    public void setLdapFlag(int ldapFlag) {
        this.ldapFlag = ldapFlag;
    }
}
