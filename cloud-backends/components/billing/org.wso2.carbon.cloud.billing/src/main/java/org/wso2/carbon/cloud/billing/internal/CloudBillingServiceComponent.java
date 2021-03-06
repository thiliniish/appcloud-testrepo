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
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.commons.config.BillingConfig;
import org.wso2.carbon.cloud.billing.commons.notifications.EmailNotifications;
import org.wso2.carbon.cloud.billing.commons.utils.BillingConfigUtils;
import org.wso2.carbon.cloud.billing.service.APICloudMonetizationService;
import org.wso2.carbon.cloud.billing.service.CloudBillingService;
import org.wso2.carbon.cloud.billing.subscription.tasks.BillingDbUpdateScheduler;
import org.wso2.carbon.cloud.billing.usage.scheduler.UsageUploadScheduler;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
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
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService
 * @scr.reference name="tenant.registryloader"
 * interface="org.wso2.carbon.registry.core.service.TenantRegistryLoader" cardinality="1..1"
 * policy="dynamic" bind="setTenantRegistryLoader" unbind="unsetTenantRegistryLoader"
 * @scr.reference name="event.output.adapter.service"
 * interface="org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService"
 * cardinality="1..1" policy="dynamic"  bind="setOutputEventAdapterService"
 * unbind="unsetOutputEventAdapterService"
 */
public class CloudBillingServiceComponent {

    private static final Log LOGGER = LogFactory.getLog(CloudBillingServiceComponent.class);
    private ServiceRegistration billingServiceRef;
    private ServiceRegistration apiMonServiceRef;

    /**
     * {@inheritDoc}
     *
     * @param context
     */
    protected void activate(ComponentContext context) {
        BundleContext bundleContext = context.getBundleContext();
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Billing bundle activation is started");
            }
            this.billingServiceRef = bundleContext.registerService(CloudBillingService.class.getName(), new
                    CloudBillingService(), null);
            this.apiMonServiceRef = bundleContext.registerService(APICloudMonetizationService.class.getName(), new
                    APICloudMonetizationService(), null);

            activateScheduledTasks();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Cloud billing  bundle is activated");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to activate the Cloud Billing service.", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param context
     */
    protected void deactivate(ComponentContext context) {
        EmailNotifications.getInstance().shutdownAndAwaitTermination();
        this.billingServiceRef.unregister();
        this.apiMonServiceRef.unregister();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Cloud billingService  bundle is deactivated ");
        }
    }

    /**
     * Set task service
     *
     * @param taskService task service
     * @throws RegistryException
     */
    protected void setTaskService(TaskService taskService) throws RegistryException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("TaskService is acquired");
        }
        ServiceDataHolder.getInstance().setTaskService(taskService);
    }

    /**
     * Remove task service
     *
     * @param taskService task service
     */
    protected void unsetTaskService(TaskService taskService) {
        ServiceDataHolder.getInstance().setTaskService(null);
    }

    /**
     * set secret callback handler service
     *
     * @param secretCallbackHandlerService secret callback handler service
     */
    protected void setSecretCallbackHandlerService(SecretCallbackHandlerService secretCallbackHandlerService) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("SecretCallbackHandlerService is acquired");
        }
        ServiceDataHolder.getInstance().setSecretCallbackHandlerService(
                secretCallbackHandlerService);
    }

    /**
     * remove secret callback handler service
     *
     * @param secretCallbackHandlerService secret callback handler service
     */
    protected void unsetSecretCallbackHandlerService(SecretCallbackHandlerService secretCallbackHandlerService) {
        ServiceDataHolder.getInstance().setSecretCallbackHandlerService(null);
    }

    /**
     * Set realm service
     *
     * @param realmService realm service
     */
    protected void setRealmService(RealmService realmService) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("UserManagementService is acquired");
        }
        ServiceDataHolder.getInstance().setRealmService(realmService);
    }

    /**
     * remove realm service
     *
     * @param realmService realm service
     */
    protected void unsetRealmService(RealmService realmService) {
        ServiceDataHolder.getInstance().setRealmService(null);
    }

    /**
     * Set registry service.
     *
     * @param registryService registry service
     */
    protected void setRegistryService(RegistryService registryService) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("RegistryService is acquired.");
        }
        ServiceDataHolder.getInstance().setRegistryService(registryService);
    }

    /**
     * Remove registry service.
     *
     * @param registryService registry service
     */
    protected void unsetRegistryService(RegistryService registryService) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Unset Registry service.");
        }
        ServiceDataHolder.getInstance().setRegistryService(null);
    }

    /**
     * Set tenant registry loader
     *
     * @param tenantRegLoader tenant registry loader
     */
    protected void setTenantRegistryLoader(TenantRegistryLoader tenantRegLoader) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("TenantRegistryLoader is aquired.");
        }
        ServiceDataHolder.getInstance().setTenantRegistryLoader(tenantRegLoader);
    }

    /**
     * Remove tenant registry loader
     *
     * @param tenantRegLoader tenant registry loader
     */
    protected void unsetTenantRegistryLoader(TenantRegistryLoader tenantRegLoader) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Unset Tenant Registry Loader.");
        }
        ServiceDataHolder.getInstance().setTenantRegistryLoader(null);
    }

    /**
     * Initialize the Output EventAdapter Service dependency
     *
     * @param outputEventAdapterService Output EventAdapter Service reference
     */
    protected void setOutputEventAdapterService(
            OutputEventAdapterService outputEventAdapterService) {
        ServiceDataHolder.getInstance().setOutputEventAdapterService(outputEventAdapterService);
    }

    /**
     * De-reference the Output EventAdapter Service dependency.
     *
     * @param outputEventAdapterService
     */
    protected void unsetOutputEventAdapterService(
            OutputEventAdapterService outputEventAdapterService) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Unset the Output Email Adapter service.");
        }
        ServiceDataHolder.getInstance().setOutputEventAdapterService(null);
    }

    /**
     * Gets the Output EventAdapter Service
     *
     * @return
     */
    public static OutputEventAdapterService getOutputEventAdapterService() {
        return ServiceDataHolder.getInstance().getOutputEventAdapterService();
    }

    /**
     * Activate scheduled tasks if billing enabled
     */
    private void activateScheduledTasks() {
        BillingConfig configuration = BillingConfigUtils.getBillingConfiguration();
        if (configuration.isBillingEnabled() && configuration.isMgtModeEnabled()) {

            boolean enableDailyUsageUpload = configuration.getZuoraConfig().getUsageConfig().isEnableUsageUploading();

            if (enableDailyUsageUpload) {
                UsageUploadScheduler usageScheduler = new UsageUploadScheduler();
                String cronExpression = configuration.getZuoraConfig().getUsageConfig().getCron();
                usageScheduler.invokeUsageUpload(cronExpression);
            } else {
                LOGGER.warn("Usage uploader disabled");
            }
            boolean enableSubscriptionCleanUp = configuration.getZuoraConfig().getSubscriptionCleanUp().isEnabled();
            if (enableSubscriptionCleanUp) {
                BillingDbUpdateScheduler billingDbUpdateScheduler = new BillingDbUpdateScheduler();
                String cronExpression = configuration.getZuoraConfig().getSubscriptionCleanUp().getCron();

                billingDbUpdateScheduler.invokeBillingDbUpdateTask(cronExpression);
            } else {
                LOGGER.warn("Subscription cleanup disabled");
            }

            registerUsageUploaderTask();
        } else if (!configuration.isBillingEnabled()) {
            LOGGER.warn("Billing disabled. billing related scheduler tasks will not get initialized");
        } else {
            LOGGER.info("Billing component mgt mode disabled");
        }
    }

    /**
     * Register user uploader task
     */
    private void registerUsageUploaderTask() {
        try {
            ServiceDataHolder.getInstance().getTaskService().registerTaskType(BillingConstants
                                                                                      .USAGE_UPLOADER_TASK_NAME);
        } catch (TaskException e) {
            LOGGER.error("Error in registering usage upload tas task type: " + e.getMessage(), e);
        }
    }
}
