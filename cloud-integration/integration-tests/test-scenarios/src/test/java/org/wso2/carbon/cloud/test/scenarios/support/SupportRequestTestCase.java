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

package org.wso2.carbon.cloud.test.scenarios.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.cloud.integration.test.utils.CloudIntegrationConstants;
import org.wso2.carbon.cloud.integration.test.utils.CloudIntegrationTest;
import org.wso2.carbon.cloud.integration.test.utils.CloudIntegrationTestUtils;
import org.wso2.carbon.cloud.integration.test.utils.external.HttpHandler;
import org.wso2.carbon.cloud.integration.test.utils.restclients.JaggeryAppAuthenticatorClient;

import java.io.IOException;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;

/**
 * This class runs the test cases to test if user's support requests are sent as required to the cloud team.
 * Also it checks if the jira creation is enabled/disabled prior to running the test case in
 * order to avoid the creation of dummy jiras.
 */
public class SupportRequestTestCase extends CloudIntegrationTest {

    private static final Log log = LogFactory.getLog(SupportRequestTestCase.class);
    private String supportRequestActionName;
    private String userEmail;
    private String emailSubject;
    private String emailBody;
    private String JiraCreationActionName;
    private String jiraCreationStatus;
    private JaggeryAppAuthenticatorClient authenticatorClient;
    private boolean loginStatus;
    private final String supportUrl = cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_CONTACT_SUPPORT_URL_SFX;

    /**
     * This method will authenticate the tenant and initialize the input parameters.
     *
     * @throws Exception
     */
    @BeforeClass(alwaysRun = true)
    public void deployService() throws Exception {
        authenticatorClient = new JaggeryAppAuthenticatorClient(cloudMgtServerUrl);
        supportRequestActionName = CloudIntegrationConstants.SUPPORT_REQUEST_ACTION_NAME;
        userEmail = CloudIntegrationTestUtils.getPropertyValue(
                CloudIntegrationConstants.CLOUD_SUPPORT_REQUEST_USER_EMAIL);
        emailSubject = CloudIntegrationTestUtils.getPropertyValue(
                CloudIntegrationConstants.CLOUD_SUPPORT_REQUEST_EMAIL_SUBJECT);
        emailBody = CloudIntegrationTestUtils.getPropertyValue(
                CloudIntegrationConstants.CLOUD_SUPPORT_REQUEST_EMAIL_BODY);
        JiraCreationActionName = CloudIntegrationConstants.JIRA_CREATION_ENABLED_ACTION_NAME;
        jiraCreationStatus = CloudIntegrationTestUtils.getPropertyValue(
                CloudIntegrationConstants.CLOUD_SUPPORT_REQUEST_JIRA_CREATION_STATUS);
        loginStatus = authenticatorClient.login(tenantAdminUserName, tenantAdminPassword);
    }

    /**
     * This is the method that will send the support request email to the cloud team.
     *
     * @throws Exception
     */
    @Test(description = "Send the support request to the cloud team", dependsOnMethods = {
            "checkJiraCreationEnabled" }) public void sendSupportRequestToCloud() throws Exception {
        log.info("Started the test case to check the function of sending the support request to the cloud team");
        Assert.assertTrue(loginStatus, CloudIntegrationConstants.LOGIN_ERROR_MESSAGE);
        Map<String, String> params = new HashMap<>();
        params.put(CloudIntegrationConstants.PARAMETER_KEY_ACTION, supportRequestActionName);
        params.put("from", userEmail);
        params.put("subject", emailSubject);
        params.put("body", emailBody);
        Map resultMap = HttpHandler.doPostHttps(supportUrl, params, authenticatorClient.getSessionCookie(), false);
        JSONObject supportRequestResult = new JSONObject(resultMap.get(CloudIntegrationConstants.RESPONSE).toString());
        if (supportRequestResult.length() != 0) {
            String createJiraTicketResult = supportRequestResult.getString("createJiraTicketResult");
            String sendSupportEmailResult = supportRequestResult.getString("sendSupportEmailResult");
            Assert.assertTrue("success".equals(createJiraTicketResult) || "disabled".equals(createJiraTicketResult),
                              "Error while creating jira");
            Assert.assertEquals(sendSupportEmailResult, "true", "Error while sending support email");
        } else {
            Assert.fail(
                    "The result returned from the execution of the " + supportRequestActionName + "method was empty");
        }
    }
    /**
     * This method will check if the Jira creation is enabled or disabled as required. In order to avoid the creation
     * of sample JIRAs.
     *
     * @throws Exception
     */
    @Test(description = "Checks if the Jira creation is enabled/disabled as required")
    public void checkJiraCreationEnabled() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Making sure Jira creation is enabled/disabled as required.");
        }
        Map<String, String> params = new HashMap<>();
        params.put(CloudIntegrationConstants.PARAMETER_KEY_ACTION, JiraCreationActionName);
        Map resultMap = HttpHandler.doPostHttps(supportUrl, params, authenticatorClient.getSessionCookie(), false);
        Assert.assertEquals(resultMap.get(CloudIntegrationConstants.RESPONSE), jiraCreationStatus,
                            "Please check if the Jira creation is enabled/disabled as required");
    }

    @AfterClass(alwaysRun = true) public void destroy() throws IOException, LogoutAuthenticationExceptionException {
        authenticatorClient.logout();
        super.cleanup();
    }

}
