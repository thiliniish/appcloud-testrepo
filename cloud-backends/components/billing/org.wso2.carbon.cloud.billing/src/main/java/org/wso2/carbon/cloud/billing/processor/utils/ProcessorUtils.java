/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.cloud.billing.processor.utils;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingException;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Utility methods for HTTP request processors
 */
public class ProcessorUtils {

    private static final Log LOGGER = LogFactory.getLog(ProcessorUtils.class);
    private static final String NOT_FOUND_ERROR_MSG = "Failed with HTTP error code : " + HttpURLConnection
            .HTTP_NOT_FOUND + " (Not Found). URI is incorrect.";
    private static final String AUTH_ERROR_MSG = "Failed with HTTP error code : " + HttpURLConnection.HTTP_UNAUTHORIZED
                                                 + " (Unauthorized). Credentials used are incorrect.";

    private ProcessorUtils() {
    }

    /**
     * Executes the HTTPMethod with retry.
     *
     * @param httpClient     HTTPClient
     * @param httpMethod     HTTPMethod
     * @param executionCount No of retries
     * @return response. it will return an empty string if response body is null
     * @throws CloudBillingException
     */
    public static String executeHTTPMethodWithRetry(HttpClient httpClient, HttpMethodBase httpMethod,
                                                    int executionCount) throws CloudBillingException {

        int response;
        int retryCount = 0;
        String result = "";
        String methodName = httpMethod.getName();
        String uri = getURI(httpMethod);

        do {
            try {
                response = httpClient.executeMethod(httpMethod);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("HTTP response code for the " + methodName + " request to URI: " + uri + " is " +
                                 response);
                }
                switch (response) {
                    case HttpURLConnection.HTTP_OK:
                        return handleCaseHTTPOk(httpMethod);

                    case HttpURLConnection.HTTP_ACCEPTED:
                        //Reason : https://wso2.org/jira/browse/DS-886
                        httpMethod.releaseConnection();
                        return String.valueOf(response);

                    case HttpURLConnection.HTTP_NOT_FOUND:
                        throw new CloudBillingException(NOT_FOUND_ERROR_MSG);

                    case HttpURLConnection.HTTP_UNAUTHORIZED:
                        throw new CloudBillingException(AUTH_ERROR_MSG);

                    default:
                        retryCount++;
                        handleDefaultCase(executionCount, response, retryCount, methodName, uri);
                        break;
                }
                httpMethod.releaseConnection();
            } catch (CloudBillingException ex) {
                throw ex;
            } catch (Exception ex) {
                retryCount++;
                handleExceptionWithRetry(executionCount, retryCount, methodName, uri, ex);
            } finally {
                httpMethod.releaseConnection();
            }

        } while (retryCount < executionCount);
        return result;
    }

    /**
     * Executes HTTPMethod without retry
     *
     * @param httpClient HTTPClient
     * @param httpMethod HTTPMethod
     * @return response. it will return an empty string if response body is null
     * @throws CloudBillingException
     */
    public static String executeHTTPMethod(HttpClient httpClient, HttpMethodBase httpMethod)
            throws CloudBillingException {

        int response;
        String uri = getURI(httpMethod);
        String methodName = httpMethod.getName();

        try {
            response = httpClient.executeMethod(httpMethod);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("HTTP response code for the " + methodName + " request: " + uri + " is " + response);
            }
            switch (response) {
                case HttpURLConnection.HTTP_OK:
                    return handleCaseHTTPOk(httpMethod);

                case HttpURLConnection.HTTP_ACCEPTED:
                    //Reason : https://wso2.org/jira/browse/DS-886
                    httpMethod.releaseConnection();
                    return String.valueOf(response);

                case HttpURLConnection.HTTP_NOT_FOUND:
                    throw new CloudBillingException(NOT_FOUND_ERROR_MSG);

                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    throw new CloudBillingException(AUTH_ERROR_MSG);

                default:
                    throw new CloudBillingException(methodName + " request failed for URI: " + uri
                                                    + " with HTTP error code : " + response);
            }
        } catch (CloudBillingException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CloudBillingException(methodName + " request failed for URI: " + uri, ex);
        } finally {
            httpMethod.releaseConnection();
        }
    }

    private static String handleCaseHTTPOk(HttpMethodBase httpMethod) throws IOException {
        String result = "";
        if (httpMethod.getResponseBody().length > 0) {
            result = httpMethod.getResponseBodyAsString();
        }
        httpMethod.releaseConnection();
        return result;
    }

    private static void handleExceptionWithRetry(int executionCount, int retryCount, String methodName, String uri,
                                                 Exception ex) throws CloudBillingException {
        if (retryCount >= executionCount) {
            throw new CloudBillingException(methodName + " request failed for the " + retryCount + " attempt for URI:" +
                                            " " + uri, ex);
        } else {
            LOGGER.warn(methodName + " request failed for URI: " + uri + " with exception : " + ex.getMessage()
                        + ". Retry attempt: " + retryCount + "/" + executionCount);
        }
    }

    private static void handleDefaultCase(int executionCount, int response, int retryCount, String methodName,
                                          String uri) throws CloudBillingException {
        if (retryCount >= executionCount) {
            throw new CloudBillingException(methodName + " request failed for the " + retryCount + " attempt for URI:" +
                                            " " + uri
                                            + " with HTTP error code: " + response);
        } else {
            LOGGER.warn(methodName + " request failed for URI: " + uri + " with HTTP error code: " +
                        response + ". Retry: " + retryCount + "/" + executionCount);
        }
    }

    private static String getURI(HttpMethodBase httpMethod) throws CloudBillingException {
        String uri;
        try {
            uri = httpMethod.getURI().getURI();
        } catch (URIException e) {
            throw new CloudBillingException("URI exception while getting the URI", e);
        }
        return uri;
    }
}