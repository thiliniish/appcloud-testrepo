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
import org.wso2.carbon.cloud.billing.core.exceptions.CloudBillingException;

import java.io.File;
import java.util.Map;

/**
 * Represents the API invocation methods which are needed for cloud billing
 * platform.
 */
public interface BillingRequestProcessor {

    String doGet(String url, String acceptType, NameValuePair[] nameValuePairs) throws CloudBillingException;

    String doGet(String url, String acceptType, Map<String, String> customHeaders, NameValuePair[] nameValuePairs)
            throws CloudBillingException;

    void doUpload(String url, String acceptType, File file) throws CloudBillingException;

    String doPost(String url, String acceptType, String jsonPayload) throws CloudBillingException;

    String doPost(String url, String acceptType, NameValuePair[] keyValuePair) throws CloudBillingException;

    String doPut(String url, String acceptType, String jsonPayload) throws CloudBillingException;

    String doPut(String url, String acceptType, NameValuePair[] nameValuePairs) throws CloudBillingException;

    String doDelete(String url, String acceptType) throws CloudBillingException;

    String doDelete(String url, String acceptType, NameValuePair[] nameValuePairs) throws CloudBillingException;
}
