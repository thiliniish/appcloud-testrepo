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

package org.wso2.cloud.heartbeat.monitoring.ui.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.cloud.heartbeat.monitoring.ui.configuration.Node;

import java.sql.*;
import java.util.List;

/**
 * Class used to access database and execute mysql queries against a datasource
 */
public class DataAccessManager {

    private static final Log log = LogFactory.getLog(DataAccessManager.class);
    private static final String DRIVER_CLASS_NAME =
            ConfigReader.getInstance().getDataSourceFromNode().getProperty("database_driver");
    private Connection connection = null;
    private PreparedStatement preparedStatement = null;

    public DataAccessManager(Node dataSource) throws HeartbeatException {

        String userName = dataSource.getProperty("user");
        String password = dataSource.getProperty("password");
        String dbUrl = "jdbc:mysql://" + dataSource.getProperty("host_name") + "/" +
                       dataSource.getProperty("database_name");

        try {
            Class.forName(DRIVER_CLASS_NAME).newInstance();
            connection = DriverManager.getConnection(dbUrl, userName, password);
        } catch (ClassNotFoundException e) {
            log.error("Heartbeat - Monitor - ClassNotFoundException thrown while initializing data access: ", e);
            throw new HeartbeatException(
                    "Heartbeat - Monitor - ClassNotFoundException thrown while initializing data access: " + e);
        } catch (SQLException e) {
            log.error("Heartbeat - Monitor - SQLException thrown while initializing data access: ", e);
            throw new HeartbeatException(
                    "Heartbeat - Monitor - SQLException thrown while initializing data access: " + e);
        } catch (InstantiationException e) {
            log.error("Heartbeat - Monitor - InstantiationException thrown while initializing data access: ", e);
            throw new HeartbeatException(
                    "Heartbeat - Monitor - InstantiationException thrown while initializing data access: " + e);
        } catch (IllegalAccessException e) {
            log.error("Heartbeat - Monitor - IllegalAccessException thrown while initializing data access: ", e);
            throw new HeartbeatException(
                    "Heartbeat - Monitor - IllegalAccessException thrown while initializing data access: " + e);
        }
    }

    /**
     * Method to execute select queries
     *
     * @param query      String query to be executed
     * @param parameters List of parameters to set on query
     * @return sql ResultSet
     * @throws SQLException
     */

    public ResultSet runQuery(String query, List<String> parameters) throws SQLException {
        preparedStatement = connection.prepareStatement(query);
        for (int i = 0; i < parameters.size(); i++) {
            preparedStatement.setString(i + 1, parameters.get(i));
        }
        return preparedStatement.executeQuery();
    }

    /**
     * Method to execute update queries
     *
     * @param query      String query to be executed
     * @param parameters List of parameters to set on query
     * @return sql ResultSet
     * @throws SQLException
     */
    public int updateQuery(String query, List<String> parameters) throws SQLException {
        preparedStatement = connection.prepareStatement(query);
        for (int i = 0; i < parameters.size(); i++) {
            preparedStatement.setString(i + 1, parameters.get(i));
        }
        return preparedStatement.executeUpdate();
    }

    /**
     * Closes sql connection
     *
     * @throws SQLException
     */
    public void closeConnection() throws SQLException {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
                preparedStatement = null;
            } catch (SQLException e) {
                log.error("SQL Exception while closing prepared statement" + e);
            }
        }
        try {
            if (connection != null && !connection.isClosed()) {
                if (!connection.getAutoCommit()) {
                    connection.commit();
                    connection.setAutoCommit(true);
                }
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            log.error("SQL Exception while closing connection" + e);
        }
    }

    public void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                log.error("SQL Exception while closing ResultSet" + e);
            }
        }
    }
}
