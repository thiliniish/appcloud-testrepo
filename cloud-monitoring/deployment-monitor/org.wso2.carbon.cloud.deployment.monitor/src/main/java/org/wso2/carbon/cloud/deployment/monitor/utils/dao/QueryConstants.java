package org.wso2.carbon.cloud.deployment.monitor.utils.dao;

/**
 * SQL Queries
 */
public class QueryConstants {

    public static final String ADD_SUCCESS_RECORD = "INSERT INTO SUCCESS_RECORD "
            + "(DATETIME,TASK,SERVER) VALUES(?,?,?)";

    public static final String ADD_FAILURE_RECORD = "INSERT INTO FAILURE_RECORD "
            + "(DATETIME,TASK,SERVER,ERROR) VALUES(?,?,?,?)";

    public static final String ADD_FAILURE_SUMMARY = "INSERT INTO FAILURE_SUMMARY "
            + "(TASK,SERVER,START_ID,END_ID,DATE,START_TIME,END_TIME,DOWN_TIME) VALUES(?,?,?,?,?,?,?,?)";

    public static final String UPDATE_LIVE_STATUS = "INSERT INTO LIVE_STATUS (SERVER,TASK,STATUS) VALUES (?,?,?) "
            + "ON DUPLICATE KEY UPDATE STATUS=?,LAST_UPDATED=?";

    public static final String UPDATE_LIVE_STATUS_FOR_MAINTENANCE = "UPDATE LIVE_STATUS SET STATUS=?, LAST_UPDATED=? "
            + "WHERE SERVER=? AND TASK=?";

    public static final String UPDATE_LIVE_STATUS_FOR_MAINTENANCE_FOR_SERVER = "UPDATE LIVE_STATUS SET STATUS=?, "
            + "LAST_UPDATED=? WHERE SERVER=?";

    public static final String ADD_MAINTENANCE_SUMMARY = "INSERT INTO MAINTENANCE_SUMMARY "
            + "(SERVER, TASK, DATE, STATUS, START_TIME) VALUES(?,?,?,?,?)";

    public static final String SELECT_MAINTENANCE_SUMMARY = "SELECT ID, START_TIME FROM MAINTENANCE_SUMMARY "
            + "WHERE SERVER=? AND TASK=? AND STATUS=?";

    public static final String UPDATE_MAINTENANCE_SUMMARY = "UPDATE MAINTENANCE_SUMMARY "
            + "SET STATUS=?, END_TIME=?, DOWN_TIME=? WHERE ID=?";
}
