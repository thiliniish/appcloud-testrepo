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
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import javax.xml.xpath.XPathExpressionException;

public class CloudIntegrationTestUtils {
    private static AutomationContext context;
    private static final Log log = LogFactory.getLog(CloudIntegrationTest.class);

    static {
        try {
            context = new AutomationContext(CloudIntegrationConstants.CLOUD_PRODUCT_GROUP,
                                            TestUserMode.SUPER_TENANT_ADMIN);
        } catch (XPathExpressionException e) {
            log.error("Error occurred while initializing automation context", e);
        }
    }

    /**
     * Get value passing xpath
     *
     * @param xPath expression
     * @return value
     * @throws XPathExpressionException
     */
    public static String getPropertyValue(String xPath) throws IllegalArgumentException {
        try {
            return context.getConfigurationValue(xPath);
        } catch (XPathExpressionException e) {
            throw new IllegalArgumentException("Error reading " + xPath, e);
        }
    }
}
