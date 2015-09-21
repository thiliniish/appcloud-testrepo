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

import org.wso2.carbon.cloud.integration.test.utils.CloudConstants;
import org.wso2.carbon.cloud.integration.test.utils.external.HttpHandler;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JaggeryAppAuthenticatorClient {

    private String loginUrl;
    private String logOutUrl;

    public JaggeryAppAuthenticatorClient(String hostName) {
        loginUrl = hostName + CloudConstants.LOGIN_URL_SFX;
        logOutUrl = hostName + CloudConstants.LOGOUT_URL_SFX;
    }

    public JaggeryAppAuthenticatorClient(String hostName, String appName) {
        if (appName.equals("cloudmgt")) {
            loginUrl = hostName + CloudConstants.CLOUD_LOGIN_URL_SFX;
            logOutUrl = loginUrl;
        }
    }

    public boolean login(String userName, String password) throws IOException {
        HttpHandler httpHandler = new HttpHandler();
        String value = httpHandler.doPostHttps(loginUrl, "action=login&userName="+userName+"&password="+password,"none","application/x-www-form-urlencoded");

        if (!"false".equals(value)) {
            return true;
        } else {
            return false;
        }

    }

    public boolean logout() throws IOException{
        Map<String, String> params = new HashMap<String, String>();
        HttpHandler httpHandler = new HttpHandler();
        params.put("action", "logout");
        String logoutValue = httpHandler.doPostHttps(logOutUrl, "action=logout", "none",
                                                     "application/x-www-form-urlencoded");
        if (!"false".equals(logoutValue)) {
            return true;
        } else {
            return false;
        }
    }
}

