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
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.cloud.das.datapurge.tool.internal.ServiceHolder;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
        AnalyticsDataAPI analyticsDataAPI = ServiceHolder.getAnalyticsDataAPI();
        log.info("Data purge tool is starting...");
        int tenantId = -1234;
        // Get tables
        try {
            List<String> tables = analyticsDataAPI.listTables(tenantId);
            for (int i = 0; i < tables.size(); i++) {
                log.info("Table: " + tables.get(i));


                //Get column names
                AnalyticsSchema analyticsSchema = analyticsDataAPI.getTableSchema(tenantId, tables.get(i));
                Map<String, ColumnDefinition> columns = analyticsSchema.getColumns();
                Set<String> columnNames = columns.keySet();

                String year = "2016";
                String tenantDomain = "wso2.com";

                if (columnNames.contains("tenantDomain") && columnNames.contains("year")) {
                    String columnName ="year";
                    //"SELECT * FROM " + tables.get(i) + " WHERE " + columnName+ " = '" +year+"' ;";
                    String query = "tenantDomain:" +tenantDomain + "AND year:" +year;
                    List<SearchResultEntry> searchResultEntries = analyticsDataAPI
                            .search(tenantId, tables.get(i), query, 0, 100);
                    for (SearchResultEntry searchResultEntry : searchResultEntries) {
                        log.info("Record Id: "+ searchResultEntry.getId());
                    }
                }
            }
        } catch (AnalyticsException e) {
            log.error("An error occurred while listing tables..", e);
        } catch (Throwable e) {
            log.error("Some exception occurred..", e);
        }
    }
}
