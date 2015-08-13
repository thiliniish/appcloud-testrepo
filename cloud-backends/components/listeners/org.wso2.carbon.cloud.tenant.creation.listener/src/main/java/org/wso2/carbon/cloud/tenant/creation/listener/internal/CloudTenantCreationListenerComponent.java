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

package org.wso2.carbon.cloud.tenant.creation.listener.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cloud.common.CloudMgtConfiguration;
import org.wso2.carbon.cloud.tenant.creation.listener.CloudTenantCreationListener;
import org.wso2.carbon.securevault.SecretCallbackHandlerService;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="cloud.user.store.manager.dscomponent" immediate=true
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="cloud.configuration"
 * interface="org.wso2.carbon.cloud.common.CloudMgtConfiguration"
 * cardinality="1..1" policy="dynamic"
 * bind="setCloudMgtConfiguration"
 * unbind="unsetCloudMgtConfiguration"
 */
public class CloudTenantCreationListenerComponent {
    private static Log log = LogFactory.getLog(CloudTenantCreationListenerComponent.class);
    private static RealmService realmService;
    private static SecretCallbackHandlerService secretCallbackHandlerService;

    protected void activate(ComponentContext ctxt) {
        CloudTenantCreationListener listener = new CloudTenantCreationListener();
        ctxt.getBundleContext().registerService(TenantMgtListener.class.getName(), listener, null);

        log.info("CloudTenantCreationListenerComponent bundle activated successfully.");
    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("CloudTenantCreationListenerComponent is deactivated ");
        }
    }


    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("RealmService acquired");
        }
        ServiceHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        ServiceHolder.setRealmService(null);
    }

    protected void setCloudMgtConfiguration(CloudMgtConfiguration cloudMgtConfiguration) {
        ServiceHolder.setCloudMgtConfiguration(cloudMgtConfiguration);
    }

    protected void unsetCloudMgtConfiguration(CloudMgtConfiguration cloudMgtConfiguration) {
        ServiceHolder.setCloudMgtConfiguration(null);
    }
}
