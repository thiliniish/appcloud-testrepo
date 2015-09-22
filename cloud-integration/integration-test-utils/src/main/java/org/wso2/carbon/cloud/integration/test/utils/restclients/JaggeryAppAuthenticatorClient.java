/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.cloud.integration.test.utils.restclients;

import org.wso2.carbon.cloud.integration.test.utils.CloudIntegrationConstants;
import org.wso2.carbon.cloud.integration.test.utils.external.HttpHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JaggeryAppAuthenticatorClient {

    private String loginUrl;
    private String logOutUrl;

    public JaggeryAppAuthenticatorClient(String hostName) {
            loginUrl = hostName + CloudIntegrationConstants.CLOUD_LOGIN_URL_SFX;
            logOutUrl = loginUrl;
    }

    public boolean login(String userName, String password) throws IOException {
        Map<String,String> params = new HashMap<String, String>();
        params.put("action","login");
        params.put("userName",userName);
        params.put("password", password);
        String value = HttpHandler.doPostHttps(loginUrl,params);
        return "true".equals(value);
    }

    public boolean logout() throws IOException{
        Map<String, String> params = new HashMap<String, String>();
        String logoutValue = "false";
        params.put("action", "logout");
        if(HttpHandler.cookie != null && !"".equals(HttpHandler.cookie)){
            logoutValue = HttpHandler.doPostHttps(logOutUrl, params);
            HttpHandler.cookie = null;
        }
        return "true".equals(logoutValue);
    }
}

