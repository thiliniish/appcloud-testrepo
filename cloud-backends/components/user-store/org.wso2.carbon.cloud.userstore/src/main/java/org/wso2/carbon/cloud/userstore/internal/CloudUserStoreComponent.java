/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.cloud.userstore.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cloud.userstore.CloudUserStoreManager;
import org.wso2.carbon.cloud.userstore.WSO2CloudUserStoreManager;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="cloud.user.store.manager.component" immediate=true
 * @scr.reference name="realm.service"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 */
public class CloudUserStoreComponent {
    private static final Log LOGGER = LogFactory.getLog(CloudUserStoreComponent.class);

    private ServiceRegistration cloudUserStore;
    private ServiceRegistration wso2CloudUserStore;

    protected void activate(ComponentContext context) {
        UserStoreManager cloudUserStoreManager = new CloudUserStoreManager();
        cloudUserStore = context.getBundleContext().registerService(UserStoreManager.class.getName(), cloudUserStoreManager, null);

        UserStoreManager wSO2CloudUserStoreManager = new WSO2CloudUserStoreManager();
        wso2CloudUserStore = context.getBundleContext().registerService(UserStoreManager.class.getName(), wSO2CloudUserStoreManager, null);

        LOGGER.info("CloudUserStoreComponent bundle activated successfully.");
    }

    protected void deactivate(ComponentContext context) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("CloudUserStoreComponent is deactivated ");
        }
        this.cloudUserStore.unregister();
        this.wso2CloudUserStore.unregister();
    }

    protected void setRealmService(RealmService realmService) {
        ServiceDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        ServiceDataHolder.getInstance().setRealmService(null);
    }
}
