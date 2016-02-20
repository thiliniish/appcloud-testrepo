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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server login test scenario for a Cloud setup, implemented in this class
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ServerLoginTest implements Job {

    private Map<String, String> hostNames = new HashMap<String, String>();
    private String tenantUser;
    private String tenantUserPwd;
    private String loginTestSeverity = "2";

    /**
     * Overrides execute method
     * @param jobExecutionContext "hostNames" ,"tenantUser", "tenantUserPwd", "serverName" params passed via JobDataMap.
     * @throws org.quartz.JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        int numOfServers = hostNames.size();
        ExecutorService executorService = Executors.newFixedThreadPool(numOfServers);

        for (final Map.Entry<String, String> entry : hostNames.entrySet()) {
            Runnable loginRunnable = new Runnable() {
                public void run() {
                    LoginUtilsBean loginUtilsBean = new LoginUtilsBean();
                    loginUtilsBean.setServerName(entry.getKey());
                    loginUtilsBean.setHostName(entry.getValue());
                    loginUtilsBean.setTenantUser(tenantUser);
                    loginUtilsBean.setTenantUserPwd(tenantUserPwd);
                    loginUtilsBean.setLoginTestSeverity(loginTestSeverity);

                    LoginUtils loginUtils = new LoginUtils();
                    loginUtils.initializeLoginTest(loginUtilsBean);
                    loginUtils.login();
                }
            };
            executorService.execute(loginRunnable);
        }
        executorService.shutdown();
    }

    /**
     * Sets host names and relevant IP addresses.
     * @param hostNames service host
     */
    public void setHostNames(String hostNames) {
        String[] combinedHostNames = hostNames.split(",");
        for (String hostName : combinedHostNames) {
            String hostPair[] = hostName.split("-");
            this.hostNames.put(hostPair[0], hostPair[1]);
        }
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
}