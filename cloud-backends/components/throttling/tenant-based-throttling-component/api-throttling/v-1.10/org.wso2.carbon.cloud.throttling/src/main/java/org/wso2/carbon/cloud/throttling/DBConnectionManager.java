/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License
 */
package org.wso2.carbon.cloud.throttling;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.cloud.throttling.common.CloudThrottlingException;
import org.wso2.carbon.cloud.throttling.common.Constants;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class managers database connection
 */
public class DBConnectionManager {

    private static final Log LOG = LogFactory.getLog(DBConnectionManager.class);

    /**
     * Utility method to get a new database connection
     *
     * @return Connection
     * @throws org.wso2.carbon.cloud.throttling.common.CloudThrottlingException if failed to get Connection
     */
    public static Connection getDbConnection() throws CloudThrottlingException {
        int tenantId = MultitenantConstants.SUPER_TENANT_ID;
        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        //changing the tenant flow to the supper tenant
        Connection conn = null;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            privilegedCarbonContext.setTenantId(tenantId);
            privilegedCarbonContext.setTenantDomain(tenantDomain);
            //look for cloud mgt data source
            DataSource ds = (DataSource) privilegedCarbonContext.getJNDIContext().lookup(Constants.CLOUD_DB_NAME);
            conn = ds.getConnection();
        } catch (NamingException e) {
            throw new CloudThrottlingException("Error while getting the DataSource for cloud management db " + e);
        } catch (SQLException e) {
            throw new CloudThrottlingException("SQL Error while getting the DataSource for cloud management db", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return conn;
    }

    /**
     * Utility method to close the connection streams.
     *
     * @param preparedStatement PreparedStatement
     * @param connection        Connection
     * @param resultSet         ResultSet
     */
    public static void closeAllConnections(PreparedStatement preparedStatement, Connection connection,
                                           ResultSet resultSet) {
        closeResultSet(resultSet);
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
                LOG.warn("Database error. Could not close database connection. Continuing with " +
                        "others. - " + e.getMessage(), e);
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
                LOG.warn("Database error. Could not close ResultSet  - " + e.getMessage(), e);
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
                LOG.warn("Database error. Could not close PreparedStatement. Continuing with" +
                        " others. - " + e.getMessage(), e);
            }
        }
    }
}
