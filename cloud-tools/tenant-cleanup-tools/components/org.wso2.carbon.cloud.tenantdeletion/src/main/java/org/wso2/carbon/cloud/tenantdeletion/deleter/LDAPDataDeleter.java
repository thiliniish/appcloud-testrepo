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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.cloud.tenantdeletion.deleter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.tenantdeletion.constants.DeletionConstants;
import org.wso2.carbon.cloud.tenantdeletion.internal.ServiceHolder;
import org.wso2.carbon.cloud.tenantdeletion.utils.DataAccessManager;
import org.wso2.carbon.cloud.tenantdeletion.utils.TenantDeletionMap;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.TenantMgtConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.ldap.LDAPConnectionContext;
import org.wso2.carbon.user.core.ldap.LDAPConstants;

import java.util.Map;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;

/**
 * LDAP Data deletion class for tenants
 */
public class LDAPDataDeleter {
    private static final Log LOG = LogFactory.getLog(LDAPDataDeleter.class);
    private static DirContext dirContext;
    private static TenantMgtConfiguration tenantMgtConfig;

    public LDAPDataDeleter() {
        setTenantMgtConfig();
    }

    public static void setTenantMgtConfig() {
        tenantMgtConfig = ServiceHolder.getInstance().getRealmService().getTenantMgtConfiguration();
        RealmConfiguration realmConfig = ServiceHolder.getInstance().getRealmService().
                getBootstrapRealmConfiguration();
        try {
            LDAPConnectionContext ldapConnectionSource = new LDAPConnectionContext(realmConfig);
            dirContext = ldapConnectionSource.getContext();
        } catch (UserStoreException e) {
            String msg = "Error occurred while connecting to the LDAP.";
            LOG.error(msg, e);
        }
    }

    /**
     * Delete tenant specific LDAP data
     *
     * @param dirContext
     * @param dnOfOrganizationalContext
     * @param dnOfGroupContext
     */
    public static void deleteTenantLDAPData(DirContext dirContext, String dnOfOrganizationalContext,
                                            String dnOfGroupContext) {
        LOG.info("Deleting Organizational Context : " + dnOfOrganizationalContext);
        try {
            while (dirContext.list(dnOfGroupContext).hasMore()) {
                dirContext.unbind(dirContext.list(dnOfGroupContext).next().getNameInNamespace());
            }
            dirContext.unbind(dnOfGroupContext);
            dirContext.unbind(dnOfOrganizationalContext);
        } catch (NamingException e) {
            LOG.error("Naming Exception", e);
        }
    }

    /**
     * Method to startDeletion Governance data for given tenants.
     *
     * @param deletionLimit Number of tenants to be cleaned up in a single round
     */
    public void delete(String deletionLimit) {
        Map<String, Integer> tenantMap;
        boolean deletionCompleted = TenantDeletionMap.getInstance().checkDeletionCompleted(DeletionConstants.LDAP);
        if (!deletionCompleted) {
            //If deletion has been limited to specific number of tenants
            if (StringUtils.isNotEmpty(deletionLimit)) {
                int limit = Integer.parseInt(deletionLimit);
                tenantMap = TenantDeletionMap.getInstance().getInactiveTenantMap(DeletionConstants.LDAP, limit);
            } else {
                tenantMap = TenantDeletionMap.getInstance().getInactiveTenantMap(DeletionConstants.LDAP);
            }
            if (tenantMap != null && !tenantMap.isEmpty()) {
                triggerDelete(tenantMap);
                LOG.info("LDAP data deletion completed for all the " + tenantMap.size() + " tenants.");
            } else {
                LOG.info("No LDAP data to be deleted");
            }
        } else {
            LOG.info("All LDAP data already deleted");
        }
        DataAccessManager.getInstance().raiseDeletionFlag(DeletionConstants.LDAP);
    }

    /**
     * Deletion start method for the class.
     * Runs the deletion on LDAP while creating relevant organizational contexts
     *
     * @param tenantMap Map of tenant Domain, tenant Id to be delete APIs
     */
    public void triggerDelete(Map<String, Integer> tenantMap) {
        for (Map.Entry<String, Integer> entry : tenantMap.entrySet()) {
            String tenantDomain = entry.getKey();
            String organizationNameAttribute = tenantMgtConfig.getTenantStoreProperties().
                    get(UserCoreConstants.TenantMgtConfig.PROPERTY_ORG_SUB_CONTEXT_ATTRIBUTE);
            String partitionDN = tenantMgtConfig.getTenantStoreProperties().
                    get(UserCoreConstants.TenantMgtConfig.PROPERTY_ROOT_PARTITION);
            //ou=processDeletion.com,dc=wso2,dc=org
            String dnOfOrganizationalContext = organizationNameAttribute + "=" + tenantDomain + "," + partitionDN;
            //ou=groups,ou=processDeletion.com,dc=wso2,dc=org
            String dnOfGroupContext = tenantMgtConfig.getTenantStoreProperties().
                    get(UserCoreConstants.TenantMgtConfig.PROPERTY_ORG_SUB_CONTEXT_ATTRIBUTE) + "=" +
                                      LDAPConstants.GROUP_CONTEXT_NAME + "," +
                                      dnOfOrganizationalContext;
            try {
                deleteTenantLDAPData(dirContext, dnOfOrganizationalContext, dnOfGroupContext);
                //Sets deletion flag to 1 for the tenant domain
                DataAccessManager.getInstance().raiseDeletionFlag(DeletionConstants.LDAP, tenantDomain,
                                                                  DeletionConstants.DELETION_SUCCESS_STATUS);
            } catch (Exception e) {
                String msg = "Error occurred while deleting the Organization Sub context for tenant : " + tenantDomain;
                LOG.error(msg, e);
                //Sets deletion flag to 2 for the tenant domain
                DataAccessManager.getInstance().raiseDeletionFlag(DeletionConstants.LDAP, tenantDomain,
                                                                  DeletionConstants.DELETION_ERROR_STATUS);
            }
        }
    }
}
