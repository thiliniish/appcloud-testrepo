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
    public static final String DS_API_URI_MON_APIC_SUBSCRIPTION =
            "/v1/subscriptions/{accountNumber}/{appName}/{apiName}/{apiVersion}";
    public static final String DS_API_URI_TENANT_USAGE = "/v1/usage/tenants/{tenant}";
    public static final String DS_API_URI_API_USAGE = "/v1/usage/apis/{api}/{version}";
    public static final String DS_API_URI_SUBSCRIBER_USAGE = "/v1/usage/subscribers/{subscriberId}";
    public static final String DS_API_URI_SUBSCRIBER_API_USAGE_BY_APPLICATION = "/v1/usage/subscriber-application-api/{subscriberId}/{application}/{api}/{version}";
    public static final String DS_API_URI_SUBSCRIBER_API_USAGE = "/v1/usage/subscriber-api/{subscriberId}/{api}/{version}";

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
    public static final String RESOURCE_IDENTIFIER_API = "{api}";
    public static final String RESOURCE_IDENTIFIER_VERSION = "{version}";
    public static final String RESOURCE_IDENTIFIER_SUBSCRIBER_ID = "{subscriberId}";
    public static final String RESOURCE_IDENTIFIER_APPLICATION = "{application}";

    /*Usage related*/
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String TENANT = "tenant";
    public static final String APPLICATION = "application";

    /*param names*/
    public static final String PARAM_IS_TEST_ACCOUNT = "isTestAccount";

    /*api manager use subscriptions related */
    public static final String USER_ID = "userId";
    public static final String API_SUBSCRIPTION_STATUS = "subStatus";
    public static final String API_SUBSCRIPTION_BLOCKED_STATUS = "BLOCKED";

    public static final String ACCOUNT = "Account";
    public static final String RATE_PLAN = "RatePlan";
    public static final String RATE_PLAN_ID = "RatePlanId";
    public static final String SUBSCRIPTION = "Subscription";
    public static final String MAX_DAILY_USAGE = "MaxDailyUsage";
    public static final String ENTRY = "Entry";
    public static final String YEAR = "Year";
    public static final String MONTH = "Month";
    public static final String DAY = "Day";
    public static final String UNIT_OF_MEASURE = "OverUsageUnits";
    public static final int OVER_USAGE_THRESHOLD = 0;
    public static final String TOTAL_COUNT = "TotalCount";
    public static final String CSV_EXTENSION = ".csv";
    public static final String UNIT_OF_MEASURE_DISPLAY_NAME = "10K Overage Daily API Calls";

    //Email related Constants
    public static final String EMAIL_BODY_OVERAGE_FAILURE = "Hi Cloud Team, \n Error has occurred while uploading the" +
                                                           " overage data to zuora on {today}. Please verify.";
    public static final String EMAIL_SUBJECT_OVERAGE_FAILURE = "Error Uploading overage data " ;
    public static final String REPLACE_TODAY = "{today}";

    private MonetizationConstants() {
    }
}
