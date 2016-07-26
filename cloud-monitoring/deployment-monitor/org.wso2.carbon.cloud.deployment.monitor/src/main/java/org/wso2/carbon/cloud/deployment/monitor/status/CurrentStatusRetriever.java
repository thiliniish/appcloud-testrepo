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

package org.wso2.carbon.cloud.deployment.monitor.status;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.wso2.carbon.cloud.deployment.monitor.utils.CloudMonitoringConstants;
import org.wso2.carbon.cloud.deployment.monitor.utils.dao.UptimeInformationDAO;
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.CurrentTaskStatus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * {@link CurrentStatusRetriever}
 */
public class CurrentStatusRetriever {

    private SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy, HH:mm:ss");


    public JsonObject getCurrentServerStatus(String server) {
        UptimeInformationDAO uptimeInformationDAO = new UptimeInformationDAO();
        List<CurrentTaskStatus> currentTaskStatuses = uptimeInformationDAO.getCurrentStatus(server);
        JsonObject serverObj = new JsonObject();
        if (currentTaskStatuses == null) {
            createErrorObject(serverObj, 500, "Error occurred while getting the current status of the server");
        } else if (currentTaskStatuses.isEmpty()) {
            createErrorObject(serverObj, 404, "No Current Status records found for server");
        } else {
            serverObj = createServerObject(server, currentTaskStatuses);
        }

        return serverObj;

    }

    public JsonObject getAllCurrentServerStatuses() {
        UptimeInformationDAO uptimeInformationDAO = new UptimeInformationDAO();
        Map<String, List<CurrentTaskStatus>> currentServerStatuses = uptimeInformationDAO.getAllCurrentStatuses();

        JsonObject rootObject = new JsonObject();
        if (currentServerStatuses == null) {
            createErrorObject(rootObject, 500, "Error occurred while getting the current status of the server");
        } else if (currentServerStatuses.isEmpty()) {
            createErrorObject(rootObject, 404, "No Current Status records found for all servers");
        } else {
            JsonArray servers = new JsonArray();
            for (Map.Entry<String, List<CurrentTaskStatus>> entry : currentServerStatuses.entrySet()) {
                JsonObject serverObj = createServerObject(entry.getKey(), entry.getValue());
                servers.add(serverObj);
            }
            rootObject.add("servers", servers);
        }
        return rootObject;
    }

    private JsonObject createServerObject(String server, List<CurrentTaskStatus> currentTaskStatuses) {
        JsonObject serverObj = new JsonObject();
        JsonArray tasks = new JsonArray();
        int failedTaskCount = 0;
        int maintenanceTaskCount = 0;
        for (CurrentTaskStatus currentTaskStatus : currentTaskStatuses) {
            if (CurrentTaskStatus.State.DOWN.name().equalsIgnoreCase(currentTaskStatus.getState())) {
                failedTaskCount++;
            } else if (CurrentTaskStatus.State.MAINTENANCE.name().equalsIgnoreCase(currentTaskStatus.getState())) {
                maintenanceTaskCount++;
            }
            JsonObject taskObj = new JsonObject();
            taskObj.addProperty("name", currentTaskStatus.getTaskName());
            taskObj.addProperty("status", currentTaskStatus.getState());
            taskObj.addProperty("lastUpdated", sdf.format(currentTaskStatus.getLastUpdated()));
            tasks.add(taskObj);
        }
        serverObj.addProperty("server", server);
        if (failedTaskCount == currentTaskStatuses.size()) {
            serverObj.addProperty("status", CloudMonitoringConstants.DOWN);
        } else if (maintenanceTaskCount == currentTaskStatuses.size()) {
            serverObj.addProperty("status", CloudMonitoringConstants.MAINTENANCE);
        } else if (failedTaskCount > 0 || maintenanceTaskCount > 0) {
            serverObj.addProperty("status", CloudMonitoringConstants.DISRUPTIONS);
        } else {
            serverObj.addProperty("status", CloudMonitoringConstants.UP);
        }
        Date lastUpdated = currentTaskStatuses.get(0).getLastUpdated();
        serverObj.addProperty("lastUpdated", sdf.format(lastUpdated));
        serverObj.add("tasks", tasks);
        return serverObj;
    }

    private void createErrorObject(JsonObject rootObject, int code, String msg) {
        rootObject.addProperty("error", true);
        rootObject.addProperty("code", code);
        rootObject.addProperty("message", msg);
    }

}
