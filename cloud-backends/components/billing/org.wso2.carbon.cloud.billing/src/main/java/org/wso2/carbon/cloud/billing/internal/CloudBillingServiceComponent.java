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
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.commons.config.BillingConfig;
import org.wso2.carbon.cloud.billing.service.CloudBillingService;
import org.wso2.carbon.cloud.billing.subscription.tasks.RemoveAssociatedRolesScheduler;
import org.wso2.carbon.cloud.billing.usage.scheduler.UsageUploadScheduler;
import org.wso2.carbon.cloud.billing.utils.CloudBillingUtils;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.securevault.SecretCallbackHandlerService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="org.wso2.carbon.cloud.billing"
 * immediate="true"
 * @scr.reference name="realm.service"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="ntask.component"
 * interface="org.wso2.carbon.ntask.core.service.TaskService"
 * cardinality="1..1" policy="dynamic" bind="setTaskService"
 * unbind="unsetTaskService"
 * @scr.reference name="secret.callback.handler.service"
 * interface="org.wso2.carbon.securevault.SecretCallbackHandlerService"
 * cardinality="1..1" policy="dynamic"
 * bind="setSecretCallbackHandlerService" unbind="unsetSecretCallbackHandlerService"
 */
public class CloudBillingServiceComponent {

    private static Log log = LogFactory.getLog(CloudBillingServiceComponent.class);
    private ServiceRegistration billingServiceRef;

    protected void activate(ComponentContext context) {
        BundleContext bundleContext = context.getBundleContext();
        BillingConfig configuration;

        try {
            if (log.isDebugEnabled()) {
                log.debug("Billing bundle activation is started");
            }
            configuration = CloudBillingUtils.getBillingConfiguration();

            this.billingServiceRef =
                    bundleContext.registerService(CloudBillingService.class.getName(), new CloudBillingService(), null);

            boolean enableDailyUsageUpload = configuration.getZuoraConfig().getUsageConfig().isEnableUsageUploading();
            if (enableDailyUsageUpload) {
                UsageUploadScheduler usageScheduler = new UsageUploadScheduler();
                String cronExpression = configuration.getZuoraConfig().getUsageConfig().getCron();
                usageScheduler.invokeUsageUpload(cronExpression);
            } else {
                log.warn("Usage uploader disabled");
            }
            boolean enableSubscriptionCleanUp = configuration.getZuoraConfig().getSubscriptionCleanUp().isEnabled();
            if (enableSubscriptionCleanUp) {
                RemoveAssociatedRolesScheduler removeAssociatedRolesScheduler = new RemoveAssociatedRolesScheduler();
                String cronExpression = configuration.getZuoraConfig().getSubscriptionCleanUp().getCron();

                removeAssociatedRolesScheduler.invokeRoleRemovalTask(cronExpression);
            } else {
                log.warn("Subscription cleanup disabled");
            }

            try {
                ServiceDataHolder.getInstance().getTaskService().registerTaskType(BillingConstants
                                                                                          .USAGE_UPLOADER_TASK_NAME);
            } catch (TaskException e) {
                log.error("Error in registering usage upload tas task type: " + e.getMessage(), e);
            }
            if (log.isDebugEnabled()) {
                log.debug("Cloud billing  bundle is activated");
            }
        } catch (Throwable e) {
            log.error("Error in creating cloud billing configuration", e);
        }
    }

    protected void deactivate(ComponentContext context) {
        this.billingServiceRef.unregister();
        if (log.isDebugEnabled()) {
            log.debug("DefaultRolesCreatorServiceComponent Service  bundle is deactivated ");
        }
    }

    protected void setTaskService(TaskService taskService) throws RegistryException {
        if (log.isDebugEnabled()) {
            log.debug("TaskService is acquired");
        }
        ServiceDataHolder.getInstance().setTaskService(taskService);
    }

    protected void unsetTaskService(TaskService taskService) {
        ServiceDataHolder.getInstance().setTaskService(null);
    }

    protected void setSecretCallbackHandlerService(SecretCallbackHandlerService secretCallbackHandlerService) {
        if (log.isDebugEnabled()) {
            log.debug("SecretCallbackHandlerService is acquired");
        }
        ServiceDataHolder.getInstance().setSecretCallbackHandlerService(secretCallbackHandlerService);
    }

    protected void unsetSecretCallbackHandlerService(SecretCallbackHandlerService secretCallbackHandlerService) {
        ServiceDataHolder.getInstance().setSecretCallbackHandlerService(null);
    }

    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("UserManagementService is acquired");
        }
        ServiceDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        ServiceDataHolder.getInstance().setRealmService(null);
    }
}