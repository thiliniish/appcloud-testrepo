/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ApiBillingUsageTestCase extends CloudIntegrationTest {
	private static final Log log = LogFactory.getLog(AccountInfoTestCase.class);
	private JaggeryAppAuthenticatorClient authenticatorClient;
	private String isApiExist;
	private boolean loginStatus;

	/**
	 * Before test tenant user will get authenticated.
	 *
	 * @throws Exception
	 */
	@BeforeClass(alwaysRun = true) public void setEnvironment() throws Exception {

		//login to the API Publisher
		authenticatorClient = new JaggeryAppAuthenticatorClient(apiMgtServerUrl,
		                                                        CloudIntegrationConstants.API_PUBLISHER_LOGIN_URL_SFX);
		loginStatus = authenticatorClient.login(tenantAdminUserName, tenantAdminPassword);
		if (loginStatus) {
			//Checking if the API is already exist in the tenant
			Map<String, String> paramsApiExist = new HashMap<String, String>();
			paramsApiExist.put("action", "isAPINameExist");
			paramsApiExist.put("apiName", CloudIntegrationConstants.API_NAME);
			String apiExistUrl =
					apiMgtServerUrl + CloudIntegrationConstants.PUBLISHER_ADD_API_URL_SFX;
			Map resultIsApiExist = HttpHandler.doPostHttps(apiExistUrl, paramsApiExist,
			                                               authenticatorClient.getSessionCookie());
			isApiExist = (String) (new JSONObject(
					resultIsApiExist.get(CloudIntegrationConstants.RESPONSE).toString()))
					.get("exist");
			//create the API if API dose not exists
			if (isApiExist.equalsIgnoreCase("false")) {
				Map<String, String> params = new HashMap<String, String>();
				params.put("action", "addAPI");
				params.put("name", CloudIntegrationConstants.API_NAME);
				params.put("context", CloudIntegrationConstants.API_CONTEXT);
				params.put("visibility", "public");
				params.put("thumbUrl", "");
				params.put("endpointType", "nonsecured");
				params.put("tags", "statsApi");
				params.put("description", "This is create to test stats generation");
				params.put("version", CloudIntegrationConstants.API_VERSION);
				params.put("https_checked", "https");
				params.put("http_checked", "http");
				params.put("resourceCount", "0");
				params.put("resourceMethod-0", "POST");
				params.put("resourceMethodAuthType-0", "Application User");
				params.put("uriTemplate-0", "/*");
				params.put("default_version_checked", "default_version");
				params.put("resourceMethodThrottlingTier-0", "Gold");
				params.put("tiersCollection", "Gold,Bronze");
				params.put("endpoint_config", CloudIntegrationConstants.API_ENDPOINT);

				String apiCreationUrl =
						apiMgtServerUrl + CloudIntegrationConstants.PUBLISHER_ADD_API_URL_SFX;
				Map result = HttpHandler.doPostHttps(apiCreationUrl, params,
				                                     authenticatorClient.getSessionCookie());
				if (!isOperationSuccess(result)) {
					log.error("Error occurred while creating the API. " +
					          result.get(CloudIntegrationConstants.RESPONSE).toString());
				} else {
					params.clear();
					params.put("action", "updateStatus");
					params.put("status", "PUBLISHED");
					params.put("publishToGateway", "true");
					params.put("requireResubscription", "true");
					params.put("name", CloudIntegrationConstants.API_NAME);
					params.put("version", CloudIntegrationConstants.API_VERSION);
					params.put("provider", tenantAdminUserName);

					String changeStatusApiUrl = apiMgtServerUrl +
					                            CloudIntegrationConstants.PUBLISHER_LIFE_CYCLE_URL_SFX;
					Map resultOfStatusChange = HttpHandler.doPostHttps(changeStatusApiUrl, params,
					                                                   authenticatorClient
							                                                   .getSessionCookie());
					if (!isOperationSuccess(resultOfStatusChange)) {
						log.error("Error occurred while changing the API Status. " +
						          resultOfStatusChange.get(CloudIntegrationConstants.RESPONSE)
						                              .toString());
					}

					params.clear();
					authenticatorClient.logout();
					super.cleanup();
					//login to the API Store
					authenticatorClient = new JaggeryAppAuthenticatorClient(apiMgtServerUrl,
					                                                        CloudIntegrationConstants.API_STORE_LOGIN_URL_SFX);
					loginStatus =
							authenticatorClient.login(tenantAdminUserName, tenantAdminPassword);
					if (loginStatus) {
						//adding the Application
						params.put("action", "addApplication");
						params.put("application", CloudIntegrationConstants.APP_NAME);
						params.put("tier", "Gold");
						params.put("description", "Application for the Api");
						params.put("callbackUrl", "");

						String ApplicationCreation = apiMgtServerUrl +
						                             CloudIntegrationConstants.STORE_ADD_APPLICATION_URL_SFX;

						Map resultOfAppCreation = HttpHandler
								.doPostHttps(ApplicationCreation, params,
								             authenticatorClient.getSessionCookie());
						if (!isOperationSuccess(resultOfAppCreation)) {
							log.error("Error occurred while creating the application " +
							          resultOfAppCreation.get(CloudIntegrationConstants.RESPONSE)
							                             .toString());
						}
						//Subscribe to the API
						params.clear();
						params.put("action", "addAPISubscription");
						params.put("name", CloudIntegrationConstants.API_NAME);
						params.put("version", CloudIntegrationConstants.API_VERSION);
						params.put("provider", tenantAdminUserName);
						params.put("tier", "Gold");
						params.put("applicationName", CloudIntegrationConstants.APP_NAME);

						String subscribeToApi = apiMgtServerUrl +
						                        CloudIntegrationConstants.STORE_ADD_SUBSCRIPTION_URL_SFX;

						Map resultSubscribeToAPI = HttpHandler.doPostHttps(subscribeToApi, params,
						                                                   authenticatorClient
								                                                   .getSessionCookie());
						if (!isOperationSuccess(resultSubscribeToAPI) ||
						    resultSubscribeToAPI == null) {
							log.error("Error occurred while subscribing to the Api");
						}
						//Generate Application Keys
						params.clear();
						params.put("action", "generateApplicationKey");
						params.put("application", CloudIntegrationConstants.APP_NAME);
						params.put("keytype", CloudIntegrationConstants.APP_SCOPE);
						params.put("callbackUrl", "");
						params.put("authorizedDomains", "ALL");
						params.put("validityTime", "600");

						String generateApiKeys = apiMgtServerUrl +
						                         CloudIntegrationConstants.STORE_ADD_SUBSCRIPTION_URL_SFX;

						Map resultOfKeyGeneration = HttpHandler.doPostHttps(generateApiKeys, params,
						                                                    authenticatorClient
								                                                    .getSessionCookie());
						if (resultOfKeyGeneration == null ||
						    !isOperationSuccess(resultOfKeyGeneration)) {
							log.error("Error occurred while generating Application Keys ");
						}

					}
				}
			}
		}
		authenticatorClient.logout();
		super.cleanup();

		authenticatorClient = new JaggeryAppAuthenticatorClient(cloudMgtServerUrl);
		loginStatus = authenticatorClient.login(tenantAdminUserName, tenantAdminPassword);

	}

	private boolean isOperationSuccess(Map result) throws JSONException {
		return "false"
				.equals(new JSONObject(result.get(CloudIntegrationConstants.RESPONSE).toString())
						        .getString("error"));
	}

	/**
	 * Check if the usage data is correct.
	 *
	 * @throws Exception
	 */
	@Test(description = "Test to validate the api usage") public void test() throws Exception {
		log.info("started running test case API billing usage");

		//date rage for the stats data
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		String today = dateFormat.format(cal.getTime());
		cal.add(Calendar.DATE, -2);
		String twoDaysBack = dateFormat.format(cal.getTime());
		DbConnectionManager con =
				new DbConnectionManager(CloudIntegrationConstants.APIM_STATS_DATASOURCE);
		String queryResultLength = null;
		try {
			List<String> queryParameters = new ArrayList<String>();
			queryParameters.add(CloudIntegrationTestUtils.getPropertyValue(
					CloudIntegrationConstants.TENANT_ADMIN_USER_NAME));
			queryParameters.add(twoDaysBack);
			queryParameters.add(today);

			ResultSet queryResult =
					con.runQuery(CloudIntegrationConstants.GET_API_STATS_USAGE, queryParameters);

			if (queryResult.next()) {
				queryResultLength = queryResult.getString(1);
			}
		} finally {
			con.closeConnection();
		}

		Map<String, String> params = new HashMap<String, String>();
		params.put("action", "getTenantUsage");
		params.put("fromDate", twoDaysBack);
		params.put("toDate", today);

		String getAPIUsageUrl =
				cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_BILLING_API_USAGE;
		Map result = HttpHandler
				.doPostHttps(getAPIUsageUrl, params, authenticatorClient.getSessionCookie());
		JSONObject resultJson =
				new JSONObject(result.get(CloudIntegrationConstants.RESPONSE).toString());
		JSONArray usageDataArray = resultJson.getJSONArray("entry");
		if (isApiExist.equalsIgnoreCase("true") &&
		    (queryResultLength == null || usageDataArray.length() == 0)) {
			log.warn("API exist but no stat data available.");
		}
		Assert.assertEquals(queryResultLength, String.valueOf(usageDataArray.length()),
		                    " Error in validating the usage data");

	}

	@AfterClass(alwaysRun = true) public void invokeAndUnDeployService() throws Exception {

		authenticatorClient.logout();
		super.cleanup();
		//login to the API Store
		authenticatorClient = new JaggeryAppAuthenticatorClient(apiMgtServerUrl,
		                                                        CloudIntegrationConstants.API_STORE_LOGIN_URL_SFX);
		loginStatus = authenticatorClient.login(tenantAdminUserName, tenantAdminPassword);

		Map<String, String> params = new HashMap<String, String>();
		params.put("action", "getAllSubscriptions");
		String getAppSubscriptionUrl =
				apiMgtServerUrl + CloudIntegrationConstants.STORE_LIST_SUBSCRIPTION_URL_SFX;

		Map resultAppSubscription = HttpHandler
				.doPostHttps(getAppSubscriptionUrl, params, authenticatorClient.getSessionCookie());
		params.clear();
		JSONObject getAllSubscriptionsObj = new JSONObject(
				resultAppSubscription.get(CloudIntegrationConstants.RESPONSE).toString());
		JSONArray array =
				getAllSubscriptionsObj.getJSONObject("subscriptions").getJSONArray("applications");
		String prodConsumerKey = "";
		String prodConsumerSecret = "";
		//get the relevant application details
		for (int x = 0; x < array.length(); x++) {
			JSONObject jsonObject = array.getJSONObject(x);
			if (CloudIntegrationConstants.APP_NAME.equals(jsonObject.getString("name"))) {
				prodConsumerKey = jsonObject.getString("prodConsumerKey");
				prodConsumerSecret = jsonObject.getString("prodConsumerSecret");
				break;
			}
		}
		//encode to the base64, consumerKey:consumerSecret
		prodConsumerKey = prodConsumerKey.concat(":");
		String authToken = prodConsumerKey.concat(prodConsumerSecret);
		byte[] bytesEncodedAuthToken = Base64.encodeBase64(authToken.getBytes());

		params.put("grant_type", "password");
		params.put("username", tenantAdminUserName);
		params.put("password", tenantAdminPassword);

		Map<String, String> paramHeaderMap = new HashMap<String, String>();
		paramHeaderMap.put("Authorization: Basic", new String(bytesEncodedAuthToken));
		paramHeaderMap.put("Content-Type", "application/x-www-form-urlencoded");

		String passwordGrantTypeUrl =
				apiMgrPassThroughUrl + CloudIntegrationConstants.API_TOKEN_GENERATION_URL_SFX;
		Map resultOfPasswordGrantTypeUrl = HttpHandler
				.doPostHttps(passwordGrantTypeUrl, params, authenticatorClient.getSessionCookie(),
				             paramHeaderMap);
		JSONObject responseFromPasswordGrantType = new JSONObject(
				resultOfPasswordGrantTypeUrl.get(CloudIntegrationConstants.RESPONSE).toString());
		String newAccessToken = (String) responseFromPasswordGrantType.get("access_token");
		params.clear();
		paramHeaderMap.clear();

		authenticatorClient.logout();
		super.cleanup();
		//login to the API Store
		authenticatorClient = new JaggeryAppAuthenticatorClient(apiMgtServerUrl,
		                                                        CloudIntegrationConstants.API_PUBLISHER_LOGIN_URL_SFX);
		loginStatus = authenticatorClient.login(tenantAdminUserName, tenantAdminPassword);

		//invoke the Api with the generated access token
		params.put("PhoneNumber", "180067832");
		params.put("LicenseKey", "0");
		String tenantDomain = CloudIntegrationTestUtils
				.getPropertyValue(CloudIntegrationConstants.TENANT_ADMIN_DOMAIN);

		String invokeApi = apiMgrPassThroughUrl + "/t/" + tenantDomain + "/" +
		                   CloudIntegrationConstants.API_CONTEXT + "/" +
		                   CloudIntegrationConstants.API_VERSION + "/CheckPhoneNumber";
		String authHeaderString = "Bearer ".concat(newAccessToken);

		Map<String, String> headerMap = new HashMap<String, String>();
		headerMap.put("Authorization:", authHeaderString);

		Map resultOfInvokeApi = HttpHandler
				.doPostHttps(invokeApi, params, authenticatorClient.getSessionCookie(), headerMap);
		if (resultOfInvokeApi == null) {
			log.error("Error occurred while generating Application Keys ");
		}

		authenticatorClient.logout();
		super.cleanup();
	}

}
