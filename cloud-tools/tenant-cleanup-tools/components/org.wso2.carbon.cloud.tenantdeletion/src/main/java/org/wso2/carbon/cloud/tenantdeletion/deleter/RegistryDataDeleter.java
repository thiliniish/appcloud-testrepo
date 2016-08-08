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
import org.wso2.carbon.cloud.tenantdeletion.constants.RegistryQueries;
import org.wso2.carbon.cloud.tenantdeletion.utils.DataAccessManager;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Represent the super class for registry deletion
 */
public class RegistryDataDeleter {
    private static final Log LOG = LogFactory.getLog(RegistryDataDeleter.class);

    /**
     * Delete all tenant information related to tenant stored in REG tables
     *
     * @param tenantId id of tenant whose data should be deleted
     * @param conn     database connection object
     */
    public static void deleteTenantRegistryData(int tenantId, Connection conn) {
        try {
            conn.setAutoCommit(false);
            String deleteClusterLockSql = RegistryQueries.QUERY_DELETE_CLUSTER_LOCK;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteClusterLockSql, tenantId);

            String deleteLogSql = RegistryQueries.QUERY_DELETE_LOG;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteLogSql, tenantId);

            String deleteAssociationSql = RegistryQueries.QUERY_DELETE_ASSOCIATION;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteAssociationSql, tenantId);

            String deleteSnapshotSql = RegistryQueries.QUERY_DELETE_SNAPSHOT;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteSnapshotSql, tenantId);

            String deleteResourceCommentSql = RegistryQueries.QUERY_DELETE_RESOURCE_COMMENT;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteResourceCommentSql, tenantId);

            String deleteCommentSql = RegistryQueries.QUERY_DELETE_COMMENT;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteCommentSql, tenantId);

            String deleteResourceRatingSql = RegistryQueries.QUERY_DELETE_RESOURCE_RATING;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteResourceRatingSql, tenantId);

            String deleteRatingSql = RegistryQueries.QUERY_DELETE_RATING;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteRatingSql, tenantId);

            String deleteResourceTagSql = RegistryQueries.QUERY_DELETE_RESOURCE_TAG;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteResourceTagSql, tenantId);

            String deleteTagSql = RegistryQueries.QUERY_DELETE_TAG;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteTagSql, tenantId);

            String deleteResourcePropertySql = RegistryQueries.QUERY_DELETE_RESOURCE_PROPERTY;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteResourcePropertySql, tenantId);

            String deletePropertySql = RegistryQueries.QUERY_DELETE_PROPERTY;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deletePropertySql, tenantId);

            String deleteResourceHistorySql = RegistryQueries.QUERY_DELETE_RESOURCE_HISTORY;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteResourceHistorySql, tenantId);

            String deleteContentHistorySql = RegistryQueries.QUERY_DELETE_CONTENT_HISTORY;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteContentHistorySql, tenantId);

            String deleteResourceSql = RegistryQueries.QUERY_DELETE_RESOURCE;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteResourceSql, tenantId);

            String deleteContentSql = RegistryQueries.QUERY_DELETE_CONTENT;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deleteContentSql, tenantId);

            String deletePathSql = RegistryQueries.QUERY_DELETE_PATH;
            DataAccessManager.getInstance().executeDeleteQuery(conn, deletePathSql, tenantId);

            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                LOG.error("SQL Exception occurred due to rollback", e1);
            }
            String errorMsg = "An error occurred while deleting registry data for tenant: " + tenantId;
            LOG.error(errorMsg, e);
        } catch (Exception e) {
            LOG.error("SQL Exception occurred while executing queries ", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                LOG.error("SQL Exception occurred while Closing connection ", e);
            }
        }
    }
}