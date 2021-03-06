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

package org.wso2.carbon.cloud.deployment.monitor.utils.dto;

/**
 * SuccessRecord bean
 */
public class SuccessRecord {

    private String taskName;
    private String server;
    private long timestamp;

    public SuccessRecord(String taskName, String server, long timestamp) {
        this.taskName = taskName;
        this.server = server;
        this.timestamp = timestamp;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getServer() {
        return server;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
