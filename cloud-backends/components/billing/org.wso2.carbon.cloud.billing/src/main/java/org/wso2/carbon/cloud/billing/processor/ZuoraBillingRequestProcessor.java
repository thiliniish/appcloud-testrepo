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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.commons.config.HttpClientConfig;
import org.wso2.carbon.cloud.billing.commons.config.ZuoraConfig;
import org.wso2.carbon.cloud.billing.commons.utils.BillingConfigUtils;
import org.wso2.carbon.cloud.billing.commons.utils.CloudBillingUtils;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.processor.utils.ProcessorUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Represent the request api processor for zuora
 */
public class ZuoraBillingRequestProcessor extends AbstractBillingRequestProcessor {

    private static final Log LOGGER = LogFactory.getLog(ZuoraBillingRequestProcessor.class);

    private static ZuoraConfig zuoraConfig = BillingConfigUtils.getBillingConfiguration().getZuoraConfig();

    public ZuoraBillingRequestProcessor(HttpClientConfig httpClientConfig) {
        super(httpClientConfig);
    }

    /**
     * Overridden method for add zuora enabled ssl protocols
     *
     * @param httpClientConfig http client configuration
     * @return http client
     */
    @Override protected HttpClient initHttpClient(HttpClientConfig httpClientConfig) {
        HttpClient client = super.initHttpClient(httpClientConfig);
        String sslEnabledProtocols = BillingConfigUtils.getBillingConfiguration().getZuoraConfig()
                .getEnabledProtocols();
        Protocol customHttps = CloudBillingUtils.getCustomProtocol(BillingConstants.HTTPS_SCHEME, sslEnabledProtocols);
        client.getHostConfiguration().setHost(httpClientConfig.getHostname(), httpClientConfig.getPort(), customHttps);
        return client;
    }

    /**
     * Zuora upload request
     *
     * @param url        URL
     * @param acceptType Accept header
     * @throws CloudBillingException
     */
    @Override public void doUpload(String url, String acceptType, File file) throws CloudBillingException {
        PostMethod post = new PostMethod(url);
        // default accept response body in JSON
        String acceptTypeHeader = StringUtils.isBlank(acceptType) ?
                BillingConstants.HTTP_TYPE_APPLICATION_JSON :
                acceptType;
        post.addRequestHeader(BillingConstants.HTTP_RESPONSE_TYPE_ACCEPT, acceptTypeHeader);
        addAccessKeyHeaders(post);
        //Initializing the multipart contents used by the usage file uploader functionality.
        Part part;
        try {
            part = new FilePart(BillingConstants.FILE_PART_NAME, file);
        } catch (FileNotFoundException e) {
            throw new CloudBillingException("Error occurred while reading usage data", e);
        }
        RequestEntity uploadReqEntity = new MultipartRequestEntity(new Part[] { part }, new HttpClientParams());
        post.setRequestEntity(uploadReqEntity);
        ProcessorUtils.executeHTTPMethodWithRetry(this.getHttpClient(), post, DEFAULT_CONNECTION_RETRIES);
        //removing the file after successful upload
        boolean isSuccessful = file.delete();
        if (LOGGER.isDebugEnabled()) {
            if (isSuccessful) {
                LOGGER.debug("The file was removed successfully after upload.");
            }
        }
    }

    /**
     * Zuora GET request
     *
     * @param url            URL
     * @param acceptType     Accept header
     * @param nameValuePairs query params
     * @return response
     * @throws CloudBillingException
     */
    @Override public String doGet(String url, String acceptType, NameValuePair[] nameValuePairs)
            throws CloudBillingException {
        GetMethod get = new GetMethod(url);

        addAccessKeyHeaders(get);
        // default accept response body in JSON
        String acceptTypeHeader = StringUtils.isBlank(acceptType) ?
                BillingConstants.HTTP_TYPE_APPLICATION_JSON :
                acceptType;
        get.addRequestHeader(BillingConstants.HTTP_RESPONSE_TYPE_ACCEPT, acceptTypeHeader);

        // for a GET call, chase redirects
        get.addRequestHeader(BillingConstants.HTTP_FOLLOW_REDIRECT, "true");
        if (ArrayUtils.isNotEmpty(nameValuePairs)) {
            get.setQueryString(nameValuePairs);
        }
        return ProcessorUtils.executeHTTPMethodWithRetry(this.getHttpClient(), get, DEFAULT_CONNECTION_RETRIES);
    }

    /**
     * Zuora GET request with custom headers
     *
     * @param url               URL
     * @param acceptType        Accept header
     * @param customHeaders     map of custom headers
     * @param nameValuePairs    query params
     * @return response
     * @throws CloudBillingException
     */
    @Override public String doGet(String url, String acceptType, Map<String, String> customHeaders,
            NameValuePair[] nameValuePairs) throws CloudBillingException {
        throw new UnsupportedOperationException(
                "GET method with custom headers is not supported by Zuora Billing Request Processor");
    }

    /**
     * Zuora post request
     *
     * @param url         URL
     * @param acceptType  Accept header
     * @param jsonPayload json payload
     * @return response
     * @throws CloudBillingException
     */
    @Override public String doPost(String url, String acceptType, String jsonPayload) throws CloudBillingException {

        PostMethod post = new PostMethod(url);
        // default accept response body in JSON
        String acceptTypeHeader = StringUtils.isBlank(acceptType) ?
                BillingConstants.HTTP_TYPE_APPLICATION_JSON :
                acceptType;
        post.addRequestHeader(BillingConstants.HTTP_RESPONSE_TYPE_ACCEPT, acceptTypeHeader);

        addAccessKeyHeaders(post);
        setJsonPayload(post, jsonPayload);
        return ProcessorUtils.executeHTTPMethodWithRetry(this.getHttpClient(), post, DEFAULT_CONNECTION_RETRIES);
    }

    /**
     * POST with name value pairs not supported for zuora
     *
     * @param url          URL
     * @param acceptType   Accept header
     * @param keyValuePair name value pair
     * @return response
     * @throws CloudBillingException
     */
    @Override public String doPost(String url, String acceptType, NameValuePair[] keyValuePair)
            throws CloudBillingException {
        throw new UnsupportedOperationException("This method is not supported by Zuora Billing Request Processor");
    }

    /**
     * PUT with name value pairs not supported for zuora
     *
     * @param url            URL
     * @param acceptType     Accept header
     * @param nameValuePairs name value pair
     * @return response
     * @throws CloudBillingException
     */
    @Override public String doPut(String url, String acceptType, NameValuePair[] nameValuePairs)
            throws CloudBillingException {
        throw new UnsupportedOperationException(
                "PUT method with name value pairs is not supported by Zuora Billing Request Processor");
    }

    /**
     * Zuora post request
     *
     * @param url         URL
     * @param acceptType  Accept header
     * @param jsonPayload json payload
     * @return response
     * @throws CloudBillingException
     */
    @Override public String doPut(String url, String acceptType, String jsonPayload) throws CloudBillingException {

        PutMethod put = new PutMethod(url);
        // default accept response body in JSON
        String acceptTypeHeader = StringUtils.isBlank(acceptType) ?
                BillingConstants.HTTP_TYPE_APPLICATION_JSON :
                acceptType;
        put.addRequestHeader(BillingConstants.HTTP_RESPONSE_TYPE_ACCEPT, acceptTypeHeader);
        addAccessKeyHeaders(put);
        setJsonPayload(put, jsonPayload);
        return ProcessorUtils.executeHTTPMethodWithRetry(this.getHttpClient(), put, DEFAULT_CONNECTION_RETRIES);
    }

    /**
     * Zuora delete request
     *
     * @param url        Request URL to doDelete
     * @param acceptType Accept header which needs to be passed for request header
     * @return response
     * @throws CloudBillingException
     */
    @Override public String doDelete(String url, String acceptType) throws CloudBillingException {

        DeleteMethod delete = new DeleteMethod(url);
        // default accept response body in JSON
        String acceptTypeHeader = StringUtils.isBlank(acceptType) ?
                BillingConstants.HTTP_TYPE_APPLICATION_JSON :
                acceptType;
        delete.addRequestHeader(BillingConstants.HTTP_RESPONSE_TYPE_ACCEPT, acceptTypeHeader);

        addAccessKeyHeaders(delete);
        return ProcessorUtils.executeHTTPMethodWithRetry(this.getHttpClient(), delete, DEFAULT_CONNECTION_RETRIES);
    }

    /**
     * DELETE with name value pairs not supported for zuora
     *
     * @param url            URL
     * @param acceptType     Accept header
     * @param nameValuePairs name value pair
     * @return response
     * @throws CloudBillingException
     */
    @Override public String doDelete(String url, String acceptType, NameValuePair[] nameValuePairs)
            throws CloudBillingException {
        throw new UnsupportedOperationException(
                "DELETE method with name value pairs is not supported by Zuora Billing Request Processor");
    }

    /**
     * Setting the json payload of EntityEnclosingMethods
     *
     * @param method      method POST/PUT
     * @param jsonPayload json payload
     * @throws CloudBillingException
     */
    private void setJsonPayload(EntityEnclosingMethod method, String jsonPayload) throws CloudBillingException {
        RequestEntity requestEntity;
        try {
            if (StringUtils.isNotBlank(jsonPayload)) {
                requestEntity = new StringRequestEntity(jsonPayload, BillingConstants.HTTP_TYPE_APPLICATION_JSON,
                        BillingConstants.ENCODING);
                method.setRequestEntity(requestEntity);
            }
        } catch (UnsupportedEncodingException e) {
            throw new CloudBillingException("Error occurred while encoding json payload" + jsonPayload, e);
        }
    }

    private void addAccessKeyHeaders(HttpMethodBase method) {
        String apiAccessKeyId = zuoraConfig.getUser();
        String apiSecretAccessKey = zuoraConfig.getPassword();
        method.addRequestHeader(BillingConstants.API_ACCESS_KEY_ID, apiAccessKeyId);
        method.addRequestHeader(BillingConstants.API_SECRET_ACCESS_KEY, apiSecretAccessKey);
    }
}
