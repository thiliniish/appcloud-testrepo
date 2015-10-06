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

package org.wso2.carbon.cloud.billing.subscription.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.internal.ServiceDataHolder;
import org.wso2.carbon.cloud.billing.utils.CloudBillingUtils;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;

import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 */
public class BillingDbUpdateScheduler {

    private static final Log LOGGER = LogFactory.getLog(BillingDbUpdateScheduler.class);

    public void invokeBillingDbUpdateTask(String cron) {
        if (cron != null && !cron.isEmpty()) {
            TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo();
            triggerInfo.setCronExpression(cron);
            @SuppressWarnings("deprecation")
            TaskInfo info = new TaskInfo();

            info.setName(BillingConstants.BILLING_DB_UPDATE_TASK_NAME);
            info.setTriggerInfo(triggerInfo);
            info.setTaskClass(BillingConstants.BILLING_DB_UPDATE_TASK_CLASS_NAME);

            Map<String, String> properties = new HashMap<String, String>();

            String pendingDisableTenantsDSUrl = CloudBillingUtils.getBillingConfiguration().getDSConfig()
                    .getPendingDisableTenants();
            properties.put(BillingConstants.PENDING_DISABLES_URL_KEY, pendingDisableTenantsDSUrl);

            String disableTenantDSUrl = CloudBillingUtils.getBillingConfiguration().getDSConfig().getDisableTenant();
            properties.put(BillingConstants.DISABLE_TENANT_URL_KEY, disableTenantDSUrl);

            String updateSubscriptionDSUrl = CloudBillingUtils.getBillingConfiguration().getDSConfig()
                    .getSubscriptionStatus();
            properties.put(BillingConstants.UPDATE_SUBSCRIPTION_STATUS_URL_KEY, updateSubscriptionDSUrl);

            String billingHistoryDSUrl = CloudBillingUtils.getBillingConfiguration().getDSConfig().getBillingHistory();
            properties.put(BillingConstants.BILLING_HISTORY_URL_KEY, billingHistoryDSUrl);

            info.setProperties(properties);

            try {
                LOGGER.info("Registering Task " + BillingConstants.BILLING_DB_UPDATE_TASK_NAME);
                ServiceDataHolder serviceDataHolder = ServiceDataHolder.getInstance();

                serviceDataHolder.getTaskManager(BillingConstants.BILLING_DB_UPDATE_TASK_CLASS_NAME).registerTask(info);
                serviceDataHolder.getTaskManager(BillingConstants.BILLING_DB_UPDATE_TASK_CLASS_NAME).rescheduleTask
                        (info.getName());
            } catch (TaskException e) {
                LOGGER.error("Error while scheduling billing database update for disabled subscriptions task : " + info
                        .getName(), e);
            }
        }
    }
}
