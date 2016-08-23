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

package org.wso2.carbon.cloud.billing.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.securevault.SecretCallbackHandlerService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * Represent the data holder for the service component
 */
public class ServiceDataHolder {
    private static final ServiceDataHolder SERVICE_DATA_HOLDER = new ServiceDataHolder();
    private static final Log LOGGER = LogFactory.getLog(ServiceDataHolder.class);
    private TaskService taskService;
    private SecretCallbackHandlerService secretCallbackHandlerService;
    private RealmService realmService;
    private RegistryService registryService;
    private TenantRegistryLoader tenantRegistryLoader;
    private OutputEventAdapterService outputEventAdapterService;

    private ServiceDataHolder() {
    }

    /**
     * Get service data holder instance
     *
     * @return service data holder instance
     */
    public static ServiceDataHolder getInstance() {
        return SERVICE_DATA_HOLDER;
    }

    /**
     * Set registry service
     *
     * @param registryService registryService
     */
    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    /**
     * Get registry service
     *
     * @return registry service
     */
    public RegistryService getRegistryService() {
        return registryService;
    }

    /**
     * Set tenant registry loader
     *
     * @param tenantRegistryLoader registry loader
     */
    public void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        this.tenantRegistryLoader = tenantRegistryLoader;
    }

    /**
     * Get tenant registry loader
     *
     * @return tenant registry loader
     */
    public TenantRegistryLoader gerTenantRegistryLoader() {
        return tenantRegistryLoader;
    }

    /**
     * Get task service
     *
     * @return TaskService
     */
    public TaskService getTaskService() {
        return taskService;
    }

    /**
     * Set task service
     *
     * @param taskService task service
     */
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Get task manager
     *
     * @param taskName task name
     * @return task manager
     */
    public TaskManager getTaskManager(String taskName) {
        try {
            return this.taskService.getTaskManager(taskName);
        } catch (TaskException e) {
            LOGGER.error("Error while initializing TaskManager. ", e);
            return null;
        }
    }

    /**
     * Get secret callback handler service
     *
     * @return SecretCallbackHandlerService
     */
    public SecretCallbackHandlerService getSecretCallbackHandlerService() {
        return secretCallbackHandlerService;
    }

    /**
     * Set secret callback handler service
     *
     * @param secretCallbackHandlerService service available
     */
    public void setSecretCallbackHandlerService(SecretCallbackHandlerService secretCallbackHandlerService) {
        this.secretCallbackHandlerService = secretCallbackHandlerService;
    }

    /**
     * Get realm service
     *
     * @return RealmService
     */
    public RealmService getRealmService() {
        return realmService;
    }

    /**
     * Set realm service
     *
     * @param realmService RealmService
     */
    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    /**
     * Set Output EventAdapter Service reference
     *
     * @param outputEventAdapterService Output EventAdapter Service reference
     */
    public void setOutputEventAdapterService(OutputEventAdapterService outputEventAdapterService) {
        this.outputEventAdapterService = outputEventAdapterService;
    }

    /**
     * Get Output EventAdapter Service reference
     *
     * @return Output EventAdapter Service reference
     */
    public OutputEventAdapterService getOutputEventAdapterService() {
        return this.outputEventAdapterService;
    }
}
