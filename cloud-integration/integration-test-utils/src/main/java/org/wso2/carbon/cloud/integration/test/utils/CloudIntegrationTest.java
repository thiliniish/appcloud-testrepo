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
package org.wso2.carbon.cloud.integration.test.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Parent class used to initilaize test cases
 */
public class CloudIntegrationTest {
    private static final Log log = LogFactory.getLog(CloudIntegrationTest.class);

    protected static String cloudMgtServerUrl;
    protected static String apiMgtServerUrl;
    protected static String apiMgrPassThroughUrl;
    protected String tenantAdminUserName;
    protected String tenantAdminPassword;
    protected String superAdminUserName;
    protected String superAdminPassword;

    public CloudIntegrationTest() {
        cloudMgtServerUrl = CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.CLOUD_MGT_SERVER_URL);
        tenantAdminUserName = CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.TENANT_ADMIN_USER_NAME);
        tenantAdminPassword = CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.TENANT_ADMIN_PASSWORD);
        superAdminUserName = CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.SUPER_ADMIN_USER_NAME);
        superAdminPassword = CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.SUPER_ADMIN_PASSWORD);
        apiMgtServerUrl = CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.API_MGT_SERVER_URL);
        apiMgrPassThroughUrl = CloudIntegrationTestUtils
		        .getPropertyValue(CloudIntegrationConstants.API_MGR_PASS_THROUGH_SERVER_URL);
    }

    protected void cleanup() {
        log.info("cleanup called");
    }
}
