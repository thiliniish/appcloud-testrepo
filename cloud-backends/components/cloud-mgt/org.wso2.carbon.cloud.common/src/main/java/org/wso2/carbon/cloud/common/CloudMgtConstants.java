/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * Constants for Cloud components
 */
public class CloudMgtConstants {
    public static final String CONFIG_FOLDER = "cloud";

    public static final String CONFIG_NAMESPACE = "http://www.wso2.org/cloud/";
    public static final String CONFIG_FILE_NAME = "cloud-mgt.xml";

    public static final String SECURE_VAULT_NS = "http://org.wso2.securevault/configuration";
    public static final String SECRET_ALIAS_ATTR_NAME = "secretAlias";

    public static final String DENY = "deny:";
    public static final String REGISTRY_GET = "REGISTRY_GET";
    public static final String REGISTRY_PUT = "REGISTRY_PUT";
    public static final String REGISTRY_DELETE = "REGISTRY_DELETE";

    public static final String TENANT_ROLES_ROLE = "TenantRoles.Role";

    //HTTP headers
    public static final String CONTENT_ID = "Content-ID";
    public static final String CONTENT_TYPE = "Content-Type";

    //Cloud datasource properties
    public static final String CLOUD_DB_PROPERTY = "CloudDatasourceName";
    public static final String SUBSCRIPTION_TYPE_PLACEHOLDER = "$SUBSCRIPTIONTYPE";

    public static final int REINVITE_THRESHOLD_COUNT = 3;
    public static final int REINVITE_TIME_LIMIT_IN_HOURS = 1;
}

