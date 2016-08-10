/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.cloud.tenantdeletion.reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.tenantdeletion.beans.DeleteJob;
import org.wso2.carbon.cloud.tenantdeletion.conf.ConfigurationsType;
import org.wso2.carbon.cloud.tenantdeletion.constants.DeletionConstants;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

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
     * Returns configuration reader instance, if  instance is null creates an instance
     *
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

    /**
     * Returns ConfigurationType instance
     *
     * @return ConfigurationType instance
     */
    public ConfigurationsType getConfiguration() {
        return configuration;
    }

    /**
     * Sets configurationType object
     *
     * @return Configuration
     */
    private ConfigurationsType loadConfigurationConfig() {
        ConfigurationsType configurationType = null;
        try {
            String carbonHome = CarbonUtils.getCarbonHome() + File.separator;
            File inputFile = new File(carbonHome + DeletionConstants.TENANT_DELETION_XML_FILE_PATH);
            // Un-marshaling Tenant Deletion configuration
            JAXBContext cdmContext = JAXBContext.newInstance(ConfigurationsType.class);
            Unmarshaller unmarshaller = cdmContext.createUnmarshaller();
            configurationType = (ConfigurationsType) unmarshaller.unmarshal(inputFile);
        } catch (JAXBException e) {
            // When tenant-deletion file is not in the correct location, a warning log will be printed without throwing
            // an error
            LOG.warn("Tenant-deletion.xml file is missing");
        }
        return configurationType;
    }

    /**
     * Returns tenant deletion object List
     *
     * @return DeleteJob object array
     */
    public List<DeleteJob> getDeletionData() {
        List<DeleteJob> list = new ArrayList<>();
        List deleterList = configuration.getDeletionOrder().getDeleter();
        if (!deleterList.isEmpty()) {
            for (int i = 0; i < deleterList.size(); i++) {
                DeleteJob object = new DeleteJob();
                object.setClassName(configuration.getDeletionOrder().getDeleter().get(i).getClazz());
                object.setDependency(configuration.getDeletionOrder().getDeleter().get(i).getDependency());
                object.setServerKey(configuration.getDeletionOrder().getDeleter().get(i).getServerKey());
                list.add(object);
            }
        } else {
            String errorMsg = "There is no deleter in XML file";
            LOG.error(errorMsg);
        }
        return list;
    }
}
