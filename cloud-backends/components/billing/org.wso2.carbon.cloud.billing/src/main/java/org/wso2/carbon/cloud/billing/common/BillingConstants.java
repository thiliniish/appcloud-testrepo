/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.cloud.billing.common;

public final class BillingConstants {

    public static final String CONFIG_FOLDER = "billing";
    public static final String CONFIG_FILE_NAME = "billing.xml";
    public static final String CONFIG_NAMESPACE = "http://www.wso2.org/cloud/billing";
    public static final String ZUORA_USER_NAME = "zuoraUser";
    public static final String ZUORA_PASSWORD = "zuoraPassword";

    // ApiM Request Summary Conf
    public static final String ENTRY = "Entry";
    public static final String YEAR = "year";
    public static final String MONTH = "month";
    public static final String TIME = "time";
    public static final String DAY = "day";
    public static final String DATE_SEPARATOR = "-";
    public static final String TOTAL_COUNT = "totalCount";
    public static final String API_PUBLISHER = "apiPublisher";
    public static final String ACCOUNTS = "Accounts";
    public static final String ACCOUNT = "Account";

    public static final String ENCODING = "UTF-8";
    public static final String HTTP_REQ_HEADER_AUTHZ = "Authorization";

    public static final String ACCOUNT_ID = "ACCOUNT_ID";
    public static final String UOM = "UOM";
    public static final String QTY = "QTY";
    public static final String STARTDATE = "STARTDATE";
    public static final String ENDDATE = "ENDDATE";
    public static final String SUBSCRIPTION_ID = "SUBSCRIPTION_ID";
    public static final String CHARGE_ID = "CHARGE_ID";
    public static final String DESCRIPTION = "DESCRIPTION";

    public static final String USAGE_UPLOADER_TASK_NAME = "usageUploader";
    public static final String USAGE_UPLOADER_TASK_CLASS_NAME = "org.wso2.carbon.cloud.billing.usage.scheduler" +
                                                                ".UsageUploaderTask";
    public static final String USAGE_UPLOADER_TASK_TENANT_ID_KEY = "__TENANT_ID_PROP__";

    public static final String BILLING_DB_UPDATE_TASK_NAME = "updateBillingDbTask";
    public static final String BILLING_DB_UPDATE_TASK_CLASS_NAME = "org.wso2.carbon.cloud.billing.subscription.tasks" +
                                                              ".BillingDbUpdateTask";
    public static final String REMOVE_ROLES_PROPERTY_KEY = "__ROLES_";
    public static final String PENDING_DISABLES_URL_KEY = "__PENDING_DISABLES__";
    public static final String DISABLE_TENANT_URL_KEY = "__DISABLE_TENANT__";
    public static final String UPDATE_SUBSCRIPTION_STATUS_URL_KEY = "__UPDATE_SUBSCRIPTION_STATUS__";
    public static final String BILLING_HISTORY_URL_KEY = "__BILLING_HISTORY__";

    public static final String SECURE_VAULT_NS = "http://org.wso2.securevault/configuration";
    public static final String SECRET_ALIAS_ATTR_NAME = "secretAlias";

    public static final String ENABLE_USAGE_UPLOAD = "Usage.EnableUsageUploading";
    public static final String USAGE_CRON_EXPRESSION = "Usage.Cron";
    public static final String USAGE_FILE = "Usage.UsageUploadFileLocation";
    public static final String GET_TENANT_USAGE = "DataServiceAPI.dataservice.Property.usage";
    public static final String GET_PAYMENTPLAN_AMENDMENT = "DataServiceAPI.dataservice.Property.amendments";

    public static final String DS_USERNAME = "DataServiceAPI.dataservice.Property.user";
    public static final String DS_PASSWORD = "DataServiceAPI.dataservice.Property.password";

    public static final String GET_ALL_REQUESTS = "DataServiceAPI.dataservice.Property.requestCount";
    public static final String GET_ACCOUNTID = "DataServiceAPI.dataservice.Property.tenantAccount";
    // Zuora elements
    public static final String GET_ACCOUNT_SUMMARY = "ZouraAPI.zuora.Property.accountSummary";
    public static final String GET_RATE_PLAN_SUMMARY = "ZouraAPI.zuora.Property.ratePlans";
    public static final String POST_USAGE = "ZouraAPI.zuora.Property.usage";
    public static final String ZUORA_USER = "ZouraAPI.zuora.Property.user";
    public static final String ZUORA_PASWORD = "ZouraAPI.zuora.Property.password";

    public static final String TRUSTSTORE_PASSWORD = "SSORelyingParty.TruststorePassword";
    public static final String TRUSTSTORE_LOCATION = "SSORelyingParty.keyStorePath";

    public static final String ACCOUNT_KEY_PARAM = "{account-key}";
    public static final String TENANT_DOMAIN_PARAM = "{tenantDomain}";

    //Zuora coupon details
    public static final String COUPON_HEADER = "coupon";

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_MAX_CONNECTION_PER_HOST = 2;
    public static final int DEFAULT_MAX_TOTAL_CONNECTION = 10;

    public static final String HTTP_RESPONSE_TYPE_ACCEPT = "Accept";
    public static final String HTTP_RESPONSE_TYPE_JSON = "application/json";
    public static final String HTTP_FOLLOW_REDIRECT = "follow_redirect";
    // Account summary json elements
    public static final String PRODUCTS = "products";
    public static final String NAME = "name";
    public static final String PRODUCTRATEPLANS = "productRatePlans";
    public static final String SUBSCRIPTIONS = "subscriptions";
    public static final String RATEPLANS = "ratePlans";
    public static final String PRODUCT_RATE_PLAN_ID = "productRatePlanId";
    public static final String PRODUCT_NAME = "productName";
    public static final String LAST_CHANGE_TYPE = "lastChangeType";
    public static final String AMENDEMENT_ADD_TYPE = "Add";
    public static final String SUBSCRIPTION_NUMBER = "subscriptionNumber";
    public static final String RATE_PLAN_NAME = "ratePlanName";

    public static final String SUBSCRIPTIONS_ELE = "Subscriptions";

    public static final String DATE_FOMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DS_DATE_FOMAT = "yyyy-MM-dd";
    // Commercial ProductNames
    public static final String API_CLOUD = "API Cloud";

    public static final String START_DATE = "START_DATE";
    public static final String END_DATE = "END_DATE";
    public static final String PENDING_DISABLE_TENANT_DOMAIN = "TenantDomain";
    public static final String PENDING_DISABLE_START_DATE = "StartDate";
    public static final String PENDING_DISABLE_END_DATE = "EndDate";
    public static final String PENDING_DISABLE_SUBSCRIPTION = "Subscription";

    //Should generalize
    public static final String TENANT_DOMAIN_QUERY_PARAM = "TENANT_DOMAIN";
    public static final String TENANT_ID_QUERY_PARAM = "TENANT_ID";
    public static final String END_DATE_QUERY_PARAM = "END_DATE";
    public static final String SUBSCRIPTION_QUERY_PARAM = "SUBSCRIPTION";
    public static final String ACCOUNT_NUMBER_QUERY_PARAM = "ACCOUNT_NUMBER";
    public static final String TYPE_QUERY_PARAM = "TYPE";


    public static final String STATUS_QUERY_PARAM = "STATUS";
    public static final String CLOUD_TYPE_QUERY_PARAM = "CLOUD_TYPE";


    public static final String UNIT_OF_MEASURE = "10K Overage Daily API Calls";

    public static final int OVER_USAGE_THRESHOLD = 0;

    public static final String TRUSTSTORE_NAME_PROPERTY = "javax.net.ssl.trustStore";
    public static final String TRUSTSTORE_PASSWORD_PROPERTY = "javax.net.ssl.trustStorePassword";

    public static final String API_ACCESS_KEY_ID = "apiAccessKeyId";
    public static final String API_SECRET_ACCESS_KEY = "apiSecretAccessKey";

    public static final String FILE_PART_NAME = "file";

    public static final String API_CLOUD_SUBSCRIPTION_ID = "api_cloud";

    public static final class SecureValueProperties {
        public static final String SECRET_ALIAS_ATTRIBUTE_NAME_WITH_NAMESPACE = "secretAlias";
        public static final String SECURE_VAULT_NS = "http://org.wso2.securevault/configuration";

        private SecureValueProperties() {
            throw new AssertionError();
        }
    }

}
