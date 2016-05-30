/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.cloud.das.datapurge.tool.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This DB Connector class will create the DB Connection to the cloud and get the paid tenant list
 */
public class DBConnector {

    private final Log log = LogFactory.getLog(DBConnector.class);

    /**
     * Accessing the status of the tenant
     * @return String
     * @throws NamingException
     * @throws SQLException
     */
    public String getPaidTenantDomains() throws NamingException, SQLException {
        Connection connection = getDataSourceConnection();
        String query = DASPurgeToolConstants.SQL_SELECT_STATUS_FROM_BILLING_STATUS;
        PreparedStatement preparedStatement ;
        ResultSet resultSet;
        try {
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String tenantDomain = resultSet.getString(1);
                resultSet.close();
                return tenantDomain;
            }
        } catch (SQLException e) {
            String message = "Error while accessing database. Query - "+ query + e.getErrorCode();
            log.error(message , e);
            throw new SQLException(message,e);
        } finally {
            closeConnection(connection);
        }
        return null;

    }

    public Connection getDataSourceConnection() throws NamingException, SQLException {

        //super tenant details
        int tenantId = MultitenantConstants.SUPER_TENANT_ID;
        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        Connection conn = null;

        //changing the tenant flow to the supper tenant
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(tenantId);
        privilegedCarbonContext.setTenantDomain(tenantDomain);

        //getting the cloud-mgt data source connection
        DataSource ds = (DataSource) privilegedCarbonContext.getJNDIContext().lookup(DASPurgeToolConstants.CLOUD_DATASOURCE);
        try {
            conn = ds.getConnection();
        } catch (SQLException e) {
            String message = "Error while connecting to data Source "+ DASPurgeToolConstants.CLOUD_DATASOURCE +
                    " , "+ e.getErrorCode();
            log.error(message , e);
            throw new SQLException(message,e);
        } finally {
            //Ending the tenant flow
            PrivilegedCarbonContext.endTenantFlow();
        }
        return conn;
    }

    public void closeConnection(Connection connection) throws SQLException {
        try {
            connection.close();

        } catch (SQLException e) {
            String message = "Error while closing the database connection - "+ e.getErrorCode();
            log.error(message , e);
            throw new SQLException(message,e);
        }

    }


}
