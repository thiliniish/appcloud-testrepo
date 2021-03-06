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
package org.wso2.carbon.cloud.appcloudlistener.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.cloud.appcloudlistener.AppCloudListener;
import org.wso2.carbon.cloud.listener.CloudListener;

/**
 * @scr.component name="appcloud.listener.serviceComponent"" immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.base.api.ServerConfigurationService"
 * cardinality="1..1" policy="dynamic" bind="setServerConfigurationService"
 * unbind="unsetServerConfigurationService"
 */
public class AppCloudListenerComponent {
    private static final Log log = LogFactory.getLog(AppCloudListenerComponent.class);
    private ServiceRegistration serviceRegistration;

    /**
     * Method to activate bundle.
     *
     * @param context OSGi component context.
     */
    protected void activate(ComponentContext context) {
        log.info("Activating App Cloud listener component");
        AppCloudListener appCloudListener = new AppCloudListener();
        serviceRegistration = context.getBundleContext().registerService(CloudListener.class.getName(),
                appCloudListener, null);
    }

    /**
     * Method to deactivate bundle.
     *
     * @param context OSGi component context.
     */
    protected void deactivate(ComponentContext context) {
        log.info("Deactivating App Cloud listener component");
        serviceRegistration.unregister();
    }

    protected void setServerConfigurationService(ServerConfigurationService ghostMetaArtifactsLoader){

    }

    protected void unsetServerConfigurationService(ServerConfigurationService ghostMetaArtifactsLoader){

    }
}
