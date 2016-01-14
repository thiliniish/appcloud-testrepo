/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.cloud.heartbeat.monitor.modules.apimanager;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.wso2.cloud.heartbeat.monitor.utils.TestInfo;
import org.wso2.cloud.heartbeat.monitor.utils.TestStateHandler;
import org.wso2.cloud.heartbeat.monitor.utils.fileutils.CaseConverter;

/**
 * Api invoke test scenario for a cloud setup, implemented in this class
 */
public class ApiInvokeTest implements Job {

    private final String TEST_NAME = "ApiInvokeTest";

    private String hostName;
    private String gatewayURL;
    private String contextPath;
    private String bearer;
    private String serviceName = "ApiManager";
    private TestInfo testInfo;
    private TestStateHandler testStateHandler;
    private String severity;
    private String completeTestName;

    /**
     * Overrides execute() method of Job interface.
     * @param jobExecutionContext "hostName" ,"severity", "contextPath", "authHeader" params passed via JobDataMap.
     * @throws JobExecutionException
     * @throws org.quartz.JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        setCompleteTestName(CaseConverter.splitCamelCase(serviceName) + " - API Invoke Test : ");

        final String[] requestProtocols = { "http", "https" };

        for (String requestProtocol : requestProtocols) {
            gatewayURL = requestProtocol + "://" + hostName + contextPath;

            initApiInvokeTest(gatewayURL, requestProtocol);
        }
    }

    /**
     * Initializes api invoke test
     */
    public void initApiInvokeTest(String gatewayURL, String requestProtocol) {

        DefaultHttpClient httpClient = null;

        try {
            testStateHandler = TestStateHandler.getInstance();
            testInfo = new TestInfo(serviceName, TEST_NAME, hostName, severity);

            httpClient = new DefaultHttpClient();
            HttpGet getRequest = new HttpGet(gatewayURL);
            getRequest.addHeader("accept", "application/json");
            getRequest.addHeader("Authorization", bearer);

            HttpResponse response = httpClient.execute(getRequest);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode >= 200 && statusCode < 300) {
                testStateHandler.onSuccess(testInfo);
            } else {
                testStateHandler.onFailure(testInfo,
                        requestProtocol + " protocol: test failed with " + gatewayURL + ", status code: " + statusCode);
            }

        } catch (ClientProtocolException e) {
            testStateHandler
                    .onFailure(testInfo, requestProtocol + " protocol violation: test failed with " + gatewayURL, e);

        } catch (IOException e) {
            testStateHandler.onFailure(testInfo,
                    requestProtocol + " protocol: error executing the HTTP request, test failed with " + gatewayURL, e);

        } finally {
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
        }
    }

    /**
     * Sets service host
     * @param hostName Service host
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * Sets bearer
     * @param authHeader, bearer value
     */
    public void setAuthHeader(String authHeader) {
        this.bearer = authHeader;
    }

    /**
     * Sets Service name
     * @param serviceName Service name
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Sets display service name
     * @param completeTestName Service name
     */
    public void setCompleteTestName(String completeTestName) {
        this.completeTestName = completeTestName;
    }

    /**
     * sets severity
     * @param severity severity value
     */
    public void setSeverity(String severity) {
        this.severity = severity;
    }

    /**
     * sets context path
     * @param contextPath for URLs
     */
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }
}