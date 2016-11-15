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

package org.wso2.carbon.cloud.billing.core.usage.apiusage.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.cloud.billing.core.beans.usage.AccountUsage;
import org.wso2.carbon.cloud.billing.core.beans.usage.Usage;
import org.wso2.carbon.cloud.billing.core.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.core.commons.MonetizationConstants;
import org.wso2.carbon.cloud.billing.core.commons.config.model.Plan;
import org.wso2.carbon.cloud.billing.core.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.core.service.CloudBillingService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * Util class to process API Usage
 */
public class APIUsageProcessorUtil {

    /**
     * Private constructor
     */
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
        Plan plan = null;

        try {
            // checking to see if there are amendments
            if (!hasAmendments) {
                CloudBillingService cloudBillingService = new CloudBillingService();
                String ratePlanId = cloudBillingService.getCurrentRatePlan(accountId);
                /*plan = (Plan) CloudBillingServiceUtils.getSubscriptionForId(BillingConstants
                .API_CLOUD_SUBSCRIPTION_ID, ratePlanId);*/
            }
            elements = AXIOMUtil.stringToOM(response);
            Iterator<?> entries = elements.getChildrenWithName(new QName(BillingConstants.ENTRY));
            List<AccountUsage> usageList = new ArrayList<AccountUsage>();
            while (entries.hasNext()) {
                AccountUsage usage = new AccountUsage();
                OMElement usageEle = (OMElement) entries.next();
                OMElement accounts =
                        (OMElement) usageEle.getChildrenWithName(new QName(BillingConstants.ACCOUNTS)).next();
                if (accounts.getChildElements().next() != null) {
                    int qty = Integer.parseInt(
                            ((OMElement) usageEle.getChildrenWithName(new QName(BillingConstants.TOTAL_COUNT)).next())
                                    .getText());
                    String date = ((OMElement) usageEle.getChildrenWithName(new QName(BillingConstants.TIME)).next())
                            .getText();

                    String tenantDomain =
                            ((OMElement) usageEle.getChildrenWithName(new QName(BillingConstants.API_PUBLISHER)).next())
                                    .getText();
                    if (hasAmendments) {
                        String currentRatePlan = getRatePlanIdForDate(amendmentResponse, date);
                        /*plan = (APICloudPlan) CloudBillingServiceUtils
                                .getSubscriptionForId(BillingConstants.API_CLOUD_SUBSCRIPTION_ID, currentRatePlan);*/
                    }
                    if (plan != null) {
                        /*String overUsageRate = plan.getOverUsage();
                        int maxUsage = plan.getMaxDailyUsage();
                        float overage = calculateCharge(maxUsage, qty, overUsageRate);
                        usage.setAccountId(accountId);
                        usage.setDate(date);
                        usage.setMaxDailyUsage(maxUsage);
                        usage.setOverage(overage);
                        usage.setPaidAccount(true);
                        usage.setProductName(BillingConstants.API_CLOUD);
                        usage.setRatePlan(overUsageRate);
                        usage.setTenantDomain(tenantDomain);
                        usage.setUsage(qty);
                        usageList.add(usage);*/
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
            if (!ratePlanName.contains(BillingConstants.COUPON_HEADER)) {
                return (String) jsonObject.get(BillingConstants.PRODUCT_RATE_PLAN_ID);
            }
        }
        return null;
    }

    /**
     * Calculates usage charges
     *
     * @param maxUsage
     * @param currUsage
     * @param rate
     * @return
     */
    public static float calculateCharge(int maxUsage, int currUsage, String rate) {
        // calculate overUsage
        int overUsage = currUsage - maxUsage;
        if (overUsage < BillingConstants.OVER_USAGE_THRESHOLD) {
            return 0;
        }
        // get the amount of dollars which needs to be added
        int ratePrice = Integer.parseInt(rate.split("/")[0].replace("$", ""));
        // Max number of API calls per a given rate
        int overageValue = Integer.parseInt(rate.split("/")[1]);

        int dailyPriceRate = overUsage / overageValue;
        return dailyPriceRate * ratePrice;
    }

    /**
     * Retrieve rate plan id given the date
     *
     * @param response
     * @param currDate
     * @return
     * @throws CloudBillingException
     */
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
                Date currentDate = new SimpleDateFormat(BillingConstants.DATE_FORMAT).parse(currDate);
                Date startDate = new SimpleDateFormat(BillingConstants.DATE_FORMAT).parse(amendmentStartDate);
                Date endDate = new SimpleDateFormat(BillingConstants.DATE_FORMAT).parse(amendmentEmdDate);
                if (currentDate.after(startDate) && currentDate.before(endDate) || currentDate.equals(startDate)) {
                    return ((OMElement) amendEle.getChildrenWithName(new QName("PRODUCT_RATE_PLAN_ID"))
                            .next()).getText();
                }
            }
        } catch (XMLStreamException e) {
            throw new CloudBillingException("Error while reading xml response from data service", e);
        } catch (ParseException e) {
            throw new CloudBillingException("Error Parsing the dates to date format " + BillingConstants
                    .DATE_FORMAT, e);
        }
        return null;
    }

    /**
     * Get tenant usage from APIM
     *
     * @param response
     * @return
     * @throws CloudBillingException
     */
    public static AccountUsage[] getTenantUsageFromAPIM(String response) throws CloudBillingException {
        OMElement elements;
        try {
            elements = AXIOMUtil.stringToOM(response);
            Iterator<?> entries = elements.getChildrenWithName(new QName(BillingConstants.ENTRY));
            List<AccountUsage> usageList = new ArrayList<AccountUsage>();
            while (entries.hasNext()) {
                AccountUsage usage = new AccountUsage();
                OMElement usageEle = (OMElement) entries.next();

                int qty = Integer.parseInt(
                        ((OMElement) usageEle.getChildrenWithName(new QName(BillingConstants.TOTAL_COUNT)).next())
                                .getText());
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

    /**
     * Get Usage for APIM
     *
     * @param usageEle
     * @return
     * @throws CloudBillingException
     */
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

    /**
     * Get daily usage data from APIM
     *
     * @param response
     * @return
     * @throws CloudBillingException
     */
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

    /**
     * Calculate over usage
     *
     * @param usage
     * @param accountId
     * @param productName
     * @return
     * @throws CloudBillingException
     * @throws XMLStreamException
     */
    private static int calculateOverUsage(int usage, String accountId, String productName)
            throws CloudBillingException, XMLStreamException {

        /*JSONArray ratePlans = ZuoraRESTUtils.getCurrentRatePlan(productName, accountId);
        String productRatePlanId = getCurrentRatePlanId(ratePlans);
        APICloudPlan plan = (APICloudPlan) CloudBillingServiceUtils
                .getSubscriptionForId(BillingConstants.API_CLOUD_SUBSCRIPTION_ID, productRatePlanId);
        if (plan != null) {
            String overUsageRate = plan.getOverUsage();
            int overageValue = Integer.parseInt(overUsageRate.split("/")[1]);
            int maxUsage = plan.getMaxDailyUsage();
            int overUsage = usage - maxUsage;
            return (overUsage > BillingConstants.OVER_USAGE_THRESHOLD) ? overUsage / overageValue : 0;
        } else {
            throw new CloudBillingException("Subscription plan for accountId: " + accountId + " cannot be null");
        }*/
        return 1;
    }

    /**
     * Get daily usage data for paid customers
     *
     * @param response
     * @return
     * @throws CloudBillingException
     */
    public static Usage[] getDailyUsageDataForPaidSubscribers(String response) throws CloudBillingException {
        try {
            OMElement elements = AXIOMUtil.stringToOM(response);
            Iterator<?> entries = elements.getChildrenWithName(new QName(MonetizationConstants.ENTRY));
            List<Usage> usageList = new ArrayList<Usage>();
            while (entries.hasNext()) {
                OMElement usageEle = (OMElement) entries.next();
                OMElement account =
                        (OMElement) usageEle.getChildrenWithName(new QName(MonetizationConstants.ACCOUNT))
                                            .next();
                //filtering only the monetization enabled child accounts
                if (account.getChildElements().next() != null) {

                    Usage usage = APIUsageProcessorUtil.getUsageForPaidSubscribers(usageEle);
                    usage.setAccountId(account.getFirstElement().getText());
                    int dailyUsage = Integer.parseInt(
                            ((OMElement) usageEle.getChildrenWithName(new QName(MonetizationConstants.TOTAL_COUNT))
                                                 .next()).getText());
                    //Getting the subscription id
                    OMElement subscription =
                            (OMElement) account.getChildrenWithName(new QName(MonetizationConstants.SUBSCRIPTION))
                                               .next();
                    usage.setSubscriptionId(subscription.getFirstElement().getText());

                    //Getting the rate plan details
                    OMElement ratePlanDetails =
                            (OMElement) subscription.getChildrenWithName(new QName(MonetizationConstants.RATE_PLAN))
                                                    .next();
                    int maxDailyUsage = Integer.parseInt((((OMElement) ratePlanDetails
                            .getChildrenWithName(new QName(MonetizationConstants.MAX_DAILY_USAGE)).next()).getText()));
                    int unitOfMeasure = Integer.parseInt((((OMElement) ratePlanDetails
                            .getChildrenWithName(new QName(MonetizationConstants.UNIT_OF_MEASURE)).next()).getText()));


                    //Calculating the over used Api calls
                    int overUsageCount = dailyUsage - maxDailyUsage;
                    int overUsageUnits = (overUsageCount > BillingConstants.OVER_USAGE_THRESHOLD) ?
                                         (overUsageCount / unitOfMeasure) : 0;

                    if (overUsageUnits > MonetizationConstants.OVER_USAGE_THRESHOLD) {
                        usage.setQty(overUsageUnits);
                        usageList.add(usage);
                    }
                }
            }
            return usageList.toArray(new Usage[usageList.size()]);
        } catch (XMLStreamException e) {
            throw new CloudBillingException("Error while reading xml response from data service", e);
        }
    }

    /**
     * Get usage data for paid subscribers
     *
     * @param usageEle
     * @return
     * @throws CloudBillingException
     */
    private static Usage getUsageForPaidSubscribers(OMElement usageEle) throws CloudBillingException {
        Usage usage = new Usage();
        usage.setUom(MonetizationConstants.UNIT_OF_MEASURE_DISPLAY_NAME);
        String date =
                ((OMElement) usageEle.getChildrenWithName(new QName(MonetizationConstants.MONTH)).next()).getText() +
                        "/" +
                        ((OMElement) usageEle.getChildrenWithName(new QName(MonetizationConstants.DAY)).next())
                                .getText() +
                        "/" +
                        ((OMElement) usageEle.getChildrenWithName(new QName(MonetizationConstants.YEAR)).next())
                                .getText();
        usage.setDescription("Usage Data");
        usage.setStartDate(date);
        usage.setEndDate(date);
        return usage;
    }
}
