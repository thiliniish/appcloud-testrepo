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
    private int config_PUBSTORE;
    private int config_BPS;
    private int config_CLOUD_MGT;
    private int config_IS;
    private int config_SS;
    private int config_DAS;
    private int config_AF;
    private int governanceFlag;
    private int userMgtFlag;
    private int cloudMgtFlag;
    private int LDAPFlag;

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

    public int getConfig_PUBSTORE() {
        return config_PUBSTORE;
    }

    public void setConfig_PUBSTORE(int config_PUBSTORE) {
        this.config_PUBSTORE = config_PUBSTORE;
    }

    public int getConfig_BPS() {
        return config_BPS;
    }

    public void setConfig_BPS(int config_BPS) {
        this.config_BPS = config_BPS;
    }

    public int getConfig_CLOUD_MGT() {
        return config_CLOUD_MGT;
    }

    public void setConfig_CLOUD_MGT(int config_CLOUD_MGT) {
        this.config_CLOUD_MGT = config_CLOUD_MGT;
    }

    public int getConfig_IS() {
        return config_IS;
    }

    public void setConfig_IS(int config_IS) {
        this.config_IS = config_IS;
    }

    public int getConfig_SS() {
        return config_SS;
    }

    public void setConfig_SS(int config_SS) {
        this.config_SS = config_SS;
    }

    public int getConfig_DAS() {
        return config_DAS;
    }

    public void setConfig_DAS(int config_DAS) {
        this.config_DAS = config_DAS;
    }

    public int getConfig_AF() {
        return config_AF;
    }

    public void setConfig_AF(int config_AF) {
        this.config_AF = config_AF;
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

    public int getLDAPFlag() {
        return LDAPFlag;
    }

    public void setLDAPFlag(int LDAPFlag) {
        this.LDAPFlag = LDAPFlag;
    }
}
