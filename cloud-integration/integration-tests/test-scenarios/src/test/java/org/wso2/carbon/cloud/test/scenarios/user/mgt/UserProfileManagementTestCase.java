/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
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
import org.wso2.carbon.cloud.integration.test.utils.external.HttpHandler;
import org.wso2.carbon.cloud.integration.test.utils.restclients.JaggeryAppAuthenticatorClient;

import java.util.HashMap;
import java.util.Map;

/**
 * This is to test User Edit profile (First Name & Last Name), and Password Change.
 */
public class UserProfileManagementTestCase extends CloudIntegrationTest {
    private static final Log log = LogFactory.getLog(UserProfileManagementTestCase.class);

    private String userName;
    private String password;
    private String firstName;
    private String lastName;

    private static final String TEMP_PASSWORD = "Admin@321";

    private JaggeryAppAuthenticatorClient authenticatorClient;

    /**
     * Before test, Getting user information and authenticating the user
     *
     * @throws Exception
     */
    @BeforeClass(alwaysRun = true) public void deployService() throws Exception {
        //initializing user info
        userName = CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.TENANT_USER_USERNAME);
        password = CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.TENANT_USER_PASSWORD);
        firstName = CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.TENANT_USER_FIRST_NAME);
        lastName = CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.TENANT_USER_LAST_NAME);

        authenticatorClient = new JaggeryAppAuthenticatorClient(cloudMgtServerUrl);
        boolean loginStatus = authenticatorClient.login(userName, password);
        if (!loginStatus) {
            String msg = "Authentication failure for CloudMgt app before Edit Profile & Change Password tests"
                    + " for user : " + userName;
            log.error(msg);
            throw new Exception(msg);
        }
    }

    /**
     * Testing Edit Profile.
     * <p/>
     * <ul>
     * <li>Step 1: Change the first name and last name and check weather its successful.</li>
     * <li>Step 2: Retrieve first name and and last name and verify they are changed.</li>
     * <ul/>
     *
     * @throws Exception
     */
    @Test(description = "Edit Profile Test for User") public void editProfileTest() throws Exception {
        log.info("Running Edit Profile test");
        String url = cloudMgtServerUrl + CloudIntegrationConstants.USER_PROFILE_URL_SFX;
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "updateProfile");
        params.put("firstName", "FirstName");
        params.put("lastName", "LastName");
        Map resultMap = HttpHandler.doPostHttps(url, params, authenticatorClient.getSessionCookie());
        String result = (String) resultMap.get(CloudIntegrationConstants.RESPONSE);
        Assert.assertEquals(result, "true", "Value mismatch, Should be true.");

        params = new HashMap<String, String>();
        params.put("action", "getProfile");
        params.put("user", userName);
        resultMap = HttpHandler.doPostHttps(url, params, authenticatorClient.getSessionCookie());
        JSONObject resultObj = new JSONObject(resultMap.get(CloudIntegrationConstants.RESPONSE).toString());
        Assert.assertEquals(resultObj.getString("firstname"), "FirstName",
                "FirstName does not match with the new value");
        Assert.assertEquals(resultObj.getString("lastname"), "LastName", "LastName does not match with the new value");
    }

    /**
     * Testing Change Password
     * <p/>
     * <ul>
     * <li>Step 1: Change the password and check weather its successful</li>
     * <li>Step 2: Login using new password and check weather its successful</li>
     * </ul>
     *
     * @throws Exception
     */
    @Test(description = "Change Password Test for User") public void changePasswordTest() throws Exception {
        log.info("Running Change Password test");
        String url = cloudMgtServerUrl + CloudIntegrationConstants.CHANGE_PASSWORD_URL_SFX;
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "changePassword");
        params.put("oldPassword", password);
        params.put("password", TEMP_PASSWORD);
        Map responseMap = HttpHandler.doPostHttps(url, params, authenticatorClient.getSessionCookie());
        String result = (String) responseMap.get(CloudIntegrationConstants.RESPONSE);
        Assert.assertEquals(result, "true", "Value mismatch, Should be true.");

        authenticatorClient = new JaggeryAppAuthenticatorClient(cloudMgtServerUrl);
        boolean loginStatus = authenticatorClient.login(userName, TEMP_PASSWORD);
        Assert.assertTrue(loginStatus, "Cannot login with new password");
    }

    /**
     * Reverting profile update and password change
     *
     * @throws Exception
     */
    @AfterClass(alwaysRun = true) public void unDeployService() throws Exception {
        String passwordUrl = cloudMgtServerUrl + CloudIntegrationConstants.CHANGE_PASSWORD_URL_SFX;
        String profileUrl = cloudMgtServerUrl + CloudIntegrationConstants.USER_PROFILE_URL_SFX;
        Map<String, String> params;
        Map responseMap;

        params = new HashMap<String, String>();
        params.put("action", "updateProfile");
        params.put("firstName", firstName);
        params.put("lastName", lastName);
        responseMap = HttpHandler.doPostHttps(profileUrl, params, authenticatorClient.getSessionCookie());
        String updateProfileResult = (String) responseMap.get(CloudIntegrationConstants.RESPONSE);

        params = new HashMap<String, String>();
        params.put("action", "changePassword");
        params.put("oldPassword", TEMP_PASSWORD);
        params.put("password", password);
        responseMap = HttpHandler.doPostHttps(passwordUrl, params, authenticatorClient.getSessionCookie());
        String changePassResult = (String) responseMap.get(CloudIntegrationConstants.RESPONSE);

        authenticatorClient.logout();
        super.cleanup();

        Assert.assertEquals(updateProfileResult, "true",
                "Error occurred while reverting the user profile changes after Profile Update Test.");
        Assert.assertEquals(changePassResult, "true",
                "Error occurred while resetting to the old password after Change Password Test.");

    }
}