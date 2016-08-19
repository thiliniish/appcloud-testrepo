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

package org.wso2.carbon.cloud.tenantdeletion.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.cloud.tenantdeletion.DeletionManager;
import org.wso2.carbon.cloud.tenantdeletion.beans.DeletedTenant;
import org.wso2.carbon.cloud.tenantdeletion.constants.DeletionConstants;
import org.wso2.carbon.cloud.tenantdeletion.constants.DeletionQueries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represent the data access layer to access database and execute mysql queries
 */
public class DataAccessManager {

    private static final Log LOG = LogFactory.getLog(DataAccessManager.class);
    private static final DataAccessManager instance = new DataAccessManager();

    public static DataAccessManager getInstance() {
        return instance;
    }

    /**
     * Updates database user_login table  with tenant details
     *
     * @param id          Tenant id
     * @param domainName  Tenant Domain name
     * @param currentDate Current Date -format(yyyy-MM-dd)
     */
    public void insertUserLoginInfo(int id, String domainName, String currentDate) {
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            String query = DeletionQueries.QUERY_INSERT_USER_LOGIN_INFO;
            connection = DataConnectionManager.getCloudMgtDbConnection();
            if (connection != null) {
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, id);
                preparedStatement.setString(2, domainName);
                preparedStatement.setString(3, currentDate);
                preparedStatement.setString(4, currentDate);
                preparedStatement.executeUpdate();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Updated Tenant Login to database...");
                }
            } else {
                LOG.error("Database connection is null, and unable to update database user login table");
            }
        } catch (TenantDeletionException | SQLException e) {
            LOG.error("SQL Exception occurred while executing query", e);
        } finally {
            closeConnection(connection);
            closePreparedStatement(preparedStatement);
        }
    }

    /**
     * Check a tenant is paid prior to inserting login info
     *
     * @param id          Tenant id
     * @param domainName  Tenant Domain name
     * @param currentDate Current Date -format(yyyy-MM-dd)
     */
    public void insertCloudOperationInfo(int id, String domainName, String currentDate) {
        if (isPaidTenant(domainName)) {
            insertUserLoginInfo(id, domainName, currentDate);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Not Updating User Activity Info due to unpaid tenant");
            }
        }
    }

    /**
     * Updates database tenant_delete table with tenant details
     *
     * @param deleteMap Inactive tenant list
     */
    public void insertInactiveTenantsInfo(Map<Integer, String> deleteMap) {
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            String query = DeletionQueries.QUERY_INSERT_DELETE_TENANT_INFO;
            int counter = 0;
            connection = DataConnectionManager.getCloudMgtDbConnection();
            preparedStatement = connection.prepareStatement(query);
            for (Map.Entry<Integer, String> entry : deleteMap.entrySet()) {
                String tenantDomain = entry.getValue();
                int tenantID = entry.getKey();
                preparedStatement.setInt(1, tenantID);
                preparedStatement.setString(2, tenantDomain);
                preparedStatement.setInt(3, tenantID);
                preparedStatement.addBatch();
                counter++;
                //Default execution is 1000 items because some JDBC drivers  may have conf limitation on batch length.
                if (counter % DeletionConstants.COUNTER_VALUE == 0 || counter == deleteMap.size() - 1) {
                    preparedStatement.executeBatch();
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Updated Tenants_for_delete to the database");
            }
        } catch (TenantDeletionException | SQLException e) {
            LOG.error("SQL Exception occurred while executing query", e);
        } finally {
            closePreparedStatement(preparedStatement);
            closeConnection(connection);
        }
    }

    /**
     * Gets all active tenant id list from the database user_login table
     *
     * @param startDate start date of the date range
     * @param endDate   end date of the date range
     * @return tenant id list of all active tenants
     */
    public List<Integer> getAllActiveTenantIdList(String startDate, String endDate) {
        List<Integer> list = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Connection connection = null;
        try {
            String query = DeletionQueries.QUERY_GET_ALL_TENANT_IDS_FROM_USER_LOGIN_TABLE;
            connection = DataConnectionManager.getCloudMgtDbConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, startDate);
            preparedStatement.setString(2, endDate);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                list.add(resultSet.getInt(DeletionConstants.TENANT_ID));
            }
        } catch (TenantDeletionException | SQLException e) {
            LOG.error("SQL Exception occurred while executing query", e);
        } finally {
            closeResultSet(resultSet);
            closePreparedStatement(preparedStatement);
            closeConnection(connection);
        }
        return list;
    }

    /**
     * Gets all inactive tenant Id list from database tenant_delete table
     *
     * @return tenant id list of all inactive tenants
     */
    public List<Integer> getAllInactiveTenantIdList() {
        List<Integer> list = new ArrayList<>();
        ResultSet resultSet = null;
        Statement statement = null;
        Connection connection = null;
        try {
            connection = DataConnectionManager.getCloudMgtDbConnection();
            statement = connection.createStatement();
            String query = DeletionQueries.QUERY_GET_ALL_TENANT_IDS_FROM_DELETE_TENANTS_TABLE;
            resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                list.add(resultSet.getInt(DeletionConstants.TENANT_ID));
            }
        } catch (TenantDeletionException | SQLException e) {
            LOG.error("Error while executing the query", e);
        } finally {
            closeResultSet(resultSet);
            closeStatement(statement);
            closeConnection(connection);
        }
        return list;
    }

    /**
     * Gets all inactive tenant domain list from database tenant_delete table
     *
     * @return tenant domain list of all inactive tenants
     */
    public List<String> getAllInactiveTenantDomainList() {
        List<String> list = new ArrayList<>();
        ResultSet resultSet = null;
        Statement statement = null;
        Connection connection = null;
        try {
            connection = DataConnectionManager.getCloudMgtDbConnection();
            statement = connection.createStatement();
            String query = DeletionQueries.QUERY_GET_ALL_TENANT_DOMAINS_FROM_DELETE_TENANTS_TABLE;
            resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                list.add(resultSet.getString(DeletionConstants.TENANT_DOMAIN));
            }
        } catch (TenantDeletionException | SQLException e) {
            LOG.error("Error while executing the query", e);
        } finally {
            closeResultSet(resultSet);
            closeStatement(statement);
            closeConnection(connection);
        }
        return list;
    }

    /**
     * Removes all data in the delete table
     */
    public void clearTenantDeletionTable() {
        Statement statement = null;
        Connection connection = null;
        try {
            connection = DataConnectionManager.getCloudMgtDbConnection();
            statement = connection.createStatement();
            String query = DeletionQueries.QUERY_EMPTY_DELETE_TABLE;
            statement.executeUpdate(query);
        } catch (TenantDeletionException | SQLException e) {
            LOG.error("Error while executing the query", e);
        } finally {
            closeStatement(statement);
            closeConnection(connection);
        }
    }

    /**
     * Gets set of ACTIVE and PAID tenant list from CloudMgt database
     *
     * @return paid tenant domain list
     */
    public List<String> getPaidTenantList() {
        List<String> list = new ArrayList<>();
        ResultSet resultSet = null;
        Statement statement = null;
        Connection connection = null;
        try {
            connection = DataConnectionManager.getCloudMgtDbConnection();
            statement = connection.createStatement();
            String query = DeletionQueries.QUERY_GET_PAID_TENANT_DOMAIN_LIST;
            resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                list.add(resultSet.getString(DeletionConstants.TENANT_DOMAIN));
            }
        } catch (TenantDeletionException | SQLException e) {
            LOG.error("Error while executing the query", e);
        } finally {
            closeResultSet(resultSet);
            closeStatement(statement);
            closeConnection(connection);
        }
        return list;
    }

    /**
     * Returns the status of an account whether it is a paid account or not
     *
     * @param tenantDomain String tenantd domain parameter
     * @return Tenant paid account status
     */
    public boolean isPaidTenant(String tenantDomain) {
        boolean isPaid = false;
        ResultSet resultSet = null;
        PreparedStatement statement = null;
        Connection connection = null;
        try {
            String query = DeletionQueries.QUERY_GET_PAID_TENANT;
            connection = DataConnectionManager.getCloudMgtDbConnection();
            statement = connection.prepareStatement(query);
            statement.setString(1, tenantDomain);
            resultSet = statement.executeQuery();
            isPaid = resultSet.next();
        } catch (TenantDeletionException | SQLException e) {
            LOG.error("Error while executing the query", e);
        } finally {
            closeResultSet(resultSet);
            closePreparedStatement(statement);
            closeConnection(connection);
        }
        return isPaid;
    }

    /**
     * Returns exclusive tenant list in the tenant deletion database
     *
     * @return exclusive tenant id list
     */
    public List<Integer> getExclusiveTenantList() {
        List<Integer> list = new ArrayList<>();
        ResultSet resultSet = null;
        Statement statement = null;
        Connection connection = null;
        try {
            connection = DataConnectionManager.getCloudMgtDbConnection();
            statement = connection.createStatement();
            String query = DeletionQueries.QUERY_GET_EXCLUSION_TENANT_ID_LIST;
            resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                list.add(resultSet.getInt(DeletionConstants.TENANT_ID));
            }
        } catch (TenantDeletionException | SQLException e) {
            LOG.error("Error while executing the query", e);
        } finally {
            closeResultSet(resultSet);
            closeStatement(statement);
            closeConnection(connection);
        }
        return list;
    }

    /**
     * Removes conf set of tenants from delete table in the database
     *
     * @param removeList tenant id list
     */
    public void removeTenantsFromDeleteList(List<Integer> removeList) {
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            String query = DeletionQueries.QUERY_REMOVE_TENANTS_FROM_DELETE_LIST;
            int counter = 0;
            connection = DataConnectionManager.getCloudMgtDbConnection();
            preparedStatement = connection.prepareStatement(query);
            for (int i = 0; i < removeList.size(); i++) {
                Integer tenantId = removeList.get(i);
                preparedStatement.setInt(1, tenantId);
                preparedStatement.addBatch();
                counter++;
                //Default execution is 1000 items because some JDBC drivers  may have conf limitation on batch length.
                if (counter % DeletionConstants.COUNTER_VALUE == 0 || i == removeList.size() - 1) {
                    preparedStatement.executeBatch();
                }
            }
        } catch (TenantDeletionException | SQLException e) {
            LOG.error("SQL Exception occurred while executing query", e);
        } finally {
            closePreparedStatement(preparedStatement);
            closeConnection(connection);
        }
    }

    /**
     * Removes conf set of tenants from delete table in the database
     *
     * @param removeList tenant domain list
     */
    public void removeTenantFromDeleteList(List<String> removeList) {
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            String query = DeletionQueries.QUERY_REMOVE_TENANTS_FROM_DELETE_LIST_WITH_DOMAIN;
            int counter = 0;
            connection = DataConnectionManager.getCloudMgtDbConnection();
            preparedStatement = connection.prepareStatement(query);
            for (int i = 0; i < removeList.size(); i++) {
                String tenantDomain = removeList.get(i);
                preparedStatement.setString(1, tenantDomain);
                preparedStatement.addBatch();
                counter++;
                //Default execution is 1000 items because some JDBC drivers  may have conf limitation on batch length.
                if (counter % DeletionConstants.COUNTER_VALUE == 0 || i == removeList.size() - 1) {
                    preparedStatement.executeBatch(); // Execute every 1000 items.
                }
            }
        } catch (TenantDeletionException | SQLException e) {
            LOG.error("SQL Exception occurred while executing query", e);
        } finally {
            closePreparedStatement(preparedStatement);
            closeConnection(connection);
        }
    }

    /**
     * Checks deletion flag status and return status
     *
     * @param type type of status (0,1,2)
     * @return deletion status
     */
    public boolean getDeletionStatus(String type) {
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        boolean status = false;
        if (!type.isEmpty()) {
            ResultSet resultSet = null;
            try {
                String query = DeletionQueries.QUERY_GET_DELETION_STATUS_FLAG;
                connection = DataConnectionManager.getCloudMgtDbConnection();
                if (connection != null) {
                    preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setString(1, type);
                    resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        status = resultSet.getBoolean(DeletionConstants.STATUS);
                    }
                } else {
                    DeletionManager.getInstance().stopTimer();
                    return false;
                }
            } catch (TenantDeletionException | SQLException e) {
                LOG.error("SQL Exception occurred while executing query", e);
            } finally {
                closeConnection(connection);
                closePreparedStatement(preparedStatement);
                closeResultSet(resultSet);
                closeConnection(connection);
            }
        }
        return status;
    }

    /**
     * Updates the type of flag after finishing deletion process for the specific tenant
     *
     * @param type           deletion type (API, APP, CONFIG, ..)
     * @param tenantDomain   tenantDomain
     * @param deletionStatus deletion status (0,1,2)
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value =
            "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING") public void raiseDeletionFlag(
            String type, String tenantDomain, int deletionStatus) {
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            String columnName = new DeletionColumnEnum().verifyColumnName(type);
            if (columnName != null) {
                String query = DeletionQueries.QUERY_SET_DELETION_STATUS_FOR_GIVEN_STATUS;
                query = query.replaceAll("%%columnName", columnName);
                connection = DataConnectionManager.getCloudMgtDbConnection();
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, deletionStatus);
                preparedStatement.setString(2, tenantDomain);
                preparedStatement.executeUpdate();
            } else {
                LOG.error("Column name was not matched with the deletion type" + type);
            }
        } catch (TenantDeletionException | SQLException e) {
            LOG.error("SQL Exception occurred while executing query", e);
        } finally {
            closePreparedStatement(preparedStatement);
            closeConnection(connection);
        }
    }

    /**
     * Updates the type of the flag after completing all deletion process
     *
     * @param type deletion type
     */
    public void raiseDeletionFlag(String type) {
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            String query = DeletionQueries.QUERY_SET_DELETION_STATUS;
            connection = DataConnectionManager.getCloudMgtDbConnection();
            connection = DataConnectionManager.getCloudMgtDbConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, type);
            preparedStatement.executeUpdate();
            if (LOG.isDebugEnabled()) {
                LOG.debug(type + " deletion is complete");
            }
        } catch (TenantDeletionException | SQLException e) {
            LOG.error("SQL Exception occurred while executing query", e);
        } finally {
            closePreparedStatement(preparedStatement);
            closeConnection(connection);
        }
    }

    /**
     * Returns conf list of inactive tenant Ids
     *
     * @param type Deletion type
     * @return tenant id list
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value =
            "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING") public Map<String, Integer>
    getAllInactiveTenants(
            String type) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Connection connection = null;
        Map<String, Integer> tenantMap = new HashMap<>();
        try {
            String query = DeletionQueries.QUERY_GET_ALL_DELETE_TENANTS.replaceAll("%%columnName", type);
            connection = DataConnectionManager.getCloudMgtDbConnection();
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                if (resultSet.getInt(DeletionConstants.TENANT_ID) != MultitenantConstants.SUPER_TENANT_ID &&
                    resultSet.getInt(DeletionConstants.TENANT_ID) != -1) {
                    tenantMap.put(resultSet.getString(DeletionConstants.TENANT_DOMAIN),
                                  resultSet.getInt(DeletionConstants.TENANT_ID));
                }
            }
        } catch (TenantDeletionException | SQLException e) {
            LOG.error("SQL Exception occurred while executing query", e);
        } finally {
            closePreparedStatement(preparedStatement);
            closeResultSet(resultSet);
            closeConnection(connection);
        }
        return tenantMap;
    }

    /**
     * Returns conf specific list of inactive tenant ids for the given limit
     *
     * @param type deletion type
     * @return tenant id list
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value =
            "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING") public Map<String, Integer> getInactiveTenants(
            String type, int deletionLimit) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Connection connection = null;
        Map<String, Integer> tenantMap = new HashMap<>();
        try {
            String query = DeletionQueries.QUERY_GET_DELETE_TENANTS;
            query = query.replaceAll("%%columnName", type);
            connection = DataConnectionManager.getCloudMgtDbConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, deletionLimit);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                if (resultSet.getInt(DeletionConstants.TENANT_ID) != MultitenantConstants.SUPER_TENANT_ID &&
                    resultSet.getInt(DeletionConstants.TENANT_ID) != -1) {
                    tenantMap.put(resultSet.getString(DeletionConstants.TENANT_DOMAIN),
                                  resultSet.getInt(DeletionConstants.TENANT_ID));
                }
            }
        } catch (TenantDeletionException | SQLException e) {
            LOG.error("SQL Exception occurred while executing query", e);
        } finally {
            closePreparedStatement(preparedStatement);
            closeResultSet(resultSet);
            closeConnection(connection);
        }
        return tenantMap;
    }

    /**
     * Resets deletion flags after finishing deletion process
     */
    public void resetDatabaseFlags() {
        PreparedStatement preparedStatement = null;
        List<String> deletionFlagList = getDeletionFlagList();
        Connection connection = null;
        try {
            connection = DataConnectionManager.getCloudMgtDbConnection();
            connection.setAutoCommit(false);
            String query = DeletionQueries.QUERY_RESET_DELETION_FLAGS;
            for (String deletionFlag : deletionFlagList) {
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, deletionFlag);
                preparedStatement.executeUpdate();
            }
            connection.commit();
        } catch (TenantDeletionException | SQLException e) {
            LOG.error("SQL Exception occurred while executing query", e);
        } finally {
            closePreparedStatement(preparedStatement);
            closeConnection(connection);
        }
    }

    /**
     * Returns conf list of deletion flag name list in the database
     *
     * @return deletion flag name list
     */
    public List<String> getDeletionFlagList() {
        List<String> list = new ArrayList<>();
        ResultSet resultSet = null;
        Statement statement = null;
        Connection connection = null;
        try {
            connection = DataConnectionManager.getCloudMgtDbConnection();
            statement = connection.createStatement();
            String query = DeletionQueries.QUERY_GET_DELETION_FLAG_LIST;
            resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                list.add(resultSet.getString(DeletionConstants.TYPE));
            }
        } catch (TenantDeletionException | SQLException e) {
            LOG.error("Error while executing the query", e);
        } finally {
            closeResultSet(resultSet);
            closeStatement(statement);
            closeConnection(connection);
        }
        return list;
    }

    /**
     * Returns conf DeletedTenant object which contains deleted flags and tenant domain
     *
     * @param tenantDomain tenant domain
     * @return DeletedTenant object
     */
    public DeletedTenant getDeletedTenants(String tenantDomain) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Connection connection = null;
        DeletedTenant tenantObj = new DeletedTenant();
        try {
            String query = DeletionQueries.QUERY_GET_DELETED_TENANT_FLAGS;
            connection = DataConnectionManager.getCloudMgtDbConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, tenantDomain);
            resultSet = preparedStatement.executeQuery();
            //Sets info of the tenant object
            while (resultSet.next()) {
                tenantObj.setTenantDomain(tenantDomain);
                tenantObj.setApiFlag(resultSet.getInt(DeletionConstants.API));
                tenantObj.setAppFlag(resultSet.getInt(DeletionConstants.APP));
                tenantObj.setCloudMgtFlag(resultSet.getInt(DeletionConstants.CLOUD_MGT));
                tenantObj.setUserMgtFlag(resultSet.getInt(DeletionConstants.USER_MGT));
                tenantObj.setLdapFlag(resultSet.getInt(DeletionConstants.LDAP));
                tenantObj.setConfigPubstore(resultSet.getInt(DeletionConstants.CONFIG_PUBSTORE));
                tenantObj.setConfigBps(resultSet.getInt(DeletionConstants.CONFIG_BPS));
                tenantObj.setConfigCloudMgt(resultSet.getInt(DeletionConstants.CONFIG_CLOUD_MGT));
                tenantObj.setConfigIs(resultSet.getInt(DeletionConstants.CONFIG_IS));
                tenantObj.setConfigSs(resultSet.getInt(DeletionConstants.CONFIG_SS));
                tenantObj.setConfigDas(resultSet.getInt(DeletionConstants.CONFIG_DAS));
                tenantObj.setConfigAf(resultSet.getInt(DeletionConstants.CONFIG_AF));
                tenantObj.setGovernanceFlag(resultSet.getInt(DeletionConstants.GOVERNANCE));
            }
        } catch (TenantDeletionException | SQLException e) {
            LOG.error("SQL Exception occurred while executing query", e);
        } finally {
            closeResultSet(resultSet);
            closePreparedStatement(preparedStatement);
            closeConnection(connection);
        }
        return tenantObj;
    }

    /**
     * Columns in DELETION_TENANTS table
     *
     * @return String list of Columns
     */
    public List<String> getColumnListOfDeletionTable() {
        List<String> list = new ArrayList<>();
        ResultSet resultSet = null;
        Statement statement = null;
        Connection connection = null;
        try {
            connection = DataConnectionManager.getCloudMgtDbConnection();
            statement = connection.createStatement();
            String query = DeletionQueries.QUERY_GET_DELETION_COLUMN_NAMES;
            resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                list.add(resultSet.getString(DeletionConstants.FIELD));
            }
        } catch (TenantDeletionException | SQLException e) {
            LOG.error("Error while executing the query", e);
        } finally {
            closeResultSet(resultSet);
            closeStatement(statement);
            closeConnection(connection);
        }
        return list;
    }

    /**
     * Executes delete queries with connection, query and tenantDomain
     *
     * @param conn         database connection
     * @param query        mysql query
     * @param tenantDomain tenant domain of the tenant
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value =
            "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING") public void executeDeleteQuery(
            Connection conn, String query, String tenantDomain) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, tenantDomain);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String errMsg = "Error executing query " + query + " for tenant: " + tenantDomain;
            LOG.error(errMsg, e);
        } finally {
            closePreparedStatement(preparedStatement);
        }
    }

    /**
     * Executes delete queries with connection, query and tenantID
     *
     * @param conn     database connection
     * @param query    mysql connection
     * @param tenantId tenant id
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value =
            "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING") public void executeDeleteQuery(
            Connection conn, String query, int tenantId) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = conn.prepareStatement(query);
            preparedStatement.setInt(1, tenantId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String errMsg = "Error executing query " + query + " for tenant: " + tenantId;
            LOG.error(errMsg, e);
        } finally {
            closePreparedStatement(preparedStatement);
        }
    }

    /**
     * Retrieving the current deletion limit recorded in the database for batch deletion
     *
     * @return current deletion limit
     */
    public int getDeletionLimit() {
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        int limit = 0;
        ResultSet resultSet = null;
        try {
            connection = DataConnectionManager.getCloudMgtDbConnection();
            String query = DeletionQueries.QUERY_GET_DELETION_LIMIT;
            if (connection != null) {
                preparedStatement = connection.prepareStatement(query);
                resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    limit = resultSet.getInt(DeletionConstants.STATUS);
                }
            } else {
                return limit;
            }
        } catch (TenantDeletionException | SQLException e) {
            LOG.error("SQL Exception occurred while executing query", e);
        } finally {
            closePreparedStatement(preparedStatement);
            closeResultSet(resultSet);
            closeConnection(connection);
        }
        return limit;
    }

    /**
     * Retrieve the type specific deletion status before executing the deletion class
     *
     * @param type Required type to check the status
     * @return List of type status
     */
    public List<Integer> getTypeDeletionStatus(String type) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Connection connection = null;
        List<Integer> list = new ArrayList<>();
        try {
            String query = DeletionQueries.QUERY_GET_TYPE_DELETION_STATUS;
            connection = DataConnectionManager.getCloudMgtDbConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, type);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                list.add(resultSet.getInt(DeletionConstants.STATUS));
            }
        } catch (TenantDeletionException | SQLException e) {
            LOG.error("SQL Exception occurred while executing query", e);
        } finally {
            closePreparedStatement(preparedStatement);
            closeResultSet(resultSet);
            closeConnection(connection);
        }
        return list;
    }

    /**
     * Retrieving tenant admin user email address for the specific tenant
     *
     * @param tenantDomain String tenant domain for the required tenant
     * @return String email address of the tenant admin user
     */
    public String getTenantAdminEmail(String tenantDomain) {
        String email = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            String query = DeletionQueries.QUERY_GET_TENANT_ADMIN_EMAIL;
            connection = DataConnectionManager.getUserMgtDbConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, tenantDomain);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                email = resultSet.getString(DeletionConstants.UM_EMAIL);
            }
        } catch (TenantDeletionException | SQLException e) {
            LOG.error("SQL Exception occurred while executing query", e);
        } finally {
            closeResultSet(resultSet);
            closePreparedStatement(preparedStatement);
            closeConnection(connection);
        }
        return email;
    }

    /**
     * Closing resultSet
     *
     * @param resultSet result set
     */
    public void closeResultSet(ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException e) {
            LOG.error("Failed to close resultSet", e);
        }
    }

    /**
     * Closing the prepared statement
     *
     * @param preparedStatement prepared statement
     */
    public void closePreparedStatement(PreparedStatement preparedStatement) {
        try {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        } catch (SQLException e) {
            LOG.error("Failed to close preparedStatement", e);
        }
    }

    /**
     * Closing the executeUpdate type query statements
     *
     * @param statement statement
     */
    public void closeStatement(Statement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            LOG.error("Failed to close statement", e);
        }
    }

    /**
     * Closing the connection
     *
     * @param connection connection
     */
    public void closeConnection(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            LOG.error("Failed to close statement", e);
        }
    }
}
