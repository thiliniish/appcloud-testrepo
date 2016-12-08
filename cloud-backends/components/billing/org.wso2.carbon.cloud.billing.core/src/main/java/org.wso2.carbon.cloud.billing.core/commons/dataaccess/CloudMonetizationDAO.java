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
import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.cloud.billing.core.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.common.CloudMgtDBConnectionManager;
import org.wso2.carbon.cloud.common.CloudMgtException;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Cloud Monetization DAO
 */
public class CloudMonetizationDAO {
    private static final Log LOG = LogFactory.getLog(CloudMonetizationDAO.class);

    private static final String insertIntoMonetizationStatusQuery =
            "INSERT INTO MONETIZATION_STATUS(TENANT_DOMAIN,CLOUD_APPLICATION)  VALUES (?,?)";

    private static final String insertMonetizationSubscriptionHistoryQuery =
            "INSERT INTO MONETIZATION_API_CLOUD_SUBSCRIPTIONS_HISTORY (ACCOUNT_NUMBER,AM_APP_NAME,AM_API_NAME," +
            "AM_API_VERSION, AM_API_PROVIDER,RATE_PLAN_ID,SUBSCRIPTION_NUMBER,START_DATE,END_DATE) SELECT " +
            "ACCOUNT_NUMBER,AM_APP_NAME,AM_API_NAME,AM_API_VERSION,AM_API_PROVIDER,RATE_PLAN_ID,SUBSCRIPTION_NUMBER," +
            "START_DATE,? FROM MONETIZATION_API_CLOUD_SUBSCRIPTIONS WHERE (ACCOUNT_NUMBER, SUBSCRIPTION_NUMBER) = " +
            "(?,?)";

    private static final String updateMonetizationSubscribersQuery = "UPDATE MONETIZATION_API_CLOUD_SUBSCRIBERS SET " +
                                                                     "TEST_ACCOUNT = (?) WHERE (USER_NAME," +
                                                                     " TENANT_DOMAIN) = (?, ?)";

    private static final String deleteMonetizationSubscriptionQuery =
            "DELETE FROM MONETIZATION_API_CLOUD_SUBSCRIPTIONS WHERE (ACCOUNT_NUMBER, SUBSCRIPTION_NUMBER) = (?,?)";

    private static final String insertMonetizationSubscriberQuery =
            "INSERT INTO MONETIZATION_API_CLOUD_SUBSCRIBERS VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE TEST_ACCOUNT " +
            "= (?)";

    private static final String selectSubscriberFromAccountQuery =
            "SELECT TEST_ACCOUNT,ACCOUNT_NUMBER FROM MONETIZATION_API_CLOUD_SUBSCRIBERS WHERE USER_NAME= ? AND " +
            "TENANT_DOMAIN= ?";

    private static final String selectNumberOfProductPlansForTenantQuery =
            "SELECT COUNT(*) as PRODUCT_PLAN_COUNT from MONETIZATION_PRODUCT_PLANS WHERE TENANT_DOMAIN = (?) and " +
            "PRODUCT_NAME = (?)";

    private static final String selectAccountNumberForSubscribersQuery =
            "SELECT ACCOUNT_NUMBER FROM  MONETIZATION_API_CLOUD_SUBSCRIBERS WHERE USER_NAME = ? AND  TENANT_DOMAIN = ?";

    /**
     * Insert into Monetization_STATUS table
     *
     * @param tenantDomain     tenant domain
     * @param cloudApplication Cloud Application Type
     * @return
     * @throws CloudBillingException
     */
    public boolean insertIntoMonetizationStatus(String tenantDomain, String cloudApplication)
            throws CloudBillingException {

        Connection conn = null;
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
            throw new CloudBillingException("Failed to insert Monetization status for tenant : " + tenantDomain, e);
        } finally {
            CloudMgtDBConnectionManager.closePSAndConnection(ps, conn);
        }
        return executionResult;
    }

    /**
     * Add Monetization subscriber
     *
     * @param username      subscribers username
     * @param tenantDomain  tenant domain subscribed to
     * @param testAccount   test account status
     * @param accountNumber account number
     * @return
     * @throws CloudBillingException
     */
    public boolean insertMonetizationSubscriber(String username, String tenantDomain, boolean testAccount,
                                                String accountNumber) throws CloudBillingException {

        Connection conn = null;
        PreparedStatement ps = null;
        boolean executionResult = false;

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(insertMonetizationSubscriberQuery);
                ps.setString(1, username);
                ps.setString(2, tenantDomain);
                ps.setBoolean(3, testAccount);
                ps.setString(4, accountNumber);
                ps.setBoolean(5, testAccount);
                ps.execute();
                executionResult = true;
            }
        } catch (SQLException | CloudMgtException e) {
            throw new CloudBillingException(
                    "Failed to insert monetization subscription to DB failed for tenant : " + tenantDomain, e);
        } finally {
            CloudMgtDBConnectionManager.closePSAndConnection(ps, conn);
        }
        return executionResult;
    }

    /**
     * Insert to Api Cloud Monetization subscription history
     *
     * @param effectiveDate  Effective date of subscription end
     * @param accountId      Account number of the subscriber
     * @param subscriptionId Subscription Id of the user
     * @return
     * @throws CloudBillingException
     */
    public boolean insertMonetizationSubscriptionHistory(String effectiveDate, String accountId, String subscriptionId)
            throws CloudBillingException {

        Connection conn = null;
        PreparedStatement ps = null;
        boolean executionResult = false;

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(insertMonetizationSubscriptionHistoryQuery);
                ps.setDate(1, new Date(format.parse(effectiveDate).getTime()));
                ps.setString(2, accountId);
                ps.setString(3, subscriptionId);
                ps.execute();
                executionResult = true;
            }
        } catch (SQLException | CloudMgtException | ParseException e) {
            throw new CloudBillingException(
                    "Failed to insert into monetization subscription history for account : " + accountId, e);
        } finally {
            CloudMgtDBConnectionManager.closePSAndConnection(ps, conn);
        }
        return executionResult;
    }

    /**
     * Update test account status for Monetization subscriber
     *
     * @param testAccount  test account status
     * @param username     username of subscriber
     * @param tenantDomain tenant domain
     * @return
     * @throws CloudBillingException
     */
    public boolean updateMonetizationSubscribers(boolean testAccount, String username, String tenantDomain)
            throws CloudBillingException {

        Connection conn = null;
        PreparedStatement ps = null;
        boolean executionResult = false;

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(updateMonetizationSubscribersQuery);
                ps.setBoolean(1, testAccount);
                ps.setString(2, username);
                ps.setString(3, tenantDomain);
                ps.executeUpdate();
                executionResult = true;
            }
        } catch (SQLException | CloudMgtException e) {
            throw new CloudBillingException(
                    "Error while updating subscriber status for monetization account for tenant : " + tenantDomain, e);
        } finally {
            CloudMgtDBConnectionManager.closePSAndConnection(ps, conn);
        }
        return executionResult;
    }

    /**
     * Delete Monetization Subscription from DB
     *
     * @param accountNumber      Subscription account number
     * @param subscriptionNumber Subscription number of the subscriber
     * @return
     * @throws CloudBillingException
     */
    public boolean deleteMonetizationSubscription(String accountNumber, String subscriptionNumber)
            throws CloudBillingException {

        Connection conn = null;
        PreparedStatement ps = null;
        boolean executionResult = false;

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(deleteMonetizationSubscriptionQuery);
                ps.setString(1, accountNumber);
                ps.setString(2, subscriptionNumber);
                ps.execute();
                executionResult = true;
            }
        } catch (SQLException | CloudMgtException e) {
            throw new CloudBillingException(
                    "Error while deleting subscriber for monetization account : " + accountNumber, e);
        } finally {
            CloudMgtDBConnectionManager.closePSAndConnection(ps, conn);
        }
        return executionResult;
    }

    /**
     * Get subscriber account information
     *
     * @param username     user name
     * @param tenantDomain tenant domain
     * @return
     * @throws CloudBillingException
     */
    public JSONArray getSubscriberFromAccount(String username, String tenantDomain) throws CloudBillingException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        JSONObject jsonObject;
        JSONArray resultArray = new JSONArray();

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(selectSubscriberFromAccountQuery);
                ps.setString(1, username);
                ps.setString(2, tenantDomain);
                resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    jsonObject = new JSONObject();
                    jsonObject.put("TEST_ACCOUNT", resultSet.getBoolean("TEST_ACCOUNT"));
                    jsonObject.put("ACCOUNT_NUMBER", resultSet.getString("ACCOUNT_NUMBER"));
                    resultArray.put(jsonObject);
                }
            }
        } catch (SQLException | CloudMgtException e) {
            throw new CloudBillingException(
                    "Failed to susbcriber information for tenant : " + tenantDomain + " and username : " + tenantDomain,
                    e);
        } finally {
            CloudMgtDBConnectionManager.closeAllConnections(ps, conn, resultSet);
        }
        return resultArray;
    }

    /**
     * Get number of registered Product plans for a Tenant
     *
     * @param tenantDomain tenant domain
     * @param productName  Product names
     * @return
     * @throws CloudBillingException
     */
    public String getNumberOfProductPlansForTenant(String tenantDomain, String productName)
            throws CloudBillingException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        String count = null;

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(selectNumberOfProductPlansForTenantQuery);
                ps.setString(1, tenantDomain);
                ps.setString(2, productName);
                resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    count = resultSet.getString("PRODUCT_PLAN_COUNT");
                }
            }
        } catch (SQLException | CloudMgtException e) {
            throw new CloudBillingException(
                    "Failed to retrieve status from BILLING_STATUS for tenant : " + tenantDomain, e);
        } finally {
            CloudMgtDBConnectionManager.closeAllConnections(ps, conn, resultSet);
        }
        return count;
    }

    /**
     * Get account number for a subscriber
     *
     * @param username      username of the subscriber
     * @param tenantDomain  tenant domain
     * @return
     * @throws CloudBillingException
     */
    public JSONArray getAccountNumberForSubscribers(String username, String tenantDomain) throws CloudBillingException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        String count = null;
        JSONObject jsonObject;
        JSONArray resultArray = new JSONArray();

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(selectAccountNumberForSubscribersQuery);
                ps.setString(1, username);
                ps.setString(2, tenantDomain);
                resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    jsonObject = new JSONObject();
                    jsonObject.put("ACCOUNT_NUMBER", resultSet.getString("ACCOUNT_NUMBER"));
                    resultArray.put(jsonObject);
                }
            }
        } catch (SQLException | CloudMgtException e) {
            throw new CloudBillingException("Failed to Account Number for user : " + username + "" + tenantDomain, e);
        } finally {
            CloudMgtDBConnectionManager.closeAllConnections(ps, conn, resultSet);
        }
        return resultArray;
    }

}
