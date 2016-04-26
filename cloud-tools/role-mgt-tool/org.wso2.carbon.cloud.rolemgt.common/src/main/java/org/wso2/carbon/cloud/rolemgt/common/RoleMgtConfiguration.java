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

package org.wso2.carbon.cloud.rolemgt.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model to represent the role-mgt.xml
 */
public class RoleMgtConfiguration {
    private Map<String, List<String>> configuration;

    /**
     * Constructor for RoleMgtConfiguration
     *
     * @param config Map of List of Strings representing role-mgt.xml
     */
    public RoleMgtConfiguration(Map<String, List<String>> config) {
        configuration = new HashMap<String, List<String>>();
        configuration.putAll(config);
    }

    /**
     * Method to get properties for a given key
     *
     * @param key
     * @return values String[]
     */
    public String[] getProperties(String key) {
        List<String> values = configuration.get(key);
        if (values == null) {
            return new String[0];
        }
        return values.toArray(new String[0]);
    }

    /**
     * Method to get properties for a given key
     *
     * @param key String
     * @return value String
     */
    public String getFirstProperty(String key) {
        List<String> value = configuration.get(key);
        if (value == null) {
            return null;
        }
        return value.get(0);
    }

    /**
     * Method to get all properties of configuration
     *
     * @return Map of List of Strings representing role-mgt.xml
     */
    public Map<String,List<String>> getAllProperties(){
        return configuration;
    }
}