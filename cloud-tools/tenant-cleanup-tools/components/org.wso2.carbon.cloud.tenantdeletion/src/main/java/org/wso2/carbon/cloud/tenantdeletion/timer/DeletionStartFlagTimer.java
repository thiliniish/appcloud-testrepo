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

package org.wso2.carbon.cloud.tenantdeletion.timer;

import org.wso2.carbon.cloud.tenantdeletion.DeletionManager;
import org.wso2.carbon.cloud.tenantdeletion.constants.DeletionConstants;
import org.wso2.carbon.cloud.tenantdeletion.utils.DataAccessManager;

import java.util.TimerTask;

/**
 * Represent Timer to check the start deletion flag.
 */
public class DeletionStartFlagTimer extends TimerTask {
    /**
     * Checks database Start flag to start deletion process
     */
    @Override public void run() {
        //Checks the tenant deletion start flag in the database
        boolean startAvailable = DataAccessManager.getInstance().getDeletionStatus(DeletionConstants.START);
        if (startAvailable) {
            //If the start flag has been raised then, the server is going to find whether it is the coordinator in
            // the cluster.
            DeletionManager.getInstance().isCoordinator();
        }
    }
}

