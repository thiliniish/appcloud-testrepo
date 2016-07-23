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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.cloud.deployment.monitor.utils.dao.UptimeInformationDAO;
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.CurrentTaskStatus;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * {@link CurrentStatusRetriever}
 */
public class CurrentStatusRetriever {

    public JSONObject getCurrentServerStatus(String server) {
        UptimeInformationDAO uptimeInformationDAO = new UptimeInformationDAO();
        List<CurrentTaskStatus> currentTaskStatuses = uptimeInformationDAO.getCurrentStatus(server);
        JSONObject serverObj = new JSONObject();
        if (currentTaskStatuses == null) {
            createErrorObject(serverObj, 500, "Error occurred while getting the current status of the server");
        } else if (currentTaskStatuses.isEmpty()) {
            createErrorObject(serverObj, 404, "No Current Status records found for server");
        } else {
            serverObj = createServerObject(server, currentTaskStatuses);
        }

        return serverObj;

    }

    public JSONObject getAllCurrentServerStatuses() {
        UptimeInformationDAO uptimeInformationDAO = new UptimeInformationDAO();
        Map<String, List<CurrentTaskStatus>> currentServerStatuses = uptimeInformationDAO.getAllCurrentStatuses();

        JSONObject rootObject = new JSONObject();
        if (currentServerStatuses == null) {
            createErrorObject(rootObject, 500, "Error occurred while getting the current status of the server");
        } else if (currentServerStatuses.isEmpty()) {
            createErrorObject(rootObject, 404, "No Current Status records found for all servers");
        } else {
            JSONArray servers = new JSONArray();
            for (Map.Entry<String, List<CurrentTaskStatus>> entry : currentServerStatuses.entrySet()) {
                JSONObject serverObj = createServerObject(entry.getKey(), entry.getValue());
                servers.add(serverObj);
            }
            rootObject.put("servers", servers);
        }
        return rootObject;
    }

    private JSONObject createServerObject(String server, List<CurrentTaskStatus> currentTaskStatuses) {
        JSONObject serverObj = new JSONObject();
        JSONArray tasks = new JSONArray();
        int failedTaskCount = 0;
        int maintenanceTaskCount = 0;
        for (CurrentTaskStatus currentTaskStatus : currentTaskStatuses) {
            if (CurrentTaskStatus.State.DOWN.name().equalsIgnoreCase(currentTaskStatus.getState())) {
                failedTaskCount++;
            } else if (CurrentTaskStatus.State.MAINTENANCE.name().equalsIgnoreCase(currentTaskStatus.getState())) {
                maintenanceTaskCount++;
            }
            JSONObject taskObj = new JSONObject();
            taskObj.put("name", currentTaskStatus.getTaskName());
            taskObj.put("status", currentTaskStatus.getState());
            taskObj.put("lastUpdated", currentTaskStatus.getLastUpdated().toString());
            tasks.add(taskObj);
        }
        serverObj.put("server", server);
        if (failedTaskCount == currentTaskStatuses.size()) {
            serverObj.put("status", "DOWN");
        } else if (maintenanceTaskCount == currentTaskStatuses.size()) {
            serverObj.put("status", "MAINTENANCE");
        } else if (failedTaskCount > 0 || maintenanceTaskCount > 0) {
            serverObj.put("status", "DISRUPTIONS");
        } else {
            serverObj.put("status", "NORMAL");
        }
        Date lastUpdated = currentTaskStatuses.get(0).getLastUpdated();
        serverObj.put("last-updated", lastUpdated.toString());
        serverObj.put("tasks", tasks);
        return serverObj;
    }

    private void createErrorObject(JSONObject rootObject, int code, String msg) {
        rootObject.put("error", true);
        rootObject.put("code", code);
        rootObject.put("message", msg);
    }

}
