/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.cloud.billing.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;
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

    private ServiceDataHolder() {
    }

    public static ServiceDataHolder getInstance() {
        return SERVICE_DATA_HOLDER;
    }

    public TaskService getTaskService() {
        return taskService;
    }

    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    public TaskManager getTaskManager(String taskName) {
        TaskService taskService = this.getTaskService();
        try {
            return taskService.getTaskManager(taskName);
        } catch (TaskException e) {
            LOGGER.error("Error while initializing TaskManager. ", e);
            return null;
        }
    }

    public SecretCallbackHandlerService getSecretCallbackHandlerService() {
        return secretCallbackHandlerService;
    }

    public void setSecretCallbackHandlerService(SecretCallbackHandlerService secretCallbackHandlerService) {
        this.secretCallbackHandlerService = secretCallbackHandlerService;
    }

    public RealmService getRealmService() {
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }
}