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
import org.wso2.carbon.cloud.user.manager.util.CloudDatabaseManager;
import org.wso2.carbon.cloud.user.manager.util.CloudUserManagerConstants;
import org.wso2.carbon.cloud.user.manager.util.CloudUserManagerException;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A service which exposes user information
 */
public class CloudUserManager {

    private static Log log = LogFactory.getLog(CloudUserManager.class);

    /**
     * Returns an Array of {@link TenantInfoBean}s of the current user
     *
     * @return an Array of {@link TenantInfoBean}
     * @throws CloudUserManagerException
     */
    public TenantInfoBean[] getTenantDisplayNames() throws CloudUserManagerException {
        List<TenantInfoBean> tenantInfoList;
        String loggedInUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            tenantInfoList = new ArrayList<>();
            connection = CloudDatabaseManager.getConnection();
            preparedStatement = connection.prepareStatement(CloudUserManagerConstants.GET_TENANT_INFORMATION_QUERY);
            preparedStatement.setString(1, loggedInUser);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                TenantInfoBean tenantInfo = new TenantInfoBean();
                String tenantDomain = resultSet.getString(CloudUserManagerConstants.TENANT_DOMAIN);
                String tenantDisplayName = resultSet.getString(CloudUserManagerConstants.DISPLAY_NAME);
                tenantInfo.setTenantDomain(tenantDomain);
                tenantInfo.setTenantDisplayName(tenantDisplayName);
                tenantInfoList.add(tenantInfo);
            }
        } catch (SQLException | CloudUserManagerException e) {
            String msg = "Error occurred while getting the tenant information of user : " + loggedInUser;
            log.error(msg, e);
            throw new CloudUserManagerException(msg);
        } finally {
            CloudDatabaseManager.closeAllConnections(connection, preparedStatement, resultSet);
        }
        return tenantInfoList.toArray(new TenantInfoBean[tenantInfoList.size()]);
    }

}
