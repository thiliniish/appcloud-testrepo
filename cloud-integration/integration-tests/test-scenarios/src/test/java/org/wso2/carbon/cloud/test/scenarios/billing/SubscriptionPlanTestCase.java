/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.cloud.test.scenarios.billing;

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
 * This class is to test the billing subscription plan view billing enabled tenant.
 */
public class SubscriptionPlanTestCase extends CloudIntegrationTest {

	private static final Log log = LogFactory.getLog(SubscriptionPlanTestCase.class);
	private JaggeryAppAuthenticatorClient authenticatorClient;
	private boolean loginStatus;

	/**
	 * Before test tenant user will get authenticated.
	 *
	 * @throws Exception
	 */
	@BeforeClass(alwaysRun = true)
	public void setEnvironment() throws Exception {
		authenticatorClient = new JaggeryAppAuthenticatorClient(cloudMgtServerUrl);
		loginStatus = authenticatorClient.login(tenantAdminUserName, tenantAdminPassword);
	}

	/**
	 * Get billing subscription plan
	 *
	 * @throws Exception
	 */
	@Test(description = "Get billing subscription plan for a Billing enabled tenant")
	public void getBillingSubscriptionPlan() throws Exception {
		log.info("Started running test case get billing subscription plan.");
		Assert.assertTrue(loginStatus, CloudIntegrationConstants.LOGIN_ERROR_MESSAGE);
		String errorMessage = "Error getting the billing subscription plan.";
		String serviceId =
				CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.BILLING_PAYMENT_SERVICE_ID);
		Map<String, String> params = new HashMap<>();
		params.put(CloudIntegrationConstants.PARAMETER_KEY_ACTION, "getRatePlans");
		params.put(CloudIntegrationConstants.PARAMETER_KEY_SERVICE_ID, serviceId);
		String changeSubscriptionPlanUrl =
				cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_BILLING_BILLING_PLAN_GET_URL_SFX;
		Map result = HttpHandler
				.doPostHttps(changeSubscriptionPlanUrl, params, authenticatorClient.getSessionCookie(), false);
		JSONObject resultObj =
				new JSONObject(result.get(CloudIntegrationConstants.RESPONSE).toString());
		Assert.assertNotNull(resultObj, errorMessage);
	}

	/**
	 * Cancel billing subscription plan
	 *
	 * @throws Exception
	 */
	@Test(description = "Cancel billing subscription plan")
	public void cancelBillingSubscriptionPlan() throws Exception {
		log.info("Started running test case cancel Billing subscription plan.");
		Assert.assertTrue(loginStatus, CloudIntegrationConstants.LOGIN_ERROR_MESSAGE);
		String errorMessage = "Error cancelling the billing subscription plan.";
		String serviceId =
				CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.BILLING_PAYMENT_SERVICE_ID);
		Map<String, String> params = new HashMap<>();
		params.put(CloudIntegrationConstants.PARAMETER_KEY_ACTION, "cancelSubscription");
		params.put(CloudIntegrationConstants.PARAMETER_KEY_SERVICE_ID, serviceId);
		String changeSubscriptionPlanUrl =
				cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_BILLING_BILLING_PLAN_REMOVE_URL_SFX;
		Map result = HttpHandler
				.doPostHttps(changeSubscriptionPlanUrl, params, authenticatorClient.getSessionCookie(), false);
		Assert.assertEquals(result.get(CloudIntegrationConstants.RESPONSE).toString(), CloudIntegrationConstants.TRUE,
		                    errorMessage);
	}

	@AfterClass(alwaysRun = true)
	public void unDeployService() throws Exception {
		authenticatorClient.logout();
		super.cleanup();
	}
}
