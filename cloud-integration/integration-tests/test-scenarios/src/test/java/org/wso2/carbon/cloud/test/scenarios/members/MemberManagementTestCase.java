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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemberManagementTestCase extends CloudIntegrationTest {
    private static final Log log = LogFactory.getLog(MemberManagementTestCase.class);
    private JaggeryAppAuthenticatorClient authenticatorClient;
    private CarbonAuthenticatorClient adminServiceClient;
    private boolean loginStatus;
    private String allRoles;
    private String users;
    private List<String> usersEmailList;
    private String session;
    private String loginUrl;
    private String identityServerUrl;
    private String tenantUsersUrl;

    @BeforeClass(alwaysRun = true) public void deployService() throws Exception {
        authenticatorClient = new JaggeryAppAuthenticatorClient(cloudMgtServerUrl);
        identityServerUrl = CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.IDENTITY_SERVER_URL);
        loginStatus = authenticatorClient.login(tenantAdminUserName, tenantAdminPassword);
        loginUrl = cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_LOGIN_URL_SFX;
        tenantUsersUrl = cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_TENANT_USERS_URL_SFX;
        users = CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.NEW_CLOUD_USER_EMAILS).trim();
        allRoles = CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.CLOUD_API_SUBSCRIBER_ROLE)
                + CloudIntegrationConstants.COMMA_SEPERATOR + CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.CLOUD_API_PUBLISHER_ROLE)
                + CloudIntegrationConstants.COMMA_SEPERATOR + CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.CLOUD_DEVICE_USER_ROLE)
                + CloudIntegrationConstants.COMMA_SEPERATOR + CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.CLOUD_DEVICE_ADMIN_ROLE)
                + CloudIntegrationConstants.COMMA_SEPERATOR + CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.CLOUD_INTEGRATION_USER_ROLE)
                + CloudIntegrationConstants.COMMA_SEPERATOR + CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.CLOUD_ADMIN_ROLE);
        usersEmailList = Arrays.asList(users.split(CloudIntegrationConstants.COMMA_SEPERATOR));
        //login admin service client to access admin services
        adminServiceClient = new CarbonAuthenticatorClient(identityServerUrl);
        session = adminServiceClient.login(superAdminUserName, superAdminPassword, "localhost");
    }

    /**
     * This method checks whether any user already exists in the system
     *
     * @throws Exception
     */
    @Test(description = "This will check existence of username")
    public void checkUsernameExistenceTest() throws Exception {
        Assert.assertTrue(loginStatus, CloudIntegrationConstants.LOGIN_ERROR_MESSAGE);
        String signUpUrl = cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_SIGNUP_URL_SFX;
        for (String userEmail : usersEmailList) {
            Map<String, String> params = new HashMap<>();
            Map resultMap;
            params.put(CloudIntegrationConstants.PARAMETER_KEY_ACTION, "isExistingUser");
            params.put("username", userEmail.trim());
            resultMap = HttpHandler.doPostHttps(signUpUrl, params, null, false);
            String response = resultMap.get(CloudIntegrationConstants.RESPONSE).toString();
            ArrayList<String> responseData = new ArrayList<>(Arrays.asList(response.split(",")));
            Assert.assertEquals(responseData.get(0), "{\"error\" : false", "User already exists.");
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
        log.info("Started running invite members.");
        Map<String, String> params = new HashMap<>();
        params.put(CloudIntegrationConstants.PARAMETER_KEY_ACTION, "sendUserInvite");
        params.put("users", users);
        params.put("roles", allRoles);
        Map resultMap = HttpHandler.doPostHttps(tenantUsersUrl, params, authenticatorClient.getSessionCookie(), false);
        JSONObject resultObj = new JSONObject(resultMap.get(CloudIntegrationConstants.RESPONSE).toString());
        Assert.assertEquals(resultObj.get("error").toString(), CloudIntegrationConstants.FALSE,
                "Value mismatch, Should be false.");
    }

    /**
     * This method checks update user invitations.
     *
     * @throws Exception
     */
    @Test(dependsOnMethods = {"inviteMembers" }, description = "This will check update user invitations")
    public void updateUserInvitations() throws Exception {
        log.info("Started running update roles of invited members.");
        //This will be done for only one user.
        Map<String, String> params = new HashMap<>();
        params.put(CloudIntegrationConstants.PARAMETER_KEY_ACTION, "updateUserInvitation");
        //All the roles except subscriber role will be removed from the first user in the list
        String displayNamesOfRolesToRemove = CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.CLOUD_API_PUBLISHER_ROLE_DISPLAY_NAME)
                + CloudIntegrationConstants.COMMA_SEPERATOR + CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.CLOUD_DEVICE_USER_ROLE_DISPLAY_NAME)
                + CloudIntegrationConstants.COMMA_SEPERATOR + CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.CLOUD_DEVICE_ADMIN_ROLE_DISPLAY_NAME)
                + CloudIntegrationConstants.COMMA_SEPERATOR + CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.CLOUD_INTEGRATION_USER_ROLE_DISPLAY_NAME)
                + CloudIntegrationConstants.COMMA_SEPERATOR + CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.CLOUD_ADMIN_ROLE_DISPLAY_NAME);
        //Fist user in the list will be considered.
        params.put(CloudIntegrationConstants.EMAIL, usersEmailList.get(0));
        params.put("rolesToAdd", "");
        params.put("rolesToDelete", displayNamesOfRolesToRemove);
        Map resultMap = HttpHandler.doPostHttps(tenantUsersUrl, params, authenticatorClient.getSessionCookie(), false);
        JSONObject resultObj = new JSONObject(resultMap.get(CloudIntegrationConstants.RESPONSE).toString());
        Assert.assertEquals(resultObj.get(CloudIntegrationConstants.ERROR).toString(), CloudIntegrationConstants.FALSE,
                "Value mismatch, Should be false.");
    }

    /**
     * This method checks revoke user invitations
     *
     * @throws Exception
     */
    @Test(dependsOnMethods = { "updateUserInvitations" }, description = "This will check revoke user invitations")
    public void revokeUserInvitations() throws Exception {
        log.info("Started running revoke invited members.");
        //This will done for only one user
        Map<String, String> params = new HashMap<>();
        params.put(CloudIntegrationConstants.PARAMETER_KEY_ACTION, "revokeInvitation");
        //Invitation sent to the last user in the list will be removed
        int lastUserIndex = usersEmailList.size() - 1;
        params.put(CloudIntegrationConstants.EMAIL, usersEmailList.get(lastUserIndex));
        Map resultMap = HttpHandler.doPostHttps(tenantUsersUrl, params, authenticatorClient.getSessionCookie(), false);
        JSONObject resultObj = new JSONObject(resultMap.get(CloudIntegrationConstants.RESPONSE).toString());
        Assert.assertEquals(resultObj.get(CloudIntegrationConstants.ERROR).toString(), CloudIntegrationConstants.FALSE,
                "Value mismatch, Should be false.");
    }

    /**
     * This method will test confirming and adding members
     *
     * @throws SQLException
     * @throws IOException
     * @throws JSONException
     */
    @Test(dependsOnMethods = {"revokeUserInvitations" }, description = "This will check confirm invited users")
    public void confirmInvitedMembers() throws Exception {
        log.info("Started running confirm invited members.");
        String tenantDomain = CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.TENANT_ADMIN_DOMAIN);
        String confirmUserUrl = cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_CONFIRM_USER_URL_SFX;
        String query = CloudIntegrationConstants.GER_UUID_FOR_TEMP_INVITEE;
        DbConnectionManager connectionManager = new DbConnectionManager(CloudIntegrationConstants.CLOUD_MGT_DATASOURCE);
        try {
            //usersEmailList.size() - 1 is used since we need to ignore the revoked user
            for (int i = 0; i < usersEmailList.size() - 1; i++) {
                String user = usersEmailList.get(i).trim();
                List<String> queryParameters = new ArrayList<>();
                queryParameters.add(tenantDomain);
                queryParameters.add(user);
                ResultSet results = connectionManager.runQuery(query, queryParameters);
                if (results.next()) {
                    String uuid = results.getString(1);
                    Map<String, String> confirmUserParams = new HashMap<>();
                    confirmUserParams.put(CloudIntegrationConstants.PARAMETER_KEY_ACTION, "confirmUser");
                    confirmUserParams.put("confirm", uuid);
                    confirmUserParams.put("isInvitee", CloudIntegrationConstants.TRUE);
                    Map confirmUserResultMap = HttpHandler
                            .doPostHttps(confirmUserUrl, confirmUserParams, authenticatorClient.getSessionCookie(),
                                    false);
                    String response = confirmUserResultMap.get(CloudIntegrationConstants.RESPONSE).toString();
                    ArrayList<String> responseData = new ArrayList<>(
                            Arrays.asList(response.split(CloudIntegrationConstants.COMMA_SEPERATOR)));
                    //existing user is added to the tenant and will be redirected to index page.
                    if (i == 1) {
                        log.info("Adding existing user to tenant.");
                        Assert.assertEquals(responseData.get(2), " \"data\" : \"../pages/index.jag\"}",
                                "Value mismatch, Should be ../pages/index.jag for user: " + user);
                        continue;
                    }
                    Assert.assertEquals(responseData.get(2), " \"data\" : \"add-tenant.jag\"",
                                        "Value mismatch, Should be add-tenant.jag for user: " + user);
                    Map<String, String> addUserParams = new HashMap<>();
                    addUserParams.put(CloudIntegrationConstants.PARAMETER_KEY_ACTION, "importInvitedUser");
                    addUserParams.put("adminPassword",
                            CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.TENANT_USER_PASSWORD));
                    addUserParams.put("confirmationKey", uuid);
                    addUserParams.put("firstName", CloudIntegrationTestUtils
                            .getPropertyValue(CloudIntegrationConstants.TENANT_USER_FIRST_NAME));
                    addUserParams.put("lastName", CloudIntegrationTestUtils
                            .getPropertyValue(CloudIntegrationConstants.TENANT_USER_LAST_NAME));
                    Map addUserResultMap = HttpHandler
                            .doPostHttps(tenantUsersUrl, addUserParams, authenticatorClient.getSessionCookie(), false);
                    //User with only subscriber role, should be redirected to store
                    if (i == 0) {
                        log.info("Confirming user with only subscriber role.");
                        boolean isRedirectedToStore = addUserResultMap.get(CloudIntegrationConstants.RESPONSE)
                                .toString().contains("/store");
                        Assert.assertTrue(isRedirectedToStore, "Value mismatch, Should be TRUE");
                    } else {
                        //users with more than one role will be redirected to index page
                        String expectedResponse =
                                "{\"error\" : false, \"status\" : 200, \"data\" : \"../pages/index" + ".jag\"}";
                        Assert.assertEquals(addUserResultMap.get(CloudIntegrationConstants.RESPONSE).toString(),
                                expectedResponse, "Value mismatch, Should be ../pages/index.jag.");
                    }
                } else {
                    Assert.fail("Sending user invitation has been failed.");
                }
            }
        } catch (Exception e) {
            log.error("Error occurred when getting the data", e);
            throw e;
        } finally {
            try {
                connectionManager.closeConnection();
            } catch (Exception e) {
                log.error("Error occurred when closing the connection.", e);
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
    @Test(dependsOnMethods = {"confirmInvitedMembers" }, description = "This will check update allRoles of members")
    public void updateMemberRoles() throws SQLException, IOException, JSONException {
        //test for only one user
        String username = usersEmailList.get(0);
        Map<String, String> params = new HashMap<>();
        params.put(CloudIntegrationConstants.PARAMETER_KEY_ACTION, "updateUserRoles");
        params.put("userName", username);
        //Will delete the subscriber role and add the publisher and device cloud user roles to first user in the list
        params.put("rolesToAdd",
                CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.CLOUD_API_PUBLISHER_ROLE)
                        + CloudIntegrationConstants.COMMA_SEPERATOR + CloudIntegrationTestUtils
                        .getPropertyValue(CloudIntegrationConstants.CLOUD_DEVICE_USER_ROLE));
        params.put("rolesToDelete",
                CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.CLOUD_API_SUBSCRIBER_ROLE));
        Map resultMap = HttpHandler.doPostHttps(tenantUsersUrl, params, authenticatorClient.getSessionCookie(), false);
        JSONObject resultObj = new JSONObject(resultMap.get(CloudIntegrationConstants.RESPONSE).toString());
        Assert.assertEquals(resultObj.get(CloudIntegrationConstants.ERROR).toString(), CloudIntegrationConstants.FALSE,
                "Value mismatch, Should be false.");
    }

    /**
     * Checks login status for the newly invited users
     *
     * @throws Exception
     */
    @Test(alwaysRun = true, dependsOnMethods = { "confirmInvitedMembers" }, description = "Login test for the tenant")
    public void loginTest() throws Exception {
        log.info("Started running test case login.");
        //This should run for all the user in order to add them to super tenant's default role
        boolean isLoginToTenantSuccess = true;
        boolean isLoginToSuperTenantSuccess = true;
        for (int i = 0; i < usersEmailList.size() - 1; i++) {
            String userEmail = usersEmailList.get(i).trim();
            Map<String, String> params = new HashMap<>();
            params.put(CloudIntegrationConstants.PARAMETER_KEY_ACTION, "login");
            params.put("userName", userEmail);
            params.put("password",
                    CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.TENANT_USER_PASSWORD));
            Map resultMap = HttpHandler.doPostHttps(loginUrl, params, null, false);
            //Login to super tenant test
            String loginToSuperTenantStatus = resultMap.get(CloudIntegrationConstants.RESPONSE).toString();
            if (CloudIntegrationConstants.FALSE.equals(loginToSuperTenantStatus)) {
                isLoginToSuperTenantSuccess = false;
            }
            params.put("userName", userEmail + '@' + CloudIntegrationTestUtils
                    .getPropertyValue(CloudIntegrationConstants.TENANT_ADMIN_DOMAIN));
            //Login to tenant test
            resultMap = HttpHandler.doPostHttps(loginUrl, params, null, false);
            String loginToTenantStatus = resultMap.get(CloudIntegrationConstants.RESPONSE).toString();
            if (CloudIntegrationConstants.FALSE.equals(loginToTenantStatus)) {
                isLoginToTenantSuccess = false;
            }
        }
        Assert.assertTrue(isLoginToTenantSuccess, "Invited users 'super tenant' login failed");
        Assert.assertTrue(isLoginToSuperTenantSuccess, "Invited users 'tenant' login failed");
    }

    /**
     * This method checks remove uses from tenant
     *
     * @throws Exception
     */
    @Test(alwaysRun = true, dependsOnMethods = { "confirmInvitedMembers", "updateMemberRoles", "loginTest" },
            description = "This will check remove members")
    public void removeMembers() throws Exception {
        boolean isRemoveFromTenantSuccess = true;
        boolean isUserExist = true;
        try {
            for (int i = 0; i < usersEmailList.size() - 1; i++) {
                String userEmail = usersEmailList.get(i).trim();
                //check the user existence before removing the user.
                Map<String, String> checkExistenceParams = new HashMap<>();
                Map checkExistenceResultMap;
                String signUpUrl = cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_SIGNUP_URL_SFX;
                checkExistenceParams.put(CloudIntegrationConstants.PARAMETER_KEY_ACTION, "isExistingUser");
                checkExistenceParams.put("username", userEmail.trim());
                checkExistenceResultMap = HttpHandler
                        .doPostHttps(signUpUrl, checkExistenceParams, authenticatorClient.getSessionCookie(), false);
                String response = checkExistenceResultMap.get(CloudIntegrationConstants.RESPONSE).toString();
                ArrayList<String> responseData = new ArrayList<>(Arrays.asList(response.split(",")));
                if (!"{\"error\" : false".equals(responseData.get(0))) {
                    isUserExist = false;
                }
                //remove user from the tenant
                Map<String, String> deleteUserParams = new HashMap<>();
                deleteUserParams.put(CloudIntegrationConstants.PARAMETER_KEY_ACTION, "deleteUserFromTenant");
                deleteUserParams.put("userName", userEmail);
                Map deleteUserResultMap = HttpHandler
                        .doPostHttps(tenantUsersUrl, deleteUserParams, authenticatorClient.getSessionCookie(), false);
                JSONObject resultObj = new JSONObject(
                        deleteUserResultMap.get(CloudIntegrationConstants.RESPONSE).toString());
                if (CloudIntegrationConstants.TRUE.equals(resultObj.get(CloudIntegrationConstants.ERROR).toString())) {
                    isRemoveFromTenantSuccess = false;
                }
            }
            Assert.assertTrue(isUserExist, "Users do not exist.");
            Assert.assertTrue(isRemoveFromTenantSuccess, "Remove users from tenant failed.");
        } catch (Exception e) {
            log.error("Error occurred when deleting the users", e);
            throw e;
        }
    }

    /**
     * This method remove uses from user store
     * Added users have to be removed from user store therefore UserAdmin service is called
     * to remove users from the user store
     *
     * @throws Exception
     */
    @Test(alwaysRun = true, dependsOnMethods = { "confirmInvitedMembers", "updateMemberRoles",
            "loginTest", "removeMembers" }, description = "This will check remove users from userstore")
    public void removeMembersFromUserStore()
            throws Exception {
        //user admin client to access user admin service which is used to
        //remove user from user store
        UserAdminClient userAdminClient = new UserAdminClient(identityServerUrl, session);
        try {
            for (int i = 0; i < usersEmailList.size() - 1; i++) {
                String userName = usersEmailList.get(i).trim();
                //remove user form the user store. Please note that existing user who was not a member of the tenant
                // will not be removed from userstore
                if (i != 1) {
                    userAdminClient.deleteUser(userName);
                }
            }
        } catch (Exception e) {
            log.error("Error occurred when deleting the users", e);
            throw e;
        }
    }

    @AfterClass(alwaysRun = true) public void destroy() throws IOException, LogoutAuthenticationExceptionException {
        authenticatorClient.logout();
        adminServiceClient.logOut();
        super.cleanup();
    }

}
