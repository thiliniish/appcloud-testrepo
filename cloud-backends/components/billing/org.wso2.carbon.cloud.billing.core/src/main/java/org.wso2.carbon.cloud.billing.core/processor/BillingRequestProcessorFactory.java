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

import org.wso2.carbon.cloud.billing.core.commons.config.BillingConfigManager;
import org.wso2.carbon.cloud.billing.core.commons.config.model.BillingConfig;

/**
 * Represents the Billing Request processor factory which pick the processor type for a given request.
 */
public final class BillingRequestProcessorFactory {

    private static BillingRequestProcessorFactory instance = new BillingRequestProcessorFactory();

    private BillingRequestProcessor dataServiceBillingRequestProcessor;
    private BillingRequestProcessor apimRestAPIRequestProcessor;

    /**
     * Private constructor of the factory
     */
    private BillingRequestProcessorFactory() {
        BillingConfig billingConfig = BillingConfigManager.getBillingConfiguration();

        dataServiceBillingRequestProcessor =
                new DataServiceBillingRequestProcessor(billingConfig.getDataServiceConfig().getHttpClientConfig());
        apimRestAPIRequestProcessor = new APIMRestAPIRequestProcessor(
                billingConfig.getApimRestAPIConfig().getHttpClientConfig());

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
            case APIM_REST:
                return apimRestAPIRequestProcessor;
            default:
                throw new IllegalArgumentException("Unsupported billing request processor type requested");
        }
    }

    /**
     * Enum for Processor types
     */
    public enum ProcessorType {
        DATA_SERVICE, APIM_REST
    }

}
