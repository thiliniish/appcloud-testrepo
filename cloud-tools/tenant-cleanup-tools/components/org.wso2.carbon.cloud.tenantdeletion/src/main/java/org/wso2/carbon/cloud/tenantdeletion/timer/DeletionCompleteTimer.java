/*
 *
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.wso2.carbon.cloud.tenantdeletion.timer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.tenantdeletion.DeletionManager;
import org.wso2.carbon.cloud.tenantdeletion.constants.DeletionConstants;
import org.wso2.carbon.cloud.tenantdeletion.utils.DataAccessManager;

import java.util.TimerTask;

/**
 * Timer task to check whether the deletion is completed to reset the startFlagTimer
 */
public class DeletionCompleteTimer extends TimerTask {
    private static final Log LOG = LogFactory.getLog(DeletionCompleteTimer.class);

    @Override public void run() {
        boolean isDeletionCompleted = DataAccessManager.getInstance().getDeletionStatus(DeletionConstants.START);
        if (!isDeletionCompleted) {
            DeletionManager.getInstance().resetStartFlagTimer();
        }
    }
}
