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
import org.wso2.carbon.cloud.deployment.monitor.utils.CloudMonitoringException;
import org.wso2.carbon.cloud.deployment.monitor.utils.dao.UptimeInformationDAOImpl;
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.CurrentTaskStatus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * {@link CurrentStatusRetriever}
 */
public class CurrentStatusRetriever {

    private SimpleDateFormat sdf = new SimpleDateFormat(CloudMonitoringConstants.DATE_FORMAT_FOR_DASHBOARD);

    private UptimeInformationDAOImpl uptimeInformationDAO;

    public CurrentStatusRetriever() throws CloudMonitoringException {
        uptimeInformationDAO =  new UptimeInformationDAOImpl();
    }


    public JsonObject getCurrentServerStatus(String server) throws CloudMonitoringException {
        List<CurrentTaskStatus> currentTaskStatuses = uptimeInformationDAO.getCurrentStatus(server);
        if (currentTaskStatuses.isEmpty()) {
            throw new CloudMonitoringException("No Current Status records found for server", 404);
        } else {
            return createServerObject(server, currentTaskStatuses);
        }
    }

    public JsonArray getAllCurrentServerStatuses() throws CloudMonitoringException {
        Map<String, List<CurrentTaskStatus>> currentServerStatuses = uptimeInformationDAO.getAllCurrentStatuses();

        if (currentServerStatuses.isEmpty()) {
            throw new CloudMonitoringException("No Current Status records found for all servers", 404);
        } else {
            JsonArray servers = new JsonArray();
            for (Map.Entry<String, List<CurrentTaskStatus>> entry : currentServerStatuses.entrySet()) {
                JsonObject serverObj = createServerObject(entry.getKey(), entry.getValue());
                servers.add(serverObj);
            }
            return  servers;
        }
    }

    private JsonObject createServerObject(String server, List<CurrentTaskStatus> currentTaskStatuses) {
        JsonObject serverObj = new JsonObject();
        JsonArray tasks = new JsonArray();

        int failedTaskCount = 0;
        int maintenanceTaskCount = 0;

        for (CurrentTaskStatus currentTaskStatus : currentTaskStatuses) {
            if (CurrentTaskStatus.State.DOWN == currentTaskStatus.getState()) {
                failedTaskCount++;
            } else if (CurrentTaskStatus.State.MAINTENANCE == currentTaskStatus.getState()) {
                maintenanceTaskCount++;
            }
            JsonObject taskObj = new JsonObject();
            taskObj.addProperty("name", currentTaskStatus.getTaskName());
            taskObj.addProperty("status", currentTaskStatus.getState().toString());
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

        // Tasks are in descending order of the last updated time, hence server's last updated time is equal
        // to the first tasks last updated time
        Date lastUpdated = currentTaskStatuses.get(0).getLastUpdated();
        serverObj.addProperty("lastUpdated", sdf.format(lastUpdated));
        serverObj.add("tasks", tasks);
        return serverObj;
    }

}
