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
    public static final String DATE_FORMAT_YEAR_MONTH_DAY = "yyyy-MM-dd";
    public static final String DATA = "data";

    /**
     * OAuth enpoint related params
     */
    public static final String TOKEN_ENDPOINT = "/oauth/token";
    public static final String GRANT_TYPE = "grant_type";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String CODE = "code";
    public static final String AUTHORIZATION_CODE = "authorization_code";

    /**
     * Monetization account table params
     */
    public static final String ACCOUNT_NUMBER = "account_number";
    public static final String TOKEN_TYPE = "token_type";
    public static final String STRIPE_PUBLISHABLE_KEY = "stripe_publishable_key";
    public static final String STRIPE_ACCESS_TOKEN = "AccessToken";
    public static final String SCOPE = "scope";
    public static final String LIVEMODE = "livemode";
    public static final String STRIPE_USER_ID = "stripe_user_id";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String ACCOUNT_CREATION_DATE = "account_creation_date";

    public static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

    public static final String DS_API_URI_VENDOR_ACCOUNT_INFO = "/v1/accounts/getVendorInfo/{accountNumber}";
    public static final String RESOURCE_IDENTIFIER_CUSTOMER_ID = "{accountNumber}";

    /**
     * Invoice related params
     */
    public static final int CENTS = 100;
    public static final String SUBSCRIPTION_NAME = "subscriptionName";
    public static final String SERVICE_PERIOD = "servicePeriod";
    public static final String CHARGE_DATE = "chargeDate";
    public static final String CHARGE_NAME = "chargeName";
    public static final String DISCOUNT = "discount";
    public static final String AMOUNT = "amount";
    public static final String STARTING_BALANCE = "starting_balance";
    public static final String ORGANIZATION = "organization";
    public static final String EMAIL = "email";
    public static final String ADDITIONAL_EMAILS = "additionalEmails";
    public static final String NAME = "name";
    public static final String CUSTOMER_ID = "customerId";
    public static final String INVOICE_NUMBER = "invoiceNumber";
    public static final String INVOICE_DATE = "invoiceDate";
    public static final String INVOICE_ITEM = "invoiceItem";
    public static final String ADDRESS_CITY = "addressCity";
    public static final String ADDRESS_LINE1 = "addressLine1";
    public static final String ADDRESS_LINE2 = "addressLine2";
    public static final String ADDRESS_ZIP = "addressZip";
    public static final String ADDRESS_COUNTRY = "addressCountry";

}
