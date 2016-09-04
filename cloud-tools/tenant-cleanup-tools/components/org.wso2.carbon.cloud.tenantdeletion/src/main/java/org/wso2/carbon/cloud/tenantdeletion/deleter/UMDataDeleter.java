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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.tenantdeletion.DeletionManager;
import org.wso2.carbon.cloud.tenantdeletion.constants.DeletionConstants;
import org.wso2.carbon.cloud.tenantdeletion.constants.UMQueries;
import org.wso2.carbon.cloud.tenantdeletion.internal.ServiceHolder;
import org.wso2.carbon.cloud.tenantdeletion.utils.DataAccessManager;
import org.wso2.carbon.cloud.tenantdeletion.utils.TenantDeletionMap;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import javax.sql.DataSource;

/**
 * User Management Data deleter class for tenants
 */
public class UMDataDeleter {
    private static final Log LOG = LogFactory.getLog(UMDataDeleter.class);
    private static DataSource realmDataSource;

    public UMDataDeleter() {
        setRealmDataSource();
    }

    public static void setRealmDataSource() {
        RealmConfiguration realmConfig = ServiceHolder.getInstance().getRealmService().
                getBootstrapRealmConfiguration();
        realmDataSource = DatabaseUtil.getRealmDataSource(realmConfig);
    }

    /**
     * Delete all tenant information related to tenant stored in UM tables
     *
     * @param tenantId id of tenant whose data should be deleted
     * @param conn     database connection object
     */
    public static void deleteTenantUMData(int tenantId, Connection conn) {
        try {
            conn.setAutoCommit(false);
            String deleteUserPermissionSql = UMQueries.QUERY_DELETE_USER_PERMISSION;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteUserPermissionSql, tenantId);

            String deleteRolePermissionSql = UMQueries.QUERY_DELETE_ROLE_PERMISSION;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteRolePermissionSql, tenantId);

            String deletePermissionSql = UMQueries.QUERY_DELETE_PERMISSION;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deletePermissionSql, tenantId);

            String deleteProfileConfigSql = UMQueries.QUERY_DELETE_PROFILE_CONFIG;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteProfileConfigSql, tenantId);

            String deleteClaimSql = UMQueries.QUERY_DELETE_CLAIM;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteClaimSql, tenantId);

            String deleteDialectSql = UMQueries.QUERY_DELETE_DIALECT;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteDialectSql, tenantId);

            String deleteUserAttributeSql = UMQueries.QUERY_DELETE_DELETE_USER_ATTRIBUTE;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteUserAttributeSql, tenantId);

            String deleteHybridUserRoleSql = UMQueries.QUERY_DELETE_HYBRID_USER_ROLE;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteHybridUserRoleSql, tenantId);

            String deleteHybridRoleSql = UMQueries.QUERY_DELETE_HYBRID_ROLE;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteHybridRoleSql, tenantId);

            String deleteHybridRememberMeSql = UMQueries.QUERY_DELETE_HYBRID_REMEMBER_ME;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteHybridRememberMeSql, tenantId);

            String deleteUserRoleSql = UMQueries.QUERY_DELETE_USER_ROLE;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteUserRoleSql, tenantId);

            String deleteRoleSql = UMQueries.QUERY_DELETE_ROLE;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteRoleSql, tenantId);

            String deleteUserSql = UMQueries.QUERY_DELETE_USER;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteUserSql, tenantId);

            String deleteDomainSql = UMQueries.QUERY_DELETE_DOMAIN;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteDomainSql, tenantId);

            String deleteSystemUserRoleSql = UMQueries.QUERY_DELETE_SYSTEM_USER_ROLE;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteSystemUserRoleSql, tenantId);

            String deleteSystemRoleSql = UMQueries.QUERY_DELETE_SYSTEM_ROLE;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteSystemRoleSql, tenantId);

            String deleteSystemUserSql = UMQueries.QUERY_DELETE_SYSTEM_USER;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteSystemUserSql, tenantId);

            String deleteTenantSql = UMQueries.QUERY_DELETE_TENANT;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteTenantSql, tenantId);

            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                LOG.error("SQL Exception occurred due to rollback", e1);
            }
            String errorMsg = "An error occurred while deleting registry data for tenant: " + tenantId;
            LOG.error(errorMsg, e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                LOG.error("SQL Exception occurred while Closing connection ", e);
            }
        }
    }

    /**
     * Method to startDeletion User Manager Data for given tenants.
     *
     * @param deletionLimit Number of tenants to be cleaned up in a single round
     */
    public void delete(int deletionLimit) {
        Map<String, Integer> tenantMap = null;
        boolean deletionCompleted = TenantDeletionMap.getInstance().checkDeletionCompleted(DeletionConstants.USER_MGT);
        //If deletion has been limited to specific number of tenants
        if (!deletionCompleted) {
            if (deletionLimit != 0) {
                tenantMap =
                        TenantDeletionMap.getInstance().getInactiveTenantMap(DeletionConstants.USER_MGT, deletionLimit);
            } else {
                tenantMap = TenantDeletionMap.getInstance().getInactiveTenantMap(DeletionConstants.USER_MGT);
            }
            if (tenantMap != null && !tenantMap.isEmpty()) {
                triggerDelete(tenantMap);
                LOG.info("User Management data deletion completed for all the " + tenantMap.size() + " tenants.");
            } else {
                LOG.info("User Management data to be deleted");
            }

        } else {
            LOG.info("User Management Data already Deleted");
        }
        //After completion UserMgt data deletion, Database flag updated to notify that UserMgt data deletion is over
        DataAccessManager.getInstance().raiseDeletionFlag(DeletionConstants.USER_MGT);
        //At last, UserMgt data will be deleted. Then this server will finish tenant deletion round
        if (tenantMap != null && !tenantMap.isEmpty()) {
            DeletionManager.getInstance().finishDeletionProcess(tenantMap);
        } else {
            DeletionManager.getInstance().finishDeletionWithoutEmail();
        }
    }

    /**
     * Deletion start method for the class.
     * Runs the deletion queries on User Manager by calling deleteTenantUMData()
     *
     * @param tenantMap Map of tenant Domain, tenant Id to be delete APIs
     */
    public void triggerDelete(Map<String, Integer> tenantMap) {
        for (Map.Entry<String, Integer> entry : tenantMap.entrySet()) {
            String tenantDomain = entry.getKey();
            try {
                deleteTenantUMData(tenantMap.get(tenantDomain), realmDataSource.getConnection());
                //Sets deletion flag to 1 for the tenant domain
                DataAccessManager.getInstance().raiseDeletionFlag(DeletionConstants.USER_MGT, tenantDomain,
                                                                  DeletionConstants.DELETION_SUCCESS_STATUS);
            } catch (SQLException e) {
                String msg = "Error while deleting user management data of tenant : " + tenantMap.get(tenantDomain);
                LOG.error(msg, e);
                //Sets deletion flag to 2 for the tenant domain
                DataAccessManager.getInstance().raiseDeletionFlag(DeletionConstants.USER_MGT, tenantDomain,
                                                                  DeletionConstants.DELETION_ERROR_STATUS);
            }
        }
    }
}
