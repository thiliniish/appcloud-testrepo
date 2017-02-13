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
 * This class is to test the billing subscription plan change with/without a coupon for a billing enabled tenant.
 * Test cases will cover upgrade and downgrade billing account with a coupon and without a coupon.
 */
public class SubscriptionPlanChangeTestCase extends CloudIntegrationTest {

	private static final Log log = LogFactory.getLog(SubscriptionPlanChangeTestCase.class);
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
	 * Upgrade Account without a coupon
	 *
	 * @throws Exception
	 */
	@Test(description = "Upgrade the Billing Account without a coupon for a Billing enabled tenant")
	public void upgradeAccountWithoutCoupon() throws Exception {
		log.info("Started running test case upgrade Billing Account without coupon.");
		Assert.assertTrue(loginStatus, CloudIntegrationConstants.LOGIN_ERROR_MESSAGE);
		String errorMessage = "Error upgrading the  billing account without coupon.";
		String upgradeRatePlanId = CloudIntegrationTestUtils
				.getPropertyValue(CloudIntegrationConstants.BILLING_PAYMENT_UPGRADE_RATE_PLAN_ID);

		String upgradeAccountWithoutCouponStatus = changeSubscriptionPlan(upgradeRatePlanId, null);
		Assert.assertEquals(upgradeAccountWithoutCouponStatus, CloudIntegrationConstants.TRUE, errorMessage);
	}

	/**
	 * Downgrade Account without Coupon
	 *
	 * @throws Exception
	 */
	@Test(dependsOnMethods = {
			"upgradeAccountWithoutCoupon" }, description = "Downgrade the Billing Account without a coupon for a Billing enabled tenant")
	public void downgradeAccountWithoutCoupon() throws Exception {
		log.info("Started running test case downgrade Billing Account without coupon.");
		Assert.assertTrue(loginStatus, CloudIntegrationConstants.LOGIN_ERROR_MESSAGE);
		String errorMessage = "Error downgrading the  billing account without coupon.";
		String downgradeRatePlanId = CloudIntegrationTestUtils
				.getPropertyValue(CloudIntegrationConstants.BILLING_PAYMENT_PRODUCT_RATE_PLAN_ID);

		String downgradeAccountWithoutCouponStatus = changeSubscriptionPlan(downgradeRatePlanId, null);
		Assert.assertEquals(downgradeAccountWithoutCouponStatus, CloudIntegrationConstants.TRUE, errorMessage);
	}

	/**
	 * Upgrade Account with a coupon
	 *
	 * @throws Exception
	 */
	@Test(dependsOnMethods = {
			"downgradeAccountWithoutCoupon" }, description = "Upgrade the Billing Account with a coupon for a Billing enabled tenant")
	public void upgradeAccountWithCoupon() throws Exception {
		log.info("Started running test case upgrade Billing Account with a coupon.");
		Assert.assertTrue(loginStatus, CloudIntegrationConstants.LOGIN_ERROR_MESSAGE);
		String errorMessage = "Error upgrading the  billing account with a coupon.";
		String upgradeRatePlanId = CloudIntegrationTestUtils
				.getPropertyValue(CloudIntegrationConstants.BILLING_PAYMENT_UPGRADE_RATE_PLAN_ID);
		String couponId =
				CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.BILLING_PAYMENT_COUPON_ID);

		String upgradeAccountWithCouponStatus = changeSubscriptionPlan(upgradeRatePlanId, couponId);
		Assert.assertEquals(upgradeAccountWithCouponStatus, CloudIntegrationConstants.TRUE, errorMessage);
	}

	/**
	 * Downgrade Account with a coupon
	 *
	 * @throws Exception
	 */
	@Test(dependsOnMethods = {
			"upgradeAccountWithCoupon" }, description = "Downgrade the Billing Account with a Coupon for a Billing enabled tenant")
	public void downgradeAccountWithCoupon() throws Exception {
		log.info("Started running test case downgrade Billing Account with a coupon.");
		Assert.assertTrue(loginStatus, CloudIntegrationConstants.LOGIN_ERROR_MESSAGE);
		String errorMessage = "Error downgrading the  billing account with a coupon.";
		String downgradeRatePlanId = CloudIntegrationTestUtils
				.getPropertyValue(CloudIntegrationConstants.BILLING_PAYMENT_PRODUCT_RATE_PLAN_ID);
		String couponId =
				CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.BILLING_PAYMENT_COUPON_ID);

		String downgradeAccountWithCouponStatus = changeSubscriptionPlan(downgradeRatePlanId, couponId);
		Assert.assertEquals(downgradeAccountWithCouponStatus, CloudIntegrationConstants.TRUE, errorMessage);
	}

	/**
	 * Method to change the subscription plan
	 *
	 * @param productRatePlanId rate plan id to be change
	 * @param couponId          coupon id
	 * @return response from the change subscription plan
	 * @throws Exception
	 */
	public String changeSubscriptionPlan(String productRatePlanId, String couponId) throws Exception {
		String serviceId =
				CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.BILLING_PAYMENT_SERVICE_ID);
		String accountId =
				CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.BILLING_PAYMENT_ACCOUNT_ID);
		Map<String, String> params = new HashMap<>();
		params.put(CloudIntegrationConstants.PARAMETER_KEY_ACTION, "createAccount");
		params.put(CloudIntegrationConstants.PARAMETER_KEY_RESPONSE_FROM, "isFromChangePlan");
		params.put(CloudIntegrationConstants.PARAMETER_KEY_SERVICE_ID, serviceId);
		params.put(CloudIntegrationConstants.PARAMETER_KEY_PRODUCT_RATE_PLAN_ID, productRatePlanId);
		params.put(CloudIntegrationConstants.PARAMETER_KEY_ACCOUNT_ID, accountId);
		params.put(CloudIntegrationConstants.PARAMETER_KEY_COUPON_ID, couponId);
		params.put(CloudIntegrationConstants.PARAMETER_KEY_RATE_PLANS, null);

		String changeSubscriptionPlanUrl =
				cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_BILLING_ACCOUNT_DETAILS_ADD_URL_SFX;
		Map result = HttpHandler
				.doPostHttps(changeSubscriptionPlanUrl, params, authenticatorClient.getSessionCookie(), false);
		return result.get(CloudIntegrationConstants.RESPONSE).toString();
	}

	@AfterClass(alwaysRun = true)
	public void destroy() throws Exception {
		authenticatorClient.logout();
		super.cleanup();
	}
}
