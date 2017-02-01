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

    private static final Log LOGGER = LogFactory.getLog(MigrationManager.class);
    private ConfigReader configReader;
    private RegistryManager registryManager;

    public MigrationManager(ConfigReader config) throws IOException, RegistryException {
        this.configReader = config;
        registryManager = new RegistryManager(configReader, RegistryMigrationConstants
                                                                    .AXIS2_CONF_FILE_PATH);
    }

    /**
     * Migrates the registry structure for the given list of tenants
     *
     * @param tenantList Comma separated list of tenants
     * @param regionList Comma separated list of regions
     * @throws RegistryException
     * @throws IOException
     */
    void migrateRegistryResources(String tenantList, String regionList) throws RegistryException, IOException {
        //Get tenant list
        String[] tenants = tenantList.split(",");
        String[] regions = regionList.split(",");
        String registryPath = this.configReader.getProperty("remoteregistry.path");
        String defaultRegion = this.configReader.getProperty("default.region");

        try {
            if (registryManager.resourceExists(registryPath)) {
                for (int i = 0; i < tenants.length; i++) {
                    String tenantDomain = tenants[i];
                    String region = regions[i];

                    //Copy url mappings
                    String customPath = registryPath + tenantDomain + "/urlMapping/" + region + "-" + tenantDomain;
                    String customPathDefaultRegion = registryPath + tenantDomain + "/urlMapping/" + defaultRegion + "-"
                                                             + tenantDomain;
                    String defaultPath = registryPath + tenantDomain + "/urlMapping/" + tenantDomain;
                    if (registryManager.resourceExists(defaultPath)) {
                        registryManager.copyRegistryResource(defaultPath, customPath);
                        //This will add a mapping to default region (For store)
                        if (!defaultRegion.equals(region)) {
                            registryManager.copyRegistryResource(defaultPath, customPathDefaultRegion);
                        }
                    }
                    //Copy certificates
                    defaultPath = registryPath + tenantDomain + "/securityCertificates/";
                    if (registryManager.resourceExists(defaultPath)) {
                        registryManager.copyRegistryCollection(defaultPath, region, defaultRegion);
                    }
                }
            } else {
                LOGGER.error("Aborting migration as registry path " + registryPath + " does not exist");
            }
        } catch (RegistryException ex) {
            String errorMessage =
                    "Error occurred when moving url mappings & certificates from default registry location.";
            LOGGER.error(errorMessage, ex);
            throw new RegistryException(errorMessage, ex);
        }
    }

}
