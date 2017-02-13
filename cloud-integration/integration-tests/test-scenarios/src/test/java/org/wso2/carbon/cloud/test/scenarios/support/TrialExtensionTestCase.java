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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.cloud.integration.test.utils.CloudIntegrationConstants;
import org.wso2.carbon.cloud.integration.test.utils.CloudIntegrationTest;
import org.wso2.carbon.cloud.integration.test.utils.CloudIntegrationTestUtils;
import org.wso2.carbon.cloud.integration.test.utils.external.DbConnectionManager;
import org.wso2.carbon.cloud.integration.test.utils.external.HttpHandler;
import org.wso2.carbon.cloud.integration.test.utils.restclients.JaggeryAppAuthenticatorClient;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrialExtensionTestCase extends CloudIntegrationTest {

    private static final Log LOGGER = LogFactory.getLog(TrialExtensionTestCase.class);
    private String extensionRequestActionName;
    private String userEmail;
    private String emailSubject;
    private String emailBody;
    private String tenantDomain;
    private String apiCloudSubscriptionName;
    private String trialUserDefaultStatus;
    private String trialExtensionPeriod;
    private JaggeryAppAuthenticatorClient authenticatorClient;
    private boolean loginStatus;
    private final String supportUrl =
            cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_CONTACT_SUPPORT_URL_SFX;

    /**
     * This method will authenticate the tenant and initialize the input parameters.
     *
     * @throws Exception
     */
    @BeforeClass(alwaysRun = true)
    public void deployService() throws Exception {
        authenticatorClient = new JaggeryAppAuthenticatorClient(cloudMgtServerUrl);
        extensionRequestActionName =
                CloudIntegrationConstants.CLOUD_ACCOUNT_EXTENTION_REQUEST_ACTION_NAME;
        userEmail = CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.CLOUD_SUPPORT_REQUEST_USER_EMAIL);
        emailSubject = CloudIntegrationTestUtils
                .getPropertyValue(
                        CloudIntegrationConstants.CLOUD_ACCOUNT_EXTENTION_REQUEST_EMAIL_SUBJECT);
        emailBody = CloudIntegrationTestUtils
                .getPropertyValue(
                        CloudIntegrationConstants.CLOUD_ACCOUNT_EXTENTION_REQUEST_EMAIL_BODY);
        tenantDomain = CloudIntegrationTestUtils
                .getPropertyValue(
                        CloudIntegrationConstants.TENANT_ADMIN_DOMAIN);
        apiCloudSubscriptionName = CloudIntegrationTestUtils
                .getPropertyValue(
                        CloudIntegrationConstants.API_CLOUD_SUBSCRIPTION_NAME);
        trialUserDefaultStatus = CloudIntegrationTestUtils
                .getPropertyValue(
                        CloudIntegrationConstants.API_CLOUD_TRIAL_USER_DEFAULT_STATUS);
        trialExtensionPeriod = CloudIntegrationTestUtils
                .getPropertyValue(
                        CloudIntegrationConstants.API_CLOUD_TRIAL_EXTENSION_PERIOD);
        loginStatus = authenticatorClient.login(tenantAdminUserName, tenantAdminPassword);
    }

    /**
     * This is the method that will send the API Cloud trial extension support request to the cloud team.
     * This will also extend the trial of the user.
     *
     * @throws Exception
     */
    @Test(description = "Send the trial extension support request to the cloud team")
    public void sendTrialExtensionRequestToCloud() throws Exception {
        LOGGER.info(
                "Started the test case to send the API Cloud trial extension request to the cloud team");
        Assert.assertTrue(loginStatus, "Tenant login failed.");
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", extensionRequestActionName);
        params.put("from", userEmail);
        params.put("subject", emailSubject);
        params.put("body", emailBody);
        params.put("serviceId", apiCloudSubscriptionName);
        Map resultMap = HttpHandler
                .doPostHttps(supportUrl, params, authenticatorClient.getSessionCookie(), false);
        String extensionRequestResult =
                resultMap.get(CloudIntegrationConstants.RESPONSE).toString();
        Assert.assertEquals(extensionRequestResult, CloudIntegrationConstants.STRING_TRUE_RESPONSE,
                            "An error occurred while extending the API Cloud trial and sending the trial extension request to the Cloud Team.");
    }

    @AfterClass(alwaysRun = true) public void destroy()
            throws IOException, LogoutAuthenticationExceptionException, SQLException {
        authenticatorClient.logout();

        //This code block reverts the database entries made when requesting a trial extension.
        DbConnectionManager connectionManager =
                new DbConnectionManager(CloudIntegrationConstants.CLOUD_MGT_DATASOURCE);
        try {
            List<String> updateQueryParameters = new ArrayList<String>();
            List<String> deleteQueryParameters = new ArrayList<String>();

            //Deleting the entry from the BILLING_HISTORY table.
            deleteQueryParameters.add(tenantDomain);
            deleteQueryParameters.add(apiCloudSubscriptionName);
            connectionManager
                    .executeUpdate(CloudIntegrationConstants.DELETE_BILLING_STATUS_HISTORY_QUERY,
                                   deleteQueryParameters);

            //Updating the status back to an active trial user.
            DateFormat dateFormat =
                    new SimpleDateFormat(CloudIntegrationConstants.DATE_TIME_FORMAT);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            String endDate = dateFormat.format(cal.getTime());
            int dateDifference = Integer.parseInt(trialExtensionPeriod);
            cal.add(Calendar.DATE, -dateDifference);
            String startdate = dateFormat.format(cal.getTime());

            updateQueryParameters.add(trialUserDefaultStatus);
            updateQueryParameters.add(startdate);
            updateQueryParameters.add(endDate);
            updateQueryParameters.add(tenantDomain);
            updateQueryParameters.add(apiCloudSubscriptionName);
            connectionManager.executeUpdate(
                    CloudIntegrationConstants.REVERT_EXTENSION_REQUEST_QUERY,
                    updateQueryParameters);
        } catch (SQLException e) {
            String message =
                    "An error occurred while reverting the trial extension request for API Cloud " +
                    e.getMessage();
            LOGGER.error(message, e);
            throw new SQLException(message, e);
        } finally {
            connectionManager.closeConnection();
        }
        super.cleanup();
    }
}
