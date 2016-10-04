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
 * Represents the constants used in RegistryDataDeleter class
 */
public class RegistryQueries {

    public static final String QUERY_DELETE_CLUSTER_LOCK = "DELETE FROM REG_CLUSTER_LOCK WHERE REG_TENANT_ID = ?";

    public static final String QUERY_DELETE_LOG = "DELETE FROM REG_LOG WHERE REG_TENANT_ID = ?";

    public static final String QUERY_DELETE_ASSOCIATION = "DELETE FROM REG_ASSOCIATION WHERE REG_TENANT_ID = ?";

    public static final String QUERY_DELETE_SNAPSHOT = "DELETE FROM REG_SNAPSHOT WHERE REG_TENANT_ID = ?";

    public static final String QUERY_DELETE_RESOURCE_COMMENT =
            "DELETE FROM REG_RESOURCE_COMMENT WHERE REG_TENANT_ID = ?";

    public static final String QUERY_DELETE_COMMENT = "DELETE FROM REG_COMMENT WHERE REG_TENANT_ID = ?";

    public static final String QUERY_DELETE_RESOURCE_RATING = "DELETE FROM REG_RESOURCE_RATING WHERE REG_TENANT_ID = ?";

    public static final String QUERY_DELETE_RATING = "DELETE FROM REG_RATING WHERE REG_TENANT_ID = ?";

    public static final String QUERY_DELETE_RESOURCE_TAG = "DELETE FROM REG_RESOURCE_TAG WHERE REG_TENANT_ID = ?";

    public static final String QUERY_DELETE_TAG = "DELETE FROM REG_TAG WHERE REG_TENANT_ID = ?";

    public static final String QUERY_DELETE_RESOURCE_PROPERTY =
            "DELETE FROM REG_RESOURCE_PROPERTY WHERE REG_TENANT_ID = ?";

    public static final String QUERY_DELETE_PROPERTY = "DELETE FROM REG_PROPERTY WHERE REG_TENANT_ID = ?";

    public static final String QUERY_DELETE_RESOURCE_HISTORY =
            "DELETE FROM REG_RESOURCE_HISTORY WHERE REG_TENANT_ID = ?";

    public static final String QUERY_DELETE_CONTENT_HISTORY = "DELETE FROM REG_CONTENT_HISTORY WHERE REG_TENANT_ID = ?";

    public static final String QUERY_DELETE_RESOURCE = "DELETE FROM REG_RESOURCE WHERE REG_TENANT_ID = ?";

    public static final String QUERY_DELETE_CONTENT = "DELETE FROM REG_CONTENT WHERE REG_TENANT_ID = ?";

    public static final String QUERY_DELETE_PATH = "DELETE FROM REG_PATH WHERE REG_TENANT_ID = ?";

    private RegistryQueries() {
    }
}
