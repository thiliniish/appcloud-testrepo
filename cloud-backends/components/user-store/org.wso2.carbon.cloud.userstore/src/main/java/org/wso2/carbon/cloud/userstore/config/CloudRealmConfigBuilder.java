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
package org.wso2.carbon.cloud.userstore.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.TenantMgtConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.config.multitenancy.CommonLDAPRealmConfigBuilder;
import org.wso2.carbon.user.core.ldap.LDAPConstants;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.Map;

/**
 * This is to create tenant specific realm configuration when
 * org.wso2.carbon.user.core.tenant.CommonHybridLDAPTenantManager is used as the tenant manager
 * which supports any external ldap server.
 */

public class CloudRealmConfigBuilder extends CommonLDAPRealmConfigBuilder {

    private static Log logger = LogFactory.getLog(CloudRealmConfigBuilder.class);

    public RealmConfiguration getRealmConfigForTenantToPersist(RealmConfiguration bootStrapConfig,
            TenantMgtConfiguration tenantMgtConfig,
            Tenant tenantInfo, int tenantId)
            throws UserStoreException {

        try {
            RealmConfiguration ldapRealmConfig = bootStrapConfig.cloneRealmConfigurationWithoutSecondary();
            //remove non-tenant specific info from tenant-specific user-mgt.xml before persisting.
            removePropertiesFromTenantRealmConfig(ldapRealmConfig);

            ldapRealmConfig.setAdminPassword(UserCoreUtil.getDummyPassword());
            ldapRealmConfig.setAdminUserName(tenantInfo.getAdminName());
            ldapRealmConfig.setTenantId(tenantId);

            String everyoneRoleName = ldapRealmConfig.getEveryOneRoleName();
            ldapRealmConfig.setEveryOneRoleName(UserCoreUtil.removeDomainFromName(everyoneRoleName));

            Map<String, String> userStoreProperties = ldapRealmConfig.getUserStoreProperties();

            String partitionDN = tenantMgtConfig.getTenantStoreProperties().get(
                    UserCoreConstants.TenantMgtConfig.PROPERTY_ROOT_PARTITION);
            String organizationName = tenantInfo.getDomain();
            //eg: o=cse.rog
            String organizationRDN = tenantMgtConfig.getTenantStoreProperties().get(
                    UserCoreConstants.TenantMgtConfig.PROPERTY_ORGANIZATIONAL_ATTRIBUTE) + "=" +
                    organizationName;
            //eg: ou=users
            String orgSubContextAttribute = tenantMgtConfig.getTenantStoreProperties().get(
                    UserCoreConstants.TenantMgtConfig.PROPERTY_ORG_SUB_CONTEXT_ATTRIBUTE);
            //************ Cloud Specific Implementation ******************
            String userContextRDN = orgSubContextAttribute + "=" +
                    LDAPConstants.USER_CONTEXT_NAME;
            //            String userContextRDN = orgSubContextAttribute + "=user" ;
            //eg: ou=users,dc=cloud, dc=com
            String userSearchBase = userContextRDN + "," + partitionDN;
            //*************************************************************
            //replace the tenant specific user search base.
            userStoreProperties.put(LDAPConstants.USER_SEARCH_BASE, userSearchBase);


            //if UserDNPattern is mentioned, replace it to align with tenant's user store.
            if (bootStrapConfig.getUserStoreProperties().containsKey(LDAPConstants.USER_DN_PATTERN)) {
                //get userDN pattern from super tenant realm config
                String userDNPattern = bootStrapConfig.getUserStoreProperties().get(
                        LDAPConstants.USER_DN_PATTERN);
                //obtain the identifier - eg: uid={0}
                String userIdentifier = userDNPattern.split(",")[0];
                //build tenant specific one - eg:uid={0},ou=Users,ou=cse.org,dc=wso2,dc=org
                String tenantUserDNPattern = userIdentifier + "," + userSearchBase;
                userStoreProperties.put(LDAPConstants.USER_DN_PATTERN, tenantUserDNPattern);
            } else {
                userStoreProperties.put(LDAPConstants.USER_ENTRY_OBJECT_CLASS,
                        bootStrapConfig.getUserStoreProperties().get(LDAPConstants.USER_ENTRY_OBJECT_CLASS));
                userStoreProperties.put(LDAPConstants.USER_NAME_LIST_FILTER,
                        bootStrapConfig.getUserStoreProperties().get(LDAPConstants.USER_ENTRY_OBJECT_CLASS));
                userStoreProperties.put(LDAPConstants.USER_NAME_ATTRIBUTE,
                        bootStrapConfig.getUserStoreProperties().get(LDAPConstants.USER_NAME_ATTRIBUTE));
                userStoreProperties.put(LDAPConstants.USER_NAME_SEARCH_FILTER,
                        bootStrapConfig.getUserStoreProperties().get(LDAPConstants.USER_NAME_SEARCH_FILTER));
            }

            //if read ldap group is enabled, set the tenant specific group search base
            if (("true").equals(bootStrapConfig.
                    getUserStoreProperty(UserCoreConstants.RealmConfig.READ_GROUPS_ENABLED))) {
                //eg: ou=groups
                String groupContextRDN = orgSubContextAttribute + "=" +
                        LDAPConstants.GROUP_CONTEXT_NAME;
                //eg: ou=users,o=cse.org, dc=cloud, dc=com
                String groupSearchBase = groupContextRDN + "," + organizationRDN + "," + partitionDN;

                userStoreProperties.put(LDAPConstants.GROUP_SEARCH_BASE, groupSearchBase);

                //if RoleDNPattern is mentioned, replace it to align with tenant's user store.
                if (bootStrapConfig.getUserStoreProperties().containsKey(LDAPConstants.ROLE_DN_PATTERN)) {
                    //get userDN pattern from super tenant realm config
                    String roleDNPattern = bootStrapConfig.getUserStoreProperties().
                            get(LDAPConstants.ROLE_DN_PATTERN);
                    //obtain the identifier - eg: uid={0}
                    String roleIdentifier = roleDNPattern.split(",")[0];
                    //build tenant specific one - eg:uid={0},ou=Users,ou=cse.org,dc=wso2,dc=org
                    String tenantRoleDNPattern = roleIdentifier + "," + groupSearchBase;
                    userStoreProperties.put(LDAPConstants.ROLE_DN_PATTERN, tenantRoleDNPattern);

                } else {
                    userStoreProperties.put(LDAPConstants.GROUP_ENTRY_OBJECT_CLASS,
                            bootStrapConfig.getUserStoreProperties().get(LDAPConstants.GROUP_ENTRY_OBJECT_CLASS));
                    userStoreProperties.put(LDAPConstants.GROUP_NAME_LIST_FILTER,
                            bootStrapConfig.getUserStoreProperties().get(LDAPConstants.GROUP_ENTRY_OBJECT_CLASS));
                    userStoreProperties.put(LDAPConstants.GROUP_NAME_ATTRIBUTE,
                            bootStrapConfig.getUserStoreProperties().get(LDAPConstants.GROUP_NAME_ATTRIBUTE));
                    userStoreProperties.put(LDAPConstants.ROLE_NAME_FILTER,
                            bootStrapConfig.getUserStoreProperties().get(LDAPConstants.ROLE_NAME_FILTER));
                }
            }

            return ldapRealmConfig;

        } catch (Exception e) {
            String errorMessage = "Error while building tenant specific realm configuration " +
                    "to be persisted for tenant id : " + tenantId;
            if (logger.isDebugEnabled()) {
                logger.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        }
    }

    private void removePropertiesFromTenantRealmConfig(
            RealmConfiguration tenantRealmConfiguration) {
        //remove sensitive information from realm properties before persisting
        // tenant specific user-mgt.xml
        tenantRealmConfiguration.getRealmProperties().clear();

        //remove sensitive information from user store properties before persisting
        //tenant specific user-mgt.xml
        //but keep the tenant manager property
        String tenantManagerKey = UserCoreConstants.TenantMgtConfig.LOCAL_NAME_TENANT_MANAGER;
        String tenantManagerValue = tenantRealmConfiguration.getUserStoreProperty(tenantManagerKey);

        // We need remove only the sensitive  data from user store config..  If not, we need
        // put all config in to tenant realm
        tenantRealmConfiguration.getUserStoreProperties().remove(LDAPConstants.CONNECTION_NAME);
        tenantRealmConfiguration.getUserStoreProperties().remove(LDAPConstants.CONNECTION_PASSWORD);
        tenantRealmConfiguration.getUserStoreProperties().remove(LDAPConstants.CONNECTION_URL);
        tenantRealmConfiguration.getUserStoreProperties().remove(LDAPConstants.PASSWORD_HASH_METHOD);
        tenantRealmConfiguration.getUserStoreProperties().remove("passwordHashMethod");
        tenantRealmConfiguration.getUserStoreProperties().remove(LDAPConstants.USER_SEARCH_BASE);
        tenantRealmConfiguration.getUserStoreProperties().remove(LDAPConstants.GROUP_SEARCH_BASE);
        tenantRealmConfiguration.getUserStoreProperties().put(tenantManagerKey, tenantManagerValue);
    }
}
