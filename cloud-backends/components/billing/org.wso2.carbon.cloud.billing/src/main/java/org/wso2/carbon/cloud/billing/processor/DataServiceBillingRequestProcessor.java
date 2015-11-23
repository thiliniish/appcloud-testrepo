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

package org.wso2.carbon.cloud.billing.processor;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.commons.config.DataServiceConfig;
import org.wso2.carbon.cloud.billing.commons.config.HttpClientConfig;
import org.wso2.carbon.cloud.billing.commons.config.SSOConfig;
import org.wso2.carbon.cloud.billing.commons.utils.BillingConfigUtils;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.processor.utils.ProcessorUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.UnsupportedEncodingException;

/**
 * Represent the request api processor for data services
 */
public class DataServiceBillingRequestProcessor extends AbstractBillingRequestProcessor {

    private static final Log LOGGER = LogFactory.getLog(DataServiceBillingRequestProcessor.class);
    private static DataServiceConfig dataServiceConfig = BillingConfigUtils.getBillingConfiguration().getDSConfig();
    private static SSOConfig ssoConfig = BillingConfigUtils.getBillingConfiguration().getSSOConfig();

    private static String basicAuthHeader;

    static {
        try {
            /**
             * Initializing Basic Auth header out of the username and the
             * password extracted out form the billing
             * configuration.
             */
            basicAuthHeader = initBasicAuthHeader();
        } catch (Exception e) {
            LOGGER.error("Error while initializing upload request entity", e);
        }
    }

    public DataServiceBillingRequestProcessor(HttpClientConfig httpClientConfig) {
        super(httpClientConfig);
    }

    /**
     * Initialize the billing request basin auth header
     *
     * @return base 64 encoded auth header
     * @throws CloudBillingException
     */
    private static String initBasicAuthHeader() throws CloudBillingException {
        String userName = dataServiceConfig.getUser();
        String password = dataServiceConfig.getPassword();
        try {
            return "Basic " +
                   DatatypeConverter.printBase64Binary((userName + ":" + password).getBytes(BillingConstants.ENCODING));
        } catch (UnsupportedEncodingException e) {
            throw new CloudBillingException("Error occurred while initializing encoding basic auth header", e);
        }
    }

    /**
     * Get request
     *
     * @param url            URL
     * @param nameValuePairs query params
     * @return GET request response
     * @throws CloudBillingException
     */
    public String doGet(String url, NameValuePair[] nameValuePairs) throws CloudBillingException {
        setTrustStoreParams();
        GetMethod get = new GetMethod(url);
        if (basicAuthHeader == null || basicAuthHeader.isEmpty()) {
            throw new IllegalStateException("Data Service Billing Processor is not initialized properly");
        }
        get.addRequestHeader(BillingConstants.HTTP_REQ_HEADER_AUTHZ, basicAuthHeader);
        get.addRequestHeader(BillingConstants.HTTP_FOLLOW_REDIRECT, "true");
        if (!ArrayUtils.isEmpty(nameValuePairs)) {
            get.setQueryString(nameValuePairs);
        }
        return ProcessorUtils.executeHTTPMethodWithRetry(this.getHttpClient(), get, DEFAULT_CONNECTION_RETRIES);
    }

    /**
     * Upload not supported
     *
     * @throws CloudBillingException
     */
    @Override public void doUpload(File file) throws CloudBillingException {
        throw new UnsupportedOperationException("This method is not supported for Data Service Billing request "
                                                + "processor");
    }

    /**
     * Data service POST request
     *
     * @param url         URL
     * @param jsonPayload payload
     * @return response
     * @throws CloudBillingException
     */
    @Override
    public String doPost(String url, String jsonPayload) throws CloudBillingException {
        throw new UnsupportedOperationException("This method is not supported by Data Service Billing Request " +
                                                "Processor");
    }

    /**
     * Data service POST request
     *
     * @param url          URL
     * @param nameValuePairs name value pair
     * @return response
     * @throws CloudBillingException
     */
    @Override
    public String doPost(String url, NameValuePair[] nameValuePairs) throws CloudBillingException {
        setTrustStoreParams();
        PostMethod post = new PostMethod(url);
        // indicate accept response body in JSON
        post.addRequestHeader(BillingConstants.HTTP_RESPONSE_TYPE_ACCEPT, BillingConstants.HTTP_RESPONSE_TYPE_JSON);
        post.setRequestBody(nameValuePairs);
        return ProcessorUtils.executeHTTPMethodWithRetry(this.getHttpClient(), post, DEFAULT_CONNECTION_RETRIES);
    }

    /**
     * Data service PUT request
     *
     * @param url
     * @param jsonPayLoad
     * @return
     * @throws CloudBillingException
     */
    @Override public String doPut(String url, String jsonPayLoad) throws CloudBillingException {
        setTrustStoreParams();
        PutMethod put = new PutMethod(url);
        put.addRequestHeader(BillingConstants.HTTP_RESPONSE_TYPE_ACCEPT, BillingConstants.HTTP_RESPONSE_TYPE_JSON);
        try {
            put.setRequestEntity(new StringRequestEntity(jsonPayLoad, BillingConstants.HTTP_RESPONSE_TYPE_JSON,
                    BillingConstants.ENCODING));
        } catch (UnsupportedEncodingException e) {
            throw new CloudBillingException("Error occurred while initializing encoding request payload", e);
        }
        return ProcessorUtils.executeHTTPMethodWithRetry(this.getHttpClient(), put, DEFAULT_CONNECTION_RETRIES);
    }

    private void setTrustStoreParams() {
        System.setProperty(BillingConstants.TRUST_STORE_NAME_PROPERTY, ssoConfig.getTrustStorePath());
        System.setProperty(BillingConstants.TRUST_STORE_PASSWORD_PROPERTY, ssoConfig.getTrustStorePassword());
    }
}
