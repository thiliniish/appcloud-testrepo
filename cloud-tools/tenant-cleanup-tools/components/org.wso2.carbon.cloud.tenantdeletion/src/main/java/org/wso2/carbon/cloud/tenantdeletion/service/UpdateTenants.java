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

package org.wso2.carbon.cloud.tenantdeletion.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.tenantdeletion.constants.DeletionConstants;
import org.wso2.carbon.cloud.tenantdeletion.internal.ServiceHolder;
import org.wso2.carbon.cloud.tenantdeletion.utils.DataAccessManager;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jws.WebMethod;

/**
 * Represent Class for web services
 */
public class UpdateTenants {
    private static final Log LOG = LogFactory.getLog(UpdateTenants.class);
    private List<Integer> allActiveTenantIdList = new ArrayList<>();
    private List<Integer> allTenantIdList = new ArrayList<>();

    /**
     * Trigger start deletion flag of the database to start deletion
     */
    public void startDeletion() {
        DataAccessManager dataAccessManager = DataAccessManager.getInstance();
        dataAccessManager.raiseDeletionFlag(DeletionConstants.START);
    }

    /**
     * Compare active tenant ID list with all tenants and update inactive tenants to database
     *
     * @param startDate start date of date range
     * @param endDate   end date of date range
     */
    public void prepareDeleteTenantList(String startDate, String endDate) {
        Map<Integer, String> deleteTenantMap = new HashMap<>();
        DataAccessManager dbHandler = new DataAccessManager();
        //Gets all active tenants who had logged into conf server in this give date range. Sometimes APIs can be loaded
        // even tenant doesn't log into the system. Then that tenant also treated as an active tenant.
        allActiveTenantIdList = dbHandler.getAllActiveTenantIdList(startDate, endDate);
        Tenant[] allTenants;
        TenantManager tenantManager = ServiceHolder.getInstance().getRealmService().getTenantManager();
        try {
            if (tenantManager == null) {
                LOG.error("Tenant manager is null");
                return;
            }
            allTenants = tenantManager.getAllTenants();
            if (allTenants != null) {
                for (Tenant tenant : allTenants) {
                    allTenantIdList.add(tenant.getId());
                }
            }
            //Compare all tenant list and active tenant list.
            for (Integer tenantId : allTenantIdList) {
                for (int j = 0; j < allActiveTenantIdList.size(); j++) {
                    if (allActiveTenantIdList.get(j).equals(tenantId)) {
                        break;
                    }
                    //If current tenant is not in the active list, that tenant will be added to the deletion list
                    if (j == (allActiveTenantIdList.size() - 1) && !allActiveTenantIdList.get(j).equals(tenantId)) {
                        String tenantDomain = tenantManager.getTenant(tenantId).getDomain();
                        deleteTenantMap.put(tenantId, tenantDomain);
                    }
                }
            }
        } catch (UserStoreException e) {
            LOG.error(e.getMessage(), e);
        }
        if (!deleteTenantMap.isEmpty()) {
            //Before entering the new tenant list, removing previous deleted list is required.
            dbHandler.clearTenantDeletionTable();
            dbHandler.insertInactiveTenantsInfo(deleteTenantMap);
            removePaidUsersFromDeleteList();
            prepareEmailList(deleteTenantMap);
        } else {
            LOG.info("No inactive tenants");
        }
    }

    /**
     * Prepares the email list of the tenants which notification is to be sent and write it to a file
     *
     * @param deleteMap tenant list to be notified
     */
    public void prepareEmailList(Map<Integer, String> deleteMap) {
        DataAccessManager dbHandler = new DataAccessManager();
        List<String> deleteTenantDomainList = dbHandler.getAllInactiveTenantDomainList();
        BufferedWriter bufferedWriter = null;
        StringBuilder stringBuilder = null;
        try {
            bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(DeletionConstants.TENANT_DELETION_CSV_EXPORT_FILE_PATH),
                                           "UTF-8"));
            stringBuilder = new StringBuilder();
            for (String tenantDomain : deleteTenantDomainList) {
                stringBuilder.append(tenantDomain);
                stringBuilder.append(DeletionConstants.SEPARATOR);
                stringBuilder.append(dbHandler.getTenantAdminEmail(tenantDomain));
                stringBuilder.append(DeletionConstants.NEW_LINE);
            }
            bufferedWriter.write(stringBuilder.toString());
            bufferedWriter.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            LOG.info("CSV file not found : ", e);
        } catch (IOException e) {
            LOG.info("IO Exception while writing to file : ", e);
        }
    }

    /**
     * Method to remove responded users from deletion list after sending an email
     *
     * @param startDate start date of date range
     * @param endDate   end date of date range
     */
    public void removeRespondUsersToTheEmail(String startDate, String endDate) {
        DataAccessManager dbHandler = new DataAccessManager();
        List<Integer> respondTenantList = new ArrayList<>();
        allActiveTenantIdList = dbHandler.getAllActiveTenantIdList(startDate, endDate);
        List<Integer> deleteTenantIdList = dbHandler.getAllInactiveTenantIdList();
        if (deleteTenantIdList.isEmpty()) {
            LOG.debug("No Tenants to be deleted");
            return;
        }
        //Compare active tenant list and tenants who are in the delete list
        for (Integer deleteTenantId : deleteTenantIdList) {
            for (Integer activeTenantId : allActiveTenantIdList) {
                if (deleteTenantId.equals(activeTenantId)) {
                    //Add responded users to conf list
                    respondTenantList.add(deleteTenantId);
                }
            }
        }
        dbHandler.removeTenantsFromDeleteList(respondTenantList);
        removePaidUsersFromDeleteList();
    }

    /**
     * Method to remove paid users from deletion list
     */
    @WebMethod(exclude = true) public void removePaidUsersFromDeleteList() {
        DataAccessManager dbHandler = new DataAccessManager();
        //Gets PAID user list from CloudMgt database
        List<String> paidTenantList = dbHandler.getPaidTenantList();
        List<String> deleteTenantDomainList = dbHandler.getAllInactiveTenantDomainList();
        List<String> paidListToRemoveFromDeleteTable = new ArrayList<>();
        if (paidTenantList.isEmpty()) {
            LOG.debug("Paid tenant list is empty");
            return;
        }
        //Compare paid tenant list and tenants who are in the delete list
        for (String deleteTenantDomain : deleteTenantDomainList) {
            for (String paidTenantDomain : paidTenantList) {
                if (deleteTenantDomain.equals(paidTenantDomain)) {
                    paidListToRemoveFromDeleteTable.add(deleteTenantDomain);
                }
            }
        }
        dbHandler.removeTenantFromDeleteList(paidListToRemoveFromDeleteTable);
    }

    /**
     * Method to remove exclusive tenants (Special list of tenants) from deletion list
     */
    @WebMethod(exclude = true) public void removeExclusionListFromDeleteList() {
        DataAccessManager dbHandler = new DataAccessManager();
        //Gets exclusion list from the database
        List<Integer> exclusionList = dbHandler.getExclusiveTenantList();
        List<Integer> deleteTenantDomainList = dbHandler.getAllInactiveTenantIdList();
        List<Integer> exclusionListToRemoveFromDeleteTable = new ArrayList<>();
        if (exclusionList.isEmpty()) {
            LOG.debug("Deletion Exclusion tenant list is empty");
            return;
        }
        //Compare exclusion tenant list and tenants who are in the delete list
        for (Integer deleteTenantDomain : deleteTenantDomainList) {
            for (Integer tenantId : exclusionList) {
                if (deleteTenantDomain.equals(tenantId)) {
                    exclusionListToRemoveFromDeleteTable.add(deleteTenantDomain);
                }
            }
        }
        dbHandler.removeTenantsFromDeleteList(exclusionListToRemoveFromDeleteTable);
    }
}
