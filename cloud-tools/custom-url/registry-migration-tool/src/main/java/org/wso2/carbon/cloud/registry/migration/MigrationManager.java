/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.cloud.registry.migration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.cloud.registry.migration.conf.ConfigReader;
import org.wso2.carbon.cloud.registry.migration.util.RegistryManager;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.IOException;

/**
 * Migration Manager is used to migrate the custom url related configs to regions
 */
public class MigrationManager {

    private static final Log log = LogFactory.getLog(MigrationManager.class);
    ConfigReader configReader;
    RegistryManager registryManager;

    public MigrationManager(ConfigReader config) throws IOException, RegistryException {
        this.configReader = config;
        registryManager = new RegistryManager(configReader, RegistryMigrationConstants
                                                                    .AXIS2_CONF_FILE_PATH);
    }

    /**
     * Migrates the registry structure for the given list of tenants
     *
     * @param tenantList Comma separated list of tenants
     * @throws RegistryException
     * @throws IOException
     */
    void migrateRegistryResources(String tenantList) throws RegistryException, IOException {
        //Get tenant list
        String[] tenants = tenantList.split(",");
        String registryPath = this.configReader.getProperty("remoteregistry.path");
        try {
            if (registryManager.resourceExists(registryPath)) {
                for (String tenant : tenants) {
                    //Copy url mappings
                    String customPath = registryPath + tenant + "/urlMapping/" + configReader.getProperty("region") +
                                                "-" + tenant;
                    String defaultPath = registryPath + tenant + "/urlMapping/" + tenant;
                    if (registryManager.resourceExists(defaultPath)) {
                        registryManager.copyRegistryResource(defaultPath, customPath);
                    }
                    //Copy certificates
                    defaultPath = registryPath + tenant + "/securityCertificates/";
                    if (registryManager.resourceExists(defaultPath)) {
                        //Create region collection
                        registryManager.copyRegistryCollection(defaultPath, configReader.getProperty("region"));
                    }
                }
            }
        } catch (RegistryException ex) {
            String errorMessage =
                    "Error occurred when moving url mappings & certificates from default registry location.";
            log.error(errorMessage, ex);
            throw new RegistryException(errorMessage, ex);
        }
    }

}
