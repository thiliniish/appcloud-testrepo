/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.cloud.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * This class creates the connection to the cloudmgt database
 */
public class CloudMgtDBConnectionManager {
    private static final Log LOG = LogFactory.getLog(CloudMgtDBConnectionManager.class);

    /**
     * Utility method to get a new database connection
     *
     * @return Connection
     * @throws CloudMgtException if it fails to get the db connection
     */
    public static Connection getDbConnection() throws CloudMgtException {
        int tenantId = MultitenantConstants.SUPER_TENANT_ID;
        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        //changing the tenant flow to the supper tenant
        Connection conn = null;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            privilegedCarbonContext.setTenantId(tenantId);
            privilegedCarbonContext.setTenantDomain(tenantDomain);

            //get the name of the cloud management data source
            String cloudDBName = getCloudDatasourceName();
            cloudDBName = CloudMgtConstants.CLOUD_DATASOURCE_PREFIX + File.separator + cloudDBName;
            if (cloudDBName != null) {
                DataSource ds = (DataSource) privilegedCarbonContext.getJNDIContext().lookup(cloudDBName);
                conn = ds.getConnection();
            } else {
                throw new CloudMgtException("Unable to initiate the cloud management dataSource");
            }
        } catch (NamingException e) {
            throw new CloudMgtException(
                    "An error occurred while getting the dataSource for the cloud management database " + e);
        } catch (SQLException e) {
            throw new CloudMgtException(
                    "An SQL error occurred while getting the dataSource for cloud management database", e);
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
                LOG.warn("A database error occurred. Unable to close the database connections. Continuing with " +
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
                LOG.warn("A database error occurred. Unable to close the PreparedStatement. Continuing with" +
                         " others. - " + e.getMessage(), e);
            }
        }
    }

    /**
     * This method reads the cloudmgt dataSource name from the cloud-mgt.xml
     *
     * @return the name of the cloudmgt dataSource
     * @throws CloudMgtException
     */
    private static String getCloudDatasourceName() throws CloudMgtException {
        String fileLocation = CarbonUtils.getCarbonConfigDirPath() +
                              File.separator + CloudMgtConstants.CONFIG_FOLDER +
                              File.separator + CloudMgtConstants.CONFIG_FILE_NAME;
        String cloudDatasource = null;
        try {
            CloudMgtConfiguration configuration =
                    new CloudMgtConfigurationBuilder(fileLocation).buildCloudMgtConfiguration();
            if (configuration.getFirstProperty(CloudMgtConstants.CLOUD_DB_PROPERTY) != null) {
                cloudDatasource = configuration.getFirstProperty(CloudMgtConstants.CLOUD_DB_PROPERTY);
            } else {
                LOG.error("Unable to find the cloudmgt datasource name");
                throw new CloudMgtException("Unable to find the cloudmgt datasource name");
            }

        } catch (CloudMgtException e) {
            LOG.error("An error occurred while reading the configuration file " + fileLocation);
            throw new CloudMgtException("Unable to initiate the cloudmgt datasource");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("The cloudmgt datasouce name is " + cloudDatasource);
        }
        return cloudDatasource;
    }
}
