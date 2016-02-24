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

package org.wso2.cloud.heartbeat.monitor.modules.utils;

/**
 * This is a bean class
 */
public class LoginUtilsBean implements java.io.Serializable {
    private String tenantUser;
    private String tenantUserPwd;
    private String hostName;
    private String serverName;
    private String loginTestSeverity;

    public LoginUtilsBean() {
    }

    /**
     * Sets tenant user name
     * @param tenantUser tenant user name
     */
    public void setTenantUser(String tenantUser) {
        this.tenantUser = tenantUser;
    }

    /**
     * Gets tenant user
     * @return tenantUser
     */
    public String getTenantUser() {
        return tenantUser;
    }

    /**
     * Sets tenant user password
     * @param tenantUserPwd tenant user password
     */
    public void setTenantUserPwd(String tenantUserPwd) {
        this.tenantUserPwd = tenantUserPwd;
    }

    /**
     * Gets tenant user password
     * @return tenantUserPwd
     */
    public String getTenantUserPwd() {
        return tenantUserPwd;
    }

    /**
     * Sets host name
     * @param hostName host name
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * Gets host name
     * @return hostName
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * Sets server name
     * @param serverName server name
     */
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    /**
     * Gets server name
     * @return server name
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Sets login test severity
     * @param loginTestSeverity login test severity
     */
    public void setLoginTestSeverity(String loginTestSeverity) {
        this.loginTestSeverity = loginTestSeverity;
    }

    /**
     * Gets login test severity
     * @return login test severity
     */
    public String getLoginTestSeverity() {
        return loginTestSeverity;
    }
}