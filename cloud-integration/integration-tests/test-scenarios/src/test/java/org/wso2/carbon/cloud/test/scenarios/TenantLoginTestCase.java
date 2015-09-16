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
package org.wso2.carbon.cloud.test.scenarios;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.cloud.integration.test.utils.CloudIntegrationTest;
import org.wso2.carbon.cloud.integration.test.utils.restclients.JaggeryAppAuthenticatorClient;

public class TenantLoginTestCase extends CloudIntegrationTest {
    private static final Log log = LogFactory.getLog(TenantLoginTestCase.class);
    private boolean errorsReported;
    private JaggeryAppAuthenticatorClient authenticatorClient;
    private String hostName;
    private String testUser;
    private String testUserPassword;
    private boolean loginStatus = false;


    @BeforeClass(alwaysRun = true) public void deployService() throws Exception {
    }

    @Test() public void loginTest() throws Exception {
        log.info("started running test case");
        errorsReported = false;
        try {
            authenticatorClient = new JaggeryAppAuthenticatorClient("https://cloudmgt.cloud.wso2.com", "cloudmgt");
            loginStatus = authenticatorClient.login("malithm@wso2.com", "Baki:123#");


        } catch (Exception ee) {

        }
        Assert.assertEquals(loginStatus,true);

    }

    @AfterClass(alwaysRun = true) public void unDeployService() throws Exception {
        // undeploying deployed artifact
        super.cleanup();
    }
}