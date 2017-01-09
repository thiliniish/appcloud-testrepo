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
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.FailureSummary;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * DAO Interface
 */
public interface UptimeInformationDAO {

    List<CurrentTaskStatus> getCurrentStatus(String server) throws CloudMonitoringException;

    Map<String, List<CurrentTaskStatus>> getAllCurrentStatuses() throws CloudMonitoringException;

    List<DailyServiceStatus> getDailyServiceStatuses(String service, Date from, Date to)
            throws CloudMonitoringException;

    Map<String, List<DailyServiceStatus>> getAllDailyServiceStatuses(Date from, Date to)
            throws CloudMonitoringException;

    List<FailureSummary> getFailureSummaries(String server, Date from, Date to) throws CloudMonitoringException;

    Map<String, List<FailureSummary>> getAllFailureSummaries(Date from, Date to) throws CloudMonitoringException;

}
