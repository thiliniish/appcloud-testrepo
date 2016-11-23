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
package org.wso2.carbon.cloud.billing.vendor.stripe;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Charge;
import com.stripe.model.Coupon;
import com.stripe.model.Customer;
import com.stripe.model.CustomerSubscriptionCollection;
import com.stripe.model.ExternalAccount;
import com.stripe.model.Invoice;
import com.stripe.model.Plan;
import com.stripe.model.Subscription;
import com.stripe.net.RequestOptions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.wso2.carbon.cloud.billing.core.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.core.commons.CloudBillingServiceProvider;
import org.wso2.carbon.cloud.billing.vendor.commons.BillingVendorConstants;
import org.wso2.carbon.cloud.billing.vendor.commons.utils.BillingVendorConfigUtils;
import org.wso2.carbon.cloud.billing.vendor.stripe.exceptions.CloudBillingVendorException;
import org.wso2.carbon.cloud.billing.vendor.stripe.utils.APICloudMonetizationUtils;
import org.wso2.carbon.cloud.billing.vendor.stripe.utils.CloudBillingVendorUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents class for Stripe related monetization functionalities.
 */
public class StripeCloudMonetization implements CloudBillingServiceProvider {

    private static final Log LOGGER = LogFactory.getLog(StripeCloudMonetization.class);
    private static Gson gsonObj = new Gson();
    private Map<String, Object> customerParams = new HashMap<>();
    private Map<String, Object> planParams = new HashMap<>();
    private Map<String, Object> subscriptionParams = new HashMap<>();
    private Map<String, Object> cardParams = new HashMap<>();
    private Map<String, Object> invoiceParams = new HashMap<>();
    private static RequestOptions requestOptions;

    public StripeCloudMonetization(String tenantDomain) throws CloudBillingVendorException {
        setRequestOptions(tenantDomain);
    }

    /**
     * Method to set the request option
     *
     * @param tenantDomain api key of tenant
     */
    private static void setRequestOptions(String tenantDomain) throws CloudBillingVendorException {
        try {
            String apiKey = getSecretKey(tenantDomain);
            requestOptions = (new RequestOptions.RequestOptionsBuilder()).setApiKey(apiKey).build();
        } catch (CloudBillingVendorException e) {
            throw new CloudBillingVendorException(
                    "Cloud Billing Exception Occurred while setting vendor request option for tenant : " + tenantDomain,
                    e);
        }
    }

    /**
     * Get secret key for the tenant
     *
     * @param tenantDomain tenant domain
     * @return secret key
     * @throws CloudBillingVendorException
     */
    private static String getSecretKey(String tenantDomain) throws CloudBillingVendorException {
        try {
            return APICloudMonetizationUtils.getSecretKey(tenantDomain);
        } catch (CloudBillingVendorException e) {
            throw new CloudBillingVendorException(
                    "Cloud Billing Exception Occurred while getting Secret key for tenant : " + tenantDomain, e);
        }
    }

    /**
     * Create the Customer for monetization
     *
     * @param customerInfoJson customer details
     *                         {
     *                         "id": "tenant_sithu_001",
     *                         "account_balance": 0,
     *                         "source": "tok_18vQTSFteW7cCxnd45AXjm4E",
     *                         "description": "WSO2 Customer",
     *                         "coupon": "coupon_fixed",
     *                         "email": "wso2customer@wso2.com"
     *                         }
     * @return success Json string
     */
    @Override public String createCustomer(String customerInfoJson) throws CloudBillingVendorException {
        try {
            customerParams.clear();
            customerParams = ObjectParams.setObjectParams(customerInfoJson);
            return CloudBillingVendorUtils
                    .validateResponseString(Customer.create(customerParams, requestOptions).toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while creating customer : ", ex);
        }
    }

    /**
     * Retrieve customer details  for Monetization
     *
     * @param customerId customer id
     * @return json string of customer information
     */
    @Override public String getCustomerDetails(String customerId) throws CloudBillingVendorException {
        JsonObject response = new JsonObject();
        try {
            Customer customer = Customer.retrieve(customerId, requestOptions);
            JsonObject customerJsonObj =
                    new JsonParser().parse(CloudBillingVendorUtils.validateResponseString(customer.toString()))
                                    .getAsJsonObject();
            response.addProperty(BillingVendorConstants.RESPONSE_SUCCESS, true);
            response.add(BillingVendorConstants.RESPONSE_DATA, customerJsonObj);
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            response.addProperty(BillingVendorConstants.RESPONSE_SUCCESS, false);
            response.addProperty(BillingVendorConstants.RESPONSE_MESSAGE, ex.getMessage());
            response.add(BillingVendorConstants.RESPONSE_DATA, null);
            LOGGER.error("Error while retrieving customer : ", ex);
        }
        return response.toString();
    }

    /**
     * Update Customer Info for monetization
     *
     * @param customerId       customerId Id
     * @param customerInfoJson customer details
     *                         {
     *                         "account_balance": 0,
     *                         "default_source": "card_18wF2AFteW7cCxndwoqOBnlS",
     *                         "source": "tok_18vQTSFteW7cCxnd45AXjm4E",
     *                         "description": "Customer for joseph.davis@example.com",
     *                         "coupon": "coupon_fixed",
     *                         "email": "wso2customer@wso2.com",
     *                         }
     * @return success Json string
     */
    @Override public String updateCustomer(String customerId, String customerInfoJson)
            throws CloudBillingVendorException {
        JsonObject response = new JsonObject();
        try {
            Customer customer = Customer.retrieve(customerId, requestOptions);
            customerParams.clear();
            customerParams = ObjectParams.setObjectParams(customerInfoJson);
            JsonObject customerJsonObj = new JsonParser().parse(CloudBillingVendorUtils.validateResponseString(
                    customer.update(customerParams, requestOptions).toString())).getAsJsonObject();
            response.addProperty(BillingVendorConstants.RESPONSE_SUCCESS, true);
            response.add(BillingVendorConstants.RESPONSE_DATA, customerJsonObj);
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            response.addProperty(BillingVendorConstants.RESPONSE_SUCCESS, false);
            response.addProperty(BillingVendorConstants.RESPONSE_MESSAGE, ex.getMessage());
            response.add(BillingVendorConstants.RESPONSE_DATA, null);
            LOGGER.error("Error while updating customer : ", ex);
        }
        return response.toString();
    }

    /**
     * Delete customer for monetization
     *
     * @param customerId customer Id
     * @return success json string
     */
    @Override public String deleteCustomer(String customerId) throws CloudBillingVendorException {
        try {
            Customer customer = Customer.retrieve(customerId, requestOptions);
            return CloudBillingVendorUtils.validateResponseString(customer.delete(requestOptions).toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while deleting customer : ", ex);
        }
    }

    /**
     * Create rate plan for the Product
     *
     * @param ratePlanInfoJson rate-plan details
     *                         {
     *                         "id": "Gold",
     *                         "amount": 10000,
     *                         "currency": "usd",
     *                         "interval": "month",
     *                         "interval_count": 1,
     *                         "metadata": {
     *                         "product": "api_cloud"
     *                         },
     *                         "name": "Gold"
     *                         }
     * @return success json string
     */
    @Override public String createProductRatePlan(String tenantDomain, String ratePlanInfoJson)
            throws CloudBillingVendorException {
        try {
            planParams.clear();
            planParams = ObjectParams.setObjectParams(ratePlanInfoJson);
            return CloudBillingVendorUtils.validateResponseString(Plan.create(planParams, requestOptions).toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while creating rate-plan : ", ex);
        }
    }

    /**
     * retrieve a specific rate plan for monetization
     *
     * @param ratePlanId rate-plan Id
     * @return success json string
     */
    @Override public String getProductRatePlan(String ratePlanId) throws CloudBillingVendorException {
        try {
            return CloudBillingVendorUtils.validateResponseString(Plan.retrieve(ratePlanId, requestOptions).toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while retrieving rate-plan : ", ex);
        }
    }

    /**
     * Method to update Product rate plan for monetization
     *
     * @param planId           rate plan ID
     * @param ratePlanInfoJson rate-plan details
     *                         {
     *                         "metadata": {
     *                         "product": "api_cloud"
     *                         },
     *                         "name": "Gold"
     *                         }
     *                         plan details like price, interval, etc.. are, by design, not editable.
     * @return success json string
     */
    @Override public String updateProductRatePlan(String planId, String ratePlanInfoJson)
            throws CloudBillingVendorException {
        try {
            Plan plan = Plan.retrieve(planId, requestOptions);
            planParams.clear();
            planParams = ObjectParams.setObjectParams(ratePlanInfoJson);
            return CloudBillingVendorUtils.validateResponseString(plan.update(planParams, requestOptions).toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while updating rate-plan : ", ex);
        }
    }

    /**
     * Method to delete a specific Product rate plan for monetization
     *
     * @param ratePlanId rate-plan Id
     * @return success json string
     */
    @Override public String deleteProductRatePlan(String ratePlanId) throws CloudBillingVendorException {
        try {
            return CloudBillingVendorUtils.validateResponseString(Plan.retrieve(ratePlanId, requestOptions).toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while deleting rate-plan : ", ex);
        }
    }

    /**
     * Method to retrieve all the product rate-plans for monetization
     *
     * @param ratePlanInfoJson rate-plan details
     *                         {
     *                         "ending_before": "planObj1",
     *                         "starting_after": "planObj2",
     *                         "limit": "20"
     *                         }
     * @return a list of rate plans
     */
    @Override public String getAllProductRatePlans(String ratePlanInfoJson) throws CloudBillingVendorException {
        try {
            planParams.clear();
            planParams = ObjectParams.setObjectParams(ratePlanInfoJson);
            return CloudBillingVendorUtils.validateResponseString(Plan.list(planParams, requestOptions).toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while retrieving all rate-plans : ", ex);
        }
    }

    /**
     * Create a subscription for monetization
     *
     * @param subscriptionInfoJson subscription details. This includes customer id and the product rate-plan id
     *                             {
     *                             "plan": "Gold",
     *                             "customer": "tenant_sithu_001",
     *                             "application_fee_percent": "20000"
     *                             }
     * @return success Json string
     */
    @Override public String createSubscription(String subscriptionInfoJson) throws CloudBillingVendorException {
        try {
            subscriptionParams.clear();
            subscriptionParams = ObjectParams.setObjectParams(subscriptionInfoJson);

            Subscription subscription = Subscription.create(subscriptionParams, requestOptions);

            JsonObject dataObj = new JsonObject();
            dataObj.addProperty(BillingConstants.PARAM_SUBSCRIPTION_NUMBER, subscription.getId());

            JsonObject response = new JsonObject();
            response.addProperty(BillingVendorConstants.RESPONSE_SUCCESS, true);
            response.add(BillingVendorConstants.RESPONSE_DATA, dataObj);

            return response.toString();
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while creating subscription : " + ex.getMessage(), ex);
        }
    }

    /**
     * Retrieve a subscription for monetization
     *
     * @param subscriptionId subscription Id
     * @return success Json string
     */
    @Override public String getSubscription(String subscriptionId) throws CloudBillingVendorException {
        try {
            return CloudBillingVendorUtils
                    .validateResponseString(Subscription.retrieve(subscriptionId, requestOptions).toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while retrieving subscription : ", ex);
        }
    }

    /**
     * Method to retrieve all the subscriptions for monetization
     *
     * @param subscriptionInfoJson subscription details.
     * @return a list of subscriptions
     */
    @Override public String getAllSubscriptions(String subscriptionInfoJson) throws CloudBillingVendorException {
        try {
            subscriptionParams.clear();
            subscriptionParams = ObjectParams.setObjectParams(subscriptionInfoJson);
            return CloudBillingVendorUtils
                    .validateResponseString(Subscription.list(subscriptionParams, requestOptions).toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while retrieving all subscriptions : ", ex);
        }
    }

    /**
     * Update subscription for monetization
     *
     * @param subscriptionId       subscription Id
     * @param subscriptionInfoJson subscription details for downgrade or upgrade. This includes customer id and the
     *                             product rate-plan id
     *                             {
     *                             "plan": "Gold",
     *                             "coupon": "coupon_fixed",
     *                             "prorate": "true",
     *                             "proration_date": "1474485877",
     *                             "source": "tok_18vQTSFteW7cCxnd45AXjm4E",
     *                             "application_fee_percent": "20000",
     *                             "metadata": {
     *                             "product": "api_cloud"
     *                             },
     *                             }
     * @param isUpgrade            is Upgrade subscription
     * @return success Json string
     */
    @Override public String updateSubscription(String subscriptionId, String subscriptionInfoJson, boolean isUpgrade)
            throws CloudBillingVendorException {
        JsonObject returnResponse = new JsonObject();
        try {
            subscriptionParams.clear();
            subscriptionParams = ObjectParams.setObjectParams(subscriptionInfoJson);
            Subscription subscription = Subscription.retrieve(subscriptionId, requestOptions);

            //Remove discount if avail
            if (subscription.getDiscount() != null) {
                deleteSubscriptionDiscount(subscriptionId);
            }
            JsonObject response = new JsonParser().parse(CloudBillingVendorUtils.validateResponseString(
                    subscription.update(subscriptionParams, requestOptions).toString())).getAsJsonObject();
            LOGGER.info("Subscription has being successfully updated for customer: " + subscription.getCustomer());

            // if successfully update the if its and upgrade then immediately charge for the proration
            if (response.get("id") != null && isUpgrade) {
                JsonObject immediateChargeObject = new JsonObject();
                immediateChargeObject.addProperty("description", "Prorated Charges");
                immediateChargeObject.addProperty("customer", subscription.getCustomer());

                String createInvoiceResponse = createInvoice(immediateChargeObject.toString());
                JsonNode createInvoiceResponseObj = APICloudMonetizationUtils.getJsonList(createInvoiceResponse);
                if (createInvoiceResponseObj.get("success").asBoolean()) {
                    String chargeInvoiceResult = chargeInvoice(createInvoiceResponseObj.get("data").asText());
                    JsonNode chargeInvoiceResultobj = APICloudMonetizationUtils.getJsonList(chargeInvoiceResult);
                    if (chargeInvoiceResultobj.get("success").asBoolean()) {
                        LOGGER.info(
                                "Successfully charged the prorated amount for customer: " + subscription.getCustomer());
                    }
                }
            }
            returnResponse.addProperty(BillingVendorConstants.RESPONSE_SUCCESS, true);
            returnResponse.add(BillingVendorConstants.RESPONSE_DATA, response);
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException | IOException ex) {
            returnResponse.addProperty(BillingVendorConstants.RESPONSE_SUCCESS, false);
            returnResponse.addProperty(BillingVendorConstants.RESPONSE_MESSAGE, ex.getMessage());
            returnResponse.add(BillingVendorConstants.RESPONSE_DATA, null);
            LOGGER.error("Error while updating subscription: " + subscriptionId, ex);
        }
        return returnResponse.toString();
    }

    /**
     * Cancel subscription by the subscription id for monetization
     *
     * @param subscriptionId       subscription id
     * @param subscriptionInfoJson this is not required for Stripe
     * @return success jason string
     */
    @Override public String cancelSubscription(String subscriptionId, String subscriptionInfoJson)
            throws CloudBillingVendorException {
        try {
            Subscription subscription = Subscription.retrieve(subscriptionId, requestOptions);
            subscriptionParams.clear();
            // We cancel the subscription at the end of currently subscribed time period
            subscriptionParams.put(BillingVendorConstants.AT_PERIOD_END, true);
            subscription = subscription.cancel(subscriptionParams, requestOptions);

            JsonObject subscriptionJsonObj =
                    new JsonParser().parse(CloudBillingVendorUtils.validateResponseString(subscription.toString()))
                                    .getAsJsonObject();

            JsonObject response = new JsonObject();
            if (subscription.getCancelAtPeriodEnd()) {
                response.addProperty(BillingVendorConstants.RESPONSE_SUCCESS, true);
                response.add(BillingVendorConstants.RESPONSE_DATA, subscriptionJsonObj);
            } else {
                response.addProperty(BillingVendorConstants.RESPONSE_SUCCESS, false);
                response.addProperty(BillingVendorConstants.RESPONSE_MESSAGE,
                                     "Subscription cancellation was not successful");
                response.add(BillingVendorConstants.RESPONSE_DATA, subscriptionJsonObj);
            }
            return response.toString();
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException
                | APIException ex) {
            throw new CloudBillingVendorException("Error while cancelling subscription : " + ex.getMessage(), ex);
        }
    }

    /**
     * Add default payment method to a specific customer for monetization
     *
     * @param customerId            customer Id
     * @param paymentMethodInfoJson payment method details
     * @return success Json string
     */
    @Override public String addPaymentMethod(String customerId, String paymentMethodInfoJson)
            throws CloudBillingVendorException {
        JsonObject response = new JsonObject();
        try {
            Customer customer = Customer.retrieve(customerId, requestOptions);
            cardParams.clear();
            cardParams = ObjectParams.setObjectParams(paymentMethodInfoJson);
            JsonElement customerDetails = new JsonParser().parse(getCustomerDetails(customerId));
            JsonElement paymentMethodSuccess = new JsonParser().parse(CloudBillingVendorUtils.validateResponseString(
                    customer.getSources().create(cardParams, requestOptions).toString()));
            JsonObject responseData = new JsonObject();
            responseData.add("default_payment_method",
                             customerDetails.getAsJsonObject().get("data").getAsJsonObject().get("default_source"));
            responseData.add("add_payment_method_response", paymentMethodSuccess);
            response.addProperty(BillingVendorConstants.RESPONSE_SUCCESS, true);
            response.add(BillingVendorConstants.RESPONSE_DATA, responseData);
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while adding the new payment method : ", ex);
        }
        return response.toString();
    }

    /**
     * Set specific payment method of a specific customer as default for monetization
     *
     * @param customerId            customer Id
     * @param paymentMethodInfoJson payment method details
     *                              {
     *                              "default_source": "card_18wF2AFteW7cCxndwoqOBnlS"
     *                              }
     *                              default_source : ID of source to make the customer’s new default for invoice
     *                              payments
     * @return success Json string
     */
    @Override public String setDefaultPaymentMethod(String customerId, String paymentMethodInfoJson)
            throws CloudBillingVendorException {
        JsonObject response = new JsonObject();
        try {
            JsonElement customerJsonObj = new JsonParser().parse(CloudBillingVendorUtils.validateResponseString(
                    updateCustomer(customerId, paymentMethodInfoJson)));
            response.addProperty(BillingVendorConstants.RESPONSE_SUCCESS, true);
            response.add(BillingVendorConstants.RESPONSE_DATA, customerJsonObj);
        } catch (CloudBillingVendorException ex) {
            response.addProperty(BillingVendorConstants.RESPONSE_SUCCESS, false);
            response.addProperty(BillingVendorConstants.RESPONSE_MESSAGE, ex.getMessage());
            response.add(BillingVendorConstants.RESPONSE_DATA, null);
            LOGGER.error("Error while setting the default payment method : ", ex);
        }
        return response.toString();
    }

    /**
     * Update default payment method for monetization
     *
     * @param customerId            customer Id
     * @param paymentMethodId       payment method id
     * @param paymentMethodInfoJson payment method details
     *                              {
     *                              "address_city": "colombo",
     *                              "address_country": "sri lanka",
     *                              "address_line1": "temple road",
     *                              "address_line2": "kotte",
     *                              "address_zip": "000100",
     *                              "exp_month": 8,
     *                              "exp_year": 2017,
     *                              "default_for_currency": "false",
     *                              "metadata": {
     *                              },
     *                              "name": "Emma Robinson"
     *                              }
     *                              "default_for_currency" : Only applicable on monetization accounts (not customers or
     *                              recipients). If set to true, this card will become the default external account
     *                              for its currency.
     * @return success Json string
     */
    @Override public String updatePaymentMethod(String customerId, String paymentMethodId, String paymentMethodInfoJson)
            throws CloudBillingVendorException {
        try {
            Customer customer = Customer.retrieve(customerId, requestOptions);
            ExternalAccount cardInfo = customer.getSources().retrieve(paymentMethodId, requestOptions);
            cardParams.clear();
            cardParams = ObjectParams.setObjectParams(paymentMethodInfoJson);
            return CloudBillingVendorUtils
                    .validateResponseString(cardInfo.update(cardParams, requestOptions).toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while updating payment method : ", ex);
        }
    }

    /**
     * Retrieve all payment methods of a specific customer for monetization
     *
     * @param customerId            customer Id
     * @param paymentMethodInfoJson payment method details
     *                              {
     *                              "ending_before": "cardObj1",
     *                              "starting_after": "cardObj2",
     *                              "limit": "2",
     *                              "object": "card"
     *                              }
     *                              "object": "card" this is required.
     * @return success Json string
     */
    @Override public String getAllPaymentMethods(String customerId, String paymentMethodInfoJson)
            throws CloudBillingVendorException {
        JsonObject response = new JsonObject();
        try {
            cardParams.clear();
            cardParams = ObjectParams.setObjectParams(paymentMethodInfoJson);
            JsonElement paymentMethod = new JsonParser().parse(CloudBillingVendorUtils.validateResponseString(
                    Customer.retrieve(customerId, requestOptions).getSources().all(cardParams, requestOptions)
                            .toString()));
            response.addProperty(BillingVendorConstants.RESPONSE_SUCCESS, true);
            response.add(BillingVendorConstants.RESPONSE_DATA, paymentMethod);
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while retrieving all payment methods : ", ex);
        }
        return response.toString();
    }

    /**
     * Remove payment method/ card of a customer for monetization
     *
     * @param customerId      customer Id
     * @param paymentMethodId payment method id
     * @return success jason string
     */
    @Override public String removePaymentMethod(String customerId, String paymentMethodId)
            throws CloudBillingVendorException {
        JsonObject response = new JsonObject();
        try {
            JsonElement deleteStatus = new JsonParser().parse(CloudBillingVendorUtils.validateResponseString(
                    Customer.retrieve(customerId, requestOptions).getSources().retrieve(paymentMethodId, requestOptions)
                            .delete(requestOptions).toString()));
            response.addProperty(BillingVendorConstants.RESPONSE_SUCCESS, true);
            response.add(BillingVendorConstants.RESPONSE_DATA, deleteStatus);
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            response.addProperty(BillingVendorConstants.RESPONSE_SUCCESS, false);
            response.addProperty(BillingVendorConstants.RESPONSE_MESSAGE, ex.getMessage());
            response.add(BillingVendorConstants.RESPONSE_DATA, null);
            LOGGER.error("Error while removing payment method : ", ex);
        }
        return response.toString();
    }

    /**
     * Method to create Monetization account for monetization
     *
     * @param customerId                  monetization customer id
     * @param monetizationAccountInfoJson monetization account info
     *                                    {
     *                                    "accountNumber": "acc999999",
     *                                    "access_token": "sk_test_UVFvcC6Exzpe5Lelq5IAZa9z",
     *                                    "livemode": false,
     *                                    "refresh_token": "rt_9MTBa0i5i5FLg1znSlWnuQF8jIHuT4x",
     *                                    "token_type": "bearer",
     *                                    "stripe_publishable_key": "pk_test_HHdWwgu",
     *                                    "stripe_user_id": "acct_193k8qDMGlVlrnlQ",
     *                                    "scope": "read_write"
     *                                    }
     *                                    these values add to database
     * @return success jason string
     */
    @Override public String createMonetizationAccount(String customerId, String monetizationAccountInfoJson)
            throws CloudBillingVendorException {
        // Add standalone account creation response values to database.
        return String
                .valueOf(APICloudMonetizationUtils.addMonetizationAccount(customerId, monetizationAccountInfoJson));
    }

    /**
     * Retrieve invoices associated with a customer for monetization
     *
     * @param invoiceInfoJson invoice retrieval info for a specific customer
     *                        {
     *                        "customer": "Emma_Robinson001",
     *                        "ending_before": "invoiceObj1",
     *                        "starting_after": "invoiceObj2",
     *                        "limit": "20",
     *                        "date": "1474198894"
     *                        }
     *                        "customer" is required.
     * @return Json String of invoices
     */
    @Override public String getInvoices(String invoiceInfoJson) throws CloudBillingVendorException {
        try {
            invoiceParams.clear();
            invoiceParams = ObjectParams.setObjectParams(invoiceInfoJson);
            return CloudBillingVendorUtils
                    .validateResponseString(Invoice.list(invoiceParams, requestOptions).toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while getting invoices : ", ex);
        }
    }

    /**
     * Retrieve invoice details for monetization
     *
     * @param invoiceId invoice id
     * @return json string of invoice information
     */
    @Override public String getInvoiceDetails(String invoiceId) throws CloudBillingVendorException {
        try {
            return CloudBillingVendorUtils
                    .validateResponseString(Invoice.retrieve(invoiceId, requestOptions).toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while retrieving invoice : ", ex);
        }
    }

    /**
     * Retrieve invoices associated with a customer for monetization
     *
     * @param invoiceInfoJson invoice creation info
     * @return String of invoices id
     */
    @Override public String createInvoice(String invoiceInfoJson) throws CloudBillingVendorException {
        JsonObject response = new JsonObject();
        try {
            Map<String, Object> invoiceParam = ObjectParams.setObjectParams(invoiceInfoJson);
            Invoice invoice = Invoice.create(invoiceParam, requestOptions);
            JsonObject invoiceJsonObj =
                    new JsonParser().parse(CloudBillingVendorUtils.validateResponseString(invoice.toString()))
                                    .getAsJsonObject();
            response.addProperty(BillingVendorConstants.RESPONSE_SUCCESS, true);
            response.add(BillingVendorConstants.RESPONSE_DATA, invoiceJsonObj.get("id"));
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            response.addProperty(BillingVendorConstants.RESPONSE_SUCCESS, false);
            response.addProperty(BillingVendorConstants.RESPONSE_MESSAGE, ex.getMessage());
            response.add(BillingVendorConstants.RESPONSE_DATA, null);
            LOGGER.error("Error while creating the invoice : ", ex);
        }
        return response.toString();
    }

    /**
     * Charge the customer associated with the given invoice for monetization
     *
     * @param invoiceId invoice id
     * @return json string of invoice information
     */
    @Override public String chargeInvoice(String invoiceId) throws CloudBillingVendorException {
        JsonObject response = new JsonObject();
        try {
            Invoice invoice = Invoice.retrieve(invoiceId, requestOptions);
            Invoice invoiceData = invoice.pay(requestOptions);
            JsonObject invoiceJsonObj =
                    new JsonParser().parse(CloudBillingVendorUtils.validateResponseString(invoiceData.toString()))
                                    .getAsJsonObject();
            response.addProperty(BillingVendorConstants.RESPONSE_SUCCESS, true);
            response.add(BillingVendorConstants.RESPONSE_DATA, invoiceJsonObj);
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            response.addProperty(BillingVendorConstants.RESPONSE_SUCCESS, false);
            response.addProperty(BillingVendorConstants.RESPONSE_MESSAGE, ex.getMessage());
            response.add(BillingVendorConstants.RESPONSE_DATA, null);
            LOGGER.error("Error while charging for invoice: " + invoiceId, ex);
        }
        return response.toString();
    }

    /**
     * Get current plan subscribed to a service for monetization
     *
     * @param customerId customer id
     * @return current active rate plan
     */
    @Override public String getCurrentRatePlan(String customerId) throws CloudBillingVendorException {
        try {
            Customer customer = Customer.retrieve(customerId, requestOptions);
            CustomerSubscriptionCollection subscriptionCollection = customer.getSubscriptions();
            for (int i = 0; i < subscriptionCollection.getData().size(); i++) {
                if (customer.getSubscriptions().getData().get(i).getStatus()
                            .equals(BillingVendorConstants.ACTIVE_RESPONSE)) {
                    return customer.getSubscriptions().getData().get(i).getPlan().getId();
                }
            }
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException(
                    "Error while retrieving current rate plan for customer : " + customerId, ex);
        }
        return null;
    }

    /**
     * Get customer coupons for monetization
     *
     * @param customerId customer id
     * @return current coupons
     * @throws CloudBillingVendorException
     */
    @Override public String getCustomerCoupons(String customerId) throws CloudBillingVendorException {
        try {
            return CloudBillingVendorUtils
                    .validateResponseString(Customer.retrieve(customerId, requestOptions).getDiscount().toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException(
                    "Error occurred while retrieving the coupons of the customer : " + customerId, ex);
        }
    }

    /**
     * Get a specific coupon details for monetization
     *
     * @param couponID coupon id
     * @return coupon data
     * @throws CloudBillingVendorException
     */
    @Override public String retrieveCouponInfo(String couponID) throws CloudBillingVendorException {
        try {
            return CloudBillingVendorUtils.validateResponseString(Coupon.retrieve(couponID, requestOptions).toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException(
                    "Error occurred while retrieving the coupons information of the coupon : " + couponID, ex);
        }
    }

    /**
     * Retrieve the publishable key for monetization
     *
     * @return json publishable key
     */
    public String getPublishableKeyForTenant(String tenantDomain) throws CloudBillingVendorException {
        try {
            return APICloudMonetizationUtils.getPublishableKeyForTenant(tenantDomain);
        } catch (CloudBillingVendorException e) {
            throw new CloudBillingVendorException("Error while obtaining publishable key for tenant : " + tenantDomain,
                                                  e);
        }
    }

    /**
     * Get a specific account details for monetization
     *
     * @param customerId coupon id
     * @return account information
     * @throws CloudBillingVendorException
     */
    @Override public String retrieveAccountInfo(String customerId) throws CloudBillingVendorException {
        JsonObject response = new JsonObject();
        try {
            String customerDetails = getCustomerDetails(customerId);
            JsonObject object = new JsonObject();
            object.addProperty("customer", customerId);
            String customerInvoices = getInvoices(object.toString());
            JsonNode customerObj = APICloudMonetizationUtils.getJsonList(customerDetails);
            JsonNode invoiceObj = APICloudMonetizationUtils.getJsonList(customerInvoices);
            JsonObject accountInfo = new JsonObject();
            // Validate if customer details are successfully retrieved
            if (Boolean.parseBoolean(customerObj.get(BillingVendorConstants.RESPONSE_SUCCESS).toString())) {
                JsonObject accountSummaryObj = new JsonObject();
                JsonObject subscriptionDetailsObj = new JsonObject();
                JsonObject defaultPaymentMethodObj = new JsonObject();
                JsonObject contactInformationObj = new JsonObject();
                ArrayList<String> invoiceArrayList = new ArrayList<String>();
                ArrayList<String> paymentArrayList = new ArrayList<String>();

                accountSummaryObj.addProperty("accountName",
                                              customerObj.get(BillingVendorConstants.RESPONSE_DATA).get("description")
                                                         .asText());
                accountSummaryObj.addProperty("accountBalance", customerObj.get(BillingVendorConstants.RESPONSE_DATA)
                                                                           .get("account_balance").asText());
                accountSummaryObj.addProperty("currency",
                                              customerObj.get(BillingVendorConstants.RESPONSE_DATA).get("currency")
                                                         .asText());

                JsonNode subscriptionNode =
                        customerObj.get(BillingVendorConstants.RESPONSE_DATA).get("subscriptions").get("data").get(0);
                subscriptionDetailsObj.addProperty("serviceName", subscriptionNode.get("plan").get("name").asText());
                subscriptionDetailsObj.addProperty("startDate", convertUnixTimestamp(
                        BillingVendorConstants.DATE_FORMAT_YEAR_MONTH_DAY, subscriptionNode.get("start").asLong()));
                subscriptionDetailsObj.addProperty("endDate", convertUnixTimestamp(
                        BillingVendorConstants.DATE_FORMAT_YEAR_MONTH_DAY, subscriptionNode.get("ended_at").asLong()));
                subscriptionDetailsObj.addProperty("status", subscriptionNode.get("status").asText());
                subscriptionDetailsObj
                        .addProperty("isCancelled", subscriptionNode.get("cancel_at_period_end").asText());

                // payment Details
                JsonNode paymentNode = customerObj.get(BillingVendorConstants.RESPONSE_DATA).get("sources").get("data");
                JsonNode defaultPaymentNode = null;
                for (int x = 0; x < paymentNode.size(); x++) {
                    String defualtPaymentId =
                            customerObj.get(BillingVendorConstants.RESPONSE_DATA).get("default_source").toString();
                    if (defualtPaymentId.equalsIgnoreCase(paymentNode.get(x).get("id").toString())) {
                        defaultPaymentNode = paymentNode.get(x);
                    }
                }
                if (defaultPaymentNode != null) {
                    defaultPaymentMethodObj.addProperty("paymentId", defaultPaymentNode.get("id").asText());
                    defaultPaymentMethodObj.addProperty("paymentMethodType", defaultPaymentNode.get("object").asText());
                    defaultPaymentMethodObj.addProperty("paymentType", defaultPaymentNode.get("brand").asText());
                    defaultPaymentMethodObj
                            .addProperty("expirationMonth", defaultPaymentNode.get("exp_month").asText());
                    defaultPaymentMethodObj.addProperty("expirationYear", defaultPaymentNode.get("exp_year").asText());
                    defaultPaymentMethodObj
                            .addProperty("cardNumber", "************" + defaultPaymentNode.get("last4").asText());
                }

                // Customer Details
                JsonNode contactDetailsNode = customerObj.get(BillingVendorConstants.RESPONSE_DATA).get("shipping");
                contactInformationObj.addProperty("name", contactDetailsNode.get("name").asText());
                contactInformationObj.addProperty("firstName",
                                                  customerObj.get(BillingVendorConstants.RESPONSE_DATA).get("metadata")
                                                             .get("firstName").asText());
                contactInformationObj.addProperty("lastName",
                                                  customerObj.get(BillingVendorConstants.RESPONSE_DATA).get("metadata")
                                                             .get("lastName").asText());
                contactInformationObj.addProperty("state", contactDetailsNode.get("address").get("state").asText());
                contactInformationObj.addProperty("city", contactDetailsNode.get("address").get("city").asText());
                contactInformationObj.addProperty("country", contactDetailsNode.get("address").get("country").asText());
                contactInformationObj
                        .addProperty("postalcode", contactDetailsNode.get("address").get("postal_code").asText());
                contactInformationObj.addProperty("address1", contactDetailsNode.get("address").get("line1").asText());
                contactInformationObj.addProperty("address2", contactDetailsNode.get("address").get("line2").asText());
                contactInformationObj.addProperty("email",
                                                  customerObj.get(BillingVendorConstants.RESPONSE_DATA).get("email")
                                                             .asText());

                for (int x = 0; x < invoiceObj.get("data").size(); x++) {
                    JsonNode invoiceItem = invoiceObj.get("data").get(x);
                    JsonObject invoiceItemObj = new JsonObject();
                    JsonObject chargeItemObj = new JsonObject();
                    invoiceItemObj.addProperty("date",
                                               convertUnixTimestamp(BillingVendorConstants.DATE_FORMAT_YEAR_MONTH_DAY,
                                                                    invoiceItem.get("date").asLong()));
                    invoiceItemObj.addProperty("InvoiceId", invoiceItem.get("id").asText());
                    invoiceItemObj.addProperty("TargetDate",
                                               convertUnixTimestamp(BillingVendorConstants.DATE_FORMAT_YEAR_MONTH_DAY,
                                                                    invoiceItem.get("period_end").asLong()));
                    invoiceItemObj.addProperty("Amount", invoiceItem.get("total").asText());
                    invoiceItemObj.addProperty("paid", invoiceItem.get("paid").asText());
                    invoiceArrayList.add(invoiceItemObj.toString());
                    if (!invoiceItem.get("charge").isNull()) {
                        //get ChargeObject
                        String chargeDetails = getChargedDetails(invoiceItem.get("charge").asText());
                        JsonNode chargeObj = APICloudMonetizationUtils.getJsonList(chargeDetails);
                        chargeItemObj.addProperty("type", chargeObj.get("data").get("source").get("object").asText());
                        chargeItemObj.addProperty("effectiveDate", convertUnixTimestamp(
                                BillingVendorConstants.DATE_FORMAT_YEAR_MONTH_DAY,
                                chargeObj.get("data").get("created").asLong()));
                        chargeItemObj.addProperty("paymentNumber", chargeObj.get("data").get("id").asText());
                        chargeItemObj.addProperty("invoiceNumber", invoiceItem.get("id").asText());
                        chargeItemObj.addProperty("Status", chargeObj.get("data").get("status").asText());
                        paymentArrayList.add(chargeItemObj.toString());
                    }

                }

                String st = new Gson().toJson(invoiceArrayList);
                JsonArray invoiceJsonObject = (new JsonParser()).parse(st).getAsJsonArray();

                st = new Gson().toJson(paymentArrayList);
                JsonArray paymentJsonObject = (new JsonParser()).parse(st).getAsJsonArray();

                accountInfo.add("accountSummary", accountSummaryObj);
                accountInfo.add("subscriptionDetails", subscriptionDetailsObj);
                accountInfo.add("defaultPaymentDetails", defaultPaymentMethodObj);
                accountInfo.add("contactDetails", contactInformationObj);
                accountInfo.add("invoicesInformation", invoiceJsonObject);
                accountInfo.add("chargeInformation", paymentJsonObject);

                response.addProperty(BillingVendorConstants.RESPONSE_SUCCESS, true);
                response.add(BillingVendorConstants.RESPONSE_DATA, accountInfo);

                return CloudBillingVendorUtils.validateResponseString(response.toString());
            } else {
                return customerDetails;
            }
        } catch (IOException e) {
            LOGGER.error("Error occurred while retrieving account info ", e);
            response.addProperty(BillingVendorConstants.RESPONSE_SUCCESS, false);
            response.addProperty(BillingVendorConstants.RESPONSE_MESSAGE, e.getMessage());
            return response.toString();

        }
    }

    /**
     * Get Charge information for monetization
     *
     * @param chargeId subscription id
     * @return json charge information json string
     */
    public String getChargedDetails(String chargeId) throws CloudBillingVendorException {

        JsonObject response = new JsonObject();
        try {
            Charge charge = Charge.retrieve(chargeId, requestOptions);
            JsonObject chargeJsonObj =
                    new JsonParser().parse(CloudBillingVendorUtils.validateResponseString(charge.toString()))
                                    .getAsJsonObject();
            response.addProperty(BillingVendorConstants.RESPONSE_SUCCESS, true);
            response.add(BillingVendorConstants.RESPONSE_DATA, chargeJsonObj);
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            response.addProperty(BillingVendorConstants.RESPONSE_SUCCESS, false);
            response.addProperty(BillingVendorConstants.RESPONSE_MESSAGE, ex.getMessage());
            response.add(BillingVendorConstants.RESPONSE_DATA, null);
            LOGGER.error("Error while retrieving charge details : ", ex);
        }
        return response.toString();
    }

    public String convertUnixTimestamp(String format, long unixSeconds) {
        if (unixSeconds != 0) {
            Date date = new Date(unixSeconds * 1000L); // *1000 is to convert seconds to milliseconds
            SimpleDateFormat sdf = new SimpleDateFormat(format); // the format of your date
            return sdf.format(date);
        }
        return null;
    }

    /**
     * Remove the discount in the subscription for monetization
     *
     * @param subscriptionId subscription id
     * @return json response string
     */
    public String deleteSubscriptionDiscount(String subscriptionId) throws CloudBillingVendorException {
        try {
            Subscription.retrieve(subscriptionId, requestOptions).deleteDiscount();
            if (Subscription.retrieve(subscriptionId, requestOptions).getDiscount() == null) {
                return "success";
            }
            return null;
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while removing the subscription discount: ", ex);
        }
    }

    /**
     * @return Currency Used by Vendor for monetization
     */
    @Override public String getCurrencyUsed() {
        JsonObject response = new JsonObject();
        JsonObject currencyData = new JsonObject();
        String currency = BillingVendorConfigUtils.getBillingVendorConfiguration().getCurrency();
        if (currency != null) {
            currencyData.addProperty("currency", currency);
        } else {
            currencyData.addProperty("currency", "USD");
        }
        currencyData.addProperty("conversion", "CENTS");
        response.addProperty(BillingVendorConstants.RESPONSE_SUCCESS, true);
        response.add(BillingVendorConstants.RESPONSE_DATA, currencyData);
        return response.toString();
    }

    /**
     * Inner class to set parameters to vendor model class objects
     */
    private static class ObjectParams {
        protected static Map<String, Object> setObjectParams(String objectInfoJson) {
            return gsonObj.fromJson(objectInfoJson, new TypeToken<Map<String, Object>>() {
            }.getType());
        }
    }

    /**
     * Get billed organization Name
     *
     * @param customerId customer id
     * @return current billed organization Name
     * @throws CloudBillingVendorException
     */
    @Override public String getBilledOrganizationName(String customerId) throws CloudBillingVendorException {
        JsonObject response = new JsonObject();
        try {
            Customer customer = Customer.retrieve(customerId);
            JsonObject customerJsonObj =
                    new JsonParser().parse(CloudBillingVendorUtils.validateResponseString(customer.toString()))
                                    .getAsJsonObject();
            response.addProperty(BillingVendorConstants.RESPONSE_SUCCESS, true);
            response.add(BillingVendorConstants.RESPONSE_DATA, customerJsonObj.get("description"));
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            response.addProperty(BillingVendorConstants.RESPONSE_SUCCESS, false);
            response.addProperty(BillingVendorConstants.RESPONSE_MESSAGE, ex.getMessage());
            response.add(BillingVendorConstants.RESPONSE_DATA, null);
            LOGGER.error("Error while retrieving customer details : ", ex);
        }
        return response.toString();
    }
}
