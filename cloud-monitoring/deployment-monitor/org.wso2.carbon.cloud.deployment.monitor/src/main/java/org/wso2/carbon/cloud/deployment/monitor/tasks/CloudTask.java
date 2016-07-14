/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.deployment.monitor.api.RunStatus;
import org.wso2.deployment.monitor.core.model.ServerGroup;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Abstract Class for Cloud Tasks
 */
public abstract class CloudTask {

    private static final Logger logger = LoggerFactory.getLogger(CloudTask.class);

    private int counter = 0;

    public RunStatus retryTask(ServerGroup serverGroup, Properties properties, String msg, String reason, Exception e) {
        if (logger.isDebugEnabled()) {
            logger.debug(msg + " Retrying task...");
        }
        counter++;
        if (counter == 3) {
            counter = 0;
            return handleErrors(msg, reason, e);
        } else {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ie) {
                //Exception ignored
            }
            return runTask(serverGroup, properties);
        }
    }

    public RunStatus handleErrors(String msg, String reason, Exception e) {
        if (logger.isDebugEnabled()) {
            logger.debug(msg + " due to : " + reason);
        }
        RunStatus status = new RunStatus();
        Map<String, Object> customReturnDetails;
        status.setSuccess(false);
        status.setMessage(msg);
        customReturnDetails = new HashMap<>();
        customReturnDetails.put("Reason", reason);
        customReturnDetails.put("Exception", e);
        status.setCustomTaskDetails(customReturnDetails);
        return status;
    }

    public abstract RunStatus runTask(ServerGroup serverGroup, Properties properties);

}
