/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.cloud.rolemgt.tool.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.rolemgt.tool.RoleManager;
import org.wso2.carbon.core.ServerStartupHandler;

/**
 * Represents the listener that identify the server startup
 */
public class RoleManagerServerStartListener implements ServerStartupHandler {

    private static final Log log = LogFactory.getLog(RoleManagerServerStartListener.class);

    /**
     * This method waits until the server starts for execution
     */
    @Override public void invoke() {
        if (log.isDebugEnabled()) {
            log.debug("RoleManagerServerStartListener is activated");
        }
        Thread t1 = new Thread(new RoleManager());
        t1.start();
    }
}