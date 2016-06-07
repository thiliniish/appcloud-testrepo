/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.cloud.das.purge.admin.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.exception.AnalyticsIndexException;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.cloud.das.purge.admin.service.util.*;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.naming.NamingException;
import java.sql.SQLException;
import java.util.*;

/**
 * Helper class that executes DAS data purging
 */
public class DataPurgeTool {
    private static final Log log = LogFactory.getLog(DataPurgeTool.class);

    private AnalyticsDataAPI analyticsDataAPI;

    /**
     * Set the AnalyticsDataAPI for Data purging
     */
    private void setAnalyticsDataAPI() {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            Object service = PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .getOSGiService(AnalyticsDataAPI.class);
            if (service instanceof AnalyticsDataAPI) {
                this.analyticsDataAPI = (AnalyticsDataAPI) service;
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Purge data corresponding to a specific year and month, from all tenants except paid tenants
     *
     * @param tenantDomain
     */
    public boolean purgeDataForTenant(String tenantDomain) {
        // Variable used to track success of data purge
        boolean isSuccessful = false;
        setAnalyticsDataAPI();

        //Get super user tenant id
        int superUserTenantId = MultitenantConstants.SUPER_TENANT_ID;

        // Get statistics tables
        List<String> tables;
        try {
            tables = analyticsDataAPI.listTables(superUserTenantId);
        } catch (AnalyticsException e) {
            log.error("An error occurred while getting list of tables from super tenant.", e);
            log.warn("Aborting the data purge for tenant:" + tenantDomain);
            return isSuccessful;
        }

        for (int i = 0; i < tables.size(); i++) {
            try {
                //Get column names of table
                AnalyticsSchema analyticsSchema = analyticsDataAPI.getTableSchema(superUserTenantId, tables.get(i));
                Map<String, ColumnDefinition> columns = analyticsSchema.getColumns();
                Set<String> columnNames = columns.keySet();
                //Set the tenant domain based filtering
                List<String> columnNamesWithTenantDomain = getColumnNamesWithTenantDomain(columnNames);
                String query = getTenantDomainBasedQuery(columnNamesWithTenantDomain, tenantDomain);

                //Skip this search if the query is empty
                if (query.isEmpty()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Skipping the data purge for tenant:" + tenantDomain + " in " + tables.get(i)
                                + " as there are no columns corresponding to tenant domain.");
                    }
                    log.info("Skipping the data purge for tenant:" + tenantDomain + " related records in " + tables
                            .get(i));
                    continue;
                }
                //Get the search count for each query
                int searchResultCount = analyticsDataAPI.searchCount(superUserTenantId, tables.get(i), query);
                //Skip the search if there are no results
                if (searchResultCount <= 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("Skipping the data purge for tenant:" + tenantDomain + " in " + tables.get(i)
                                + " as there are" + " no records satisfying the query " + query);
                    }
                    log.info("Skipping the data purge for tenant:" + tenantDomain + " related records in " + tables
                            .get(i));
                    continue;
                }
                //Get record ids for each table satisfying the query
                List<SearchResultEntry> searchResultEntries = analyticsDataAPI
                        .search(superUserTenantId, tables.get(i), query, 0, searchResultCount);
                List<String> resultIds = new ArrayList<>();
                for (SearchResultEntry searchResultEntry : searchResultEntries) {
                    resultIds.add(searchResultEntry.getId());
                }
                //Delete records which satisfy the search query
                analyticsDataAPI.delete(superUserTenantId, tables.get(i), resultIds);
            } catch (AnalyticsException e) {
                log.error("An error occurred while deleting records related to tenant:" + tenantDomain + " in " + tables
                        .get(i), e);
                return isSuccessful;
            }
            if (log.isDebugEnabled()) {
                log.debug("Data Purge task for tenant:" + tenantDomain + " in table:" + tables.get(i) + " is "
                        + "successfully completed.");
            }
        }
        isSuccessful = true;
        if (log.isDebugEnabled()) {
            log.debug("Data Purge task for tenant:" + tenantDomain + " is successfully completed.");
        }
        return isSuccessful;
    }

    /**
     * Purge data corresponding to a specific year and month, from all tenants except paid tenants
     *
     * @param year
     * @param month
     */
    public boolean purgeDataForDate(String year, String month, boolean useYearOnly) {
        boolean isSuccessful = false;
        setAnalyticsDataAPI();

        int superUserTenantId = MultitenantConstants.SUPER_TENANT_ID;
        // Get statistics tables
        List<String> tables;
        try {
            tables = analyticsDataAPI.listTables(superUserTenantId);
        } catch (AnalyticsException e) {
            log.error("An error occurred while getting list of tables from super tenant.", e);
            log.warn("Aborting the data purge based on date.");
            return isSuccessful;
        }

        //Search for all tenants except paid tenants
        List<String> allNotPaidTenants = getTenantDomainsFromDB();
        for (String tenantDomain : allNotPaidTenants) {
            boolean isSuccessForTenant = false;
            for (int i = 0; i < tables.size(); i++) {
                log.info("Table: " + tables.get(i));

                try {
                    //Get column names
                    AnalyticsSchema analyticsSchema = analyticsDataAPI.getTableSchema(superUserTenantId, tables.get(i));
                    Map<String, ColumnDefinition> columns = analyticsSchema.getColumns();
                    Set<String> columnNames = columns.keySet();

                    //Get the query based on date-time related columns
                    String query = getTimeBasedQuery(columnNames, year, month, useYearOnly);

                    //Skip this search if the time based query is empty
                    if (query.isEmpty()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Skipping the data purge for date as there are no columns in table: " + tables
                                    .get(i) + " denoting time.");
                        }
                        log.info("Skipping the data purge for date related records in " + tables.get(i) + " for tenant:"
                                +
                                tenantDomain);
                        continue;
                    }
                    //Set the tenant domain based filtering
                    List<String> columnNamesWithTenantDomain = getColumnNamesWithTenantDomain(columnNames);

                    if (!columnNamesWithTenantDomain.isEmpty()) {
                        query = query.concat(" " + DASPurgeToolConstants.AND_OPERATOR + " ( ");
                        query = query.concat(getTenantDomainBasedQuery(columnNamesWithTenantDomain, tenantDomain));
                        query = query.concat(" )");
                    }

                    //Get the search count for each query
                    int searchResultCount = analyticsDataAPI.searchCount(superUserTenantId, tables.get(i), query);

                    //Skip this search as there are no results
                    if (searchResultCount <= 0) {
                        if (log.isDebugEnabled()) {
                            log.debug("Skipping the data purge for date in " + tables.get(i)
                                    + " as there are no records satisfying the query " + query);
                        }
                        log.info("Skipping the data purge for date related records in " + tables.get(i) + " for tenant:"
                                +
                                tenantDomain);
                        continue;
                    }

                    //Get record ids for each table satisfying the query
                    List<SearchResultEntry> searchResultEntries = analyticsDataAPI
                            .search(superUserTenantId, tables.get(i), query, 0, searchResultCount);
                    List<String> resultIds = new ArrayList<>();
                    for (SearchResultEntry searchResultEntry : searchResultEntries) {
                        resultIds.add(searchResultEntry.getId());
                    }
                    //Delete records which satisfy the search query
                    analyticsDataAPI.delete(superUserTenantId, tables.get(i), resultIds);
                } catch (AnalyticsException e) {
                    log.error("An error occurred while deleting records related to date in " + tables.get(i) + " for "
                            + "tenant " + tenantDomain, e);
                    log.warn("Skipping data purge for tenant:" + tenantDomain + " due to error.");
                    isSuccessForTenant = false;
                    break;
                }
                isSuccessForTenant = true;
                if (log.isDebugEnabled()) {
                    log.debug("Date based Data Purge task for tenant:" + tenantDomain + " in table:" + tables.get(i) +
                            " is successfully completed.");
                }
            }
            if (log.isDebugEnabled() && isSuccessForTenant) {
                log.debug("Date based Data Purge task for tenant:" + tenantDomain + " is successfully completed.");
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Data Purge task for year:" + year + " and month:" + month + " is successfully completed.");
        }
        isSuccessful = true;
        return isSuccessful;
    }

    /**
     * Given a list of column names with year or (year & month) constructs a query based on date-time
     *
     * @param columnNames
     * @param year
     * @param month
     * @param useYearOnly
     * @return
     */
    private String getTimeBasedQuery(Set<String> columnNames, String year, String month, boolean useYearOnly) {
        String query = "";
        //Set the time based filtering
        if (columnNames.contains(DASPurgeToolConstants.YEAR_COLUMN) && columnNames
                .contains(DASPurgeToolConstants.MONTH_COLUMN)) {
            if (useYearOnly) {
                query = DASPurgeToolConstants.YEAR_COLUMN + ":" + year;
            } else {
                query = DASPurgeToolConstants.YEAR_COLUMN + ":" + year + " " + DASPurgeToolConstants.AND_OPERATOR + " "
                        + DASPurgeToolConstants.MONTH_COLUMN + ":" + month;
            }

        } else if (columnNames.contains(DASPurgeToolConstants.TIMESTAMP_COLUMN)) {
            if (useYearOnly) {
                query = DASPurgeToolConstants.TIMESTAMP_COLUMN + ":" + year + "*";
            } else {
                query = DASPurgeToolConstants.TIMESTAMP_COLUMN + ":" + year + "-" + month + "*";
            }
        } else if (columnNames.contains(DASPurgeToolConstants.REQUEST_TIME_COLUMN)) {
            query = getTimeQuery(DASPurgeToolConstants.REQUEST_TIME_COLUMN, year, month, useYearOnly);

        } else if (columnNames.contains(DASPurgeToolConstants.EVENT_TIME_COLUMN)) {
            query = getTimeQuery(DASPurgeToolConstants.EVENT_TIME_COLUMN, year, month, useYearOnly);

        } else if (columnNames.contains(DASPurgeToolConstants.CREATED_TIME_COLUMN)) {
            query = getTimeQuery(DASPurgeToolConstants.CREATED_TIME_COLUMN, year, month, useYearOnly);

        } else if (columnNames.contains(DASPurgeToolConstants.THROTTLED_TIME_COLUMN)) {
            query = getTimeQuery(DASPurgeToolConstants.THROTTLED_TIME_COLUMN, year, month, useYearOnly);

        }
        return query;
    }

    /**
     * Given a list of column names return the column names which contain tenant domain
     *
     * @param columnNames
     * @return list of column names
     */
    private List<String> getColumnNamesWithTenantDomain(Set<String> columnNames) {
        List<String> columnNamesWithTenantDomain = new ArrayList<>();
        if (columnNames.contains(DASPurgeToolConstants.API_PUBLISHER_COLUMN)) {
            columnNamesWithTenantDomain.add(DASPurgeToolConstants.API_PUBLISHER_COLUMN);
        }
        if (columnNames.contains(DASPurgeToolConstants.TENANT_DOMAIN_COLUMN)) {
            columnNamesWithTenantDomain.add(DASPurgeToolConstants.TENANT_DOMAIN_COLUMN);
        }
        if (columnNames.contains(DASPurgeToolConstants.USER_ID_COLUMN)) {
            columnNamesWithTenantDomain.add(DASPurgeToolConstants.USER_ID_COLUMN);
        }
        return columnNamesWithTenantDomain;
    }

    /**
     * Creates the query to search for the given tenant domain in all tenant domain based columns
     *
     * @param columnNamesWithTenantDomain
     * @param tenantDomain
     * @return
     */
    private String getTenantDomainBasedQuery(List<String> columnNamesWithTenantDomain, String tenantDomain) {
        String query = "";
        String columnName;
        for (int j = 0; j < columnNamesWithTenantDomain.size(); j++) {
            columnName = columnNamesWithTenantDomain.get(j);
            query = query.concat(columnName + ":" + tenantDomain);
            if (j != columnNamesWithTenantDomain.size() - 1) {
                query = query.concat(" " + DASPurgeToolConstants.OR_OPERATOR + " ");
            }
        }
        return query;
    }

    /**
     * Get time based query for columns which require time in millis
     *
     * @param timeRelatedColumn
     * @param year
     * @param month
     * @return a time based query
     */
    private String getTimeQuery(String timeRelatedColumn, String year, String month, boolean useYearOnly) {
        String query = "";
        if (useYearOnly) {
            //Get start date and end date
            Calendar gc = new GregorianCalendar(Integer.parseInt(year), 0, 1); // 0 = January and 1 = 1st Day
            Date startDate = gc.getTime();
            gc.setTime(startDate);
            gc.set(Calendar.MONTH, 11); // 11 = December
            gc.set(Calendar.DAY_OF_MONTH, gc.getActualMaximum(Calendar.DAY_OF_MONTH));
            Date endDate = gc.getTime();
            query = timeRelatedColumn + ":[" + startDate.getTime() + " TO " + "" + endDate.getTime() + "]";
        } else {
            //Get start date and end date
            Calendar gc = new GregorianCalendar(Integer.parseInt(year), Integer.parseInt(month) - 1, 1);
            Date startDate = gc.getTime();
            gc.setTime(startDate);
            gc.set(Calendar.DAY_OF_MONTH, gc.getActualMaximum(Calendar.DAY_OF_MONTH));
            Date endDate = gc.getTime();
            query = timeRelatedColumn + ":[" + startDate.getTime() + " TO " + "" + endDate.getTime() + "]";
        }
        return query;
    }

    /**
     * Get all tenant domains except paid tenants from the cloud-mgt database
     *
     * @return list of tenant Domains except paid tenants
     */
    private List<String> getTenantDomainsFromDB() {
        DBConnector dbConnector = new DBConnector();
        List<String> tenantDomains = new ArrayList<>();
        try {
            tenantDomains = dbConnector.getNotPaidTenantDomains();

        } catch (NamingException e) {
            log.error("Error while checking user account validity for admin user." + e.getMessage(), e);
        } catch (SQLException e) {
            log.error("Error while getting paid tenant list " + e.getMessage(), e);
        }
        return tenantDomains;
    }

}
