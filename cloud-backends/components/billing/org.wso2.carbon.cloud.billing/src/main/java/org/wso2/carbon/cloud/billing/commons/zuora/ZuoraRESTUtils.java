/*
 *  Copyright (c) 2015-2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.cloud.billing.commons.zuora;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.commons.config.BillingConfig;
import org.wso2.carbon.cloud.billing.commons.config.ZuoraConfig;
import org.wso2.carbon.cloud.billing.commons.utils.BillingConfigUtils;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.processor.BillingRequestProcessor;
import org.wso2.carbon.cloud.billing.processor.BillingRequestProcessorFactory;
import org.wso2.carbon.cloud.billing.utils.CloudBillingServiceUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents class for Zuora related functionalities.
 */
public class ZuoraRESTUtils {

    private static final Log LOGGER = LogFactory.getLog(ZuoraRESTUtils.class);
    private static BillingRequestProcessor zuoraApi = BillingRequestProcessorFactory.getInstance()
            .getBillingRequestProcessor(BillingRequestProcessorFactory.ProcessorType.ZUORA);

    private static JsonObject subscriptionPlanInfoObj;

    static {
        subscriptionPlanInfoObj = new JsonObject();
        subscriptionPlanInfoObj.addProperty("autoRenew", true);
        subscriptionPlanInfoObj.addProperty("invoiceCollect", true);
        subscriptionPlanInfoObj
                .addProperty("termType", BillingConfigUtils.getBillingConfiguration().getZuoraConfig().getTermType());
    }

    private ZuoraRESTUtils() {
    }

    /**
     * Retrieve account subscriptions
     *
     * @param accountId zuora accountId
     * @return json string of subscriptions
     * @throws CloudBillingException
     */
    public static String getSubscriptionIdForAccount(String accountId, String serviceId) throws CloudBillingException {
        String response = getAccountSummary(accountId);
        JSONArray subscriptions = getSubscriptions(accountId, response);
        String zuoraProductId = CloudBillingServiceUtils.getZuoraProductIdForServiceId(serviceId);

        for (Object subscriptionObj : subscriptions) {
            JSONObject subscription = (JSONObject) subscriptionObj;
            //Any of the rate plans can be taken since only there is a one to one mapping between subscription and
            // product according to cloud use case: hence the magic number 0
            JSONObject ratePlan = (JSONObject) ((JSONArray) subscription.get(BillingConstants.RATE_PLANS)).get(0);
            if (zuoraProductId.equals(ratePlan.get(BillingConstants.PRODUCT_ID))) {
                return (String) subscription.get(BillingConstants.SUBSCRIPTION_NUMBER);
            }
        }

        throw new CloudBillingException("No " + serviceId + " subscription available for account id: " + accountId);
    }

    /**
     * Get the zuora product rate plan id which the account has the subscription
     *
     * @param productName product name ex:api_cloud
     * @param accountId   zuora account id
     * @return product rate plans subscribed (including if any coupons)
     * @throws CloudBillingException
     */
    public static String[] getProductRatePlanIdForAccount(String productName, String accountId)
            throws CloudBillingException {
        // getting subscriptions elements
        List<String> ratePlansList = new ArrayList<String>();
        String response = getAccountSummary(accountId);
        JSONArray subscriptions = getSubscriptions(accountId, response);

        // take the elements of the json array of subscriptions
        for (int i = 0; i < subscriptions.size(); i++) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The " + i + " element of the subscriptions array: " + subscriptions.get(i));
            }
            // get all rate plans
            JSONArray ratePlans = (JSONArray) ((JSONObject) subscriptions.get(0)).get(BillingConstants.RATE_PLANS);
            for (Object ratePlan : ratePlans) {
                if (((JSONObject) ratePlan).get(BillingConstants.PRODUCT_NAME).equals(productName)) {
                    String productRatePlanId = (String) ((JSONObject) ratePlan)
                            .get(BillingConstants.PRODUCT_RATE_PLAN_ID);
                    ratePlansList.add(productRatePlanId);

                }
            }
        }
        return ratePlansList.toArray(new String[ratePlansList.size()]);
    }

    /**
     * Get subscriptions that the account has
     *
     * @param accountId zuora account id
     * @param response  response of account summary
     * @return subscriptions
     * @throws CloudBillingException
     */
    public static JSONArray getSubscriptions(String accountId, String response) throws CloudBillingException {
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(response);
            // getting all subscriptions elements for accountId
            return (JSONArray) jsonObject.get(BillingConstants.SUBSCRIPTIONS);
        } catch (ParseException e) {
            throw new CloudBillingException("Error passing the response " + response + " to json object", e);
        }
    }

    /**
     * Get subscribed products
     *
     * @param accountId account id
     * @return string array of products subscribed
     * @throws CloudBillingException
     */
    public static String[] getSubscribedProducts(String accountId) throws CloudBillingException {
        List<String> products = new ArrayList<String>();
        String response = getAccountSummary(accountId);
        JSONArray subscriptions = getSubscriptions(accountId, response);
        // take the elements of the json array of subscriptions
        for (int i = 0; i < subscriptions.size(); i++) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The " + i + " element of the subscriptions array: " + subscriptions.get(i));
            }
            // get all rate plans
            JSONArray ratePlans = (JSONArray) ((JSONObject) subscriptions.get(0)).get(BillingConstants.RATE_PLANS);
            for (int j = 0; j < subscriptions.size(); j++) {
                // Check if the selected rate planId is from the current product
                String productName = (String) ((JSONObject) ratePlans.get(j)).get(BillingConstants.PRODUCT_NAME);
                products.add(productName);
            }
        }
        return products.toArray(new String[products.size()]);
    }

    /**
     * Retrieve account summary
     *
     * @param accountId account id
     * @return json string of account summary
     * @throws CloudBillingException
     */
    public static String getAccountSummary(String accountId) throws CloudBillingException {
        return zuoraApi.doGet(BillingConstants.ZUORA_REST_API_URI_ACCOUNT_SUMMARY
                .replace(BillingConstants.ACCOUNT_KEY_PARAM, accountId), null, null);

    }

    /**
     * Retrieve invoices for the accountId
     *
     * @param accountId zuora accountId
     * @return json string of invoices
     * @throws CloudBillingException
     */
    public static String getInvoices(String accountId) throws CloudBillingException {
        return zuoraApi.doGet(BillingConstants.ZUORA_REST_API_URI_INVOICE_INFO
                .replace(BillingConstants.ACCOUNT_KEY_PARAM, accountId), null, null);
    }

    /**
     * Get account payments
     *
     * @param accountId zuora account id
     * @return json string of payments
     * @throws CloudBillingException
     */
    public static String getPayments(String accountId) throws CloudBillingException {
        return zuoraApi.doGet(BillingConstants.ZUORA_REST_API_URI_PAYMENT_INFO
                .replace(BillingConstants.ACCOUNT_KEY_PARAM, accountId), null, null);
    }

    /**
     * Get current rate plan for a subscribed product
     *
     * @param productName zuora product name
     * @param accountId   zuora account id
     * @return Json array of rate plans
     * @throws CloudBillingException
     */
    @SuppressWarnings("unchecked") public static JSONArray getCurrentRatePlan(String productName, String accountId)
            throws CloudBillingException {
        String response;
        JSONArray currentRatePlanList = new JSONArray();
        JSONArray starterRatePlanList = new JSONArray();

        response = zuoraApi.doGet(BillingConstants.ZUORA_REST_API_URI_RATE_PLANS
                .replace(BillingConstants.ACCOUNT_KEY_PARAM, accountId), null, null);
        JSONArray subscriptions = getSubscriptions(accountId, response);
        for (Object subscription : subscriptions) {
            // get all rate plans
            JSONArray ratePlans = (JSONArray) ((JSONObject) subscription).get(BillingConstants.RATE_PLANS);
            if (ratePlans.size() == 1) {
                currentRatePlanList.add(ratePlans.get(0));
            }
            //if adding a rate plan when cre
            for (Object ratePlan : ratePlans) {
                if (((JSONObject) ratePlan).get(BillingConstants.PRODUCT_NAME).equals(productName)) {
                    String lastChangeType = (String) ((JSONObject) ratePlan).get(BillingConstants.LAST_CHANGE_TYPE);
                    if ((lastChangeType != null) && (lastChangeType.equals(BillingConstants.AMENDEMENT_ADD_TYPE))) {
                        currentRatePlanList.add(ratePlan);
                    }
                    starterRatePlanList.add(ratePlan);
                }
            }
            //if a coupon is added when creating the payment method (first time). should return both the coupon
            // and payment plan.
            if (!currentRatePlanList.isEmpty()) {
                return currentRatePlanList;
            } else {
                return starterRatePlanList;
            }
        }
        return null;
    }

    /**
     * Get product rate plans for a product (ex: api_cloud)
     * This is the description we maintain in billing.xml
     * Zuora sends the response in multiple pages. Iteratively get the product list of each page. Zuora api request
     * contains a parameter named nextPage which contains the request URL for the next page.
     * <p>
     * Below is a Sample response message with a nextPage value from zuora
     * { [
     * ...
     * "useTenantDefaultForPriceChange" : null,
     * "taxable" : false,
     * "taxCode" : "",
     * "taxMode" : null,
     * "triggerEvent" : "ContractEffective",
     * "description" : ""
     * } ]
     * } ]
     * },
     * { "id" : "2c92c0f850dca6750150dccae664226f", "sku" : "SKU-00000009", "name" : "ch7777", "description" : "",
     * "productRatePlans" : [ ] }
     * ],
     * "nextPage" : "https://apisandbox-api.zuora.com/rest/v1/catalog/products?page=2&pageSize=10",
     * "success" : true
     * }
     *
     * @param productName product name
     * @return Json array of rate plans
     * @throws CloudBillingException
     */
    public static JSONArray getProductRatePlans(String productName) throws CloudBillingException {
        boolean isProductAvailable = false;
        JSONArray ratePlanList = null;
        String response;
        if (productName == null) {
            throw new CloudBillingException("Product name is null, while getting product plans");
        }
        // Zuora api request URL
        String requestUrl = BillingConstants.ZUORA_REST_API_URI_PRODUCTS;
        while (!isProductAvailable) {
            response = zuoraApi.doGet(requestUrl, null, null);
            if (response == null) {
                throw new CloudBillingException(
                        "Zuora api response is null, while getting product rate plans for URL: " + requestUrl);
            }
            try {
                JSONParser jsonParser = new JSONParser();
                JSONObject productJsonObject = (JSONObject) jsonParser.parse(response);
                // Get the status of the request
                boolean success = (Boolean) productJsonObject.get(BillingConstants.PRODUCT_RATE_PLANS_SUCCESS_STATUS);
                if (success) {
                    JSONArray products = ((JSONArray) productJsonObject.get(BillingConstants.PRODUCTS));
                    for (Object product : products) {
                        String zuoraProductName = ((JSONObject) product).get(BillingConstants.NAME).toString();
                        if (zuoraProductName.equals(productName)) {
                            ratePlanList = (JSONArray) ((JSONObject) product).get(BillingConstants.PRODUCT_RATE_PLANS);
                        }
                    }
                    if (ratePlanList == null) {
                        String nextPage;
                        // Get the next page value
                        nextPage = (String) productJsonObject.get(BillingConstants.PRODUCT_RATE_PLANS_NEXTPAGE);
                        if (nextPage != null) {
                            BillingConfig billingConfig = BillingConfigUtils.getBillingConfiguration();
                            ZuoraConfig zuoraConfig = billingConfig.getZuoraConfig();
                            String serviceUrlHost = zuoraConfig.getServiceUrlHost();
                            // Service url value is null if it is same as hostname in httpclientconfig. If this value is
                            // null take value from zuora http configuration.
                            if (null == serviceUrlHost) {
                                serviceUrlHost = zuoraConfig.getHttpClientConfig().getHostname();
                            }
                            // Removing the redundant service URL host value from the nextPage
                            requestUrl = nextPage
                                    .replaceAll(BillingConstants.HTTPS + serviceUrlHost, BillingConstants.EMPTY_STRING)
                                    .trim();
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

    /**
     * Retrieve account subscriptions
     *
     * @param accountId zuora accountId
     * @return json string of subscriptions
     * @throws CloudBillingException
     */
    @SuppressWarnings("unchecked") public static JSONArray getActiveSubscriptionIdsForAccountId(String accountId,
            String productName) throws CloudBillingException {
        String response = getAccountSummary(accountId);
        JSONArray subscriptions = getSubscriptions(accountId, response);
        JSONArray subscriptionIdList = new JSONArray();
        for (Object subscriptionObj : subscriptions) {
            JSONObject subscription = (JSONObject) subscriptionObj;
            //Any of the rate plans can be taken since only there is a one to one mapping between subscription and
            //product according to cloud use case: hence the magic number 0
            JSONObject ratePlan = (JSONObject) ((JSONArray) subscription.get(BillingConstants.RATE_PLANS)).get(0);
            String subscriptionProductPlan = (String) ratePlan.get(BillingConstants.PRODUCT_NAME);
            String subscriptionStatus = (String) subscription.get(BillingConstants.ZUORA_SUBSCRIPTION_STATUS);
            if (productName.equals(subscriptionProductPlan) && BillingConstants.SUBSCRIPTION_STATUS_ACTIVE
                    .equals(subscriptionStatus)) {
                subscriptionIdList.add(subscription.get(BillingConstants.SUBSCRIPTION_NUMBER));
            }
        }
        return subscriptionIdList;
    }

    /**
     * Cancel account subscription
     *
     * @param subscriptionNumber   subscription id
     * @param subscriptionInfoJson subscription details
     * @return success Json string
     * @throws CloudBillingException
     */
    public static String cancelSubscription(String subscriptionNumber, String subscriptionInfoJson)
            throws CloudBillingException {
        return zuoraApi.doPut(BillingConstants.ZUORA_REST_API_URI_CANCEL_SUBSCRIPTION
                        .replace(BillingConstants.SUBSCRIPTION_KEY_PARAM, subscriptionNumber.trim()), null,
                subscriptionInfoJson);
    }

    /**
     * Update account details
     *
     * @param accountId       account id
     * @param accountInfoJson account details
     * @return success Json string
     * @throws CloudBillingException
     */
    public static String updateAccount(String accountId, String accountInfoJson) throws CloudBillingException {
        // Zuora api request URL
        String requestUrl = BillingConstants.ZUORA_REST_API_URI_ACCOUNTS + "/" + accountId.trim();
        return zuoraApi.doPut(requestUrl, null, accountInfoJson);
    }

    /**
     * Create account
     *
     * @param accountInfoJson account details
     * @return success Json string
     * @throws CloudBillingException
     */
    public static String createAccount(String accountInfoJson) throws CloudBillingException {
        // Zuora api request URL
        String requestUrl = BillingConstants.ZUORA_REST_API_URI_ACCOUNTS;
        return zuoraApi.doPost(requestUrl, null, accountInfoJson);
    }

    /**
     * Update subscription details
     *
     * @param subscriptionId       subscription id
     * @param subscriptionInfoJson subscription details
     * @return success Json string
     * @throws CloudBillingException
     */
    public static String updateSubscription(String subscriptionId, String subscriptionInfoJson)
            throws CloudBillingException {
        // Zuora api request URL
        String requestUrl = BillingConstants.ZUORA_REST_API_URI_SUBSCRIPTIONS + "/" + subscriptionId;
        return zuoraApi.doPut(requestUrl, null, subscriptionInfoJson);
    }

    /**
     * Retrieve account details
     *
     * @param accountId account id
     * @return json string of account
     * @throws CloudBillingException
     */
    public static String getAccountDetails(String accountId) throws CloudBillingException {
        // Zuora api request URL
        String requestUrl = BillingConstants.ZUORA_REST_API_URI_ACCOUNTS + "/" + accountId;
        return zuoraApi.doGet(requestUrl, null, null);
    }

    /**
     * Remove payment method
     *
     * @param methodId payment method id
     * @return success Json string
     * @throws CloudBillingException
     */
    public static String removePaymentMethod(String methodId) throws CloudBillingException {
        // Zuora api request URL
        String requestUrl = BillingConstants.ZUORA_REST_API_URI_REMOVE_PAYMENT_METHOD + "/" + methodId;
        return zuoraApi.doDelete(requestUrl, null);
    }

    /**
     * Retrieve all the payment methods
     *
     * @param accountId account id
     * @return success Json string
     * @throws CloudBillingException
     */
    public static String getAllPaymentMethods(String accountId) throws CloudBillingException {
        // Zuora api request URL
        String requestUrl = BillingConstants.ZUORA_REST_API_URI_PAYMENT_METHODS + "/accounts/" + accountId;
        return zuoraApi.doGet(requestUrl, null, null);
    }

    /**
     * Update default payment method
     *
     * @param methodId              method id
     * @param paymentMethodInfoJson payment method details
     * @return success Json string
     * @throws CloudBillingException
     */
    public static String updateDefaultPaymentMethod(String methodId, String paymentMethodInfoJson)
            throws CloudBillingException {
        // Zuora api request URL
        String requestUrl = BillingConstants.ZUORA_REST_API_URI_PAYMENT_METHODS + "/" + methodId;
        return zuoraApi.doPut(requestUrl, null, paymentMethodInfoJson);
    }

    public static String createSubscription(String accountNumber, String ratePlanId, Date planEffectiveDate)
            throws CloudBillingException {
        subscriptionPlanInfoObj.addProperty(BillingConstants.ACCOUNT_KEY, accountNumber);
        subscriptionPlanInfoObj.addProperty(BillingConstants.CONTRACT_EFFECTIVE_DATE,
                new SimpleDateFormat(BillingConstants.DATE_FORMAT).format(planEffectiveDate));

        JsonObject ratePlan = new JsonObject();
        ratePlan.addProperty(BillingConstants.PRODUCT_RATE_PLAN_ID, ratePlanId);
        JsonArray ratePlans = new JsonArray();
        ratePlans.add(ratePlan);
        subscriptionPlanInfoObj.add(BillingConstants.SUBSCRIBED_TO_RATE_PLANS, ratePlans);

        return zuoraApi
                .doPost(BillingConstants.ZUORA_REST_API_URI_SUBSCRIPTIONS, null, subscriptionPlanInfoObj.toString());
    }

    /**
     * Get product rate plan object from zuora for rate plan name and product name
     *
     * @param productName  product name
     * @param ratePlanName rate plan name
     * @return Json object of product rate plan for given product rate plan name
     * @throws CloudBillingException
     */
    public static JSONObject getProductRatePlanObject(String productName, String ratePlanName)
            throws CloudBillingException {
        try {
            JSONArray productPlans = getProductRatePlans(productName);
            for (Object productPlan : productPlans) {
                JSONObject jsonObject = (JSONObject) productPlan;
                String planName = (String) jsonObject.get(BillingConstants.NAME);
                if (planName != null && planName.equals(ratePlanName)) {
                    return jsonObject;
                }
            }
        } catch (CloudBillingException e) {
            String msg = "Error getting product rate plan object for " + ratePlanName;
            throw new CloudBillingException(msg, e);
        }
        return null;
    }
}
