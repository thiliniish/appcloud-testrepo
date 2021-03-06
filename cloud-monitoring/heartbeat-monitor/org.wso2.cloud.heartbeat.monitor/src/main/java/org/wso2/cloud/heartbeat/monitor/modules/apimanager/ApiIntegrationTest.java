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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.cloud.heartbeat.monitor.utils.TestInfo;
import org.wso2.cloud.heartbeat.monitor.utils.TestStateHandler;
import org.wso2.cloud.heartbeat.monitor.utils.fileutils.CaseConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Runs APIM integration test cases through heartbeat
 */
public class ApiIntegrationTest implements Job {

    private static final Log log = LogFactory.getLog(ApiIntegrationTest.class);
    private static final String ARG_SUITE_XML_FILES = "suiteXmlFile";
    private static final String ARG_VERBOSE = "verbose.mode";
    private String serviceName;
    private String hostName;
    private String completeTestName;
    private String severity;
    private TestStateHandler testStateHandler;
    private TestInfo testInfo;

    /**
     * Overrides execute() method of Job interface.
     *
     * @param jobExecutionContext "serviceName", "severity", "hostName" params passed via JobDataMap.
     * @throws JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        setCompleteTestName(CaseConverter.splitCamelCase(serviceName) + " - ApiIntegration Test : ");
        String TEST_NAME = "ApiIntegrationTest";
        log.info("Started test execution :" + TEST_NAME);
        testStateHandler = TestStateHandler.getInstance();
        testInfo = new TestInfo(serviceName, TEST_NAME, hostName, severity);

        try {
            runTestSuite();
            testStateHandler.onSuccess(testInfo);
        } catch (AutomationFrameworkException e) {
            testStateHandler.onFailure(testInfo, "Api Manager Integration Test AutomationFrameworkException", e);
        } catch (Exception e) {
            testStateHandler.onFailure(testInfo, "Api Manager Integration Test exception", e);
        }
    }

    /**
     * Runs test suit.
     *
     * @throws AutomationFrameworkException
     */
    public void runTestSuite() throws AutomationFrameworkException {
        System.setProperty("APIMIntegrationTestSeverity", severity);
        System.setProperty("framework.resource.location", "resources/");
        System.setProperty(ARG_VERBOSE, "0");
        System.setProperty(ARG_SUITE_XML_FILES,
                "/resources/api_testng.xml");
        TestListenerAdapter tla = new TestListenerAdapter();
        String verbose = System.getProperty(ARG_VERBOSE);

        TestNG testNg = new TestNG();
        testNg.addListener(tla);
        testNg.setParallel(Boolean.FALSE.toString());
        testNg.setUseDefaultListeners(true);

        if (verbose == null || verbose.isEmpty()) {
            testNg.setVerbose(2);
        } else {
            testNg.setVerbose(Integer.parseInt(verbose.trim()));
        }

        List<String> files = new ArrayList<String>();

        //testNg file path
        String testNgFiles = System.getProperty(ARG_SUITE_XML_FILES);

        if (testNgFiles == null || testNgFiles.isEmpty()) {
            throw new AutomationFrameworkException("No testNg test suite to execute.");
        }
        files.addAll(Arrays.asList(testNgFiles.split(",")));

        testNg.setTestSuites(files);
        testNg.run();
    }

    /**
     * Sets completeTestName
     *
     * @param completeTestName complete test name
     */
    public void setCompleteTestName(String completeTestName) {
        this.completeTestName = completeTestName;
    }

    /**
     * Sets serviceName
     *
     * @param serviceName service name
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Sets severity
     *
     * @param severity severity
     */
    public void setSeverity(String severity) {
        this.severity = severity;
    }

    /**
     * Sets service host
     *
     * @param hostName service host
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
}