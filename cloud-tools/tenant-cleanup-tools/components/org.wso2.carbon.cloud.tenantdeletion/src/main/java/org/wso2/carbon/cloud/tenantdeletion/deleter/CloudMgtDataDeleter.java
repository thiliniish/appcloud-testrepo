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
import org.wso2.carbon.cloud.tenantdeletion.conf.ConfigurationsType;
import org.wso2.carbon.cloud.tenantdeletion.constants.DeletionConstants;
import org.wso2.carbon.cloud.tenantdeletion.reader.ConfigReader;
import org.wso2.carbon.cloud.tenantdeletion.utils.DataAccessManager;
import org.wso2.carbon.cloud.tenantdeletion.utils.DataConnectionManager;
import org.wso2.carbon.cloud.tenantdeletion.utils.TenantDeletionMap;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Class to delete Cloud Management Data for the tenants
 */
public class CloudMgtDataDeleter {
    private static final Log LOG = LogFactory.getLog(CloudMgtDataDeleter.class);

    /**
     * Delete all tenant information related to tenant stored in CloudMgt tables
     *
     * @param tenantDomain tenantDomain whose data should be deleted
     * @param connection   database connection object
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value =
            "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING") public static void deleteTenantCloudMgtData(
            String tenantDomain, Connection connection) {
        try {
            ConfigurationsType configuration = ConfigReader.getInstance().getConfiguration();
            //CloudMgt queries are stored in the Tenant-Deletion.xml
            List<String> cloudMgtQueryList = configuration.getCloudMgtQueries().getCloudMgtQuery();
            DataAccessManager dataAccessManager = new DataAccessManager();
            connection.setAutoCommit(false);
            for (String sqlQuery : cloudMgtQueryList) {
                dataAccessManager.executeDeleteQuery(connection, sqlQuery, tenantDomain);
            }
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                LOG.error("SQL Exception occurred due to rollback", e1);
            }
            String errorMsg = "An error occurred while deleting registry data for tenant:" + tenantDomain;
            LOG.error(errorMsg, e);
        } catch (Exception e) {
            LOG.error("SQL Exception occurred while executing queries", e);
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                LOG.error("SQL Exception occurred while Closing connection", e);
            }
        }
    }

    /**
     * Method to startDeletion Cloud Mgt Data for given tenants.
     *
     * @param deletionLimit Number of tenants to be cleaned up in a single round
     */
    public void delete(String deletionLimit) {
        Map<String, Integer> tenantMap;
        boolean deletionCompleted = TenantDeletionMap.getInstance().checkDeletionCompleted(DeletionConstants.CLOUD_MGT);
        if (!deletionCompleted) {
            if (StringUtils.isNotEmpty(deletionLimit)) {
                int limit = Integer.parseInt(deletionLimit);
                tenantMap = TenantDeletionMap.getInstance().getInactiveTenantMap(DeletionConstants.CLOUD_MGT, limit);
            } else {
                tenantMap = TenantDeletionMap.getInstance().getInactiveTenantMap(DeletionConstants.CLOUD_MGT);
            }
            if (tenantMap != null && !tenantMap.isEmpty()) {
                triggerDelete(tenantMap);
                LOG.info("Cloud Management deletion completed for all the " + tenantMap.size() + " tenants.");
            } else {
                LOG.info("No Cloud Management data to be deleted");
            }
        } else {
            LOG.info("All Cloud Management data already completed");
        }
        DataAccessManager.getInstance().raiseDeletionFlag(DeletionConstants.CLOUD_MGT);
    }

    /**
     * Deletion start method for the class.
     * Runs the deletion queries after retrieving the cloud management queries from tenant_deletion.xml
     *
     * @param tenantMap Map of tenant Domain, tenant Id to be delete APIs
     */
    public void triggerDelete(Map<String, Integer> tenantMap) {
        for (Map.Entry<String, Integer> entry : tenantMap.entrySet()) {
            String tenantDomain = entry.getKey();
            try {
                Connection connection = DataConnectionManager.getInstance().getCloudMgtDbConnection();
                deleteTenantCloudMgtData(tenantDomain, connection);
                //Sets deletion flag to 1 for the tenant domain
                DataAccessManager.getInstance().raiseDeletionFlag(DeletionConstants.CLOUD_MGT, tenantDomain,
                                                                  DeletionConstants.DELETION_SUCCESS_STATUS);
            } catch (Exception e) {
                String msg = "Error while deleting cloud management data of tenant : " + tenantDomain;
                LOG.error(msg, e);
                //Sets deletion flag to 2 for the tenant domain
                DataAccessManager.getInstance().raiseDeletionFlag(DeletionConstants.CLOUD_MGT, tenantDomain,
                                                                  DeletionConstants.DELETION_ERROR_STATUS);
            }
        }
    }
}
