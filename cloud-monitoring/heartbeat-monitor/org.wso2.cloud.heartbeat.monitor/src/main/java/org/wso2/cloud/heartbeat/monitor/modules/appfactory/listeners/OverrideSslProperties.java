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
package org.wso2.cloud.heartbeat.monitor.modules.appfactory.listeners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.extensions.TestNGExtensionExecutor;
import org.wso2.carbon.automation.engine.frameworkutils.TestFrameworkUtils;
import org.wso2.carbon.automation.engine.testlisteners.TestExecutionListener;
import org.wso2.cloud.heartbeat.monitor.utils.PlatformUtils;

public class OverrideSslProperties extends TestExecutionListener {
    private static final Log log = LogFactory.getLog(TestExecutionListener.class);

    public OverrideSslProperties() {
    }

    public void onExecutionStart() {
        try {
            AutomationContext e = new AutomationContext();
            System.setProperty("framework.execution.mode", e.getConfigurationValue("//executionEnvironment/text()"));
            TestFrameworkUtils.setKeyStoreProperties(e);
            TestNGExtensionExecutor testNGExtensionExecutor = new TestNGExtensionExecutor();
            testNGExtensionExecutor.initiate();
            TestNGExtensionExecutor.executeExtensible("platformExecutionManager", "onExecutionStart", false);
            log.info("Inside Test Execution Listener - On Execution");
        } catch (Exception var3) {
            this.handleException("Error on initializing test environment ", var3);
        }
        PlatformUtils.setKeyStoreProperties();
        PlatformUtils.setTrustStoreParams();
        PlatformUtils.setKeyStoreParams();
    }

    public void onExecutionFinish() {
        try {
            log.info("Inside Test Execution Listener - On Finish");
            TestNGExtensionExecutor.executeExtensible("platformExecutionManager", "onExecutionFinish", true);
        } catch (Exception var2) {
            this.handleException("Error while tear down the execution environment ", var2);
        }

    }

    private void handleException(String msg, Exception e) {
        log.error("Execution error occurred in TestExecutionListener:-" + e.getStackTrace());
        throw new RuntimeException(msg, e);
    }
}
