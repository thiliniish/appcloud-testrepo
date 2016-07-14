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

package org.wso2.carbon.cloud.deployment.monitor.callbacks;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.cloud.deployment.monitor.utils.FailureSummaryCache;
import org.wso2.carbon.cloud.deployment.monitor.utils.ReSchedulingCache;
import org.wso2.carbon.cloud.deployment.monitor.utils.dao.StatusReportingDAOImpl;
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.FailureRecord;
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.FailureSummary;
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.LiveStatus;
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.SuccessRecord;
import org.wso2.deployment.monitor.api.HostBean;
import org.wso2.deployment.monitor.api.OnResultCallback;
import org.wso2.deployment.monitor.api.RunStatus;
import org.wso2.deployment.monitor.core.TaskUtils;
import org.wso2.deployment.monitor.core.model.TaskConfig;
import org.wso2.deployment.monitor.core.scheduler.ScheduleManager;
import org.wso2.deployment.monitor.utils.notification.email.EmailSender;
import org.wso2.deployment.monitor.utils.notification.sms.SMSSender;

import java.util.Date;

/**
 * Cloud Callback implementation for Single Host Test Cases
 */
public class CloudDefaultCallBack implements OnResultCallback {

    private static final Logger logger = LoggerFactory.getLogger(CloudDefaultCallBack.class);

    @Override public void callback(RunStatus runStatus) {
        long currentTime = System.currentTimeMillis();
        FailureSummaryCache failureSummaryCache = FailureSummaryCache.getInstance();
        StatusReportingDAOImpl reportingDAO = new StatusReportingDAOImpl();
        String cacheKey = runStatus.getServerGroupName() + ":" + runStatus.getTaskName();
        if (runStatus.isSuccess()) {
            logger.info("[Task Successful] " + runStatus.getServerGroupName() + " : " + runStatus.getTaskName());
            SuccessRecord successRecord = new SuccessRecord(runStatus.getTaskName(), runStatus.getServerGroupName(),
                    currentTime);
            LiveStatus liveStatus = new LiveStatus(runStatus.getServerGroupName(), runStatus.getTaskName(),
                    LiveStatus.Status.UP);
            reportingDAO.addSuccessRecord(successRecord);
            reportingDAO.updateLiveStatus(liveStatus);

            if (failureSummaryCache.getCacheEntry(cacheKey) != null) {
                FailureSummary failureSummary = failureSummaryCache.getCacheEntry(cacheKey);
                long duration = currentTime - failureSummary.getStartTime();
                failureSummary.setDownTime(duration);
                failureSummary.setEndTime(currentTime);
                reportingDAO.addFailureSummary(failureSummary);
                failureSummaryCache.clearCacheEntry(cacheKey);

                resetSchedule(runStatus, cacheKey);
            }
        } else {
            String msg = "[Task Failed] " + runStatus.getServerGroupName() + " : " + runStatus.getTaskName();
            HostBean hostBean = runStatus.getFailedHosts().get(0);
            String errorMsg = hostBean.getErrorMsg();
            logger.error(msg + ", Error: " + errorMsg);

            //send notifications
            EmailSender.getInstance().send(msg, errorMsg);
            SMSSender.getInstance().send(msg);
            FailureRecord failureRecord = new FailureRecord(runStatus.getTaskName(), runStatus.getServerGroupName(),
                    errorMsg, currentTime);
            LiveStatus liveStatus = new LiveStatus(runStatus.getServerGroupName(), runStatus.getTaskName(),
                    LiveStatus.Status.DOWN);
            int currentID = reportingDAO.addFailureRecord(failureRecord);
            reportingDAO.updateLiveStatus(liveStatus);

            if (failureSummaryCache.getCacheEntry(cacheKey) != null) {
                FailureSummary failureSummary = failureSummaryCache.getCacheEntry(cacheKey);
                failureSummary.setEndID(currentID);
                long duration = currentTime - failureSummary.getStartTime();
                failureSummary.setDownTime(duration);
                failureSummaryCache.addToCache(cacheKey, failureSummary);
            } else {
                FailureSummary failureSummary = new FailureSummary(runStatus.getServerGroupName(),
                        runStatus.getTaskName(), currentID, new Date(), currentTime);
                failureSummaryCache.addToCache(cacheKey, failureSummary);
            }
            increaseScheduleFrequency(runStatus, cacheKey);
        }
    }

    private void increaseScheduleFrequency(RunStatus runStatus, String cacheKey) {
        //The task has been re-scheduled previously
        if (ReSchedulingCache.getInstance().getCacheEntry(cacheKey)) {
            return;
        }
        TaskConfig taskConfig = TaskUtils.getTaskConfigByName(runStatus.getTaskName());
        if (taskConfig != null) {
            boolean increaseFrequencyInFailure = (boolean) taskConfig.getTaskParams().get("increaseFrequencyInFailure");
            if (increaseFrequencyInFailure) {
                logger.info("Increasing scheduling frequency for Task : {} for Server : {}", runStatus.getTaskName(),
                        runStatus.getServerGroupName());
                String triggerType = (String) taskConfig.getTaskParams().get("triggerType");
                String triggerValue = (String) taskConfig.getTaskParams().get("trigger");
                try {
                    ScheduleManager.getInstance()
                            .reScheduleTaskForServer(runStatus.getTaskName(), runStatus.getServerGroupName(),
                                    triggerType, triggerValue);
                } catch (SchedulerException e) {
                    logger.error(
                            "Increasing scheduling frequency for Task - " + runStatus.getTaskName() + " for Server - "
                                    + runStatus.getServerGroupName() + " failed", e);
                }
            }
            ReSchedulingCache.getInstance().addToCache(cacheKey, true);
        } else {
            logger.warn("Unable to increase the schedule frequency. Cannot find a task with the name : {}",
                    runStatus.getTaskName());
        }
    }

    private void resetSchedule(RunStatus runStatus, String cacheKey) {
        //The task has been re-scheduled previously
        if (ReSchedulingCache.getInstance().getCacheEntry(cacheKey)) {
            logger.info("Resetting scheduling frequency for Task : {} for Server : {}", runStatus.getTaskName(),
                    runStatus.getServerGroupName());
            TaskConfig taskConfig = TaskUtils.getTaskConfigByName(runStatus.getTaskName());
            if (taskConfig != null) {
                try {
                    ScheduleManager.getInstance()
                            .reScheduleTaskForServer(runStatus.getTaskName(), runStatus.getServerGroupName(),
                                    taskConfig.getTriggerType(), taskConfig.getTrigger());
                    ReSchedulingCache.getInstance().clearCacheEntry(cacheKey);
                } catch (SchedulerException e) {
                    logger.error(
                            "Resetting scheduling frequency for Task - " + runStatus.getTaskName() + " for Server - "
                                    + runStatus.getServerGroupName() + " failed", e);
                }
            }
        }
    }

}
