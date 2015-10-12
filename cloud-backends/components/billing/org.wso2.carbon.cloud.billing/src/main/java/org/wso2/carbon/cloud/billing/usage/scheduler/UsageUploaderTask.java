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
import org.wso2.carbon.cloud.billing.commons.utils.BillingConfigUtils;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.usage.apiusage.APICloudUsageManager;
import org.wso2.carbon.ntask.core.Task;

import java.util.Date;
import java.util.Map;

/**
 * Represents the Usage uploader task which execute usage uplaod tasks at a given time
 */
public class UsageUploaderTask implements Task {

    private static final Log LOGGER = LogFactory.getLog(UsageUploaderTask.class);
    private Map<String, String> properties;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        //No initialization requirement
    }

    /**
     * {@inheritDoc}
     *
     * @param properties
     */
    @Override
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() {
        LOGGER.info("Running usage uploader task " + BillingConstants.USAGE_UPLOADER_TASK_NAME + ". [" + new Date() +
                    "]");
        try {
            boolean enableDailyUsageUpload = BillingConfigUtils.getBillingConfiguration().getZuoraConfig()
                    .getUsageConfig().isEnableUsageUploading();

            if (enableDailyUsageUpload) {
                APICloudUsageManager usageManager = new APICloudUsageManager();
                usageManager.uploadDailyAPIUsage();
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Daily usage will not be uploaded to zuora");
                }
            }
        } catch (CloudBillingException e) {
            LOGGER.error("Error occurred while uploading daily usage ", e);
        }
    }

}
