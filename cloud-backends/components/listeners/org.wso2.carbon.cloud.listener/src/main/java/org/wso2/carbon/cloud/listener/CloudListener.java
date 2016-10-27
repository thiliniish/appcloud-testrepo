/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.wso2.carbon.cloud.listener;

import org.wso2.carbon.cloud.common.CloudMgtException;

import java.util.HashMap;

/**
 * Represents the interface containing methods to be implmented by each cloud.
 */
public interface CloudListener {

    /**
     * Method to invoke actions to be executed after setting the custom url.
     *
     * @param parameterMap map with parameters required to invoke actions specific for each cloud
     * @throws CloudMgtException
     */
    void triggerOnCustomUrlAdded(HashMap<String, String> parameterMap) throws CloudMgtException;

}
