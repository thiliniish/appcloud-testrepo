/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.cloud.rolemgt.tool.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cloud.rolemgt.common.RoleMgtConfiguration;
import org.wso2.carbon.core.ServerStartupHandler;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="org.wso2.carbon.rolemgt.tool"
 * @scr.reference name="realm.service"
 * interface="org.wso2.carbon.user.core.service.RealmService" cardinality="1..1"
 * policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="tenant.registryloader"
 * interface="org.wso2.carbon.registry.core.service.TenantRegistryLoader" cardinality="1..1"
 * policy="dynamic" bind="setTenantRegistryLoader" unbind="unsetTenantRegistryLoader"
 * @scr.reference name="rolemgt.configuration"
 * interface="org.wso2.carbon.cloud.rolemgt.common.RoleMgtConfiguration"
 * cardinality="1..1" policy="dynamic"
 * bind="setRoleMgtConfiguration"
 * unbind="unsetRoleMgtConfiguration"
 */

public class RoleManagerComponent {
    private static final Log log = LogFactory.getLog(RoleManagerComponent.class);

    /**
     * Method to activate bundle.
     *
     * @param context OSGi component context.
     */
    protected void activate(ComponentContext context) {
        try{
            //Register the server start up handler which hold the execution of its invoke method until the server starts
            context.getBundleContext()
                    .registerService(ServerStartupHandler.class.getName(), new RoleManagerServerStartListener(), null);
            if (log.isDebugEnabled()) {
                log.debug("Role Manager bundle activated successfully.");
            }
        } catch (Throwable e) {
            log.error("Error while creating Role Manager bundle.", e);
        }
    }

    /**
     * Method to deactivate bundle.
     *
     * @param context OSGi component context.
     */
    protected void deactivate(ComponentContext context) {
        log.info("Role Manager bundle is deactivated.");
    }

    /**
     * Method to set realm service.
     *
     * @param realmService service to get tenant data.
     */
    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting RealmService.");
        }
        ServiceHolder.setRealmService(realmService);
    }

    /**
     * Method to unset realm service.
     *
     * @param realmService service to get tenant data.
     */
    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Unset Realm service");
        }
        ServiceHolder.setRealmService(null);
    }

    /**
     * Method to set tenant registry loader
     *
     * @param tenantRegLoader tenant registry loader
     */
    protected void setTenantRegistryLoader(TenantRegistryLoader tenantRegLoader) {
        if (log.isDebugEnabled()) {
            log.debug("Setting TenantRegistryLoader.");
        }
        ServiceHolder.setTenantRegLoader(tenantRegLoader);
    }

    /**
     * Method to unset tenant registry loader
     *
     * @param tenantRegLoader tenant registry loader
     */
    protected void unsetTenantRegistryLoader(TenantRegistryLoader tenantRegLoader) {
        if (log.isDebugEnabled()) {
            log.debug("Unset Tenant Registry Loader");
        }
        ServiceHolder.setTenantRegLoader(null);
    }

    /**
     * Method to set role mgt configuration
     *
     * @param roleMgtConfiguration service to read role-mgt data
     */
    protected void setRoleMgtConfiguration(RoleMgtConfiguration roleMgtConfiguration) {
        ServiceHolder.setRoleMgtConfiguration(roleMgtConfiguration);
    }

    /**
     * Method to unset role mgt configuration
     *
     * @param roleMgtConfiguration service to read role-mgt data
     */
    protected void unsetRoleMgtConfiguration(RoleMgtConfiguration roleMgtConfiguration) {
        ServiceHolder.setRoleMgtConfiguration(null);
    }
}