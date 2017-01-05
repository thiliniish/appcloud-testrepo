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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.Interceptor;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.ServiceMethodInfo;

/**
 * Intercepts the requests and add "Access-Control-Allow-Origin: *" header
 */
public class HeaderInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(HeaderInterceptor.class);


    @Override public boolean preCall(Request request, Response response, ServiceMethodInfo serviceMethodInfo)
            throws Exception {
        response.setHeader("Access-Control-Allow-Origin", "*");
        return true;
    }

    @Override public void postCall(Request request, int i, ServiceMethodInfo serviceMethodInfo) throws Exception {

    }
}
