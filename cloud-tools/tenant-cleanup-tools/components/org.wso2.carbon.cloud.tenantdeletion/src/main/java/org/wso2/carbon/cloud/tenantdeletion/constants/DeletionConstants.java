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
 * Represents the constants used in the component
 */
public class DeletionConstants {

    public static final String AT_SYMBOL = "@";
    public static final String UNDERSCORE_SYMBOL = "_";
    public static final String SEPARATOR = ",";
    public static final String NEW_LINE = "\n";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TYPE = "TYPE";
    public static final String TENANT_ID = "TENANT_ID";
    public static final String TENANT_DOMAIN = "TENANT_DOMAIN";
    public static final String SERVER_KEY = "ServerKey";
    public static final String STATUS = "STATUS";
    public static final String START = "START";
    public static final String COMPLETE = "COMPLETE";
    public static final String FIELD = "Field";
    public static final String UM_EMAIL = "UM_EMAIL";
    public static final String DELETE = "delete";
    public static final int COUNTER_VALUE = 1000;

    //Deletion Status
    public static final int DELETION_SUCCESS_STATUS = 1;
    public static final int DELETION_ERROR_STATUS = 2;

    //System Properties
    public static final String API_DELETION_NAP_TIME = "API_DELETION_NAP_TIME";
    public static final String DELETION_NODE = "DELETION_NODE";
    public static final String DELETION_NODE_STATUS_TRUE = "true";

    //File path
    public static final String TENANT_DELETION_XML_FILE_PATH = "repository/conf/tenant_deletion.xml";
    public static final String TENANT_DELETION_ERROR_EMAIL_FILE_PATH =
            "repository/conf/email/TenantDeletionErrorNotification";
    public static final String TENANT_DELETION_COMPLETE_EMAIL_FILE_PATH = "repository/conf/email/TenantDeletionReport";
    public static final String TENANT_DELETION_CSV_EXPORT_FILE_PATH = "repository/data/tenantDeletionEmailList.csv";

    //Deletion timeouts
    public static final int DELETION_START_WAITING_TIME = 1800000; //half an hour
    public static final int COORDINATOR_CHECK_WAITING_TIME = 60000;
    public static final int DEPENDENCY_CHECK_WAITING_TIME = 60000;
    public static final int DEPENDENCY_CHECK_COMPLETE_TIME = 60000;
    public static final int NAP_TIME = 5000;

    //Deletion Types
    public static final String API = "API";
    public static final String LOWERCASEAPI = "api";
    public static final String APP = "APP";
    public static final String CLOUD_MGT = "CLOUD_MGT";
    public static final String LDAP = "LDAP";
    public static final String USER_MGT = "USER_MGT";
    public static final String GOVERNANCE = "GOVERNANCE";
    public static final String CONFIG = "CONFIG";
    public static final String CONFIG_PUBSTORE = "CONFIG_PUBSTORE";
    public static final String CONFIG_BPS = "CONFIG_BPS";
    public static final String CONFIG_CLOUD_MGT = "CONFIG_CLOUD_MGT";
    public static final String CONFIG_IS = "CONFIG_IS";
    public static final String CONFIG_SS = "CONFIG_SS";
    public static final String CONFIG_DAS = "CONFIG_DAS";
    public static final String CONFIG_AF = "CONFIG_AF";

    //Email properties
    public static final String DELETION_ERROR_MAIL_SUBJECT = "Tenant Deletion Error";
    public static final String DELETION_COMPLETE_MAIL_SUBJECT = "TenantDeletion has been completed";

    //Tenant deletion table Names
    public static final String USER_LOGIN = "USER_LOGIN";
    public static final String DELETION_TENANTS = "DELETION_TENANTS";
    public static final String DELETION_STATUS = "DELETION_STATUS";
    public static final String DELETION_EXCLUSION_TENANTS = "DELETION_EXCLUSION_TENANTS";

    public static final String JAVA_NAMING_FACTORY_INITIAL_KEY = "java.naming.factory.initial";
    public static final String JAVA_NAMING_FACTORY_INITIAL_VALUE =
            "org.wso2.carbon.tomcat.jndi.CarbonJavaURLContextFactory";

    private DeletionConstants() {
    }
}
