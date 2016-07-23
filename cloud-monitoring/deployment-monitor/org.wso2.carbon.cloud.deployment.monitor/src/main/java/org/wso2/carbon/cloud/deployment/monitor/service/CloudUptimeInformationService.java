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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.cloud.deployment.monitor.status.CurrentStatusRetriever;
import org.wso2.carbon.cloud.deployment.monitor.status.DailyStatusRetriever;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * This service expose management actions of the Deployment Monitor
 */
@Path("/status") @Produces({ "application/json" }) public class CloudUptimeInformationService
        implements Microservice {

    private static final Logger logger = LoggerFactory.getLogger(CloudUptimeInformationService.class);

    @GET @Path("/current/{server}") public Response getCurrentServerStatus(@PathParam("server") String server) {
        CurrentStatusRetriever currentStatusRetriever = new CurrentStatusRetriever();
        JSONObject object = currentStatusRetriever.getCurrentServerStatus(server);
        if (object.get("error") != null) {
            return Response.status((Integer) object.get("code")).entity(object).build();
        } else {
            return Response.status(Response.Status.OK).entity(object).build();
        }

    }

    @GET @Path("/current/all") public Response getCurrentStatus() {
        CurrentStatusRetriever currentStatusRetriever = new CurrentStatusRetriever();
        JSONObject object = currentStatusRetriever.getAllCurrentServerStatuses();
        if (object.get("error") != null) {
            return Response.status((Integer) object.get("code")).entity(object).build();
        } else {
            return Response.status(Response.Status.OK).entity(object).build();
        }
    }

    @GET @Path("/failures/today/{server}") public Response getTodayFailures(@PathParam("server") String server) {
        DailyStatusRetriever dailyStatusRetriever = new DailyStatusRetriever();
        JSONObject object = dailyStatusRetriever.getTodayFailures(server);
        if (object.get("error") != null) {
            return Response.status((Integer) object.get("code")).entity(object).build();
        } else {
            return Response.status(Response.Status.OK).entity(object).build();
        }
    }


}
