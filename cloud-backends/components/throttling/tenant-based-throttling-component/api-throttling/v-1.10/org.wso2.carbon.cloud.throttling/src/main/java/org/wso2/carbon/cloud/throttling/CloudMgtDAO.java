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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.cloud.throttling.common.CloudThrottlingException;
import org.wso2.carbon.cloud.throttling.common.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class is used for retrieving data from cloud mgt db
 */
public class CloudMgtDAO {

    /*
    * This method is used to get the subscription type from the cloud mgt db
    * @param tenantDomain : String registered tenant domain
    * @param subscription : String subscribed cloud
    * @return type of subscription (PAID, TRIAL, FREE)
    * */
    public String getSubscriptionType(String tenantDomain, String subscription) throws CloudThrottlingException {
        if (StringUtils.isBlank(tenantDomain) || StringUtils.isBlank(subscription)) {
            throw new CloudThrottlingException("Invalid parameters tenantDomain : " + tenantDomain + " subscription : " + subscription);
        }
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        String subscriptionType = null;
        try {
            conn = DBConnectionManager.getDbConnection();
            if (conn != null) {
                String sqlQuery = "SELECT " +
                        "TYPE FROM BILLING_STATUS " +
                        "WHERE TENANT_DOMAIN = ? AND SUBSCRIPTION = ? AND STATUS IN('ACTIVE', 'EXTENDED', 'PENDING_DISABLE');";

                ps = conn.prepareStatement(sqlQuery);
                ps.setString(1, tenantDomain);
                ps.setString(2, subscription);
                resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    subscriptionType = resultSet.getString("TYPE");
                }
            }
        } catch (SQLException e) {
            Utils.handleException("Failed to retrieve type of subscription for tenant domain " + tenantDomain + " for " + subscription, e);
        } catch (CloudThrottlingException e) {
            Utils.handleException("Failed to get db connection for cloud mgt db ", e);
        } finally {
            DBConnectionManager.closeAllConnections(ps, conn, resultSet);
        }
        return subscriptionType;
    }

    /*
     * This method is used to get the Account Id from the cloud mgt db
     * @param tenantDomain : String registered tenant domain
     * @return Account Number of the tenant <tenantDomain>
     * */
    public String getAccountNumber(String tenantDomain) throws CloudThrottlingException {
        if (StringUtils.isBlank(tenantDomain)) {
            throw new CloudThrottlingException("Invalid parameter tenantDomain : " + tenantDomain);
        }
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        String accountId = null;
        try {
            conn = DBConnectionManager.getDbConnection();
            if (conn != null) {
                String sqlQuery = "SELECT ACCOUNT_NUMBER FROM BILLING_ACCOUNT WHERE TENANT_DOMAIN = ?;";
                ps = conn.prepareStatement(sqlQuery);
                ps.setString(1, tenantDomain);
                resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    accountId = resultSet.getString("ACCOUNT_NUMBER");
                }
            }
        } catch (SQLException e) {
            Utils.handleException("Failed to retrieve Account Id for tenant domain " + tenantDomain, e);
        } catch (CloudThrottlingException e) {
            Utils.handleException("Failed to get db connection for cloud mgt db ", e);
        } finally {
            DBConnectionManager.closeAllConnections(ps, conn, resultSet);
        }
        return accountId;
    }

    /*
     * This method is used to get the Product rate Plan Id from the cloud mgt db
     * @param tenantDomain : String registered tenant domain
     * @param subscription : String subscribed cloud
     * @return Zuora  ProductRatePlanId of the Account Number <accountNumber> and for the cloud type <subscription>
     * */
    public String getProductRatePlanId(String accountNumber, String subscription) throws CloudThrottlingException {
        if (StringUtils.isBlank(accountNumber) || StringUtils.isBlank(subscription)) {
            throw new CloudThrottlingException("Invalid parameters accountNumber : " + accountNumber + " subscription : " + subscription);
        }
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        String accountId = null;

        try {
            conn = DBConnectionManager.getDbConnection();
            if (conn != null) {
                String sqlQuery = "SELECT PRODUCT_RATE_PLAN_ID FROM BILLING_ACCOUNT_AMENDMENTS " +
                        "WHERE ACCOUNT_NUMBER = ? AND SUBSCRIPTION = ? ORDER BY END_DATE DESC LIMIT 1";
                ps = conn.prepareStatement(sqlQuery);
                ps.setString(1, accountNumber);
                ps.setString(2, subscription);
                resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    accountId = resultSet.getString("PRODUCT_RATE_PLAN_ID");
                }
            }
        } catch (SQLException e) {
            Utils.handleException("Failed to retrieve Account Id for tenant domain " + accountNumber, e);
        } catch (CloudThrottlingException e) {
            Utils.handleException("Failed to get db connection for cloud mgt db ", e);
        } finally {
            DBConnectionManager.closeAllConnections(ps, conn, resultSet);
        }
        return accountId;
    }
}
