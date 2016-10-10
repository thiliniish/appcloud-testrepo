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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Represent a collection of {@link CloudType}
 */
@XmlRootElement(name = "CloudTypes")
public class CloudTypes {

    @XmlElement(name = "CloudType", required = true)
    private CloudType[] cloudTypes;

    private static volatile Map<String, CloudType> cloudTypeMap;

    private static final Log LOGGER = LogFactory.getLog(CloudTypes.class);

    public CloudType[] getAllCloudTypes() {
        return cloudTypes;
    }

    /**
     * Returns a {@link CloudType} with the given ID
     *
     * @param id Unique ID of the CloudType
     * @return a {@link CloudType}
     */
    public CloudType getCloudTypeById(String id) {
        if (cloudTypeMap == null) {
            initCloudTypesMap();
        }
        CloudType cloudType = cloudTypeMap.get(id);
        if (cloudType == null) {
            LOGGER.warn("CloudType with the id : " + id + " not found.");
        }
        return cloudType;
    }

    private void initCloudTypesMap() {
        synchronized (CloudTypes.class) {
            if (cloudTypeMap == null) {
                cloudTypeMap = new HashMap<>();
                for (CloudType type : cloudTypes) {
                    cloudTypeMap.put(type.getId(), type);
                }
            }
        }
    }

}
