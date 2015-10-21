/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.cloud.integration.test.utils.external;

import org.apache.http.HttpHeaders;
import org.wso2.carbon.cloud.integration.test.utils.CloudIntegrationConstants;
import org.wso2.carbon.cloud.integration.test.utils.CloudIntegrationTestUtils;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is used as a http client
 */
public class HttpHandler {

    static {
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                String deploymentContext = CloudIntegrationTestUtils
                        .getPropertyValue(CloudIntegrationConstants.DEPLOYMENT_CONTEXT);
                return deploymentContext.equals("local");
            }
        });
    }

    /**
     * This method is used to do a https post request
     *
     * @param url        request url
     * @param params     Content of the post request
     * @param authCookie authCookie for authentication
     * @param headerMap     header list of the request
     * @return response and if cookie is null returns the cookie in a Map
     * @throws java.io.IOException - Throws this when failed to fulfill a https post request
     */
    public static Map<String, String> doPostHttps(String url, Map<String, String> params, String authCookie,
                                                  Map<String, String> headerMap) throws IOException {

        URL obj = new URL(url);
        String payload = mapToString(params);
        Map<String, String> responseMap = new HashMap<String, String>();

        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        if (authCookie != null && !"".equals(authCookie)) {
            con.setRequestProperty("Cookie", authCookie);
        }
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            con.setRequestProperty(entry.getKey(), entry.getValue());
        }
        if (headerMap.get(HttpHeaders.CONTENT_TYPE) == null) {
            con.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        }

        con.setRequestProperty(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.5");
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(payload);
        wr.flush();
        wr.close();
        int responseCode = con.getResponseCode();
        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            if (authCookie == null || authCookie.equals("")) {
                Map<String, List<String>> headers = con.getHeaderFields();
                List<String> cookies = headers.get("Set-Cookie");
                StringBuilder sb = new StringBuilder();
                for (String s : cookies) {
                    sb.append(s).append("; ");
                }
                responseMap.put(CloudIntegrationConstants.COOKIE, sb.substring(0, sb.length() - 1));
            }
            responseMap.put(CloudIntegrationConstants.RESPONSE, response.toString());
            return responseMap;
        }
        return null;
    }

    /**
     * This method is used to do posts for values which doesn't contain session id's media types.
     *
     * @param url    String url to do the post
     * @param params Map of parameters as to make the contents of the post
     * @return String reponse with the post value
     * @throws IOException Throws this when failed to fulfill a https post request
     */
    public static Map doPostHttps(String url, Map<String, String> params, String authCookie)
            throws IOException {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        return doPostHttps(url, params, authCookie, headers);
    }

    /**
     * Converts parameter maps to string
     *
     * @param params Map of parameters to be converted to String
     * @return String of parameters ready for execution
     */
    public static String mapToString(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        Set<String> keySet = params.keySet();
        for (String paramKey : keySet) {
            sb.append(paramKey).append("=").append(params.get(paramKey)).append("&");
        }
        return sb.substring(0, sb.length() - 1);
    }
}
