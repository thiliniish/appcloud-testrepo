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

package org.wso2.cloud.heartbeat.monitor.modules.common.tests;

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

import java.io.IOException;

/**
 * Endpoint invoke test scenario for a Cloud setup, implemented in this class
 */
public class EndpointInvokeTest implements Job {

    private final static String TEST_NAME = "EndpointInvokeTest";

    private String hostName;
    private String serviceName = "AppFactory";
    private TestInfo testInfo;
    private TestStateHandler testStateHandler;
    private String severity;
    private String endpointURL;
    private String contextPath;
    private String completeTestName;

    /**
     * Overrides execute() method of Job interface.
     * @param jobExecutionContext "endPointUrl" param passed via JobDataMap.
     * @throws JobExecutionException
     * @throws org.quartz.JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        setCompleteTestName(CaseConverter.splitCamelCase(serviceName) + " - Endpoint Invoke Test : ");

        final String[] requestProtocols = { "http", "https" };

        for (String requestProtocol : requestProtocols) {
            endpointURL = requestProtocol + "://" + hostName + contextPath;

            initEndpointInvokeTest(endpointURL, requestProtocol);
        }
    }

    /**
     * Initializes endpoint invoke test.
     */
    public void initEndpointInvokeTest(String endpointURL, String requestProtocol) {

        DefaultHttpClient httpClient = null;

        try {
            testStateHandler = TestStateHandler.getInstance();

            testInfo = new TestInfo(serviceName, TEST_NAME, hostName, severity);

            httpClient = new DefaultHttpClient();
            HttpGet getRequest = new HttpGet(endpointURL);
            HttpResponse response = httpClient.execute(getRequest);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode >= 200 && statusCode < 300) {
                testStateHandler.onSuccess(testInfo);
            } else {
                testStateHandler.onFailure(testInfo,
                        requestProtocol + " protocol: test failed with " + endpointURL + ", status code: " + statusCode);
            }

        } catch (ClientProtocolException e) {
            testStateHandler
                    .onFailure(testInfo, requestProtocol + " protocol violation: test failed with " + endpointURL, e);

        } catch (IOException e) {
            testStateHandler.onFailure(testInfo,
                    requestProtocol + " protocol: error executing the request, test failed with " + endpointURL, e);
        } finally {
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
        }
    }

    /**
     * Sets host name for server
     * @param hostName of the server
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
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
     * Sets context path for server
     * @param contextPath for the server
     */
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }
}