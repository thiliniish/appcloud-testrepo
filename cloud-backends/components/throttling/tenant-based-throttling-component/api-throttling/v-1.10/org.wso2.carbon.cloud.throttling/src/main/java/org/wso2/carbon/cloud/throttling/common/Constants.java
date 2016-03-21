/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License
 */
package org.wso2.carbon.cloud.throttling.common;

/**
 * Tenant Throttling related constants.
 * todo move relevant constants to config
 */
public class Constants {

    /*
    * Data base name
    * */
    public static final String CLOUD_DB_NAME = "jdbc/cloud_mgt";
    /*
    * cloud subscription
    * */
    public static final String SUBSCRIPTION_API_CLOUD = "api_cloud";

    /*
    * cloud subscription
    * */
    public static final String RATE_PLAN_DEFAULT = "Free";

    /*
    *  Time out interval for Rate Plan Cache
    * */
    public static final long CACHE_TIME_TO_LIVE = 60 * 60 * 1000;
    /*
    * Delay for cache cleanup Timer to start
    *
    * */
    public static final long CACHE_TIME_DELAY = 60 * 60 * 1000;

    /*
    * role_based throttle context
    * */
    public static final String ROLE_BASED_THROTTLE_KEY = "key_of_role_based_throttle";
    /*
    * throttle policy resource path
    * */
    public static final String POLICY_KEY = "/throttling/tenant_tier_policies.xml";
    /*
    * Interval to notify throttled out tenants
    * */
    public static final long EMAIL_ALERT_DELAY = 15 * 60 * 1000;
    /*
    * enable/disable email alert to tenant admin when tenant level throttle out occurred
    * */
    public static final boolean IS_ALERT_ENABLED_FOR_TENANT_ADMIN = true;
    /*
    * Sender email
    * */
    public static final String SENDER_EMAIL = "wso2heartbeat@gmail.com";
    /*
    * Sender email
    * */
    public static final String SENDER_EMAIL_PASSWORD = "w$o2admin";
    /*
    * Sender email
    * */
    public static final String SENDER_HOST = "smtp.gmail.com";
    /*
    * Sender email
    * */
    public static final String SENDER_PORT = "25";


}
