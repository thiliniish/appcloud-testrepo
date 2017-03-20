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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.cloud.integration.test.utils.CloudIntegrationConstants;
import org.wso2.carbon.cloud.integration.test.utils.external.HttpHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Authenticator client used for login and logout to cloudmgt
 */
public class JaggeryAppAuthenticatorClient {

    public static final Log log = LogFactory.getLog(JaggeryAppAuthenticatorClient.class);

    private static String loginUrl;
    private static String logOutUrl;
    private String sessionCookie;
    private String usernamePara;

    public JaggeryAppAuthenticatorClient(String hostName) {
        loginUrl = hostName + CloudIntegrationConstants.CLOUD_LOGIN_URL_SFX;
        logOutUrl = loginUrl;
        usernamePara = "userName";
    }
    
    public JaggeryAppAuthenticatorClient(String hostName, String loginUrlSfx){
        loginUrl = hostName + loginUrlSfx;
        logOutUrl = loginUrl;
        usernamePara = "username";
    }

    /**
     * Login for cloud management app through login jag
     *
     * @param userName login username
     * @param password login password
     * @return boolean value of login success or fail
     * @throws IOException
     */
    public boolean login(String userName, String password) throws IOException, JSONException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "login");
        params.put(usernamePara, userName);
        params.put("password", password);
        Map resultMap = HttpHandler.doPostHttps(loginUrl, params, null, false);
        setSessionCookie((String) resultMap.get(CloudIntegrationConstants.COOKIE));
        if(loginUrl.contains(CloudIntegrationConstants.API_PUBLISHER_LOGIN_URL_SFX) ||
           loginUrl.contains(CloudIntegrationConstants.API_STORE_LOGIN_URL_SFX)){
            return "false".equals(new JSONObject(resultMap.get(CloudIntegrationConstants.RESPONSE)
                                                          .toString()).getString("error"));
        }
        return "true".equals(resultMap.get(CloudIntegrationConstants.RESPONSE));
    }

    /**
     * logout the session using cloudmgt
     *
     * @return logout status
     * @throws IOException
     */
    public boolean logout() throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "logout");
        if (sessionCookie != null) {
            Map resultMap = HttpHandler.doPostHttps(logOutUrl, params, sessionCookie, false);
            return "true".equals(resultMap.get(CloudIntegrationConstants.RESPONSE));
        } else {
            log.error("Please Login first");
            return false;
        }
    }

    /**
     * Exposing the private session cookie
     *
     * @return sessioncokkie
     */
    public String getSessionCookie() {
        return this.sessionCookie;
    }

    public void setSessionCookie(String sessionCookie) {
        this.sessionCookie = sessionCookie;
    }
}

