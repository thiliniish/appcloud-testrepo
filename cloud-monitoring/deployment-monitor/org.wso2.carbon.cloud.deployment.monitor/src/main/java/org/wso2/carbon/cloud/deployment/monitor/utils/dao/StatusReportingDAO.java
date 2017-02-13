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

package org.wso2.carbon.cloud.deployment.monitor.utils.dao;

import org.wso2.carbon.cloud.deployment.monitor.utils.CloudMonitoringException;
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.CurrentTaskStatus;
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.DailyServiceStatus;
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.FailureRecord;
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.FailureSummary;
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.SuccessRecord;

/**
 * DAO Interface
 */
public interface StatusReportingDAO {

    void addSuccessRecord(SuccessRecord successRecord) throws CloudMonitoringException;

    int addFailureRecord(FailureRecord failureRecord) throws CloudMonitoringException;

    void addFailureSummary(FailureSummary failureSummary) throws CloudMonitoringException;

    void updateCurrentTaskStatus(CurrentTaskStatus currentTaskStatus) throws CloudMonitoringException;

    void updateCurrentTaskStatusForMaintenance(String serverName, String task, CurrentTaskStatus.State state)
            throws CloudMonitoringException;

    void updateMaintenanceSummary(String serverName, String task) throws CloudMonitoringException;

    void addDailyServiceStatus(DailyServiceStatus dailyServiceStatus) throws CloudMonitoringException;

}
