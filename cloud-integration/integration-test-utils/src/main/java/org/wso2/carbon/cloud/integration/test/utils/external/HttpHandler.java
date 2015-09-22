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

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.wso2.carbon.cloud.integration.test.utils.CloudIntegrationConstants;
import org.wso2.carbon.cloud.integration.test.utils.CloudIntegrationTestUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is used as a http client
 */
public class HttpHandler {

    public static String cookie = null;

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
     * This method is use get a html file for given url
     *
     * @param url Web page url
     * @return response
     * @throws java.io.IOException Throws this when failed to retrieve web page
     */
    public static String getHtml(String url) throws IOException {
        HttpClient httpclient = HttpClientBuilder.create().build();
        HttpGet httpget = new HttpGet(url);
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();
        InputStream content = entity.getContent();
        BufferedReader in = new BufferedReader(new InputStreamReader(content));
        StringBuilder responseBuffer = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            responseBuffer.append(line);
        }
        return responseBuffer.toString();
    }

    /**
     * This method is used to do a https post request
     *
     * @param url         request url
     * @param payload     Content of the post request
     * @param authCookie  authCookie for authentication
     * @param contentType content type of the post request
     * @return response
     * @throws java.io.IOException - Throws this when failed to fulfill a https post request
     */
    public static String doPostHttps(String url, String payload, String authCookie,
                                     String contentType) throws IOException {
        URL obj = new URL(url);

        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        if (authCookie != null && !"".equals(authCookie)) {
            con.setRequestProperty("Cookie", authCookie);
        }
        if (contentType != null && !"".equals(contentType)) {
            con.setRequestProperty(HttpHeaders.CONTENT_TYPE, contentType);
        } else {
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
                cookie = sb.substring(0, sb.length() - 1);
            }
            return response.toString();
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
    public static String doPostHttps(String url, Map<String, String> params) throws IOException {
        String payLoad = mapToString(params);
        return doPostHttps(url, payLoad, cookie, MediaType.APPLICATION_FORM_URLENCODED);
    }

    /**
     * This method is used to do a http get request
     *
     * @param url                request url
     * @param trackingCode       tracking code of the web application
     * @param appmSamlSsoTokenId appmSamlSsoTokenId id of the web application
     * @param refer              web page url
     * @return response
     * @throws java.io.IOException Throws this when failed to fulfill a http get request
     */
    public static String doGet(String url, String trackingCode, String appmSamlSsoTokenId, String refer)
            throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        //add request header
        if (trackingCode.equals("")) {
            con.setRequestProperty("Accept",
                                   "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            con.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
            con.setRequestProperty("Cookie", "JSESSIONID=" + appmSamlSsoTokenId);
        } else {
            con.setRequestProperty("Cookie", "appmSamlSsoTokenId=" + appmSamlSsoTokenId);
            con.setRequestProperty("trackingCode", trackingCode);
            con.setRequestProperty("Referer", refer);
        }
        int responseCode = con.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
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
