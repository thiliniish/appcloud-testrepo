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

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.cookie.SM;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.wso2.carbon.cloud.common.CloudMgtException;
import org.wso2.carbon.cloud.listener.CloudListener;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the implementation of interface CloudListener to perform App Cloud specific actions.
 */
public class AppCloudListener implements CloudListener {

    private static final Log log = LogFactory.getLog(AppCloudListener.class);
    private String serverBlockBaseUrl;

    public AppCloudListener() {
        init();
    }

    /**
     * Method to initialize system properties.
     */
    private void init() {
        System.setProperty(AppCloudConstants.TRUST_STORE_PROPERTY,
                CarbonUtils.getServerConfiguration().getFirstProperty(AppCloudConstants.TRUST_STORE_LOCATION));
        System.setProperty(AppCloudConstants.TRUST_STORE_PASSWORD_PROPERTY,
                CarbonUtils.getServerConfiguration().getFirstProperty(AppCloudConstants.TRUST_STORE_PASSWORD));
        System.setProperty(AppCloudConstants.TRUST_STORE_TYPE_PROPERTY,
                CarbonUtils.getServerConfiguration().getFirstProperty(AppCloudConstants.TRUST_STORE_TYPE));

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
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return response;
            } else {
                retryCount++;
            }
        } while (retryCount < AppCloudConstants.MAX_RETRY_COUNT);
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
     * @param username            user name
     * @param authorizationHeader jwt authorization header
     * @return http response
     * @throws IOException
     */
    private HttpResponse login(String username, String authorizationHeader) throws IOException {
        String loginUrl = serverBlockBaseUrl + AppCloudConstants.LOGIN_BLOCK_SUFFIX;

        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put(HttpHeaders.CONTENT_TYPE, URLEncodedUtils.CONTENT_TYPE);
        headerMap.put(HttpHeaders.AUTHORIZATION, authorizationHeader);
        headerMap.put(HttpHeaders.CONNECTION, HTTP.CONN_CLOSE);

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(
                new BasicNameValuePair(AppCloudConstants.ACTION_PARAMETER, AppCloudConstants.JWT_LOGIN_ACTION));
        urlParameters.add(new BasicNameValuePair(AppCloudConstants.USERNAME_PARAMETER, username));

        return executeHTTPPostWithRetry(loginUrl, urlParameters, headerMap);
    }

    /**
     * Method to get session Id from log in response.
     *
     * @param username            user name
     * @param authorizationHeader jwt authorization header
     * @return session Id
     * @throws IOException
     */
    private String getSessionId(String username, String authorizationHeader) throws IOException {
        HttpResponse response = login(username, authorizationHeader);
        if (response != null) {
            return response.getHeaders(SM.SET_COOKIE)[0].getValue().split(AppCloudConstants.SEMI_COLON_SEPARATOR)[0];
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
        String urlMapperUrl = serverBlockBaseUrl + AppCloudConstants.URL_MAPPER_BLOCK_SUFFIX;

        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put(HttpHeaders.CONTENT_TYPE, URLEncodedUtils.CONTENT_TYPE);
        headerMap.put(SM.COOKIE, sessionId);
        headerMap.put(HttpHeaders.CONNECTION, HTTP.CONN_CLOSE);

        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(
                new BasicNameValuePair(AppCloudConstants.ACTION_PARAMETER, AppCloudConstants.UPDATE_CUSTOM_URL_ACTION));
        urlParameters.add(new BasicNameValuePair(AppCloudConstants.POINTED_URL_PARAMETER, pointedUrl));
        urlParameters.add(new BasicNameValuePair(AppCloudConstants.CUSTOM_URL_PARAMETER, customUrl));
        urlParameters.add(new BasicNameValuePair(AppCloudConstants.APPLICATION_NAME_PARAMETER, applicationName));

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
        String username = parameterMap.get(AppCloudConstants.USERNAME_PARAMETER);
        try {
            log.info("Started triggering actions for App Cloud after setting custom URL");
            String cloudType = parameterMap.get(AppCloudConstants.CLOUD_TYPE_PARAMETER);
            if (AppCloudConstants.APP_CLOUD_CLOUD_TYPE.equals(cloudType)) {
                String authorizationHeader = parameterMap.get(AppCloudConstants.AUTHORIZATION_HEADER_PARAMETER);
                serverBlockBaseUrl = parameterMap.get(AppCloudConstants.SERVER_BLOCK_BASE_URL);
                String sessionId = getSessionId(username, authorizationHeader);
                if (sessionId != null) {
                    String pointedUrl = parameterMap.get(AppCloudConstants.POINTED_URL_PARAMETER);
                    String customUrl = parameterMap.get(AppCloudConstants.CUSTOM_URL_PARAMETER);
                    String applicationName = parameterMap.get(AppCloudConstants.APPLICATION_NAME_PARAMETER);

                    HttpResponse response = updateCustomUrl(sessionId, pointedUrl, customUrl, applicationName);
                    if (response != null) {
                        log.info("Completed actions for App Cloud after setting custom URL");
                    } else {
                        String msg = "Updating custom url: " + customUrl + " for application: " + applicationName +
                                " for user: " + username + " failed.";
                        log.error(msg);
                        throw new CloudMgtException(msg);
                    }
                } else {
                    String msg = "Authentication of user " + username + " from App Cloud failed.";
                    log.error(msg);
                    throw new CloudMgtException(msg);
                }
            }
        } catch (IOException e) {
            String msg = "Error occurred while updating custom url for user: " + username + ".";
            log.error(msg, e);
            throw new CloudMgtException(msg, e);
        }
    }

}
