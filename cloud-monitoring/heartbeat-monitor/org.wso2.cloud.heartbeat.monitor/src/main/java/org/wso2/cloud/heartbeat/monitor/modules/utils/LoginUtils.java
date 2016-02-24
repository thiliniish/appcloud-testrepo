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

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.cloud.heartbeat.monitor.core.clients.authentication.CarbonAuthenticatorClient;
import org.wso2.cloud.heartbeat.monitor.utils.TestInfo;
import org.wso2.cloud.heartbeat.monitor.utils.TestStateHandler;
import org.wso2.cloud.heartbeat.monitor.utils.fileutils.CaseConverter;

import java.rmi.RemoteException;

/**
 * This is an utility class for TenantLoginTest class and ServerLoginTest class
 */
public class LoginUtils {
    private static final Log log = LogFactory.getLog(LoginUtils.class);
    private final String TEST_NAME = "TenantLoginTest";

    private String tenantUser;
    private String tenantUserPwd;
    private String hostName;
    private String serviceName;
    private int requestCount = 0;
    private TestInfo testInfo;
    private TestStateHandler testStateHandler;

    private CarbonAuthenticatorClient carbonAuthenticatorClient;

    /**
     * Initializes login test
     * @param loginUtilsBean login utils bean
     */
    public void initializeLoginTest(LoginUtilsBean loginUtilsBean) {
        requestCount = 0;

        tenantUser = loginUtilsBean.getTenantUser();
        tenantUserPwd = loginUtilsBean.getTenantUserPwd();
        hostName = loginUtilsBean.getHostName();
        serviceName = loginUtilsBean.getServerName();

        try {
            carbonAuthenticatorClient = new CarbonAuthenticatorClient(hostName);
            testStateHandler = TestStateHandler.getInstance();
            testInfo = new TestInfo(serviceName, TEST_NAME, hostName, loginUtilsBean.getLoginTestSeverity());
        } catch (AxisFault axisFault) {
            log.error(CaseConverter.splitCamelCase(serviceName) + " - Tenant Login: " + hostName +
                    ": AxisFault thrown while initiating the test : ", axisFault);
        }
    }

    /**
     * checks login for a service
     */
    public void login() {
        try {
            boolean loginStatus =
                    carbonAuthenticatorClient.checkLogin(tenantUser, tenantUserPwd, hostName);
            if (loginStatus) {
                testStateHandler.onSuccess(testInfo);
            } else {
                countNoOfLoginRequests("LoginError", null);
            }
        } catch (RemoteException e) {
            countNoOfLoginRequests("RemoteException", e);
        } catch (LoginAuthenticationExceptionException e) {
            countNoOfLoginRequests("LoginAuthenticationExceptionException", e);
        } catch (Exception e) {
            countNoOfLoginRequests("Exception", e);
        }
    }

    /**
     * Counts no of requests
     * @param type String type
     * @param obj  Object obj
     */
    public void countNoOfLoginRequests(String type, Object obj) {
        requestCount++;
        if (requestCount == 3) {
            handleError(type, obj);
            requestCount = 0;
        } else {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                //Exception ignored
            }
            login();
        }
    }

    /**
     * Handles errors
     * @param type String type
     * @param obj  Object obj
     */
    private void handleError(String type, Object obj) {
        if (type.equals("LoginError")) {
            log.error(CaseConverter.splitCamelCase(serviceName) + " - Tenant Login: " + hostName +
                    ": Login failure. Returned false as a login status by Server");
            testStateHandler.onFailure(testInfo, "Tenant login failure");
        } else if (type.equals("AxisFault")) {
            AxisFault axisFault = (AxisFault) obj;
            log.error(CaseConverter.splitCamelCase(serviceName) + " - Tenant Login: " + hostName +
                    ": AxisFault thrown while authenticating the stub : ", axisFault);
            testStateHandler.onFailure(testInfo, axisFault.getMessage());
        } else if (type.equals("RemoteException")) {
            RemoteException remoteException = (RemoteException) obj;
            log.error(CaseConverter.splitCamelCase(serviceName) + " - Tenant Login: " + hostName +
                    ": RemoteException thrown while login from Heartbeat tenant : ", remoteException);
            testStateHandler.onFailure(testInfo, remoteException.getMessage());
        } else if (type.equals("LoginAuthenticationExceptionException")) {
            LoginAuthenticationExceptionException e = (LoginAuthenticationExceptionException) obj;
            log.error(CaseConverter.splitCamelCase(serviceName) + " - Tenant Login: " + hostName +
                    ": LoginAuthenticationException thrown while login from Heartbeat tenant : ", e);
            testStateHandler.onFailure(testInfo, e.getMessage());
        } else if (type.equals("Exception")) {
            Exception e = (Exception) obj;
            log.error(CaseConverter.splitCamelCase(serviceName) + " - Tenant Login: " + hostName +
                    ": Exception thrown while login from Heartbeat tenant : ", e);
            testStateHandler.onFailure(testInfo, e.getMessage());
        }
    }
}