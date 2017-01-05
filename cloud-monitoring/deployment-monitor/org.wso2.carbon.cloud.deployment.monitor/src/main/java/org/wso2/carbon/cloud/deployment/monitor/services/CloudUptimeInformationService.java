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

package org.wso2.carbon.cloud.deployment.monitor.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.cloud.deployment.monitor.status.CurrentStatusRetriever;
import org.wso2.carbon.cloud.deployment.monitor.status.DailyStatusRetriever;
import org.wso2.carbon.cloud.deployment.monitor.utils.CloudMonitoringConstants;
import org.wso2.carbon.cloud.deployment.monitor.utils.CloudMonitoringException;
import org.wso2.msf4j.Microservice;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * This service exposes uptime statistics
 */
@Path("/status")
@Produces({ "application/json" })
public class CloudUptimeInformationService implements Microservice {

    private static final Logger logger = LoggerFactory.getLogger(CloudUptimeInformationService.class);

    private DailyStatusRetriever dailyStatusRetriever;
    private CurrentStatusRetriever currentStatusRetriever;

    public CloudUptimeInformationService() throws CloudMonitoringException {
        currentStatusRetriever = new CurrentStatusRetriever();
        dailyStatusRetriever = new DailyStatusRetriever();
    }

    @GET
    @Path("/current/{server}")
    public Response getCurrentServerStatus(@PathParam("server") String server) {
        try {
            JsonObject object = currentStatusRetriever.getCurrentServerStatus(server);
            return Response.status(Response.Status.OK).entity(object)
                    .header(CloudMonitoringConstants.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*").build();

        } catch (CloudMonitoringException e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/current/all")
    public Response getAllCurrentServerStatuses() {
        try {
            JsonArray array = currentStatusRetriever.getAllCurrentServerStatuses();
            return Response.status(Response.Status.OK).entity(array)
                    .header(CloudMonitoringConstants.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*").build();
        } catch (CloudMonitoringException e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/daily/{service}")
    public Response getDailyServiceStatuses(@PathParam("service") String service,
            @QueryParam("from") String fromDateStr, @QueryParam("to") String toDateStr) {

        Date fromDate = parseDateString(fromDateStr);
        Date toDate = parseDateString(toDateStr);

        if (fromDate != null && toDate != null) {
            try {
                JsonArray jsonArray = dailyStatusRetriever.getDailyStatuses(service, fromDate, toDate);
                return Response.status(Response.Status.OK).entity(jsonArray)
                        .header(CloudMonitoringConstants.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*").build();
            } catch (CloudMonitoringException e) {
                return createErrorResponse(e);
            }
        } else {
            return createErrorResponse(new CloudMonitoringException("Invalid date range specified", 400));
        }
    }

    @GET
    @Path("/overview")
    public Response getOverview() {

        Calendar toDate = Calendar.getInstance();
        Calendar fromDate = Calendar.getInstance();
        fromDate.add(Calendar.DATE, -7);

        JsonArray overview = new JsonArray();
        JsonArray servers;

        try {
            servers = currentStatusRetriever.getAllCurrentServerStatuses();
        } catch (CloudMonitoringException e) {
            return createErrorResponse(e);
        }

        for (JsonElement element : servers) {
            JsonObject serverObj = element.getAsJsonObject();
            serverObj.remove("tasks");
            JsonArray jsonArray = new JsonArray();
            try {
                jsonArray = dailyStatusRetriever
                        .getDailyStatuses(serverObj.get("server").getAsString(), fromDate.getTime(), toDate.getTime());
            } catch (CloudMonitoringException e) {
                //Ignore Exception here
            }
            serverObj.add("sevenDayStatus", jsonArray);

            if (jsonArray.size() > 0) {
                double sevenDayUptime = 0;
                for (JsonElement dailyStatus : jsonArray) {
                    sevenDayUptime =
                            sevenDayUptime + dailyStatus.getAsJsonObject().get("uptimePercentage").getAsDouble();
                }
                sevenDayUptime = sevenDayUptime / jsonArray.size();
                DecimalFormat df = new DecimalFormat("#.##");
                df.setRoundingMode(RoundingMode.DOWN);
                String uptimeVal = df.format(sevenDayUptime) + "%";
                serverObj.addProperty("sevenDayUptime", uptimeVal);
            } else {
                serverObj.addProperty("sevenDayUptime", "N/A");
            }
            overview.add(serverObj);
        }
        return Response.status(Response.Status.OK).entity(overview)
                .header(CloudMonitoringConstants.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*").build();
    }

    private Date parseDateString(String dateString) {
        if (dateString != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                return sdf.parse(dateString);
            } catch (ParseException e) {
                logger.warn("An Error occurred while parsing the date string : " + dateString);
            }
        }
        return null;
    }

    private Response createErrorResponse(CloudMonitoringException e) {
        JsonObject errorObj = new JsonObject();
        errorObj.addProperty("error", true);
        errorObj.addProperty("code", e.getErrorCode());
        errorObj.addProperty("message", e.getMessage());
        return Response.status(e.getErrorCode()).entity(errorObj).build();
    }

}
