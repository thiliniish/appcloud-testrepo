/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.cloud.signup.internal;

import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.registry.core.service.RegistryService;

/**
 * This class consists of the getter and setter methods needed to access the configuration parameter
 */

public class ServiceReferenceHolder {

    private static ConfigurationContextService contextService;
    private APIManagerConfigurationService amConfigurationService;
    private RealmService realmService;
    private RegistryService registryService;

    private static final ServiceReferenceHolder instance = new ServiceReferenceHolder();

    public static ConfigurationContextService getContextService() {

        return contextService;
    }

    public static void setContextService(ConfigurationContextService contextService) {
        ServiceReferenceHolder.contextService = contextService;
    }

    public static ServiceReferenceHolder getInstance() {
        return instance;
    }

    public APIManagerConfigurationService getAPIManagerConfigurationService() {
        return amConfigurationService;
    }

    public void setAPIManagerConfigurationService(
            APIManagerConfigurationService amConfigurationService) {
        this.amConfigurationService = amConfigurationService;
    }

    /**
     * Get realm service
     *
     * @return RealmService
     */
    public RealmService getRealmService() {
        return realmService;
    }

    /**
     * Set realm service
     *
     * @param realmService RealmService
     */
    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    /**
     * Set registry service
     *
     * @param registryService registryService
     */
    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    /**
     * Get registry service
     *
     * @return registry service
     */
    public RegistryService getRegistryService() {
        return registryService;
    }
}