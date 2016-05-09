/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.cloud.rolemgt.tool.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.rolemgt.tool.util.RoleManagerConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Configuration builder used to build role-mgt.xml file
 */
public class RoleMgtConfigurationBuilder {
    private static final Log log = LogFactory.getLog(RoleMgtConfigurationBuilder.class);
    private Map<String, List<String>> configurationMap = new HashMap<String, List<String>>();
    private File configFile;

    /**
     * Initialize the builder by providing the xml file location
     *
     * @param roleConfigFile
     */
    public RoleMgtConfigurationBuilder(File roleConfigFile) {
        this.configFile = roleConfigFile;
    }

    /**
     * Builds the role management configuration if the config File exists.
     */
    public void buildRoleMgtConfiguration() {
        if (configFile.exists()) {
            loadRoleMgtConfiguration(this.configFile);
        }
    }

    /**
     * Method to load RoleMgtConfiguration
     *
     * @param roleConfigFile File
     */
    private void loadRoleMgtConfiguration(File roleConfigFile) {
        OMElement roleMgtElement = loadXML(roleConfigFile);
        if (!RoleManagerConstants.CONFIG_NAMESPACE.equals(roleMgtElement.getNamespace().getNamespaceURI())) {
            String message = "Cloud namespace is invalid. Expected [" + RoleManagerConstants.CONFIG_NAMESPACE +
                    "], received [" + roleMgtElement.getNamespace() + "]";
            log.error(message);
        }
        Stack<String> nameStack = new Stack<String>();
        readChildElements(roleMgtElement, nameStack, null);
    }

    /**
     * Method to load XML
     *
     * @param configFile File
     * @return OMElement
     */
    private OMElement loadXML(File configFile) {
        InputStream inputStream = null;
        OMElement configXMLFile = null;
        try {
            inputStream = new FileInputStream(configFile);
            String xmlContent = IOUtils.toString(inputStream);
            configXMLFile = AXIOMUtil.stringToOM(xmlContent);
        } catch (IOException e) {
            String msg = "Unable to read the file at " + configFile.getAbsolutePath();
            log.error(msg, e);
        } catch (XMLStreamException e) {
            String msg =
                    "Error in parsing " + RoleManagerConstants.CONFIG_FILE_NAME + " at " + configFile.getAbsolutePath();
            log.error(msg, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                String msg = "Error in closing input stream ";
                log.error(msg, e);
            }
        }
        return configXMLFile;
    }

    /**
     * Method to read child elements
     *
     * @param serverConfig  OMElement
     * @param nameStack     Stack of String
     * @param configuration Map of String
     */
    private void readChildElements(OMElement serverConfig, Stack<String> nameStack, Map<String, String> configuration) {
        for (Iterator childElements = serverConfig.getChildElements(); childElements.hasNext(); ) {
            OMElement element = (OMElement) childElements.next();
            nameStack.push(element.getLocalName());
            String nameAttribute = element.getAttributeValue(new QName("name"));
            if (nameAttribute != null && nameAttribute.trim().length() != 0) {
                // some name attribute exists
                String key = getKey(nameStack);
                addToConfiguration(key, nameAttribute.trim(), configuration);
                // all child element will be having this attribute as part of their name
                nameStack.push(nameAttribute.trim());
            }
            String enabledAttribute = element.getAttributeValue(new QName("enabled"));
            if (enabledAttribute != null && enabledAttribute.trim().length() != 0) {
                String key = getKey(nameStack) + ".Enabled";
                addToConfiguration(key, enabledAttribute.trim(), configuration);
            }
            String text = element.getText();
            if (text != null && text.trim().length() != 0) {
                String key = getKey(nameStack);
                String value = replaceSystemProperty(text.trim());
                addToConfiguration(key, value, configuration);
            }
            readChildElements(element, nameStack, configuration);
            // If we had a named attribute, we have to pop that out
            if (nameAttribute != null && nameAttribute.trim().length() != 0) {
                nameStack.pop();
            }
            nameStack.pop();
        }
    }

    /**
     * Method to get Key given the nameStack
     *
     * @param nameStack Stack of String
     * @return String
     */
    private String getKey(Stack<String> nameStack) {
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < nameStack.size(); i++) {
            String name = nameStack.elementAt(i);
            key.append(name).append(".");
        }
        key.deleteCharAt(key.lastIndexOf("."));
        return key.toString();
    }

    /**
     * Method to replace System Property
     *
     * @param text String
     * @return String
     */
    private String replaceSystemProperty(String text) {
        int indexOfStartingChars = -1;
        int indexOfClosingBrace;
        // The following condition deals with properties.
        // Properties are specified as ${system.property},
        // and are assumed to be System properties
        while (indexOfStartingChars < text.indexOf("${") && (indexOfStartingChars = text.indexOf("${")) != -1 &&
                (indexOfClosingBrace = text.indexOf('}')) != -1) { // Is a property used?
            // Get the system property name
            String sysProp = text.substring(indexOfStartingChars + 2, indexOfClosingBrace);
            // Resolve the system property name to a value
            String propValue = System.getProperty(sysProp);
            // If the system property is carbon home and is relative path,
            // we have to resolve it to absolute path
            if (sysProp.equals("carbon.home") && propValue != null && propValue.equals(".")) {
                propValue = new File(".").getAbsolutePath() + File.separator;
            }
            // Replace the system property with valid value
            if (propValue != null) {
                text = text.substring(0, indexOfStartingChars) + propValue + text.substring(indexOfClosingBrace + 1);
            }
        }
        return text;
    }

    /**
     * Method to add key-value pairs to configuration map
     *
     * @param key   String
     * @param value String
     */
    private void addToConfiguration(String key, String value) {
        List<String> list = configurationMap.get(key);
        if (list == null) {
            list = new ArrayList<String>();
            list.add(value);
            configurationMap.put(key, list);
        } else {
            if (!list.contains(value)) {
                list.add(value);
            }
        }
    }

    /**
     * Method to add key-value pairs to configuration
     *
     * @param key           String
     * @param value         String
     * @param configuration Map of String
     */
    private void addToConfiguration(String key, String value, Map<String, String> configuration) {
        if (configuration == null) {
            addToConfiguration(key, value);
        } else {
            if (configuration.get(key) == null) {
                configuration.put(key, value);
            }
        }
    }

    /**
     * Method to get properties from the configuration map for a given key
     *
     * @param key
     * @return String[]
     */
    public String[] getProperties(String key) {
        List<String> values = configurationMap.get(key);
        if (values == null) {
            return new String[0];
        }
        return values.toArray(new String[0]);
    }

    /**
     * Method to get properties from the configuration map for a given key
     *
     * @param key String
     * @return String
     */
    public String getFirstProperty(String key) {
        List<String> value = configurationMap.get(key);
        if (value == null) {
            return null;
        }
        return value.get(0);
    }

}
