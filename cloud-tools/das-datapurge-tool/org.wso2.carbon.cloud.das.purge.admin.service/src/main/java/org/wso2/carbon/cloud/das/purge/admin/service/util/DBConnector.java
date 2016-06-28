/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.cloud.das.purge.admin.service.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This DB Connector class will create the DB Connection to the cloudmgt database and get the trial tenant list
 */
public class DBConnector {
    private static final Log log = LogFactory.getLog(DBConnector.class);

    private static volatile DataSource dataSource = null;

    private DBConnector() {
    }

    /**
     * Initializes the data source
     *
     * @throws NamingException if an error occurs while loading DB configuration
     */
    private static void initialize() throws NamingException {
        synchronized (DBConnector.class) {
            if (dataSource == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Initializing data source for : " + DASPurgeToolConstants.CLOUD_DATASOURCE);
                }
                Context ctx = new InitialContext();
                dataSource = (DataSource) ctx.lookup(DASPurgeToolConstants.CLOUD_DATASOURCE);
            }
        }
    }

    /**
     * Utility method to get a new database connection
     *
     * @return Connection
     * @throws NamingException, SQLException if failed to get Connection
     */
    public static Connection getConnection() throws NamingException, SQLException {
        if (dataSource == null) {
            initialize();
        }
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new SQLException("Error when getting a database connection object from the Cloud Mgt data source.",
                    e);
        }
    }

    /**
     * Get all tenants except the paid tenants from cloud-mgt database
     *
     * @return
     * @throws NamingException
     * @throws SQLException
     */
    public static List<String> getTrialTenantDomains() throws NamingException, SQLException {
        Connection connection = getConnection();
        PreparedStatement preparedStatement = null;
        String query = DASPurgeToolConstants.SQL_SELECT_NOT_PAID_TENANTS;
        ResultSet resultSet = null;
        List<String> allTenantDomains = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                allTenantDomains.add(resultSet.getString(1));
            }
            return allTenantDomains;
        } catch (SQLException e) {
            String message = "Error while accessing database. Query - " + query + e.getErrorCode();
            log.error(message, e);
            throw new SQLException(message, e);
        } finally {
            closeAllConnections(connection, preparedStatement, resultSet);
        }
    }

    /**
     * Utility method to close the connection streams.
     *
     * @param preparedStatement PreparedStatement
     * @param connection        Connection
     * @param resultSet         ResultSet
     */
    public static void closeAllConnections(Connection connection, PreparedStatement preparedStatement,
            ResultSet resultSet) {
        closeConnection(connection);
        closeResultSet(resultSet);
        closeStatement(preparedStatement);
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
                log.warn("Error occurred while closing the database connection.", e);
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
                log.warn("Error occurred while closing the result set.", e);
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
                log.warn("Error occurred while closing the prepared statement.", e);
            }
        }
    }
}
