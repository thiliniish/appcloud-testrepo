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

import org.wso2.carbon.appfactory.application.mgt.service.ApplicationInfoService;
import org.wso2.carbon.appfactory.tenant.mgt.service.TenantManagementService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * Represents the data holder for the app deletion component
 */
public class ServiceHolder {

    private ServiceHolder() {
    }

    //Realm Service which is used to get tenant data.
    private static RealmService realmService;
    //Tenant Registry loader which is used to load registry of the tenant
    private static TenantRegistryLoader tenantRegLoader;
    //ApplicationInfo service which is used to get application related information
    private static ApplicationInfoService appInfoService;
    //App Factory Tenant management service which is used to manage tenant related information
    private static TenantManagementService tenantManagementService;

    /**
     * Method to get TenantManagementService.
     *
     * @return TenantManagementService.
     */
    public static TenantManagementService getTenantManagementService() {
        return tenantManagementService;
    }

    /**
     * Method to set TenantManagementService.
     *
     * @param service tenantManagementService.
     */
    public static void setTenantManagementService(TenantManagementService service) {
        ServiceHolder.tenantManagementService = service;
    }

    /**
     * Method to get AppinfoService.
     *
     * @return appinfoService.
     */
    public static ApplicationInfoService getAppinfoService() {
        return appInfoService;
    }

    /**
     * Method to set ApplicationInfoService.
     *
     * @param service appinfoService.
     */
    public static void setAppinfoService(ApplicationInfoService service) {
        ServiceHolder.appInfoService = service;
    }

    /**
     * This method used to get RealmService.
     *
     * @return RealmService.
     */
    public static RealmService getRealmService() {
        return realmService;
    }

    /**
     * Method to set registry RealmService.
     *
     * @param service RealmService.
     */
    public static void setRealmService(RealmService service) {
        realmService = service;
    }

    /**
     * This method used to get TenantRegistryLoader
     *
     * @return tenantRegLoader  Tenant registry loader for load tenant registry
     */
    public static TenantRegistryLoader getTenantRegLoader() {
        return tenantRegLoader;
    }

    /**
     * This method used to set TenantRegistryLoader
     *
     * @param service Tenant registry loader for load tenant registry
     */
    public static void setTenantRegLoader(TenantRegistryLoader service) {
        tenantRegLoader = service;
    }

}
