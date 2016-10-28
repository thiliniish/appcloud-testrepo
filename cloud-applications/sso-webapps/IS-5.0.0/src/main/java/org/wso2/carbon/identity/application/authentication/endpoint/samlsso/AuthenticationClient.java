/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.endpoint.samlsso;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;

import java.rmi.RemoteException;

/**
 * Authentication of the user is handled in this class
 */
public class AuthenticationClient {

    private static final Log log = LogFactory.getLog(AuthenticationClient.class);
    private AuthenticationAdminStub authenticationAdminStub;

    /**
     * Initializes authenticator client
     * @param hostName Service host
     */
    public AuthenticationClient(String hostName) {
        String serviceName = "AuthenticationAdmin";
        String endPoint = hostName + "services/" + serviceName;

        try {
            authenticationAdminStub = new AuthenticationAdminStub(endPoint);
        } catch (AxisFault axisFault) {
            System.out.println("authenticationAdminStub initialization fails: " + axisFault.getMessage());
        }
    }

    /**
     * Authenticates an user into a service
     * @param userName User name
     * @param password Password
     * @param host Service host
     * @return Authorized session cookie
     */
    public boolean login(String userName, Object password, String host){
        Boolean loginStatus = false;
        long timeout = 2 * 60 * 1000; // Setting the time out to Two minutes
        authenticationAdminStub._getServiceClient().getOptions().setTimeOutInMilliSeconds(timeout);
        try {
            loginStatus = authenticationAdminStub.login(userName, (String) password, hostWithoutHTTPSPort(host));
        } catch (RemoteException e) {
            log.error(e.getMessage());
        } catch (LoginAuthenticationExceptionException e) {
            log.error(e.getMessage());
        }
        return loginStatus;
    }

    /**
     * Returns the host address after removing port.
     * @param hostName Service host with port
     * @return Service host without port
     */
    private String hostWithoutHTTPSPort(String hostName){
        return hostName.contains(":") ? hostName.split(":")[0] : hostName;
    }
}
