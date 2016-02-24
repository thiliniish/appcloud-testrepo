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

package org.wso2.cloud.heartbeat.monitor.modules.common.tests;

import org.quartz.*;
import org.wso2.cloud.heartbeat.monitor.modules.utils.LoginUtils;
import org.wso2.cloud.heartbeat.monitor.modules.utils.LoginUtilsBean;

/**
 * Tenant login test scenario for a Cloud setup, implemented in this class
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class TenantLoginTest implements Job {

    private String tenantUser;
    private String tenantUserPwd;
    private String hostName;
    private String serviceName;
    private String loginTestSeverity = "2";

    /**
     * Overrides execute method
     * @param jobExecutionContext "hostName" ,"tenantUser", "tenantUserPwd" "serviceName" params passed via JobDataMap.
     * @throws org.quartz.JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LoginUtils loginUtils = new LoginUtils();
        LoginUtilsBean loginUtilsBean = new LoginUtilsBean();

        loginUtilsBean.setLoginTestSeverity(loginTestSeverity);
        loginUtilsBean.setTenantUser(tenantUser);
        loginUtilsBean.setTenantUserPwd(tenantUserPwd);
        loginUtilsBean.setHostName(hostName);
        loginUtilsBean.setServerName(serviceName);

        loginUtils.initializeLoginTest(loginUtilsBean);
        loginUtils.login();
    }

    /**
     * Sets tenant user name
     * @param tenantUser tenant user name
     */
    public void setTenantUser(String tenantUser) {
        this.tenantUser = tenantUser;
    }

    /**
     * Sets tenant user password
     * @param tenantUserPwd tenant user password
     */
    public void setTenantUserPwd(String tenantUserPwd) {
        this.tenantUserPwd = tenantUserPwd;
    }

    /**
     * Sets service host
     * @param hostName service host
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * Sets service name
     * @param serviceName service name
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}