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

package org.wso2.carbon.cloud.signup.workflow.fileuploader.configreader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.cloud.signup.workflow.fileuploader.constants.Constants;

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
     */
    public static JSONObject getJsonConfigObject() throws ParseException, IOException {
        synchronized (ConfigFileReader.class) {

            String errorMessage;
            String fileName = Constants.CONFIG_FILE_NAME;
            String filePath = Constants.CARBON_HOME + "repository" + File.separator + "resources" +
                              File.separator + "bpelFileUploader" + File.separator + fileName;
            //if (jsonObjectInstance == null) {
            JSONParser parser = new JSONParser();
            String jsonString;
            Object jsonobject;
            BufferedReader reader = null;
            if (jsonObjectInstance == null) {
                try {
                    reader = new BufferedReader(
                            new InputStreamReader(new FileInputStream(filePath), Constants.CHARACTER_ENCODING));
                    String line = "";
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    jsonString = stringBuilder.toString();
                    jsonobject = parser.parse(jsonString);
                    jsonObjectInstance = (JSONObject) jsonobject;
                } catch (FileNotFoundException e) {
                    errorMessage = "The file " + fileName + " could not be located";
                    log.error(errorMessage, e);
                    throw new FileNotFoundException();
                } catch (IOException e) {
                    errorMessage = "Error reading file " + fileName + " could not be located";
                    log.error(errorMessage, e);
                    throw new IOException(errorMessage, e);
                } catch (ParseException e) {
                    errorMessage = "Error parsing the json file " + fileName + " could not be located";
                    log.error(errorMessage, e);
                    throw new ParseException(0, e);
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }
            }
            return jsonObjectInstance;
        }
    }

    /**
     * This methods returns the particular type of configuration needed to be retrieved from the config object
     *
     * @param configType is the main json attribute name of the json configuration file.
     * @return json object consisting of the specific configuration information requested
     */
    public static JSONObject retrieveConfigProperty(String configType, String tenantDomain)
            throws ParseException, IOException {
        String errorMessage;
        try {
            JSONObject configRetriever = ConfigFileReader.getJsonConfigObject();
            JSONObject configObject = null;
            configObject = (JSONObject) configRetriever.get(configType);
            return configObject;
        } catch (ParseException e) {
            errorMessage = "Error in getting the " + configType +
                           " from the configuration file for the tenant " + tenantDomain;
            log.error(errorMessage, e);
            throw new ParseException(0, e);
        } catch (IOException e) {
            errorMessage = "Error in getting the " + configType +
                           " from the configuration file for the tenant " + tenantDomain;
            log.error(errorMessage, e);
            throw new IOException(errorMessage, e);
        }
    }

    /**
     * This method retrieves a certain property name in the configuration file
     *
     * @param configType    is the main json attribute name
     * @param attributeName is the sub attribute of the json configuration file
     * @return the corresponding value for the requested attribute
     * @throws ParseException
     * @throws IOException
     */
    public static String retrieveConfigAttribute(String configType, String attributeName)
            throws ParseException, IOException {
        String errorMessage;
        String attribute;
        try {
            JSONObject configRetriever = ConfigFileReader.getJsonConfigObject();
            JSONObject configObject;
            configObject = (JSONObject) configRetriever.get(configType);
            attribute = configObject.get(attributeName).toString();
            return attribute;
        } catch (ParseException e) {
            errorMessage =
                    "Error in getting the " + configType + " and the attribute " + attributeName +
                    " from the configuration file";
            log.error(errorMessage, e);
            throw new ParseException(0, e);
        } catch (IOException e) {
            errorMessage =
                    "Error in getting the " + configType + " and the attribute " + attributeName +
                    " from the configuration file";
            log.error(errorMessage, e);
            throw new IOException(errorMessage, e);
        }
    }

}

