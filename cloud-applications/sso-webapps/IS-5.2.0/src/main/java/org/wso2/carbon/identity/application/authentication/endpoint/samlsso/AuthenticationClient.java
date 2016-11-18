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
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

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
    public boolean login(String userName, Object password, String host, String tenantDomain){
        Boolean loginStatus = false;
        long timeout = 2 * 60 * 1000; // Setting the time out to Two minutes
        authenticationAdminStub._getServiceClient().getOptions().setTimeOutInMilliSeconds(timeout);
        UserStoreManager secondaryUserStoreManager = null;
        try {
            if (tenantDomain != null) {
                secondaryUserStoreManager = getSecondaryUserStoreManager(tenantDomain);
            }

            if (secondaryUserStoreManager != null) {
                loginStatus = secondaryUserStoreManager.authenticate(userName, password);
                if (!loginStatus) { //If authentication fails from secondary user store, authenticate as a normal user i.e from primary user store
                    loginStatus = authenticationAdminStub.login(userName + "@carbon.super", (String) password, hostWithoutHTTPSPort(host));
                }
            } else {
                loginStatus = authenticationAdminStub.login(userName, (String) password, hostWithoutHTTPSPort(host));
            }
        } catch (RemoteException e) {
            log.error(e.getMessage());
        } catch (LoginAuthenticationExceptionException e) {
            log.error(e.getMessage());
        } catch (UserStoreException e) {
            log.error(e);
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

    /**
     * Get secondary user store manager of provided tenant.
     * @param tenantDomain
     * @return
     */
    private UserStoreManager getSecondaryUserStoreManager(String tenantDomain) {
        UserStoreManager secondaryUserStoreManager = null;
        try {
            RealmService realmService = (RealmService) PrivilegedCarbonContext.getThreadLocalCarbonContext().getOSGiService
                    (Class.forName("org.wso2.carbon.user.core.service.RealmService"));
            int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
            UserRealm userRealm = realmService.getTenantUserRealm(tenantId);
            if (userRealm != null) {
                UserStoreManager userStoreManager = (UserStoreManager) userRealm.getUserStoreManager();
                secondaryUserStoreManager = userStoreManager.getSecondaryUserStoreManager();
            }
        } catch (ClassNotFoundException e) {
            log.error(e);
        } catch (UserStoreException e) {
            log.error(e);
        }

        return secondaryUserStoreManager;
    }
}
