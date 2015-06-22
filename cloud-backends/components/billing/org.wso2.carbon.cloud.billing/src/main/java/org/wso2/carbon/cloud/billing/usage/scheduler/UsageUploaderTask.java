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
package org.wso2.carbon.cloud.billing.usage.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.billing.common.BillingConstants;
import org.wso2.carbon.cloud.billing.common.CloudBillingException;
import org.wso2.carbon.cloud.billing.usage.CloudUsageManager;
import org.wso2.carbon.cloud.billing.utils.CloudBillingUtils;
import org.wso2.carbon.ntask.core.Task;

import java.util.Date;
import java.util.Map;

/**
 * Represents the Usage uploader task which execute usage uplaod tasks at a given time
 */
public class UsageUploaderTask implements Task {

    private static Log log = LogFactory.getLog(UsageUploaderTask.class);
    private Map<String, String> properties;

    public void init() {

    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;

    }

    public void execute() {
        log.info("Running usage uploader task " + BillingConstants.USAGE_UPLOADER_TASK_NAME + ". [" + new Date() + "]");
        try {
            boolean enableDailyUsageUpload =
                    CloudBillingUtils.getBillingConfiguration().getZuoraConfig().getUsageConfig()
                            .isEnableUsageUploading();

            if (enableDailyUsageUpload) {
                CloudUsageManager usageManager = new CloudUsageManager();
                usageManager.uploadDailyAPIUsage();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Daily usage will not be uploaded to zuora");
                }
            }
        } catch (CloudBillingException e) {
            log.error("Error occurred while uploading daily usage ", e);
        }
    }

}
