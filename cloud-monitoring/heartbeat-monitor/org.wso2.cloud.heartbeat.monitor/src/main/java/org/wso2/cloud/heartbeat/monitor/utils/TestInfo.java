/*
 * Copyright 2005-2013 WSO2, Inc. http://www.wso2.org
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.cloud.heartbeat.monitor.utils;

/**
 * This  bean class contains information about the test.
 */
public class TestInfo {

    private String serviceName;
    private String testName;
    private String hostname;
    private String severity;


    /**
     * Construcor.
     * @param serviceName Service Name
     * @param testName  Test Name
     * @param hostname  Host Name
     * @param severity    Severity level
     */
    public TestInfo(String serviceName, String testName, String hostname, String severity) {
        this.serviceName = serviceName;
        this.testName = testName;
        this.hostname = hostname;
        this.severity = severity;
    }

    /**
     * Return the Service Name
     * @return  Service Name
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Return the Test Name
     * @return  Test Name
     */
    public String getTestName() {
        return testName;
    }

    /**
     * Return the Host Name
     * @return  Host Name
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Return the severity
     *
     * @return Host severity
     */
    public String getSeverity() { return severity; }
}
