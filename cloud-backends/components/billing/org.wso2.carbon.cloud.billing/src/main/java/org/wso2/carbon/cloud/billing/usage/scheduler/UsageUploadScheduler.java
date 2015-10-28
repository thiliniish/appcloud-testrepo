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

package org.wso2.carbon.cloud.billing.usage.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.internal.ServiceDataHolder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Represent the usage upload scheduler which schedule daily usage uploading.
 */
public class UsageUploadScheduler {

    private static final Log LOGGER = LogFactory.getLog(UsageUploadScheduler.class);

    /**
     * Schedule usage upload task
     *
     * @param cron cron expression
     */
    public void invokeUsageUpload(String cron) {
        if (cron != null && !cron.isEmpty()) {
            TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo();
            triggerInfo.setCronExpression(cron);
            @SuppressWarnings("deprecation")
            TaskInfo info = new TaskInfo();

            info.setName(BillingConstants.USAGE_UPLOADER_TASK_NAME);
            info.setTriggerInfo(triggerInfo);
            info.setTaskClass(BillingConstants.USAGE_UPLOADER_TASK_CLASS_NAME);

            final int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

            Map<String, String> properties = new HashMap<String, String>();
            properties.put(BillingConstants.USAGE_UPLOADER_TASK_TENANT_ID_KEY, String.valueOf(tenantId));

            info.setProperties(properties);

            try {
                LOGGER.info("Registering Task " + BillingConstants.USAGE_UPLOADER_TASK_NAME);
                ServiceDataHolder.getInstance().getTaskManager(BillingConstants.USAGE_UPLOADER_TASK_CLASS_NAME)
                        .registerTask(info);
                ServiceDataHolder.getInstance().getTaskManager(BillingConstants.USAGE_UPLOADER_TASK_CLASS_NAME)
                        .rescheduleTask(info.getName());
            } catch (TaskException e) {
                LOGGER.error("Error while scheduling usage uploader task : " + info.getName() + " for tenant : " +
                             tenantId + "..", e);
            }
        }
    }

}
