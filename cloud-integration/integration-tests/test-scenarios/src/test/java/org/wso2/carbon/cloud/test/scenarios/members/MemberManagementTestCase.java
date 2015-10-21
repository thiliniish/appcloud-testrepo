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

package org.wso2.carbon.cloud.test.scenarios.members;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.cloud.integration.test.utils.CloudIntegrationConstants;
import org.wso2.carbon.cloud.integration.test.utils.CloudIntegrationTest;
import org.wso2.carbon.cloud.integration.test.utils.clients.authentication.CarbonAuthenticatorClient;
import org.wso2.carbon.cloud.integration.test.utils.clients.service.UserAdminClient;
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

public class MemberManagementTestCase extends CloudIntegrationTest {
    private static final Log log = LogFactory.getLog(MemberManagementTestCase.class);
    private JaggeryAppAuthenticatorClient authenticatorClient;
    private CarbonAuthenticatorClient adminServiceClient;
    private boolean loginStatus;
    private String users;
    private String roles;
    private String[] usersEmailArray;
    private String session;

    @BeforeClass(alwaysRun = true) public void deployService() throws Exception {
        authenticatorClient = new JaggeryAppAuthenticatorClient(cloudMgtServerUrl);
        loginStatus = authenticatorClient.login(tenantAdminUserName, tenantAdminPassword);
        users = CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.NEW_CLOUD_USER_EMAILS).trim();
        roles = CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.ALL_CLOUD_USER_ROLES).trim();
        usersEmailArray = users.split(",");
        //login admin service client to access admin services
        adminServiceClient = new CarbonAuthenticatorClient(cloudMgtServerUrl);
        session = adminServiceClient.login(superAdminUserName, superAdminPassword, "localhost");
    }

    /**
     * This method checks whether any user already exists in the system
     *
     * @throws Exception
     */
    @Test(description = "This will check existence of username")
    public void checkUsernameExistenceTest() throws Exception {
        Assert.assertTrue(loginStatus, "Tenant login failed.");
        String signUpUrl = cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_SIGNUP_URL_SFX;
        for (String userEmail : usersEmailArray) {
            Map<String, String> params = new HashMap<String, String>();
            Map resultMap;
            params.put("action", "isExistingUser");
            params.put("username", userEmail.trim());
            resultMap = HttpHandler.doPostHttps(signUpUrl, params, null);
            Assert.assertEquals(resultMap.get(CloudIntegrationConstants.RESPONSE), "false",
                                "User already exists.");
        }
    }

    /**
     * This method checks invite members method by sending user invitations.
     *
     * @throws Exception
     */
    @Test(dependsOnMethods = {"checkUsernameExistenceTest" },
            description = "This will check invite single or more members to an organization")
    public void inviteMembers() throws Exception {

        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "sendUserInvite");
        params.put("users", users);
        params.put("roles", roles);
        String sendUserInviteUrl =
                cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_SEND_USER_INVITE_URL_SFX;
        Map resultMap = HttpHandler
                .doPostHttps(sendUserInviteUrl, params, authenticatorClient.getSessionCookie());
        JSONObject resultObj =
                new JSONObject(resultMap.get(CloudIntegrationConstants.RESPONSE).toString());
        Assert.assertEquals(resultObj.get("error").toString(), "false",
                            "Value mismatch, Should be false.");
    }

    /**
     * This method will test confirming and adding members
     *
     * @throws SQLException
     * @throws IOException
     * @throws JSONException
     */
    @Test(dependsOnMethods = {"inviteMembers" },
            description = "This will check confirm invited users")
    public void confirmInvitedMembers() throws Exception {

        String tenantDomain = CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.TENANT_ADMIN_DOMAIN);
        String confirmUserUrl =
                cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_CONFIRM_USER_URL_SFX;
        String addUserUrl =
                cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_TENANT_USERS_URL_SFX;
        String query = CloudIntegrationConstants.GER_UUID_FOR_TEMP_INVITEE;
        DbConnectionManager connectionManager = new DbConnectionManager(CloudIntegrationConstants.CLOUD_MGT_DATASOURCE);
        try {
            for (String user : usersEmailArray) {
                user = user.trim();
                List<String> queryParameters = new ArrayList<String>();
                queryParameters.add(tenantDomain);
                queryParameters.add(user);
                ResultSet results = connectionManager.runQuery(query, queryParameters);
                if (results.next()) {
                    String uuid = results.getString(1);
                    Map<String, String> confirmUserParams = new HashMap<String, String>();
                    confirmUserParams.put("action", "confirmUser");
                    confirmUserParams.put("confirm", uuid);
                    confirmUserParams.put("isInvitee", "true");
                    Map confirmUserResultMap =
                            HttpHandler.doPostHttps(confirmUserUrl, confirmUserParams, null);
                    Assert.assertEquals(
                            confirmUserResultMap.get(CloudIntegrationConstants.RESPONSE).toString(),
                            "add-tenant.jag", "Value mismatch, Should be add-tenant.jag");
                    Map<String, String> addUserParams = new HashMap<String, String>();
                    addUserParams.put("action", "importInvitedUser");
                    addUserParams.put("adminPassword", CloudIntegrationTestUtils
                            .getPropertyValue(CloudIntegrationConstants.TENANT_USER_PASSWORD));
                    addUserParams.put("confirmationKey", uuid);
                    addUserParams.put("firstName", CloudIntegrationTestUtils
                            .getPropertyValue(CloudIntegrationConstants.TENANT_USER_FIRST_NAME));
                    addUserParams.put("lastName", CloudIntegrationTestUtils
                            .getPropertyValue(CloudIntegrationConstants.TENANT_USER_LAST_NAME));
                    Map addUserResultMap = HttpHandler.doPostHttps(addUserUrl, addUserParams, null);
                    Assert.assertEquals(
                            addUserResultMap.get(CloudIntegrationConstants.RESPONSE).toString(),
                            "../pages/index.jag", "Value mismatch, Should be ../pages/index.jag.");
                } else {
                    Assert.fail("Sending user invitation has been failed");
                }
            }
        } catch (Exception e) {
            log.error("Error occurred when getting the data", e);
            throw e;
        } finally {
            try {
                connectionManager.closeConnection();
            } catch (Exception e) {
                log.error("Error occurred when closing the connection", e);
                throw e;
            }
        }
    }

    /**
     * This method checks update member roles
     *
     * @throws SQLException
     * @throws IOException
     * @throws JSONException
     */
    @Test(dependsOnMethods = {"confirmInvitedMembers" },
            description = "This will check update roles of members")
    public void updateMemberRoles() throws SQLException, IOException, JSONException {

        //test for only one user
        String userName = usersEmailArray[0].replace('@', '.').trim();
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "updateUserRoles");
        params.put("userName", userName);
        params.put("rolesToAdd", "");//no roles will be added since user has all the roles
        params.put("rolesToDelete", "qa,cxo");
        String updateUserInviteUrl =
                cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_TENANT_USERS_URL_SFX;
        Map resultMap = HttpHandler
                .doPostHttps(updateUserInviteUrl, params, authenticatorClient.getSessionCookie());
        JSONObject resultObj =
                new JSONObject(resultMap.get(CloudIntegrationConstants.RESPONSE).toString());
        Assert.assertEquals(resultObj.get("error").toString(), "false",
                            "Value mismatch, Should be false.");
    }

    /**
     * This method checks remove uses from tenant
     * Added users have to be removed from user store therefore UserAdmin service is called
     * to remove users from the user store
     *
     * @throws Exception
     */
    @Test(alwaysRun = true, dependsOnMethods = { "confirmInvitedMembers","updateMemberRoles" },
            description = "This will check remove members")
    public void removeMembers() throws Exception {

        //user admin client to access user admin service which is used to
        //remove user from user store
        UserAdminClient userAdminClient = new UserAdminClient(cloudMgtServerUrl, session);
        try {
            for (String userEmail : usersEmailArray) {
                //check the user existence before removing the user.
                Map<String, String> checkExistenceParams = new HashMap<String, String>();
                Map checkExistenceResultMap;
                String signUpUrl =
                        cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_SIGNUP_URL_SFX;
                checkExistenceParams.put("action", "isExistingUser");
                checkExistenceParams.put("username", userEmail.trim());
                checkExistenceResultMap =
                        HttpHandler.doPostHttps(signUpUrl, checkExistenceParams, null);
                Assert.assertEquals(checkExistenceResultMap.get(CloudIntegrationConstants.RESPONSE),
                                    "true", "User does not exist.");
                //remove user from the tenant
                String userName = userEmail.replace('@', '.').trim();
                Map<String, String> deleteUserParams = new HashMap<String, String>();
                deleteUserParams.put("action", "deleteUserFromTenant");
                deleteUserParams.put("userName", userName);
                String removeUserUrl =
                        cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_TENANT_USERS_URL_SFX;
                Map deleteUserResultMap = HttpHandler.doPostHttps(removeUserUrl, deleteUserParams,
                                                        authenticatorClient.getSessionCookie());
                JSONObject resultObj = new JSONObject(
                        deleteUserResultMap.get(CloudIntegrationConstants.RESPONSE).toString());
                Assert.assertEquals(resultObj.get("error").toString(), "false",
                                    "Value mismatch, Should be false.");
                //remove user form the user store
                userAdminClient.deleteUser(userName);
            }
        } catch (Exception e) {
            log.error("Error occurred when deleting the users", e);
            throw e;
        }
    }

    @AfterClass(alwaysRun = true) public void destroy()
            throws IOException, LogoutAuthenticationExceptionException {
        authenticatorClient.logout();
        adminServiceClient.logOut();
        super.cleanup();
    }

}
