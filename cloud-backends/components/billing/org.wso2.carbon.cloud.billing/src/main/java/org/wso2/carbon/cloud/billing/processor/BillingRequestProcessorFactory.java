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

import org.wso2.carbon.cloud.billing.commons.config.HttpClientConfig;

/**
 * Represents the Billing Request processor factory which pick the processor type for a given request.
 */
public class BillingRequestProcessorFactory {

    public static BillingRequestProcessor getBillingRequestProcessor(ProcessorType type,
                                                                     HttpClientConfig httpClientConfig) {
        switch (type) {
            case DATA_SERVICE:
                return new DataServiceBillingRequestProcessor(httpClientConfig);
            case ZUORA:
                return new ZuoraBillingRequestProcessor(httpClientConfig);
            default:
                throw new IllegalArgumentException("Unsupported billing request processor type requested");
        }
    }

    public enum ProcessorType {
        DATA_SERVICE, ZUORA
    }

}
