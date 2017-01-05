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
import org.wso2.carbon.cloud.deployment.monitor.utils.CloudMonitoringException;
import org.wso2.carbon.cloud.deployment.monitor.utils.dao.UptimeInformationDAOImpl;
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.DailyServiceStatus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * {@link DailyStatusRetriever}
 */
public class DailyStatusRetriever {

    private SimpleDateFormat sdf = new SimpleDateFormat("dd MMM");
    UptimeInformationDAOImpl uptimeInformationDAO;

    public DailyStatusRetriever() throws CloudMonitoringException {
        uptimeInformationDAO = new UptimeInformationDAOImpl();
    }

    public JsonArray getDailyStatuses(String service, Date from, Date to) throws CloudMonitoringException {

        List<DailyServiceStatus> dailyServiceStatuses = uptimeInformationDAO.getDailyServiceStatuses(service, from, to);
        if (dailyServiceStatuses.isEmpty()) {
            throw new CloudMonitoringException("No Current Status records found for server", 404);
        } else {
            JsonArray jsonArray = new JsonArray();
            for (DailyServiceStatus dailyServiceStatus : dailyServiceStatuses) {
                JsonObject object = new JsonObject();
                object.addProperty("date", sdf.format(dailyServiceStatus.getDate()));
                object.addProperty("status", dailyServiceStatus.getDailyServiceStatus());
                object.addProperty("downtime", dailyServiceStatus.getServiceDowntime());
                object.addProperty("uptimePercentage", dailyServiceStatus.getUptimePercentage());
                jsonArray.add(object);
            }
            return jsonArray;
        }
    }

}
