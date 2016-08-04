/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.cloud.tenantdeletion.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cloud.tenantdeletion.listeners.CloudUserOperationListener;
import org.wso2.carbon.cloud.tenantdeletion.listeners.CustomServerStartupHandler;
import org.wso2.carbon.cloud.tenantdeletion.listeners.TenantLoaderObserver;
import org.wso2.carbon.core.ServerStartupHandler;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="wso2.carbon.cloud.tenantdeletion" immediate="true"
 * @scr.reference name="realm.service"
 * interface="org.wso2.carbon.user.core.service.RealmService" cardinality="1..1"
 * policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService
 * @scr.reference name="tenant.registryloader"
 * interface="org.wso2.carbon.registry.core.service.TenantRegistryLoader" cardinality="1..1"
 * policy="dynamic" bind="setTenantRegistryLoader" unbind="unsetTenantRegistryLoader"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */

public class TenantDeletionServiceComponent {
    private static final Log LOG = LogFactory.getLog(TenantDeletionServiceComponent.class);
    private ServiceRegistration userOperationListenerRef;
    private ServiceRegistration contextObserverRef;
    private ServiceRegistration serverStartupListnerRef;

    /**
     * Method to activate OSGi service component.
     *
     * @param context OSGi component context.
     */
    protected void activate(ComponentContext context) {
        BundleContext bundleContext = context.getBundleContext();
        try {
            CloudUserOperationListener cloudUserOperationListener = new CloudUserOperationListener();
            TenantLoaderObserver userStoreConfgurationContextObserver = new TenantLoaderObserver();
            this.userOperationListenerRef = bundleContext
                    .registerService(UserOperationEventListener.class.getName(), cloudUserOperationListener, null);
            this.contextObserverRef = bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(),
                                                                    userStoreConfgurationContextObserver, null);
            this.serverStartupListnerRef = bundleContext
                    .registerService(ServerStartupHandler.class.getName(), new CustomServerStartupHandler(), null);
        } catch (Exception e) {
            LOG.error("Failed to activate the Tenant Deletion service.", e);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Tenant Deletion service component activated successfully.");
        }
    }

    /**
     * Method to deactivate bundle.
     *
     * @param context OSGi component context.
     */
    protected void deactivate(ComponentContext context) {
        this.userOperationListenerRef.unregister();
        this.contextObserverRef.unregister();
        this.serverStartupListnerRef.unregister();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Tenant Deletion service component is deactivated ");
        }
    }

    /**
     * Method to set registry service.
     *
     * @param registryService service to get tenant data.
     */
    protected void setRegistryService(RegistryService registryService) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting RegistryService.");
        }
        ServiceHolder.getInstance().setRegistryService(registryService);
    }

    /**
     * Method to unset registry service.
     *
     * @param registryService service to get registry data.
     */
    protected void unsetRegistryService(RegistryService registryService) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Unset RegistryService.");
        }
        ServiceHolder.getInstance().setRegistryService(null);
    }

    /**
     * Method to set realm service.
     *
     * @param realmService service to get tenant data.
     */
    protected void setRealmService(RealmService realmService) {
        ServiceHolder.getInstance().setRealmService(realmService);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Realm service initialized");
        }
    }

    /**
     * Method to unset realm service.
     *
     * @param realmService service to get tenant data.
     */
    protected void unsetRealmService(RealmService realmService) {
        ServiceHolder.getInstance().setRealmService(null);
    }

    /**
     * Method to set tenant registry loader
     *
     * @param tenantRegLoader tenant registry loader
     */
    protected void setTenantRegistryLoader(TenantRegistryLoader tenantRegLoader) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting TenantRegistryLoader.");
        }
        ServiceHolder.getInstance().setTenantRegLoader(tenantRegLoader);
    }

    /**
     * Method to unset tenant registry loader
     *
     * @param tenantRegLoader tenant registry loader
     */
    protected void unsetTenantRegistryLoader(TenantRegistryLoader tenantRegLoader) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Unset TenantRegistryLoader.");
        }
        ServiceHolder.getInstance().setTenantRegLoader(null);
    }

    /**
     * Method to set configurationContextService
     *
     * @param contextService context service
     */
    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting the ConfigurationContext");
        }
        ServiceHolder.getInstance().setConfigurationContextService(contextService);
    }

    /**
     * Method to unset configurationContextService
     *
     * @param contextService context service
     */
    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Unsetting the ConfigurationContext");
        }
        ServiceHolder.getInstance().setConfigurationContextService(null);
    }
}