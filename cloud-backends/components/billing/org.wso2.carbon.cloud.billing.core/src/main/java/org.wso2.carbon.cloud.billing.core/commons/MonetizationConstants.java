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

import javax.xml.namespace.QName;

/**
 * API Cloud Monetization specific constants
 */
public final class MonetizationConstants {

    /*Common cloud monetization DSS v1 suffixes*/
    public static final String DS_API_URI_MONETIZATION_STATUS = "/v1/monetization-status/{tenant}/{cloudType}";
    public static final String DS_API_URI_MONETIZATION_TENANT_RATE_PLAN =
            "/v1/rate-plans/{tenant}/{zuoraProductName}/{ratePlanName}";

    /*API Cloud Monetization DSS v1 suffixes*/
    public static final String DS_API_URI_MON_APIC_SUBSCRIBER = "/v1/subscribers/{tenant}/{username}";
    public static final String DS_API_URI_MON_APIC_DAILY_USAGE = "/v1/usage/daily-usage";
    public static final String DS_API_URI_UPDATE_API_SUBSCRIPTION = "/v1/apim/subscriptions/{tenantId}";
    public static final String DS_API_URI_API_SUBSCRIPTION_HISTORY =
            "/v1/subscriptions-history/{accountNumber}/{appName}/{apiName}/{apiVersion}";
    public static final String DS_API_URI_APP_SUBSCRIPTIONS = "/v1/app-subscriptions/{accountNumber}/{appName}";
    public static final String DS_API_URI_MON_APIC_SUBSCRIPTION =
            "/v1/subscriptions/{accountNumber}/{appName}/{apiName}/{apiVersion}";
    public static final String DS_API_URI_TENANT_USAGE = "/v1/usage/tenants/{tenant}";
    public static final String DS_API_URI_API_USAGE = "/v1/usage/apis/{apiName}/{version}";
    public static final String DS_API_URI_USER_APIS = "/v1/apis/{username}";
    public static final String DS_API_URI_USER_API_APPLICATIONS = "/v1/apps/{username}/{apiName}";
    public static final String DS_API_URI_SUBSCRIBER_USAGE = "/v1/usage/subscribers/{subscriberId}";
    public static final String DS_API_URI_SUBSCRIBER_API_USAGE_BY_APPLICATION =
            "/v1/usage/subscriber-application-api/{subscriberId}/{appName}/{apiName}/{version}";
    public static final String DS_API_URI_SUBSCRIBER_API_USAGE =
            "/v1/usage/subscriber-api/{subscriberId}/{apiName}/{version}";
    public static final String DS_API_URI_APIC_RATE_PLANS = "/v1/rate-plans/tenants/{tenant}/api-cloud";
    public static final String DS_API_URI_REMOVE_API_SUBSCRIPTION = "/v1/apim/{tenantId}/{subscriberId}/subscriptions";
    public static final String DS_API_URI_SUBSCRIBER_USAGE_INFORMATION = "/v1/usage/usage-info";

    /*resource identifiers*/
    public static final String RESOURCE_IDENTIFIER_TENANT = "{tenant}";
    public static final String RESOURCE_IDENTIFIER_TENANT_ID = "{tenantId}";
    public static final String RESOURCE_IDENTIFIER_USERNAME = "{username}";
    public static final String RESOURCE_IDENTIFIER_ACCOUNT_NO = "{accountNumber}";
    public static final String RESOURCE_IDENTIFIER_APP_NAME = "{appName}";
    public static final String RESOURCE_IDENTIFIER_API_NAME = "{apiName}";
    public static final String RESOURCE_IDENTIFIER_API_VERSION = "{apiVersion}";
    public static final String RESOURCE_IDENTIFIER_CLOUD_TYPE = "{cloudType}";
    public static final String RESOURCE_IDENTIFIER_ZUORA_PRODUCT_NAME = "{zuoraProductName}";
    public static final String RESOURCE_IDENTIFIER_RATE_PLAN_NAME = "{ratePlanName}";
    public static final String RESOURCE_IDENTIFIER_VERSION = "{version}";
    public static final String RESOURCE_IDENTIFIER_SUBSCRIBER_ID = "{subscriberId}";

    /*param names*/
    public static final String PARAM_IS_TEST_ACCOUNT = "isTestAccount";

    /*api manager user subscriptions related */
    public static final String USER_ID = "userId";
    public static final String API_SUBSCRIPTION_STATUS = "subStatus";
    public static final String API_SUBSCRIPTION_BLOCKED_STATUS = "BLOCKED";
    public static final String FREE_TIER = "freeTier";
    public static final String FREE = "FREE";

    public static final String ACCOUNT = "Account";
    public static final String RATE_PLAN = "RatePlan";
    public static final String RATE_PLANS = "RatePlans";
    public static final String RATE_PLAN_ID = "RatePlanId";
    public static final String SUBSCRIPTION = "Subscription";
    public static final String SUBSCRIPTIONS = "Subscriptions";
    public static final String SUBSCRIPTION_NUMBER = "SubscriptionNumber";
    public static final String MAX_DAILY_USAGE = "MaxDailyUsage";
    public static final String ENTRY = "Entry";
    public static final String YEAR = "Year";
    public static final String MONTH = "Month";
    public static final String DAY = "Day";
    public static final String UNIT_OF_MEASURE = "OverUsageUnits";
    public static final int OVER_USAGE_THRESHOLD = 0;
    public static final String TOTAL_COUNT = "TotalCount";
    public static final String CSV_EXTENSION = ".csv";
    public static final String UNIT_OF_MEASURE_DISPLAY_NAME = "100K Overage Daily API Calls";
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String TENANT = "tenant";
    public static final String ASTERISK_SYMBOL = "*";
    public static final String USAGE_FILTER_QUERY = "filterQuery";
    public static final String USAGE_GROUP_BY_QUERY = "groupByQuery";

    /* SOAP body constants */
    public static final String SOAP_APP_NAME = "appName";
    public static final String SOAP_API_NAME = "apiName";
    public static final String SOAP_API_VERSION = "apiVersion";
    public static final String SOAP_API_PROVIDER = "apiProvider";

    /* XML/JSON attribute constants */
    public static final String ATTRIB_APP_NAME = "AppName";
    public static final String ATTRIB_API_NAME = "ApiName";
    public static final String ATTRIB_API_VERSION = "ApiVersion";
    public static final String ATTRIB_API_PROVIDER = "ApiProvider";

    /*tiers.xml related attributes*/
    public static final String TIERS_XML_URL = "/apimgt/applicationdata/tiers.xml";
    public static final String UNAUTHENTICATED = "Unauthenticated";
    public static final String THROTTLE_NAMESPACE = "http://www.wso2.org/products/wso2commons/throttle";
    public static final QName ASSERTION_ELEMENT = new QName(THROTTLE_NAMESPACE, "MediatorThrottleAssertion");
    public static final QName POLICY_ELEMENT = new QName("http://schemas.xmlsoap.org/ws/2004/09/policy", "Policy");
    public static final QName THROTTLE_ID_ELEMENT = new QName(THROTTLE_NAMESPACE, "ID");
    public static final QName THROTTLE_CONTROL_ELEMENT = new QName(THROTTLE_NAMESPACE, "Control");
    public static final QName THROTTLE_ATTRIBUTES_ELEMENT = new QName(THROTTLE_NAMESPACE, "Attributes");
    public static final QName THROTTLE_ATTRIBUTES_BILLING_PLAN_ELEMENT = new QName(
            MonetizationConstants.THROTTLE_NAMESPACE, "x-wso2-BillingPlan");
    public static final QName THROTTLE_ATTRIBUTES_MAXIMUM_COUNT_ELEMENT = new QName(
            MonetizationConstants.THROTTLE_NAMESPACE, "MaximumCount");
    public static final QName THROTTLE_ATTRIBUTES_UNIT_TIME_ELEMENT = new QName(
            MonetizationConstants.THROTTLE_NAMESPACE, "UnitTime");
    public static final String TIER_NAME = "tierName";
    public static final String MAXIMUM_COUNT = "maximumCount";
    public static final String UNIT_TIME = "unitTime";
    public static final String BILLING_PLAN = "BillingPlan";

    /*workflow-extensions.xml related attributes*/
    public static final String WORKFLOW_EXTENSION_URL = "/apimgt/applicationdata/workflow-extensions.xml";
    public static final String SUBSCRIPTION_CREATION_EXECUTOR =
            "org.wso2.carbon.cloud.monetization.apimgt.workflows.SubscriptionCreationWorkflowExecutor";
    public static final String PROPERTY_SERVICEURL_NAME = "apiMonetizationServiceUrl";
    public static final String PROPERTY_MONETIZATION_SERVICE_VALUE =
            "/services/APICloudMonetizationService/";
    public static final String PROPERTY_SERVICE_END_POINT = "serviceEndpoint";
    public static final String PROPERTY_USERNAME_NAME = "username";
    public static final String PROPERTY_PASSWORD_NAME = "password";
    public static final String TAG_WORKFLOWEXTENSION = "WorkFlowExtensions";
    public static final String TAG_SUSCRIPTION_CREATION = "SubscriptionCreation";
    public static final String ATTRIBUTE_EXECUTOR = "executor";
    public static final String ATTRIBUTE_PROPERTY = "Property";
    public static final String ATTRIBUTE_NAME = "name";
    public static final String YES = "yes";

    // Registry related configurations
    public static final String TENANT_CONF_URL = "apimgt/applicationdata/tenant-conf.json";
    public static final String EMAIL_FILE_BASE_URL = "/apimgt/pricing/resources/";
    //Email related Constants
    public static final String EMAIL_BODY_OVERAGE_FAILURE = "Hi Cloud Team, \n Error has occurred while uploading " +
                                                            "the" +
                                                            " overage data to zuora on {today}. Please verify.";
    public static final String EMAIL_SUBJECT_OVERAGE_FAILURE = "Error Uploading overage data ";
    public static final String REPLACE_TODAY = "{today}";
    //Email notifications related parameters
    public static final String SUBSCRIPTION_NOTIFICATION_EMAIL_FILE_NAME = "user-subscription-notification-email.txt";
    public static final String TEST_ACCOUNT_CREATION_EMAIL_FILE_NAME = "monetization-create-test-account.txt";
    public static final String TEST_ACCOUNT_DELETION_EMAIL_FILE_NAME = "monetization-remove-test-account.txt";

    public static final String EMAIL_RESOURCES_FOLDER = "repository/resources/cloud/emails";

    /* DSS query-strings */
    public static final String GROUP_USAGE_BY_MONTH = "year,month";
    public static final String GROUP_USAGE_BY_DAY = "year,month,day";

    //This is used for JSON responses
    public static final String RESPONSE_SUCCESS = "success";
    public static final String RESPONSE_DATA = "data";

    private MonetizationConstants() {
    }
}
