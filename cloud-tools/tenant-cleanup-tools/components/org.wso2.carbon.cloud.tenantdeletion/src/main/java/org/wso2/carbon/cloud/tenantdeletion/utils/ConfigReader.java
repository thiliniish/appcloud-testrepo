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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.cloud.tenantdeletion.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.tenantdeletion.utils.conf.ConfigurationsType;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Class to read xml configuration files (tenant_deletion.xml) using xpath
 */
public class ConfigReader {
    private static final Log LOG = LogFactory.getLog(ConfigReader.class);
    private static volatile ConfigReader instance;
    private ConfigurationsType configuration;

    private ConfigReader() {
        configuration = loadConfigurationConfig();
    }

    /**
     * Returs ConfigurationType instance
     * @return ConfigurationType instance
     */
    public ConfigurationsType getConfiguration() {
        return configuration;
    }

    /**
     * Sets configurationType object
     * @return Configuration
     */
    private ConfigurationsType loadConfigurationConfig() {
        ConfigurationsType configurationType = null;
        try {
            String CARBON_HOME = CarbonUtils.getCarbonHome() + File.separator;
            File inputFile = new File(CARBON_HOME + "repository/conf/tenant_deletion.xml");

            /* Un-marshaling Tenant Deletion configuration */
            JAXBContext cdmContext = JAXBContext.newInstance(ConfigurationsType.class);
            Unmarshaller unmarshaller = cdmContext.createUnmarshaller();
            configurationType = (ConfigurationsType) unmarshaller.unmarshal(inputFile);
        } catch (JAXBException e) {
            LOG.error("Error occurred while initializing Configuration config", e);
        }
        return configurationType;
    }

    /**
     * Returns cofig reader instance, if  instance is null creates an instance
     * @return Config reader
     */
    public static ConfigReader getInstance() {
        if (instance == null) {
            synchronized (ConfigReader.class) {
                if (instance == null) {
                    instance = new ConfigReader();
                }
            }
        }
        return instance;
    }
}