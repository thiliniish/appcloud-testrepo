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
    private static final String insertIntoMonetizationProductPlans =
            "INSERT INTO MONETIZATION_PRODUCT_PLANS (TENANT_DOMAIN, PRODUCT_NAME,RATE_PLAN_NAME, RATE_PLAN_ID)" +
            " VALUES (?, ?, ?, ?);";

    private static final String selectAccountNumberForSubscribersQuery =
            "SELECT ACCOUNT_NUMBER FROM  MONETIZATION_API_CLOUD_SUBSCRIBERS WHERE USER_NAME = ? AND  TENANT_DOMAIN = ?";

    private static final String insertIntoMonetizationApiCloudPlanInfo =
            "INSERT INTO MONETIZATION_API_CLOUD_PLANS_INFO (RATE_PLAN_ID, MAX_DAILY_USAGE, MONTHLY_RENTAL, UOM_UNIT, " +
            "UOM_PRICE) VALUES (?, ?, ?, ?, ?);";

    private static final String updateMonetizationApiCloudPlanInfo =
            "UPDATE MONETIZATION_API_CLOUD_PLANS_INFO SET MAX_DAILY_USAGE = (?), MONTHLY_RENTAL = (?), " +
            "UOM_UNIT = (?),UOM_PRICE = (?) WHERE (RATE_PLAN_ID) = (?)";

    private static final String getMonetizationProductPlanForTenant =
            "SELECT COUNT(*) as COUNT FROM MONETIZATION_PRODUCT_PLANS WHERE TENANT_DOMAIN = (?) AND" +
            " RATE_PLAN_NAME = (?);";

    private static final String getMonetizationSubscriptionDataFromId =
            "SELECT  MAPICS.AM_API_NAME , MAPICS.AM_API_VERSION ," +
            " MPP.RATE_PLAN_NAME FROM MONETIZATION_API_CLOUD_SUBSCRIPTIONS MAPICS INNER JOIN " +
            "MONETIZATION_PRODUCT_PLANS MPP ON  MAPICS.RATE_PLAN_ID = MPP.RATE_PLAN_ID WHERE " +
            "MAPICS.SUBSCRIPTION_NUMBER = ? limit 1;";

    private static final String getMonetizationSubscriptionForAccount =
            "SELECT DISTINCT AM_API_NAME, AM_API_VERSION FROM MONETIZATION_API_CLOUD_SUBSCRIPTIONS" +
            " WHERE ACCOUNT_NUMBER = (?);";
    private static final String getMonetizationProductPlans =
            "SELECT MPP.RATE_PLAN_NAME, MACPP.MAX_DAILY_USAGE, MACPP.MONTHLY_RENTAL," +
            "MACPP.UOM_UNIT, MACPP.UOM_PRICE  FROM MONETIZATION_PRODUCT_PLANS MPP INNER JOIN " +
            "MONETIZATION_API_CLOUD_PLANS_INFO MACPP ON MPP.RATE_PLAN_ID = MACPP.RATE_PLAN_ID WHERE MPP.TENANT_DOMAIN" +
            " = (?);";

    private static final String getMonetizationSubscriptionQuery =
            "SELECT MAPICS.SUBSCRIPTION_NUMBER, '-' END_DATE, DATE_FORMAT(MAPICS.START_DATE,'%Y-%m-%d') START_DATE, " +
            "MAPICS.AM_APP_NAME, MAPICS.AM_API_NAME, MAPICS.AM_API_VERSION, MPP.RATE_PLAN_NAME  FROM" +
            " MONETIZATION_API_CLOUD_SUBSCRIPTIONS MAPICS INNER JOIN MONETIZATION_PRODUCT_PLANS MPP ON " +
            " MAPICS.RATE_PLAN_ID = MPP.RATE_PLAN_ID WHERE ACCOUNT_NUMBER = (?) UNION ALL " +
            "SELECT MAPICSH.SUBSCRIPTION_NUMBER, MAPICSH.END_DATE, " +
            "DATE_FORMAT(MAPICSH.START_DATE,'%Y-%m-%d') AS START_DATE," +
            " MAPICSH.AM_APP_NAME, MAPICSH.AM_API_NAME,  MAPICSH.AM_API_VERSION, MPP.RATE_PLAN_NAME  FROM " +
            "MONETIZATION_API_CLOUD_SUBSCRIPTIONS_HISTORY MAPICSH INNER JOIN MONETIZATION_PRODUCT_PLANS MPP " +
            "ON MAPICSH.RATE_PLAN_ID = MPP.RATE_PLAN_ID WHERE  MAPICSH.ACCOUNT_NUMBER= (?)  ORDER BY  START_DATE DESC;";

    private static final String getMonetizationSubscriptionHistoryQuery =
            "SELECT  MAPICS.AM_API_NAME, MAPICS.AM_API_VERSION, MPP.RATE_PLAN_NAME FROM " +
            "MONETIZATION_API_CLOUD_SUBSCRIPTIONS_HISTORY  MAPICS INNER JOIN MONETIZATION_PRODUCT_PLANS MPP ON " +
            "MAPICS.RATE_PLAN_ID = MPP.RATE_PLAN_ID WHERE MAPICS.SUBSCRIPTION_NUMBER = (?) limit 1;";

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
     * This method is to insert product plan details
     *
     * @param tenantDomain
     * @param productName
     * @param planName
     * @param ratePlanId
     * @throws CloudBillingException
     */
    public void insertProductPlanDetails(String tenantDomain, String productName, String planName, String ratePlanId)
            throws CloudBillingException {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(insertIntoMonetizationProductPlans);
                ps.setString(1, tenantDomain);
                ps.setString(2, productName);
                ps.setString(3, planName);
                ps.setString(4, ratePlanId);
                ps.executeUpdate();
            }
        } catch (SQLException | CloudMgtException e) {
            throw new CloudBillingException("Failed to insert product plan details for tenant : " + tenantDomain, e);
        } finally {
            CloudMgtDBConnectionManager.closePSAndConnection(ps, conn);
        }
    }

    /**
     * This method is to insert product plan info details
     *
     * @param ratePlanId
     * @param dailyUsage
     * @param pricing
     * @param overageLimit
     * @param overageCharge
     * @throws CloudBillingException
     */
    public void insertIntoMonetizationApiCloudPlanInfo(String ratePlanId, int dailyUsage, double pricing,
                                                       int overageLimit, double overageCharge)
            throws CloudBillingException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(insertIntoMonetizationApiCloudPlanInfo);
                ps.setString(1, ratePlanId);
                ps.setInt(2, dailyUsage);
                ps.setDouble(3, pricing);
                ps.setInt(4, overageLimit);
                ps.setDouble(5, overageCharge);
                ps.executeUpdate();
            }
        } catch (SQLException | CloudMgtException e) {
            throw new CloudBillingException("Failed to insert product plan info details for rate plan : " + ratePlanId,
                                            e);
        } finally {
            CloudMgtDBConnectionManager.closePSAndConnection(ps, conn);
        }
    }

    /**
     * This method is to update product plan info details
     *
     * @param ratePlanId
     * @param dailyUsage
     * @param pricing
     * @param overageLimit
     * @param overageCharge
     * @throws CloudBillingException
     */
    public void updateMonetizationApiCloudPlanInfo(int dailyUsage, double pricing, int overageLimit,
                                                   double overageCharge, String ratePlanId)
            throws CloudBillingException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(updateMonetizationApiCloudPlanInfo);

                ps.setInt(1, dailyUsage);
                ps.setDouble(2, pricing);
                ps.setInt(3, overageLimit);
                ps.setDouble(4, overageCharge);
                ps.setString(5, ratePlanId);
                ps.executeUpdate();
            }
        } catch (SQLException | CloudMgtException e) {
            throw new CloudBillingException("Failed to update product plan info details for rate plan : " + ratePlanId,
                                            e);
        } finally {
            CloudMgtDBConnectionManager.closePSAndConnection(ps, conn);
        }
    }

    /**
     * This is to verify the availability for the product rate plan to the tenant domain
     *
     * @param tenantDomain tenant domain
     * @param ratePlanName product rate plan
     * @return
     * @throws CloudBillingException
     */
    public int getMonetizationProductPlanForTenant(String tenantDomain, String ratePlanName)
            throws CloudBillingException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        int count = 0;

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(getMonetizationProductPlanForTenant);
                ps.setString(1, tenantDomain);
                ps.setString(2, ratePlanName);
                resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    count = Integer.parseInt(resultSet.getString("Count"));

                }
            }
        } catch (SQLException | CloudMgtException e) {
            throw new CloudBillingException("Failed to get monetization product plan count for tenant " + tenantDomain,
                                            e);
        } finally {
            CloudMgtDBConnectionManager.closeAllConnections(ps, conn, resultSet);
        }
        return count;
    }

    /**
     * Get subscription information
     *
     * @param subscriptionNumber
     * @return json array of subscription information
     * @throws CloudBillingException
     */
    public JSONArray getSubscriptionDataforSubscriptionId(String subscriptionNumber) throws CloudBillingException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        JSONObject jsonObject;
        JSONArray resultArray = new JSONArray();

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(getMonetizationSubscriptionDataFromId);
                ps.setString(1, subscriptionNumber);
                resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    jsonObject = new JSONObject();
                    jsonObject.put("AM_API_NAME", resultSet.getString("AM_API_NAME"));
                    jsonObject.put("AM_API_VERSION", resultSet.getString("AM_API_VERSION"));
                    jsonObject.put("RATE_PLAN_NAME", resultSet.getString("RATE_PLAN_NAME"));
                    resultArray.put(jsonObject);
                }
            }
        } catch (SQLException | CloudMgtException e) {
            throw new CloudBillingException(
                    "Failed to get subscription information for subscription Id " + subscriptionNumber, e);
        } finally {
            CloudMgtDBConnectionManager.closeAllConnections(ps, conn, resultSet);
        }
        return resultArray;
    }

    /**
     * Get subscription information for account
     *
     * @param accountNumber
     * @return json array of subscription information
     * @throws CloudBillingException
     */
    public JSONArray getMonetizationSubscriptionsForAccount(String accountNumber) throws CloudBillingException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        JSONObject jsonObject;
        JSONArray resultArray = new JSONArray();

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(getMonetizationSubscriptionForAccount);
                ps.setString(1, accountNumber);
                resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    jsonObject = new JSONObject();
                    jsonObject.put("AM_API_NAME", resultSet.getString("AM_API_NAME"));
                    jsonObject.put("AM_API_VERSION", resultSet.getString("AM_API_VERSION"));
                    resultArray.put(jsonObject);
                }
            }
        } catch (SQLException | CloudMgtException e) {
            throw new CloudBillingException("Failed to subscription information for account Id " + accountNumber, e);
        } finally {
            CloudMgtDBConnectionManager.closeAllConnections(ps, conn, resultSet);
        }
        return resultArray;
    }

    /**
     * Get monetization product plans
     *
     * @param tenantDomain
     * @return json array of product plan information
     * @throws CloudBillingException
     */
    public JSONArray getMonetizationProductPlans(String tenantDomain) throws CloudBillingException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        JSONObject jsonObject;
        JSONArray resultArray = new JSONArray();

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(getMonetizationProductPlans);
                ps.setString(1, tenantDomain);
                resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    jsonObject = new JSONObject();
                    jsonObject.put("RATE_PLAN_NAME", resultSet.getString("RATE_PLAN_NAME"));
                    jsonObject.put("MAX_DAILY_USAGE", resultSet.getInt("MAX_DAILY_USAGE"));
                    jsonObject.put("MONTHLY_RENTAL", resultSet.getDouble("MONTHLY_RENTAL"));
                    jsonObject.put("UOM_UNIT", resultSet.getInt("UOM_UNIT"));
                    jsonObject.put("UOM_PRICE", resultSet.getInt("UOM_PRICE"));
                    resultArray.put(jsonObject);
                }
            }
        } catch (SQLException | CloudMgtException e) {
            throw new CloudBillingException("Failed to get product plans for tenant domain " + tenantDomain, e);
        } finally {
            CloudMgtDBConnectionManager.closeAllConnections(ps, conn, resultSet);
        }
        return resultArray;
    }

    /**
     * Get monetization subscriptions
     *
     * @param accountNumber
     * @return json array of subscription information
     * @throws CloudBillingException
     */
    public JSONArray getMonetizationSubscriptions(String accountNumber) throws CloudBillingException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        JSONObject jsonObject;
        JSONArray resultArray = new JSONArray();

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(getMonetizationSubscriptionQuery);
                ps.setString(1, accountNumber);
                ps.setString(2, accountNumber);
                resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    jsonObject = new JSONObject();
                    jsonObject.put("SUBSCRIPTION_NUMBER", resultSet.getString("SUBSCRIPTION_NUMBER"));
                    jsonObject.put("END_DATE", resultSet.getDate("END_DATE"));
                    jsonObject.put("START_DATE", resultSet.getDate("START_DATE"));
                    jsonObject.put("AM_APP_NAME", resultSet.getString("AM_APP_NAME"));
                    jsonObject.put("AM_API_VERSION", resultSet.getString("AM_API_VERSION"));
                    jsonObject.put("RATE_PLAN_NAME", resultSet.getString("RATE_PLAN_NAME"));
                    resultArray.put(jsonObject);
                }
            }
        } catch (SQLException | CloudMgtException e) {
            throw new CloudBillingException("Failed to get subscriptions for account " + accountNumber, e);
        } finally {
            CloudMgtDBConnectionManager.closeAllConnections(ps, conn, resultSet);
        }
        return resultArray;
    }

    /**
     * Get monetization subscriptions history
     *
     * @param subscriptionNumber
     * @return json array of porduct plan information
     * @throws CloudBillingException
     */
    public JSONArray getMonetizationSubscriptionHistory(String subscriptionNumber) throws CloudBillingException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        JSONObject jsonObject;
        JSONArray resultArray = new JSONArray();

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(getMonetizationSubscriptionHistoryQuery);
                ps.setString(1, subscriptionNumber);
                resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    jsonObject = new JSONObject();
                    jsonObject.put("AM_API_NAME", resultSet.getString("AM_API_NAME"));
                    jsonObject.put("AM_API_VERSION", resultSet.getString("AM_API_VERSION"));
                    jsonObject.put("RATE_PLAN_NAME", resultSet.getString("RATE_PLAN_NAME"));
                    resultArray.put(jsonObject);
                }
            }
        } catch (SQLException | CloudMgtException e) {
            throw new CloudBillingException("Failed to get subscriptions history data for subscription " +
                                            subscriptionNumber, e);
        } finally {
            CloudMgtDBConnectionManager.closeAllConnections(ps, conn, resultSet);
        }
        return resultArray;
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
