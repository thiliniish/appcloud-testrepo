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

package org.wso2.carbon.cloud.userstore.tenant;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.TenantMgtConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.ldap.LDAPConnectionContext;
import org.wso2.carbon.user.core.ldap.LDAPConstants;
import org.wso2.carbon.user.core.tenant.CommonHybridLDAPTenantManager;
import org.wso2.carbon.user.core.tenant.Tenant;

import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.sql.DataSource;

/**
 * This class is the tenant manager for any external LDAP and based on the "ou" partitioning
 * per tenant under one DIT.
 */
public class CloudTenantManager extends CommonHybridLDAPTenantManager {

    private static final Log LOGGER = LogFactory.getLog(CloudTenantManager.class);
    private LDAPConnectionContext ldapConnectionSource = null;
    private TenantMgtConfiguration tenantMgtConfig = null;
    private RealmConfiguration realmConfig = null;

    public CloudTenantManager(OMElement omElement, Map<String, Object> properties) throws Exception {
        super(omElement, properties);

        tenantMgtConfig = (TenantMgtConfiguration) properties.get(UserCoreConstants.TENANT_MGT_CONFIGURATION);

        realmConfig = (RealmConfiguration) properties.get(UserCoreConstants.REALM_CONFIGURATION);
        if (realmConfig == null) {
            throw new UserStoreException("Tenant Manager can not function without a bootstrap realm config");
        }
        ldapConnectionSource = new LDAPConnectionContext(realmConfig);
    }

    public CloudTenantManager(DataSource dataSource, String superTenantDomain) {
        super(dataSource, superTenantDomain);
    }

    /**
     * Create a space for tenant in LDAP.
     *
     * @param orgName           Organization name.
     * @param tenant            The tenant
     * @param initialDirContext The directory connection.
     * @throws UserStoreException If an error occurred while creating.
     */
    @Override
    protected void createOrganizationalUnit(String orgName, Tenant tenant, DirContext initialDirContext)
            throws UserStoreException {
        //e.g: ou=wso2.com
        String partitionDN = tenantMgtConfig.getTenantStoreProperties()
                .get(UserCoreConstants.TenantMgtConfig.PROPERTY_ROOT_PARTITION);
        createOrganizationalContext(partitionDN, orgName, initialDirContext);

        //create user store
        String organizationNameAttribute = tenantMgtConfig.getTenantStoreProperties()
                .get(UserCoreConstants.TenantMgtConfig.PROPERTY_ORG_SUB_CONTEXT_ATTRIBUTE);
        //eg:o=cse.org,dc=wso2,dc=com
        String dnOfOrganizationalContext = organizationNameAttribute + "=" + orgName + "," +
                partitionDN;
        //************ Cloud Specific Implementation ******************
        //We don't create the users ou inside the tenant ou
        //        createOrganizationalSubContext(dnOfOrganizationalContext,
        //                LDAPConstants.USER_CONTEXT_NAME, initialDirContext);
        //*************************************************************

        //create group store
        createOrganizationalSubContext(dnOfOrganizationalContext, LDAPConstants.GROUP_CONTEXT_NAME, initialDirContext);

        // This is introduced in CARBON-15187 and because of that Email is set as null for admin user. To resolve this
        // We need to apply the patch from IDENTITY-2949
        // See https://wso2.org/jira/browse/CARBON-15187 and https://wso2.org/jira/browse/IDENTITY-2949
        if (("true").equals(realmConfig.getAddAdmin())) {
            //create admin entry
            String orgSubContextAttribute = tenantMgtConfig.getTenantStoreProperties().
                    get(UserCoreConstants.TenantMgtConfig.PROPERTY_ORG_SUB_CONTEXT_ATTRIBUTE);
            //************ Cloud Specific Implementation ******************
            //eg: ou=users, dc=wso2,dc=com
//            String dnOfUserContext = orgSubContextAttribute + "="
//                    + LDAPConstants.USER_CONTEXT_NAME + "," + partitionDN;
            String dnOfUserContext = orgSubContextAttribute + "=user" + "," + partitionDN;
            //*************************************************************
            String dnOfUserEntry = createAdminEntry(dnOfUserContext, tenant, initialDirContext);

            //create admin group if write ldap group is enabled
            if (("true").equals(realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.WRITE_GROUPS_ENABLED))) {
                //construct dn of group context: eg:ou=groups,o=cse.org,dc=wso2,dc=com
                String dnOfGroupContext = orgSubContextAttribute + "=" + LDAPConstants.GROUP_CONTEXT_NAME + "," +
                        dnOfOrganizationalContext;
                createAdminGroup(dnOfGroupContext, dnOfUserEntry, initialDirContext);
            }
        }
    }

    @Override
    protected String createAdminEntry(String dnOfUserContext, Tenant tenant, DirContext initialDirContext)
            throws UserStoreException {
        String userDN;

        //************ Cloud Specific Implementation ******************
        //We allow a user to be admin of Multiple organizations
        Tenant convertedTenant = tenant;
        convertedTenant.setAdminName(doConvert(tenant.getAdminName()));
        if (doCheckExistingUser(convertedTenant.getAdminName(), initialDirContext)) {
            String userNameAttribute = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);
            String userRDN = userNameAttribute + "=" + convertedTenant.getAdminName();
            userDN = userRDN + "," + dnOfUserContext;
            return userDN;
        }
        //*************************************************************

        return super.createAdminEntry(dnOfUserContext, convertedTenant, initialDirContext);
    }

    /**
     * Cloud Specific implementation : Checks if the user is already existing
     *
     * @param userName          userName
     * @param initialDirContext initialDirContext
     * @return true if the user is existing
     * @throws UserStoreException
     */
    public boolean doCheckExistingUser(String userName, DirContext initialDirContext) throws UserStoreException {
        String convertedUserName = doConvert(userName);

        boolean bFound = false;

        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Searching for user " + convertedUserName);
            }
            String name = getNameInSpaceForUserName(convertedUserName, initialDirContext);
            if (name != null && name.length() > 0) {
                bFound = true;
            }
        } catch (UserStoreException e) {
            String msg = "Error occurred while checking existence of user : " + convertedUserName;
            throw new UserStoreException(msg, e);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("User: " + convertedUserName + " exist: " + bFound);
        }

        return bFound;
    }

    /**
     * @param userName          userName
     * @param initialDirContext initialDirContext
     * @return name
     * @throws UserStoreException
     */
    protected String getNameInSpaceForUserName(String userName, DirContext initialDirContext)
            throws UserStoreException {
        String searchBase;
        String userSearchFilter = realmConfig.getUserStoreProperty(LDAPConstants.USER_NAME_SEARCH_FILTER);
        userSearchFilter = userSearchFilter.replace("?", userName);
        searchBase = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
        return getNameInSpaceForUserName(userName, searchBase, userSearchFilter, initialDirContext);
    }

    /**
     * @param userName     userName
     * @param searchBase   searchBase
     * @param searchFilter searchFilter
     * @return name
     * @throws UserStoreException
     */
    protected String getNameInSpaceForUserName(String userName, String searchBase, String searchFilter,
            DirContext dirContext) throws UserStoreException {
        String name = null;

        NamingEnumeration<SearchResult> answer;
        try {
            SearchControls searchCtls = new SearchControls();
            searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Searching for user with SearchFilter: " + searchFilter + " in SearchBase: " + searchBase);
            }
            SearchResult userObj;
            String[] searchBases = searchBase.split("#");
            for (String base : searchBases) {
                answer = dirContext.search(base, searchFilter, searchCtls);
                if (answer.hasMore()) {
                    userObj = answer.next();
                    if (userObj != null) {
                        name = userObj.getNameInNamespace();
                        break;
                    }
                }
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Name in space for " + userName + " is " + name);
            }
        } catch (NamingException e) {
            throw new UserStoreException(e);
        }
        return name;
    }

    /**
     * converts the <code>@</code> symbol in the user name to a <code>.</code> symbol
     *
     * @param userName - user name to be converted
     * @return converted user Name
     */
    public String doConvert(String userName) {
        StringBuilder convertedUser = new StringBuilder(userName);
        if (userName.contains("@")) {
            int index = userName.indexOf('@');
            convertedUser.setCharAt(index, '.');
        }
        return convertedUser.toString();
    }

}
