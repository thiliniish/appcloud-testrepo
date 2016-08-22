/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.cloud.common;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.common.internal.ServiceHolder;
import org.wso2.carbon.securevault.SecretManagerInitializer;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * Configuration builder used to build cloud-mgt.xml file
 */
public class CloudMgtConfigurationBuilder {
    private static final Log log = LogFactory.getLog(CloudMgtConfigurationBuilder.class);
    private SecretResolver secretResolver;
    private Map<String, List<String>> configurationMap = new HashMap<String, List<String>>();
    private File configFile;

    /**
     * Initialize the builder by providing the xml file location
     *
     * @param cloudMgtConfigFileLocation String value of the absolute path of the xml file
     * @throws CloudMgtException if the file is not there
     */
    public CloudMgtConfigurationBuilder(String cloudMgtConfigFileLocation) throws CloudMgtException {
        File cloudConfigFile = new File(cloudMgtConfigFileLocation);
        if (cloudConfigFile.exists()) {
            this.configFile = cloudConfigFile;
        } else {
            String msg = "The provided file " + cloudConfigFile.getAbsolutePath() + " does not " +
                         "exist ";
            log.error(msg);
            throw new CloudMgtException(msg);
        }
    }

    /**
     * If it's a cloud-mgt.xml it will give {@code CloudConfiguration}
     *
     * @return {@link CloudMgtConfiguration}
     * @throws CloudMgtException
     */
    public CloudMgtConfiguration buildCloudMgtConfiguration() throws CloudMgtException {
        return loadCloudMgtConfiguration(this.configFile);
    }

    private CloudMgtConfiguration loadCloudMgtConfiguration(File cloudConfigFile) throws CloudMgtException {
        OMElement cloudElement = loadXML(cloudConfigFile);

        // Initialize secure vault
        SecretManagerInitializer secretManagerInitializer = new SecretManagerInitializer();
        secretManagerInitializer.init();
        secretResolver = SecretResolverFactory.create(cloudElement, true);

        if (!CloudMgtConstants.CONFIG_NAMESPACE.equals(cloudElement.getNamespace().getNamespaceURI())) {
            String message = "Cloud namespace is invalid. Expected [" + CloudMgtConstants.CONFIG_NAMESPACE +
                             "], received [" + cloudElement.getNamespace() + "]";
            log.error(message);
            throw new CloudMgtException(message);
        }

        Stack<String> nameStack = new Stack<String>();
        readChildElements(cloudElement, nameStack, null);

        return new CloudMgtConfiguration(configurationMap);
    }

    /**
     * if the provided file is a xml file it can return map of name values in the xml
     *
     * @return Map of name values
     * @throws CloudMgtException if there is a problem in reading the file
     */
    public Map<String, String> loadConfigurationFile() throws CloudMgtException {
        OMElement xmlElement = loadXML(configFile);

        // Initialize secure vault
        SecretManagerInitializer secretManagerInitializer = new SecretManagerInitializer();
        secretManagerInitializer.init();
        secretResolver = SecretResolverFactory.create(xmlElement, true);

        Stack<String> nameStack = new Stack<String>();
        Map<String, String> configuration = new HashMap<String, String>();
        readChildElements(xmlElement, nameStack, configuration);

        return configuration;
    }

    private OMElement loadXML(File configFile) throws CloudMgtException {

        InputStream inputStream = null;
        OMElement configXMLFile = null;
        try {
            inputStream = new FileInputStream(configFile);
            String xmlContent = IOUtils.toString(inputStream);
            configXMLFile = AXIOMUtil.stringToOM(xmlContent);
        } catch (IOException e) {
            String msg = "Unable to read the file at " + configFile.getAbsolutePath();
            log.error(msg, e);
            throw new CloudMgtException(msg, e);
        } catch (XMLStreamException e) {
            String msg = "Error in parsing " + CloudMgtConstants.CONFIG_FILE_NAME;
            log.error(msg, e);
            throw new CloudMgtException(msg, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                String msg = "Error in closing stream ";
                log.error(msg, e);
            }
        }
        return configXMLFile;
    }

    private void readChildElements(OMElement serverConfig, Stack<String> nameStack, Map<String, String> configuration) {
        for (Iterator childElements = serverConfig.getChildElements(); childElements.hasNext(); ) {
            OMElement element = (OMElement) childElements.next();
            nameStack.push(element.getLocalName());

            secureVaultResolve(element);

            String nameAttribute = element.getAttributeValue(new QName("name"));
            if (nameAttribute != null && nameAttribute.trim().length() != 0) {
                // We have some name attribute
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

                // Check wither the value is secured using secure valut
                if (isProtectedToken(key)) {
                    value = getProtectedValue(key);
                }
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

    private String getKey(Stack<String> nameStack) {
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < nameStack.size(); i++) {
            String name = nameStack.elementAt(i);
            key.append(name).append(".");
        }
        key.deleteCharAt(key.lastIndexOf("."));

        return key.toString();
    }

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

    private boolean isProtectedToken(String key) {
        return secretResolver != null && secretResolver.isInitialized() &&
               secretResolver.isTokenProtected("Carbon." + key);
    }

    private String getProtectedValue(String key) {
        return secretResolver.resolve("Carbon." + key);
    }

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

    private void addToConfiguration(String key, String value, Map<String, String> configuration) {
        if (configuration == null) {
            addToConfiguration(key, value);
        } else {
            if (configuration.get(key) == null) {
                configuration.put(key, value);
            }
        }
    }

    private void secureVaultResolve(OMElement element) {
        String secretAliasAttr = element.getAttributeValue(
                new QName(CloudMgtConstants.SECURE_VAULT_NS, CloudMgtConstants.SECRET_ALIAS_ATTR_NAME));
        if (secretAliasAttr != null) {
            element.setText(loadFromSecureVault(secretAliasAttr));
        }
    }

    public synchronized String loadFromSecureVault(String alias) {
        if (secretResolver == null) {
            secretResolver = SecretResolverFactory.create((OMElement) null, false);
            secretResolver.init(ServiceHolder.getSecretCallbackHandlerService().getSecretCallbackHandler());
        }
        return secretResolver.resolve(alias);
    }

}
