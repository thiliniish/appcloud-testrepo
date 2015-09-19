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
package org.wso2.carbon.cloud.testlisteners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.wso2.carbon.automation.engine.testlisteners.TestManagerListener;

public class CloudTestManagerListener extends TestManagerListener {
    private static final Log log = LogFactory.getLog(CloudTestManagerListener.class);

    @Override public void onTestStart(ITestResult iTestResult) {
        super.onTestStart(iTestResult);
    }

    @Override public void onTestSuccess(ITestResult iTestResult) {
        super.onTestSuccess(iTestResult);
    }

    @Override public void onTestFailure(ITestResult iTestResult) {
        super.onTestFailure(iTestResult);
    }

    @Override public void onTestSkipped(ITestResult iTestResult) {
        super.onTestSkipped(iTestResult);
    }

    @Override public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {
        super.onTestFailedButWithinSuccessPercentage(iTestResult);
    }

    @Override public void onStart(ITestContext iTestContext) {
        super.onStart(iTestContext);
    }

    @Override public void onFinish(ITestContext iTestContext) {
        super.onFinish(iTestContext);
    }
}
