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

package org.wso2.carbon.cloud.billing.core.usage.apiusage;

import org.apache.commons.httpclient.NameValuePair;
import org.wso2.carbon.cloud.billing.core.beans.usage.AccountUsage;
import org.wso2.carbon.cloud.billing.core.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.core.commons.config.BillingConfigManager;
import org.wso2.carbon.cloud.billing.core.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.core.processor.BillingRequestProcessor;
import org.wso2.carbon.cloud.billing.core.processor.BillingRequestProcessorFactory;
import org.wso2.carbon.cloud.billing.core.usage.UsageProcessorContext;
import org.wso2.carbon.cloud.billing.core.usage.UsageProcessorFactory;
import org.wso2.carbon.cloud.billing.core.utils.CloudBillingServiceUtils;

/**
 * Represents the usage manager which does usage related operations
 */
public class APICloudUsageManager {

    private static String usageForTenantUrl =
            BillingConfigManager.getBillingConfiguration().getDataServiceConfig().getCloudBillingServiceUri() +
            BillingConstants.DS_API_URI_USAGE;
    private BillingRequestProcessor dsBRProcessor;

    public APICloudUsageManager() {
        BillingRequestProcessorFactory processorFactory = BillingRequestProcessorFactory.getInstance();
        dsBRProcessor = processorFactory.getBillingRequestProcessor(
                BillingRequestProcessorFactory.ProcessorType.DATA_SERVICE);
    }

    public AccountUsage[] getTenantUsageDataForGivenDateRange(String tenantDomain, String productName, String 
            startDate,
                                                              String endDate) throws CloudBillingException {
        // get accountId from tenant
        String accountId = CloudBillingServiceUtils.getAccountIdForTenant(tenantDomain);
        String response = getUsageForTenant(tenantDomain, startDate, endDate);

        UsageProcessorContext context = new UsageProcessorContext();
        context.setResponse(response);
        context.setAccountId(accountId);
        context.setStartDate(startDate);
        context.setEndDate(endDate);
        return UsageProcessorFactory.createUsageProcessor(UsageProcessorFactory.UsageProcessorType.API_CLOUD).process(
                context);
    }

    private String getUsageForTenant(String tenantDomain, String startDate, String endDate)
            throws CloudBillingException {
        NameValuePair[] nameValuePairs = new NameValuePair[] { new NameValuePair("apiPublisher", "%@" + tenantDomain),
                                                               new NameValuePair("startDate", startDate),
                                                               new NameValuePair("endDate", endDate) };
        return dsBRProcessor.doGet(usageForTenantUrl, null, nameValuePairs);
    }

}
