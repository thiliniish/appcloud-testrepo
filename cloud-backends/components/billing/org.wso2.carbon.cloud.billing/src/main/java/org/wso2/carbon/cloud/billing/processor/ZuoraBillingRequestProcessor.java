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
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.commons.config.HttpClientConfig;
import org.wso2.carbon.cloud.billing.commons.config.ZuoraConfig;
import org.wso2.carbon.cloud.billing.commons.utils.BillingConfigUtils;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.processor.utils.ProcessorUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

/**
 * Represent the request api processor for zuora
 */
public class ZuoraBillingRequestProcessor extends AbstractBillingRequestProcessor {

    private static String uploadURL = BillingConfigUtils.getBillingConfiguration().getZuoraConfig().getServiceUrl()
                                      + BillingConstants.ZUORA_REST_API_URI_USAGE;
    private static ZuoraConfig zuoraConfig = BillingConfigUtils.getBillingConfiguration().getZuoraConfig();

    public ZuoraBillingRequestProcessor(HttpClientConfig httpClientConfig) {
        super(httpClientConfig);
    }

    /**
     * Zuora upload request
     *
     * @throws CloudBillingException
     */
    @Override
    public void doUpload() throws CloudBillingException {
        PostMethod post = new PostMethod(uploadURL);
        // indicate accept response body in JSON
        post.addRequestHeader(BillingConstants.HTTP_RESPONSE_TYPE_ACCEPT, BillingConstants.HTTP_RESPONSE_TYPE_JSON);
        String apiAccessKeyId = zuoraConfig.getUser();
        String apiSecretAccessKey = zuoraConfig.getPassword();
        post.addRequestHeader(BillingConstants.API_ACCESS_KEY_ID, apiAccessKeyId);
        post.addRequestHeader(BillingConstants.API_SECRET_ACCESS_KEY, apiSecretAccessKey);

        /**
         * Initializing the multipart contents used by the usage file uploader functionality.
         */
        File file = new File(zuoraConfig.getUsageConfig().getUsageUploadFileLocation());
        Part part;
        try {
            part = new FilePart(BillingConstants.FILE_PART_NAME, file);
        } catch (FileNotFoundException e) {
            throw new CloudBillingException("Error occurred while reading usage data", e);
        }
        RequestEntity uploadReqEntity = new MultipartRequestEntity(new Part[]{part}, new HttpClientParams());
        post.setRequestEntity(uploadReqEntity);
        ProcessorUtils.executeHTTPMethodWithRetry(this.getHttpClient(), post, DEFAULT_CONNECTION_RETRIES);
    }

    /**
     * Zuora GET request
     *
     * @param url URL
     * @param nameValuePairs query params
     * @return response
     * @throws CloudBillingException
     */
    @Override
    public String doGet(String url, NameValuePair[] nameValuePairs) throws CloudBillingException {
        GetMethod get = new GetMethod(url);

        String apiAccessKeyId = zuoraConfig.getUser();
        String apiSecretAccessKey = zuoraConfig.getPassword();
        get.addRequestHeader(BillingConstants.API_ACCESS_KEY_ID, apiAccessKeyId);
        get.addRequestHeader(BillingConstants.API_SECRET_ACCESS_KEY, apiSecretAccessKey);

        // indicate accept response body in JSON
        get.addRequestHeader(BillingConstants.HTTP_RESPONSE_TYPE_ACCEPT, BillingConstants.HTTP_RESPONSE_TYPE_JSON);
        // for a GET call, chase redirects
        get.addRequestHeader(BillingConstants.HTTP_FOLLOW_REDIRECT, "true");
        if (!ArrayUtils.isEmpty(nameValuePairs)) {
            get.setQueryString(nameValuePairs);
        }
        return ProcessorUtils.executeHTTPMethodWithRetry(this.getHttpClient(), get, DEFAULT_CONNECTION_RETRIES);
    }

    /**
     * Zuora post request
     *
     * @param url         URL
     * @param jsonPayload json payload
     * @return response
     * @throws CloudBillingException
     */
    @Override
    public String doPost(String url, String jsonPayload) throws CloudBillingException {

        PostMethod post = new PostMethod(url);
        // indicate accept response body in JSON
        post.addRequestHeader(BillingConstants.HTTP_RESPONSE_TYPE_ACCEPT, BillingConstants.HTTP_RESPONSE_TYPE_JSON);

        String apiAccessKeyId = zuoraConfig.getUser();
        String apiSecretAccessKey = zuoraConfig.getPassword();
        post.addRequestHeader(BillingConstants.API_ACCESS_KEY_ID, apiAccessKeyId);
        post.addRequestHeader(BillingConstants.API_SECRET_ACCESS_KEY, apiSecretAccessKey);

        RequestEntity requestEntity;
        try {
            requestEntity = new StringRequestEntity(jsonPayload, BillingConstants.HTTP_RESPONSE_TYPE_JSON,
                                                    BillingConstants.ENCODING);
            post.setRequestEntity(requestEntity);
        } catch (UnsupportedEncodingException e) {
            throw new CloudBillingException("Error occurred while encoding json payload" + jsonPayload, e);
        }
        return ProcessorUtils.executeHTTPMethodWithRetry(this.getHttpClient(), post, DEFAULT_CONNECTION_RETRIES);
    }

    /**
     * POST with name value pairs not supported for zuora
     *
     * @param url          URL
     * @param keyValuePair name value pair
     * @return response
     * @throws CloudBillingException
     */
    @Override
    public String doPost(String url, NameValuePair[] keyValuePair) throws CloudBillingException {
        throw new UnsupportedOperationException("This method is not supported by Zuora Billing Request Processor");
    }
}