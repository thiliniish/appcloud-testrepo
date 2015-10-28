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
package org.wso2.carbon.appfactory.appdeletion.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.appdeletion.AppDeleter;
import org.wso2.carbon.appfactory.appdeletion.util.AppDeleterConstants;
import org.wso2.carbon.core.ServerStartupHandler;
import org.wso2.carbon.user.api.UserStoreException;

public class AppDeletionServerStartListener implements ServerStartupHandler {

	private static final Log log = LogFactory.getLog(AppDeletionServerStartListener.class);

	/**
	 * This method waits until the server starts for the execution
	 */
	public void invoke() {

		String tenantFile = System.getProperty(AppDeleterConstants.TENANT_FILE);
		String napTime = System.getProperty(AppDeleterConstants.NAP_TIME);
		//checks for null and empty values
		if (!("".equals(tenantFile)) && !("".equals(napTime))) {
            log.info("Tenant app deletion for tenant list located at: " + tenantFile + "will start after " + napTime
                    + "milli-seconds.");
            //A separate thread is created for app deletion.
			Thread t1 = new Thread(new AppDeleter());
			t1.start();
		} else {
            log.warn(
                    "Required system properties tenantFile/napTime are not specified for starting api deletion, no apis will be deleted");
        }
	}
}
