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
package org.wso2.carbon.cloud.ssl.securitycomponent.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.ssl.securitycomponent.Constants;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigReader {
    private static final Log log = LogFactory.getLog(ConfigReader.class);
    private Map<String, String> configProperties = new HashMap<String, String>();

    public ConfigReader() throws IOException {

        try {
            init();
        } catch (IOException ex) {
            String errorMessage = "Error occurred when initializing config reader";
            log.error(errorMessage, ex);
            throw new IOException(errorMessage, ex);
        }
    }

    private void init() throws IOException {
        log.info("Reading configuration file...");
        Properties properties = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream(Constants.KEY_STORE_CONFIG_FILE);
            properties.load(input);
            Enumeration<?> e = properties.propertyNames();
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                String value = properties.getProperty(key);
                configProperties.put(key, value);
            }

        } catch (IOException ex) {
            String errorMessage = "Error while reading the config file";
            log.error(errorMessage, ex);
            throw new IOException(errorMessage, ex);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    String errorMessage = "Error closing the input stream while reading the config file";
                    log.error(errorMessage, e);
                }
            }
        }
        log.info("Reading configuration completed.");
    }

    public String getProperty(String key) {
        return configProperties.get(key);
    }
}
