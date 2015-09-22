/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.cloud.test.scenarios;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.cloud.integration.test.utils.CloudIntegrationConstants;
import org.wso2.carbon.cloud.integration.test.utils.CloudIntegrationTest;
import org.wso2.carbon.cloud.integration.test.utils.external.HttpHandler;
import org.wso2.carbon.cloud.integration.test.utils.restclients.JaggeryAppAuthenticatorClient;

import java.util.HashMap;
import java.util.Map;

public class TenantManagementTestCase extends CloudIntegrationTest {
    private static final Log log = LogFactory.getLog(TenantManagementTestCase.class);

    private JaggeryAppAuthenticatorClient authenticatorClient;
    private String signUpUrl =  cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_SIGNUP_URL_SFX;

    @BeforeClass(alwaysRun = true) public void deployService() throws Exception {
    }

    @Test(priority = 1)
    public void checkUsernameExistenceTest() throws Exception {
        log.info("started username exsitence test case ");
        Map<String,String> params = new HashMap<String, String>();
        params.put("action","isExistingUser");
        params.put("username", "jusid1931@einrot.com");
        String checkExistenceStatus = HttpHandler.doPostHttps(signUpUrl, params);
        Assert.assertEquals("false".equals(checkExistenceStatus), true);
    }

    @Test(priority = 2)
    public void checkSendInviteTest() throws Exception {
        log.info("started sending invite for user");
        Map<String,String> params = new HashMap<String, String>();
        params.put("action", "sendInvite");
        params.put("email","jusid1931@einrot.com");
        String checkInviteStatus = HttpHandler.doPostHttps(signUpUrl, params);
        Assert.assertEquals("true".equals(checkInviteStatus), true);
    }

    @Test(description = "Login test for the tenant", priority = 3)
    public void loginTest() throws Exception {
        log.info("started running test case login");
        authenticatorClient = new JaggeryAppAuthenticatorClient(cloudMgtServerUrl);
        boolean loginStatus = authenticatorClient.login(tenantAdminUserName, tenantAdminPassword);
        Assert.assertEquals(loginStatus, true);
    }

    @Test(description = "logout test", priority = 4)
    public void logoutTest() throws Exception {
        log.info("started running test case log out");
        authenticatorClient = new JaggeryAppAuthenticatorClient(cloudMgtServerUrl);
        boolean logOutStatus = authenticatorClient.logout();
        Assert.assertEquals(logOutStatus, true);
    }

    @AfterClass(alwaysRun = true) public void unDeployService() throws Exception {
        super.cleanup();
    }
}