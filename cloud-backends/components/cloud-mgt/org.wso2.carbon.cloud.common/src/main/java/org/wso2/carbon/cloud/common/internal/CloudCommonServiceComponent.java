/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.cloud.common.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cloud.common.CloudMgtConfiguration;
import org.wso2.carbon.cloud.common.CloudMgtConfigurationBuilder;
import org.wso2.carbon.cloud.common.CloudMgtConstants;
import org.wso2.carbon.securevault.SecretCallbackHandlerService;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

/**
 * @scr.component name="cloud.user.store.manager.dscomponent" immediate=true
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="secret.callback.handler.service"
 * interface="org.wso2.carbon.securevault.SecretCallbackHandlerService"
 * cardinality="1..1" policy="dynamic"
 * bind="setSecretCallbackHandlerService" unbind="unsetSecretCallbackHandlerService"
 */
public class CloudCommonServiceComponent {
    private static Log log = LogFactory.getLog(CloudCommonServiceComponent.class);
    private static RealmService realmService;
    private static SecretCallbackHandlerService secretCallbackHandlerService;

    protected void activate(ComponentContext ctxt) {
        BundleContext bundleContext = ctxt.getBundleContext();
        CloudMgtConfiguration configuration;
        try {
            String fileLocation = CarbonUtils.getCarbonConfigDirPath() +
                    File.separator + CloudMgtConstants.CONFIG_FOLDER +
                    File.separator + CloudMgtConstants.CONFIG_FILE_NAME;
            configuration = new CloudMgtConfigurationBuilder(fileLocation).buildCloudMgtConfiguration();
            bundleContext.registerService(CloudMgtConfiguration.class.getName(), configuration, null);

            if (log.isDebugEnabled()) {
                log.debug("Cloud common bundle is activated");
            }
        } catch (Throwable e) {
            log.error("Error in creating cloud-mgt configuration", e);
        }

    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Cloud Common bundle is deactivated ");
        }
    }


    protected void setRealmService(RealmService rlmService) {
        ServiceHolder.setRealmService(rlmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        ServiceHolder.setRealmService(null);
    }

    protected void setSecretCallbackHandlerService(
            SecretCallbackHandlerService secretCallbackHandlerService) {
        if (log.isDebugEnabled()) {
            log.debug("SecretCallbackHandlerService acquired");
        }
        ServiceHolder.setSecretCallbackHandlerService(secretCallbackHandlerService);

    }

    protected void unsetSecretCallbackHandlerService(
            SecretCallbackHandlerService secretCallbackHandlerService) {
        ServiceHolder.setSecretCallbackHandlerService(null);
    }
}
