/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.cloud.users.util;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.cloud.users.common.CloudConstants;
import org.wso2.carbon.cloud.users.beans.UserInfoBean;
import org.wso2.carbon.cloud.users.service.UserManagementException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class conatining utility methods for User Management
 */
public class UserMgtUtil {
    public static String emailClaimUri = "http://wso2.org/claims/emailaddress";
    public static String firstNameClaimUri = "http://wso2.org/claims/givenname";
    public static String lastNameClaimUri = "http://wso2.org/claims/lastname";
    public static String adminUserName;
    public static String everyOneRoleName;
    private static RealmService realmService;
    private static RegistryService registryService;
    private static ConfigurationContextService configurationContextService;

    public static RealmService getRealmService() {
        return realmService;
    }

    public static synchronized void setRealmService(RealmService realmSer) {
        realmService = realmSer;
        adminUserName = getRealmService().getBootstrapRealmConfiguration().getAdminUserName();
        everyOneRoleName = getRealmService().getBootstrapRealmConfiguration().getEveryOneRoleName();
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static void setRegistryService(RegistryService registryService) {
        UserMgtUtil.registryService = registryService;
    }

    public static ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public static void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        UserMgtUtil.configurationContextService = configurationContextService;
    }

    public static UserInfoBean getUserInfoBean(String userName, int tenantId) throws UserManagementException {
        try {
            UserRealm realm = UserMgtUtil.getRealmService().getTenantUserRealm(tenantId);
            String[] claims = { emailClaimUri, firstNameClaimUri, lastNameClaimUri };
            UserStoreManager userStoreManager = realm.getUserStoreManager();
            if (userStoreManager.isExistingUser(userName)) {
                java.util.Map<String, String> userClaims = userStoreManager.getUserClaimValues(userName, claims, null);

                String firstName = userClaims.get(firstNameClaimUri);
                String lastName = userClaims.get(lastNameClaimUri);
                String email = userClaims.get(emailClaimUri);
                StringBuilder displayNameBuilder = new StringBuilder();

                // Display name is constructed by concatenating first name and
                // the
                // last name of the user.
                if (StringUtils.isNotEmpty(firstName)) {
                    displayNameBuilder.append(firstName);
                }

                if (StringUtils.isNotEmpty(lastName)) {
                    displayNameBuilder.append(' ').append(lastName);
                }

                return new UserInfoBean(userName, firstName, lastName, email, displayNameBuilder.toString(),
                                        filterDefaultUserRoles(userStoreManager.getRoleListOfUser(userName)));
            } else {
                String msg = "No user found with the name " + userName;
                throw new UserManagementException(msg);
            }

            /* return new UserInfoBean(userName, firstName, lastName, email); */
        } catch (UserStoreException e) {
            String msg = "Error while getting info for user " + userName;
            throw new UserManagementException(msg, e);
        }

    }

    /**
     * Filter out default role list,appRole,everyone role from given role array
     *
     * @param roleListOfUser - given role array
     * @return - filtered array of roles
     * @throws UserManagementException
     */
    public static String[] filterDefaultUserRoles(String[] roleListOfUser) throws UserManagementException {
        List<String> roleList = new ArrayList<String>(Arrays.asList(roleListOfUser));
        ArrayList<String> roles = new ArrayList<String>();
        for (String role : roleList) {
            // filter everyone role and appRoles
            if ((!everyOneRoleName.equals(role)) && (!isAppRole(role))) {
                roles.add(role);
            }
        }
        return roles.toArray(new String[roles.size()]);
    }

    public static boolean isAppRole(String role) {
        return role.startsWith(CloudConstants.APP_ROLE_PREFIX);
    }

    /**
     * Removes admin and everyone role from the set of roles
     *
     * @param roles
     * @return
     */
    public static String[] removeEveryoneRoles(String[] roles) {
        String everyOneRoleName = getRealmService().getBootstrapRealmConfiguration().getEveryOneRoleName();
        return (String[]) ArrayUtils.removeElement(roles, everyOneRoleName);
    }

}
