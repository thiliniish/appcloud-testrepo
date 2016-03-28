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
package org.wso2.carbon.cloud.billing.common.zuora;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.cloud.billing.common.BillingConstants;
import org.wso2.carbon.cloud.billing.common.CloudBillingException;
import org.wso2.carbon.cloud.billing.processor.BillingRequestProcessor;
import org.wso2.carbon.cloud.billing.processor.BillingRequestProcessorFactory;
import org.wso2.carbon.cloud.billing.utils.CloudBillingUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents class for Zuora related functionalities.
 */
public class ZuoraUtils {


    private static final Log log = LogFactory.getLog(ZuoraUtils.class);
    private static BillingRequestProcessor zuoraApi =
            BillingRequestProcessorFactory.getBillingRequestProcessor(
                    BillingRequestProcessorFactory.ProcessorType.ZUORA,
                    CloudBillingUtils.getBillingConfiguration().getZuoraConfig().getHttpClientConfig());

    public static String getSubscriptionIdForAccount(String accountId) throws CloudBillingException {
        String response = getAccountSummary(accountId);
        JSONArray subscriptions = getSubscriptions(accountId, response);
        return (String) ((JSONObject) subscriptions.get(0)).get(BillingConstants.SUBSCRIPTION_NUMBER);
    }

    public static String[] getProductRatePlanIdForAccount(String productName,
                                                          String accountId) throws CloudBillingException {
        // getting subscriptions elements
        List<String> ratePlansList = new ArrayList<String>();
        String response = getAccountSummary(accountId);
        JSONArray subscriptions = getSubscriptions(accountId, response);

        // take the elements of the json array of subscriptions
        for (int i = 0; i < subscriptions.size(); i++) {
            if (log.isDebugEnabled()) {
                log.debug("The " + i + " element of the subscriptions array: " + subscriptions.get(i));
            }
            // get all rate plans
            JSONArray ratePlans = (JSONArray) ((JSONObject) subscriptions.get(0)).get(BillingConstants.RATEPLANS);
            for (Object ratePlan : ratePlans) {
                if (((JSONObject) ratePlan).get(BillingConstants.PRODUCT_NAME).equals(productName)) {
                    String productRatePlanId =
                            (String) ((JSONObject) ratePlan).get(BillingConstants.PRODUCT_RATE_PLAN_ID);
                    ratePlansList.add(productRatePlanId);

                }
            }
        }
        return ratePlansList.toArray(new String[ratePlansList.size()]);
    }

    public static JSONArray getSubscriptions(String accountId, String response) throws CloudBillingException {
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(response);
            // getting all subscriptions elements for accountId
            return ((JSONArray) jsonObject.get(BillingConstants.SUBSCRIPTIONS));
        } catch (ParseException e) {
            String msg = "Error passing the response " + response + " to json object";
            log.error(msg, e);
            throw new CloudBillingException(msg, e);
        }
    }

    public static String[] getSubscribedProducts(String accountId) throws CloudBillingException {
        List<String> products = new ArrayList<String>();
        String response = getAccountSummary(accountId);
        JSONArray subscriptions = getSubscriptions(accountId, response);
        // take the elements of the json array of subscriptions
        for (int i = 0; i < subscriptions.size(); i++) {
            if (log.isDebugEnabled()) {
                log.debug("The " + i + " element of the subscriptions array: " + subscriptions.get(i));
            }
            // get all rate plans
            JSONArray ratePlans = (JSONArray) ((JSONObject) subscriptions.get(0)).get(BillingConstants.RATEPLANS);
            for (int j = 0; j < subscriptions.size(); j++) {
                // Check if the selected rate planId is from the current product
                String productName = (String) ((JSONObject) ratePlans.get(j)).get(BillingConstants.PRODUCT_NAME);
                products.add(productName);
            }
        }
        return products.toArray(new String[products.size()]);
    }

    public static String getAccountSummary(String accountId) throws CloudBillingException {
        String url;
        try {
            url = CloudBillingUtils.getBillingConfiguration().getZuoraConfig().getApiConfigs().getAccountSummary();

            url = url.replace(BillingConstants.ACCOUNT_KEY_PARAM, accountId);
            return zuoraApi.doGet(url);
        } catch (CloudBillingException e) {
            String msg = "Error getting Account summary from the account " + accountId;
            log.error(msg, e);
            throw new CloudBillingException(msg, e);
        }
    }

    public static String getInvoices(String accountId) throws CloudBillingException {
        String url;
        try {
            url = CloudBillingUtils.getBillingConfiguration().getZuoraConfig().getApiConfigs().getInvoiceInfo();

            url = url.replace(BillingConstants.ACCOUNT_KEY_PARAM, accountId);
            return zuoraApi.doGet(url);
        } catch (CloudBillingException e) {
            String msg = "Error getting invoices summary from the account " + accountId;
            log.error(msg, e);
            throw new CloudBillingException(msg, e);
        }
    }

    public static String getPayments(String accountId) throws CloudBillingException {
        String url;
        try {
            url = CloudBillingUtils.getBillingConfiguration().getZuoraConfig().getApiConfigs().getPaymentInfo();

            url = url.replace(BillingConstants.ACCOUNT_KEY_PARAM, accountId);
            return zuoraApi.doGet(url);
        } catch (CloudBillingException e) {
            String msg = "Error getting payment information summary from the account " + accountId;
            log.error(msg, e);
            throw new CloudBillingException(msg, e);
        }
    }

    public static JSONArray getCurrentRatePlan(String productName, String accountId) throws CloudBillingException {
        String response = null;
        JSONArray currentRatePlanList = new JSONArray();
        JSONArray starterRatePlanList = new JSONArray();
        try {
            String url = CloudBillingUtils.getBillingConfiguration().getZuoraConfig().getApiConfigs().getRatePlans();
            url = url.replace(BillingConstants.ACCOUNT_KEY_PARAM, accountId);
            response = zuoraApi.doGet(url);
            JSONArray subscriptions = getSubscriptions(accountId, response);
            for (Object subscription : subscriptions) {
                // get all rate plans
                JSONArray ratePlans = (JSONArray) ((JSONObject) subscription).get(BillingConstants.RATEPLANS);
                if (ratePlans.size() == 1) {
                    currentRatePlanList.add(ratePlans.get(0));
                }
                //if adding a rate plan when cre
                for (Object ratePlan : ratePlans) {
                    if (((JSONObject) ratePlan).get(BillingConstants.PRODUCT_NAME).equals(productName)) {
                        String lastChangeType =
                                (String) ((JSONObject) ratePlan).get(BillingConstants.LAST_CHANGE_TYPE);
                        if ((lastChangeType != null) && (lastChangeType.equals(BillingConstants.AMENDEMENT_ADD_TYPE))) {
                            currentRatePlanList.add(ratePlan);
                        }
                        starterRatePlanList.add(ratePlan);
                    }
                }
                //if a coupon is added when creating the payment method (first time). should return both the coupon
                // and payment plan.
                if (currentRatePlanList.size() != 0) {
                    return currentRatePlanList;
                } else {
                    return starterRatePlanList;
                }
            }
        } catch (CloudBillingException e) {
            String msg = "Error getting ratePlans from the account " + accountId;
            log.error(msg, e);
            throw new CloudBillingException(msg, e);
        } catch (Exception e) {
            String msg = "Error passing the response " + response + " to json object";
            log.error(msg, e);
            throw new CloudBillingException(msg, e);
        }
        return null;
    }

    public static JSONArray getProductRatePlans(String productName) throws CloudBillingException {
        String url = CloudBillingUtils.getBillingConfiguration().getZuoraConfig().getApiConfigs().getProducts();
        String response;
        boolean isProductAvailable = false;
        JSONArray ratePlanList = null;

        while (!isProductAvailable) {
            response = zuoraApi.doGet(url);
            if (response == null) {
                throw new CloudBillingException(
                        "Zuora api response is null, while getting product rate plans for URL" + url);
            }
            try {
                JSONParser jsonParser = new JSONParser();
                JSONObject productJsonObject = (JSONObject) jsonParser.parse(response);
                boolean success = (Boolean) productJsonObject.get(BillingConstants.PRODUCT_RATE_PLANS_SUCCESS_STATUS);
                if (success) {
                    JSONArray products = ((JSONArray) productJsonObject.get(BillingConstants.PRODUCTS));
                    for (Object product : products) {
                        String zuoraProductName = ((JSONObject) product).get(BillingConstants.NAME).toString();
                        if (zuoraProductName != null && productName.equals(BillingConstants.API_CLOUD_SUBSCRIPTION_ID)
                                && zuoraProductName.equals(BillingConstants.API_CLOUD)) {
                            ratePlanList = (JSONArray) ((JSONObject) product).get(BillingConstants.PRODUCT_RATE_PLANS);
                        }
                    }
                    if (ratePlanList == null) {
                        String nextPage;
                        // Get the next page value
                        nextPage = (String) productJsonObject.get(BillingConstants.PRODUCT_RATE_PLANS_NEXTPAGE);
                        if (nextPage != null) {
                            url = nextPage;
                        } else {
                            return null;
                        }
                    } else {
                        isProductAvailable = true;
                    }
                } else {
                    return null;
                }
            } catch (ParseException e) {
                throw new CloudBillingException("Error passing the response " + response + " to json object.", e);
            }
        }
        return ratePlanList;
    }

    public static JSONObject getProductRatePlanObject(String productName, String ratePlanName)
            throws CloudBillingException {
        try {
            JSONArray productPlans = getProductRatePlans(productName);
            for (Object productPlan : productPlans) {
                JSONObject jsonObject = (JSONObject) productPlan;
                String planName = (String) jsonObject.get(BillingConstants.PRODUCT_RATE_PLAN_NAME);
                if (planName != null && planName.equals(ratePlanName)) {
                    return jsonObject;
                }
            }
        } catch (CloudBillingException e) {
            String msg = "Error getting Product ratePlan object for " + ratePlanName;
            log.error(msg, e);
            throw new CloudBillingException(msg, e);
        }
        return null;
    }
}
