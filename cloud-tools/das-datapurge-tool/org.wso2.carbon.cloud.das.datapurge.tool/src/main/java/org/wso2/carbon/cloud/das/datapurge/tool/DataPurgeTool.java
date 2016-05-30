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

package org.wso2.carbon.cloud.das.datapurge.tool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.exception.AnalyticsIndexException;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.cloud.das.datapurge.tool.internal.ServiceHolder;
import org.wso2.carbon.cloud.das.datapurge.tool.util.DASPurgeToolConstants;
import org.wso2.carbon.cloud.das.datapurge.tool.util.DBConnector;

import javax.naming.NamingException;
import java.sql.SQLException;
import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Helper class that executes DAS data purging
 */
public class DataPurgeTool implements Runnable {
    private static final Log log = LogFactory.getLog(DataPurgeTool.class);

    /**
     * Method to override run
     */
    @Override public void run() {
        purge();
        if (log.isDebugEnabled()) {
            log.debug("Data purge tool started Successfully.");
        }
    }

    /**
     * Method used to purge data from DAS
     */
    public void purge() {
        log.info("Data purge tool is starting...");
        String year = "2016";
        String month = "4";
        String tenantDomain = "wso2.com";
        //purge data based on date and tenant domain
        purgeData(year, month, tenantDomain);
        //Read paid tenant list
        getPaidTenantList();
    }

    private void purgeData(String year, String month, String tenantDomain) {
        AnalyticsDataAPI analyticsDataAPI = ServiceHolder.getAnalyticsDataAPI();
        // Get tables
        try {
            List<String> tables = analyticsDataAPI.listTables(DASPurgeToolConstants.SUPER_USER_TENANT_ID);
            for (int i = 0; i < tables.size(); i++) {
                log.info("Table: " + tables.get(i));

                //Get column names
                AnalyticsSchema analyticsSchema = analyticsDataAPI
                        .getTableSchema(DASPurgeToolConstants.SUPER_USER_TENANT_ID, tables.get(i));
                Map<String, ColumnDefinition> columns = analyticsSchema.getColumns();
                Set<String> columnNames = columns.keySet();

                String query = "";

                //Set the time based filtering
                if (columnNames.contains(DASPurgeToolConstants.YEAR_COLUMN) && columnNames
                        .contains(DASPurgeToolConstants.MONTH_COLUMN)) {

                    query = DASPurgeToolConstants.YEAR_COLUMN + ":" + year + " " + DASPurgeToolConstants.AND_OPERATOR
                            + " " + DASPurgeToolConstants.MONTH_COLUMN + ":" + month;

                } else if (columnNames.contains(DASPurgeToolConstants.TIMESTAMP_COLUMN)) {
                    query = DASPurgeToolConstants.TIMESTAMP_COLUMN + ":" + year + "-" + month + "*";

                } else if (columnNames.contains(DASPurgeToolConstants.REQUEST_TIME_COLUMN)) {
                    query = getTimeQuery(DASPurgeToolConstants.REQUEST_TIME_COLUMN, year, month);

                } else if (columnNames.contains(DASPurgeToolConstants.EVENT_TIME_COLUMN)) {
                    query = getTimeQuery(DASPurgeToolConstants.EVENT_TIME_COLUMN, year, month);

                } else if (columnNames.contains(DASPurgeToolConstants.CREATED_TIME_COLUMN)) {
                    query = getTimeQuery(DASPurgeToolConstants.CREATED_TIME_COLUMN, year, month);

                } else if (columnNames.contains(DASPurgeToolConstants.THROTTLED_TIME_COLUMN)) {
                    query = getTimeQuery(DASPurgeToolConstants.THROTTLED_TIME_COLUMN, year, month);

                } else {
                    continue;
                }

                //Set the tenant domain based filtering
                List<String> columnNamesWithTenantDomain = getColumnNamesWithTenantDomain(columnNames);
                query = query.concat(" " + DASPurgeToolConstants.AND_OPERATOR + " ( ");
                String columnName;
                for (int j = 0; j < columnNamesWithTenantDomain.size(); j++) {
                    columnName = columnNamesWithTenantDomain.get(j);
                    log.info("Column with tenant Id: " + columnName);
                    query = query.concat(columnName + ":" +
                            tenantDomain);
                    if (j != columnNamesWithTenantDomain.size() - 1) {
                        query = query.concat(" " + DASPurgeToolConstants.OR_OPERATOR + " ");
                    }
                }
                query = query.concat(" )");
                log.info("Search query: " + query);
                //Get the search count for each query
                int searchResultCount = analyticsDataAPI
                        .searchCount(DASPurgeToolConstants.SUPER_USER_TENANT_ID, tables.get(i), query);

                //Skip this search as there are no results
                if (searchResultCount <= 0) {
                    log.info("Skipping this search as there are no records satisfying the query.");
                    continue;
                }

                //Get record ids for each table satisfying the query
                List<SearchResultEntry> searchResultEntries = analyticsDataAPI
                        .search(DASPurgeToolConstants.SUPER_USER_TENANT_ID, tables.get(i), query, 0, searchResultCount);
                List<String> resultIds = new ArrayList<String>();
                for (SearchResultEntry searchResultEntry : searchResultEntries) {
                    log.info("Result Id: " + searchResultEntry.getId());
                    resultIds.add(searchResultEntry.getId());
                }
                //Delete records which satisfy the search query
                //analyticsDataAPI.delete(DASPurgeToolConstants.SUPER_USER_TENANT_ID, tables.get(i),resultIds);

            }
        } catch (AnalyticsIndexException e) {
            log.error("An error occurred while indexing table records.", e);
        } catch (AnalyticsException e) {
            log.error("An error occurred while listing tables.", e);
        } catch (Exception e) {
            log.error("An error occurred while purging data from DAS.", e);
        }
    }

    private List<String> getColumnNamesWithTenantDomain(Set<String> columnNames) {
        List<String> columnNamesWithTenantDomain = new ArrayList<String>();
        if (columnNames.contains(DASPurgeToolConstants.API_PUBLISHER_COLUMN)) {
            columnNamesWithTenantDomain.add(DASPurgeToolConstants.API_PUBLISHER_COLUMN);
        } else if (columnNames.contains(DASPurgeToolConstants.TENANT_DOMAIN_COLUMN)) {
            columnNamesWithTenantDomain.add(DASPurgeToolConstants.TENANT_DOMAIN_COLUMN);
        } else if (columnNames.contains(DASPurgeToolConstants.USER_ID_COLUMN)) {
            columnNamesWithTenantDomain.add(DASPurgeToolConstants.USER_ID_COLUMN);
        }
        return columnNamesWithTenantDomain;
    }

    private String getTimeQuery(String timeRelatedColumn, String year, String month) {
        //Get start date and end date
        Calendar gc = new GregorianCalendar(Integer.parseInt(year), Integer.parseInt(month) - 1, 1);
        Date startDate = gc.getTime();
        gc.setTime(startDate);
        gc.set(Calendar.DAY_OF_MONTH, gc.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = gc.getTime();
        String query = timeRelatedColumn + ":[" + startDate.getTime() + " TO " + "" + endDate.getTime() + "]";
        return query;
    }

    private void getPaidTenantList() {
        DBConnector dbConnector = new DBConnector();
        try {
            String tenantDomains = dbConnector.getPaidTenantDomains();
        } catch (NamingException e) {
            log.error("Error while checking user account validity for admin user." + e.getMessage(), e);
        } catch (SQLException e) {
            log.error("Error while getting paid tenant list " + e.getMessage(), e);
        }
    }
}
