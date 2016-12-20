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

package org.wso2.carbon.cloud.billing.core.processor;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.wso2.carbon.cloud.billing.core.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.core.commons.config.model.HttpClientConfig;

/**
 * Abstract billing request processor
 */
public abstract class AbstractBillingRequestProcessor implements BillingRequestProcessor {

    protected static final int DEFAULT_CONNECTION_RETRIES = 5;
    private HttpClient httpClient;

    public AbstractBillingRequestProcessor(HttpClientConfig httpClientConfig) {
        if (httpClientConfig != null) {
            this.httpClient = initHttpClient(httpClientConfig);
        } else {
            HttpClientConfig defaultHttpClientConfig =
                    new HttpClientConfig(BillingConstants.DEFAULT_HOST, BillingConstants.DEFAULT_PORT,
                                         BillingConstants.DEFAULT_MAX_CONNECTION_PER_HOST,
                                         BillingConstants.DEFAULT_MAX_TOTAL_CONNECTION);
            this.httpClient = initHttpClient(defaultHttpClientConfig);
        }
    }

    /**
     * Get http client
     *
     * @return http client
     */
    public HttpClient getHttpClient() {
        return httpClient;
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

}
