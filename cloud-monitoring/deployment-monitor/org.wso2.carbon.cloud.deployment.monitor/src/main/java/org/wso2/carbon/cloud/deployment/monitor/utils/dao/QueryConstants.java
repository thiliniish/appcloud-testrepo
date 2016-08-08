/*
 * Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.cloud.deployment.monitor.utils.dao;

/**
 * SQL Queries
 */
public class QueryConstants {

    //StatusReporting

    public static final String ADD_SUCCESS_RECORD =
            "INSERT INTO SUCCESS_RECORD (TASK,SERVER,TIME) VALUES(?,?,?)";

    public static final String ADD_FAILURE_RECORD =
            "INSERT INTO FAILURE_RECORD (TASK,SERVER,TIME,ERROR) VALUES(?,?,?,?)";

    public static final String ADD_FAILURE_SUMMARY = "INSERT INTO FAILURE_SUMMARY "
            + "(TASK,SERVER,START_ID,END_ID,DATE,START_TIME,END_TIME,DOWN_TIME) VALUES(?,?,?,?,?,?,?,?)";

    public static final String UPDATE_CURRENT_TASK_STATUS =
            "INSERT INTO CURRENT_TASK_STATUS (TASK,SERVER,STATUS) VALUES (?,?,?) "
                    + "ON DUPLICATE KEY UPDATE STATUS=?,LAST_UPDATED=?";

    public static final String UPDATE_CURRENT_TASK_STATUS_FOR_MAINTENANCE =
            "UPDATE CURRENT_TASK_STATUS SET STATUS=?, LAST_UPDATED=? WHERE SERVER=? AND TASK=?";

    public static final String UPDATE_CURRENT_TASK_STATUS_FOR_MAINTENANCE_FOR_SERVER =
            "UPDATE CURRENT_TASK_STATUS SET STATUS=?, LAST_UPDATED=? WHERE SERVER=?";

    public static final String ADD_MAINTENANCE_SUMMARY =
            "INSERT INTO MAINTENANCE_SUMMARY (SERVER, TASK, DATE, STATUS, START_TIME) VALUES(?,?,?,?,?)";

    public static final String SELECT_MAINTENANCE_SUMMARY =
            "SELECT ID, START_TIME FROM MAINTENANCE_SUMMARY WHERE SERVER=? AND TASK=? AND STATUS=?";

    public static final String UPDATE_MAINTENANCE_SUMMARY =
            "UPDATE MAINTENANCE_SUMMARY SET STATUS=?, END_TIME=?, DOWN_TIME=? WHERE ID=?";

    public static final String ADD_DAILY_SERVICE_STATUS =
            "INSERT INTO DAILY_SERVICE_STATUS (SERVICE,DATE,STATUS,DOWN_TIME) VALUES(?,?,?,?)";

    //UptimeInformation

    public static final String SELECT_CURRENT_TASK_STATUSES_OF_SERVER =
            "SELECT * FROM CURRENT_TASK_STATUS WHERE SERVER=? ORDER BY LAST_UPDATED DESC";

    public static final String SELECT_ALL_CURRENT_TASK_STATUSES =
            "SELECT * FROM CURRENT_TASK_STATUS ORDER BY SERVER, LAST_UPDATED DESC";

    public static final String SELECT_FAILURE_SUMMARIES_OF_SERVER_IN_DATE_RANGE =
            "SELECT * FROM FAILURE_SUMMARY WHERE SERVER=? AND DATE BETWEEN ? AND ?";

    public static final String SELECT_DAILY_STATUSES_OF_SERVICE_IN_DATE_RANGE =
            "SELECT * FROM DAILY_SERVICE_STATUS WHERE SERVICE=? AND DATE BETWEEN ? AND ? ORDER BY DATE";

}
