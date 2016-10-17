/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.cloud.billing.vendor.commons.processor.utils;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.billing.core.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.vendor.stripe.exceptions.CloudBillingVendorException;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Utility methods for HTTP request processors
 */
public class ProcessorUtils {

    private static final Log LOGGER = LogFactory.getLog(ProcessorUtils.class);
    private static final String NOT_FOUND_ERROR_MSG =
            "Failed with HTTP error code : " + HttpURLConnection.HTTP_NOT_FOUND + " (Not Found). URI is incorrect.";
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
     * @throws CloudBillingVendorException
     */
    public static String executeHTTPMethodWithRetry(HttpClient httpClient, HttpMethodBase httpMethod,
            int executionCount) throws CloudBillingVendorException {

        int response;
        int retryCount = 0;
        String result = BillingConstants.EMPTY_STRING;
        String methodName = httpMethod.getName();
        String url = getURL(httpClient, httpMethod);

        do {
            try {
                response = httpClient.executeMethod(httpMethod);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("HTTP response code for the " + methodName + " request to URL: " + url + " is " +
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
                    throw new CloudBillingVendorException(NOT_FOUND_ERROR_MSG);

                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    throw new CloudBillingVendorException(AUTH_ERROR_MSG);

                default:
                    retryCount++;
                    handleDefaultCase(executionCount, response, retryCount, methodName, url);
                    break;
                }
                httpMethod.releaseConnection();
            } catch (CloudBillingVendorException ex) {
                throw ex;
            } catch (Exception ex) {
                retryCount++;
                handleExceptionWithRetry(executionCount, retryCount, methodName, url, ex);
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
     * @throws CloudBillingVendorException
     */
    public static String executeHTTPMethod(HttpClient httpClient, HttpMethodBase httpMethod)
            throws CloudBillingVendorException {

        int response;
        String uri = getURL(httpClient, httpMethod);
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
                throw new CloudBillingVendorException(NOT_FOUND_ERROR_MSG);

            case HttpURLConnection.HTTP_UNAUTHORIZED:
                throw new CloudBillingVendorException(AUTH_ERROR_MSG);

            default:
                throw new CloudBillingVendorException(
                        methodName + " request failed for URI: " + uri + " with HTTP error code : " + response);
            }
        } catch (CloudBillingVendorException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CloudBillingVendorException(methodName + " request failed for URI: " + uri, ex);
        } finally {
            httpMethod.releaseConnection();
        }
    }

    /**
     * Handles the default HTTP 200 response
     *
     * @param httpMethod http method used
     * @return validates the response
     * @throws IOException
     */
    private static String handleCaseHTTPOk(HttpMethodBase httpMethod) throws IOException {
        String result = BillingConstants.EMPTY_STRING;
        if (httpMethod.getResponseBody().length > 0) {
            result = httpMethod.getResponseBodyAsString();
        }
        httpMethod.releaseConnection();
        return result;
    }

    /**
     * Handles the exceptions with retries
     *
     * @param executionCount number of retries configured
     * @param retryCount     current retry
     * @param methodName     HTTP method name
     * @param uri            URI used
     * @param ex             exception thrown
     * @throws CloudBillingVendorException
     */
    private static void handleExceptionWithRetry(int executionCount, int retryCount, String methodName, String uri,
            Exception ex) throws CloudBillingVendorException {
        if (retryCount >= executionCount) {
            throw new CloudBillingVendorException(methodName + " request failed for the maximum no. of attempts(" +
                    retryCount + ") for URL: " + uri, ex);
        } else {
            LOGGER.warn(methodName + " request failed for URL: " + uri + " with exception : " + ex.getMessage()
                    + ". Retry attempt: " + retryCount + "/" + executionCount);
        }
    }

    /**
     * Handles the default case of the switch cases
     *
     * @param executionCount number of retries configured
     * @param response       http response
     * @param retryCount     current retry
     * @param methodName     HTTP method name
     * @param uri            URI used
     * @throws CloudBillingVendorException
     */
    private static void handleDefaultCase(int executionCount, int response, int retryCount, String methodName,
            String uri) throws CloudBillingVendorException {
        if (retryCount >= executionCount) {
            throw new CloudBillingVendorException(
                    methodName + " request failed for the " + retryCount + " attempt for URI:" +
                            " " + uri + " with HTTP error code: " + response);
        } else {
            LOGGER.warn(methodName + " request failed for URI: " + uri + " with HTTP error code: " +
                    response + ". Retry: " + retryCount + "/" + executionCount);
        }
    }

    /**
     * Get URI from the http method
     *
     * @param httpMethod http method
     * @return URI string
     * @throws CloudBillingVendorException
     */
    private static String getURL(HttpClient client, HttpMethodBase httpMethod) throws CloudBillingVendorException {
        String uri;
        try {
            uri = client.getHostConfiguration().getHostURL() + httpMethod.getURI().getURI();
        } catch (URIException e) {
            throw new CloudBillingVendorException("URI exception while getting the URI", e);
        }
        return uri;
    }
}
