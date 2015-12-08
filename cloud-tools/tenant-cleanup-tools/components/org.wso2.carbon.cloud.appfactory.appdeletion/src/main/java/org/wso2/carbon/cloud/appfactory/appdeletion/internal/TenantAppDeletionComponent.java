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
package org.wso2.carbon.cloud.appfactory.appdeletion.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appfactory.application.mgt.service.ApplicationInfoService;
import org.wso2.carbon.appfactory.tenant.mgt.service.TenantManagementService;
import org.wso2.carbon.core.ServerStartupHandler;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="org.wso2.carbon.appfactory.appdeletion"
 * @scr.reference name="realm.service"
 * interface="org.wso2.carbon.user.core.service.RealmService" cardinality="1..1"
 * policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="tenant.registryloader"
 * interface="org.wso2.carbon.registry.core.service.TenantRegistryLoader" cardinality="1..1"
 * policy="dynamic" bind="setTenantRegistryLoader" unbind="unsetTenantRegistryLoader"
 * @scr.reference name="tenant.tenantManagementService"
 * interface="org.wso2.carbon.appfactory.tenant.mgt.service.TenantManagementService" cardinality="1..1"
 * policy="dynamic" bind="setTenantManagementService" unbind="unsetTenantManagementService"
 * @scr.reference name="application.applicationInfoService"
 * interface="org.wso2.carbon.appfactory.application.mgt.service.ApplicationInfoService" cardinality="1..1"
 * policy="dynamic" bind="setApplicationInfoService" unbind="unsetApplicationInfoService"
 */

/**
 * Represents the tenant app deletion component
 */
public class TenantAppDeletionComponent {
    private static final Log log = LogFactory.getLog(TenantAppDeletionComponent.class);

    /**
     * Method to activate bundle.
     *
     * @param context OSGi component context.
     */
    protected void activate(ComponentContext context) {
        //register the server start up handler which hold the execution of its invoke method until the server starts
        context.getBundleContext()
                .registerService(ServerStartupHandler.class.getName(), new AppDeletionServerStartListener(), null);
        log.info("Tenant App Deletion bundle activated successfully.");
    }

    /**
     * Method to deactivate bundle.
     *
     * @param context OSGi component context.
     */
    protected void deactivate(ComponentContext context) {
        log.info("Tenant App Deletion bundle is deactivated.");
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
     * Method to set applicationInfoService
     *
     * @param applicationInfoService application info service
     */
    protected void setApplicationInfoService(ApplicationInfoService applicationInfoService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting ApplicationInfo Service.");
        }
        ServiceHolder.setAppinfoService(applicationInfoService);
    }

    /**
     * Method to unset applicationInfoService
     *
     * @param applicationInfoService application info service
     */
    protected void unsetApplicationInfoService(ApplicationInfoService applicationInfoService) {
        if (log.isDebugEnabled()) {
            log.debug("Unset ApplicationInfo Service.");
        }
        ServiceHolder.setAppinfoService(null);
    }

    /**
     * Method to set tenantManagementService
     *
     * @param tenantManagementService tenant management service
     */
    protected void setTenantManagementService(TenantManagementService tenantManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Set TenantManagementService");
        }
        ServiceHolder.setTenantManagementService(tenantManagementService);
    }

    /**
     * Method to unset tenantManagementService
     *
     * @param tenantManagementService tenant management service
     */
    protected void unsetTenantManagementService(TenantManagementService tenantManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Unset TenantManagementService");
        }
        ServiceHolder.setTenantManagementService(null);
    }
}
