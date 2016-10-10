/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.cloud.billing.core.commons.config.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import java.util.HashMap;
import java.util.Map;

/**
 * Plan
 */
@XmlRootElement(name = "Plan")
public class Plan {

    //id uniquely identifies cloud type ex: api_cloud, app_cloud
    @XmlAttribute(name = "id")
    private String id;

    @XmlAttribute(name = "name")
    private String name;

    @XmlElement(name = "Property")
    private Property[] properties;

    private static volatile Map<String, String> propertiesMap;

    private static final Log LOGGER = LogFactory.getLog(Plan.class);

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getAllProperties() {
        if (propertiesMap == null) {
            initPropertyMap();
        }
        return propertiesMap;
    }

    /**
     * Returns the value of the property with the given name
     *
     * @param name name of the property
     * @return Value of the property
     */
    public String getProperty(String name) {
        if (propertiesMap == null) {
            initPropertyMap();
        }
        String propertyValue = propertiesMap.get(name);
        if (propertyValue == null) {
            LOGGER.warn("Property with the name : " + name + " not found.");
        }
        return propertyValue;
    }

    private void initPropertyMap() {
        synchronized (Plan.class) {
            if (propertiesMap == null) {
                propertiesMap = new HashMap<>();
                for (Property property : properties) {
                    propertiesMap.put(property.getName(), property.getValue());
                }
            }
        }
    }

    /**
     * Property
     */
    @XmlRootElement(name = "Property")
    public static class Property {

        @XmlAttribute(name = "name")
        private String name;

        @XmlValue
        private String value;

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }
}
