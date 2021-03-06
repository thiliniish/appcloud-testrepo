/*
 *  Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.cloud.users.internal;

import org.wso2.carbon.registry.core.service.RegistryService;

/**
 * Holder Class for Service used within the service components
 */
public class ServiceHolder {

    private static final ServiceHolder instance = new ServiceHolder();
    private static RegistryService registryService;

    private ServiceHolder() {
    }

    public static ServiceHolder getInstance() {
        return instance;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static void setRegistryService(RegistryService registryService) {
        ServiceHolder.registryService = registryService;
    }

}
