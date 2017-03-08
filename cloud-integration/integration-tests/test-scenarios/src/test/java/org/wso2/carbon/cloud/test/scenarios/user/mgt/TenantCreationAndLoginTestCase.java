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

package org.wso2.carbon.cloud.test.scenarios.user.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
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

/**
 * This class is to test user existence, send invitation, create user
 * and organization and test first login for that specific user
 * This tenant is created appending timestamp to the given tenantDomain
 * it is required to provide only two characters to tenant domain
 */
public class TenantCreationAndLoginTestCase extends CloudIntegrationTest {
    private static final Log log = LogFactory.getLog(TenantCreationAndLoginTestCase.class);

    private JaggeryAppAuthenticatorClient authenticatorClient;
    private String uuid;
    public static String tenantEmail;
    private String currentTimeStamp;

    private String signUpUrl = cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_SIGNUP_URL_SFX;
    private String confirmVerificationUrl = cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_SIGNUP_CONFIRM_URL_SFX;
    private String addNewTenantUrl = cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_ADD_NEW_TENANT_URL_SFX;
    private String addTenantUrl = cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_ADD_TENANT_URL_SFX;
    private String updateTenantProfileUrl = cloudMgtServerUrl + CloudIntegrationConstants.UPDATE_TENANT_PROFILE_SFX;
    private static final String tenantDomainPrefix =
            CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.NEW_TENANT_DOMAIN);
    private static final String tenantEmailSuffix = CloudIntegrationTestUtils
            .getPropertyValue(CloudIntegrationConstants.NEW_TENANT_ADMINUSER);
    public static String newAdminPassword;
    public static String newAdminUsername;

    @BeforeClass(alwaysRun = true) public void deployService() throws Exception {
        authenticatorClient = new JaggeryAppAuthenticatorClient(cloudMgtServerUrl);
        uuid = null;
        Calendar calendar = Calendar.getInstance();
        Long longTimeStamp = calendar.getTimeInMillis();
        currentTimeStamp = longTimeStamp.toString();
        tenantEmail = tenantDomainPrefix + currentTimeStamp + "@" + tenantEmailSuffix;
        newAdminPassword =
                CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.NEW_TENANT_ADMINPASSWORD) +
                currentTimeStamp;
        newAdminUsername = tenantDomainPrefix + currentTimeStamp + "@" + tenantEmailSuffix + "@" + tenantDomainPrefix +
                           currentTimeStamp;
    }

    /**
     * Checks the user existence in cloudmgt
     *
     * @throws Exception
     */
    @Test(description = "Check for existence of username")
    public void checkUsernameExistenceTest() throws Exception {
        log.info("started username existence test case ");
        Map<String, String> params = new HashMap<>();
        params.put(CloudIntegrationConstants.PARAMETER_KEY_ACTION, "isExistingUser");
        params.put("username", tenantEmail);
        Map resultMap = HttpHandler.doPostHttps(signUpUrl, params, null, false);
        Assert.assertEquals(resultMap.get(CloudIntegrationConstants.RESPONSE), "{\"error\" : false, \"status\" : 200," +
                                                                               " \"data\" : false}",
                            "failed to check the user existence");
    }

    /**
     * Sending invitation to the user and getting the uuid which was set for the transaction
     * Assert 1 : checks whether the email is sent successful
     * Assert 2 : When provided with confirm verification url checks whether it redirects to add-tenant.jag
     *
     * @throws Exception
     */
    @Test(description = "send the invitation for user", dependsOnMethods = {
            "checkUsernameExistenceTest" })
    public void checkSendInviteTest() throws Exception {
        log.info("started sending invite for user");
        Map<String, String> params = new HashMap<>();
        List<String> queryParameters = new ArrayList<>();

        params.put(CloudIntegrationConstants.PARAMETER_KEY_ACTION, "sendInvite");
        params.put("email", tenantEmail);
        Map resultMap = HttpHandler.doPostHttps(signUpUrl, params, null, false);
	    String response = "{\"error\" : false, \"status\" : 200, \"data\" : \"" + tenantEmail + "\"}";
        Assert.assertEquals(resultMap.get(CloudIntegrationConstants.RESPONSE), response,
                            "user invitation sending failed");
        DbConnectionManager con = new DbConnectionManager(CloudIntegrationConstants.CLOUD_MGT_DATASOURCE);
        queryParameters.add(tenantEmail);
        ResultSet queryResult = con.runQuery(CloudIntegrationConstants.GET_TEMP_UUID_FOR_REGISTRATION, queryParameters);
        if (queryResult.next()) {
	        uuid = queryResult.getString(1);
        }
        con.closeConnection();
        params.clear();

        params.put(CloudIntegrationConstants.PARAMETER_KEY_ACTION, "confirmUser");
        params.put("confirm", uuid);
        Map confirmMap = HttpHandler.doPostHttps(confirmVerificationUrl, params, null, false);
	    response = confirmMap.get("Response").toString();
	    ArrayList<String> responseData = new ArrayList<>(Arrays.asList(response.split(",")));
        Assert.assertEquals(responseData.get(2), " \"data\" : \"add-tenant.jag\"", "Adding the user to ldap failed");
    }

    /**
     * After invitation send is successful tenant creation is tested
     *
     * @throws Exception
     */
    @Test(description = "This will test add new tenant", dependsOnMethods = { "checkSendInviteTest" })
    public void addNewTenant() throws Exception {
        log.info("Adding new Tenant Started");
        Map<String, String> params = new HashMap<>();
        params.put(CloudIntegrationConstants.PARAMETER_KEY_ACTION, "registerOrg");
        params.put("companyName", tenantDomainPrefix + currentTimeStamp);
        params.put("adminPassword", newAdminPassword);
        params.put("usagePlan", CloudIntegrationTestUtils.getPropertyValue(
                CloudIntegrationConstants.NEW_TENANT_USAGE_PLAN));
        params.put("confirmationKey", uuid);
        params.put("firstName", CloudIntegrationTestUtils.getPropertyValue(
                CloudIntegrationConstants.NEW_TENANT_FIRSTNAME));
        params.put("lastName",
                   CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.NEW_TENANT_LASTNAME));
		    Map resultMap = HttpHandler.doPostHttps(addNewTenantUrl, params, null, false);
	    Assert.assertEquals(resultMap.get("Response"), CloudIntegrationConstants.TRUE, "Adding new tenant successful");

    }

    /**
     * Checks login status for the newly created user and organization
     *
     * @throws Exception
     */
    @Test(description = "Login test for the tenant", dependsOnMethods = { "addNewTenant" })
    public void loginTest() throws Exception {
        log.info("Started running test case login.");
        boolean loginStatus = authenticatorClient.login(newAdminUsername, newAdminPassword);
        Assert.assertTrue(loginStatus, CloudIntegrationConstants.LOGIN_ERROR_MESSAGE);
    }

    /**
     * Update profile of the tenant
     *
     * @throws Exception
     */
    @Test(description = "Update profile test for the tenant", dependsOnMethods = { "loginTest" })
    public void updateProfile() throws Exception {
        log.info("Started running test case update tenant profile.");
        Map<String, String> params = new HashMap<>();
        params.put(CloudIntegrationConstants.PARAMETER_KEY_ACTION, "updateProfile");
        params.put("tenantDomain", tenantDomainPrefix + currentTimeStamp);
        //Updated the display name
        params.put("displayName", tenantDomainPrefix + currentTimeStamp + "_Updated");
        Map resultMap = HttpHandler
                .doPostHttps(updateTenantProfileUrl, params, authenticatorClient.getSessionCookie(), false);
        JSONObject resultObj = new JSONObject(resultMap.get(CloudIntegrationConstants.RESPONSE).toString());
        Assert.assertEquals(resultObj.get(CloudIntegrationConstants.ERROR).toString(), CloudIntegrationConstants.FALSE,
                "Value mismatch, Should be false.");
    }

    /**
     * Add tenant for an existing user
     *
     * @throws Exception
     */
    @Test(description = "Add tenant test for an existing user", dependsOnMethods = { "loginTest" })
    public void addTenant() throws Exception {
        log.info("Started running test case add tenant for existing user.");
        Map<String, String> params = new HashMap<>();
        params.put(CloudIntegrationConstants.PARAMETER_KEY_ACTION, "registerOrg");
        params.put("companyName", tenantDomainPrefix + currentTimeStamp + "_New");
        params.put("adminPassword", newAdminPassword);
        params.put("usagePlan",
                CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.NEW_TENANT_USAGE_PLAN));
        Map resultMap = HttpHandler.doPostHttps(addTenantUrl, params, authenticatorClient.getSessionCookie(), false);
        Assert.assertEquals(resultMap.get(CloudIntegrationConstants.RESPONSE), CloudIntegrationConstants.TRUE,
                "Adding tenant for existing user successful");
    }

    /**
     * Checks logout status for the newly created user and organization
     *
     * @throws Exception
     */
    @Test(description = "logout test", dependsOnMethods = { "loginTest", "updateProfile", "addTenant"})
    public void logoutTest() throws Exception {
        log.info("Started running test case log out.");
        boolean logOutStatus = authenticatorClient.logout();
        Assert.assertTrue(logOutStatus, "tenant log out failed");
    }

    @AfterClass(alwaysRun = true) public void unDeployService() throws Exception {
        super.cleanup();
    }
}