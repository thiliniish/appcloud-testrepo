/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
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

package org.jenkins.wso2.cloud;

import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationServiceException;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.userdetails.UserDetails;
import org.jenkins.wso2.appfactory.CarbonSecurityRealm;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class WSO2CloudCarbonSecurityRealm extends CarbonSecurityRealm {

    /**
     * Logger for debugging purposes.
     */
    private static final Logger LOGGER = Logger.getLogger(WSO2CloudCarbonSecurityRealm.class.getName());

    /*public static final String JENKINS_SERVER_ADMIN_USERNAME = "JenkinsServerAdminUsername";
    public static final String JENKINS_SERVER_ADMIN_PASSWORD = "JenkinsServerAdminPassword";

    @DataBoundConstructor
    public WSO2CloudCarbonSecurityRealm() {
    }

    @Extension
    public static DescriptorImpl install() {
        return new DescriptorImpl();
    }

    public static final class DescriptorImpl extends Descriptor<SecurityRealm> {

        public DescriptorImpl() {
            load();
        }

        public FormValidation doCheckAuthenticationServiceEPR(@QueryParameter final String authenticationServiceEPR) {
            return FormValidation.ok();
        }

        public FormValidation doCheckClientTrustStorePassword(@QueryParameter final String clientTrustStorePassword) {
            return FormValidation.ok();
        }

        public FormValidation doCheckAppfactorySystemUsername(@QueryParameter final String appfactorySystemUsername) {
            return FormValidation.ok();
        }

        public FormValidation doCheckAppfactorySystemUserPassword(@QueryParameter final String appfactorySystemUserPassword) {
            return FormValidation.ok();
        }

        @Override
        public String getDisplayName() {
            return Messages.DisplayName();
        }
    }
    */

    @Override
    protected UserDetails authenticate(String username, String password)
            throws AuthenticationException {

        /*if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("login request received for : " + username);
        }*/

        System.out.println("login request received for : " + username);
        UserDetails userDetails = null;

        if (isJenkinsSystemUser(username)) {

            System.out.println("Authenticating JenkinsSystemAdmin user");
            if (authenticateJenkinsSystemUser(password)) {
                userDetails = createUserDetails(username, password);// create user details for appfactory system user.
            } else {
                throw new BadCredentialsException(
                        "Invalid credentials supplied app factory system user, " +
                                "check app factory configurations.");
            }

        } else {

            System.out.println("Authenticating: " + username);

            CarbonContext context=CarbonContext.getThreadLocalCarbonContext();
            String tenantDomainFromUrl = context.getTenantDomain();
            String tenantDomainFromUsername = MultitenantUtils.getTenantDomain(username);

            String actualUsername = "";

            if(tenantDomainFromUrl != null && tenantDomainFromUrl.equalsIgnoreCase(tenantDomainFromUsername)){
                actualUsername = MultitenantUtils.getTenantAwareUsername(username);
            } else {
                actualUsername = username;
            }

            System.out.println("Actual username: " + actualUsername);
            actualUsername = actualUsername.replace("@", ".");
            System.out.println("Converted username: " + actualUsername);

            try {
                UserStoreManager userStoreManager=context.getUserRealm().getUserStoreManager();
                if (userStoreManager!=null && userStoreManager.authenticate(actualUsername,password)){
                    System.out.println("Creating user details for: " + actualUsername);
                    userDetails = createUserDetails(actualUsername, password);// create user details for tenant user.
                }else {
                    throw new BadCredentialsException("Invalid credentials supplied user name - " +
                            username + "Password : *****");
                }
            } catch (UserStoreException e) {
                throw new AuthenticationServiceException(e.getLocalizedMessage(), e);
            }
        }
        return userDetails;
    }

    /*private UserDetails createUserDetails(String username, String password) {
        GrantedAuthority[] authorities =
                new GrantedAuthority[]{SecurityRealm.AUTHENTICATED_AUTHORITY};

        return new CarbonUserDetails(username, password, authorities);
    }

    private boolean isJenkinsSystemUser(String userName) {
        String adminUsername = "";
        try {
            adminUsername = AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(JENKINS_SERVER_ADMIN_USERNAME);
        } catch (AppFactoryException e) {
            Log.error("Error reading jenkins admin username from configuration");
        }
        return adminUsername.equals(userName);
    }

    private boolean authenticateJenkinsSystemUser(String password) {
        String adminPassword = "";
        try {
            adminPassword = AppFactoryUtil.getAppfactoryConfiguration().getFirstProperty(JENKINS_SERVER_ADMIN_PASSWORD);
        } catch (AppFactoryException e) {
            Log.error("Error reading jenkins admin password from configuration");
        }
        return adminPassword.equals(password);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException,
            DataAccessException {
        throw new UsernameNotFoundException("loading users by name is not supported");

    }

    @Override
    public GroupDetails loadGroupByGroupname(String groupname) throws UsernameNotFoundException,
            DataAccessException {
        return new CarbonGroupDetails(groupname);
    }

    class CarbonGroupDetails extends GroupDetails {
        private String name;

        CarbonGroupDetails(String n) {
            this.name = n;
        }

        @Override
        public String getName() {
            return name;
        }

    }
    */

}
