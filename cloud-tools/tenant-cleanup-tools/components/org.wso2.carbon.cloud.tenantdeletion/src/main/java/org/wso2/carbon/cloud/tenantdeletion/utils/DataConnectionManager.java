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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * The class used to create conf database connection
 */
public class DataConnectionManager {
    private static final Log LOG = LogFactory.getLog(DataConnectionManager.class);
    private static volatile Map<String, DataSource> dataSource = null;

    private DataConnectionManager() {
    }

    /**
     * Returns MySQL database connection for CLOUD_MGT Database
     *
     * @return Database connection
     */
    public static Connection getCloudMgtDbConnection() throws TenantDeletionException {
        return getConnection(DeletionConstants.CLOUD_MGT);
    }

    /**
     * Returns MySQL database connection for USER_MGT Database
     *
     * @return Database connection
     */

    public static Connection getUserMgtDbConnection() throws TenantDeletionException {
        return getConnection(DeletionConstants.USER_MGT);
    }

    /**
     * @param dataSourceType
     * @return
     * @throws TenantDeletionException
     */
    private static Connection getConnection(String dataSourceType) throws TenantDeletionException {
        synchronized (DataConnectionManager.class) {
            String datasourceName;
            ConfigReader configReader = ConfigReader.getInstance();
            ConfigurationsType configuration = configReader.getConfiguration();
            if (DeletionConstants.USER_MGT.equals(dataSourceType)) {
                datasourceName = configuration.getDatasources().getUserMgtDatasource();
            } else {
                datasourceName = configuration.getDatasources().getCloudMgtDatasource();
            }
            if (dataSource == null) {
                dataSource = new HashMap<>();
                setDataSource(dataSourceType, datasourceName);
            }
            if (!dataSource.containsKey(dataSourceType) || dataSource.isEmpty()) {
                setDataSource(dataSourceType, datasourceName);
            }
            try {
                return dataSource.get(dataSourceType).getConnection();
            } catch (SQLException e) {
                throw new TenantDeletionException(
                        "Error while looking up the data source: " + DeletionConstants.USER_MGT, e);
            }
        }

    }

    /**
     * Sets datasources in to the DataSource Map type
     *
     * @param dataSourceType Type of Data source
     * @param dataSourceName data source jndi name
     */
    private static void setDataSource(String dataSourceType, String dataSourceName) {
        try {
            Hashtable<String, String> environment = new Hashtable<>();
            environment.put(DeletionConstants.JAVA_NAMING_FACTORY_INITIAL_KEY,
                            DeletionConstants.JAVA_NAMING_FACTORY_INITIAL_VALUE);
            Context ctx = new InitialContext(environment);
            dataSource.put(dataSourceType, (DataSource) ctx.lookup(dataSourceName));
        } catch (NamingException e) {
            LOG.error("Naming exception occurred while retrieving the datasource", e);
        }
    }
}
