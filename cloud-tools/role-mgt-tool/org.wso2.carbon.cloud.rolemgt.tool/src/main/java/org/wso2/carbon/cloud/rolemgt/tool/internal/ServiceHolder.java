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

import org.wso2.carbon.cloud.rolemgt.common.RoleMgtConfiguration;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * Represents the data holder for the role manager component
 */
public class ServiceHolder {

    private ServiceHolder() {}

    //Role configuration which is used to get role update info
    private static RoleMgtConfiguration roleConfiguration;
    //Tenant Registry loader which is used to load registry of a tenant
    private static TenantRegistryLoader tenantRegLoader;
    //Realm Service which is used to get tenant data.
    private static RealmService realmService;

    /**
     * Method to get Role Configuration
     *
     * @return RoleMgtConfiguration
     */
    public static RoleMgtConfiguration getRoleConfiguration() {
        return roleConfiguration;
    }

    /**
     * Method to set Role Configuration
     *
     * @param roleConfiguration
     */
    public static void setRoleMgtConfiguration(RoleMgtConfiguration roleConfiguration) {
        ServiceHolder.roleConfiguration = roleConfiguration;
    }

    /**
     * Method used to get RealmService.
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
     * Method used to get TenantRegistryLoader
     *
     * @return tenantRegLoader  Tenant registry loader for load tenant registry
     */
    public static TenantRegistryLoader getTenantRegLoader() {
        return tenantRegLoader;
    }

    /**
     * Method used to set TenantRegistryLoader
     *
     * @param service Tenant registry loader for load tenant registry
     */
    public static void setTenantRegLoader(TenantRegistryLoader service) {
        tenantRegLoader = service;
    }

}