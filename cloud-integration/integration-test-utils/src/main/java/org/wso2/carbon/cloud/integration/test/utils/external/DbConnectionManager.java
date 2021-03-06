/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.cloud.integration.test.utils.external;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.integration.test.utils.CloudIntegrationConstants;
import org.wso2.carbon.cloud.integration.test.utils.CloudIntegrationTestUtils;

import java.sql.*;
import java.util.List;

/**
 * Used to create and handle the database queries
 */
public class DbConnectionManager {
    private static final Log log = LogFactory.getLog(DbConnectionManager.class);

    private Connection connection;

    public DbConnectionManager(String key) {
        try {
            String jdbcDriver = CloudIntegrationTestUtils.getPropertyValue(
                    CloudIntegrationConstants.MYSQL_DRIVER_CLASS_NAME
                            .replace(CloudIntegrationConstants.MYSQL_REPLACE, key));
            String dbUrl = CloudIntegrationTestUtils.getPropertyValue(
                    CloudIntegrationConstants.MYSQL_DATA_SOURCE_URL
                            .replace(CloudIntegrationConstants.MYSQL_REPLACE, key));
            String dbUserName = CloudIntegrationTestUtils.getPropertyValue(
                    CloudIntegrationConstants.MYSQL_USERNAME
                            .replace(CloudIntegrationConstants.MYSQL_REPLACE, key));
            String dbPassword = CloudIntegrationTestUtils.getPropertyValue(
                    CloudIntegrationConstants.MYSQL_PASSWORD
                            .replace(CloudIntegrationConstants.MYSQL_REPLACE, key));

            Class.forName(jdbcDriver).newInstance();

            connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
        } catch (SQLException e) {
            log.error("SQLException thrown while getting the connection: ", e);
        } catch (ClassNotFoundException e) {
            log.error(
                    "ClassNotFoundException thrown while getting the connection: Please add the mysql" +
                    " connector to lib folder: ", e);
        } catch (InstantiationException e) {
            log.error("InstantiationException thrown while getting the connection: ", e);
        } catch (IllegalAccessException e) {
            log.error("IllegalAccessException thrown while getting the connection: ", e);
        }

    }

    /**
     * returns the result set after executing the query
     *
     * @param query      query with values to be replaced
     * @param parameters List<String> of parameter values to use within the query
     * @return java.sql.Resulset
     * @throws SQLException
     */
    public ResultSet runQuery(String query, List<String> parameters) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        for (int i = 0; i < parameters.size(); i++) {
            preparedStatement.setString(i + 1, parameters.get(i));
        }
        return preparedStatement.executeQuery();
    }

    /**
     * This method can be used for mysql insert/update/delete operations.
     *
     * @param query      query which needs to be executed.
     * @param parameters List<String> of parameter values which need to be replaced within the query.
     * @throws SQLException
     */
    public void executeUpdate(String query, List<String> parameters) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        for (int i = 0; i < parameters.size(); i++) {
            preparedStatement.setString(i + 1, parameters.get(i));
        }
        preparedStatement.executeUpdate();
    }

    /**
     * After the connection is used closing the connection
     *
     * @throws SQLException
     */
    public void closeConnection() throws SQLException {
        connection.close();
    }

}

