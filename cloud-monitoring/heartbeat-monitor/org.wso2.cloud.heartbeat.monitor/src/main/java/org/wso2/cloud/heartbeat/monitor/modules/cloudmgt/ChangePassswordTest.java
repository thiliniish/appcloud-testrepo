/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.cloud.heartbeat.monitor.modules.cloudmgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.wso2.cloud.heartbeat.monitor.core.clients.authentication.JaggeryAppAuthenticatorClient;
import org.wso2.cloud.heartbeat.monitor.core.clients.https.HttpsJaggeryClient;
import org.wso2.cloud.heartbeat.monitor.core.clients.utils.JagApiProperties;
import org.wso2.cloud.heartbeat.monitor.core.notification.Mailer;
import org.wso2.cloud.heartbeat.monitor.core.notification.SMSSender;
import org.wso2.cloud.heartbeat.monitor.modules.common.exceptions.FalseReturnException;
import org.wso2.cloud.heartbeat.monitor.modules.common.exceptions.JaggeryAppLoginException;
import org.wso2.cloud.heartbeat.monitor.utils.DbConnectionManager;
import org.wso2.cloud.heartbeat.monitor.utils.TestInfo;
import org.wso2.cloud.heartbeat.monitor.utils.TestStateHandler;
import org.wso2.cloud.heartbeat.monitor.utils.fileutils.CaseConverter;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class ChangePassswordTest implements Job {

	private static final Log log = LogFactory.getLog(ChangePassswordTest.class);

	private final String TEST_NAME = "ChangePassswordTest";

	private String hostName;
	private String testUser;
	private String testUserPassword;
	private int deploymentWaitTime;
	private String serviceName;
	private TestInfo testInfo;
	private TestStateHandler testStateHandler;
	private String severity="2";

	private String completeTestName;

	private boolean errorsReported;
	private int requestCount = 0;

	private JaggeryAppAuthenticatorClient authenticatorClient;
	private boolean loginStatus = false;

	private String testUserTempPassword;

	/**
	 * @param jobExecutionContext
	 *            "managementHostName", "hostName" ,"testUser",
	 *            "testUserPassword" "httpPort"
	 *            "deploymentWaitTime" params passed via JobDataMap.
	 * @throws org.quartz.JobExecutionException
	 */
	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		setCompleteTestName(CaseConverter.splitCamelCase(serviceName) +
		                    " - Change Passsword Test : ");
		initWebAppTest();
		if (!errorsReported) {
			changePassword();
		}
		if (!errorsReported) {
			resetPasssword();
		}
	}

	/**
	 * Initializes Web application service test
	 */
	private void initWebAppTest() {

		errorsReported = false;
		try {
			hostName = "https://" + hostName;

			authenticatorClient = new JaggeryAppAuthenticatorClient(hostName, "cloudmgt");
			loginStatus = authenticatorClient.login(testUser, testUserPassword);
			testStateHandler = TestStateHandler.getInstance();
			testInfo = new TestInfo(serviceName, TEST_NAME, hostName, severity);

			if (!loginStatus) {
				throw new JaggeryAppLoginException("Login failure to cloudmgt jaggery app. Returned false as login status.");
			}
		} catch (JaggeryAppLoginException je) {
			countNoOfRequests("JaggeryAppLoginException", je, "initWebAppTest");
		} catch (Exception ee) {
			countNoOfRequests("ExecutionException", ee, "initWebAppTest");
		}
		requestCount = 0;
	}

	/**
	 * Change Tenant Password
	 */
	private void changePassword() {

		try {
			if (loginStatus) {
				String url = hostName + JagApiProperties.CHANGE_PASSWD_CLOUDMGT_TENANT_URL_SFX;
				Map<String, String> params = new HashMap<String, String>();
				params.put("action", "changePassword");
				params.put("oldPassword", testUserPassword);
				params.put("password", testUserTempPassword);
				String result = HttpsJaggeryClient.httpPost(url, params);
				if (result.equals("false")) {
					throw new FalseReturnException("Change Password returned status as false");
				} else if (result.equals("true")) {
					log.info("Change Password Success");
				}
			} else {
				throw new JaggeryAppLoginException("Login failure to cloudmgt jaggery app. Returned false as login status");
			}
		} catch (JaggeryAppLoginException je) {
			countNoOfRequests("JaggeryAppLoginException", je, "changePassword");
		} catch (FalseReturnException fe) {
			countNoOfRequests("FalseReturnException", fe, "changePassword");
		} catch (Exception ee) {
			countNoOfRequests("ExecutionException", ee, "changePassword");
		}
		requestCount = 0;
	}

	/**
	 * Reset the tenant password
	 */
	private void resetPasssword() {

		try {
			if (loginStatus) {
				String url = hostName + JagApiProperties.CHANGE_PASSWD_CLOUDMGT_TENANT_URL_SFX;
				Map<String, String> params = new HashMap<String, String>();
				params.put("action", "changePassword");
				params.put("oldPassword", testUserTempPassword);
				params.put("password", testUserPassword);
				String result = HttpsJaggeryClient.httpPost(url, params);

				if (result.equals("false")) {
					throw new FalseReturnException("Reset Password returned status as false. New Password for user:" 
													+ testUser + " is \'" + testUserTempPassword + "\'");
				} else if (result.equals("true")) {
					log.info("Reset Password Success");
					testStateHandler.onSuccess(testInfo);
				}
			} else {
				throw new JaggeryAppLoginException("Login failure to cloudmgt jaggery app. Returned false as a login status. New Password for user:" 
				                                 	+ testUser + " is \'" + testUserTempPassword + "\'");
			}
		} catch (FalseReturnException fe) {
			countNoOfRequests("FalseReturnException", fe, "resetPasssword");
		} catch (JaggeryAppLoginException je) {
			countNoOfRequests("JaggeryAppLoginException", je, "resetPasssword");
		} catch (Exception ee) {
			countNoOfRequests("ExecutionException", ee, "changePassword");
		}
		requestCount = 0;
	}

	private void countNoOfRequests(String type, Object obj, String method) {

		requestCount++;
		log.info("Retrying :" + method + " count: " + requestCount + " type: " + type);
		if (requestCount == 3) {
			System.out.println("3 times retried, handling error" + method);
			handleError(type, obj, method);
			requestCount = 0;
		} else {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// Exception ignored
			}

			if (type.equals("JaggeryAppLoginException")) {

				loginStatus = authenticatorClient.login(testUser, testUserPassword);
			} else if (type.equals("FalseReturnException")) {

				loginStatus = authenticatorClient.login(testUser, testUserPassword);
			}
			if (method.equals("initWebAppTest")) {
				initWebAppTest();
			} else if (method.equals("changePassword")) {
				changePassword();
			} else if (method.equals("resetPasssword")) {
				resetPasssword();
			}
		}
	}

	private void handleError(String type, Object obj, String method) {
		Exception exception = null;
		if (type.equals("JaggeryAppLoginException")) {
			exception = (JaggeryAppLoginException) obj;

		} else if (type.equals("FalseReturnException")) {
			exception = (FalseReturnException) obj;
		} else if (type.equals("ExecutionException")) {
			exception = (Exception) obj;
		}
		if (method.equals("initWebAppTest")) {
			log.error(CaseConverter.splitCamelCase(serviceName) + " - Initiate Test: " + hostName,
			          exception);
		} else if (method.equals("changePassword")) {
			log.error(CaseConverter.splitCamelCase(serviceName) + " - Change Password: " + hostName,
			          exception);
		} else if (method.equals("resetPasssword")) {
			log.error(CaseConverter.splitCamelCase(serviceName) + " - Reset Password: " + hostName,
			          exception);
		}
		testStateHandler.onFailure(testInfo, exception.getMessage());
	}

	/**
	 * Sets service host
	 * 
	 * @param hostName
	 *            Service host
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	/**
	 * Sets Tenant user name
	 *
	 * @param testUser
	 *            Tenant user name
	 */
	public void setTestUser(String testUser) {
		this.testUser = testUser;
	}

	/**
	 * Sets Tenant user password
	 *
	 * @param testUserPassword
	 *            Tenant user password
	 */
	public void setTestUserPassword(String testUserPassword) {
		this.testUserPassword = testUserPassword;
	}

	/**
	 * Sets temporary new password to tenant
	 * 
	 * @param testUserTempPassword
	 *            the testUserTempPassword to set
	 */
	public void setTestUserTempPassword(String testUserTempPassword) {
		this.testUserTempPassword = testUserTempPassword;
	}

	/**
	 * Sets deployment waiting time
	 * 
	 * @param deploymentWaitTime
	 *            Deployment wait time
	 */
	public void setDeploymentWaitTime(String deploymentWaitTime) {
		this.deploymentWaitTime = Integer.parseInt(deploymentWaitTime.split("s")[0].replace(" ", "")) * 1000;
	}

	/**
	 * Sets Service name
	 * 
	 * @param serviceName
	 *            Service name
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	/**
	 * Sets Display Service name
	 * 
	 * @param completeTestName
	 *            Service name
	 */
	public void setCompleteTestName(String completeTestName) {
		this.completeTestName = completeTestName;
	}

	/**
	 * set severity
	 * @param severity severity value
	 */
	public void setSeverity(String severity){
		this.severity = severity;
	}
}