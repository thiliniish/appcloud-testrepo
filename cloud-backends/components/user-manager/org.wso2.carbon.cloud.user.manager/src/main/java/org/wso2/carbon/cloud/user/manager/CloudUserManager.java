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

package org.wso2.carbon.cloud.user.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.user.manager.beans.TenantInfoBean;
import org.wso2.carbon.cloud.user.manager.util.CloudUserManagerConstants;
import org.wso2.carbon.cloud.user.manager.util.CloudUserManagerException;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * A service which exposes user information
 */
public class CloudUserManager {

    private static Log log = LogFactory.getLog(CloudUserManager.class);

    /**
     * Returns an Array of {@link TenantInfoBean}s of the current user
     * @return  an Array of {@link TenantInfoBean}
     * @throws CloudUserManagerException
     */
    public TenantInfoBean[] getTenantDisplayNames() throws CloudUserManagerException {
        ArrayList<TenantInfoBean> tenantInfoList;
        String loggedInUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try {
            tenantInfoList = new ArrayList<TenantInfoBean>();
            Hashtable<String, String> environment = new Hashtable<String, String>();
            environment.put("java.naming.factory.initial", "org.wso2.carbon.tomcat.jndi.CarbonJavaURLContextFactory");
            Context initContext = new InitialContext(environment);
            DataSource dataSource = (DataSource) initContext.lookup(CloudUserManagerConstants.CLOUD_MGT_DATASOURCE);
            if (dataSource != null) {
                conn = dataSource.getConnection();
                preparedStatement = conn.prepareStatement(CloudUserManagerConstants.GET_TENANT_INFORMATION_QUERY);
                preparedStatement.setString(1, loggedInUser);
                ResultSet rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    TenantInfoBean tenantInfo = new TenantInfoBean();
                    String tenantDomain = rs.getString(CloudUserManagerConstants.TENANT_DOMAIN);
                    String tenantDisplayName = rs.getString(CloudUserManagerConstants.DISPLAY_NAME);
                    tenantInfo.setTenantDomain(tenantDomain);
                    tenantInfo.setTenantDisplayName(tenantDisplayName);
                    tenantInfoList.add(tenantInfo);
                }
            } else {
                log.info("Cannot Find a data source with the name : " + CloudUserManagerConstants.CLOUD_MGT_DATASOURCE);
            }
        } catch (NamingException e) {
            String msg =
                    "Error occurred while getting the datasource : " + CloudUserManagerConstants.CLOUD_MGT_DATASOURCE;
            log.error(msg, e);
            throw new CloudUserManagerException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while getting the tenant information of user : " + loggedInUser;
            log.error(msg, e);
            throw new CloudUserManagerException(msg, e);
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.warn("Error occurred while cleaning up SQL connections.", e);
            }
        }
        return tenantInfoList.toArray(new TenantInfoBean[tenantInfoList.size()]);
    }

}
