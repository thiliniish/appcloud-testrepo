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
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.FailureSummary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * {@link UptimeInformationDAOImpl}
 */
public class UptimeInformationDAOImpl implements UptimeInformationDAO {

    private static final Logger logger = LoggerFactory.getLogger(UptimeInformationDAOImpl.class);

    public List<CurrentTaskStatus> getCurrentStatus(String server) throws CloudMonitoringException {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting current task statuses of server : {}", server);
        }
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<CurrentTaskStatus> currentTaskStatuses = new ArrayList<>();
        try {
            connection = DatabaseManager.getConnection();
            statement = connection.prepareStatement(QueryConstants.SELECT_CURRENT_TASK_STATUSES_OF_SERVER);
            statement.setString(1, server);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String serverName = resultSet.getString("SERVER");
                String taskName = resultSet.getString("TASK");
                String status = resultSet.getString("STATUS");
                Timestamp lastUpdated = resultSet.getTimestamp("LAST_UPDATED");
                CurrentTaskStatus currentTaskStatus = new CurrentTaskStatus(serverName, taskName,
                        CurrentTaskStatus.State.valueOf(status), lastUpdated);
                currentTaskStatuses.add(currentTaskStatus);
            }
        } catch (SQLException | CloudMonitoringException e) {
            throw new CloudMonitoringException("Error occurred while getting the current status of the server", e);
        } finally {
            DatabaseManager.closeAllConnections(resultSet, statement, connection);
        }
        return currentTaskStatuses;
    }

    public Map<String, List<CurrentTaskStatus>> getAllCurrentStatuses() throws CloudMonitoringException {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting current task statuses of all servers");
        }
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        Map<String, List<CurrentTaskStatus>> currentServerStatuses = new Hashtable<>();
        try {
            connection = DatabaseManager.getConnection();
            statement = connection.prepareStatement(QueryConstants.SELECT_ALL_CURRENT_TASK_STATUSES);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String serverName = resultSet.getString("SERVER");
                String taskName = resultSet.getString("TASK");
                String status = resultSet.getString("STATUS");
                Timestamp lastUpdated = resultSet.getTimestamp("LAST_UPDATED");
                CurrentTaskStatus currentTaskStatus = new CurrentTaskStatus(serverName, taskName,
                        CurrentTaskStatus.State.valueOf(status), lastUpdated);
                if (currentServerStatuses.containsKey(serverName)) {
                    currentServerStatuses.get(serverName).add(currentTaskStatus);
                } else {
                    List<CurrentTaskStatus> taskStatuses = new ArrayList<>();
                    taskStatuses.add(currentTaskStatus);
                    currentServerStatuses.put(serverName, taskStatuses);
                }
            }
        } catch (SQLException | CloudMonitoringException e) {
            throw new CloudMonitoringException("Error occurred while getting the current status for all the servers",
                    e);
        } finally {
            DatabaseManager.closeAllConnections(resultSet, statement, connection);
        }
        return currentServerStatuses;
    }

    @Override
    public List<DailyServiceStatus> getDailyServiceStatuses(String service, Date from, Date to)
            throws CloudMonitoringException {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting Daily Statuses of service : {} from : {} to : {}", service, from, to);
        }
        List<DailyServiceStatus> dailyServiceStatuses = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = DatabaseManager.getConnection();
            statement = connection.prepareStatement(QueryConstants.SELECT_DAILY_STATUSES_OF_SERVICE_IN_DATE_RANGE);
            statement.setString(1, service);
            statement.setDate(2, new java.sql.Date(from.getTime()));
            statement.setDate(3, new java.sql.Date(to.getTime()));
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Date date = resultSet.getDate("DATE");
                String status = resultSet.getString("STATUS");
                int downTime = resultSet.getInt("DOWN_TIME");
                DailyServiceStatus dailyServiceStatus = new DailyServiceStatus(service, date, downTime,
                        DailyServiceStatus.State.valueOf(status));
                dailyServiceStatuses.add(dailyServiceStatus);
            }
        } catch (SQLException e) {
            throw new CloudMonitoringException("Error occurred while getting the daily status of the server", e);
        } finally {
            DatabaseManager.closeAllConnections(resultSet, statement, connection);
        }
        return dailyServiceStatuses;
    }

    @Override
    public Map<String, List<DailyServiceStatus>> getAllDailyServiceStatuses(Date from, Date to) {
        return null;
    }

    public List<FailureSummary> getFailureSummaries(String server, Date from, Date to) throws CloudMonitoringException {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting failure summary of server : {} from : {} to : {}", server, from, to);
        }
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<FailureSummary> failureSummaries = new ArrayList<>();
        try {
            connection = DatabaseManager.getConnection();
            statement = connection.prepareStatement(QueryConstants.SELECT_FAILURE_SUMMARIES_OF_SERVER_IN_DATE_RANGE);
            statement.setString(1, server);
            statement.setDate(2, new java.sql.Date(from.getTime()));
            statement.setDate(3, new java.sql.Date(to.getTime()));
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String taskName = resultSet.getString("TASK");
                int startId = resultSet.getInt("START_ID");
                int endId = resultSet.getInt("END_ID");
                Date date = resultSet.getDate("DATE");
                Timestamp startTime = resultSet.getTimestamp("START_TIME");
                Timestamp endTime = resultSet.getTimestamp("END_TIME");
                int downTime = resultSet.getInt("DOWN_TIME");
                FailureSummary failureSummary = new FailureSummary(server, taskName, startId, date,
                        startTime.getTime());
                failureSummary.setEndID(endId);
                failureSummary.setEndTime(endTime.getTime());
                failureSummary.setDownTime(downTime);
                failureSummaries.add(failureSummary);
            }
        } catch (SQLException | CloudMonitoringException e) {
            throw new CloudMonitoringException("Error occurred while getting the daily status of the server", e);
        } finally {
            DatabaseManager.closeAllConnections(resultSet, statement, connection);
        }
        return failureSummaries;
    }

    @Override
    public Map<String, List<FailureSummary>> getAllFailureSummaries(Date from, Date to) {
        return null;
    }

}
