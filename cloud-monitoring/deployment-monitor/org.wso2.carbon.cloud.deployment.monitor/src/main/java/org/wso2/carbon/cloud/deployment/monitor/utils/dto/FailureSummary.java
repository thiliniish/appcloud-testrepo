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
 * FailureSummary bean
 */
public class FailureSummary {

    private String server;
    private String taskName;
    private int startID;
    private int endID;
    private Date date;
    private long startTime;
    private long endTime;
    private int downTime;

    public FailureSummary(String server, String taskName, int startID, Date date, long startTime) {
        this.server = server;
        this.taskName = taskName;
        this.startID = startID;
        this.date = new Date(date.getTime());
        this.startTime = startTime;
    }

    public String getServer() {
        return server;
    }

    public String getTaskName() {
        return taskName;
    }

    public int getStartID() {
        return startID;
    }

    public int getEndID() {
        return endID;
    }

    public void setEndID(int endID) {
        this.endID = endID;
    }

    public Date getDate() {
        return new Date(this.date.getTime());
    }

    public int getDownTime() {
        return downTime;
    }

    public void setDownTime(int downTime) {
        this.downTime = downTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getEndTime() {
        return endTime;
    }
}
