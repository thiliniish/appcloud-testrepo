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
package org.wso2.carbon.cloud.integration.test.utils.clients.service;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Stub;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.integration.test.utils.CloudIntegrationConstants;
import org.wso2.carbon.cloud.integration.test.utils.CloudIntegrationTestUtils;
import org.wso2.carbon.cloud.integration.test.utils.clients.authentication.CarbonAuthenticatorClient;
import org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceIdentityMgtServiceExceptionException;
import org.wso2.carbon.identity.mgt.stub.UserInformationRecoveryServiceStub;
import org.wso2.carbon.identity.mgt.stub.beans.VerificationBean;

import java.rmi.RemoteException;

/**
 * Service client for UserInformationRecoveryService.
 *
 * @See <a href="https://docs.wso2.com/display/IS500/Recover+with+Notification">Recover with Notification</a>
 */
public class UserInformationRecoveryServiceClient {
    UserInformationRecoveryServiceStub stub;
    private static final Log log = LogFactory.getLog(UserInformationRecoveryServiceClient.class);

    public UserInformationRecoveryServiceClient() throws Exception {
        init();
    }

    /**
     * Initialize the Client. UserInformationRecoveryServiceStub is authenticated using Super Admin (A session cookie
     * is obtained by login in using CarbonAuthenticatorClient)
     *
     * @throws Exception
     */
    private void init() throws Exception {
        try {
            String identityServerUrl = CloudIntegrationTestUtils
                    .getPropertyValue(CloudIntegrationConstants.IDENTITY_SERVER_URL);
            String endpoint = identityServerUrl + "/services/UserInformationRecoveryService";
            stub = new UserInformationRecoveryServiceStub(endpoint);
            CarbonAuthenticatorClient authenticatorClient = new CarbonAuthenticatorClient(identityServerUrl);
            String adminUserName = CloudIntegrationTestUtils
                    .getPropertyValue(CloudIntegrationConstants.SUPER_ADMIN_USER_NAME);
            String adminPassword = CloudIntegrationTestUtils
                    .getPropertyValue(CloudIntegrationConstants.SUPER_ADMIN_PASSWORD);
            String sessionCookie = authenticatorClient.login(adminUserName, adminPassword, "localhost");
            authenticateStub(sessionCookie, stub);
        } catch (Exception e) {
            String msg = "UserInformationRecoveryServiceClient initialization failed.";
            log.error(msg, e);
            throw new Exception(msg, e);
        }

    }

    /**
     * Authenticates stubs
     *
     * @param sessionCookie Authorized session cookie
     * @param stub          Axis2 service stub which needs to be authenticated
     */
    private void authenticateStub(String sessionCookie, Stub stub) {
        long soTimeout = 5 * 60 * 1000; // Three minutes

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setTimeOutInMilliSeconds(soTimeout);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
        if (log.isDebugEnabled()) {
            log.debug("AuthenticateStub : Stub created with session " + sessionCookie);
        }
    }

    /**
     * Verify the given user for initiating password reset
     *
     * @param userName userName
     * @return VerificationBean - contains the NotificationDataDTO which has verification key used for
     * sendRecoveryNotification
     * @throws Exception
     */
    public VerificationBean verifyUser(String userName) throws Exception {
        try {
            return stub.verifyUser(userName, null);
        } catch (Exception e) {
            String msg = "Error Occurred while verifying user.";
            log.error(msg, e);
            throw new Exception(msg, e);
        }
    }

    /**
     * Returns user information, and confirmation code required for sending email
     *
     * @param userName         userName
     * @param key              verification key from verifyUser method
     * @param notificationType email
     * @return contains the NotificationDataDTO which has confirmation code and user information for sending the email
     * @throws Exception
     */
    public VerificationBean sendRecoveryNotification(String userName, String key, String notificationType)
            throws Exception {
        try {
            return stub.sendRecoveryNotification(userName, key, notificationType);
        } catch (Exception e) {
            String msg = "Error Occurred while sendRecoveryNotification for user.";
            log.error(msg, e);
            throw new Exception(msg, e);
        }
    }

}
