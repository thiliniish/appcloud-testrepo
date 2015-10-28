/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.apideletion.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.apideletion.APIDeleter;
import org.wso2.carbon.apimgt.apideletion.util.ApiDeleterConstants;
import org.wso2.carbon.core.ServerStartupHandler;

public class ApiDeletionServerStartListener implements ServerStartupHandler {

    private static final Log log = LogFactory.getLog(ApiDeletionServerStartListener.class);

    /**
     * This method waits until the server starts for the execution
     */
    public void invoke() {
        String tenantFile = System.getProperty(ApiDeleterConstants.TENANT_FILE);
        String napTime = System.getProperty(ApiDeleterConstants.NAP_TIME);
        //checks for null and empty values
        if ((!"".equals(tenantFile)) && (!"".equals(napTime))) {
            log.info("Tenant api deletion for tenant-file located at: " + tenantFile + "will start after " + napTime +
                     "milli-seconds.");
            //A separate thread is created for api deletion.
            Thread t1 = new Thread(new APIDeleter());
            t1.start();
        } else {
            log.info(
                    "Required system properties tenantFile/napTime are not specified for starting api deletion, no apis will be deleted.");
        }
    }
}
