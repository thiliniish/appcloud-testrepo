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

    private String loginUrl;
    private String logOutUrl;
    private String sessionCookie;

    public JaggeryAppAuthenticatorClient(String hostName) {
        loginUrl = hostName + CloudIntegrationConstants.CLOUD_LOGIN_URL_SFX;
        logOutUrl = loginUrl;
    }

    /**
     * Login for cloud management app through login jag
     *
     * @param userName login username
     * @param password login password
     * @return boolean value of login success or fail
     * @throws IOException
     */
    public boolean login(String userName, String password) throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "login");
        params.put("userName", userName);
        params.put("password", password);
        Map resultMap = HttpHandler.doPostHttps(loginUrl, params, "");
        sessionCookie = (String) resultMap.get(CloudIntegrationConstants.COOKIE);
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
            Map resultMap = HttpHandler.doPostHttps(logOutUrl, params, sessionCookie);
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

}

