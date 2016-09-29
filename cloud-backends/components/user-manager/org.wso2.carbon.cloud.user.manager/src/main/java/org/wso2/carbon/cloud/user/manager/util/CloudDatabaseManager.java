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

package org.wso2.carbon.cloud.user.manager.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;


/**
 * Data base manager for CloudMgt data source
 */
public class CloudDatabaseManager {
    private static final Log log = LogFactory.getLog(CloudDatabaseManager.class);

    private static volatile DataSource dataSource = null;

    private CloudDatabaseManager() {
    }

    /**
     * Initializes the data source
     *
     * @throws CloudUserManagerException if an error occurs while loading DB configuration
     */
    private static void initialize() throws CloudUserManagerException {
        synchronized (CloudDatabaseManager.class) {
            if (dataSource == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Initializing data source for : " + CloudUserManagerConstants.CLOUD_MGT_DATASOURCE);
                }
                try {
                    Context ctx = new InitialContext();
                    dataSource = (DataSource) ctx.lookup(CloudUserManagerConstants.CLOUD_MGT_DATASOURCE);
                } catch (NamingException e) {
                    throw new CloudUserManagerException(
                            "Error while looking up the data source: " + CloudUserManagerConstants.CLOUD_MGT_DATASOURCE,
                            e);
                }

            }
        }
    }

    /**
     * Utility method to get a new database connection
     *
     * @return Connection
     * @throws CloudUserManagerException if failed to get Connection
     */
    public static Connection getConnection() throws CloudUserManagerException {
        if (dataSource == null) {
            initialize();
        }
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new CloudUserManagerException(
                    "Error when getting a database connection object from the Cloud Mgt data source.", e);
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
