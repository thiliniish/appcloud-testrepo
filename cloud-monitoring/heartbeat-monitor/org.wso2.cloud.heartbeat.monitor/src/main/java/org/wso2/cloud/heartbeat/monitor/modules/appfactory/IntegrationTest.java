/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.cloud.heartbeat.monitor.modules.appfactory;

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
 * Runs appfactory integration test cases through heartbeat
 */
public class IntegrationTest implements Job {

    private static final Log log = LogFactory.getLog(IntegrationTest.class);
    private static final String ARG_SUITE_XML_FILES = "suiteXmlFile";
    private static final String ARG_VERBOSE = "verbose.mode";
    private String serviceName;
    private String completeTestName;
    private int deploymentWaitTime;
    private String severity;
    private TestStateHandler testStateHandler;
    private TestInfo testInfo;

    @Override public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        setCompleteTestName(CaseConverter.splitCamelCase(serviceName) + " - Integration Test : ");
        String TEST_NAME = "IntegrationTest";
        log.info("Started test execution :" + TEST_NAME);
        try {
            runTestSuite();
            testStateHandler.onSuccess(testInfo);
        } catch (AutomationFrameworkException e) {
            log.info("Integration Test Failed :" + e);
            testStateHandler.onFailure(testInfo, "App Factory Integration Test AutomationFrameworkException", e);
        } catch (Exception e) {
            log.info("Exception Integration Test : " + e);
            testStateHandler.onFailure(testInfo, "App Factory Integration Test exception", e);
        }
    }

    public void runTestSuite() throws AutomationFrameworkException {
        System.setProperty("AFIntegrationTestSeverity", severity);
        System.setProperty("framework.resource.location", "resources/");
        System.setProperty(ARG_VERBOSE, "0");
        System.setProperty(ARG_SUITE_XML_FILES, "resources/testng.xml");
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

    public void setCompleteTestName(String completeTestName) {
        this.completeTestName = completeTestName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public void setDeploymentWaitTime(String deploymentWaitTime) {
        this.deploymentWaitTime = Integer.parseInt(deploymentWaitTime.split("s")[0].replace(" ", "")) * 1000;
    }
}
