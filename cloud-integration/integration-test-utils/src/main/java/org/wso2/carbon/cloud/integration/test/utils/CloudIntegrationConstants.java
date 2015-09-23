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
    public static final String CLOUD_BILLING_PAYMENT_METHOD_INFO_URL_SFX =
            "/cloudmgt/site/blocks/billing/method/info/ajax/get.jag";
    public static final String CLOUD_BILLING_PAYMENT_METHOD_ADD_URL_SFX =
            "/cloudmgt/site/blocks/billing/method/add/ajax/add.jag";

    //User Management
    public static final String CHANGE_PASSWORD_URL_SFX = "/cloudmgt/site/blocks/user/change/ajax/user.jag";
    public static final String USER_PROFILE_URL_SFX = "/cloudmgt/site/blocks/user/profile/ajax/profile.jag";
    public static final String INITIATE_PASSWORD_RESET_URL_SFX = "/cloudmgt/site/blocks/user/initiate/ajax/initiate.jag";

    //Automation xml parameters
    public static final String CLOUD_MGT_SERVER_URL = "//cloudProperties/urls/cloudMgtServerUrl";
    public static final String TENANT_ADMIN_USER_NAME =
            "//cloudProperties/tenantDetails/defaultTenant/adminUser";
    public static final String TENANT_ADMIN_PASSWORD =
            "//cloudProperties/tenantDetails/defaultTenant/adminPassword";
    public static final String NEW_TENANT_EMAIL =
            "//cloudProperties/tenantDetails/newTenant/tenantEmail";
    public static final String DEPLOYMENT_CONTEXT = "//cloudProperties/deploymentContext";
    public static final String BILLING_PAYMENT_SERVICE_ID =
            "//cloudProperties/billing/payments/serviceId";
    public static final String BILLING_PAYMENT_PRODUCT_RATE_PLAN_ID =
            "//cloudProperties/billing/payments/productRatePlanId";

    public static final String MYSQL_DATA_SOURCE_URL =
            "//datasources/datasource[@name='mysql']/url";
    public static final String MYSQL_USERNAME = "//datasources/datasource[@name='mysql']/username";
    public static final String MYSQL_PASSWORD = "//datasources/datasource[@name='mysql']/password";
    public static final String MYSQL_DRIVER_CLASS_NAME =
            "//datasources/datasource[@name='mysql']/driverClassName";

    public static final String CLOUD_MGT_APP = "cloudmgt";
    public static final String RESPONSE = "Response";
    public static final String COOKIE = "Cookie";

    //default tenant users
    public static final String TENANT_USER_USERNAME =
            "//cloudProperties/tenantDetails/defaultTenant/users/user[@key='user1']/userName";
    public static final String TENANT_USER_PASSWORD =
            "//cloudProperties/tenantDetails/defaultTenant/users/user[@key='user1']/password";
    public static final String TENANT_USER_EMAIL =
            "//cloudProperties/tenantDetails/defaultTenant/users/user[@key='user1']/email";

    public static final String TENANT_USER_FIRST_NAME =
            "//cloudProperties/tenantDetails/defaultTenant/users/user[@key='user1']/@firstName";
    public static final String TENANT_USER_LAST_NAME =
            "//cloudProperties/tenantDetails/defaultTenant/users/user[@key='user1']/@lastName";

}
