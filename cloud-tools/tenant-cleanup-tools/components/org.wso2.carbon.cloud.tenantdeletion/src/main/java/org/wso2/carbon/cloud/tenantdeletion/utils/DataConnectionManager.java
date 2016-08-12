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
import org.wso2.carbon.cloud.tenantdeletion.constants.DeletionConstants;

import java.sql.Connection;
import java.sql.SQLException;
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
        return setDataSource(DeletionConstants.CLOUD_MGT);
    }

    /**
     * Returns MySQL database connection for USER_MGT Database
     *
     * @return Database connection
     */

    public static Connection getUserMgtDbConnection() throws TenantDeletionException {
        return setDataSource(DeletionConstants.USER_MGT);
    }

    private static Connection setDataSource(String dataSourceName) throws TenantDeletionException {
        synchronized (DataConnectionManager.class) {
            if (!dataSource.containsKey(dataSourceName)) {
                try {
                    Hashtable<String, String> environment = new Hashtable<>();
                    environment.put(DeletionConstants.JAVA_NAMING_FACTORY_INITIAL_KEY,
                                    DeletionConstants.JAVA_NAMING_FACTORY_INITIAL_VALUE);
                    Context ctx = new InitialContext(environment);
                    dataSource.put(dataSourceName, (DataSource) ctx.lookup(dataSourceName));
                } catch (NamingException e) {
                    //jhh
                }
            }
            try {
                return dataSource.get(dataSourceName).getConnection();
            } catch (SQLException e) {
                throw new TenantDeletionException(
                        "Error while looking up the data source: " + DeletionConstants.USER_MGT, e);
            }
        }

    }
}
