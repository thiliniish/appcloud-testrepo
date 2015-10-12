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
package org.wso2.carbon.cloud.billing.usage.apiusage.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.cloud.billing.beans.usage.AccountUsage;
import org.wso2.carbon.cloud.billing.beans.usage.Usage;
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.commons.config.APICloudPlan;
import org.wso2.carbon.cloud.billing.commons.zuora.ZuoraRESTUtils;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.utils.CloudBillingUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class APIUsageProcessorUtil {

    private APIUsageProcessorUtil() {
    }

    /**
     * Retrieve tenant API usage from API manager
     *
     * @param response          response
     * @param accountId         zuora account id
     * @param hasAmendments     has zuora amendments to the account
     * @param amendmentResponse amendment response
     * @return account usage
     * @throws CloudBillingException
     */
    public static AccountUsage[] getTenantUsageFromAPIM(String response, String accountId, boolean hasAmendments,
                                                        String amendmentResponse) throws CloudBillingException {
        OMElement elements;
        APICloudPlan plan = null;
        try {
            // checking to see if there are amendments
            if (!hasAmendments) {
                JSONArray ratePlans = ZuoraRESTUtils.getCurrentRatePlan(BillingConstants.API_CLOUD, accountId);
                String ratePlanId = getCurrentRatePlanId(ratePlans);
                plan = (APICloudPlan) CloudBillingUtils
                        .getSubscriptionForId(BillingConstants.API_CLOUD_SUBSCRIPTION_ID, ratePlanId);
            }
            elements = AXIOMUtil.stringToOM(response);
            Iterator<?> entries = elements.getChildrenWithName(new QName(BillingConstants.ENTRY));
            List<AccountUsage> usageList = new ArrayList<AccountUsage>();
            while (entries.hasNext()) {
                AccountUsage usage = new AccountUsage();
                OMElement usageEle = (OMElement) entries.next();
                OMElement accounts =
                        (OMElement) usageEle.getChildrenWithName(new QName(BillingConstants.ACCOUNTS))
                                .next();
                if (accounts.getChildElements().next() != null) {
                    int qty =
                            Integer.parseInt(((OMElement) usageEle.getChildrenWithName(new QName(
                                    BillingConstants.TOTAL_COUNT))
                                    .next()).getText());
                    String date = ((OMElement) usageEle.getChildrenWithName(new QName(BillingConstants.TIME)).next())
                            .getText();

                    String tenantDomain =
                            ((OMElement) usageEle.getChildrenWithName(new QName(
                                    BillingConstants.API_PUBLISHER))
                                    .next()).getText();
                    if (hasAmendments) {
                        String currentRatePlan = getRatePlanIdForDate(amendmentResponse, date);
                        plan = (APICloudPlan) CloudBillingUtils
                                .getSubscriptionForId(BillingConstants.API_CLOUD_SUBSCRIPTION_ID, currentRatePlan);
                    }
                    if (plan != null) {
                        String overUsageRate = plan.getOverUsage();
                        int maxUsage = plan.getMaxDailyUsage();
                        float overage = calculateCharge(plan.getMaxDailyUsage(), qty, overUsageRate);
                        usage.setAccountId(accountId);
                        usage.setDate(date);
                        usage.setMaxDailyUsage(maxUsage);
                        usage.setOverage(overage);
                        usage.setPaidAccount(true);
                        usage.setProductName(BillingConstants.API_CLOUD);
                        usage.setRatePlan(overUsageRate);
                        usage.setTenantDomain(tenantDomain);
                        usage.setUsage(qty);
                        usageList.add(usage);
                    }
                }
            }
            return usageList.toArray(new AccountUsage[usageList.size()]);
        } catch (XMLStreamException e) {
            throw new CloudBillingException("Error while reading xml response from data service", e);
        }
    }

    /**
     * Get current rate plan ommiting coupons
     *
     * @param ratePlans rate plans
     * @return curent rate plan
     */
    private static String getCurrentRatePlanId(JSONArray ratePlans) {
        for (Object ratePlan : ratePlans) {
            JSONObject jsonObject = (JSONObject) ratePlan;
            String ratePlanName = ((JSONObject) ratePlan).get(BillingConstants.RATE_PLAN_NAME).toString();
            if (ratePlanName != null && !ratePlanName.contains(BillingConstants.COUPON_HEADER)) {
                return (String) jsonObject.get(BillingConstants.PRODUCT_RATE_PLAN_ID);
            }
        }
        return null;
    }

    public static float calculateCharge(int maxUsage, int currUsage, String rate) {
        // calculate overUsage
        int overUsage = currUsage - maxUsage;
        if (overUsage < BillingConstants.OVER_USAGE_THRESHOLD) {
            return 0;
        }
        // get the amount of dollars which needs to be added
        int ratePrice = Integer.parseInt(rate.split("/")[0].replace("$", ""));
        // Max number of API calls per a given rate
        int overageValue = Integer.parseInt(rate.split("/")[1].replace("K", "")) * 1000;

        int dailyPriceRate = overUsage / overageValue;
        return dailyPriceRate * ratePrice;
    }

    private static String getRatePlanIdForDate(String response, String currDate) throws CloudBillingException {
        OMElement elements;

        try {
            elements = AXIOMUtil.stringToOM(response);
            Iterator<?> entries = elements.getChildrenWithName(new QName(BillingConstants.ENTRY));
            while (entries.hasNext()) {
                OMElement amendEle = (OMElement) entries.next();

                String amendmentStartDate =
                        ((OMElement) amendEle.getChildrenWithName(new QName(
                                BillingConstants.START_DATE))
                                .next()).getText();

                String amendmentEmdDate =
                        ((OMElement) amendEle.getChildrenWithName(new QName(BillingConstants.END_DATE))
                                .next()).getText();
                Date currentDate = new SimpleDateFormat(BillingConstants.DS_DATE_FOMAT).parse(currDate);
                Date startDate = new SimpleDateFormat(BillingConstants.DS_DATE_FOMAT).parse(amendmentStartDate);
                Date endDate = new SimpleDateFormat(BillingConstants.DS_DATE_FOMAT).parse(amendmentEmdDate);
                if (currentDate.after(startDate) && currentDate.before(endDate) || currentDate.equals(startDate)) {
                    return ((OMElement) amendEle.getChildrenWithName(new QName("PRODUCT_RATE_PLAN_ID"))
                            .next()).getText();
                }
            }
        } catch (XMLStreamException e) {
            throw new CloudBillingException("Error while reading xml response from data service", e);
        } catch (ParseException e) {
            throw new CloudBillingException("Error Parsing the dates to date format " + BillingConstants
                    .DS_DATE_FOMAT, e);
        }
        return null;
    }

    public static AccountUsage[] getTenantUsageFromAPIM(String response) throws CloudBillingException {
        OMElement elements;
        try {
            elements = AXIOMUtil.stringToOM(response);
            Iterator<?> entries = elements.getChildrenWithName(new QName(BillingConstants.ENTRY));
            List<AccountUsage> usageList = new ArrayList<AccountUsage>();
            while (entries.hasNext()) {
                AccountUsage usage = new AccountUsage();
                OMElement usageEle = (OMElement) entries.next();

                int qty = Integer.parseInt(((OMElement) usageEle.getChildrenWithName(new QName(BillingConstants
                                                                                                       .TOTAL_COUNT))
                        .next()).getText());
                String date = ((OMElement) usageEle.getChildrenWithName(new QName(BillingConstants.YEAR)).next())
                                      .getText() + "/" +
                              ((OMElement) usageEle.getChildrenWithName(new QName(BillingConstants.MONTH)).next())
                                      .getText() +
                              "/" +
                              ((OMElement) usageEle.getChildrenWithName(new QName(BillingConstants.DAY)).next())
                                      .getText();
                String tenantDomain = ((OMElement) usageEle.getChildrenWithName(new QName(BillingConstants
                                                                                                  .API_PUBLISHER))
                        .next()).getText();
                usage.setDate(date);
                usage.setPaidAccount(false);
                usage.setProductName(BillingConstants.API_CLOUD);
                usage.setTenantDomain(tenantDomain);
                usage.setUsage(qty);
                usageList.add(usage);

            }
            return usageList.toArray(new AccountUsage[usageList.size()]);
        } catch (XMLStreamException e) {
            throw new CloudBillingException("Error while reading xml response from data service", e);
        }

    }

    private static Usage getUsageForApiM(OMElement usageEle) throws CloudBillingException {
        Usage usage = new Usage();
        usage.setUom(BillingConstants.UNIT_OF_MEASURE); // TODO get it from the
        // config
        String tenantDomain = ((OMElement) usageEle.getChildrenWithName(new QName(BillingConstants.API_PUBLISHER))
                .next()).getText();
        String date = ((OMElement) usageEle.getChildrenWithName(new QName(BillingConstants.MONTH)).next()).getText() +
                      "/" +
                      ((OMElement) usageEle.getChildrenWithName(new QName(BillingConstants.DAY)).next()).getText() +
                      "/" +
                      ((OMElement) usageEle.getChildrenWithName(new QName(BillingConstants.YEAR)).next()).getText();
        usage.setDescription("Usage Data for tenant " + tenantDomain + " on " + date);
        usage.setStartDate(date);
        usage.setEndDate(date);
        return usage;
    }

    public static Usage[] getDailyUsageDataForApiM(String response) throws CloudBillingException {
        try {
            OMElement elements = AXIOMUtil.stringToOM(response);
            Iterator<?> entries = elements.getChildrenWithName(new QName(BillingConstants.ENTRY));
            List<Usage> usageList = new ArrayList<Usage>();
            while (entries.hasNext()) {
                OMElement usageEle = (OMElement) entries.next();
                OMElement accounts =
                        (OMElement) usageEle.getChildrenWithName(new QName(BillingConstants.ACCOUNTS))
                                .next();

                if (accounts.getChildElements().next() != null) {
                    OMElement account = (OMElement) accounts.getChildElements().next();
                    Usage usage = APIUsageProcessorUtil.getUsageForApiM(usageEle);
                    usage.setAccountId(account.getFirstElement().getText());
                    int qty =
                            Integer.parseInt(((OMElement) usageEle.getChildrenWithName(new QName(
                                    BillingConstants.TOTAL_COUNT))
                                    .next()).getText());

                    int overUsage = calculateOverUsage(qty, usage.getAccountId(), BillingConstants.API_CLOUD);
                    if (overUsage > 0) {
                        usage.setQty(overUsage);
                        usageList.add(usage);
                    }
                }
            }
            return usageList.toArray(new Usage[usageList.size()]);
        } catch (XMLStreamException e) {
            throw new CloudBillingException("Error while reading xml response from data service", e);
        }
    }

    private static int calculateOverUsage(int usage, String accountId, String productName)
            throws CloudBillingException {

        JSONArray ratePlans = ZuoraRESTUtils.getCurrentRatePlan(productName, accountId);
        String productRatePlanId = getCurrentRatePlanId(ratePlans);
        APICloudPlan plan = (APICloudPlan) CloudBillingUtils
                .getSubscriptionForId(BillingConstants.API_CLOUD_SUBSCRIPTION_ID, productRatePlanId);
        if (plan != null) {
            int maxUsage = plan.getMaxDailyUsage();
            int overUsage = usage - maxUsage;
            return (overUsage > BillingConstants.OVER_USAGE_THRESHOLD) ? overUsage : 0;
        } else {
            throw new CloudBillingException("Subscription plan for accountId: " + accountId + " cannot be null");
        }
    }

}
