/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.cloud.integration.test.utils;

public class CloudIntegrationConstants {
    public static final String CLOUD_PRODUCT_GROUP = "cloud";

    public static final String CLOUD_LOGIN_URL_SFX =
            "/cloudmgt/site/blocks/user/authenticate/ajax/login.jag";
    public static final String CLOUD_SIGNUP_URL_SFX =
            "/cloudmgt/site/blocks/tenant/register/invite/ajax/invite.jag";
    public static final String CLOUD_ADD_TENANT_URL_SFX =
            "/cloudmgt/site/blocks/tenant/register/add/ajax/add.jag";
    public static final String CLOUD_BILLING_PAYMENT_METHOD_INFO_URL_SFX =
            "/cloudmgt/site/blocks/billing/method/info/ajax/get.jag";
    public static final String CLOUD_BILLING_PAYMENT_METHOD_ADD_URL_SFX =
            "/cloudmgt/site/blocks/billing/method/add/ajax/add.jag";
    public static final String CLOUD_SIGNUP_CONFIRM_URL_SFX =
            "/cloudmgt/site/blocks/tenant/register/confirm/ajax/confirm.jag";

    //User Management
    public static final String CHANGE_PASSWORD_URL_SFX =
            "/cloudmgt/site/blocks/user/change/ajax/user.jag";
    public static final String INITIATE_PASSWORD_RESET_SFX =
            "/cloudmgt/site/blocks/user/initiate/ajax/initiate.jag";
    public static final String PASSWORD_RESET_VERIFY_SFX =
            "/cloudmgt/site/blocks/user/reset/ajax/reset.jag";
    public static final String PASSWORD_UPDATE_SFX =
            "/cloudmgt/site/blocks/user/change/ajax/user.jag";
    public static final String USER_PROFILE_URL_SFX =
            "/cloudmgt/site/blocks/user/profile/ajax/profile.jag";

    public static final String CLOUD_SEND_USER_INVITE_URL_SFX =
            "/cloudmgt/site/blocks/tenant/users/add/ajax/add.jag";
    public static final String CLOUD_CONFIRM_USER_URL_SFX =
            "/cloudmgt/site/blocks/tenant/register/confirm/ajax/confirm.jag";
    public static final String CLOUD_TENANT_USERS_URL_SFX =
            "/cloudmgt/site/blocks/tenant/users/add/ajax/add.jag";
    public static final String COMMON_USER_PASSWORD = "Admin@123#";
    public static final String COMMON_USER_FIRST_NAME = "First";
    public static final String COMMON_USER_LAST_NAME = "Last";
    public static final String ALL_CLOUD_USER_ROLES = "//cloudProperties/allTenantRoles";
    public static final String NEW_CLOUD_USER_EMAILS = "//cloudProperties/tenantNewUserEmails";

    //Automation xml parameters
    //Super admin credentials
    public static final String SUPER_ADMIN_USER_NAME = "//userManagement/superTenant/tenant/admin/user/userName";
    public static final String SUPER_ADMIN_PASSWORD = "//userManagement/superTenant/tenant/admin/user/password";

    //Cloud Related URLs
    public static final String CLOUD_MGT_SERVER_URL = "//cloudProperties/urls/cloudMgtServerUrl";
    public static final String IDENTITY_SERVER_URL = "//cloudProperties/urls/identityServerUrl";

    public static final String TENANT_ADMIN_USER_NAME =
            "//cloudProperties/tenantDetails/Tenant[@key='defaultTenant']/adminUser";
    public static final String TENANT_ADMIN_PASSWORD =
            "//cloudProperties/tenantDetails/Tenant[@key='defaultTenant']/adminPassword";
    public static final String TENANT_ADMIN_DOMAIN =
            "//cloudProperties/tenantDetails/Tenant[@key='defaultTenant']/tenantDomain";
    public static final String NEW_TENANT_DOMAIN =
            "//cloudProperties/tenantDetails/Tenant[@key='newTenant']/tenantDomain";
    public static final String NEW_TENANT_ADMINUSER =
            "//cloudProperties/tenantDetails/Tenant[@key='newTenant']/adminUser";
    public static final String NEW_TENANT_ADMINPASSWORD =
            "//cloudProperties/tenantDetails/Tenant[@key='newTenant']/adminPassword";
    public static final String NEW_TENANT_FIRSTNAME =
            "//cloudProperties/tenantDetails/Tenant[@key='newTenant']/firstName";
    public static final String NEW_TENANT_LASTNAME =
            "//cloudProperties/tenantDetails/Tenant[@key='newTenant']/lastName";
    public static final String NEW_TENANT_USAGE_PLAN =
            "//cloudProperties/tenantDetails/Tenant[@key='newTenant']/usagePlan";
    public static final String DEPLOYMENT_CONTEXT = "//cloudProperties/deploymentContext";
    public static final String BILLING_PAYMENT_SERVICE_ID =
            "//cloudProperties/billing/payments/serviceId";
    public static final String BILLING_PAYMENT_PRODUCT_RATE_PLAN_ID =
            "//cloudProperties/billing/payments/productRatePlanId";
    public static final String SUPER_ADMIN_USER_NAME =
            "//cloudProperties/tenantDetails/Tenant[@key='superTenant']/adminUser";
    public static final String SUPER_ADMIN_PASSWORD =
            "//cloudProperties/tenantDetails/Tenant[@key='superTenant']/adminPassword";

    //mysql parameters
    public static final String MYSQL_DATA_SOURCE_URL =
            "//datasources/datasource[@name='mysql']/url";
    public static final String MYSQL_USERNAME = "//datasources/datasource[@name='mysql']/username";
    public static final String MYSQL_PASSWORD = "//datasources/datasource[@name='mysql']/password";
    public static final String MYSQL_DRIVER_CLASS_NAME =
            "//datasources/datasource[@name='mysql']/driverClassName";


    public static final String RESPONSE = "Response";
    public static final String COOKIE = "Cookie";

    //default tenant users
    public static final String TENANT_USER_USERNAME =
            "//cloudProperties/tenantDetails/Tenant[@key='defaultTenant']/users/user[@key='user1']/userName";
    public static final String TENANT_USER_PASSWORD =
            "//cloudProperties/tenantDetails/Tenant[@key='defaultTenant']/users/user[@key='user1']/password";
    public static final String TENANT_USER_EMAIL =
            "//cloudProperties/tenantDetails/Tenant[@key='defaultTenant']/users/user[@key='user1']/email";

    public static final String TENANT_USER_FIRST_NAME =
            "//cloudProperties/tenantDetails/Tenant[@key='defaultTenant']/users/user[@key='user1']/@firstName";
    public static final String TENANT_USER_LAST_NAME =
            "//cloudProperties/tenantDetails/Tenant[@key='defaultTenant']/users/user[@key='user1']/@lastName";

    //SQL Queries
    public static final String GET_TEMP_UUID_FOR_REGISTRATION =
            "SELECT uuid FROM TEMP_REGISTRATION where email=(?)";
    public static final String GER_UUID_FOR_TEMP_INVITEE =
            "SELECT uuid FROM TEMP_INVITEE WHERE tenantDomain=(?) AND email=(?)";

}
