/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.cloud.signup.workflow.fileUploader.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.cloud.signup.workflow.fileUploader.configReader.ConfigFileReader;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class contains all util methods needed for the file uploading process
 */
public class Utils {
    private static final Log log = LogFactory.getLog(Utils.class);
    private static HashMap<String, String> tenantSpecificUrlMap = new HashMap<String, String>();

    /**
     * This method return the tenant domain for the particular user name
     *
     * @param username is the username of the tenant
     * @return the tenant Domain
     */
    public String getTenantDomain(String username) {
        String tenantDomain = MultitenantUtils.getTenantDomain(username);
        return tenantDomain;
    }

    /**
     * Sets a map with the tenant specific data that will be replaced within the files.
     *
     * @param tenantDomain is the domain of the tenant
     * @throws ParseException
     * @throws IOException
     */
    public static void setTenantSpecificUrls(String tenantDomain) throws ParseException, IOException {
        String errorMessage;
        try {

            String workflowServiceAddress =
                    ConfigFileReader.retrieveConfigAttribute("configUrls",
                                                             "WORKFLOW_SERVICE_ADDRESS");
            String serviceUrl = workflowServiceAddress + "/services/t/" + tenantDomain +
                                "/UserApprovalService";
            String serviceCallbackUrl = workflowServiceAddress + "/services/t/" + tenantDomain +
                                        "/UserApprovalServiceCB";
            String gatewayCallbackUrl = ConfigFileReader.retrieveConfigAttribute("configUrls",
                                                                                 "GATEWAY_CALLBACK_URL");
            String adminUsername = ConfigFileReader.retrieveConfigAttribute("adminUser",
                                                                            "ADMIN_USERNAME");
            String adminPassword = ConfigFileReader.retrieveConfigAttribute("adminUser",
                                                                            "ADMIN_PASSWORD");

            tenantSpecificUrlMap.put("SERVICEURL", serviceUrl);
            tenantSpecificUrlMap.put("SERVICEURLCB", serviceCallbackUrl);
            tenantSpecificUrlMap.put("GATEWAYCALLBACK", gatewayCallbackUrl);
            tenantSpecificUrlMap.put("ADMINUSERNAME", adminUsername);
            tenantSpecificUrlMap.put("ADMINPASSWORD", adminPassword);

        } catch (ParseException parseException) {
            errorMessage =
                    "An error occurred while parsing the configuration file for the self sign up feature for the " +
                    "tenant " +
                    tenantDomain;
            log.error(errorMessage, parseException);
            throw new ParseException(0, parseException);
        } catch (IOException ioException) {
            errorMessage =
                    "An error occurred while reading the parsed the configuration file for the self sign up feature " +
                    "for the tenant " +
                    tenantDomain;
            log.error(errorMessage, ioException);
            throw new IOException(errorMessage, ioException);
        }
    }

    /**
     * This method is used to get the list of tenant specific properties
     *
     * @return the tenant specific url map
     */
    public HashMap<String, String> getTenantSpecificUrlMap() {
        return tenantSpecificUrlMap;
    }

    public String replaceUrls(String textFileContent) {
        Map<String, String> tenantSpecificUrlMap = getTenantSpecificUrlMap();
        String value;
        String key;
        for (Map.Entry<String, String> entry : tenantSpecificUrlMap.entrySet()) {
            value = entry.getValue();
            key = entry.getKey();
            if (value != null) {
                textFileContent = textFileContent.replace(key, value);
            }
        }
        return textFileContent;
    }

    /**
     * Replaces the tenant specific urls with the original placeholders.
     *
     * @param textFileContent is the file where the urls need replacing
     * @return the original file content
     */
    public String revertReplaceUrls(String textFileContent) {
        Map<String, String> tenantSpecificUrlMap = getTenantSpecificUrlMap();
        String value;
        String key;
        for (Map.Entry<String, String> entry : tenantSpecificUrlMap.entrySet()) {
            value = entry.getValue();
            key = entry.getKey();
            if (log.isDebugEnabled()) {
                log.debug("replacing " + value + " with key " + key);
            }
            if (value != null) {
                textFileContent = textFileContent.replace(value, key);
            }
        }
        return textFileContent;
    }
}



