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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.io.File;

/**
 * @scr.component name="org.wso2.carbon.workflowComponent" immediate="true"
 * @scr.reference name="realm.service"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="configuration.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */

public class ApiManagerSelfSignUpComponent {

    private static final Log LOGGER = LogFactory.getLog(ApiManagerSelfSignUpComponent.class);
    String errorMessage;

    /**
     * This method retrieves the configurations needed to initialize the api manager configurations
     *
     * @param componentContext
     * @throws APIManagementException
     */
    protected void activate(ComponentContext componentContext) throws APIManagementException {

        try {

            LOGGER.info("Activating the API Manager Configurations");
            APIManagerConfiguration configuration = new APIManagerConfiguration();
            String filePath = CarbonUtils.getCarbonHome() + File.separator + "repository" +
                              File.separator + "conf" + File.separator + "api-manager.xml";
            configuration.load(filePath);
            APIManagerConfigurationServiceImpl configurationService =
                    new APIManagerConfigurationServiceImpl(configuration);
            ServiceReferenceHolder.getInstance()
                                  .setAPIManagerConfigurationService(configurationService);
            LOGGER.info("API Cloud Self Sign up feature bundle is activated successfully");

        } catch (APIManagementException e) {
            errorMessage = "Failed to load the API Manager configurations";
            LOGGER.error(errorMessage, e);
            throw new APIManagementException(errorMessage);
        } catch (Exception e) {
            errorMessage = "API Cloud Self Sign up feature bundle failed to activate";
            LOGGER.error(errorMessage, e);
        }

    }

    /**
     * This method deactivates the API Manager component
     *
     * @param componentContext
     */
    protected void deactivate(ComponentContext componentContext) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Deactivating API Cloud Self Sign up feature bundle");
        }
    }

    /**
     * Sets the configurations context service
     *
     * @param contextService
     */
    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        ServiceReferenceHolder.setContextService(contextService);
    }

    /**
     * Unsets the configuration context service.
     *
     * @param contextService
     */
    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        ServiceReferenceHolder.setContextService(null);
    }

    /**
     * Set realm service
     *
     * @param realmService realm service
     */
    protected void setRealmService(RealmService realmService) {
        if (realmService != null && LOGGER.isDebugEnabled()) {
            LOGGER.debug("Realm service initialized");
        }
        ServiceReferenceHolder.getInstance().setRealmService(realmService);
    }

    /**
     * remove realm service
     *
     * @param realmService realm service
     */
    protected void unsetRealmService(RealmService realmService) {
        ServiceReferenceHolder.getInstance().setRealmService(null);
    }
}
