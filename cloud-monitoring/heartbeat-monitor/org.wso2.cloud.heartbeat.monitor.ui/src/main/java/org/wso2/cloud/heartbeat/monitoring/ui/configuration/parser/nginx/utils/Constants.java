/*
  * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */

package org.wso2.cloud.heartbeat.monitoring.ui.configuration.parser.nginx.utils;

/**
 * This keeps the element and strings used in the whole UI
 */
public class Constants {

    /* Nginx format related constants */

    public static final String NGINX_COMMENT = "#";
    public static final String NGINX_NODE_START_BRACE = "{";
    public static final String NGINX_NODE_END_BRACE = "}";
    public static final String NGINX_VARIABLE = "${";
    public static final String NGINX_LINE_DELIMITER = ";";
    public static final String NGINX_SPACE_REGEX = "[\\s]+";

    //format related constants

    public static final String DAY = "d";
    public static final String MINUTE = "m";
    public static final String HOUR = "h";
    public static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DECIMALFORMAT = "#.##";

    //heartbeat conf Strings

    public static final String DATA_SOURCE = "data_source";
    public static final String MODULES_NODE = "modules";
    public static final String ADMIN_NODE = "admin_user";
    public static final String CLOUDS_NODE = "clouds_used";
    public static final String CLOUDS_PROPERTY = "clouds";
    public static final String TIME_INTERVAL = "time_interval";

    //database column strings

    public static final String DB_TIMESTAMP = "TIMESTAMP";
    public static final String DB_SEVERITY = "SEVERITY";
    public static final String DB_STATUS = "STATUS";
    public static final String DB_TEST = "TEST";

    //heartbeat error constants

    public static final String SQL_EXCEPTION = "Heartbeat - Monitor - SQLException thrown while data access: ";
    public static final String IO_EXCEPTION =
            "Heartbeat - Monitor - IOException thrown while reading the configuration file: ";
    public static final String NO_CLOUDS = "Heartbeat - Monitor - No clouds specified in configuration: ";

    //UI Data retrieval
    public static final String UPTIMEINFO = "UptimeInfo";
    public static final String FAILURECOUNT = "failureCount";
    public static final String SUCCESSRATE = "successRate";
    public static final String DOWNTIME = "downTime";
    public static final String NORECORDSFOUND = "No Record";
    public static final String FAILUREDETAIL = "FailureDetails";
    public static final String AGGREGATION_CLAUSE = "All";
    public static final String DEFAULT_SEVERITY = "2";
    public static final int TOTAL_UPTIME = 100;
    public static final String DEFAULT_TIME_INTERVAL_IN_DAYS = "1";
    public static final String LOGIN_PAGE= "/login.html";

    //Data retrieval Query
    public static final String GET_UPTIME_INFO_QUERY =
            "SELECT TIMESTAMP, STATUS, SEVERITY FROM LIVE_STATUS WHERE SERVICE=(?) AND TEST=(?) AND TIMESTAMP BETWEEN (?) AND (?) ORDER BY TIMESTAMP DESC";
    public static final String GET_FAILURE_DETAIL_QUERY =
            "SELECT TIMESTAMP, DETAIL, FAILUREINDEX, JIRALINK FROM FAILURE_DETAIL WHERE SERVICE=(?) AND TEST=(?) AND ALARMSTATUS='1' AND TIMESTAMP BETWEEN (?) AND (?) ORDER BY TIMESTAMP ASC";
    public static final String UPDATE_JIRA_URL = "UPDATE FAILURE_DETAIL SET JIRALINK=(?) WHERE FAILUREINDEX IN (";
    public static final String GET_LIVE_STATUS_INDEX = "SELECT SERVICE, TEST, TIMESTAMP FROM FAILURE_DETAIL WHERE FAILUREINDEX IN (";
    public static final String COUNT_SERVER_RECORDS =
            "SELECT Count(*) AS count from LIVE_STATUS WHERE SERVICE=(?) AND SEVERITY <= (?)";
    public static final String SET_FAILURE_TO_TRUE =
            "UPDATE LIVE_STATUS SET STATUS=1 WHERE SERVICE=? AND TEST=? AND TIMESTAMP=?";
    public static final String SET_ALARM_STATUS_FALSE =
            "UPDATE FAILURE_DETAIL SET ALARMSTATUS=0 WHERE FAILUREINDEX IN (";
    public static final String GET_TEST_LIST =
            "SELECT DISTINCT TEST, SEVERITY FROM LIVE_STATUS WHERE SERVICE=(?) AND SEVERITY <= (?) AND TIMESTAMP BETWEEN (?) AND (?) ORDER BY TIMESTAMP ASC";
    public static final String FALSE_FAILURE_REASON_INPUT_QUERY =
            "INSERT INTO `FALSE_FAILURES`(`FAILUREINDEX`, `TIMESTAMP`, `USERID`, `CHANGEINFO`) VALUES (?,?,?,?)";
}
