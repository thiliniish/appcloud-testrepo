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

package org.wso2.carbon.appcloud.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.wso2.carbon.cloud.common.CloudMgtException;
import org.wso2.carbon.cloud.listener.CloudListener;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the implementation of interface CloudListener to perform App Cloud specific actions.
 */
public class AppCloudListener implements CloudListener {

    private static Log log = LogFactory.getLog(AppCloudListener.class);
    private static final String TRUST_STORE_PROPERTY = "javax.net.ssl.trustStore";
    private static final String TRUST_STORE_PASSWORD_PROPERTY = "javax.net.ssl.trustStorePassword";
    private static final String TRUST_STORE_TYPE_PROPERTY = "javax.net.ssl.trustStoreType";
    private static final String TRUST_STORE_PASSWORD = "wso2carbon";
    private static final String TRUST_STORE_TYPE = "JKS";
    private static final String TRUST_STORE_LOCATION = "repository/resources/security/wso2carbon.jks";
    private static final String SERVER_BLOCK_BASE_URL = "serverBlockBaseUrl";
    private static final String CONTENT_TYPE_HEADER = "Content-type";
    private static final String CONTENT_TYPE_HEADER_VALUE = "application/x-www-form-urlencoded";
    private static final String CONNECTION_HEADER = "Connection";
    private static final String CONNECTION_HEADER_VALUE = "close";
    private static final String ACTION_PARAMETER = "action";
    private static final String POINTED_URL_PARAMETER = "pointedUrl";
    private static final String CUSTOM_URL_PARAMETER = "customUrl";
    private static final String APPLICATION_NAME_PARAMETER = "applicationName";
    private static final String AUTHORIZATION_HEADER_PARAMETER = "authorizationHeader";
    private static final String USERNAME_PARAMETER = "userName";
    private static final String JWT_LOGIN_ACTION = "jwtLogin";
    private static final String UPDATE_CUSTOM_URL_ACTION = "updateCustomUrl";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String SET_COOKIE_HEADER = "Set-Cookie";
    private static final String COOKIE_HEADER = "Cookie";
    private static final String CLOUD_TYPE_PARAMETER = "cloudType";
    private static final String APP_CLOUD_CLOUD_TYPE = "app-cloud";
    private static final String LOGIN_BLOCK_SUFFIX = "user/login/ajax/login.jag";
    private static final String URL_MAPPER_BLOCK_SUFFIX = "urlmapper/urlmapper.jag";
    private static final String SEMI_COLON_SEPARATOR = ";";
    private static final int MAX_RETRY_COUNT = 3;
    private static final int HTTP_OK_STATUS_CODE = 200;
    private String serverBlockBaseUrl;

    public AppCloudListener() {
        init();
    }

    /**
     * Method to initialize system properties.
     */
    private void init() {
        String fileBasePath = CarbonUtils.getCarbonHome() + File.separator;
        String trustStorePath = fileBasePath + TRUST_STORE_LOCATION;
        System.setProperty(TRUST_STORE_PROPERTY, trustStorePath);
        System.setProperty(TRUST_STORE_PASSWORD_PROPERTY, TRUST_STORE_PASSWORD);
        System.setProperty(TRUST_STORE_TYPE_PROPERTY, TRUST_STORE_TYPE);
    }

    /**
     * Method to execute post method with a retry.
     *
     * @param url           url
     * @param urlParameters paramaters map
     * @param headerMap     header details map
     * @return http response
     * @throws IOException
     */
    private HttpResponse executeHTTPPostWithRetry(String url, List<NameValuePair> urlParameters, HashMap<String,
            String> headerMap) throws IOException {
        int retryCount = 0;
        do {
            HttpResponse response = doPost(url, urlParameters, headerMap);
            if (response.getStatusLine().getStatusCode() == HTTP_OK_STATUS_CODE) {
                return response;
            } else {
                retryCount++;
            }
        } while (retryCount < MAX_RETRY_COUNT);
        return null;
    }

    /**
     * Method to execute http post.
     *
     * @param url           url
     * @param urlParameters parameters map
     * @param headerMap     header details map
     * @return http response
     * @throws IOException
     */
    private HttpResponse doPost(String url, List<NameValuePair> urlParameters, HashMap<String, String> headerMap)
            throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);

        //Add headers
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            post.setHeader(entry.getKey(), entry.getValue());
        }
        //Add parameters
        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        return client.execute(post);
    }

    /**
     * Method to login using jwt to App Cloud.
     *
     * @param userName            user name
     * @param authorizationHeader jwt authorization header
     * @return http response
     * @throws IOException
     */
    private HttpResponse login(String userName, String authorizationHeader) throws IOException {
        String loginUrl = serverBlockBaseUrl + LOGIN_BLOCK_SUFFIX;

        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put(CONTENT_TYPE_HEADER, CONTENT_TYPE_HEADER_VALUE);
        headerMap.put(AUTHORIZATION_HEADER, authorizationHeader);
        headerMap.put(CONNECTION_HEADER, CONNECTION_HEADER_VALUE);

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(ACTION_PARAMETER, JWT_LOGIN_ACTION));
        urlParameters.add(new BasicNameValuePair(USERNAME_PARAMETER, userName));

        return executeHTTPPostWithRetry(loginUrl, urlParameters, headerMap);
    }

    /**
     * Method to get session Id from log in response.
     *
     * @param userName            user name
     * @param authorizationHeader jwt authorization header
     * @return session Id
     * @throws IOException
     */
    private String getSessionId(String userName, String authorizationHeader) throws IOException {
        HttpResponse response = login(userName, authorizationHeader);
        if (response != null) {
            return response.getHeaders(SET_COOKIE_HEADER)[0].getValue().split(SEMI_COLON_SEPARATOR)[0];
        } else {
            return null;
        }
    }

    /**
     * Method to invoke REST method to update App Cloud database and add custom domain label to default domain services.
     *
     * @param sessionId       session id
     * @param pointedUrl      default domain url for application
     * @param customUrl       custom url added by user
     * @param applicationName application name
     * @return http response
     * @throws IOException
     */
    private HttpResponse updateCustomUrl(String sessionId, String pointedUrl, String customUrl, String applicationName)
            throws IOException {
        String urlMapperUrl = serverBlockBaseUrl + URL_MAPPER_BLOCK_SUFFIX;

        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put(CONTENT_TYPE_HEADER, CONTENT_TYPE_HEADER_VALUE);
        headerMap.put(COOKIE_HEADER, sessionId);
        headerMap.put(CONNECTION_HEADER, CONNECTION_HEADER_VALUE);

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair(ACTION_PARAMETER, UPDATE_CUSTOM_URL_ACTION));
        urlParameters.add(new BasicNameValuePair(POINTED_URL_PARAMETER, pointedUrl));
        urlParameters.add(new BasicNameValuePair(CUSTOM_URL_PARAMETER, customUrl));
        urlParameters.add(new BasicNameValuePair(APPLICATION_NAME_PARAMETER, applicationName));

        return executeHTTPPostWithRetry(urlMapperUrl, urlParameters, headerMap);
    }

    /**
     * Method to invoke App Cloud REST API to update custom url details for application.
     *
     * @param parameterMap map with parameters required to invoke App Cloud REST API
     * @throws CloudMgtException
     */
    @Override
    public void triggerOnCustomUrlAdded(HashMap<String, String> parameterMap) throws CloudMgtException {
        String userName = parameterMap.get(USERNAME_PARAMETER);
        try {
            log.info("Started triggering actions for App Cloud after setting custom URL");
            String cloudType = parameterMap.get(CLOUD_TYPE_PARAMETER);
            if (APP_CLOUD_CLOUD_TYPE.equals(cloudType)) {
                String authorizationHeader = parameterMap.get(AUTHORIZATION_HEADER_PARAMETER);
                serverBlockBaseUrl = parameterMap.get(SERVER_BLOCK_BASE_URL);
                String sessionId = getSessionId(userName, authorizationHeader);
                if (sessionId != null) {
                    String pointedUrl = parameterMap.get(POINTED_URL_PARAMETER);
                    String customUrl = parameterMap.get(CUSTOM_URL_PARAMETER);
                    String applicationName = parameterMap.get(APPLICATION_NAME_PARAMETER);

                    HttpResponse response = updateCustomUrl(sessionId, pointedUrl, customUrl, applicationName);
                    if (response != null) {
                        log.info("Completed actions for App Cloud after setting custom URL");
                    } else {
                        String msg = "Updating custom url: " + customUrl + " for application: " + applicationName +
                                " for user: " + userName + " failed.";
                        log.error(msg);
                        throw new CloudMgtException(msg);
                    }
                } else {
                    String msg = "Authentication of user " + userName + " from App Cloud failed.";
                    log.error(msg);
                    throw new CloudMgtException(msg);
                }
            }
        } catch (IOException e) {
            String msg = "Error occurred while updating custom url for user: " + userName + ".";
            log.error(msg, e);
            throw new CloudMgtException(msg, e);
        }
    }

}
