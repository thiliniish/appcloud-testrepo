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

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Date;

/**
 * {@link org.wso2.carbon.cloud.deployment.monitor.utils.dto.DailyServiceStatus}
 */
public class DailyServiceStatus {
    private String service;
    private Date date;
    private int serviceDowntime;
    private State state;
    private double uptimePercentage;

    /**
     * Enum representing Live State
     */
    public enum State {
        UP, DISRUPTIONS, DOWN, NA
    }

    public DailyServiceStatus(String service, Date date, int serviceDowntime, State state) {
        this.service = service;
        this.date = new Date(date.getTime());
        this.serviceDowntime = serviceDowntime;
        this.state = state;

        //Calculating Uptime percentage
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.DOWN);
        double serviceUptimePerDay = 86400 - serviceDowntime;
        String uptimeVal = df.format(serviceUptimePerDay * 100 / 86400);
        this.uptimePercentage = Double.valueOf(uptimeVal);

    }

    public String getService() {
        return service;
    }

    public Date getDate() {
        return new Date(date.getTime());
    }

    public int getServiceDowntime() {
        return serviceDowntime;
    }

    public String getDailyServiceStatus() {
        return state.name();
    }

    public double getUptimePercentage() {
        return uptimePercentage;
    }
}

