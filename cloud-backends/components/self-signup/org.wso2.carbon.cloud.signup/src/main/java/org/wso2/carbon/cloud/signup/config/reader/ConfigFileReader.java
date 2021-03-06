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
package org.wso2.carbon.cloud.signup.config.reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.cloud.signup.constants.SignUpWorkflowConstants;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * The ConfigFileReader class reads the configurations related to the self sign up workflow
 */

public class ConfigFileReader {

    private static final Log log = LogFactory.getLog(ConfigFileReader.class);
    private static volatile JSONObject jsonObjectInstance = null;

    // private constructor
    private ConfigFileReader() {
    }

    /**
     * This method reads the json configuration file from the system
     *
     * @return jsonObjectInstance after reading the config file
     * @throws WorkflowException
     */
    public static JSONObject getJsonConfigObject() throws WorkflowException {
        String errorMessage;
        String fileName = SignUpWorkflowConstants.CONFIG_FILE_NAME;
        String configFolder = SignUpWorkflowConstants.CONFIG_FILE_LOCATION;
        BufferedReader reader = null;
        if (jsonObjectInstance == null) {
            JSONParser parser = new JSONParser();
            Object jsonObject;
            String jsonString;
            try {
                String filePath = CarbonUtils.getCarbonHome() + File.separator + "repository" +
                                  File.separator + "resources" +
                                  File.separator + configFolder + File.separator +
                                  fileName;
                if (log.isDebugEnabled()) {
                    log.debug(" The file path of the signup configuration is " + filePath);
                }
                reader = new BufferedReader(
                        new InputStreamReader(new FileInputStream(filePath),
                                              SignUpWorkflowConstants.DEFAULT_ENCODING));
                String line = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                jsonString = stringBuilder.toString();
                jsonObject = parser.parse(jsonString);
                jsonObjectInstance = (JSONObject) jsonObject;
            } catch (FileNotFoundException e) {
                errorMessage = "The file " + fileName + " could not be located";
                log.error(errorMessage, e);
                throw new WorkflowException(errorMessage, e);
            } catch (IOException e) {
                errorMessage = "An error occurred while reading the file " + fileName;
                log.error(errorMessage, e);
                throw new WorkflowException(errorMessage, e);
            } catch (ParseException e) {
                errorMessage = "Error parsing the json file " + fileName;
                log.error(errorMessage, e);
                throw new WorkflowException(errorMessage, e);
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    errorMessage = "An error occurred while closing the input reader";
                    throw new WorkflowException(errorMessage, e);
                }
            }
        }
        return jsonObjectInstance;
    }

    /**
     * This method retrieves a certain property name in the configuration file
     *
     * @param configType    is the main json attribute name
     * @param attributeName is the sub attribute of the json configuration file
     * @return the corresponding value for the requested attribute
     * @throws ParseException
     * @throws java.io.IOException
     */
    public static String retrieveConfigAttribute(String configType, String attributeName)
            throws WorkflowException {
        String errorMessage;
        String attribute;

        try {

            JSONObject configRetriever = ConfigFileReader.getJsonConfigObject();
            JSONObject configObject = null;
            configObject = (JSONObject) configRetriever.get(configType);
            attribute = configObject.get(attributeName).toString();
            return attribute;
        } catch (WorkflowException e) {
            errorMessage = "Error in getting the config type: " + configType;
            log.error(errorMessage, e);
            throw new WorkflowException(errorMessage, e);
        }
    }

}

