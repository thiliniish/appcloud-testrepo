/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
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

package org.wso2.carbon.cloud.billing.apihandler;

/**
 * API handler to block api invocation after the tenant deactivate from API Cloud
 * APIInvocationRestrictHandlerConstants class maintain the constants related to the handler
 */
public class APIInvocationRestrictHandlerConstants {

    //error handling constants
    public static final String BILLING_OUT_ERROR_CODE_NAME = "invocation_failed";
    public static final String API_BILLING_NS = "http://wso2.org/apimanager/billing";
    public static final String API_BILLING_NS_PREFIX = "amc";
    public static final String ERROR_MESSAGE = "Inactive tenant invocation";
    public static final String MESSAGE_CONTEXT_PROPERTY = "RESPONSE";
    public static final String CORS_HEADERS_ORIGIN = "Origin";
    public static final int CASH_SIZE = 7;

    //database connection constants
    public static final String CLOUD_DATASOURCE = "jdbc/cloud_mgt";
    public static final String SQL_SELECT_STATUS_FROM_BILLING_STATUS = "SELECT STATUS FROM BILLING_STATUS WHERE "
                                                                               + "TENANT_DOMAIN =(?) AND SUBSCRIPTION"
                                                                               + " LIKE 'api_cloud' AND TYPE LIKE "
                                                                               + "'PAID';";

    public static final String BILLING_INVOCATION_RESTRICTED_STATUS = "DISABLED";
}
