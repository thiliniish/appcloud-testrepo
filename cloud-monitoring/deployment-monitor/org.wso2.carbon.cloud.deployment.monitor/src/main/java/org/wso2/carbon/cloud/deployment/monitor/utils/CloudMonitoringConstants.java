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

package org.wso2.carbon.cloud.deployment.monitor.utils;

/**
 * Constants for Cloud Monitor
 */
public class CloudMonitoringConstants {

    public static final String SIMPLE_DATE_FORMAT_WITH_TIME_ZONE = "yyyy-MM-dd hh:mm:ss a z";

    public static final String TRIGGER_TYPE = "triggerType";

    public static final String TRIGGER = "trigger";

    /**
     * For {@link org.wso2.carbon.cloud.deployment.monitor.service.CloudMonitorService}
     */
    public static final String SCHEDULE = "schedule";

    public static final String UNSCHEDULE = "unschedule";

    public static final String PAUSE = "pause";

    public static final String RESUME = "resume";

    /**
     * For Statuses
     */
    public static final String DOWN = "DOWN";

    public static final String MAINTENANCE = "MAINTENANCE";

    public static final String DISRUPTIONS = "DISRUPTIONS";

    public static final String UP = "UP";

    /**
     * Severity
     */
    public static final int SEVERITY_ONE = 1;

    public static final int SEVERITY_TWO = 2;

    public static final int SEVERITY_THREE = 3;
}
