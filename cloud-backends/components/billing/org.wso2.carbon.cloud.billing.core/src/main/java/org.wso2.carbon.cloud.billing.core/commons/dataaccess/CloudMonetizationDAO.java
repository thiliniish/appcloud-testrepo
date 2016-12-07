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

package org.wso2.carbon.cloud.billing.core.commons.dataaccess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.billing.core.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.common.CloudMgtDBConnectionManager;
import org.wso2.carbon.cloud.common.CloudMgtException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Cloud Monetization DAO
 */
public class CloudMonetizationDAO {
    private static final Log LOG = LogFactory.getLog(CloudMonetizationDAO.class);

    private static final String insertIntoMonetizationStatusQuery =
            "INSERT INTO MONETIZATION_STATUS(TENANT_DOMAIN,CLOUD_APPLICATION)  VALUES (?,?)";

    /**
     * Insert into Monetization_STATUS table
     *
     * @param tenantDomain
     * @param cloudApplication
     * @return
     * @throws CloudBillingException
     */
    public boolean insertIntoMonetizationStatus(String tenantDomain, String cloudApplication)
            throws CloudBillingException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        boolean executionResult = false;

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(insertIntoMonetizationStatusQuery);
                ps.setString(1, tenantDomain);
                ps.setString(2, cloudApplication);
                ps.execute();
                executionResult = true;
            }
        } catch (SQLException | CloudMgtException e) {
            throw new CloudBillingException(
                    "Failed to retrieve status from BILLING_STATUS for tenant : " + tenantDomain, e);
        } finally {
            CloudMgtDBConnectionManager.closeAllConnections(ps, conn, resultSet);
        }
        return executionResult;
    }
}
