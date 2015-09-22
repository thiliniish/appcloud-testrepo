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
import org.wso2.carbon.cloud.integration.test.utils.CloudIntegrationTestUtils;
import org.wso2.carbon.cloud.integration.test.utils.external.HttpHandler;
import org.wso2.carbon.cloud.integration.test.utils.restclients.JaggeryAppAuthenticatorClient;

import java.util.HashMap;
import java.util.Map;

public class TenantManagementTestCase extends CloudIntegrationTest {
    private static final Log log = LogFactory.getLog(TenantManagementTestCase.class);

    private JaggeryAppAuthenticatorClient authenticatorClient;
    private String signUpUrl = cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_SIGNUP_URL_SFX;

    @BeforeClass(alwaysRun = true) public void deployService() throws Exception {
        authenticatorClient = new JaggeryAppAuthenticatorClient(cloudMgtServerUrl);
    }

    @Test(description = "Check for existence of username", priority = 1) public void checkUsernameExistenceTest()
            throws Exception {
        log.info("started username exsitence test case ");
        Map<String, String> params = new HashMap<String, String>();
        Map resultMap;
        params.put("action", "isExistingUser");
        params.put("username", CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.NEW_TENANT_EMAIL));
        resultMap = HttpHandler.doPostHttps(signUpUrl, params, null);
        Assert.assertEquals(resultMap.get(CloudIntegrationConstants.RESPONSE), "false",
                            "failed to check the user existence");
    }

    @Test(description = "send the invitaion for user", priority = 2, dependsOnMethods = {
            "checkUsernameExistenceTest" }) public void checkSendInviteTest() throws Exception {
        log.info("started sending invite for user");
        Map<String, String> params = new HashMap<String, String>();
        Map resultMap;
        params.put("action", "sendInvite");
        params.put("email", CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.NEW_TENANT_EMAIL));
        resultMap = HttpHandler.doPostHttps(signUpUrl, params, null);
        Assert.assertEquals(resultMap.get(CloudIntegrationConstants.RESPONSE),
                            CloudIntegrationTestUtils
                                    .getPropertyValue(CloudIntegrationConstants.NEW_TENANT_EMAIL),
                            "user invitation sending failed");

    }

    @Test(description = "Login test for the tenant", priority = 3) public void loginTest()
            throws Exception {
        log.info("started running test case login");
        boolean loginStatus = authenticatorClient.login(tenantAdminUserName, tenantAdminPassword);
        Assert.assertTrue(loginStatus, "tenant login failed");
    }

    @Test(description = "logout test", priority = 4) public void logoutTest() throws Exception {
        log.info("started running test case log out");
        boolean logOutStatus = authenticatorClient.logout();
        Assert.assertTrue(logOutStatus, "tenant log out failed");
    }

    @AfterClass(alwaysRun = true) public void unDeployService() throws Exception {
        super.cleanup();
    }
}