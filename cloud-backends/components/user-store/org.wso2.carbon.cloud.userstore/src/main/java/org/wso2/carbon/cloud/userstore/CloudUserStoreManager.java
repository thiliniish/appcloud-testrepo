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

package org.wso2.carbon.cloud.userstore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.ldap.ReadWriteLDAPUserStoreManager;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Custom LDAP based user store implementation for cloud.
 */
public class CloudUserStoreManager extends ReadWriteLDAPUserStoreManager {

    private static Log log = LogFactory.getLog(CloudUserStoreManager.class);

    public CloudUserStoreManager() {
    }

    public CloudUserStoreManager(RealmConfiguration realmConfig, Map<String, Object> properties,
            ClaimManager claimManager, ProfileConfigurationManager profileManager, UserRealm realm, Integer tenantId)
            throws UserStoreException {
        super(realmConfig, properties, claimManager, profileManager, realm, tenantId);
    }

    public CloudUserStoreManager(RealmConfiguration realmConfig, ClaimManager claimManager,
            ProfileConfigurationManager profileManager) throws UserStoreException {
        super(realmConfig, claimManager, profileManager);
    }

    @Override public String[] doListUsers(String filter, int maxItemLimit) throws UserStoreException {
        // this method will list the users only in the current tenant.
        // if the users in at least one of the roles, that users are included in
        // results

        return searchUsersInSP(filter, maxItemLimit);
    }

    /**
     * Search users in a tenant base on filter and limit
     *
     * @param searchFilter - Use "*", to search for all users
     * @param maxItemLimit - use -1 as the max limit for unlimited search
     * @return list of users
     * @throws UserStoreException
     */
    private String[] searchUsersInSP(String searchFilter, int maxItemLimit) throws UserStoreException {

        Set<String> users = new HashSet<String>();
        Set<String> resultedUsers = new HashSet<String>();
        try {
            UserStoreManager userStoreManager = userRealm.getUserStoreManager();
            String[] roles = userStoreManager.getRoleNames();
            // get users in all roles
            for (String role : roles) {
                // we skip everyone role and anonymous role. with the
                // centralized user model, all users are in everyone role
                if ("everyone".equals(role) || "Internal/everyone".equals(role)
                        || CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME.equals(role)) {
                    continue;
                }
                String[] usersInRole = userStoreManager.getUserListOfRole(role);
                users.addAll(Arrays.asList(usersInRole));
            }

            // change filter to matching with regular expression any number of
            // characters.
            if (searchFilter.contains("*")) {
                searchFilter = searchFilter.replace("*", ".*");
            }

            // unlimited user search
            if (maxItemLimit == -1) {
                for (String user : users) {
                    // if all users are matched(*)
                    if ("*".equals(searchFilter)) {
                        resultedUsers.addAll(users);
                        break;
                    }

                    // add users only if they matching with search filter
                    if (user.matches(searchFilter)) {
                        resultedUsers.add(user);
                    }
                }
            } else {
                int currentUserCount = 0;
                for (String user : users) {
                    // add users up to the search count
                    if (!(currentUserCount < maxItemLimit)) {
                        break;
                    }
                    // add users only if they matching with search filter
                    if (user.matches(searchFilter)) {
                        resultedUsers.add(user);
                        currentUserCount++;
                    }
                }
            }

            return resultedUsers.toArray(new String[resultedUsers.size()]);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Unable to list users in solution provider roles.";
            log.error(msg, e);
            throw new UserStoreException(msg, e);
        }
    }

}
