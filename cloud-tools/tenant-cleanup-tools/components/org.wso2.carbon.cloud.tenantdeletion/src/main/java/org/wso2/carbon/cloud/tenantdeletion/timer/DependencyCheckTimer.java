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

import java.util.TimerTask;

/**
 * Represent class for status timer.
 */
public class DependencyCheckTimer extends TimerTask {
    /**
     * Checks dependency flags to start deletion process
     */
    @Override public void run() {
        //Call delete method to check dependencies and start deleting
        DeletionManager.getInstance().delete();
    }
}