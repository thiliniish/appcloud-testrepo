/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.cloud.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * This class acts ass the database access class for the cloudmgt operations.
 */
public class CloudMgtDAO {
    private static final Log LOG = LogFactory.getLog(CloudMgtDAO.class);

    private static final String selectEmailFromTempInviteeQuery = "SELECT email FROM TEMP_INVITEE WHERE uuid=(?)";
    private static final String selectEmailFromTempRegistrationQuery =
            "SELECT email FROM TEMP_REGISTRATION WHERE uuid=(?)";
    private static final String selectRolesFromTempInviteeQuery =
            "SELECT roles FROM TEMP_INVITEE WHERE tenantDomain=(?) AND email=(?)";
    private static final String insertIntoTempInviteeQuery =
            "INSERT INTO TEMP_INVITEE (tenantDomain,email,uuid,roles,dateTime,isSelfSigned) VALUES (? , ? , ? , ?," +
            " CURRENT_TIMESTAMP, ?) ON DUPLICATE KEY UPDATE uuid=(?), roles=(?), isSelfSigned=(?) , " +
            "dateTime = CURRENT_TIMESTAMP";
    private static final String selectUUIDAndRolesOfTempInviteeQuery =
            "SELECT uuid, roles FROM TEMP_INVITEE WHERE email=(?) AND tenantDomain = (?)";
    private String selectRightwaveCloudSubscriptionQuery =
            "SELECT $SUBSCRIPTIONTYPE FROM RIGHTWAVE_CLOUD_SUBSCRIPTION WHERE TENANT_DOMAIN=(?) AND EMAIL=(?)";
    private String insertRightwaveCloudSubscriptionQuery =
            "INSERT INTO RIGHTWAVE_CLOUD_SUBSCRIPTION (TENANT_DOMAIN,$SUBSCRIPTIONTYPE,EMAIL) VALUES (?,?,?)";
    private String updateRightwaveCloudSubscriptionQuery =
            "UPDATE RIGHTWAVE_CLOUD_SUBSCRIPTION SET $SUBSCRIPTIONTYPE = ? WHERE TENANT_DOMAIN=? AND EMAIL=?;";
    private static final String selectRetryCountFromTempRegistrationQuery = "SELECT retryCount,dateTime " +
                                                                            "FROM TEMP_REGISTRATION WHERE email=(?)";
    private static final String resetRetryCountFromTempRegistrationQuery = "UPDATE TEMP_REGISTRATION SET " +
                                                                           "dateTime=CURRENT_TIMESTAMP,retryCount=1" +
                                                                           " WHERE email=(?)";
    private static final String updateRetryCountFromTempRegistrationQuery = "UPDATE TEMP_REGISTRATION SET " +
                                                                            "retryCount=(?) WHERE email=(?)";
    private static final String selectRetryCountFromTempInviteeQuery = "SELECT retryCount,dateTime FROM " +
                                                                       "TEMP_INVITEE WHERE email=(?)";
    private static final String resetRetryCountFromTempInviteeQuery = "UPDATE TEMP_INVITEE SET " +
                                                                      "dateTime=CURRENT_TIMESTAMP," +
                                                                      " retryCount=1 WHERE email=(?)";
    private static final String updateRetryCountFromTempInviteeQuery = "UPDATE TEMP_INVITEE SET " +
                                                                       "retryCount=(?) WHERE email=(?)";

    /**
     * This method returns the emails for the self-registered and the invited users from the cloud_mgt database
     *
     * @param uuid          is the uuid sent in the registration link to the user
     * @param isInvitedUser determines if this is an invited user or a self registered user
     * @return the email corresponding to the uuid
     * @throws CloudMgtException
     */
    public String getEmailForUUID(String uuid, boolean isInvitedUser) throws CloudMgtException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        String email = null;

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                if (isInvitedUser) {
                    ps = conn.prepareStatement(selectEmailFromTempInviteeQuery);
                } else {
                    ps = conn.prepareStatement(selectEmailFromTempRegistrationQuery);
                }
                ps.setString(1, uuid);
                resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    email = resultSet.getString("email");
                }
            } else {
                throw new CloudMgtException(
                        "An error occurred while obtaining a database connection from the cloudmgt db");
            }
        } catch (SQLException e) {
            throw new CloudMgtException("Failed to retrieve email for the uuid " + uuid, e);
        } catch (CloudMgtException e) {
            throw new CloudMgtException("Failed to get database connection for the cloudmgt database ", e);
        } finally {
            CloudMgtDBConnectionManager.closeAllConnections(ps, conn, resultSet);
        }
        return email;
    }

    /**
     * This method retrieves the roles which were added for the temp entry for the invited users
     *
     * @param tenantDomain
     * @param email
     * @return the roles as a comma seperated string
     * @throws CloudMgtException
     */
    public String getRolesOfTempInvitees(String tenantDomain, String email) throws CloudMgtException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        String roles = null;

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (conn != null) {
                ps = conn.prepareStatement(selectRolesFromTempInviteeQuery);
                ps.setString(1, tenantDomain);
                ps.setString(2, email);
                resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    roles = resultSet.getString("roles");
                }
            } else {
                throw new CloudMgtException(
                        "An error occurred while obtaining a database connection from the cloudmgt db");
            }
        } catch (SQLException e) {
            throw new CloudMgtException(
                    "Failed to retrieve the roles for the user " + email + " of the tenant domain "
                    + tenantDomain, e);
        } catch (CloudMgtException e) {
            throw new CloudMgtException("Failed to get database connection for the cloudmgt database ", e);
        } finally {
            CloudMgtDBConnectionManager.closeAllConnections(ps, conn, resultSet);
        }
        return roles;
    }

    /**
     * This adds a temp entry to the database for the invited users
     *
     * @param tenantDomain
     * @param email
     * @param uuid
     * @param roles
     * @param isSelfSigned
     * @return if the insert operation was a success or not
     * @throws CloudMgtException
     */
    public boolean insertIntoTempInvitee(String tenantDomain, String email, String uuid, String roles, int isSelfSigned)
            throws CloudMgtException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        boolean executionResult = false;

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            ps = conn.prepareStatement(insertIntoTempInviteeQuery);
            ps.setString(1, tenantDomain);
            ps.setString(2, email);
            ps.setString(3, uuid);
            ps.setString(4, roles);
            ps.setInt(5, isSelfSigned);
            ps.setString(6, uuid);
            ps.setString(7, roles);
            ps.setInt(8, isSelfSigned);
            ps.executeUpdate();
            executionResult = true;
        } catch (SQLException e) {
            throw new CloudMgtException(
                    "Failed to insert the temp invitee details for the " + email + " of the tenant domain " +
                    tenantDomain + " with the uuid " + uuid, e);
        } catch (CloudMgtException e) {
            throw new CloudMgtException("Failed to get database connection for the cloudmgt database ", e);
        } finally {
            CloudMgtDBConnectionManager.closeAllConnections(ps, conn, resultSet);
        }
        return executionResult;
    }

    /**
     * This method retrieves the uuid and the roles of temp invited users
     *
     * @param email
     * @param tenantDomain
     * @return a json object containing the roles and the uuid
     * @throws CloudMgtException
     */
    public JSONObject getRolesAndUUIDOfTempInvitees(String email, String tenantDomain) throws CloudMgtException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        String roles = null;
        String uuid = null;

        JSONObject resultObj = null;

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            ps = conn.prepareStatement(selectUUIDAndRolesOfTempInviteeQuery);
            ps.setString(1, email);
            ps.setString(2, tenantDomain);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                roles = resultSet.getString("roles");
                uuid = resultSet.getString("uuid");
            }
            resultObj = new JSONObject();
            resultObj.put("roles", roles);
            resultObj.put("uuid", uuid);
        } catch (SQLException e) {
            throw new CloudMgtException(
                    "Failed to retrieve the roles and uuid for the user " + email + " of the tenant domain " +
                    tenantDomain, e);
        } catch (CloudMgtException e) {
            throw new CloudMgtException("Failed to get database connection for the cloudmgt database ", e);
        } finally {
            CloudMgtDBConnectionManager.closeAllConnections(ps, conn, resultSet);
        }
        return resultObj;
    }

    /**
     * This is the method which selects the cloud subscriptions for a given user
     *
     * @param type
     * @param tenantDomain
     * @param email
     * @return if the subscription is available for the given cloud type
     * @throws CloudMgtException
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value =
            { "SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE",
              "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING" },
            justification = "Since a column name is passed as a parameter")
    public JSONObject selectCloudSubscription(String type, String tenantDomain, String email)
            throws CloudMgtException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        int subscriptionStatus = -1;

        JSONObject resultObj = null;

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            selectRightwaveCloudSubscriptionQuery = selectRightwaveCloudSubscriptionQuery.replace
                    (CloudMgtConstants.SUBSCRIPTION_TYPE_PLACEHOLDER, type);
            ps = conn.prepareStatement(selectRightwaveCloudSubscriptionQuery);
            ps.setString(1, tenantDomain);
            ps.setString(2, email);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                subscriptionStatus = resultSet.getInt(type);
            }
            if (subscriptionStatus == 0 || subscriptionStatus == 1) {
                resultObj = new JSONObject();
                resultObj.put(type, subscriptionStatus);
            }
        } catch (SQLException e) {
            throw new CloudMgtException(
                    "Failed to retrieve the subscription for the user " + email + " of the tenant domain " +
                    tenantDomain + " for the subscription type " + type, e);
        } catch (CloudMgtException e) {
            throw new CloudMgtException("Failed to get database connection for the cloudmgt database ", e);
        } finally {
            CloudMgtDBConnectionManager.closeAllConnections(ps, conn, resultSet);
        }
        return resultObj;
    }

    /**
     * This inserts a new record for the cloud subscription for the given user
     *
     * @param subscriptionType
     * @param tenantDomain
     * @param subscriptionValue
     * @param email
     * @throws CloudMgtException
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value =
            { "SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE",
              "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING" },
            justification = "Since a column name is passed as a parameter")
    public void insertCloudSubscription(String subscriptionType, String tenantDomain, int subscriptionValue,
                                        String email) throws CloudMgtException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;

        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            insertRightwaveCloudSubscriptionQuery = insertRightwaveCloudSubscriptionQuery.replace
                    (CloudMgtConstants.SUBSCRIPTION_TYPE_PLACEHOLDER, subscriptionType);
            ps = conn.prepareStatement(insertRightwaveCloudSubscriptionQuery);
            ps.setString(1, tenantDomain);
            ps.setInt(2, subscriptionValue);
            ps.setString(3, email);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new CloudMgtException(
                    "Failed to insert the subscription for the user " + email + " of the tenant domain " +
                    tenantDomain + " for the subscription type " + subscriptionType, e);
        } catch (CloudMgtException e) {
            throw new CloudMgtException("Failed to get database connection for the cloudmgt database ", e);
        } finally {
            CloudMgtDBConnectionManager.closeAllConnections(ps, conn, resultSet);
        }
    }

    /**
     * This method updates the cloud subscriptions for the given user
     *
     * @param subscriptionType
     * @param subscriptionValue
     * @param tenantDomain
     * @param email
     * @throws CloudMgtException
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value =
            { "SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE",
              "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING" },
            justification = "Since a column name is passed as a parameter")
    public void updateCloudSubscription(String subscriptionType, int subscriptionValue,
                                        String tenantDomain, String email) throws CloudMgtException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            updateRightwaveCloudSubscriptionQuery = updateRightwaveCloudSubscriptionQuery.replace
                    (CloudMgtConstants.SUBSCRIPTION_TYPE_PLACEHOLDER, subscriptionType);
            ps = conn.prepareStatement(updateRightwaveCloudSubscriptionQuery);
            ps.setInt(1, subscriptionValue);
            ps.setString(2, tenantDomain);
            ps.setString(3, email);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new CloudMgtException(
                    "Failed to update the subscription for the user " + email + " of the tenant domain " +
                    tenantDomain + " for the subscription type " + subscriptionType, e);
        } catch (CloudMgtException e) {
            throw new CloudMgtException("Failed to get database connection for the cloudmgt database ", e);
        } finally {
            CloudMgtDBConnectionManager.closeAllConnections(ps, conn, resultSet);
        }
    }

    /**
     * This method checks if the invitation to the email is permitted or not.
     * This is checked by verifying that the number of retries made for this particular email
     * does not exceed three times for the given hour.
     *
     * @param email
     * @param isInvitee
     * @return if the inviting of the given email is permitted or not
     * @throws CloudMgtException
     */
    public boolean isInvitePermitted(String email, boolean isInvitee) throws CloudMgtException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        boolean isRetryPermitted = false;
        CloudMgtUtils cloudMgtUtils = new CloudMgtUtils();
        int maxReInviteCount;
            try {
            maxReInviteCount = cloudMgtUtils.getCloudInviteeRetryCount();
            conn = CloudMgtDBConnectionManager.getDbConnection();
            if (isInvitee) {
                ps = conn.prepareStatement(selectRetryCountFromTempInviteeQuery);
            } else {
                ps = conn.prepareStatement(selectRetryCountFromTempRegistrationQuery);
            }
            ps.setString(1, email);
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                Timestamp dateTime = resultSet.getTimestamp("dateTime");
                int retryCount = resultSet.getInt("retryCount");

                if (LOG.isDebugEnabled()) {
                    LOG.debug("The retry count for the timestamp " + dateTime.toString() + " is " + retryCount);
                }
                Date newdateTime = new java.util.Date();
                Date sqlDate = (Date) new Date(dateTime.getTime());

                //Calculating the time difference(milliseconds) between the current time and the one in the table
                long diff = newdateTime.getTime() - sqlDate.getTime();

                //Calculating the time difference in hours
                long diffHours = diff / (60 * 60 * 1000);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("The time difference for the current date " + newdateTime.toString()
                              + " and the date of the registration " + sqlDate.toString()
                              + " is " + diffHours + " hours");
                }
                if (diffHours < 1 && retryCount >= maxReInviteCount) {

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("The email " + email + " has exceeded the maximum retry count for this hour. " +
                                  "Number of hours:" + diffHours + ". Retry Count: " + retryCount);
                    }
                    try {
                        updateRetryCount(retryCount, email, isInvitee);
                    } catch (CloudMgtException e) {
                        throw new CloudMgtException(
                                "Failed to update the retry count for the the user " +
                                "email " + email, e);
                    }
                    isRetryPermitted = false;
                } else if (diffHours >= CloudMgtConstants.REINVITE_TIME_LIMIT_IN_HOURS) {
                    try {
                        //Update the table and reset the request count
                        if (isInvitee) {
                            ps = conn.prepareStatement(resetRetryCountFromTempInviteeQuery);
                        } else {
                            ps = conn.prepareStatement(resetRetryCountFromTempRegistrationQuery);
                        }
                        ps.setString(1, email);
                        ps.executeUpdate();

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Resetting the dateTime column for the Temp Registration of the user " + email
                                      + " to " + newdateTime);
                        }
                        isRetryPermitted = true;
                    } catch (SQLException e) {
                        throw new CloudMgtException(
                                "Failed to reset the retry count for the temp registration for the user " +
                                "email " + email, e);
                    } finally {
                        ps.close();
                    }
                } else {
                    try {
                        updateRetryCount(retryCount, email, isInvitee);
                        isRetryPermitted = true;
                    } catch (CloudMgtException e) {
                        throw new CloudMgtException(
                                "Failed to update the retry count for the the user " +
                                "email " + email, e);
                    }
                }
            } else {

                if (LOG.isDebugEnabled()) {
                    LOG.debug("An email entry was not found in the database for the email " + email);
                }
                isRetryPermitted = true;
            }
        } catch (SQLException e) {
            throw new CloudMgtException(
                    "Failed to retrieve the retry count for the temp registration for the user email " + email, e);
        } catch (CloudMgtException e) {
            throw new CloudMgtException("Failed to get database connection for the cloudmgt database ", e);
        } finally {
            CloudMgtDBConnectionManager.closeAllConnections(ps, conn, resultSet);
        }
        return isRetryPermitted;
    }

    /**
     * Updates the retry count for the particular user in the TEMP invitee/registration tables
     *
     * @param currentRetryCount
     * @param email
     * @param isInvitee
     * @throws CloudMgtException
     */
    private void updateRetryCount(int currentRetryCount, String email, boolean isInvitee) throws CloudMgtException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        try {
            conn = CloudMgtDBConnectionManager.getDbConnection();
            int newRetryCount = currentRetryCount + 1;
            if (isInvitee) {
                ps = conn.prepareStatement(updateRetryCountFromTempInviteeQuery);
            } else {
                ps = conn.prepareStatement(updateRetryCountFromTempRegistrationQuery);
            }
            ps.setInt(1, newRetryCount);
            ps.setString(2, email);
            ps.executeUpdate();

            if (LOG.isDebugEnabled()) {
                LOG.debug("updating the retry count to " + newRetryCount + " for the email" + email);
            }
        } catch (SQLException e) {
            throw new CloudMgtException(
                    "Failed to update the retry count for the temp registration for the user " +
                    "email " + email, e);
        } finally {
            CloudMgtDBConnectionManager.closeAllConnections(ps, conn, resultSet);
        }
    }
}
