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

package org.wso2.carbon.cloud.deployment.monitor.service;

import com.google.gson.JsonObject;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.cloud.deployment.monitor.utils.dao.StatusReportingDAOImpl;
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.CurrentTaskStatus;
import org.wso2.deployment.monitor.core.scheduler.ScheduleManager;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * This service expose management actions of the Deployment Monitor
 */
@Path("/deployment-monitor")
@Produces({ "application/json" })
public class CloudMonitorService implements Microservice {

    private static final Logger logger = LoggerFactory.getLogger(CloudMonitorService.class);

    private enum ACTION {
        SCHEDULE,
        UNSCHEDULE,
        PAUSE,
        RESUME
    }

    @POST
    @Path("/schedule")
    public Response schedule(@QueryParam("task") String task,
            @QueryParam("server") String server) {
        return doScheduling(CloudMonitorService.ACTION.SCHEDULE, task, server);
    }

    @POST
    @Path("/unschedule")
    public Response unschedule(@QueryParam("task") String task,
            @QueryParam("server") String server) {
        return doScheduling(CloudMonitorService.ACTION.UNSCHEDULE, task, server);
    }

    @POST @Path("/pause") public Response pause(@QueryParam("task") String task, @QueryParam("server") String server) {
        return doScheduling(CloudMonitorService.ACTION.PAUSE, task, server);
    }

    @POST
    @Path("/resume")
    public Response resume(@QueryParam("task") String task,
            @QueryParam("server") String server) {
        return doScheduling(CloudMonitorService.ACTION.RESUME, task, server);
    }

    private Response doScheduling(CloudMonitorService.ACTION action, String task, String server) {
        ScheduleManager scheduleManager;
        boolean isSuccess = false;
        JsonObject responseObj = new JsonObject();

        String responseMsg = null;

        if (server == null) {
            logger.warn("Server group name has not been specified.");
            responseMsg = "Please specify the server group.";
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(responseMsg).build();
        }



        try {
            scheduleManager = ScheduleManager.getInstance();
            //by task and server both
            if (task != null) {
                logger.info("Performing Maintenance  Action : {}, Server : {}, Task : {}", action, server, task);
                switch (action) {

                case SCHEDULE:
                    isSuccess = scheduleManager.scheduleTaskForServer(task, server);
                    break;
                case UNSCHEDULE:
                    isSuccess = scheduleManager.unscheduleTaskForServer(task, server);
                    break;
                case PAUSE:
                    isSuccess = scheduleManager.pauseTaskForServer(task, server);
                    break;
                case RESUME:
                    isSuccess = scheduleManager.resumeTaskForServer(task, server);
                    break;

                default:
                    logger.warn("Incorrect Action has been specified");
                    responseMsg = "Incorrect Action has been specified. "
                            + "Please specify a action { schedule | unschedule | pause | resume }";
                    break;
                }
            } else {
                logger.info("Performing Maintenance  Action : {}, Server :{}, Task: ALL", action, server);
                //only by server
                switch (action) {

                case SCHEDULE:
                    isSuccess = scheduleManager.scheduleTasksOfServer(server);
                    break;
                case UNSCHEDULE:
                    isSuccess = scheduleManager.unscheduleTasksOfServer(server);
                    break;
                case PAUSE:
                    isSuccess = scheduleManager.pauseTasksOfServer(server);
                    break;
                case RESUME:
                    isSuccess = scheduleManager.resumeTasksOfServer(server);
                    break;

                default:
                    logger.warn("Incorrect Action has been specified");
                    responseMsg = "Incorrect Action has been specified."
                            + " Please specify a action { schedule | unschedule | pause | resume }";
                    break;
                }
            }

        } catch (SchedulerException e) {
            logger.error("Error occurred while running the maintenance task. ", e);
            responseMsg = "Error occurred while running the maintenance task. " + e.getMessage();
        }

        if (isSuccess) {
            StatusReportingDAOImpl reportingDAO = new StatusReportingDAOImpl();
            if (ACTION.PAUSE == action || ACTION.UNSCHEDULE == action) {
                reportingDAO.updateCurrentTaskStatusForMaintenance(server, task, CurrentTaskStatus.State.MAINTENANCE);
                reportingDAO.addMaintenanceSummary(server, task);
            } else {
                reportingDAO.updateCurrentTaskStatusForMaintenance(server, task, CurrentTaskStatus.State.UP);
                reportingDAO.updateMaintenanceSummary(server, task);
            }
            createResponseObject(responseObj, false, "Maintenance Task was successful");
            return Response.status(Response.Status.OK).entity(responseObj).build();
        } else {
            logger.error("Maintenance  Action : {}, Server {}: , Task : {} was failed", action, server, task);
            if (responseMsg != null) {
                createResponseObject(responseObj, true, responseMsg);
            } else {
                createResponseObject(responseObj, true, "Error occurred while running the maintenance task");
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseObj).build();
        }
    }

    private void createResponseObject(JsonObject jsonObject, boolean errorOccurred, String msg) {
        jsonObject.addProperty("error", errorOccurred);
        jsonObject.addProperty("message", msg);
    }
}
