/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.wso2.carbon.cloud.appcloudlistener;

/**
 * Constants for App Cloud specific REST calls.
 */
public class AppCloudConstants {

    public static final String SERVER_BLOCK_BASE_URL = "serverBlockBaseUrl";
    public static final String ACTION_PARAMETER = "action";
    public static final String POINTED_URL_PARAMETER = "pointedUrl";
    public static final String CUSTOM_URL_PARAMETER = "customUrl";
    public static final String APPLICATION_NAME_PARAMETER = "applicationName";
    public static final String AUTHORIZATION_HEADER_PARAMETER = "authorizationHeader";
    public static final String USERNAME_PARAMETER = "username";
    public static final String JWT_LOGIN_ACTION = "loginWithJWT";
    public static final String UPDATE_CUSTOM_URL_ACTION = "updateCustomUrl";
    public static final String CLOUD_TYPE_PARAMETER = "cloudType";
    public static final String APP_CLOUD_CLOUD_TYPE = "app-cloud";
    public static final String LOGIN_BLOCK_SUFFIX = "user/login/ajax/login.jag";
    public static final String URL_MAPPER_BLOCK_SUFFIX = "urlmapper/urlmapper.jag";
    public static final String SEMI_COLON_SEPARATOR = ";";
    public static final String TRUST_STORE_PROPERTY = "javax.net.ssl.trustStore";
    public static final String TRUST_STORE_PASSWORD_PROPERTY = "javax.net.ssl.trustStorePassword";
    public static final String TRUST_STORE_TYPE_PROPERTY = "javax.net.ssl.trustStoreType";
    public static final String TRUST_STORE_LOCATION = "Security.TrustStore.Location";
    public static final String TRUST_STORE_PASSWORD = "Security.TrustStore.Password";
    public static final String TRUST_STORE_TYPE = "Security.TrustStore.Type";
    public static final int MAX_RETRY_COUNT = 3;

}
