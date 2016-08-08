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

package org.wso2.carbon.cloud.tenantdeletion.constants;

/**
 * Represents the constants used in DataAccessManager
 */
public class DeletionQueries {

    private static final String USER_LOGIN = DeletionConstants.USER_LOGIN;
    private static final String DELETION_TENANTS = DeletionConstants.DELETION_TENANTS;
    private static final String DELETION_STATUS = DeletionConstants.DELETION_STATUS;
    private static final String DELETION_EXCLUSION_TENANTS = DeletionConstants.DELETION_EXCLUSION_TENANTS;

    //USER_LOGIN Table
    public static final String QUERY_INSERT_USER_LOGIN_INFO = "INSERT INTO " + USER_LOGIN +
                                                              " (TENANT_ID, TENANT_DOMAIN, LAST_LOGIN_DATE) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE LAST_LOGIN_DATE =" +
                                                              " ? ";

    public static final String QUERY_GET_ALL_TENANT_IDS_FROM_USER_LOGIN_TABLE =
            "SELECT TENANT_ID FROM " + USER_LOGIN + " WHERE LAST_LOGIN_DATE BETWEEN ? AND ?";

    //DELETE Table
    public static final String QUERY_INSERT_DELETE_TENANT_INFO = "INSERT INTO " + DELETION_TENANTS +
                                                                 " (TENANT_ID, TENANT_DOMAIN) VALUES (?,?) ON DUPLICATE KEY UPDATE TENANT_ID = ? ";

    public static final String QUERY_GET_ALL_TENANT_IDS_FROM_DELETE_TENANTS_TABLE =
            "SELECT TENANT_ID FROM " + DELETION_TENANTS;

    public static final String QUERY_GET_ALL_TENANT_DOMAINS_FROM_DELETE_TENANTS_TABLE =
            "SELECT TENANT_DOMAIN FROM " + DELETION_TENANTS;

    public static final String QUERY_EMPTY_DELETE_TABLE = "DELETE FROM " + DELETION_TENANTS;

    public static final String QUERY_REMOVE_TENANTS_FROM_DELETE_LIST =
            "DELETE FROM " + DELETION_TENANTS + " WHERE TENANT_ID = ?";

    public static final String QUERY_REMOVE_TENANTS_FROM_DELETE_LIST_WITH_DOMAIN_ =
            "DELETE FROM " + DELETION_TENANTS + " WHERE TENANT_DOMAIN = ?";

    public static final String QUERY_SET_DELETION_STATUS_FOR_GIVEN_STATUS =
            "UPDATE " + DELETION_TENANTS + " SET %%columnName = ? WHERE TENANT_DOMAIN= ?";

    public static final String QUERY_GET_DELETED_TENANT_FLAGS =
            "SELECT * FROM " + DELETION_TENANTS + " WHERE TENANT_DOMAIN= ?";

    public static final String QUERY_GET_ALL_DELETE_TENANTS =
            "SELECT TENANT_ID, TENANT_DOMAIN FROM " + DELETION_TENANTS + " WHERE %%columnName = 0";

    public static final String QUERY_GET_DELETE_TENANTS =
            "SELECT TENANT_ID, TENANT_DOMAIN FROM " + DELETION_TENANTS + " WHERE %%columnName = 0 LIMIT ?";

    public static final String QUERY_GET_DELETION_COLUMN_NAMES = "SHOW COLUMNS FROM " + DELETION_TENANTS;

    //DELETION_STATUS Table
    public static final String QUERY_RESET_DELETION_FLAGS =
            "UPDATE " + DELETION_STATUS + " SET STATUS = 0 WHERE TYPE= ?";

    public static final String QUERY_GET_DELETION_FLAG_LIST = "SELECT TYPE FROM " + DELETION_STATUS;

    public static final String QUERY_GET_DELETION_STATUS_FLAG =
            "SELECT STATUS FROM " + DELETION_STATUS + " WHERE TYPE = ?";

    public static final String QUERY_SET_DELETION_STATUS =
            "UPDATE " + DELETION_STATUS + " SET STATUS = 1 WHERE TYPE= ?";

    public static final String QUERY_GET_TYPE_DELETION_STATUS =
            "SELECT STATUS FROM " + DELETION_STATUS + " WHERE TYPE = ?";

    public static final String QUERY_GET_DELETION_LIMIT =
            "SELECT STATUS FROM " + DELETION_STATUS + " WHERE TYPE = 'DELETION_LIMIT'";

    //BILLING_STATUS Table
    public static final String QUERY_GET_PAID_TENANT_DOMAIN_LIST =
            "SELECT TENANT_DOMAIN FROM BILLING_STATUS WHERE STATUS = 'ACTIVE' AND TYPE = 'PAID'";

    public static final String QUERY_GET_PAID_TENANT =
            "SELECT TENANT_DOMAIN FROM BILLING_STATUS WHERE STATUS = 'ACTIVE' AND TYPE = 'PAID' AND TENANT_DOMAIN = ?";

    //EXCLUSION Table
    public static final String QUERY_GET_EXCLUSION_TENANT_ID_LIST =
            "SELECT TENANT_ID FROM " + DELETION_EXCLUSION_TENANTS;

    //UM_TENANT from user manager table
    public static final String QUERY_GET_TENANT_ADMIN_EMAIL = "SELECT UM_EMAIL FROM UM_TENANT WHERE UM_DOMAIN_NAME = ?";

    private DeletionQueries() {
    }
}
