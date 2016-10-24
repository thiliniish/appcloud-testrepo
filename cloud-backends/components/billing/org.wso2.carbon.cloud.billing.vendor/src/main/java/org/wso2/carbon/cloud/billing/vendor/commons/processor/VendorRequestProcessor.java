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

package org.wso2.carbon.cloud.billing.vendor.commons.processor;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.cloud.billing.core.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.vendor.commons.config.HttpClientConfig;
import org.wso2.carbon.cloud.billing.vendor.commons.config.TrustStore;
import org.wso2.carbon.cloud.billing.vendor.commons.processor.utils.ProcessorUtils;
import org.wso2.carbon.cloud.billing.vendor.commons.utils.BillingVendorConfigUtils;
import org.wso2.carbon.cloud.billing.vendor.stripe.exceptions.CloudBillingVendorException;

/**
 * Vendor Request Processor Class
 */
public class VendorRequestProcessor {

    protected static final int DEFAULT_CONNECTION_RETRIES = 5;
    private static TrustStore trustStore =
            BillingVendorConfigUtils.getBillingVendorConfiguration().getSecurityConfig().getTrustStore();
    private HttpClient httpClient;

    public VendorRequestProcessor(HttpClientConfig httpClientConfig) {
        if (httpClientConfig != null) {
            this.httpClient = initHttpClient(httpClientConfig);
        } else {
            HttpClientConfig defaultHttpClientConfig = new HttpClientConfig();
            defaultHttpClientConfig.setHostname(BillingConstants.DEFAULT_HOST);
            defaultHttpClientConfig.setMaxConnectionsPerHost(BillingConstants.DEFAULT_MAX_CONNECTION_PER_HOST);
            defaultHttpClientConfig.setMaxTotalConnections(BillingConstants.DEFAULT_MAX_TOTAL_CONNECTION);
            this.httpClient = initHttpClient(defaultHttpClientConfig);
        }
    }

    /**
     * initialize http client
     *
     * @param httpClientConfig http client configuration
     * @return Http client
     */
    protected HttpClient initHttpClient(HttpClientConfig httpClientConfig) {
        HostConfiguration hostConfig = new HostConfiguration();
        hostConfig.setHost(httpClientConfig.getHostname(), httpClientConfig.getPort(),
                           Protocol.getProtocol(BillingConstants.HTTPS_SCHEME));

        HttpConnectionManagerParams connParams = new HttpConnectionManagerParams();
        connParams.setMaxConnectionsPerHost(hostConfig, httpClientConfig.getMaxConnectionsPerHost());
        connParams.setMaxTotalConnections(httpClientConfig.getMaxTotalConnections());

        MultiThreadedHttpConnectionManager connManager = new MultiThreadedHttpConnectionManager();
        connManager.setParams(connParams);

        HttpClient client = new HttpClient();
        client.setHostConfiguration(hostConfig);
        client.setHttpConnectionManager(connManager);
        return client;
    }

    /**
     * Get http client
     *
     * @return http client
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }

    public String doGet(String url, String acceptType, NameValuePair[] nameValuePairs)
            throws CloudBillingVendorException {
        return null;
    }

    public String doPost(String url, String acceptType, NameValuePair[] nameValuePairs)
            throws CloudBillingVendorException {
        setTrustStoreParams();
        PostMethod post = new PostMethod(url);
        // default accept response body in JSON
        String acceptTypeHeader =
                StringUtils.isBlank(acceptType) ? BillingConstants.HTTP_TYPE_APPLICATION_JSON : acceptType;
        post.addRequestHeader(BillingConstants.HTTP_RESPONSE_TYPE_ACCEPT, acceptTypeHeader);
        post.addRequestHeader(BillingConstants.HTTP_CONTENT_TYPE, BillingConstants.HTTP_TYPE_APPLICATION_URL_ENCODED);
        if (ArrayUtils.isNotEmpty(nameValuePairs)) {
            post.setRequestBody(nameValuePairs);
        }
        return ProcessorUtils.executeHTTPMethodWithRetry(this.getHttpClient(), post, DEFAULT_CONNECTION_RETRIES);
    }

    private void setTrustStoreParams() {
        System.setProperty(BillingConstants.TRUST_STORE_NAME_PROPERTY, trustStore.getName());
        System.setProperty(BillingConstants.TRUST_STORE_PASSWORD_PROPERTY, trustStore.getPassword());
    }

}
