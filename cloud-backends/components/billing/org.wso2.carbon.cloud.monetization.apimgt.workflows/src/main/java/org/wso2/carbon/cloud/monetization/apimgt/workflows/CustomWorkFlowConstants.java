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

package org.wso2.carbon.cloud.monetization.apimgt.workflows;

/**
 * Custom workflow constants
 */
public final class CustomWorkFlowConstants {

    public static final String SUBSCRIBER_INFO_PAYLOAD =
            "<ser:getAPISubscriberInfo xmlns:ser=\"http://service.billing.cloud.carbon.wso2.org\">\n" +
                    "  <ser:username>$1</ser:username>\n" +
                    "  <ser:tenantDomain>$2</ser:tenantDomain>\n" +
                    "</ser:getAPISubscriberInfo>";

    public static final String ADD_SUBSCRIBER_PAYLOAD =
            "<ser:addAPISubscriberInfo xmlns:ser=\"http://service.billing.cloud.carbon.wso2.org\">\n" +
                    "  <ser:username>$1</ser:username>\n" +
                    "  <ser:tenantDomain>$2</ser:tenantDomain>\n" +
                    "  <ser:isTestAccount>$3</ser:isTestAccount>\n" +
                    "  <ser:accountNumber ser:nil=\"true\"/>\n" +
                    "</ser:addAPISubscriberInfo>";

    public static final String CANCEL_SUBSCRIPTION_PAYLOAD =
            "<ser:cancelSubscription xmlns:ser=\"http://service.billing.cloud.carbon.wso2.org\">\n" +
                    " <ser:accountNumber>$1</ser:accountNumber>\n" +
                    " <ser:appName>$2</ser:appName>\n" +
                    " <ser:apiName>$3</ser:apiName>\n" +
                    " <ser:apiVersion>$4</ser:apiVersion>\n" +
                    " </ser:cancelSubscription>";

    public static final String REMOVE_APP_SUBSCRIPTIONS_PAYLOAD =
            "<ser:removeAppSubscriptions> xmlns:ser=\"http://service.billing.cloud.carbon.wso2.org\">\n" +
                    " <ser:accountNumber>$1</ser:accountNumber>\n" +
                    " <ser:appName>$2</ser:appName>\n" +
                    " </ser:removeAppSubscriptions>";

    public static final String CREATE_API_SUBSCRIPTION_PAYLOAD =
            "<ser:createAPISubscription xmlns:ser=\"http://service.billing.cloud.carbon.wso2.org\">\n" +
                    " <ser:accountNumber>$1</ser:accountNumber>\n" +
                    " <ser:tenantDomain>$2</ser:tenantDomain>\n" +
                    " <ser:tierName>$3</ser:tierName>\n" +
                    " <ser:appName>$4</ser:appName>\n" +
                    " <ser:apiName>$5</ser:apiName>\n" +
                    " <ser:apiVersion>$6</ser:apiVersion>\n" +
                    " </ser:createAPISubscription>";

    /*SOAP actions*/
    public static final String SOAP_ACTION_GET_SUBSCRIBER = "urn:getAPISubscriberInfo";
    public static final String SOAP_ACTION_UPDATE_SUBSCRIBER = "urn:addAPISubscriberInfo";
    public static final String SOAP_ACTION_CANCEL_SUBSCRIPTION = "urn:cancelSubscription";
    public static final String SOAP_ACTION_REMOVE_APP_SUBSCRIPTIONS = "urn:removeAppSubscriptions";
    public static final String SOAP_ACTION_CREATE_API_SUBSCRIPTION = "urn:createAPISubscription";

    /*Util constants*/
    public static final String ENCODING = "UTF-8";

    public static final String TIER_PLAN_COMMERCIAL = "COMMERCIAL";
    public static final String TIER_PLAN_FREE = "FREE";

    public static final String IS_TEST_ACCOUNT_PROPERTY = "TestAccount";
    public static final String ACCOUNT_NUMBER_PROPERTY = "AccountNumber";
    public static final String SUBSCRIBERS_OBJ = "Subscribers";
    public static final String SUBSCRIBER_OBJ = "Subscriber";

    public static final String ZUORA_RESPONSE_SUCCESS = "success";
    public static final String MONETIZATION_TABLES_UPDATED = "monetizationDbUpdated";

    private CustomWorkFlowConstants() {
    }
}
