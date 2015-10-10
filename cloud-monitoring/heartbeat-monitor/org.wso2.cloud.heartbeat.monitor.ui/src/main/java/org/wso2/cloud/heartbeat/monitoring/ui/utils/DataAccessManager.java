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

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Class used to access database and execute mysql queries against a datasource
 */
public class DataAccessManager {

    private static final Log log = LogFactory.getLog(DataAccessManager.class);

    private static volatile DataAccessManager instance;
    private BasicDataSource source;

    private static final String DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";

    /**
     * creates database connection
     */
    private DataAccessManager() {
        this.source = setDbConnection();
    }

    /**
     * Returns database connection instance, if database connection instance is null creates
     * a database connection
     *
     * @return Database connection
     */
    public static DataAccessManager getInstance() {
        if (instance == null) {
            synchronized (DataAccessManager.class) {
                if (instance == null) {
                    instance = new DataAccessManager();
                }
            }
        }
        return instance;
    }

    /**
     * Initializes MySQL database connection
     *
     * @return java.sql.Connection
     */
    private BasicDataSource setDbConnection() {
        ConfigReader configObject = ConfigReader.getInstance();
        source = new BasicDataSource();
        source.setDriverClassName(DRIVER_CLASS_NAME);
        source.setUsername(configObject.getDataSourceFromNode().getProperty("user"));
        source.setPassword(configObject.getDataSourceFromNode().getProperty("password"));
        source.setUrl("jdbc:mysql://" + configObject.getDataSourceFromNode().getProperty("host_name") + "/" +
                      configObject.getDataSourceFromNode().getProperty("database_name"));
        return source;
    }

    /**
     * Making the prepared statement to run the query
     *
     * @param con        prepared database connection
     * @param query      query to be executed
     * @param parameters parameter to be attached
     * @return Prepared statement ready to execute
     * @throws SQLException
     */
    public PreparedStatement prepareStatement(Connection con, String query, List<String> parameters)
            throws SQLException {
        PreparedStatement preparedStatement = con.prepareStatement(query);
        for (int i = 0; i < parameters.size(); i++) {
            preparedStatement.setString(i + 1, parameters.get(i));
        }
        return preparedStatement;

    }

    /**
     * Returns MySQL database connection
     *
     * @return Database connection from connection pool
     */
    public Connection getConnection() throws SQLException {
        return source.getConnection();
    }

    /**
     * Closing the connection with executeQuery type query statements
     *
     * @param connection Database connection
     * @param statement statament to close
     * @param resultSet result set to close
     * @throws HeartbeatException
     */
    public void closeConnection(Connection connection, PreparedStatement statement, ResultSet resultSet)
            throws HeartbeatException {
        try {
            if (resultSet != null)
                resultSet.close();
            closeConnectionAndStatement(connection, statement);
        } catch (SQLException e) {
            throw new HeartbeatException("SQL Exception occurred while Closing connection " + e);
        }
    }

    /**
     * Closing the connection with executeUpdate type query statements
     *
     * @param connection Database connection
     * @param statement statament to close
     * @throws HeartbeatException
     */
    public void closeConnectionAndStatement(Connection connection, PreparedStatement statement) throws SQLException {
        if (statement != null)
            statement.close();
        if (connection != null)
            connection.close();
    }

}
