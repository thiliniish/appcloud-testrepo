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
 * under the License.
 */

package org.wso2.carbon.cloud.tenantdeletion.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.tenantdeletion.conf.ConfigurationsType;
import org.wso2.carbon.cloud.tenantdeletion.constants.DeletionConstants;
import org.wso2.carbon.cloud.tenantdeletion.reader.ConfigReader;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * The class used to create conf database connection
 */
public class DataConnectionManager {
    private static final Log LOG = LogFactory.getLog(DataConnectionManager.class);
    private static final DataConnectionManager instance = new DataConnectionManager();
    private Connection connection;

    /**
     * Returns database connection instance
     *
     * @return Database Connection
     */
    public static DataConnectionManager getInstance() {
        return instance;
    }

    /**
     * Returns MySQL database connection for CLOUD_MGT Database
     *
     * @return Database connection
     */
    public Connection getCloudMgtDbConnection() {
        return getDbConnection(DeletionConstants.CLOUD_MGT);
    }

    /**
     * Returns MySQL database connection for USER_MGT Database
     *
     * @return Database connection
     */

    public Connection getUserMgtDbConnection() {
        return getDbConnection(DeletionConstants.USER_MGT);
    }

    /**
     * Returns MySQL database connection
     *
     * @return Database connection
     */

    public Connection getDbConnection(String dataSource) {
        String datasourceName;
        ConfigReader configReader = ConfigReader.getInstance();
        ConfigurationsType configuration = configReader.getConfiguration();
        if (configuration != null) {
            if (DeletionConstants.USER_MGT.equals(dataSource)) {
                datasourceName = configuration.getDatasources().getUserMgtDatasource();
            } else {
                datasourceName = configuration.getDatasources().getCloudMgtDatasource();
            }
            lookUpDataSource(datasourceName);
            return connection;
        } else {
            return null;
        }
    }

    /**
     * Initialize DataSource and set connection
     *
     * @param dataSourceName dataSource name
     */
    public void lookUpDataSource(String dataSourceName) {
        try {
            Hashtable<String, String> environment = new Hashtable<>();
            environment.put(DeletionConstants.JAVA_NAMING_FACTORY_INITIAL_KEY,
                            DeletionConstants.JAVA_NAMING_FACTORY_INITIAL_VALUE);
            Context initContext = new InitialContext(environment);
            DataSource dataSource = (DataSource) initContext.lookup(dataSourceName);
            if (dataSource != null) {
                connection = dataSource.getConnection();
            } else {
                String msg = "Cannot Find conf data source with the name";
                LOG.error(msg);
            }
        } catch (NamingException e) {
            LOG.error("Naming Exception has occurred while initializing the context", e);
        } catch (SQLException e) {
            LOG.error("SQL Exception occurred while getting connection", e);
        }
    }

    /**
     * Closing database connection
     */
    public void closeDbConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOG.error("SQL Exception occurred while Closing connection ", e);
            }
        }
    }
}
