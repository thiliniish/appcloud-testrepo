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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.cloud.test.scenarios.selfSignup;

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
import org.wso2.carbon.cloud.test.scenarios.user.mgt.TenantCreationAndLoginTestCase;

import java.util.*;

/**
 * This class is to test self sign up feature
 */
public class SelfSignupTestCase extends CloudIntegrationTest {
	private static final Log log = LogFactory.getLog(SelfSignupTestCase.class);
	private JaggeryAppAuthenticatorClient authenticatorClient;
	private boolean loginStatus;
	private String newAdminUsername;
	private String newAdminPassword;
	private String tenantEmail;
	private final String apiMgtServerUrl =
			CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.API_MGT_SERVER_URL);
	private final String apiMgrGatewayUrl =
			CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.API_GATEWAY_ENDPOINT_URL);

	/**
	 * Before test tenant user will get authenticated.
	 *
	 * @throws Exception
	 */
	@BeforeClass(alwaysRun = true)
	public void setEnvironment() throws Exception {
		newAdminUsername = TenantCreationAndLoginTestCase.newAdminUsername;
		newAdminPassword = TenantCreationAndLoginTestCase.newAdminPassword;
		tenantEmail = TenantCreationAndLoginTestCase.tenantEmail;

		// Login to the API Publisher
		authenticatorClient = new JaggeryAppAuthenticatorClient(apiMgtServerUrl,
		                                                        CloudIntegrationConstants.API_PUBLISHER_LOGIN_URL_SFX);
		loginStatus = authenticatorClient.login(newAdminUsername, newAdminPassword);
		Assert.assertTrue(loginStatus, CloudIntegrationConstants.PUBLISHER_LOGIN_ERROR_MESSAGE);
		authenticatorClient.logout();
		// Login to the clougmgt
		authenticatorClient = new JaggeryAppAuthenticatorClient(cloudMgtServerUrl);
		loginStatus = authenticatorClient.login(newAdminUsername, newAdminPassword);
		Assert.assertTrue(loginStatus, CloudIntegrationConstants.LOGIN_ERROR_MESSAGE);
	}

	/**
	 * Enable self sign-up feature with sign-up approval request
	 * This test will check for enable of self sign up feature with sign-up approval request
	 *
	 * @throws Exception
	 */
	@Test(description = "Enable self sign-up feature with sign-up approval request.")
	public void enableSelfSignupWithApprovalRequest() throws Exception {
		log.info("Started running test case for enable self sign up with sign-up approval request.");
		Assert.assertTrue(loginStatus, CloudIntegrationConstants.LOGIN_ERROR_MESSAGE);
		String errorMessage = "Error while enabling self sign up feature with sign-up approval request.";
		Map resultMap = configureSelfSignup("approved");
		JSONObject resultObj = new JSONObject(resultMap.get(CloudIntegrationConstants.RESPONSE).toString());
		String expectedResponseMsg = "Completed the uploading process for the user " + newAdminUsername;
		Assert.assertEquals(resultObj.getString("message"), expectedResponseMsg, errorMessage);
	}

	/**
	 * Enable self sign-up feature for automatically approve new members on their request
	 * This test will check for enable of self sign up feature for automatically approve new members on their request
	 *
	 * @throws Exception
	 */
	@Test(description = "Enable self sign-up feature for automatically approve new members on their request.")
	public void enableSelfSignupWithoutApprovalRequest() throws Exception {
		log.info(
				"Started running test case for enable self sign up for automatically approve new members on their request.");
		Assert.assertTrue(loginStatus, CloudIntegrationConstants.LOGIN_ERROR_MESSAGE);
		String errorMessage =
				"Error while enabling self sign up feature for automatically approve new members on their request.";
		Map resultMap = configureSelfSignup("notApproved");
		JSONObject resultObj = new JSONObject(resultMap.get(CloudIntegrationConstants.RESPONSE).toString());
		String expectedResponseMsg = "Completed the uploading process for the user " + newAdminUsername;
		Assert.assertEquals(resultObj.getString("message"), expectedResponseMsg, errorMessage);
	}

	/**
	 * Check self sign-up feature configured for user.
	 * This test will check self signup configured for user.
	 *
	 * @throws Exception
	 */
	@Test(description = "Enable self sign-up feature.")
	public void checkSignUpConfiguredForUser() throws Exception {
		log.info("Started running test case for check self signup configured for user.");
		String errorMessage = "Error while checking self signup configured for user.";
		Map resultMap = configureSelfSignup("approved");
		JSONObject resultObj = new JSONObject(resultMap.get(CloudIntegrationConstants.RESPONSE).toString());
		String expectedResponseMsg = "self sign-up has been successfully configured for the user " + newAdminUsername;
		Assert.assertEquals(resultObj.getString("message"), expectedResponseMsg, errorMessage);
	}

	private Map configureSelfSignup(String signupType) throws Exception {
		String username = newAdminUsername;
		String fromAddress = "TEST";
		String userPassword = newAdminPassword;
		String contactEmail = tenantEmail;
		Map<String, String> params = new HashMap<>();
		params.put(CloudIntegrationConstants.PARAMETER_KEY_ACTION, "checkSignUpConfiguredForUser");
		params.put("username", username);
		params.put("fromAddress", fromAddress);
		params.put("userPassword", userPassword);
		params.put("contactEmail", contactEmail);
		params.put("signupType", signupType);

		String enableSelfSignupUrl = cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_SELF_SIGNUP_ENABLE_URL_SFX;
		return HttpHandler.doPostHttps(enableSelfSignupUrl, params, authenticatorClient.getSessionCookie(), false);
	}

	@AfterClass(alwaysRun = true)
	public void unDeployService() throws Exception {
		boolean logOutStatus = authenticatorClient.logout();
		Assert.assertTrue(logOutStatus, "Tenant log out failed.");
		super.cleanup();
	}
}