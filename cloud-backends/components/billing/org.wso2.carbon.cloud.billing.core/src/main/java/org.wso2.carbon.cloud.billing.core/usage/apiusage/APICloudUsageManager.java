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

import com.google.gson.JsonObject;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.cloud.billing.core.beans.usage.AccountUsage;
import org.wso2.carbon.cloud.billing.core.beans.usage.Usage;
import org.wso2.carbon.cloud.billing.core.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.core.commons.MonetizationConstants;
import org.wso2.carbon.cloud.billing.core.commons.config.BillingConfigManager;
import org.wso2.carbon.cloud.billing.core.commons.notifications.EmailNotifications;
import org.wso2.carbon.cloud.billing.core.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.core.processor.BillingRequestProcessor;
import org.wso2.carbon.cloud.billing.core.processor.BillingRequestProcessorFactory;
import org.wso2.carbon.cloud.billing.core.service.APICloudMonetizationService;
import org.wso2.carbon.cloud.billing.core.service.CloudBillingService;
import org.wso2.carbon.cloud.billing.core.usage.UsageProcessorContext;
import org.wso2.carbon.cloud.billing.core.usage.UsageProcessorFactory;
import org.wso2.carbon.cloud.billing.core.usage.apiusage.utils.APIUsageProcessorUtil;
import org.wso2.carbon.cloud.billing.core.utils.CloudBillingServiceUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Represents the usage manager which does usage related operations
 */
public class APICloudUsageManager {

    private static final Log LOGGER = LogFactory.getLog(APICloudUsageManager.class);
    private static String usageForTenantUrl =
            BillingConfigManager.getBillingConfiguration().getDataServiceConfig().getCloudBillingServiceUri() +
            BillingConstants.DS_API_URI_USAGE;
    private static String dailyUsageUrl =
            BillingConfigManager.getBillingConfiguration().getDataServiceConfig().getCloudBillingServiceUri()
            + BillingConstants.DS_API_URI_REQUEST_COUNT;
    private static String dailyMonetizationUsageUrl =
            BillingConfigManager.getBillingConfiguration().getDataServiceConfig().getApiCloudMonetizationServiceUri()
            + MonetizationConstants.DS_API_URI_MON_APIC_DAILY_USAGE;
    private BillingRequestProcessor dsBRProcessor;

    public APICloudUsageManager() {
        BillingRequestProcessorFactory processorFactory = BillingRequestProcessorFactory.getInstance();
        dsBRProcessor = processorFactory.getBillingRequestProcessor(
                BillingRequestProcessorFactory.ProcessorType.DATA_SERVICE);
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

    public void uploadDailyAPIUsage() throws CloudBillingException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(BillingConstants.DATE_TIME_FORMAT);
        LOGGER.info("Started the daily usage task for  " + dateFormat.format(new Date(System.currentTimeMillis())));
        // get daily usage from data services and create a usage array
        String response = getDailyUsage(dailyUsageUrl);
        String responseForMonetizationUsage = getDailyUsageForMonetization(dailyMonetizationUsageUrl);

        Usage[] usageList = APIUsageProcessorUtil.getDailyUsageDataForApiM(response);
        Usage[] monetizationEnabledUsageList =
                APIUsageProcessorUtil.getDailyUsageDataForPaidSubscribers(responseForMonetizationUsage);
        uploadDailyAPIUsage(usageList, false);
        uploadDailyAPIUsage(monetizationEnabledUsageList, true);
    }

    private String getDailyUsage(String usageUrl) throws CloudBillingException {

        Date currentDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        NameValuePair[] nameValuePairs =
                new NameValuePair[] { new NameValuePair("year", String.valueOf(cal.get(Calendar.YEAR))),
                                      new NameValuePair("month", String.valueOf((cal.get(Calendar.MONTH) + 1))),
                                      new NameValuePair("day", String.valueOf(cal.get(Calendar.DAY_OF_MONTH))) };
        return dsBRProcessor.doGet(usageUrl, null, nameValuePairs);
    }

    private String getDailyUsageForMonetization(String usageUrl) throws CloudBillingException {
        Date currentDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        NameValuePair[] nameValuePairs =
                new NameValuePair[] { new NameValuePair("YEAR", String.valueOf(cal.get(Calendar.YEAR))),
                                      new NameValuePair("MONTH", String.valueOf((cal.get(Calendar.MONTH) + 1))),
                                      new NameValuePair("DAY", String.valueOf(cal.get(Calendar.DAY_OF_MONTH))) };
        return dsBRProcessor.doGet(usageUrl, null, nameValuePairs);
    }

    private void uploadDailyAPIUsage(Usage[] usages, Boolean isMoentizationUsage) throws CloudBillingException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(BillingConstants.DATE_TIME_FORMAT);
        String today = dateFormat.format(new Date(System.currentTimeMillis()));

        try {
            for (int x = 0; x < usages.length; x++) {
                Double usageAmount = (usages[x].getUnitPrice() * usages[x].getQty() * BillingConstants.CENTS);
                JsonObject invoiceItem = new JsonObject();
                invoiceItem.addProperty("customer", usages[x].getAccountId());
                invoiceItem.addProperty("amount", String.valueOf(usageAmount.intValue()));
                invoiceItem.addProperty("currency", BillingConstants.CURRENCY);
                invoiceItem.addProperty("description", usages[x].getDescription());
                String invoiceItemResponse = null;
                if (!isMoentizationUsage) {
                    CloudBillingService cloudBillingService = new CloudBillingService();
                    invoiceItemResponse =
                            cloudBillingService.callVendorMethod("createInvoiceItems", invoiceItem.toString());
                } else {
                    APICloudMonetizationService cloudMonetizationService = new APICloudMonetizationService();
                    invoiceItemResponse = cloudMonetizationService
                            .callVendorMethod(usages[x].getTenantDomain(), "createInvoiceItems",
                                              invoiceItem.toString());
                }

                JSONObject object = new JSONObject(invoiceItemResponse);
                if ((!(Boolean) object.get("success"))) {
                    throw new CloudBillingException(
                            "Error occurred while uploading daily usage for " + usages[x].getAccountId());
                }
            }

        } catch (Exception e) {
            String subject = BillingConstants.EMAIL_SUBJECT_OVERAGE_FAILURE + today;
            String messageBody =
                    BillingConstants.EMAIL_BODY_OVERAGE_FAILURE.replace(BillingConstants.REPLACE_TODAY, today);

            //Sending the Email to Cloud Team for verify on the error occurred
            EmailNotifications.getInstance().sendMail(messageBody, subject,
                                                      BillingConfigManager.getBillingConfiguration()
                                                                          .getNotificationsConfig()
                                                                          .getEmailNotification().getSender(),
                                                      BillingConstants.TEXT_PLAIN_CONTENT_TYPE);
            throw new CloudBillingException("Error occurred while uploading daily usage", e);
        }
    }

}
