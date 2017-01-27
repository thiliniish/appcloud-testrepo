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
import org.apache.log4j.PropertyConfigurator;
import org.wso2.carbon.cloud.registry.migration.conf.ConfigReader;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.IOException;

/**
 * Main class to run the registry migration tool
 */
public class Agent {
    private static final String MOVE_MODE = "-Move";
    private static Log log = LogFactory.getLog(Agent.class);

    private Agent() {
    }

    public static void main(String[] args) {
        initialize();
        try {
            PropertyConfigurator.configure(RegistryMigrationConstants.LOG4J_PROPERTY_PATH);
            ConfigReader configReader = new ConfigReader();
            MigrationManager vHostManager = new MigrationManager(configReader);
            if (args.length > 0 && MOVE_MODE.equals(args[0])) {
                vHostManager.migrateRegistryResources(args[1], args[2]);
            }
        } catch (RegistryException | IOException e) {
            String errorMessage = "Error occurred when starting the Domain-Mapping agent";
            log.error(errorMessage, e);
            System.exit(1);
        }
    }

    /**
     * Initialize security properties
     */
    public static void initialize() {
        System.setProperty("javax.net.ssl.trustStore", RegistryMigrationConstants.KEY_STORE_FILE_PATH);
        System.setProperty("javax.net.ssl.keyStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.keyStoreType", "JKS");
        System.setProperty("carbon.repo.write.mode", "true");
    }

}
