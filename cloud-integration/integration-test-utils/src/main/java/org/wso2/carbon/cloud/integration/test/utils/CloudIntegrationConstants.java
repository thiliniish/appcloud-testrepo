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
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String SUCCESS = "success";
    public static final String ERROR = "error";
    public static final String EMAIL = "email";
    public static final String LOGIN_ERROR_MESSAGE = "Tenant login failed.";
    public static final String PUBLISHER_LOGIN_ERROR_MESSAGE = "Tenant login failed for publisher.";
    public static final String PARAMETER_KEY_ACTION = "action";
    public static final String PARAMETER_KEY_RESPONSE_FROM = "responseFrom";
    public static final String BILLING_STARTER_PLAN = "Starter";
    public static final String MONETIZATION_STARTER_PLAN = "Getting Traction";
    public static final String COMMA_SEPERATOR = ",";

    public static final String CLOUD_LOGIN_URL_SFX =
            "/cloudmgt/site/blocks/user/authenticate/ajax/login.jag";
    public static final String CLOUD_SIGNUP_URL_SFX =
            "/cloudmgt/site/blocks/tenant/register/invite/ajax/invite.jag";
    public static final String CLOUD_ADD_NEW_TENANT_URL_SFX =
            "/cloudmgt/site/blocks/tenant/register/add/ajax/add.jag";
    public static final String CLOUD_ADD_TENANT_URL_SFX =
            "/cloudmgt/site/blocks/tenant/manage/add/ajax/add.jag";
    public static final String CLOUD_SIGNUP_CONFIRM_URL_SFX =
            "/cloudmgt/site/blocks/tenant/register/confirm/ajax/confirm.jag";
    public static final String API_PUBLISHER_LOGIN_URL_SFX =
            "/publisher/site/blocks/user/login/ajax/login.jag";
    public static final String API_STORE_LOGIN_URL_SFX =
            "/store/site/blocks/user/login/ajax/login.jag";
    public static final String UPDATE_TENANT_PROFILE_SFX =
            "/cloudmgt/site/blocks/tenant/manage/profile/ajax/profile.jag";

    //Billing
    public static final String CLOUD_BILLING_PAYMENT_METHOD_INFO_URL_SFX =
            "/cloudmgt/site/blocks/billing/method/info/ajax/get.jag";
    public static final String CLOUD_BILLING_PAYMENT_METHOD_ADD_URL_SFX =
            "/cloudmgt/site/blocks/billing/method/add/ajax/add.jag";
    public static final String CLOUD_BILLING_ACCOUNT_INFO_URL_SFX =
            "/cloudmgt/site/blocks/billing/account/info/ajax/get.jag";
    public static final String CLOUD_BILLING_INVOICE_URL_SFX =
            "/cloudmgt/site/blocks/billing/account/invoice/ajax/get.jag";
    public static final String CLOUD_BILLING_ACCOUNT_DETAILS_ADD_URL_SFX =
            "/cloudmgt/site/blocks/billing/account/add/ajax/add.jag";
    public static final String CLOUD_BILLING_API_USAGE =
            "/cloudmgt/site/blocks/billing/usage/get/ajax/get.jag";
    public static final String CLOUD_BILLING_BILLING_PLAN_GET_URL_SFX =
            "/cloudmgt/site/blocks/billing/plan/get/ajax/get.jag";
    public static final String CLOUD_BILLING_BILLING_PLAN_REMOVE_URL_SFX =
            "/cloudmgt/site/blocks/billing/account/remove/ajax/remove.jag";
    public static final String CLOUD_MONETIZATION_ENABLE_URL_SFX =
            "/cloudmgt/site/blocks/monetizing/publisher/enable/ajax/enable.jag";
    public static final String CLOUD_MONETIZATION_ADD_RATE_PLANS_URL_SFX =
            "/cloudmgt/site/blocks/monetizing/productPlan/add/ajax/add.jag";
    public static final String CLOUD_SELF_SIGNUP_ENABLE_URL_SFX =
            "/cloudmgt/site/blocks/selfSignup/ajax/configure.jag";
    public static final String PARAMETER_KEY_SERVICE_ID = "serviceId";
    public static final String PARAMETER_KEY_TENANT_PASSWORD = "tenantPassword";
    public static final String PARAMETER_KEY_PRODUCT_RATE_PLAN_ID = "productRatePlanId";
    public static final String PARAMETER_KEY_ACCOUNT_ID = "accountId";
    public static final String PARAMETER_KEY_COUPON_ID = "couponData";
    public static final String PARAMETER_KEY_RATE_PLANS = "ratePlans";

    //User Management
    public static final String CHANGE_PASSWORD_URL_SFX =
            "/cloudmgt/site/blocks/user/change/ajax/user.jag";
    public static final String INITIATE_PASSWORD_RESET_SFX =
            "/cloudmgt/site/blocks/password-reset/initiate/ajax/initiate.jag";
    public static final String PASSWORD_RESET_VERIFY_SFX =
            "/cloudmgt/site/blocks/password-reset/reset/ajax/reset.jag";
    public static final String PASSWORD_UPDATE_SFX =
            "/cloudmgt/site/blocks/user/change/ajax/user.jag";
    public static final String USER_PROFILE_URL_SFX =
            "/cloudmgt/site/blocks/user/profile/ajax/profile.jag";
    public static final String CLOUD_CONFIRM_USER_URL_SFX =
            "/cloudmgt/site/blocks/tenant/register/confirm/ajax/confirm.jag";
    public static final String CLOUD_TENANT_USERS_URL_SFX =
            "/cloudmgt/site/blocks/tenant/users/add/ajax/add.jag";
    //cloud roles
    public static final String CLOUD_API_SUBSCRIBER_ROLE = "//cloudProperties/tenantRoles/role[@name='subscriber']/roleKey";
    public static final String CLOUD_API_PUBLISHER_ROLE = "//cloudProperties/tenantRoles/role[@name='publisher']/roleKey";
    public static final String CLOUD_DEVICE_USER_ROLE = "//cloudProperties/tenantRoles/role[@name='deviceMgtUser']/roleKey";
    public static final String CLOUD_DEVICE_ADMIN_ROLE = "//cloudProperties/tenantRoles/role[@name='deviceMgtAdmin']/roleKey";
    public static final String CLOUD_INTEGRATION_USER_ROLE = "//cloudProperties/tenantRoles/role[@name='integrationCloudUser']/roleKey";
    public static final String CLOUD_ADMIN_ROLE = "//cloudProperties/tenantRoles/role[@name='admin']/roleKey";
    public static final String NEW_CLOUD_USER_EMAILS = "//cloudProperties/tenantNewUserEmails";

    //cloud role display names
    public static final String CLOUD_API_SUBSCRIBER_ROLE_DISPLAY_NAME = "//cloudProperties/tenantRoles/role[@name='subscriber']/displayName";
    public static final String CLOUD_API_PUBLISHER_ROLE_DISPLAY_NAME = "//cloudProperties/tenantRoles/role[@name='publisher']/displayName";
    public static final String CLOUD_DEVICE_USER_ROLE_DISPLAY_NAME = "//cloudProperties/tenantRoles/role[@name='deviceMgtUser']/displayName";
    public static final String CLOUD_DEVICE_ADMIN_ROLE_DISPLAY_NAME = "//cloudProperties/tenantRoles/role[@name='deviceMgtAdmin']/displayName";
    public static final String CLOUD_INTEGRATION_USER_ROLE_DISPLAY_NAME = "//cloudProperties/tenantRoles/role[@name='integrationCloudUser']/displayName";
    public static final String CLOUD_ADMIN_ROLE_DISPLAY_NAME = "//cloudProperties/tenantRoles/role[@name='admin']/displayName";

    //Cloud Support
    public static final String CLOUD_CONTACT_SUPPORT_URL_SFX =
            "/cloudmgt/site/blocks/contact/ajax/contact.jag";

    //Super admin credentials
    public static final String SUPER_ADMIN_USER_NAME = "//userManagement/superTenant/tenant/admin/user/userName";
    public static final String SUPER_ADMIN_PASSWORD = "//userManagement/superTenant/tenant/admin/user/password";

    //Cloud Related URLs
    public static final String CLOUD_MGT_SERVER_URL = "//cloudProperties/urls/cloudMgtServerUrl";
    public static final String IDENTITY_SERVER_URL = "//cloudProperties/urls/identityServerUrl";
    public static final String API_MGT_SERVER_URL = "//cloudProperties/urls/apiMgtServerUrl";
    public static final String API_GATEWAY_ENDPOINT_URL =
            "//cloudProperties/urls/apiGatewayEndpointUrl";

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
    public static final String BILLING_PAYMENT_ACCOUNT_ID = "//cloudProperties/billing/payments/accountId";
    public static final String BILLING_PAYMENT_COUPON_ID = "//cloudProperties/billing/payments/couponId";
    public static final String BILLING_PAYMENT_UPGRADE_RATE_PLAN_ID =
            "//cloudProperties/billing/payments/upgradeRatePlanId";
    public static final String BILLING_PAYMENT_SECONDARY_PAYMENT_METHOD_ID =
            "//cloudProperties/billing/payments/secondaryPaymentMethodId";
    public static final String BILLING_PAYMENT_PRIMARY_PAYMENT_METHOD_ID =
            "//cloudProperties/billing/payments/primaryPaymentMethodId";

    //cloud support related parameters
    public static final String CLOUD_SUPPORT_REQUEST_JIRA_CREATION_STATUS =
            "//cloudProperties/support/jiraCreationEnabled";
    public static final String CLOUD_SUPPORT_REQUEST_EMAIL_BODY =
            "//cloudProperties/support/supportRequest/emailBody";
    public static final String CLOUD_SUPPORT_REQUEST_EMAIL_SUBJECT =
            "//cloudProperties/support/supportRequest/emailSubject";
    public static final String CLOUD_SUPPORT_REQUEST_USER_EMAIL =
            "//cloudProperties/support/userEmail";

    //This is the action name which calls the method to send the support request of cloud users.
    public static final String SUPPORT_REQUEST_ACTION_NAME = "sendSupportRequest";

    //This is the action name which calls the method to extend the API Cloud trial.
    public static final String CLOUD_ACCOUNT_EXTENTION_REQUEST_ACTION_NAME = "sendExtensionRequest";

    //Support extension request related properties
    public static final String CLOUD_ACCOUNT_EXTENTION_REQUEST_EMAIL_SUBJECT =
            "//cloudProperties/support/supportRequest/emailSubject";
    public static final String CLOUD_ACCOUNT_EXTENTION_REQUEST_EMAIL_BODY =
            "//cloudProperties/support/supportRequest/emailBody";
    public static final String API_CLOUD_TRIAL_EXTENSION_PERIOD =
            "//cloudProperties/support/accountExtensionRequest/extensionPeriod";
    public static final String API_CLOUD_TRIAL_USER_DEFAULT_STATUS =
            "//cloudProperties/support/accountExtensionRequest/trialUserStatus";
    public static final String API_CLOUD_SUBSCRIPTION_NAME =
            "//cloudProperties/support/accountExtensionRequest/subscriptionType";
    public static final String DELETE_BILLING_STATUS_HISTORY_QUERY =
            "DELETE FROM BILLING_STATUS_HISTORY WHERE TENANT_DOMAIN = (?) && SUBSCRIPTION = (?)";
    public static final String REVERT_EXTENSION_REQUEST_QUERY =
            "UPDATE BILLING_STATUS  SET STATUS = (?), START_DATE = (?), END_DATE = (?) WHERE TENANT_DOMAIN = (?) && SUBSCRIPTION = (?);";
    public static final String DATE_TIME_FORMAT ="yyyy-MM-dd HH:mm:ss";

    //This is the action name which calls the method to checks if the Jira creation is enabled/disabled.
    public static final String JIRA_CREATION_ENABLED_ACTION_NAME = "isJiraCreationEnabled";

    //mysql parameters
    public static final String CLOUD_MGT_DATASOURCE = "mysql-cloudMgt";
    public static final String APIM_STATS_DATASOURCE = "mysql-apiStats";
    public static final String MYSQL_DATA_SOURCE_URL =
            "//datasources/datasource[@name='mysql']/url";
    public static final String MYSQL_USERNAME = "//datasources/datasource[@name='mysql']/username";
    public static final String MYSQL_PASSWORD = "//datasources/datasource[@name='mysql']/password";
    public static final String MYSQL_DRIVER_CLASS_NAME =
            "//datasources/datasource[@name='mysql']/driverClassName";

    public static final String RESPONSE = "Response";
    public static final String COOKIE = "Cookie";
    public static final String MYSQL_REPLACE = "mysql";
    public static final String STRING_TRUE_RESPONSE = "true";
    public static final String STRING_FALSE_RESPONSE = "false";

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
    public static final String GET_API_STATS_USAGE =
            "SELECT COUNT(*) AS size FROM (SELECT SUM(total_request_count) AS totalCount," +
            "apiPublisher,time,day,year,month From API_REQUEST_SUMMARY WHERE apiPublisher=(?) " +
            "AND (time BETWEEN DATE(?) AND DATE(?)) GROUP BY " +
            "apiPublisher,day,year,month ORDER BY time) t1;";

    //API Details
    public static final String API_NAME = "statsTestAPI";
    public static final String APP_NAME = "statsTestApplication";
    public static final String API_CONTEXT = "apiStatTestContext";
    public static final String API_VERSION = "1.0.0";
    public static final String API_ENDPOINT =
            "{\"production_endpoints\":{\"url\":\"http://ws.cdyne.com/phoneverify/phoneverify.asmx\"" +
            ",\"config\":null},\"endpoint_type\":\"http\"}";
    public static final String APP_SCOPE = "PRODUCTION";

    //PubStore Related URLs
    public static final String PUBLISHER_ADD_API_URL_SFX =
            "/publisher/site/blocks/item-add/ajax/add.jag";
    public static final String PUBLISHER_LIFE_CYCLE_URL_SFX =
            "/publisher/site/blocks/life-cycles/ajax/life-cycles.jag";
    public static final String STORE_ADD_APPLICATION_URL_SFX =
            "/store/site/blocks/application/application-add/ajax/application-add.jag";
    public static final String STORE_ADD_SUBSCRIPTION_URL_SFX =
            "/store/site/blocks/subscription/subscription-add/ajax/subscription-add.jag";
    public static final String STORE_LIST_SUBSCRIPTION_URL_SFX =
            "/store/site/blocks/subscription/subscription-list/ajax/subscription-list.jag";
    public static final String API_TOKEN_GENERATION_URL_SFX = "/token";
}
