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

package org.wso2.carbon.cloud.billing.usage.apiusage;

import org.apache.commons.httpclient.NameValuePair;
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
import org.wso2.carbon.cloud.billing.usage.UsageProcessorContext;
import org.wso2.carbon.cloud.billing.usage.UsageProcessorFactory;
import org.wso2.carbon.cloud.billing.usage.apiusage.utils.APIUsageProcessorUtil;
import org.wso2.carbon.cloud.billing.usage.util.UsageCSVParser;
import org.wso2.carbon.cloud.billing.utils.CloudBillingServiceUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Represents the usage manager which does usage related operations
 */
public class APICloudUsageManager {

    private static final Log LOGGER = LogFactory.getLog(APICloudUsageManager.class);
    private static String dailyUsageUrl =
            BillingConfigUtils.getBillingConfiguration().getDSConfig().getCloudBillingServiceUrl()
                    + BillingConstants.DS_API_URI_REQUEST_COUNT;
    private static String usageForTenantUrl =
            BillingConfigUtils.getBillingConfiguration().getDSConfig().getCloudBillingServiceUrl()
                    + BillingConstants.DS_API_URI_USAGE;

    private BillingRequestProcessor dsBRProcessor;
    private BillingRequestProcessor zuoraBRProcessor;

    public APICloudUsageManager() {
        BillingConfig billingConfig = BillingConfigUtils.getBillingConfiguration();
        dsBRProcessor = BillingRequestProcessorFactory
                .getBillingRequestProcessor(BillingRequestProcessorFactory.ProcessorType.DATA_SERVICE,
                        billingConfig.getDSConfig().getHttpClientConfig());
        zuoraBRProcessor = BillingRequestProcessorFactory
                .getBillingRequestProcessor(BillingRequestProcessorFactory.ProcessorType.ZUORA,
                        billingConfig.getZuoraConfig().getHttpClientConfig());
    }

    private String getDailyUsage() throws CloudBillingException {

        Date currentDate = new Date();

        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        NameValuePair[] nameValuePairs = new NameValuePair[] {
                new NameValuePair("year", String.valueOf(cal.get(Calendar.YEAR))),
                new NameValuePair("month", String.valueOf((cal.get(Calendar.MONTH) + 1))),
                new NameValuePair("day", String.valueOf(cal.get(Calendar.DAY_OF_MONTH))) };

        return dsBRProcessor.doGet(dailyUsageUrl, nameValuePairs);
    }

    private String getUsageForTenant(String tenantDomain, String startDate, String endDate)
            throws CloudBillingException {
        NameValuePair[] nameValuePairs = new NameValuePair[] { new NameValuePair("apiPublisher", "%25@" + tenantDomain),
                new NameValuePair("startDate", startDate), new NameValuePair("endDate", endDate) };
        return dsBRProcessor.doGet(usageForTenantUrl, nameValuePairs);
    }

    public void uploadDailyAPIUsage() throws CloudBillingException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(BillingConstants.DATE_FORMAT);
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

    public AccountUsage[] getTenantUsageDataForGivenDateRange(String tenantDomain, String productName, String startDate,
            String endDate) throws CloudBillingException {
        // get accountId from tenant
        String accountId = CloudBillingServiceUtils.getAccountIdForTenant(tenantDomain);
        String response = getUsageForTenant(tenantDomain, startDate, endDate);

        UsageProcessorContext context = new UsageProcessorContext();
        context.setResponse(response);
        context.setAccountId(accountId);
        context.setStartDate(startDate);
        context.setEndDate(endDate);

        return UsageProcessorFactory.createUsageProcessor(UsageProcessorFactory.UsageProcessorType.API_CLOUD)
                .process(context);
    }

}
