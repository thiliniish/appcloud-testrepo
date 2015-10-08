/*
 * Copyright 2005-2013 WSO2, Inc. http://www.wso2.org
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.cloud.heartbeat.monitor.modules.gitblit;

import com.gitblit.models.RepositoryModel;
import com.gitblit.utils.RpcUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;
import org.wso2.cloud.heartbeat.monitor.core.notification.Mailer;
import org.wso2.cloud.heartbeat.monitor.core.notification.SMSSender;
import org.wso2.cloud.heartbeat.monitor.utils.DbConnectionManager;
import org.wso2.cloud.heartbeat.monitor.utils.TestInfo;
import org.wso2.cloud.heartbeat.monitor.utils.TestStateHandler;
import org.wso2.cloud.heartbeat.monitor.utils.fileutils.CaseConverter;

import java.io.IOException;
import java.net.ConnectException;
import java.sql.Connection;
import java.util.Map;

/**
 * Tenant login test scenario for a Cloud setup, implemented in this class
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class GitTenantLoginTest implements Job{

    private static final Log log = LogFactory.getLog(GitTenantLoginTest.class);
    private final String TEST_NAME = "TenantLoginTest";

    private String tenantUser;
    private String tenantUserPwd;
    private String hostName;
    private String serviceName;
    private int requestCount = 0;

    private String loginUrl;

    private TestInfo testInfo;
    private TestStateHandler testStateHandler;
    private String loginTestSeverity="2";

    /**
     * @param jobExecutionContext
     * "hostName" ,"tenantUser", "tenantUserPwd" "serviceName" params passed via JobDataMap.
     * @throws org.quartz.JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        initializeLoginTest();
        //test scenario
        login();
    }

    private void initializeLoginTest() {
        loginUrl = "https://" + hostName;
        testStateHandler = TestStateHandler.getInstance();
        testInfo = new TestInfo(serviceName, TEST_NAME, hostName, loginTestSeverity);
    }

    /**
     * checks login for a service
     */
    private void login(){
        try {
            Map<String,RepositoryModel> map =  RpcUtils.getRepositories(loginUrl,tenantUser,tenantUserPwd.toCharArray());
            if(map.size() > 0){
                testStateHandler.onSuccess(testInfo);
            }else {
                countNoOfLoginRequests("LoginError", null);
            }

        } catch (ConnectException e) {
            countNoOfLoginRequests("ConnectException", e);
        } catch (IOException e) {
            countNoOfLoginRequests("IOException", e);
        }
    }

    private void countNoOfLoginRequests(String type, Object obj) {
        requestCount++;
        if(requestCount == 3){
            handleError(type, obj);
            requestCount = 0;
        }
        else {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                //Exception ignored
            }
            login();
        }
    }

    private void handleError(String type, Object obj) {
        if(type.equals("LoginError")) {
            log.error(CaseConverter.splitCamelCase(serviceName) + " - Tenant Login: " + hostName +
                      ": Login failure. Check username and password.");
            testStateHandler.onFailure(testInfo,"Tenant login failure:  Check username and password.");
        }else if(type.equals("IOException")) {
            IOException ioException = (IOException) obj;
            log.error(CaseConverter.splitCamelCase(serviceName) +" - Tenant Login: " + hostName +
                    ": IOException thrown while login from Heartbeat tenant : ", ioException);
            testStateHandler.onFailure(testInfo, ioException.getMessage());
        }else if(type.equals("ConnectException")) {
            ConnectException connectException = (ConnectException) obj;
            log.error(CaseConverter.splitCamelCase(serviceName) +" - Tenant Login: " + hostName +
                    ": ConnectException thrown while login from Heartbeat tenant : ", connectException);
            testStateHandler.onFailure(testInfo, connectException.getMessage());
        }
    }

    /**
     * Sets Tenant user name
     * @param tenantUser Tenant user name
     */
    public void setTenantUser(String tenantUser) {
        this.tenantUser = tenantUser;
    }

    /**
     * Sets Tenant user password
     * @param tenantUserPwd Tenant user password
     */
    public void setTenantUserPwd(String tenantUserPwd) {
        this.tenantUserPwd = tenantUserPwd;
    }

    /**
     * Sets service host
     * @param hostName Service host
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * Sets Service name
     * @param serviceName Service name
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * set severity of login test
     * @param loginTestSeverity
     */
    public void setLoginTestSeverity(String loginTestSeverity){
        this.loginTestSeverity = loginTestSeverity;
    }
}
