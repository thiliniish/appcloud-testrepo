/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.cloud.users.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.users.common.CloudConstants;
import org.wso2.carbon.cloud.users.service.UserManagementException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 * Database Management Class to lookup datasources, get tenant display names and deleting users from user mappings
 */
public class DatabaseManager {
    private static Log log = LogFactory.getLog(DatabaseManager.class);
    private DataSource dataSource;

    public DatabaseManager(String dataSource) {
        this.dataSource = lookupDataSource(dataSource);
    }

    private static DataSource lookupDataSource(String dataSourceName) {
        try {
            Hashtable<String, String> environment = new Hashtable<String, String>();
            environment.put(CloudConstants.JAVA_NAMING_FACTORY_INITIAL, CloudConstants.CARBON_JAVA_URL_CONTEXT_FACTORY);
            Context initContext = new InitialContext(environment);
            DataSource dataSource = (DataSource) initContext.lookup(dataSourceName);
            if (dataSource != null) {
                return dataSource;
            } else {
                String msg = "Cannot Find a data source with the name";
                log.error(msg);
                throw new UserManagementException(msg);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error in looking up data source: " + e.getMessage(), e);
        }
    }

    public boolean deleteUserFromTenantUserMapping(String user, String tenantDomain) throws SQLException {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try {
            conn = dataSource.getConnection();
            preparedStatement =
                    conn.prepareStatement("DELETE FROM TENANT_USER_MAPPING WHERE userName=? AND tenantDomain=?");
            preparedStatement.setString(1, user);
            preparedStatement.setString(2, tenantDomain);
            preparedStatement.execute();
            return true;
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw e;
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    /**
     * This method retrieves the tenant display name for the tenant domain from the database.
     *
     * @param tenantDomain
     * @return the tenant display name
     * @throws SQLException
     */
    public String getTenantDisplayName(String tenantDomain) throws SQLException {

        Connection conn = null;
        PreparedStatement preparedStatement = null;
        String tenantDisplayName = "";
        try {
            conn = dataSource.getConnection();
            preparedStatement =
                    conn.prepareStatement(CloudConstants.SELECT_TENANT_DISPLAY_NAME_FOR_TENANT_DOMAIN_QUERY);
            preparedStatement.setString(1, tenantDomain);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                tenantDisplayName = rs.getString("displayName");
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw e;
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
        return tenantDisplayName;
    }
}
