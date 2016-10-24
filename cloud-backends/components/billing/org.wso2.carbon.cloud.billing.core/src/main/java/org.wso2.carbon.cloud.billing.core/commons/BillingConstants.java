/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.cloud.billing.core.commons;

/**
 * Billing constant file
 */
public final class BillingConstants {

    public static final String HTTPS_SCHEME = "https";

    public static final String CLOUD_CONFIG_FOLDER = "cloud";
    public static final String CONFIG_FILE_NAME = "billing-core.xml";
    public static final String BILLING_VENDOR_CONFIG_FILE_NAME = "billing-vendor.xml";
    public static final String CONFIG_NAMESPACE = "http://www.wso2.org/cloud/billing";
    public static final String EMPTY_STRING = "";
    public static final String COLON = ":";

    // Registry Related Conf
    public static final String GOVERNANCE_REGISTRY = "/_system/governance";
    public static final String CONFIG_REGISTRY = "/_system/config";

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
    public static final String HTTP_REQ_HEADER_X_WSO2_TENANT = "X-WSO2-Tenant";

    public static final String ACCOUNT_ID = "ACCOUNT_ID";
    public static final String UOM = "UOM";
    public static final String QTY = "QTY";
    public static final String STARTDATE = "STARTDATE";
    public static final String ENDDATE = "ENDDATE";
    public static final String SUBSCRIPTION_ID = "SUBSCRIPTION_NUMBER";
    public static final String CHARGE_ID = "CHARGE_ID";
    public static final String DESCRIPTION = "DESCRIPTION";

    public static final String USAGE_UPLOADER_TASK_NAME = "usageUploader";
    public static final String USAGE_UPLOADER_TASK_CLASS_NAME =
            "org.wso2.carbon.cloud.billing.usage.scheduler" + ".UsageUploaderTask";
    public static final String USAGE_UPLOADER_TASK_TENANT_ID_KEY = "__TENANT_ID_PROP__";

    public static final String BILLING_DB_UPDATE_TASK_NAME = "updateBillingDbTask";
    public static final String BILLING_DB_UPDATE_TASK_CLASS_NAME =
            "org.wso2.carbon.cloud.billing.subscription.tasks" + ".BillingDbUpdateTask";
    public static final String PENDING_DISABLES_URL_KEY = "__PENDING_DISABLES__";
    public static final String DISABLE_TENANT_URL_KEY = "__DISABLE_TENANT__";
    public static final String UPDATE_SUBSCRIPTION_STATUS_URL_KEY = "__UPDATE_SUBSCRIPTION_STATUS__";
    public static final String BILLING_HISTORY_URL_KEY = "__BILLING_HISTORY__";

    public static final String ACCOUNT_KEY_PARAM = "{account-key}";
    public static final String TENANT_DOMAIN_PARAM = "{tenantDomain}";
    public static final String SUBSCRIPTION_KEY_PARAM = "{subscription-key}";

    //Zuora coupon details
    public static final String COUPON_HEADER = "coupon";

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 9443;
    public static final int DEFAULT_MAX_CONNECTION_PER_HOST = 2;
    public static final int DEFAULT_MAX_TOTAL_CONNECTION = 10;

    public static final String HTTP_RESPONSE_TYPE_ACCEPT = "Accept";
    public static final String HTTP_FOLLOW_REDIRECT = "follow_redirect";
    public static final String HTTP_CONTENT_TYPE = "Content-Type";

    //Email Event Adapter configuration elements.
    public static final String MESSAGE_BODY = "email.body";
    public static final String MESSAGE_SUBJECT = "email.subject";
    public static final String MESSAGE_RECEIVER = "email.address";
    public static final String MESSAGE_TYPE = "email.type";
    public static final String EMAIL_MESSAGE_FORMAT = "email";
    public static final String RENDERING_TYPE_EMAIL = "email";
    public static final String EMAIL_ADAPTER_NAME = "cloudEmailAdapter";
    public static final int DEFAULT_TIMEOUT_VALUE = 60;

    /*Media types*/
    public static final String HTTP_TYPE_APPLICATION_URL_ENCODED = "application/x-www-form-urlencoded";
    public static final String HTTP_TYPE_APPLICATION_XML = "application/xml";
    public static final String HTTP_TYPE_APPLICATION_JSON = "application/json";
    public static final String HTML_CONTENT_TYPE = "text/html";
    public static final String TEXT_PLAIN_CONTENT_TYPE = "text/plain";

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
    public static final String CONTRACT_EFFECTIVE_DATE = "contractEffectiveDate";
    public static final String ACCOUNT_KEY = "accountKey";
    public static final String SUBSCRIBED_TO_RATE_PLANS = "subscribeToRatePlans";

    public static final String CANCELLATION_POLICY = "cancellationPolicy";
    public static final String CANCELLATION_EFFECTIVE_DATE = "cancellationEffectiveDate";
    public static final String INVOICE_COLLECT = "invoiceCollect";

    // Zuora subscription json elements
    public static final String CANCELLATION_POLICY_SPECIFIC_DATE = "SpecificDate";
    public static final String PRODUCT_RATE_PLANS_SUCCESS_STATUS = "success";
    public static final String PRODUCT_RATE_PLANS_NEXTPAGE = "nextPage";
    public static final String HTTPS = "https://";

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
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

    public static final String UNIT_OF_MEASURE = "100K Overage Daily API Calls";

    public static final int OVER_USAGE_THRESHOLD = 0;

    public static final String TRUST_STORE_NAME_PROPERTY = "javax.net.ssl.trustStore";
    public static final String TRUST_STORE_PASSWORD_PROPERTY = "javax.net.ssl.trustStorePassword";

    public static final String API_ACCESS_KEY_ID = "apiAccessKeyId";
    public static final String API_SECRET_ACCESS_KEY = "apiSecretAccessKey";

    public static final String FILE_PART_NAME = "file";

    public static final String API_CLOUD_ID = "api_cloud";

    /* zuora product related attributes */
    public static final String PRODUCT_CATEGORY = "Base Products";
    public static final String EFFECTIVE_END_DATE = "12/12/2100";
    public static final String EFFECTIVE_DATE_FORMAT = "MM/DD/YYYY";
    public static final int PRODUCT_COMMISSION_DEFAULT_VALUE = 15;

    /* json object attribute labels */
    public static final String JSON_OBJ_PRODUCT_NAME = "localName";
    public static final String JSON_OBJ_PRODUCT_CATEGORY = "localCategory";
    public static final String JSON_OBJ_PRODUCT_RATEPLAN_NAME = "localProductRatePlanName";
    public static final String JSON_OBJ_PRODUCT_RATEPLAN_RECURRING_CHARGE = "localProductRatePlanPrice";
    public static final String JSON_OBJ_PRODUCT_RATEPLAN_DESCRIPTION = "localDescription";
    public static final String JSON_OBJ_PRODUCT_RATEPLAN_THROTTLING_LIMIT = "localThrottlingLimit";
    public static final String JSON_OBJ_PRODUCT_RATEPLAN_MONTHLY_LIMIT = "localMonthlyLimit";
    public static final String JSON_OBJ_PRODUCT_RATEPLAN_OVERAGE_CHARGE = "localOverageCharge";

    /* zuora product rate plan charge related attributes */
    public static final int RATEPLAN_CHARGE_BILLCYCLEDAY = 1;
    public static final String RATEPLAN_CHARGE_BILLING_PERIOD = "Month";
    public static final String RATEPLAN_CHARGE_ALIGNMENT = "AlignToCharge";
    public static final String RATEPLAN_CHARGE_MODEL = "FlatFee";
    public static final String RATEPLAN_CHARGE_TYPE = "Recurring";
    public static final String RATEPLAN_CHARGE_NAME_MONTHLY_SUBSCRIPTION = "Monthly subscription fee";
    public static final String RATEPLAN_CHARGE_TRIGGER_EVENT = "ContractEffective";
    public static final String RATEPLAN_CHARGETIER_CURRENCY = "USD";
    public static final String RATEPLAN_CHARGETIER_PRICE_FORMAT = "Flat Fee";
    public static final int RATEPLAN_CHARGETIER_STARTING_UNIT = 1;
    public static final String RATEPLAN_CHARGE_TIER_PRICE_FORMAT = "Per Unit Pricing";
    public static final String RATEPLAN_CHARGE_TYPE_OVERUSAGE = "Usage";
    public static final String RATEPLAN_CHARGE_NAME_OVERUSAGE = "Over usage fee";
    /*DS Service utility constants*/
    public static final String DS_NAMESPACE_URI = "http://ws.wso2.org/dataservice";
    public static final String DS_REQUEST_STATUS = "REQUEST_STATUS";
    public static final String DS_REQUEST_STATUS_SUCCESS = "SUCCESSFUL";
    public static final String QUERY_ZUORA_PRODUCT_BY_NAME = "SELECT id, Name, SKU FROM product WHERE Name = '?'";
    public static final String QUERY_ZUORA_PRODUCTRATEPLAN_BY_NAME =
            "SELECT id, Name FROM ProductRatePlan WHERE Name = '?' and ProductId = '?'";
    public static final String QUERY_ZUORA_PRODUCTRATEPLAN_CHARGE_BY_NAME =
            "SELECT id, Name FROM ProductRatePlanCharge WHERE Name = '?' and ProductRatePlanId = '?'";
    public static final String QUERY_ZUORA_PRODUCTRATEPLAN_CHARGE_TIER =
            "select id from ProductRatePlanChargeTier where ProductRatePlanChargeId = '?'";
    /*Data service API v1 URIs*/
    public static final String DS_API_URI_REQUEST_COUNT = "/requestcount";
    public static final String DS_API_URI_USAGE = "/usage";
    public static final String DS_API_URI_AMENDMENTS = "/amendments";
    public static final String DS_API_URI_TENANT_ACCOUNT = "/tenantaccount?TENANT_DOMAIN={tenantDomain}";
    public static final String DS_API_URI_PENDING_DISABLE_TENANTS = "/pendingdisables";
    public static final String DS_API_URI_DISABLE_TENANT = "/disabletenant";
    public static final String DS_API_URI_SUBSCRIPTION_STATUS = "/subscriptionstatus";
    public static final String DS_API_URI_BILLING_HISTORY = "/billinghistory";
    public static final String DS_API_URI_MAPPING_FOR_SUBSCRIPTION = "/mappingforsubscription";
    /*Zuora REST API v1 URIs*/
    public static final String ZUORA_REST_API_URI_USAGE = "/rest/v1/usage";
    public static final String ZUORA_REST_API_URI_ACCOUNT_SUMMARY = "/rest/v1/accounts/{account-key}/summary";
    public static final String ZUORA_REST_API_URI_RATE_PLANS = "/rest/v1/subscriptions/accounts/{account-key}";
    public static final String ZUORA_REST_API_URI_ACCOUNTS = "/rest/v1/accounts";
    public static final String ZUORA_REST_API_URI_CANCEL_SUBSCRIPTION =
            "/rest/v1/subscriptions/{subscription-key}/cancel";
    public static final String ZUORA_REST_API_URI_SUBSCRIPTIONS = "/rest/v1/subscriptions";
    public static final String ZUORA_REST_API_URI_PAYMENT_METHODS = "/rest/v1/payment-methods/credit-cards";
    public static final String ZUORA_REST_API_URI_REMOVE_PAYMENT_METHOD = "/rest/v1/payment-methods";
    public static final String ZUORA_REST_API_URI_INVOICE_INFO =
            "/rest/v1/transactions/invoices/accounts/{account-key}";
    public static final String ZUORA_REST_API_URI_PAYMENT_INFO =
            "/rest/v1/transactions/payments/accounts/{account-key}";
    public static final String ZUORA_REST_API_URI_PRODUCTS = "/rest/v1/catalog/products";
    public static final String ZUORA_REST_API_URI_ACCOUNT_PAYMENTS_CREDIT_CARDS =
            "/rest/v1/payment-methods/credit-cards/accounts";

    /*API manager REST api suffixes*/
    public static final String APIM_ADMIN_REST_URI_TENANT_THROTLING_TIERS =
            "/admin/v0.10/throttling/policies/subscription";
    public static final String APIM_ADMIN_REST_URI_TENANT_THROTTLING_TIERS =
            "/store/v0.10/tiers/api";

    /*data service param names*/
    public static final String PARAM_ACCOUNT_NUMBER = "accountNumber";
    public static final String PARAM_ZUORA_PRODUCT_NAME = "zuoraProductName";
    public static final String PARAM_RATE_PLAN_NAME = "ratePlanName";
    public static final String PARAM_RATE_PLAN_ID = "ratePlanId";
    public static final String PARAM_SUBSCRIPTION_NUMBER = "subscriptionNumber";
    public static final String PARAM_START_DATE = "startDate";
    public static final String PARAM_TENANT = "tenant";
    /*Zuora types*/
    public static final String ZUORA_ACCOUNT = "Account";
    public static final String ZUORA_PRODUCT = "Product";
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
    /*database tables updated status*/
    public static final String MONETIZATION_DB_UPDATED = "monetizationDbUpdated";
    // File reading related constats.
    public static final String LINE_BREAK = "\r\n";
    // error response related properties
    public static final String EEROR_RESPONSE_PROPERTY_ERRORS = "errors";
    public static final String EEROR_RESPONSE_PROPERTY_SUCCESS = "success";
    public static final String EEROR_RESPONSE_PROPERTY_SUCCESS_SPECIFIED = "successSpecified";
    public static final String EEROR_RESPONSE_PROPERTY_ERRORS_SPECIFIED = "errorsSpecified";
    public static final String ENABLE_MONETIZATION_REGISTRY_PROPERTY = "EnableMonetization";
    /* Zuora queries */
    private static final String ZUORA_ACCOUNT_QUERY_PREFIX = "SELECT id, name, accountnumber, billtoid, " +
                                                             "communicationprofileid, createddate, " +
                                                             "invoicetemplateid," +
                                                             " " +
                                                             "parentid, status, defaultpaymentmethodid FROM account ";
    public static final String QUERY_ZUORA_ACCOUNT_BY_NAME = ZUORA_ACCOUNT_QUERY_PREFIX + "WHERE name = '?'";
    public static final String QUERY_ZUORA_ACCOUNT_BY_ACCOUNT_NO = ZUORA_ACCOUNT_QUERY_PREFIX + "WHERE accountnumber" +
                                                                   " " +
                                                                   "= '?'";

    private BillingConstants() {
    }

    /**
     * SecureValueProperties inner class
     */
    public static final class SecureValueProperties {
        public static final String SECRET_ALIAS_ATTRIBUTE_NAME_WITH_NAMESPACE = "secretAlias";
        public static final String SECURE_VAULT_NS = "http://org.wso2.securevault/configuration";

        private SecureValueProperties() {
            throw new AssertionError();
        }
    }

}
