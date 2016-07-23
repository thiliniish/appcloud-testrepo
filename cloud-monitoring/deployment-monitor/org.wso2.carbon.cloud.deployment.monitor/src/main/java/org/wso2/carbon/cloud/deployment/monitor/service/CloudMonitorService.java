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

import org.json.simple.JSONObject;
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
@Path("/monitor") @Produces({ "application/json" }) public class CloudMonitorService implements Microservice {

    private static final Logger logger = LoggerFactory.getLogger(CloudMonitorService.class);

    @POST @Path("/maintenance") public Response doMaintenanceAction(@QueryParam("action") String action,
            @QueryParam("task") String task, @QueryParam("server") String server) {
        ScheduleManager scheduleManager;
        boolean isSuccess = false;
        String responseMsg = null;
        JSONObject jsonObject = new JSONObject();
        //Checking the mandatory parameters
        if (action == null) {
            logger.warn("Action has not been specified.");
            createErrorObject(jsonObject, 400, "Action name is not specified. "
                    + "Please specify a action { schedule | unschedule | pause | resume }");
            return Response.status(Response.Status.BAD_REQUEST).entity(jsonObject).build();
        }

        if (server == null) {
            logger.warn("Server name has not been specified");
            createErrorObject(jsonObject, 400, "Please specify the server");
            return Response.status(Response.Status.BAD_REQUEST).entity(jsonObject).build();
        }

        logger.info("Action : " + action + ", Task : " + task + ", Server : " + server);

        try {
            scheduleManager = ScheduleManager.getInstance();
            //by task and server both
            if (task != null) {
                switch (action) {

                case "schedule":
                    isSuccess = scheduleManager.scheduleTaskForServer(task, server);
                    break;
                case "unschedule":
                    isSuccess = scheduleManager.unScheduleTaskForServer(task, server);
                    break;
                case "pause":
                    isSuccess = scheduleManager.pauseTaskForServer(task, server);

                    break;
                case "resume":
                    isSuccess = scheduleManager.resumeTaskForServer(task, server);
                    break;

                default:
                    logger.warn("Incorrect Action has been specified");
                    responseMsg = "Incorrect Action has been specified. "
                            + "Please specify a action { schedule | unschedule | pause | resume }";
                    break;
                }
                //only by server
            } else {
                switch (action) {

                case "schedule":
                    isSuccess = scheduleManager.scheduleTasksOfServer(server);
                    break;
                case "unschedule":
                    isSuccess = scheduleManager.unScheduleTasksOfServer(server);
                    break;
                case "pause":
                    isSuccess = scheduleManager.pauseTasksOfServer(server);
                    break;
                case "resume":
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
            logger.error("Error occurred while running the Scheduling Service {}", e);
            createErrorObject(jsonObject, 500, "Error occurred while running the Scheduling Service");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(jsonObject).build();
        }

        if (isSuccess) {
            StatusReportingDAOImpl reportingDAO = new StatusReportingDAOImpl();
            if ("pause".equals(action) || "unschedule".equals(action)) {
                reportingDAO.updateCurrentTaskStatusForMaintenance(server, task, CurrentTaskStatus.State.MAINTENANCE);
                reportingDAO.addMaintenanceSummary(server, task);
            } else {
                reportingDAO.updateCurrentTaskStatusForMaintenance(server, task, CurrentTaskStatus.State.UP);
                reportingDAO.updateMaintenanceSummary(server, task);
            }
            jsonObject.put("success", true);
            jsonObject.put("message", "Completed Action : " + action + " for Server : " + server);
            return Response.status(Response.Status.OK).entity(jsonObject).build();
        } else {
            createErrorObject(jsonObject, 400, responseMsg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(jsonObject).build();
        }
    }

    private void createErrorObject(JSONObject jsonObject, int code, String msg) {
        jsonObject.put("error", true);
        jsonObject.put("code", code);
        jsonObject.put("message", msg);
    }
}
