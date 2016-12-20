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

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.cloud.billing.core.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.core.commons.config.BillingConfigManager;
import org.wso2.carbon.cloud.billing.core.commons.config.model.HttpClientConfig;
import org.wso2.carbon.cloud.billing.core.commons.config.model.TrustStore;
import org.wso2.carbon.cloud.billing.core.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.core.processor.utils.ProcessorUtils;

import java.io.File;
import java.util.Map;

/**
 * Represent the request api processor for api manager REST API
 */
public class APIMRestAPIRequestProcessor extends AbstractBillingRequestProcessor {

    private static TrustStore trustStore =
            BillingConfigManager.getBillingConfiguration().getSecurityConfig().getTrustStore();

    public APIMRestAPIRequestProcessor(HttpClientConfig httpClientConfig) {
        super(httpClientConfig);
    }

    @Override public String doGet(String url, String acceptType, NameValuePair[] nameValuePairs)
            throws CloudBillingException {
        throw new UnsupportedOperationException("This method is not supported by APIM RestAPI Request Processor");
    }

    @Override public String doGet(String url, String acceptType, Map<String, String> customHeaders,
                                  NameValuePair[] nameValuePairs) throws CloudBillingException {
        setTrustStoreParams();
        GetMethod get = new GetMethod(url);
        // default accept response body in XML
        String acceptTypeHeader =
                StringUtils.isBlank(acceptType) ? BillingConstants.HTTP_TYPE_APPLICATION_JSON : acceptType;
        get.addRequestHeader(BillingConstants.HTTP_RESPONSE_TYPE_ACCEPT, acceptTypeHeader);
        for (Map.Entry<String, String> entry : customHeaders.entrySet()) {
            get.addRequestHeader(entry.getKey(), entry.getValue());
        }
        get.addRequestHeader(BillingConstants.HTTP_FOLLOW_REDIRECT, "true");
        if (ArrayUtils.isNotEmpty(nameValuePairs)) {
            get.setQueryString(nameValuePairs);
        }
        return ProcessorUtils.executeHTTPMethodWithRetry(this.getHttpClient(), get, DEFAULT_CONNECTION_RETRIES);
    }

    @Override public void doUpload(String url, String acceptType, File file) throws CloudBillingException {
        throw new UnsupportedOperationException("This method is not supported by APIM RestAPI Request Processor");
    }

    @Override public String doPost(String url, String acceptType, String jsonPayload) throws CloudBillingException {
        throw new UnsupportedOperationException("This method is not supported by APIM RestAPI Request Processor");
    }

    @Override public String doPost(String url, String acceptType, NameValuePair[] keyValuePair)
            throws CloudBillingException {
        throw new UnsupportedOperationException("This method is not supported by APIM RestAPI Request Processor");
    }

    @Override public String doPut(String url, String acceptType, String jsonPayload) throws CloudBillingException {
        throw new UnsupportedOperationException("This method is not supported by APIM RestAPI Request Processor");
    }

    @Override public String doPut(String url, String acceptType, NameValuePair[] nameValuePairs) {
        throw new UnsupportedOperationException("This method is not supported by APIM RestAPI Request Processor");
    }

    @Override public String doDelete(String url, String acceptType) throws CloudBillingException {
        throw new UnsupportedOperationException("This method is not supported by APIM RestAPI Request Processor");
    }

    @Override public String doDelete(String url, String acceptType, NameValuePair[] nameValuePairs)
            throws CloudBillingException {
        throw new UnsupportedOperationException("This method is not supported by APIM RestAPI Request Processor");
    }

    private void setTrustStoreParams() {
        System.setProperty(BillingConstants.TRUST_STORE_NAME_PROPERTY, trustStore.getName());
        System.setProperty(BillingConstants.TRUST_STORE_PASSWORD_PROPERTY, trustStore.getPassword());
    }
}
