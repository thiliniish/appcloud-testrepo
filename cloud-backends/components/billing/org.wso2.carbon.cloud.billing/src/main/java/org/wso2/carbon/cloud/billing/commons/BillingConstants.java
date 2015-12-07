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

package org.wso2.carbon.cloud.billing.commons;

public final class BillingConstants {

    public static final String CLOUD_CONFIG_FOLDER = "cloud";
    public static final String CONFIG_FILE_NAME = "billing.xml";
    public static final String CONFIG_NAMESPACE = "http://www.wso2.org/cloud/billing";
    public static final String EMPTY_STRING = "";

    // ApiM Request Summary Conf
    public static final String ENTRY = "Entry";
    public static final String YEAR = "year";
    public static final String MONTH = "month";
    public static final String TIME = "time";
    public static final String DAY = "day";
    public static final String TOTAL_COUNT = "totalCount";
    public static final String API_PUBLISHER = "apiPublisher";
    public static final String ACCOUNTS = "Accounts";
    public static final String STATUS = "Status";

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
    public static final String PENDING_DISABLES_URL_KEY = "__PENDING_DISABLES__";
    public static final String DISABLE_TENANT_URL_KEY = "__DISABLE_TENANT__";
    public static final String UPDATE_SUBSCRIPTION_STATUS_URL_KEY = "__UPDATE_SUBSCRIPTION_STATUS__";
    public static final String BILLING_HISTORY_URL_KEY = "__BILLING_HISTORY__";

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
    public static final String HTTP_CONTENT_TYPE = "Content-Type";
    public static final String HTTP_QUERY_STRING_CONTENT = "application/x-www-form-urlencoded";
    // Account summary json elements
    public static final String PRODUCTS = "products";
    public static final String NAME = "name";
    public static final String PRODUCT_RATE_PLANS = "productRatePlans";
    public static final String SUBSCRIPTIONS = "subscriptions";
    public static final String RATE_PLANS = "ratePlans";
    public static final String PRODUCT_RATE_PLAN_ID = "productRatePlanId";
    public static final String PRODUCT_NAME = "productName";
    public static final String LAST_CHANGE_TYPE = "lastChangeType";
    public static final String AMENDEMENT_ADD_TYPE = "Add";
    public static final String SUBSCRIPTION_NUMBER = "subscriptionNumber";
    public static final String PRODUCT_ID = "productId";
    public static final String RATE_PLAN_NAME = "ratePlanName";

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DS_DATE_FORMAT = "yyyy-MM-dd";
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

    public static final String TRUST_STORE_NAME_PROPERTY = "javax.net.ssl.trustStore";
    public static final String TRUST_STORE_PASSWORD_PROPERTY = "javax.net.ssl.trustStorePassword";

    public static final String API_ACCESS_KEY_ID = "apiAccessKeyId";
    public static final String API_SECRET_ACCESS_KEY = "apiSecretAccessKey";

    public static final String FILE_PART_NAME = "file";

    public static final String API_CLOUD_SUBSCRIPTION_ID = "api_cloud";

    /* Zuora queries */
    private static final String ZUORA_ACCOUNT_QUERY_PREFIX = "SELECT id, name, accountnumber, billtoid, " +
            "communicationprofileid, createddate, invoicetemplateid, parentid, status, defaultpaymentmethodid FROM account ";
    public static final String QUERY_ZUORA_ACCOUNT_BY_NAME = ZUORA_ACCOUNT_QUERY_PREFIX + "WHERE name = '?'";
    public static final String QUERY_ZUORA_ACCOUNT_BY_ACCOUNT_NO = ZUORA_ACCOUNT_QUERY_PREFIX + "WHERE accountnumber " +
            "= '?'";

    /*Data service API v1 URIs*/
    public static final String DS_NAMESPACE_URI = "http://ws.wso2.org/dataservice";

    public static final String DS_API_URI_REQUEST_COUNT = "/requestcount";
    public static final String DS_API_URI_USAGE = "/usage";
    public static final String DS_API_URI_AMENDMENTS = "/amendments";
    public static final String DS_API_URI_TENANT_ACCOUNT = "/tenantaccount?TENANT_DOMAIN={tenantDomain}";
    public static final String DS_API_URI_PENDING_DISABLE_TENANTS = "/pendingdisables";
    public static final String DS_API_URI_DISABLE_TENANT = "/disabletenant";
    public static final String DS_API_URI_SUBSCRIPTION_STATUS = "/subscriptionstatus";
    public static final String DS_API_URI_BILLING_HISTORY = "/billinghistory";
    public static final String DS_API_URI_MAPPING_FOR_SUBSCRIPTION =  "/mappingforsubscription";

    /*Zuora REST API v1 URIs*/
    public static final String ZUORA_REST_API_URI_USAGE = "/v1/usage";
    public static final String ZUORA_REST_API_URI_ACCOUNT_SUMMARY = "/v1/accounts/{account-key}/summary";
    public static final String ZUORA_REST_API_URI_RATE_PLANS = "/v1/subscriptions/accounts/{account-key}";
    public static final String ZUORA_REST_API_URI_ACCOUNTS = "/v1/accounts";
    public static final String ZUORA_REST_API_URI_CANCEL_SUBSCRIPTION = "/v1/subscriptions/{subscription-key}/cancel";
    public static final String ZUORA_REST_API_URI_SUBSCRIPTIONS = "/v1/subscriptions";
    public static final String ZUORA_REST_API_URI_PAYMENT_METHODS = "/v1/payment-methods/credit-cards";
    public static final String ZUORA_REST_API_URI_REMOVE_PAYMENT_METHOD = "/v1/payment-methods";
    public static final String ZUORA_REST_API_URI_INVOICE_INFO = "/v1/transactions/invoices/accounts/{account-key}";
    public static final String ZUORA_REST_API_URI_PAYMENT_INFO = "/v1/transactions/payments/accounts/{account-key}";
    public static final String ZUORA_REST_API_URI_PRODUCTS = "/v1/catalog/products";

    /*data service param names*/
    public static final String PARAM_ACCOUNT_NUMBER = "accountNumber";
    public static final String PARAM_ZUORA_PRODUCT_NAME = "zuoraProductName";
    public static final String PARAM_RATE_PLAN_NAME = "ratePlanName";
    public static final String PARAM_RATE_PLAN_ID = "ratePlanId";
    public static final String PARAM_SUBSCRIPTION_ID = "subscriptionId";
    public static final String PARAM_START_DATE = "startDate";
    public static final String PARAM_TENANT = "tenant";

    /*Zuora types*/
    public static final String ZUORA_ACCOUNT = "Account";
    public static final String ZUORA_SUBSCRIPTION_STATUS = "status";
    public static final String SUBSCRIPTION_STATUS_ACTIVE = "Active";


    //Zuora communication & invoice template child account suffix
    public static final String ZUORA_TEMPLATE_ACCOUNT_SUFFIX = "__TEMPLATE__";
    public static final String ZUORA_DEFAULT_TEMPLATE_ACCOUNT_SUFFIX = "DEFAULT__TEMPLATE__";

    /*Zuora soap elements*/
    public static final String ZUORA_ACCOUNT_NUMBER = "accountNumber";
    public static final String ZUORA_INVOICE_TEMPLATE_ID = "invoiceTemplateId";
    public static final String ZUORA_COMMUNICATION_PROFILE_ID = "communicationProfileId";
    public static final String ZUORA_RESPONSE_SUCCESS = "success";


    private BillingConstants() {
    }

    public static final class SecureValueProperties {
        public static final String SECRET_ALIAS_ATTRIBUTE_NAME_WITH_NAMESPACE = "secretAlias";
        public static final String SECURE_VAULT_NS = "http://org.wso2.securevault/configuration";

        private SecureValueProperties() {
            throw new AssertionError();
        }
    }

}
