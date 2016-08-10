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

package org.wso2.carbon.cloud.tenantdeletion.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.tenantdeletion.beans.DeleteJob;
import org.wso2.carbon.cloud.tenantdeletion.constants.DeletionConstants;
import org.wso2.carbon.cloud.tenantdeletion.reader.ConfigReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents class for keeping conf Map of servers to Delete list.
 */
public class TenantDeletionMap {

    private static final TenantDeletionMap instance = new TenantDeletionMap();
    private static final Log LOG = LogFactory.getLog(TenantDeletionMap.class);

    public static TenantDeletionMap getInstance() {
        return instance;
    }

    /**
     * Returns server map from tenant_deletion.xml file.
     *
     * @return server map
     */
    public Map<String, List<DeleteJob>> getServerMap() {
        Map<String, List<DeleteJob>> serverHashMap;
        serverHashMap = new HashMap<>();
        org.wso2.carbon.cloud.tenantdeletion.reader.ConfigReader configReader = ConfigReader.getInstance();
        List<DeleteJob> deleterObjList = configReader.getDeletionData();
        List<String> wso2ServerKeyList = configReader.getConfiguration().getServerKeys().getServerKey();

        for (String serverKey : wso2ServerKeyList) {
            List<DeleteJob> deleteJobList = new ArrayList<>();
            //Get conf server key from the wso2ServerKeyList and find any matching deleterObject which contain that
            // server
            // key as the server key of the object
            for (DeleteJob deleterObject : deleterObjList) {
                String[] deleteObjServerKeys = deleterObject.getServerKey().split(DeletionConstants.SEPARATOR);
                for (String deleteObjServerKey : deleteObjServerKeys) {
                    if (deleteObjServerKey.startsWith(serverKey)) {
                        deleteJobList.add(deleterObject);
                    }
                }
            }
            serverHashMap.put(serverKey, deleteJobList);
        }
        return serverHashMap;
    }

    /**
     * Return inactive Tenant map from delete table by giving the type of deleting
     *
     * @param deletionType deletion type
     * @return inactive tenant map
     */
    public Map<String, Integer> getInactiveTenantMap(String deletionType) {
        Map<String, Integer> tenantMap = DataAccessManager.getInstance().getAllInactiveTenants(deletionType);
        if (tenantMap.isEmpty()) {
            LOG.warn("No tenants found to be deleted, Please check tenant deletion database.");
            return null;
        }
        return tenantMap;
    }

    /**
     * Returns inactive map which contains conf set of tenants from delete table
     *
     * @param deletionType  deletion type
     * @param deletionLimit limit of deletion
     * @return inactive tenant map
     */
    public Map<String, Integer> getInactiveTenantMap(String deletionType, int deletionLimit) {
        Map<String, Integer> tenantMap =
                DataAccessManager.getInstance().getInactiveTenants(deletionType, deletionLimit);
        if (tenantMap.isEmpty()) {
            LOG.warn("No tenants were found to be deleted, Please check tenant deletion database.");
            return null;
        }
        return tenantMap;
    }

    /**
     * Check for deletion completed status for the specific type
     *
     * @param type String required type to check
     * @return Boolean status of deletion
     */
    public boolean checkDeletionCompleted(String type) {
        List<Integer> checkStatus = DataAccessManager.getInstance().getTypeDeletionStatus(type);
        if (!checkStatus.isEmpty()) {
            if (checkStatus.get(0) != 0) {
                return true;
            }
        }
        return false;
    }
}
