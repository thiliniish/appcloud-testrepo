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
package org.wso2.carbon.cloud.test.scenarios;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

public class UserProfileManagementTestCase extends CloudIntegrationTest {
    private static final Log log = LogFactory.getLog(UserProfileManagementTestCase.class);

    protected String userName;
    protected String password;
    protected String firstName;
    protected String lastName;

    private JaggeryAppAuthenticatorClient authenticatorClient;
    private boolean loginStatus = false;

    @BeforeClass(alwaysRun = true) public void deployService() throws Exception {
        //initializing user info
        userName = CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.TENANT_USER_USERNAME);
        password = CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.TENANT_USER_PASSWORD);
        firstName = CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.TENANT_USER_FIRST_NAME);
        lastName = CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.TENANT_USER_LAST_NAME);

        authenticatorClient = new JaggeryAppAuthenticatorClient(cloudMgtServerUrl);
        loginStatus = authenticatorClient.login(userName, password);
        if (!loginStatus) {
            String msg = "Authentication failure for cloudmgt app before Change Password test for user : "
                    + userName;
            log.warn(msg);
            throw new Exception(msg);
        }
    }

    @Test(description = "Edit Profile Test for User") public void editProfileTest() throws Exception {
        log.info("Running Edit Profile test");
        String url = cloudMgtServerUrl + CloudIntegrationConstants.USER_PROFILE_URL_SFX;
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "updateProfile");
        params.put("firstName", "FirstName");
        params.put("lastName", "LastName");
        Map responseMap = HttpHandler.doPostHttps(url, params, authenticatorClient.getSessionCookie());
        String result = (String) responseMap.get(CloudIntegrationConstants.RESPONSE);
        Assert.assertEquals(result, "true");

        params = new HashMap<String, String>();
        params.put("action", "getProfile");
        params.put("user", userName);
        responseMap = HttpHandler.doPostHttps(url, params, authenticatorClient.getSessionCookie());
        result = (String) responseMap.get(CloudIntegrationConstants.RESPONSE);
        if(result != null && !result.isEmpty()) {
            JsonParser parser = new JsonParser();
            JsonObject jsonResult = parser.parse(result).getAsJsonObject();
            Assert.assertEquals(jsonResult.get("firstname").getAsString(), "FirstName");
            Assert.assertEquals(jsonResult.get("lastname").getAsString(), "LastName");
        }
    }

    @Test(description = "Change Password Test for User") public void changePasswordTest() throws Exception {
        log.info("Running Change Password test");
        String url = cloudMgtServerUrl + CloudIntegrationConstants.CHANGE_PASSWORD_URL_SFX;
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "changePassword");
        params.put("oldPassword", password);
        params.put("password", "temp$pass1");
        Map responseMap = HttpHandler.doPostHttps(url, params, authenticatorClient.getSessionCookie());
        String result = (String) responseMap.get(CloudIntegrationConstants.RESPONSE);
        Assert.assertEquals(result, "true");

        authenticatorClient = new JaggeryAppAuthenticatorClient(cloudMgtServerUrl);
        loginStatus = authenticatorClient.login(userName, "temp$pass1");
        Assert.assertEquals(loginStatus, true);
    }

    @AfterClass(alwaysRun = true) public void unDeployService() throws Exception {
        String passwordUrl = cloudMgtServerUrl + CloudIntegrationConstants.CHANGE_PASSWORD_URL_SFX;
        String profileUrl = cloudMgtServerUrl + CloudIntegrationConstants.USER_PROFILE_URL_SFX;
        Map<String, String> params;
        Map responseMap;
        String result;

        params = new HashMap<String, String>();
        params.put("action", "updateProfile");
        params.put("firstName", firstName);
        params.put("lastName", lastName);
        responseMap = HttpHandler.doPostHttps(profileUrl, params, authenticatorClient.getSessionCookie());
        result = (String) responseMap.get(CloudIntegrationConstants.RESPONSE);
        if (!result.equals("true")) {
            String msg = "Error occurred while reverting the user profile changes after profile update";
            log.warn(msg);
        }

        params = new HashMap<String, String>();
        params.put("action", "changePassword");
        params.put("oldPassword", "temp$pass1");
        params.put("password", password);
        responseMap = HttpHandler.doPostHttps(passwordUrl, params, authenticatorClient.getSessionCookie());
        result = (String) responseMap.get(CloudIntegrationConstants.RESPONSE);
        if (!result.equals("true")) {
            String msg = "Error occurred while resetting to the old password after change password";
            log.warn(msg);
        }

        authenticatorClient.logout();
        super.cleanup();
    }
}