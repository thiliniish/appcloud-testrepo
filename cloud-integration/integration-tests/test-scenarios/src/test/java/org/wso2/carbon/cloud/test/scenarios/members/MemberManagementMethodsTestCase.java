/*
* Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.cloud.test.scenarios.members;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.cloud.integration.test.utils.CloudIntegrationConstants;
import org.wso2.carbon.cloud.integration.test.utils.CloudIntegrationTest;
import org.wso2.carbon.cloud.integration.test.utils.external.DbConnectionManager;
import org.wso2.carbon.cloud.integration.test.utils.external.HttpHandler;
import org.wso2.carbon.cloud.integration.test.utils.restclients.JaggeryAppAuthenticatorClient;
import org.wso2.carbon.cloud.integration.test.utils.CloudIntegrationTestUtils;


import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemberManagementMethodsTestCase extends CloudIntegrationTest {
    private static final Log log = LogFactory.getLog(MemberManagementMethodsTestCase.class);
    private JaggeryAppAuthenticatorClient authenticatorClient;
    private boolean loginStatus;
    private String users;
    private String roles;



    @BeforeClass(alwaysRun = true) public void deployService() throws Exception {
        authenticatorClient = new JaggeryAppAuthenticatorClient(cloudMgtServerUrl);
        loginStatus = authenticatorClient.login(tenantAdminUserName, tenantAdminPassword);
        users = CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.NEW_CLOUD_USER_EMAILS);
        roles= CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.ALL_CLOUD_USER_ROLES);
    }

    @Test(description = "Check for existence of username")
    public void checkUsernameExistenceTest()
            throws Exception {
        log.info("started username existence test case ");
        String signUpUrl = cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_SIGNUP_URL_SFX;
        String [] usersEmailArray = users.split(",");
        for(String userEmail : usersEmailArray){
            log.info("started user existence test case for "+userEmail);
            Map<String, String> params = new HashMap<String, String>();
            Map resultMap;
            params.put("action", "isExistingUser");
            params.put("username", userEmail);
            resultMap = HttpHandler.doPostHttps(signUpUrl, params, null);
            Assert.assertEquals(resultMap.get(CloudIntegrationConstants.RESPONSE), "false","user already exists");

        }
    }

    @Test(dependsOnMethods = {"checkUsernameExistenceTest"}, description = "This will check invite single or more members to an organization")
    public void inviteMembers() throws Exception {

        Assert.assertTrue(loginStatus, "Tenant login failed.");
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "sendUserInvite");
        params.put("users", users);
        params.put("roles", roles);
        String sendUserInviteUrl = cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_SEND_USER_INVITE_URL_SFX;
        Map resultMap = HttpHandler.doPostHttps(sendUserInviteUrl, params,
                                                authenticatorClient.getSessionCookie());
        JSONObject resultObj =
                new JSONObject(resultMap.get(CloudIntegrationConstants.RESPONSE).toString());
        Assert.assertEquals(resultObj.get("error").toString(), "false");

    }

    @Test(dependsOnMethods = {"checkUsernameExistenceTest","inviteMembers"},description = "This will check confirm invited users")
    public void confirmInvitedMembers() throws SQLException, IOException, JSONException {

        String [] usersEmailArray = users.split(",");
        String tenantDomain = CloudIntegrationTestUtils.getPropertyValue(
                CloudIntegrationConstants.TENANT_DOMAIN);
        String confirmUserUrl = cloudMgtServerUrl+CloudIntegrationConstants.CLOUD_CONFIRM_USER_URL_SFX;
        String addUserUrl = cloudMgtServerUrl+CloudIntegrationConstants.CLOUD_TENANT_USERS_URL_SFX;
        String query = CloudIntegrationConstants.GER_UUID_FOR_TEMP_INVITEE;
        DbConnectionManager connectionManager = new DbConnectionManager();

        for(String user : usersEmailArray){
            List<String> queryParameters = new ArrayList<String>();
            queryParameters.add(tenantDomain);
            queryParameters.add(user);
            ResultSet results = connectionManager.runQuery(query,queryParameters);
            if (results.next()) {
                String uuid = results.getString(1);
                Map<String, String> confirmUserParams = new HashMap<String, String>();
                confirmUserParams.put("action", "confirmUser");
                confirmUserParams.put("confirm", uuid);
                confirmUserParams.put("isInvitee", "true");
                Map confirmUserResultMap = HttpHandler.doPostHttps(confirmUserUrl, confirmUserParams,
                                                        null);
                Assert.assertEquals(
                        confirmUserResultMap.get(CloudIntegrationConstants.RESPONSE).toString(),
                        "add-tenant.jag");
                Map<String, String> addUserParams = new HashMap<String, String>();
                addUserParams.put("action","importInvitedUser");
                addUserParams.put("adminPassword",CloudIntegrationConstants.COMMON_USER_PASSWORD);
                addUserParams.put("confirmationKey", uuid);
                addUserParams.put("firstName",CloudIntegrationConstants.COMMON_USER_FIRST_NAME);
                addUserParams.put("lastName", CloudIntegrationConstants.COMMON_USER_LAST_NAME);
                Map addUserResultMap = HttpHandler.doPostHttps(addUserUrl, addUserParams,
                                                                   null);
                Assert.assertEquals(
                        addUserResultMap.get(CloudIntegrationConstants.RESPONSE).toString(),
                        "../pages/index.jag");

            }
        }

        connectionManager.closeConnection();

    }


    @Test(dependsOnMethods = {"confirmInvitedMembers"},description = "This will check update roles of members",priority = 1)
    public void updateMemberRoles() throws SQLException, IOException, JSONException {
        String [] usersEmailArray = users.split(",");
        String userName = usersEmailArray[0].replace('@','.');
        log.info("usrname----->"+userName);
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "updateUserRoles");
        params.put("userName", userName);
        params.put("rolesToAdd", "");//no roles will be added since user has all the roles
        params.put("rolesToDelete", "qa,cxo");

        String updateUserInviteUrl = cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_TENANT_USERS_URL_SFX;
        Map resultMap = HttpHandler.doPostHttps(updateUserInviteUrl, params,
                                                authenticatorClient.getSessionCookie());

        log.info("response"+resultMap.get(CloudIntegrationConstants.RESPONSE).toString());
        JSONObject resultObj =
                new JSONObject(resultMap.get(CloudIntegrationConstants.RESPONSE).toString());
        //Assert.assertEquals(resultObj.get("error").toString(), "false");



    }


//    @Test(dependsOnMethods = {"confirmInvitedMembers"},description = "This will check remove members",priority = 2)
//    public void removeMembers() throws SQLException, IOException, JSONException {
//
//        String [] usersEmailArray = users.split(",");
//        for(String userEmail : usersEmailArray){
//            String userName = userEmail.replace('@','.');
//            Map<String, String> params = new HashMap<String, String>();
//            params.put("action", "deleteUserFromTenant");
//            params.put("userName", userName);
//
//            String removeUserUrl = cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_TENANT_USERS_URL_SFX;
//            Map resultMap = HttpHandler.doPostHttps(removeUserUrl, params,
//                                                    authenticatorClient.getSessionCookie());
//
//            log.info("response"+resultMap.get(CloudIntegrationConstants.RESPONSE).toString());
//            JSONObject resultObj =
//                    new JSONObject(resultMap.get(CloudIntegrationConstants.RESPONSE).toString());
//
//        }
//
//
//    }


    @AfterClass(alwaysRun = true)
    public void destroy() throws IOException {
        authenticatorClient.logout();
        super.cleanup();
    }

}
