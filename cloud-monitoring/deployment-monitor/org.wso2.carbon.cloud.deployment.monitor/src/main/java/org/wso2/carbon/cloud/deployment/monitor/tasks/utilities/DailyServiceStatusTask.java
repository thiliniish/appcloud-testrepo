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

package org.wso2.carbon.cloud.deployment.monitor.tasks.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.cloud.deployment.monitor.utils.CloudMonitoringConstants;
import org.wso2.carbon.cloud.deployment.monitor.utils.CloudMonitoringException;
import org.wso2.carbon.cloud.deployment.monitor.utils.dao.StatusReportingDAOImpl;
import org.wso2.carbon.cloud.deployment.monitor.utils.dao.UptimeInformationDAOImpl;
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.DailyServiceStatus;
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.FailureSummary;
import org.wso2.deployment.monitor.api.DeploymentMonitorTask;
import org.wso2.deployment.monitor.api.RunStatus;
import org.wso2.deployment.monitor.core.TaskUtils;
import org.wso2.deployment.monitor.core.model.ServerGroup;
import org.wso2.deployment.monitor.core.model.TaskConfig;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * This Task summarizes the daily status
 */
public class DailyServiceStatusTask implements DeploymentMonitorTask {

    private static final Logger logger = LoggerFactory.getLogger(DailyServiceStatusTask.class);

    @Override public RunStatus execute(ServerGroup serverGroup, Properties customParams) {

        RunStatus runStatus = new RunStatus();
        runStatus.setServerGroupName(serverGroup.getName());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);

        UptimeInformationDAOImpl uptimeInformationDAOImpl = new UptimeInformationDAOImpl();
        List<FailureSummary> failureSummaries = null;
        try {
            failureSummaries = uptimeInformationDAOImpl
                    .getFailureSummaries(serverGroup.getName(), cal.getTime(), cal.getTime());
        } catch (CloudMonitoringException e) {
            logger.error(e.getMessage());
        }

        DailyServiceStatus dailyServiceStatus;
        if (failureSummaries == null) {
            runStatus.setSuccess(false);
            runStatus.setServerGroupName(serverGroup.getName());
            runStatus.setMessage("Error Occurred while getting failure summaries");
            return runStatus;
        }
        if (failureSummaries.size() == 0) {
            dailyServiceStatus = new DailyServiceStatus(serverGroup.getName(), cal.getTime(), 0,
                    DailyServiceStatus.State.UP);
        } else {
            int totalDownTime = 0;
            for (FailureSummary failureSummary : failureSummaries) {
                Optional<TaskConfig> optional = TaskUtils.getTaskConfigByName(runStatus.getTaskName());
                int severity =  CloudMonitoringConstants.SEVERITY_THREE;
                if (optional.isPresent()) {
                    TaskConfig taskConfig = optional.get();
                    severity = (int) taskConfig.getTaskParams().get("severity");
                }
                if (CloudMonitoringConstants.SEVERITY_ONE == severity) {
                    totalDownTime = totalDownTime + failureSummary.getDownTime();
                } else if (CloudMonitoringConstants.SEVERITY_TWO == severity) {
                    totalDownTime = totalDownTime + (failureSummary.getDownTime() * 2 / 3);
                } else if (CloudMonitoringConstants.SEVERITY_THREE == severity) {
                    totalDownTime = totalDownTime + (failureSummary.getDownTime() / 3);
                }
            }

            logger.info("Calculated Downtime for Server : {} for date : {} is : {} s", serverGroup.getName(),
                    cal.getTime(), totalDownTime);
            int downTimeInMinutes = totalDownTime / 60;

            if (downTimeInMinutes < 2) {
                dailyServiceStatus = new DailyServiceStatus(serverGroup.getName(), cal.getTime(), totalDownTime,
                        DailyServiceStatus.State.UP);
            } else if (downTimeInMinutes < 29) {
                dailyServiceStatus = new DailyServiceStatus(serverGroup.getName(), cal.getTime(), totalDownTime,
                        DailyServiceStatus.State.DISRUPTIONS);
            } else {
                dailyServiceStatus = new DailyServiceStatus(serverGroup.getName(), cal.getTime(), totalDownTime,
                        DailyServiceStatus.State.DOWN);
            }

        }

        StatusReportingDAOImpl statusReportingDAO = new StatusReportingDAOImpl();
        try {
            statusReportingDAO.addDailyServiceStatus(dailyServiceStatus);
        } catch (CloudMonitoringException e) {
            String msg = "Error occurred while adding daily status.";
            return handleException(runStatus, e, msg);
        }

        runStatus.setSuccess(true);
        runStatus.setMessage("DailyServiceStatusTask was executed successfully");
        return runStatus;
    }

    private RunStatus handleException(RunStatus runStatus, CloudMonitoringException e, String msg) {
        logger.error(msg, e);
        runStatus.setSuccess(false);
        runStatus.setMessage(msg + " " + e.getMessage());
        return runStatus;
    }
}
