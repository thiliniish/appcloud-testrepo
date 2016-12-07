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
 * Cloud Billing DAO
 */
public class CloudBillingDAO {
    private static final Log LOG = LogFactory.getLog(CloudBillingDAO.class);

    private static final String selectAllFromBillingStatusQuery =
            "SELECT * FROM BILLING_STATUS WHERE (TENANT_DOMAIN,SUBSCRIPTION,TYPE) = (?,?,?)";
    private static final String selectStatusFromBillingStatusQuery =
            "SELECT STATUS FROM BILLING_STATUS WHERE (TENANT_DOMAIN,SUBSCRIPTION,TYPE)=(?,?,?)";
    private static final String insertIntoBillingAccountQuery = "INSERT INTO BILLING_ACCOUNT VALUES (?,?)";
    private static final String updateAccountNumberForTenantQuery =
            "UPDATE BILLING_ACCOUNT SET ACCOUNT_NUMBER = ? WHERE TENANT_DOMAIN = ?";
    private static final String insertIntoBillingStatusQuery = "INSERT INTO BILLING_STATUS VALUES (?,?,?,?,?,?)";
    private static final String updateSubscriptionStatusQuery =
            "UPDATE BILLING_STATUS SET STATUS = (?), END_DATE = (?) WHERE (TENANT_DOMAIN, SUBSCRIPTION, TYPE) = (?,?," +
            "?)";
    private static final String updateBillingStatusQuery = "UPDATE BILLING_STATUS SET STATUS = (?), START_DATE = " +
                                                           "(?), END_DATE = (?) WHERE" +
                                                           " (TENANT_DOMAIN, SUBSCRIPTION, TYPE) = (?,?,?)";
    private static final String selectBillingAccountNumberQuery =
            "SELECT ACCOUNT_NUMBER FROM  BILLING_ACCOUNT WHERE TENANT_DOMAIN=?";
    private static final String selectSubscriptionForBillingAccountQuery = "SELECT SUBSCRIPTION FROM BILLING_STATUS " +
                                                                           "WHERE TENANT_DOMAIN =? AND TYPE LIKE ? " +
                                                                           "AND STATUS LIKE ?";
    private static final String selectStartDateForSubscriptionQuery =
            "SELECT START_DATE FROM BILLING_STATUS WHERE " + "TENANT_DOMAIN =? AND SUBSCRIPTION LIKE ? AND TYPE LIKE ?";
    private static final String selectgetTypeForTenantSubscriptionQuery =
            "SELECT TYPE FROM BILLING_STATUS WHERE " + "(SUBSCRIPTION,TENANT_DOMAIN)=(?,?)";

    private static final String deleteBillingAccountQuery = "DELETE FROM BILLING_ACCOUNT WHERE TENANT_DOMAIN= ?";

    private static final String updateSubscriptionEndDateQuery = "UPDATE BILLING_ACCOUNT_AMENDMENTS SET END_DATE = " +
                                                                 "(?) WHERE (ACCOUNT_NUMBER, END_DATE, SUBSCRIPTION) " +
                                                                 "= (?,?,?)";

    private static final String insertBillingStatusHistoryQuery = "INSERT INTO  BILLING_STATUS_HISTORY(SUBSCRIPTION," +
                                                                  "ACCOUNT_NUMBER,TENANT_DOMAIN,TYPE,STATUS," +
                                                                  "START_DATE,END_DATE,TENANT_ID) VALUES (?,?,?,?,?," +
                                                                  "?,?,?)";

    private static final String insertBillingAccountAmendmentsQuery =
            "INSERT INTO BILLING_ACCOUNT_AMENDMENTS VALUES (?,?,?,?,?)";

    /**
     * Get current billing status from DB for the tenant for the specific subscription
     *
     * @param tenantDomain tenant domain
     * @param subscription cloud subscription
     * @param type         subscription type
     * @return status of the subscription enum('INACTIVE','ACTIVE','EXTENDED','PENDING_DISABLE','DISABLED')
     * @throws CloudBillingException
     */
    public String selectStatusFromBillingStatus(String tenantDomain, String subscription, String type)
            throws CloudBillingException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        String status = null;

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(selectStatusFromBillingStatusQuery);
                ps.setString(1, tenantDomain);
                ps.setString(2, subscription);
                ps.setString(3, type);
                resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    status = resultSet.getString("STATUS");
                }
            }
        } catch (SQLException | CloudMgtException e) {
            throw new CloudBillingException(
                    "Failed to retrieve status from BILLING_STATUS for tenant : " + tenantDomain, e);
        } finally {
            CloudMgtDBConnectionManager.closeAllConnections(ps, conn, resultSet);
        }
        return status;
    }

    /**
     * Get all information for a tenant with relevant subscription and type
     *
     * @param tenantDomain tenant domain
     * @param subscription cloud subscription
     * @param type         subscription type
     * @return
     * @throws CloudBillingException
     */
    public JSONArray selectAllFromBillingStatus(String tenantDomain, String subscription, String type)
            throws CloudBillingException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        JSONObject jsonObject;
        JSONArray resultArray = new JSONArray();

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(selectAllFromBillingStatusQuery);
                ps.setString(1, tenantDomain);
                ps.setString(2, subscription);
                ps.setString(3, type);
                resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    jsonObject = new JSONObject();
                    jsonObject.put("TENANT_DOMAIN", resultSet.getString("TENANT_DOMAIN"));
                    jsonObject.put("SUBSCRIPTION", resultSet.getString("SUBSCRIPTION"));
                    jsonObject.put("TYPE", resultSet.getString("TYPE"));
                    jsonObject.put("STATUS", resultSet.getString("STATUS"));
                    jsonObject.put("START_DATE", resultSet.getString("START_DATE"));
                    jsonObject.put("END_DATE", resultSet.getString("END_DATE"));
                    resultArray.put(jsonObject);
                }
            }
        } catch (SQLException | CloudMgtException e) {
            throw new CloudBillingException(
                    "Failed to retrieve status from BILLING_STATUS for tenant : " + tenantDomain, e);
        } finally {
            CloudMgtDBConnectionManager.closeAllConnections(ps, conn, resultSet);
        }
        return resultArray;
    }

    /**
     * Get ACCOUNT_NUMBER of tenant from BILLING_ACCOUNT
     *
     * @param tenantDomain tenant domain
     * @return String ACCOUNT_NUMBER
     * @throws CloudBillingException
     */
    public JSONArray getBillingAccountNumber(String tenantDomain) throws CloudBillingException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        JSONArray accountArray = new JSONArray();

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(selectBillingAccountNumberQuery);
                ps.setString(1, tenantDomain);
                resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("ACCOUNT_NUMBER", resultSet.getString("ACCOUNT_NUMBER"));
                    accountArray.put(jsonObject);
                }
            }
        } catch (SQLException | CloudMgtException e) {
            throw new CloudBillingException(
                    "Failed to retrieve status from BILLING_STATUS for tenant : " + tenantDomain, e);
        } finally {
            CloudMgtDBConnectionManager.closeAllConnections(ps, conn, resultSet);
        }
        return accountArray;
    }

    /**
     * Get start date for the subscription for the tenant domain, subscription and type
     *
     * @param tenantDomain tenant domain
     * @param subscription subscription
     * @param type         subscription type enum('PAID','TRIAL','FREE')
     * @return
     * @throws CloudBillingException
     */
    public String getStartDateForSubscription(String tenantDomain, String subscription, String type)
            throws CloudBillingException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        String startDate = null;

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(selectStartDateForSubscriptionQuery);
                ps.setString(1, tenantDomain);
                ps.setString(2, subscription);
                ps.setString(3, type);
                resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    startDate = resultSet.getString("START_DATE");
                }
            }
        } catch (SQLException | CloudMgtException e) {
            throw new CloudBillingException(
                    "Failed to retrieve start date from BILLING_STATUS for tenant : " + tenantDomain + " and " +
                    "subscription : " + subscription, e);
        } finally {
            CloudMgtDBConnectionManager.closeAllConnections(ps, conn, resultSet);
        }
        return startDate;
    }

    /**
     * Get start date for the subscription for the tenant domain, subscription and type
     *
     * @param tenantDomain tenant domain
     * @param subscription cloud subscription
     * @return
     * @throws CloudBillingException
     */
    public JSONArray getTypeForTenantSubscription(String subscription, String tenantDomain)
            throws CloudBillingException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        String type = null;
        JSONArray typeArray = new JSONArray();

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(selectgetTypeForTenantSubscriptionQuery);
                ps.setString(1, subscription);
                ps.setString(2, tenantDomain);
                resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("TYPE", resultSet.getString("TYPE"));
                    typeArray.put(jsonObject);
                }
            }
        } catch (SQLException | CloudMgtException e) {
            throw new CloudBillingException(
                    "Failed to retrieve subscription type from BILLING_STATUS for tenant : " + tenantDomain + " and " +
                    "subscription : " + subscription, e);
        } finally {
            CloudMgtDBConnectionManager.closeAllConnections(ps, conn, resultSet);
        }
        return typeArray;
    }

    /**
     * Get subscription data for given tenant domain, account type and subscription status
     *
     * @param tenantDomain tenant domain
     * @param type         subscription type
     * @param status       subscription status
     * @return JSONArray subscriptions array which matches parsed parameters
     * @throws CloudBillingException
     */
    public JSONArray getSubscriptionsForBillingAccount(String tenantDomain, String type, String status)
            throws CloudBillingException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        String subscription = null;
        JSONArray subscriptionArray = new JSONArray();

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(selectSubscriptionForBillingAccountQuery);
                ps.setString(1, tenantDomain);
                ps.setString(2, type);
                ps.setString(3, status);
                resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("SUBSCRIPTION", resultSet.getString("SUBSCRIPTION"));
                    subscriptionArray.put(jsonObject);
                }
            }
        } catch (SQLException | CloudMgtException e) {
            throw new CloudBillingException(
                    "Failed to retrieve subscriptions for BILLING_STATUS for tenant : " + tenantDomain, e);
        } finally {
            CloudMgtDBConnectionManager.closeAllConnections(ps, conn, resultSet);
        }
        return subscriptionArray;
    }

    /**
     * Inserting tenant domain and account number to BILLING_ACCOUNT table
     *
     * @param accountNumber account number created at billing vendor. Usually this is a NULL value for new accounts
     * @param tenantDomain  tenant domain of the account owner
     * @return success status of the update query
     * @throws CloudBillingException
     */
    public boolean insertIntoBillingAccount(String accountNumber, String tenantDomain) throws CloudBillingException {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean executionResult = false;

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(insertIntoBillingAccountQuery);
                ps.setString(1, accountNumber);
                ps.setString(2, tenantDomain);
                ps.execute();
                executionResult = true;
            }
        } catch (SQLException | CloudMgtException e) {
            throw new CloudBillingException("Failed to add billing account : " + accountNumber + " for the tenant : " +
                                            tenantDomain + " to database ", e);
        } finally {
            CloudMgtDBConnectionManager.closePSAndConnection(ps, conn);
        }
        return executionResult;
    }

    /**
     * Update already created accounts with new billing Account Number for the given tenant domain
     *
     * @param accountNumber account number created at billing vendor
     * @param tenantDomain  tenant domain of the account owner
     * @return success status of the update query
     * @throws CloudBillingException
     */
    public boolean updateAccountNumberForTenant(String accountNumber, String tenantDomain)
            throws CloudBillingException {
        Connection conn = null;
        PreparedStatement ps = null;
        String email = null;
        boolean executionResult = false;

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(updateAccountNumberForTenantQuery);
                ps.setString(1, accountNumber);
                ps.setString(2, tenantDomain);
                ps.executeUpdate();
                executionResult = true;
            }
        } catch (SQLException | CloudMgtException e) {
            throw new CloudBillingException("Failed to update billing account number for the tenant : " +
                                            tenantDomain + " in CloudMgt database", e);
        } finally {
            CloudMgtDBConnectionManager.closePSAndConnection(ps, conn);
        }
        return executionResult;
    }

    /**
     * Insert data into BILLING_STATUS tables for a tenant
     *
     * @param tenantDomain         tenant doamin
     * @param subscription         subscription of the cloud type
     * @param billingType          billing type to be added. Values accepted by database are enum('PAID','TRIAL',
     *                             'FREE')
     * @param billingAccountStatus status of the account. Values accepted by database are enum('INACTIVE','ACTIVE',
     *                             'EXTENDED','PENDING_DISABLE','DISABLED')
     * @param startDate            date of the subscription starting
     * @param endDate              date of subscription ending
     * @return success status of the update query
     * @throws CloudBillingException
     */
    public boolean insertBillingStatus(String tenantDomain, String subscription, String billingType,
                                       String billingAccountStatus, String startDate, String endDate)
            throws CloudBillingException {
        Connection conn = null;
        PreparedStatement ps = null;
        String email = null;
        boolean executionResult = false;

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(insertIntoBillingStatusQuery);
                ps.setString(1, tenantDomain);
                ps.setString(2, subscription);
                ps.setString(3, billingType);
                ps.setString(4, billingAccountStatus);
                ps.setDate(5, new Date(format.parse(startDate).getTime()));
                ps.setDate(6, new Date(format.parse(endDate).getTime()));
                ps.execute();
                executionResult = true;
            }
        } catch (SQLException | CloudMgtException | ParseException e) {
            throw new CloudBillingException("Failed to insert BILLING_STATUS table for tenant : " + tenantDomain, e);
        } finally {
            CloudMgtDBConnectionManager.closePSAndConnection(ps, conn);
        }
        return executionResult;
    }

    /**
     * Update subscription status for tenant
     *
     * @param status        status of the account. Values accepted by database are enum('INACTIVE','ACTIVE','EXTENDED',
     *                      'PENDING_DISABLE','DISABLED')
     * @param effectiveDate end date of the updated subscription
     * @param tenantDomain  tenant domain
     * @param subscription  cloud type of the subscription
     * @param type          billing type to be queried from. Values available in database are enum('PAID','TRIAL',
     *                      'FREE')
     * @return success status of the update query
     * @throws CloudBillingException
     */
    public boolean updateSubscriptionStatus(String status, String effectiveDate, String tenantDomain,
                                            String subscription, String type) throws CloudBillingException {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean executionResult = false;

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(updateSubscriptionStatusQuery);
                ps.setString(1, status);
                ps.setDate(2, new Date(format.parse(effectiveDate).getTime()));
                ps.setString(3, tenantDomain);
                ps.setString(4, subscription);
                ps.setString(5, type);
                ps.executeUpdate();
                executionResult = true;
            }
        } catch (SQLException | CloudMgtException | ParseException e) {
            throw new CloudBillingException("Failed to update BILLING_STATUS table for tenant : " + tenantDomain, e);
        } finally {
            CloudMgtDBConnectionManager.closePSAndConnection(ps, conn);
        }
        return executionResult;
    }

    /**
     * Update Billing Status of a tenant
     *
     * @param status       status of the account. Values accepted by database are enum('INACTIVE','ACTIVE','EXTENDED',
     *                     'PENDING_DISABLE','DISABLED')
     * @param startDate    start date of the current subscription
     * @param endDate      end date of the subscription
     * @param tenantDomain tenant domain
     * @param subscription cloud type of the subscription
     * @param type         billing type to be queried from. Values available in database are enum('PAID','TRIAL',
     *                     'FREE')
     * @return success status of the update query
     * @throws CloudBillingException
     */
    public boolean updateBillingStatus(String status, String startDate, String endDate, String tenantDomain,
                                       String subscription, String type) throws CloudBillingException {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean executionResult = false;

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(updateBillingStatusQuery);
                ps.setString(1, status);
                ps.setDate(2, new Date(format.parse(startDate).getTime()));
                ps.setDate(3, new Date(format.parse(endDate).getTime()));
                ps.setString(4, tenantDomain);
                ps.setString(5, subscription);
                ps.setString(6, type);
                ps.executeUpdate();
                executionResult = true;
            }
        } catch (SQLException | CloudMgtException | ParseException e) {
            throw new CloudBillingException("Failed to update BILLING_STATUS table for tenant : " + tenantDomain, e);
        } finally {
            CloudMgtDBConnectionManager.closePSAndConnection(ps, conn);
        }
        return executionResult;
    }

    /**
     * Delete Billing account for a tenant from BILLING_ACCOUNT table
     *
     * @param tenantDomain tenant domain
     * @return boolean status of deletion
     * @throws CloudBillingException
     */
    public boolean deleteBillingAccount(String tenantDomain) throws CloudBillingException {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean executionResult = false;

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(deleteBillingAccountQuery);
                ps.setString(1, tenantDomain);
                ps.execute();
                executionResult = true;
            }
        } catch (SQLException | CloudMgtException e) {
            throw new CloudBillingException("Failed to delete billing account for the tenant : " + tenantDomain, e);
        } finally {
            CloudMgtDBConnectionManager.closePSAndConnection(ps, conn);
        }
        return executionResult;
    }

    /**
     * Update end date of the subscription when the subscription is updated
     *
     * @param newEndDate     Updated end date for the subscription
     * @param accountNumber  Billing Account Number
     * @param currentEndDate Current end date
     * @param subscription   cloud subscription type
     * @return status of update
     * @throws CloudBillingException
     */
    public boolean updateSubscriptionEndDate(String newEndDate, String accountNumber, String currentEndDate,
                                             String subscription) throws CloudBillingException {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean executionResult = false;

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(updateSubscriptionEndDateQuery);
                ps.setDate(1, new Date(format.parse(newEndDate).getTime()));
                ps.setString(2, accountNumber);
                ps.setDate(3, new Date(format.parse(currentEndDate).getTime()));
                ps.setString(4, subscription);
                ps.executeUpdate();
                executionResult = true;
            }
        } catch (SQLException | CloudMgtException | ParseException e) {
            throw new CloudBillingException("Failed to update end date for the account number : " + accountNumber + "" +
                                            " " +
                                            "to : " + newEndDate, e);
        } finally {
            CloudMgtDBConnectionManager.closePSAndConnection(ps, conn);
        }
        return executionResult;
    }

    /**
     * Insert data to the billing status history table
     *
     * @param subscription  cloud subscription
     * @param accountNumber billing account number
     * @param tenantDomain  tenant domain
     * @param type          account type
     * @param status        account status
     * @param startDate     subscription start date
     * @param endDate       subscrition enddate
     * @param tenantId      tenant id
     * @return
     * @throws CloudBillingException
     */
    public boolean insertBillingStatusHistory(String subscription, String accountNumber, String tenantDomain,
                                              String type, String status, String startDate, String endDate,
                                              String tenantId) throws CloudBillingException {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean executionResult = false;

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(insertBillingStatusHistoryQuery);
                ps.setString(1, subscription);
                ps.setString(2, accountNumber);
                ps.setString(3, tenantDomain);
                ps.setString(4, type);
                ps.setString(5, status);
                ps.setDate(6, new Date(format.parse(startDate).getTime()));
                ps.setDate(7, new Date(format.parse(endDate).getTime()));
                ps.setString(8, tenantId);
                ps.execute();
                executionResult = true;
            }
        } catch (SQLException | CloudMgtException | ParseException e) {
            throw new CloudBillingException(
                    "Failed inserting BILLING_STATUS_HISTORY table for tenant : " + tenantDomain, e);
        } finally {
            CloudMgtDBConnectionManager.closePSAndConnection(ps, conn);
        }
        return executionResult;
    }

    /**
     * Insert data to the BILLING_ACCOUNT_AMENDMENTS
     *
     * @param subscription  cloud subscription
     * @param accountNumber billing account number
     * @param ratePlanId    tenant domain
     * @param startDate     subscription start date
     * @param endDate       subscrition enddate
     * @return
     * @throws CloudBillingException
     */
    public boolean insertBillingAccountAmendments(String accountNumber, String ratePlanId, String startDate,
                                                  String endDate, String subscription) throws CloudBillingException {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean executionResult = false;

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(insertBillingAccountAmendmentsQuery);
                ps.setString(1, accountNumber);
                ps.setString(2, ratePlanId);
                ps.setDate(3, new Date(format.parse(startDate).getTime()));
                ps.setDate(4, new Date(format.parse(endDate).getTime()));
                ps.setString(5, subscription);
                ps.execute();
                executionResult = true;
            }
        } catch (SQLException | CloudMgtException | ParseException e) {
            throw new CloudBillingException(
                    "Failed to update BILLING_ACCOUNT_AMENDMENTS table for account : " + accountNumber, e);
        } finally {
            CloudMgtDBConnectionManager.closePSAndConnection(ps, conn);
        }
        return executionResult;
    }

}
