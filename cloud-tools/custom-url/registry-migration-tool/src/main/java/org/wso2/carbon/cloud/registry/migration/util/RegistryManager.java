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
package org.wso2.carbon.cloud.registry.migration.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.registry.migration.RegistryMigrationConstants;
import org.wso2.carbon.cloud.registry.migration.conf.ConfigReader;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

/**
 * Manages registry resources which are related to custom URL
 */
public class RegistryManager {

    private static final String TRUST_STORE_PASSWORD = "wso2carbon";
    private static final String TRUST_STORE_TYPE = "JKS";
    private static Log log = LogFactory.getLog(RegistryManager.class);
    private String username;
    private String password;
    private String serverUrl;
    private String axis2Conf;
    private Registry registry;

    /**
     * Constructor for RegistryManager
     *
     * @param configReader ConfigurationReader
     * @param axis2Conf    Axis2 Configuration
     * @throws AxisFault
     * @throws RegistryException
     */
    public RegistryManager(ConfigReader configReader, String axis2Conf) throws AxisFault, RegistryException {
        log.info("Initializing Registry Manager.");
        username = configReader.getProperty("remoteregistry.usermane");
        password = configReader.getProperty("remoteregistry.password");
        serverUrl = configReader.getProperty("remoteregistry.url");
        this.axis2Conf = axis2Conf;
        try {
            init();
        } catch (AxisFault e) {
            String errorMessage = "Error occurred while initializing Registry Manager.";
            log.error(errorMessage, e);
            throw new AxisFault(errorMessage, e);
        } catch (RegistryException e) {
            String errorMessage = "Error occurred while initializing Registry Manager.";
            log.error(errorMessage, e);
            throw new RegistryException(errorMessage, e);
        }
    }

    /**
     * Initializes registry service client
     *
     * @throws AxisFault
     * @throws RegistryException
     */
    private void init() throws AxisFault, RegistryException {
        try {
            System.setProperty("javax.net.ssl.trustStore", RegistryMigrationConstants.TRUST_STORE_LOCATION);
            System.setProperty("javax.net.ssl.trustStorePassword", TRUST_STORE_PASSWORD);
            System.setProperty("javax.net.ssl.trustStoreType", TRUST_STORE_TYPE);
            ConfigurationContext configContext =
                    ConfigurationContextFactory.createConfigurationContextFromFileSystem(axis2Conf);
            registry = new WSRegistryServiceClient(serverUrl, username, password, configContext);
        } catch (AxisFault ex) {
            String errorMessage = "Error thrown when creating configuration context for registry";
            log.error(errorMessage, ex);
            throw new AxisFault(errorMessage, ex);
        } catch (RegistryException ex) {
            String errorMessage = "Error thrown when creating registry ";
            log.error(errorMessage, ex);
            throw new RegistryException(errorMessage, ex);
        }
    }

    /**
     * Check if resource exists in given location
     *
     * @param registryPath
     * @return
     * @throws RegistryException
     */
    public boolean resourceExists(String registryPath) throws RegistryException {
        try {
            if (registry.resourceExists(registryPath)) {
                return true;
            }
        } catch (RegistryException ex) {
            String errorMessage = "Registry Exception thrown when checking existance of resource : " + ex.getMessage();
            log.error(errorMessage);
            throw new RegistryException(errorMessage, ex);
        }

        return false;
    }

    /**
     * Retrieve resource from given location
     *
     * @param path
     * @return
     * @throws RegistryException
     */
    public Resource getResourceFromRegistry(String path) throws RegistryException {
        try {
            return registry.get(path);
        } catch (RegistryException ex) {
            String errorMessage = "Registry Exception thrown when checking existance of resource : " + ex.getMessage();
            log.error(errorMessage);
            throw new RegistryException(errorMessage, ex);
        }
    }

    /**
     * Copies a single registry resource
     *
     * @param defaultPath
     * @param customPath
     * @throws RegistryException
     */
    public void copyRegistryResource(String defaultPath, String customPath) throws RegistryException {
        try {
            if (!resourceExists(customPath)) {
                registry.copy(defaultPath, customPath);
            } else {
                log.info("Skipping copy from " + defaultPath + " to " + customPath + " - resource already exists");
                return;
            }
        } catch (RegistryException ex) {
            String errorMessage = "Registry Exception thrown when checking existence of resource : " + ex.getMessage();
            log.error(errorMessage);
            throw new RegistryException(errorMessage, ex);
        }
        log.info("Successfully copied " + defaultPath + " to " + customPath);
    }

    /**
     * Copies a collection of resources
     *
     * @param defaultPath
     * @param customRegion
     * @param defaultRegion
     * @throws RegistryException
     */
    public void copyRegistryCollection(String defaultPath, String customRegion, String defaultRegion) throws
            RegistryException {

        Collection certificateCollection = (Collection) getResourceFromRegistry(defaultPath);
        String region;
        for (int i = 0; i < certificateCollection.getChildCount(); i++) {
            String certificateCollectionElement = certificateCollection.getChildren()[i];

            //Get Gateway or store node collection
            Collection nodeCollection = (Collection) getResourceFromRegistry(certificateCollectionElement);
            String nodeName = certificateCollectionElement.substring(
                    certificateCollectionElement.lastIndexOf("/") + 1,
                    certificateCollectionElement.length());

            //Skip if already migrated
            if (customRegion.equals(nodeName)) {
                log.info(
                        "Skipping certificate copy as resource '" + certificateCollectionElement + "' already exists.");
                break;
            }
            if (RegistryMigrationConstants.STORE_TYPE.equals(nodeName)) {
                region = defaultRegion;
            } else {
                region = customRegion;
            }
            String nodePath = defaultPath + region + "/" + nodeName;
            //Create path for region if doesn't already exist
            if (!registry.resourceExists(defaultPath + region)) {
                registry.put(defaultPath + region, registry.newCollection());
            }
            //Add node path
            registry.put(nodePath, registry.newCollection());
            //Copy each resource to new location
            for (int j = 0; j < nodeCollection.getChildCount(); j++) {
                String nodeCollectionElement = nodeCollection.getChildren()[j];
                String fileName = nodeCollectionElement.substring(
                        nodeCollectionElement.lastIndexOf("/") + 1,
                        nodeCollectionElement.length());
                copyRegistryResource(nodeCollectionElement, nodePath + "/" + fileName);
            }
        }
    }

}
