/*
 *  Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.cloud.users.common;

/**
 * Constants for Cloud Management configuration
 */
public class CloudConstants {
    public static final String APP_ROLE_PREFIX = "app_";
    public static final String JAVA_NAMING_FACTORY_INITIAL = "java.naming.factory.initial";
    public static final String CARBON_JAVA_URL_CONTEXT_FACTORY =
            "org.wso2.carbon.tomcat.jndi.CarbonJavaURLContextFactory";
    public static final String DATA_SOURCE_NAME = "jdbc/cloud_mgt";

    public static final String PERMISSION_EXECUTE = "ui.execute";
    public static final String CLOUD_DEFAULT_ROLE = "default";
    //Re-define API-M constants so that we don't have to add API-M dependencies
    public static final int AM_CREATOR_APIMGT_EXECUTION_ID = 200;
    public static final int AM_CREATOR_GOVERNANCE_EXECUTION_ID = 201;
    public static final int AM_PUBLISHER_APIMGT_EXECUTION_ID = 202;
    public static final String APIMGT_REGISTRY_LOCATION = "/apimgt";
    public static final String API_APPLICATION_DATA_LOCATION = APIMGT_REGISTRY_LOCATION + "/applicationdata";

    //SQL Queries
    public static final String SELECT_TENANT_DISPLAY_NAME_FOR_TENANT_DOMAIN_QUERY =
            "SELECT displayName FROM ORGANIZATIONS WHERE tenantDomain = ?;";

    public static final String ANY_USERS_FILTER = "*";
    public static final int UNLIMITED_USERS_LIMIT = -1;

    /**
     * Constants for permissions
     */
    public static final class Permissions {
        public static final String API_CREATE = "/permission/admin/manage/api/create";
        public static final String API_PUBLISH = "/permission/admin/manage/api/publish";
    }
}

