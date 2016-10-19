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

package org.wso2.carbon.cloud.billing.vendor.commons;

/**
 * Billing vendor constant file
 */
public final class BillingVendorConstants {

    public static final String EMPTY_STRING = "";
    public static final String ACTIVE_RESPONSE = "active";
    public static final String RESPONSE_SUCCESS = "success";
    public static final String RESPONSE_DATA = "data";
    public static final String RESPONSE_MESSAGE = "message";
    public static final String AT_PERIOD_END = "at_period_end";

    /**
     * OAuth enpoint related params
     **/
    public static final String TOKEN_ENDPOINT = "/oauth/token";
    public static final String GRANT_TYPE = "grant_type";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String CODE = "code";
    public static final String AUTHORIZATION_CODE = "authorization_code";

    /**
     * Monetization account table params
     **/
    public static final String ACCOUNT_NUMBER = "account_number";
    public static final String TOKEN_TYPE = "token_type";
    public static final String STRIPE_PUBLISHABLE_KEY = "stripe_publishable_key";
    public static final String STRIPE_ACCESS_TOKEN = "ACCESS_TOKEN";
    public static final String SCOPE = "scope";
    public static final String LIVEMODE = "livemode";
    public static final String STRIPE_USER_ID = "stripe_user_id";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String ACCOUNT_CREATION_DATE = "account_creation_date";

    public static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

    public static final String DS_API_URI_VENDOR_ACCOUNT_INFO = "/v1/accounts/getVendorInfo/{accountNumber}";
    public static final String RESOURCE_IDENTIFIER_CUSTOMER_ID = "{accountNumber}";
}
