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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Coupon;
import com.stripe.model.Customer;
import com.stripe.model.CustomerSubscriptionCollection;
import com.stripe.model.ExternalAccount;
import com.stripe.model.Invoice;
import com.stripe.model.Plan;
import com.stripe.model.Subscription;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.billing.core.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.core.commons.CloudBillingServiceProvider;
import org.wso2.carbon.cloud.billing.core.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.vendor.commons.BillingVendorConstants;
import org.wso2.carbon.cloud.billing.vendor.commons.utils.BillingVendorConfigUtils;
import org.wso2.carbon.cloud.billing.vendor.stripe.exceptions.CloudBillingVendorException;
import org.wso2.carbon.cloud.billing.vendor.stripe.utils.APICloudMonetizationUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents class for Stripe related functionalities.
 */
public class StripeCloudBilling implements CloudBillingServiceProvider {

    private Map<String, Object> customerParams = new HashMap<>();
    private Map<String, Object> planParams = new HashMap<>();
    private Map<String, Object> subscriptionParams = new HashMap<>();
    private Map<String, Object> cardParams = new HashMap<>();
    private Map<String, Object> invoiceParams = new HashMap<>();
    private Map<String, Object> monetizationAccountParams = new HashMap<>();
    private static Gson gsonObj = new Gson();

    private static final Log LOGGER = LogFactory.getLog(StripeCloudBilling.class);

    public StripeCloudBilling() {
        setApiKey(BillingVendorConfigUtils.getBillingVendorConfiguration().getAuthenticationApiKeys().getSecretKey());
    }

    public StripeCloudBilling(String tenantDomain) throws CloudBillingException {
        String apiKey = getSecretKey(tenantDomain);
        setApiKey(apiKey);

    }

    /**
     * Set the authentication secret API key
     *
     * @param apiKey api key
     */
    private static void setApiKey(String apiKey) {
        Stripe.apiKey = apiKey;
    }

    // TODO: 10/11/16 Get the SecretAPI Key from the Database
    private static String getSecretKey(String tenantDomain) throws CloudBillingException {
        LOGGER.info("Getting Secret Key for Tenant : " + tenantDomain);
        try {
            return APICloudMonetizationUtils.getSecretKey(tenantDomain);
        } catch (CloudBillingVendorException e) {
            throw new CloudBillingException(
                    "Cloud Billing Exception Occurred while getting Secret key for tenant : " + tenantDomain, e);
        }
    }

    /**
     * Create the Customer
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
            return validateResponseString(Customer.create(customerParams).toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while creating customer : ", ex);
        }
    }

    /**
     * Retrieve customer details
     *
     * @param customerId customer id
     * @return json string of customer information
     */
    @Override public String getCustomerDetails(String customerId) throws CloudBillingVendorException {
        try {
            return validateResponseString(Customer.retrieve(customerId).toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while retrieving customer : ", ex);
        }
    }

    /**
     * Update Customer Info
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
        try {
            Customer customer = Customer.retrieve(customerId);
            customerParams.clear();
            customerParams = ObjectParams.setObjectParams(customerInfoJson);
            return validateResponseString(customer.update(customerParams).toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while updating customer : ", ex);
        }
    }

    /**
     * Delete customer
     *
     * @param customerId customer Id
     * @return success json string
     */
    @Override public String deleteCustomer(String customerId) throws CloudBillingVendorException {
        try {
            Customer customer = Customer.retrieve(customerId);
            return validateResponseString(customer.delete().toString());
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
    @Override public String createProductRatePlan(String tenantDomain, String ratePlanInfoJson) throws
                                                                                     CloudBillingVendorException {
        try {
            planParams.clear();
            planParams = ObjectParams.setObjectParams(ratePlanInfoJson);
            return validateResponseString(Plan.create(planParams).toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while creating rate-plan : ", ex);
        }
    }

    /**
     * retrieve a specific rate plan
     *
     * @param ratePlanId rate-plan Id
     * @return success json string
     */
    @Override public String getProductRatePlan(String ratePlanId) throws CloudBillingVendorException {
        try {
            return validateResponseString(Plan.retrieve(ratePlanId).toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while retrieving rate-plan : ", ex);
        }
    }

    /**
     * Method to update Product rate plan
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
            Plan plan = Plan.retrieve(planId);
            planParams.clear();
            planParams = ObjectParams.setObjectParams(ratePlanInfoJson);
            return validateResponseString(plan.update(planParams).toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while updating rate-plan : ", ex);
        }
    }

    /**
     * Method to delete a specific Product rate plan
     *
     * @param ratePlanId rate-plan Id
     * @return success json string
     */
    @Override public String deleteProductRatePlan(String ratePlanId) throws CloudBillingVendorException {
        try {
            return validateResponseString(Plan.retrieve(ratePlanId).toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while deleting rate-plan : ", ex);
        }
    }

    /**
     * Method to retrieve all the product rate-plans
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
            return validateResponseString(Plan.list(planParams).toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while retrieving all rate-plans : ", ex);
        }
    }

    /**
     * Create a subscription
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

            Subscription subscription = Subscription.create(subscriptionParams);

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
     * Retrieve a subscription
     *
     * @param subscriptionId subscription Id
     * @return success Json string
     */
    @Override public String getSubscription(String subscriptionId) throws CloudBillingVendorException {
        try {
            return validateResponseString(Subscription.retrieve(subscriptionId).toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while retrieving subscription : ", ex);
        }
    }

    /**
     * Method to retrieve all the subscriptions
     *
     * @param subscriptionInfoJson subscription details.
     * @return a list of subscriptions
     */
    @Override public String getAllSubscriptions(String subscriptionInfoJson) throws CloudBillingVendorException {
        try {
            subscriptionParams.clear();
            subscriptionParams = ObjectParams.setObjectParams(subscriptionInfoJson);
            return validateResponseString(Subscription.list(subscriptionParams).toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while retrieving all subscriptions : ", ex);
        }
    }

    /**
     * Update subscription
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
     * @return success Json string
     */
    @Override public String updateSubscription(String subscriptionId, String subscriptionInfoJson)
            throws CloudBillingVendorException {
        try {
            Subscription subscription = Subscription.retrieve(subscriptionId);
            subscriptionParams.clear();
            subscriptionParams = ObjectParams.setObjectParams(subscriptionInfoJson);
            return validateResponseString(subscription.update(subscriptionParams).toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while updating subscription : ", ex);
        }
    }

    /**
     * Cancel subscription by the subscription id
     *
     * @param subscriptionId       subscription id
     * @param subscriptionInfoJson this is not required for Stripe
     * @return success jason string
     */
    @Override public String cancelSubscription(String subscriptionId, String subscriptionInfoJson)
            throws CloudBillingVendorException {
        try {
            Subscription subscription = Subscription.retrieve(subscriptionId);
            subscriptionParams.clear();
            //We cancel the subscription at the end of currently subscribed time period
            subscriptionParams.put(BillingVendorConstants.AT_PERIOD_END, true);
            subscription = subscription.cancel(subscriptionParams);

            JsonObject subscriptionJsonObj =
                    new JsonParser().parse(validateResponseString(subscription.toString())).getAsJsonObject();

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
     * Add default payment method to a specific customer
     *
     * @param customerId            customer Id
     * @param paymentMethodInfoJson payment method details
     * @return success Json string
     */
    @Override public String addPaymentMethod(String customerId, String paymentMethodInfoJson)
            throws CloudBillingVendorException {
        try {
            Customer customer = Customer.retrieve(customerId);
            cardParams.clear();
            cardParams = ObjectParams.setObjectParams(paymentMethodInfoJson);
            return validateResponseString(customer.getSources().create(cardParams).toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while adding the new payment method : ", ex);
        }
    }

    /**
     * Set specific payment method of a specific customer as default
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
        try {
            return validateResponseString(updateCustomer(customerId, paymentMethodInfoJson));
        } catch (CloudBillingVendorException ex) {
            throw new CloudBillingVendorException("Error while setting the default payment method : ", ex);
        }
    }

    /**
     * Update default payment method
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
            Customer customer = Customer.retrieve(customerId);
            ExternalAccount cardInfo = customer.getSources().retrieve(paymentMethodId);
            cardParams.clear();
            cardParams = ObjectParams.setObjectParams(paymentMethodInfoJson);
            return validateResponseString(cardInfo.update(cardParams).toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while updating payment method : ", ex);
        }
    }

    /**
     * Retrieve all payment methods of a specific customer
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
        try {
            cardParams.clear();
            cardParams = ObjectParams.setObjectParams(paymentMethodInfoJson);
            return validateResponseString(Customer.retrieve(customerId).getSources().all(cardParams).toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while retrieving all payment methods : ", ex);
        }
    }

    /**
     * Remove payment method/ card of a customer
     *
     * @param customerId      customer Id
     * @param paymentMethodId payment method id
     * @return success jason string
     */
    @Override public String removePaymentMethod(String customerId, String paymentMethodId)
            throws CloudBillingVendorException {
        try {
            return validateResponseString(
                    Customer.retrieve(customerId).getSources().retrieve(paymentMethodId).delete().toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while removing payment method : ", ex);
        }
    }

    /**
     * Method to create Monetization account
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
     * Retrieve invoices associated with a customer
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
            return validateResponseString(Invoice.list(invoiceParams).toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while removing payment method : ", ex);
        }
    }

    /**
     * Retrieve invoice details
     *
     * @param invoiceId invoice id
     * @return json string of invoice information
     */
    @Override public String getInvoiceDetails(String invoiceId) throws CloudBillingVendorException {
        try {
            return validateResponseString(Invoice.retrieve(invoiceId).toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException("Error while retrieving invoice : ", ex);
        }
    }

    /**
     * Get current plan subscribed to a service
     *
     * @param customerId customer id
     * @return current active rate plan
     */
    @Override public String getCurrentRatePlan(String customerId) throws CloudBillingVendorException {
        try {
            Customer customer = Customer.retrieve(customerId);
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
     * Get customer coupons
     *
     * @param customerId customer id
     * @return current coupons
     * @throws CloudBillingVendorException
     */
    @Override public String getCustomerCoupons(String customerId) throws CloudBillingVendorException {
        try {
            return validateResponseString(Customer.retrieve(customerId).getDiscount().toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException(
                    "Error occurred while retrieving the coupons of the customer : " + customerId, ex);
        }
    }

    /**
     * Get a specific coupon details
     *
     * @param couponID coupon id
     * @return coupon data
     * @throws CloudBillingVendorException
     */
    @Override public String retrieveCouponInfo(String couponID) throws CloudBillingVendorException {
        try {
            return validateResponseString(Coupon.retrieve(couponID).toString());
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException |
                APIException ex) {
            throw new CloudBillingVendorException(
                    "Error occurred while retrieving the coupons information of the coupon : " + couponID, ex);
        }
    }

    /**
     * Retrieve the publishable key
     *
     * @return json publishable key
     */
    public String getPublishableKey() throws CloudBillingVendorException {
        return BillingVendorConfigUtils.getBillingVendorConfiguration().getAuthenticationApiKeys().getPublishableKey();
    }

    public String getPublishableKeyForTenant(String tenantDomain) throws CloudBillingVendorException {
        try {
            return APICloudMonetizationUtils.getPublishableKeyForTenant(tenantDomain);
        } catch (CloudBillingVendorException e) {
            throw new CloudBillingVendorException("Error while obtaining publishable key for tenant : " + tenantDomain,
                                                  e);
        }
    }

    /**
     * validate the response as a JSON string
     *
     * @param response response JSON string
     * @return validated response string
     */
    private String validateResponseString(String response) {
        return "{".concat(response.replaceFirst(".*?\\{", ""));
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

}
