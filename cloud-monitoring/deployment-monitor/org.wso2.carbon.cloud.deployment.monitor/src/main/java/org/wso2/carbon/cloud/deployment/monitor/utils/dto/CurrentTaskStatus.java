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

import java.util.Date;

/**
 * CurrentTaskStatus bean
 */
public class CurrentTaskStatus {

    private String server;
    private String taskName;
    private State state;
    private Date lastUpdated;

    /**
     * Enum representing Live State
     */
    public enum State {
        UP, DOWN, MAINTENANCE, NA
    }

    public CurrentTaskStatus(String server, String taskName, State state, Date lastUpdated) {
        this.server = server;
        this.taskName = taskName;
        this.state = state;
        this.lastUpdated = new Date(lastUpdated.getTime());
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public CurrentTaskStatus.State getState() {
        return this.state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Date getLastUpdated() {
        return new Date(lastUpdated.getTime());
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = new Date(lastUpdated.getTime());
    }
}
