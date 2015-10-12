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
package org.wso2.carbon.cloud.billing.usage.apiusage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.billing.beans.usage.AccountUsage;
import org.wso2.carbon.cloud.billing.beans.usage.Usage;
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.commons.config.BillingConfig;
import org.wso2.carbon.cloud.billing.commons.utils.BillingConfigUtils;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.processor.BillingRequestProcessor;
import org.wso2.carbon.cloud.billing.processor.BillingRequestProcessorFactory;
import org.wso2.carbon.cloud.billing.usage.UsageProcessor;
import org.wso2.carbon.cloud.billing.usage.UsageProcessorContext;
import org.wso2.carbon.cloud.billing.usage.UsageProcessorFactory;
import org.wso2.carbon.cloud.billing.usage.apiusage.utils.APIUsageProcessorUtil;
import org.wso2.carbon.cloud.billing.usage.util.UsageCSVParser;
import org.wso2.carbon.cloud.billing.utils.CloudBillingUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Represents the usage manager which does usage related operations
 */
public class APICloudUsageManager {

    private static final Log LOGGER = LogFactory.getLog(APICloudUsageManager.class);
    private BillingRequestProcessor dsBRProcessor;
    private BillingRequestProcessor zuoraBRProcessor;

    public APICloudUsageManager() {
        BillingConfig billingConfig = BillingConfigUtils.getBillingConfiguration();
        dsBRProcessor =
                BillingRequestProcessorFactory.getBillingRequestProcessor(
                        BillingRequestProcessorFactory.ProcessorType.DATA_SERVICE,
                        billingConfig.getDSConfig().getHttpClientConfig());
        zuoraBRProcessor =
                BillingRequestProcessorFactory.getBillingRequestProcessor(
                        BillingRequestProcessorFactory.ProcessorType.ZUORA,
                        billingConfig.getZuoraConfig().getHttpClientConfig());
    }


    private String getDailyUsage() throws CloudBillingException {
        String url = BillingConfigUtils.getBillingConfiguration().getDSConfig().getRequestCount();
        Date currentDate = new Date();

        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        String queryString = "year=" + cal.get(Calendar.YEAR) + "&month=" + (cal.get(Calendar.MONTH) + 1) + "&day=" +
                             cal.get(Calendar.DAY_OF_MONTH);
        url = url + "?" + queryString;
        return dsBRProcessor.doGet(url);
    }

    private String getUsageForTenant(String tenantDomain, String startDate, String endDate)
            throws CloudBillingException {
        String url = BillingConfigUtils.getBillingConfiguration().getDSConfig().getUsage();
        url = url + "?apiPublisher=%25@" + tenantDomain + "&startDate=" + startDate + "&endDate=" + endDate;
        return dsBRProcessor.doGet(url);
    }


    public void uploadDailyAPIUsage() throws CloudBillingException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(BillingConstants.DATE_FOMAT);
        LOGGER.info("Uploading daily usage for  " + dateFormat.format(new Date(System.currentTimeMillis())));
        // get today;s usage from data services and create a usage array
        String response = getDailyUsage();
        Usage[] usageArr = APIUsageProcessorUtil.getDailyUsageDataForApiM(response);
        if (usageArr.length > 0) {
            // write them in to a CSV array
            UsageCSVParser.writeCSVData(usageArr);
            zuoraBRProcessor.doUpload();
        }
    }

    public AccountUsage[] getTenantUsageDataForGivenDateRange(String tenantDomain, String productName,
                                                              String startDate, String endDate)
            throws CloudBillingException {
        // get accountId from tenant
        String accountId = CloudBillingUtils.getAccountIdForTenant(tenantDomain);
        String response = getUsageForTenant(tenantDomain, startDate, endDate);

        UsageProcessorContext context = new UsageProcessorContext();
        context.setResponse(response);
        context.setAccountId(accountId);

        if (accountId == null) {
            return UsageProcessorFactory.createUsageProcessor(
                    UsageProcessorFactory.UsageProcessorType.DEFAULT).process(context);
        }
        context.setStartDate(startDate);
        context.setEndDate(endDate);

        UsageProcessorFactory.UsageProcessorType type =
                UsageProcessorFactory.UsageProcessorType.valueOf(productName);
        UsageProcessor processor = UsageProcessorFactory.createUsageProcessor(type);
        return processor.process(context);
    }

}
