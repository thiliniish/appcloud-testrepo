/*
 * Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.cloud.deployment.monitor.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.cloud.deployment.monitor.utils.CloudMonitoringConstants;
import org.wso2.deployment.monitor.api.RunStatus;
import org.wso2.deployment.monitor.core.model.ServerGroup;

import java.util.Properties;

/**
 * Abstract Class for Cloud Tasks
 */
public abstract class AbstractRetryingTask {

    private static final Logger logger = LoggerFactory.getLogger(AbstractRetryingTask.class);

    private int counter = 0;

    public abstract RunStatus runTask(ServerGroup serverGroup, Properties properties);

    public RunStatus retryTask(ServerGroup serverGroup, Properties properties, String msg, RunStatus runStatus) {
        logger.warn(msg + " Retrying task...");
        counter++;
        if (counter == 3) {
            counter = 0;
            return runStatus;
        } else {
            try {
                Thread.sleep(CloudMonitoringConstants.THREAD_SLEEP_TIME);
            } catch (InterruptedException ie) {
                //Exception ignored
            }
            return runTask(serverGroup, properties);
        }
    }

}
