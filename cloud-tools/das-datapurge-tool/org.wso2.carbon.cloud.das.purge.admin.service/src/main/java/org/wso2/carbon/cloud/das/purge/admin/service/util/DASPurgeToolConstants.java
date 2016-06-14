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

package org.wso2.carbon.cloud.das.purge.admin.service.util;

/**
 * Constants for DAS Purge Tool
 */
public class DASPurgeToolConstants {

    /**
     * Avoids instantiation of DASPurgeToolConsdtants
     */
    private DASPurgeToolConstants() {
    }

    public static final String AND_OPERATOR = "AND";
    public static final String OR_OPERATOR = "OR";

    public static final String YEAR_COLUMN = "year";
    public static final String MONTH_COLUMN = "month";
    public static final String TIMESTAMP_COLUMN = "timestamp";
    public static final String REQUEST_TIME_COLUMN = "requestTime";
    public static final String EVENT_TIME_COLUMN = "eventTime";
    public static final String CREATED_TIME_COLUMN = "createdTime";
    public static final String THROTTLED_TIME_COLUMN = "throttledTime";
    public static final String TENANT_DOMAIN_COLUMN = "tenantDomain";
    public static final String API_PUBLISHER_COLUMN = "apiPublisher";
    public static final String USER_ID_COLUMN = "userId";

    //database connection constants
    public static final String CLOUD_DATASOURCE = "jdbc/cloud_mgt";
    //SQL query to retrieve all tenants except paid tenants
    public static final String SQL_SELECT_NOT_PAID_TENANTS =
            "SELECT DISTINCT TENANT_DOMAIN FROM BILLING_STATUS WHERE TENANT_DOMAIN NOT IN (SELECT TENANT_DOMAIN FROM "
                    + "BILLING_STATUS WHERE TYPE='PAID' AND SUBSCRIPTION='api_cloud') AND "
                    + "SUBSCRIPTION='api_cloud';";
}
