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
import org.wso2.carbon.cloud.integration.test.utils.external.DbConnectionManager;
import org.wso2.carbon.cloud.integration.test.utils.external.HttpHandler;
import org.wso2.carbon.cloud.integration.test.utils.restclients.JaggeryAppAuthenticatorClient;

import java.sql.ResultSet;
import java.util.*;

public class TenantManagementTestCase extends CloudIntegrationTest {
    private static final Log log = LogFactory.getLog(TenantManagementTestCase.class);

    private JaggeryAppAuthenticatorClient authenticatorClient;
    private String uuid;
    private String tenantEmail;
    private String currentTimeStamp;
    private String signUpUrl = cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_SIGNUP_URL_SFX;
    private String confirmVerificationUrl =
            cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_SIGNUP_CONFIRM_URL_SFX;
    private String addTenantUrl =
            cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_ADD_TENANT_URL_SFX;
    private String tenantDomainPrefix =
            CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.NEW_TENANT_DOMAIN);
    private String tenantEmailSuffix = CloudIntegrationTestUtils
            .getPropertyValue(CloudIntegrationConstants.NEW_TENANT_ADMINUSER);

    @BeforeClass(alwaysRun = true) public void deployService() throws Exception {
        authenticatorClient = new JaggeryAppAuthenticatorClient(cloudMgtServerUrl);
        uuid = null;
        Calendar calendar = Calendar.getInstance();
        Long longTimeStamp = calendar.getTimeInMillis();
        currentTimeStamp = longTimeStamp.toString();
        tenantEmail = tenantDomainPrefix + currentTimeStamp + "@" + tenantEmailSuffix;
    }

    @Test(description = "Check for existence of username", priority = 1) public void checkUsernameExistenceTest()
            throws Exception {
        log.info("started username exsitence test case ");
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "isExistingUser");
        params.put("username", tenantEmail);
        Map resultMap = HttpHandler.doPostHttps(signUpUrl, params, null);
        Assert.assertEquals(resultMap.get(CloudIntegrationConstants.RESPONSE), "false",
                            "failed to check the user existence");
    }

    @Test(description = "send the invitaion for user", priority = 2, dependsOnMethods = {
            "checkUsernameExistenceTest" }) public void checkSendInviteTest() throws Exception {
        log.info("started sending invite for user");
        Map<String, String> params = new HashMap<String, String>();
        List<String> queryParameters = new ArrayList<String>();

        params.put("action", "sendInvite");
        params.put("email", tenantEmail);
        Map resultMap = HttpHandler.doPostHttps(signUpUrl, params, null);
        Assert.assertEquals(resultMap.get(CloudIntegrationConstants.RESPONSE), tenantEmail,
                            "user invitation sending failed");
        DbConnectionManager con = new DbConnectionManager();
        queryParameters.add(tenantEmail);
        ResultSet queryResult =
                con.runQuery(CloudIntegrationConstants.GET_TEMP_UUID_FOR_USER, queryParameters);
        if (queryResult.next()) {
            uuid = queryResult.getString(1);
        }
        con.closeConnection();
        params.clear();
        params.put("action", "confirmUser");
        params.put("confirm", uuid);
        Map confirmMap = HttpHandler.doPostHttps(confirmVerificationUrl, params, null);
        Assert.assertEquals(confirmMap.get("Response"), "add-tenant.jag",
                            "Adding the user to ldap failed");
    }

    @Test(description = "", priority = 3, dependsOnMethods = {
            "checkSendInviteTest" }) public void addTenant() throws Exception {
        log.info("Adding new Tenant Started");
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "registerOrg");
        params.put("companyName", tenantDomainPrefix + currentTimeStamp);
        params.put("adminPassword", CloudIntegrationTestUtils.getPropertyValue(
                CloudIntegrationConstants.NEW_TENANT_ADMINPASSWORD) + currentTimeStamp);
        params.put("usagePlan", CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.NEW_TENANT_USAGE_PLAN));
        params.put("confirmationKey", uuid);
        params.put("firstName", CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.NEW_TENANT_FIRSTNAME));
        params.put("lastName", CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.NEW_TENANT_LASTNAME));
        Map resultMap = HttpHandler.doPostHttps(addTenantUrl, params, null);
        Assert.assertEquals(resultMap.get("Response"), "true", "Adding new tenant successfull");

    }

    @Test(description = "Login test for the tenant", priority = 4, dependsOnMethods = {
            "addTenant" }) public void loginTest() throws Exception {
        log.info("started running test case login");
        boolean loginStatus = authenticatorClient
                .login(tenantDomainPrefix + currentTimeStamp + "." + tenantEmailSuffix + "@" +
                       tenantDomainPrefix + currentTimeStamp, CloudIntegrationTestUtils
                                                                      .getPropertyValue(
                                                                              CloudIntegrationConstants.NEW_TENANT_ADMINPASSWORD) +
                                                              currentTimeStamp);
        Assert.assertTrue(loginStatus, "tenant login failed");
    }

    @Test(description = "logout test", priority = 5, dependsOnMethods = {
            "loginTest" }) public void logoutTest() throws Exception {
        log.info("started running test case log out");
        boolean logOutStatus = authenticatorClient.logout();
        Assert.assertTrue(logOutStatus, "tenant log out failed");
    }

    @AfterClass(alwaysRun = true) public void unDeployService() throws Exception {
        super.cleanup();
    }
}