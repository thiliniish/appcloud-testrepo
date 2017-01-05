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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.cloud.deployment.monitor.utils.CloudMonitoringException;
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.CurrentTaskStatus;
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.DailyServiceStatus;
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.FailureRecord;
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.FailureSummary;
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.SuccessRecord;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;

/**
 * StatusReportingDAOImpl
 */
public class StatusReportingDAOImpl implements StatusReportingDAO {

    private static final Logger logger = LoggerFactory.getLogger(StatusReportingDAOImpl.class);
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void addSuccessRecord(SuccessRecord successRecord) throws CloudMonitoringException {
        if (logger.isDebugEnabled()) {
            logger.debug("Adding Success Record {}-{}", successRecord.getServer(), successRecord.getTaskName());
        }
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DatabaseManager.getConnection();
            statement = connection.prepareStatement(QueryConstants.ADD_SUCCESS_RECORD);
            statement.setString(1, successRecord.getTaskName());
            statement.setString(2, successRecord.getServer());
            statement.setString(3, sdf.format(successRecord.getTimestamp()));
            statement.executeUpdate();

        } catch (SQLException e) {
            throw  new CloudMonitoringException("Error occurred while adding Success Record", e);
        } finally {
            DatabaseManager.closeAllConnections(null, statement, connection);
        }
    }

    @Override
    public int addFailureRecord(FailureRecord failureRecord) throws CloudMonitoringException {
        if (logger.isDebugEnabled()) {
            logger.debug("Adding Failure Record {}-{}", failureRecord.getServer(), failureRecord.getTaskName());
        }
        Connection connection = null;
        PreparedStatement insertStmnt = null;
        ResultSet generatedKeysRS = null;
        try {
            connection = DatabaseManager.getConnection();
            insertStmnt = connection.prepareStatement(QueryConstants.ADD_FAILURE_RECORD,
                    Statement.RETURN_GENERATED_KEYS);
            insertStmnt.setString(1, failureRecord.getTaskName());
            insertStmnt.setString(2, failureRecord.getServer());
            insertStmnt.setString(3, sdf.format(failureRecord.getTimestamp()));
            insertStmnt.setString(4, failureRecord.getError());
            insertStmnt.executeUpdate();

            generatedKeysRS = insertStmnt.getGeneratedKeys();
            if (generatedKeysRS.next()) {
                return generatedKeysRS.getInt(1);
            }
        } catch (SQLException e) {
            throw  new CloudMonitoringException("Error occurred while adding Failure Record", e);
        } finally {
            DatabaseManager.closeAllConnections(generatedKeysRS, insertStmnt, connection);
        }
        return 0;
    }

    @Override
    public void addFailureSummary(FailureSummary failureSummary) throws CloudMonitoringException {
        if (logger.isDebugEnabled()) {
            logger.debug("Adding Failure Summary {}-{}: Downtime {}s", failureSummary.getServer(),
                    failureSummary.getTaskName(), failureSummary.getDownTime() / 1000);
        }
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DatabaseManager.getConnection();
            statement = connection.prepareStatement(QueryConstants.ADD_FAILURE_SUMMARY);
            statement.setString(1, failureSummary.getTaskName());
            statement.setString(2, failureSummary.getServer());
            statement.setInt(3, failureSummary.getStartID());
            statement.setInt(4, failureSummary.getEndID());
            statement.setDate(5, new Date(failureSummary.getDate().getTime()));
            statement.setString(6, sdf.format(failureSummary.getStartTime()));
            statement.setString(7, sdf.format(failureSummary.getEndTime()));
            statement.setInt(8, (int) (failureSummary.getDownTime() / 1000));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw  new CloudMonitoringException("Error occurred while adding Failure Summary", e);
        } finally {
            DatabaseManager.closeAllConnections(null, statement, connection);
        }
    }

    @Override
    public void updateCurrentTaskStatus(CurrentTaskStatus currentTaskStatus) throws CloudMonitoringException {
        if (logger.isDebugEnabled()) {
            logger.debug("Updating Live State {}-{} : State : {}", currentTaskStatus.getServer(),
                    currentTaskStatus.getTaskName(), currentTaskStatus.getState());
        }
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DatabaseManager.getConnection();
            statement = connection.prepareStatement(QueryConstants.UPDATE_CURRENT_TASK_STATUS);
            statement.setString(1, currentTaskStatus.getTaskName());
            statement.setString(2, currentTaskStatus.getServer());
            statement.setString(3, currentTaskStatus.getState().toString());
            statement.setString(4, currentTaskStatus.getState().toString());
            statement.setString(5, sdf.format(currentTaskStatus.getLastUpdated().getTime()));
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while adding Live State", e);
        }  finally {
            DatabaseManager.closeAllConnections(null, statement, connection);
        }
    }

    public void updateCurrentTaskStatusForMaintenance(String serverName, String task, CurrentTaskStatus.State state)
            throws CloudMonitoringException {
        if (logger.isDebugEnabled()) {
            logger.debug("Updating Live State for maintenance in : {}", serverName);
        }
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DatabaseManager.getConnection();
            if (task != null) {
                statement = connection.prepareStatement(QueryConstants.UPDATE_CURRENT_TASK_STATUS_FOR_MAINTENANCE);
                statement.setString(1, state.name());
                statement.setString(2, sdf.format(new Date(System.currentTimeMillis())));
                statement.setString(3, serverName);
                statement.setString(4, task);
                statement.executeUpdate();
            } else {
                statement = connection
                        .prepareStatement(QueryConstants.UPDATE_CURRENT_TASK_STATUS_FOR_MAINTENANCE_FOR_SERVER);
                statement.setString(1, state.name());
                statement.setString(2, sdf.format(new Date(System.currentTimeMillis())));
                statement.setString(3, serverName);
                statement.executeUpdate();
            }

        } catch (SQLException e) {
            logger.error("Error occurred while updating Live State", e);
        } finally {
            DatabaseManager.closeAllConnections(null, statement, connection);
        }
    }

    public void addMaintenanceSummary(String serverName, String task) throws CloudMonitoringException {
        if (logger.isDebugEnabled()) {
            logger.debug("Adding summary for maintenance in : {} - {}", serverName, task);
        }
        Date date = new Date(System.currentTimeMillis());
        if (task == null) {
            task = "ALL";
        }
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DatabaseManager.getConnection();
            statement = connection.prepareStatement(QueryConstants.ADD_MAINTENANCE_SUMMARY);
            statement.setString(1, serverName);
            statement.setString(2, task);
            statement.setDate(3, date);
            statement.setString(4, "INPROGRESS");
            statement.setString(5, sdf.format(date));
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while adding Maintenance Summary", e);
        } finally {
            DatabaseManager.closeAllConnections(null, statement, connection);
        }
    }

    public void updateMaintenanceSummary(String serverName, String task) throws CloudMonitoringException {
        if (logger.isDebugEnabled()) {
            logger.debug("Adding summary for maintenance in : {} - {}", serverName, task);
        }
        if (task == null) {
            task = "ALL";
        }
        Connection connection = null;
        PreparedStatement selectStatement = null;
        PreparedStatement updateStatement = null;
        ResultSet resultSet = null;
        try {
            connection = DatabaseManager.getConnection();
            selectStatement = connection.prepareStatement(QueryConstants.SELECT_MAINTENANCE_SUMMARY);
            selectStatement.setString(1, serverName);
            selectStatement.setString(2, task);
            selectStatement.setString(3, "INPROGRESS");
            resultSet = selectStatement.executeQuery();
            int id;
            long startTime;
            if (resultSet.next()) {
                id = resultSet.getInt(1);
                startTime = resultSet.getTimestamp(2).getTime();
            } else {
                logger.warn("Cannot find a Maintenance summary for : {} - {} in in progress state", serverName, task);
                return;
            }

            Date endTime = new Date(System.currentTimeMillis());
            long downTime = endTime.getTime() - startTime;

            updateStatement = connection.prepareStatement(QueryConstants.UPDATE_MAINTENANCE_SUMMARY);
            updateStatement.setString(1, "COMPLETED");
            updateStatement.setString(2, sdf.format(endTime));
            updateStatement.setInt(3, (int) (downTime / 1000));
            updateStatement.setInt(4, id);
            updateStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while updating Maintenance Summary", e);
        } finally {
            DatabaseManager.closeAllConnections(resultSet, selectStatement, connection);
            DatabaseManager.closeAllConnections(null, updateStatement, null);
        }
    }

    public void addDailyServiceStatus(DailyServiceStatus dailyServiceStatus) throws CloudMonitoringException {
        if (logger.isDebugEnabled()) {
            logger.debug("Adding Daily Service Status for server : {}", dailyServiceStatus.getService());
        }
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DatabaseManager.getConnection();
            statement = connection.prepareStatement(QueryConstants.ADD_DAILY_SERVICE_STATUS);
            statement.setString(1, dailyServiceStatus.getService());
            statement.setDate(2, new Date(dailyServiceStatus.getDate().getTime()));
            statement.setString(3, dailyServiceStatus.getDailyServiceStatus());
            statement.setInt(4, dailyServiceStatus.getServiceDowntime());
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while adding Daily Service Status for server : {}",
                    dailyServiceStatus.getService(), e);
        } finally {
            DatabaseManager.closeAllConnections(null, statement, connection);
        }
    }
}
