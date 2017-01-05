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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.deployment.monitor.utils.CloudMonitoringConstants;
import org.wso2.carbon.cloud.deployment.monitor.utils.CloudMonitoringException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Class for Managing the database connections
 */
public class DatabaseManager {
    private static final Log LOG = LogFactory.getLog(DatabaseManager.class);

    private DatabaseManager() {
    }

    private static volatile DataSource dataSource = null;

    /**
     * Utility method to get a new database connection
     *
     * @return Connection
     * @throws CloudMonitoringException if it fails to get the db connection
     */
    public static Connection getConnection() throws CloudMonitoringException {
        Connection conn;
        if (dataSource == null) {
            initializeDatasource();
        }
        try {
            conn = dataSource.getConnection();
        } catch (SQLException e) {
            throw new CloudMonitoringException(
                    "Error when getting a database connection object from the Cloud Mgt data source.", e);
        }

        if (conn != null) {
            return conn;
        } else {
            throw new CloudMonitoringException(
                    "An error when getting a database connection object from the Cloud Mgt data source");
        }
    }

    /**
     * Initializes the data source
     *
     * @throws CloudMonitoringException if an error occurs while loading DB configuration
     */
    private static void initializeDatasource() throws CloudMonitoringException {
        try {
            if (dataSource == null) {
                synchronized (StatusReportingDAOImpl.class) {
                    if (dataSource == null) {
                        Context initContext = new InitialContext();
                        Context webContext = (Context) initContext.lookup("java:/comp/env");
                        dataSource = (DataSource) webContext
                                .lookup("jdbc/" + CloudMonitoringConstants.DEFAULT_DATASOURCE_NAME);
                    }
                }
            }
        } catch (NamingException e) {
            String msg = "Error occurred while trying to look up the datasource : "
                    + CloudMonitoringConstants.DEFAULT_DATASOURCE_NAME;
            throw new CloudMonitoringException(msg, e);
        }
    }

    /**
     * Utility method to close the connection streams.
     *
     * @param preparedStatement PreparedStatement
     * @param connection        Connection
     * @param resultSet         ResultSet
     */
    public static void closeAllConnections(ResultSet resultSet, PreparedStatement preparedStatement,
            Connection connection) {
        closeResultSet(resultSet);
        closeStatement(preparedStatement);
        closeConnection(connection);
    }

    /**
     * Method used to close connection stream and prepared statement in database upadate scenario
     *
     * @param preparedStatement PreparedStatement
     * @param connection        Connection
     */
    public static void closePSAndConnection(PreparedStatement preparedStatement, Connection connection) {
        closeStatement(preparedStatement);
        closeConnection(connection);
    }

    /**
     * Close Connection
     *
     * @param dbConnection Connection
     */
    private static void closeConnection(Connection dbConnection) {
        if (dbConnection != null) {
            try {
                dbConnection.close();
            } catch (SQLException e) {
                LOG.warn("A database error occurred. Unable to close the database connections. Continuing with "
                        + "others. - " + e.getMessage(), e);
            }
        }
    }

    /**
     * Close ResultSet
     *
     * @param resultSet ResultSet
     */
    private static void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                LOG.warn("A database error occurred. Unable to close the ResultSet  - " + e.getMessage(), e);
            }
        }
    }

    /**
     * Close PreparedStatement
     *
     * @param preparedStatement PreparedStatement
     */
    private static void closeStatement(PreparedStatement preparedStatement) {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                LOG.warn("A database error occurred. Unable to close the PreparedStatement. Continuing with"
                        + " others. - " + e.getMessage(), e);
            }
        }
    }

}
