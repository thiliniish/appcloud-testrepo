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
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.FailureSummary;

import java.util.Date;
import java.util.List;

/**
 * {@link DailyStatusRetriever}
 */
public class DailyStatusRetriever {

    public JSONObject getTodayFailures(String server) {
        UptimeInformationDAO uptimeInformationDAO = new UptimeInformationDAO();
        List<FailureSummary> failureSummaries = uptimeInformationDAO.getFailureSummaries(server, new Date());
        JSONObject serverObj = new JSONObject();
        if (failureSummaries == null) {
            createErrorObject(serverObj, 500, "Error occurred while getting the current status of the server");
        } else if (failureSummaries.isEmpty()) {
            createErrorObject(serverObj, 404, "No Current Status records found for server");
        } else {
            JSONArray jsonArray = new JSONArray();
            jsonArray.addAll(failureSummaries);
            serverObj.put("failures", jsonArray);
        }
        return serverObj;

    }

    private void createErrorObject(JSONObject rootObject, int code, String msg) {
        rootObject.put("error", true);
        rootObject.put("code", code);
        rootObject.put("message", msg);
    }
}
