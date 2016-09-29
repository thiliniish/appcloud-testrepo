/*
 * Copyright 2005-2015 WSO2, Inc. http://www.wso2.org
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.cloud.nginx.vhost.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.nginx.vhost.NginxVhostConstants;
import org.wso2.carbon.cloud.nginx.vhost.conf.ConfigReader;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

/**
 * Class used to manage registry resources which are related to custom URL
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

    private void init() throws AxisFault, RegistryException {
        try {
            System.setProperty("javax.net.ssl.trustStore", NginxVhostConstants.TRUST_STORE_LOCATION);
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

    public Resource getResourceFromRegistry(String path) throws RegistryException {
        try {
            return registry.get(path);
        } catch (RegistryException ex) {
            String errorMessage = "Registry Exception thrown when checking existance of resource : " + ex.getMessage();
            log.error(errorMessage);
            throw new RegistryException(errorMessage, ex);
        }
    }

}
