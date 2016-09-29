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
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.commons.config.BillingConfig;
import org.wso2.carbon.cloud.billing.commons.config.HttpClientConfig;
import org.wso2.carbon.cloud.billing.commons.config.ZuoraConfig;
import org.wso2.carbon.cloud.billing.commons.utils.BillingConfigUtils;
import org.wso2.carbon.cloud.billing.commons.utils.CloudBillingUtils;

/**
 * Represents the Billing Request processor factory which pick the processor type for a given request.
 */
public final class BillingRequestProcessorFactory {

    private static BillingRequestProcessorFactory instance = new BillingRequestProcessorFactory();

    private BillingRequestProcessor zuoraBillingRequestProcessor;
    private BillingRequestProcessor zuoraBillingRequestProcessorRest;
    private BillingRequestProcessor dataServiceBillingRequestProcessor;

    /**
     * Private constructor of the factory
     */
    private BillingRequestProcessorFactory() {
        BillingConfig billingConfig = BillingConfigUtils.getBillingConfiguration();
        ZuoraConfig zuoraConfig = billingConfig.getZuoraConfig();
        HttpClientConfig zuoraHttpClientConfig = zuoraConfig.getHttpClientConfig();

        zuoraBillingRequestProcessor = new ZuoraBillingRequestProcessor(zuoraHttpClientConfig);
        dataServiceBillingRequestProcessor =
                new DataServiceBillingRequestProcessor(billingConfig.getDSConfig().getHttpClientConfig());

        //if the rest api require another host name. specified in billing.xml
        if (StringUtils.isNotBlank(zuoraConfig.getServiceUrlHost())) {
            zuoraBillingRequestProcessorRest =
                    new ZuoraBillingRequestProcessor(billingConfig.getZuoraConfig().getHttpClientConfig());
            initializeWithHostNameForZouraRest(zuoraConfig, zuoraHttpClientConfig);
        } else {
            this.zuoraBillingRequestProcessorRest = this.zuoraBillingRequestProcessor;
        }
    }

    /**
     * Add specific host with zuora enabled ssl protocols
     *
     * @param zuoraConfig zuora configuration
     * @param zuoraHttpClientConfig zuora http client configuration
     */
    private void initializeWithHostNameForZouraRest(ZuoraConfig zuoraConfig, HttpClientConfig zuoraHttpClientConfig) {
        HttpClient client = ((ZuoraBillingRequestProcessor) zuoraBillingRequestProcessorRest).getHttpClient();

        String sslEnabledProtocols = zuoraConfig.getEnabledProtocols();
        Protocol customProtocol = CloudBillingUtils
                .getCustomProtocol(BillingConstants.HTTPS_SCHEME, sslEnabledProtocols);

        client.getHostConfiguration()
                .setHost(zuoraConfig.getServiceUrlHost(), zuoraHttpClientConfig.getPort(), customProtocol);
    }

    /**
     * Get Factory instance
     *
     * @return BillingRequestProcessorFactory
     */
    public static BillingRequestProcessorFactory getInstance() {
        return instance;
    }

    /**
     * Get billing request processor.
     *
     * @param type enum ProcessorType
     * @return BillingRequestProcessor
     */
    public BillingRequestProcessor getBillingRequestProcessor(ProcessorType type) {
        switch (type) {
            case DATA_SERVICE:
                return dataServiceBillingRequestProcessor;
            case ZUORA:
                return zuoraBillingRequestProcessorRest;
            case ZUORA_RSA:
                return zuoraBillingRequestProcessor;
            default:
                throw new IllegalArgumentException("Unsupported billing request processor type requested");
        }
    }

    /**
     * Enum for Processor types
     */
    public enum ProcessorType {
        DATA_SERVICE, ZUORA, ZUORA_RSA
    }

}
