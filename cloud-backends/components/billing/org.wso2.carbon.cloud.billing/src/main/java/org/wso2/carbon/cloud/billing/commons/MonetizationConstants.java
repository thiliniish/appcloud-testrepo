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

    /*API Cloud Monetization DSS v1 suffixes*/
    public static final String DS_API_URI_MON_APIC_SUBSCRIBER = "/v1/subscribers/{tenant}/{username}";
    public static final String DS_API_URI_MON_APIC_DAILY_USAGE = "/v1/usage/daily-usage";
    public static final String DS_API_URI_UPDATE_API_SUBSCRIPTION = "/v1/apim/subscriptions/{tenantId}";
    public static final String DS_API_URI_MON_APIC_SUBSCRIPTION =
            "/v1/subscriptions/{accountNumber}/{appName}/{apiName}/{apiVersion}";

    /*resource identifiers*/
    public static final String RESOURCE_IDENTIFIER_TENANT = "{tenant}";
    public static final String RESOURCE_IDENTIFIER_TENANT_ID = "{tenantId}";
    public static final String RESOURCE_IDENTIFIER_USERNAME = "{username}";
    public static final String RESOURCE_IDENTIFIER_ACCOUNT_NO = "{accountNumber}";
    public static final String RESOURCE_IDENTIFIER_APP_NAME = "{appName}";
    public static final String RESOURCE_IDENTIFIER_API_NAME = "{apiName}";
    public static final String RESOURCE_IDENTIFIER_API_VERSION = "{apiVersion}";

    /*param names*/
    public static final String PARAM_IS_TEST_ACCOUNT = "isTestAccount";

    /*api manager use subscriptions related */
    public static final String USER_ID = "userId";
    public static final String API_SUBSCRIPTION_STATUS = "subStatus";
    public static final String API_SUBSCRIPTION_BLOCKED_STATUS = "BLOCKED";

    public static final String ACCOUNT = "Account";
    public static final String RATE_PLAN = "RatePlan";
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
