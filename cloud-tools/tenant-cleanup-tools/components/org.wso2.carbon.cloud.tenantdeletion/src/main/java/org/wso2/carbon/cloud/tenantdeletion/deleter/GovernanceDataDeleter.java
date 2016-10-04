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
import org.wso2.carbon.cloud.tenantdeletion.constants.DeletionConstants;
import org.wso2.carbon.cloud.tenantdeletion.internal.ServiceHolder;
import org.wso2.carbon.cloud.tenantdeletion.utils.DataAccessManager;
import org.wso2.carbon.cloud.tenantdeletion.utils.TenantDeletionMap;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.dataaccess.JDBCDataAccessManager;

import java.sql.SQLException;
import java.util.Map;

/**
 * Represents the tenant governance data deletion operation
 */
public class GovernanceDataDeleter extends RegistryDataDeleter {

    private static final Log LOG = LogFactory.getLog(RegistryDataDeleter.class);
    private static JDBCDataAccessManager govRegManager;

    public GovernanceDataDeleter() {
        setGovRegManager();
    }

    public static void setGovRegManager() {
        try {
            govRegManager = (JDBCDataAccessManager) ServiceHolder.getInstance().getRegistryService().
                    getGovernanceUserRegistry().getRegistryContext().getDataAccessManager();
        } catch (RegistryException e) {
            String msg = "Error occurred while getting governance registry access manager.";
            LOG.error(msg, e);
        }
    }

    /**
     * Method to startDeletion Governance data for given tenants.
     *
     * @param deletionLimit Number of tenants to be cleaned up in a single round
     */
    public void delete(int deletionLimit) {
        Map<String, Integer> tenantMap;
        boolean deletionCompleted =
                TenantDeletionMap.getInstance().checkDeletionCompleted(DeletionConstants.GOVERNANCE);
        //If deletion has been limited to specific number of tenants
        if (!deletionCompleted) {
            if (deletionLimit != 0) {
                tenantMap = TenantDeletionMap.getInstance()
                                             .getInactiveTenantMap(DeletionConstants.GOVERNANCE, deletionLimit);
            } else {
                tenantMap = TenantDeletionMap.getInstance().getInactiveTenantMap(DeletionConstants.GOVERNANCE);
            }
            if (tenantMap != null && !tenantMap.isEmpty()) {
                triggerDelete(tenantMap);
                LOG.info("Governance data deletion completed for all the " + tenantMap.size() + " tenants.");
            } else {
                LOG.info("No Governance data to be deleted");
            }
        } else {
            LOG.info("All Governance data already deleted");
        }
        DataAccessManager.getInstance().raiseDeletionFlag(DeletionConstants.GOVERNANCE);
    }

    /**
     * Deletion start method for the class.
     * Runs the deletion queries on governance registry calling RegistryDataDeleter.deleteTenantRegistryData()
     *
     * @param tenantMap Map of tenant Domain, tenant Id to be delete APIs
     */
    public void triggerDelete(Map<String, Integer> tenantMap) {
        for (Map.Entry<String, Integer> entry : tenantMap.entrySet()) {
            String tenantDomain = entry.getKey();
            try {
                deleteTenantRegistryData(tenantMap.get(tenantDomain), govRegManager.getDataSource().getConnection());
                DataAccessManager.getInstance().raiseDeletionFlag(DeletionConstants.GOVERNANCE, tenantDomain,
                                                                  DeletionConstants.DELETION_SUCCESS_STATUS);
            } catch (SQLException e) {
                String msg = "Error while deleting governance registry data of tenant : " + tenantMap.get(tenantDomain);
                LOG.error(msg, e);
                //Sets deletion flag to 2 for the tenant domain
                DataAccessManager.getInstance().raiseDeletionFlag(DeletionConstants.GOVERNANCE, tenantDomain,
                                                                  DeletionConstants.DELETION_ERROR_STATUS);
            }
        }
    }
}
