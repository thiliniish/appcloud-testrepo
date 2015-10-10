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
package org.wso2.cloud.heartbeat.monitoring.ui.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.cloud.heartbeat.monitoring.ui.CloudStructure;
import org.wso2.cloud.heartbeat.monitoring.ui.configuration.Node;
import org.wso2.cloud.heartbeat.monitoring.ui.configuration.parser.nginx.NodeBuilder;
import org.wso2.cloud.heartbeat.monitoring.ui.configuration.parser.nginx.utils.Constants;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reading configuration files and created a singleton object
 */
public class ConfigReader {

    private static final Log log = LogFactory.getLog(ConfigReader.class);

    private Node rootNode;
    private String configurationPath;
    private boolean nodeStatus;
    private String timeInterval;

    /**
     * Map to structure cloud name with Cloud specific server objects from CloudStructure
     */
    private Map<String, CloudStructure> cloudStructureList = new HashMap<String, CloudStructure>();

    /**
     * Singleton instance for reading hearbeat.conf
     */
    private static ConfigReader instance = new ConfigReader();

    private ConfigReader() {
        this.rootNode = new Node();
    }

    public static ConfigReader getInstance() {
        return instance;
    }

    /**
     * Reading the heartbeat.conf file to retrieve main Node, default Time Interval,
     * setting up cloud Objects and populating them
     *
     * @param configPath path to get the configuration from
     * @throws HeartbeatException IOException to throw if the heartbeat.conf has any exception
     */
    public void buildConfigurationNode(String configPath) throws HeartbeatException {
        if (!nodeStatus) {
            try {
                log.info("Hearbeat Monitor: Started reading configuration");
                configurationPath = configPath;
                NodeBuilder.buildNode(rootNode, FileManager.readFile(configurationPath));
                convertTimeInterval(getDataSourceFromNode().getProperty(Constants.TIME_INTERVAL));
                setCloudObjects(rootNode.findChildNodeByName(Constants.CLOUDS_NODE));
                setCloudStructure(getModulesFromRootNode().getChildNodes());
                nodeStatus = true;
            } catch (IOException e) {
                log.error(Constants.IO_EXCEPTION + configurationPath, e);
                throw new HeartbeatException(Constants.IO_EXCEPTION, e);
            }
        }
    }

    /**
     * Getting database elements from the configuration
     *
     * @return node for database elements
     */

    public Node getDataSourceFromNode() {
        return rootNode.findChildNodeByName(Constants.DATA_SOURCE);
    }

    /**
     * Getting test modules to be shown on UI from the configration
     *
     * @return a parent node with servers as child nodes
     */

    public Node getModulesFromRootNode() {
        return rootNode.findChildNodeByName(Constants.MODULES_NODE);
    }

    /**
     * Getting admin credentials for authentication and login
     *
     * @return a node with admin credentials
     */

    public Node getAdminNode() {
        return rootNode.findChildNodeByName(Constants.ADMIN_NODE);
    }

    /**
     * Initiallizing CloudStrucure Objects for each cloud from the configuration specified clouds.
     */
    public void setCloudObjects(Node node) {
        log.info("Hearbeat Monitor: Setting up Cloud Objects");

        if (node.getProperty(Constants.CLOUDS_PROPERTY) == null ||
            node.getProperty(Constants.CLOUDS_PROPERTY).isEmpty()) {
            log.error(Constants.NO_CLOUDS);
        } else {
            String[] clouds = (node.getProperty(Constants.CLOUDS_PROPERTY)).split(",");
            cloudStructureList = new HashMap<String, CloudStructure>();
            for (String individualCloud : clouds) {
                cloudStructureList.put(StringConverter.underscoreToTitleCase(individualCloud),
                                       new CloudStructure(StringConverter.underscoreToTitleCase(individualCloud)));
            }
        }
    }

    /**
     * Adding servers to relevant cloud objects while reading cloud properties.
     */
    public void setCloudStructure(List<Node> nodeList) throws HeartbeatException {
        log.info("Hearbeat Monitor: Creating Cloud to Server Structure");
        for (Node node : nodeList) {
            if (node.getProperty(Constants.CLOUDS_PROPERTY) == null ||
                node.getProperty(Constants.CLOUDS_PROPERTY).isEmpty()) {
                log.error(Constants.NO_CLOUDS + StringConverter.underscoreToTitleCase(node.getName()));
            } else {
                String[] cloudsProperty = node.getProperty(Constants.CLOUDS_PROPERTY).split(",");
                for (String singleCloudName : cloudsProperty) {
                    CloudStructure cloudObject =
                            cloudStructureList.get(StringConverter.underscoreToTitleCase(singleCloudName));
                    String serviceNameInTitleCase = StringConverter.underscoreToTitleCase(node.getName());
                    cloudObject.putService(serviceNameInTitleCase);
                }
            }
        }
    }

    /**
     * @return cloud name to cloud strucure object Map
     */
    public Map<String, CloudStructure> getCloudStructure() {
        return cloudStructureList;
    }

    /**
     * Converting parsed time interval to minutes
     *
     * @param interval Expected time interval
     * @throws HeartbeatException
     */
    public void convertTimeInterval(String interval) throws HeartbeatException {
        if (interval.contains(Constants.DAY)) {
            timeInterval = Integer.toString(Integer.parseInt(interval.split(Constants.DAY)[0].replace(" ", "")));
        } else if (interval.contains(Constants.HOUR)) {
            timeInterval = Integer.toString(Integer.parseInt(interval.split(Constants.HOUR)[0].replace(" ", "")) / 24);
        } else if (interval.contains(Constants.MINUTE)) {
            timeInterval =
                    Integer.toString(Integer.parseInt(interval.split(Constants.MINUTE)[0].replace(" ", "")) / 1440);
        } else {
            timeInterval = "1";    //Default value 1 day = 1440 minutes
            log.info("Parsed invalid time interval for service uptime status :");
        }
    }

    /**
     * Get default time interval from the configuration
     *
     * @return time interval in days
     */
    public String getTimeInterval() {
        return timeInterval;
    }
}
