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

package org.wso2.carbon.cloud.user.manager.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cloud.user.manager.CloudUserManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="org.wso2.carbon.cloud.user.manager"
 *                immediate="true"
 * @scr.reference name="user.realmservice.default"
 *                interface="org.wso2.carbon.user.core.service.RealmService"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setRealmService"
 *                unbind="unsetRealmService"
 * @scr.reference name="configuration.context.service"
 *                interface="org.wso2.carbon.utils.ConfigurationContextService"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setConfigurationContextService"
 *                unbind="unsetConfigurationContextService"
 */
public class CloudUserManagerComponent {
    private static Log log = LogFactory.getLog(CloudUserManagerComponent.class);

    protected void activate(ComponentContext context) {
        context.getBundleContext().registerService(CloudUserManager.class.getName(), new CloudUserManager(), null);
        log.info("CloudUserManagerComponent activated successfully.");
    }

    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("CloudUserManagerComponent Service  bundle is deactivated ");
        }
    }

    protected void setRealmService(RealmService realmService) {
        if (realmService != null && log.isDebugEnabled()) {
            log.debug("Realm service initialized");
        }
        ServiceReferenceHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        ServiceReferenceHolder.setRealmService(null);
    }

    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        ServiceReferenceHolder.setConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
        ServiceReferenceHolder.setConfigurationContextService(null);
    }

}
