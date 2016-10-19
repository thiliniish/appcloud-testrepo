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
package org.wso2.carbon.cloud.billing.core.commons;

import org.wso2.carbon.cloud.billing.core.exceptions.CloudBillingException;

/**
 * This includes all the core functionalities of cloud billing module.
 */
public interface CloudBillingServiceProvider {

    /**
     * Create the Customer
     *
     * @param customerInfoJson customer details
     * @return success Json string
     */
    public String createCustomer(String customerInfoJson) throws CloudBillingException;

    /**
     * Retrieve customer details
     *
     * @param customerId customer id
     * @return json string of customer information
     */
    public String getCustomerDetails(String customerId) throws CloudBillingException;

    /**
     * Update Customer Info
     *
     * @param customerId       customerId Id
     * @param customerInfoJson customer details
     * @return success Json string
     */
    public String updateCustomer(String customerId, String customerInfoJson) throws CloudBillingException;

    /**
     * Delete customer
     *
     * @param customerId customer Id
     * @return success json string
     */
    public String deleteCustomer(String customerId) throws CloudBillingException;

    /**
     * Create rate plan for the Product
     *
     * @param tenantDomain tenant domain
     * @param ratePlanInfoJson rate-plan details
     * @return success json string
     */
    public String createProductRatePlan(String tenantDomain, String ratePlanInfoJson) throws CloudBillingException;

    /**
     * retrieve a specific rate plan
     *
     * @param ratePlanId rate-plan Id
     * @return success json string
     */
    public String getProductRatePlan(String ratePlanId) throws CloudBillingException;

    /**
     * Method to update Product rate plan
     *
     * @param planId           rate plan ID
     * @param ratePlanInfoJson rate-plan details
     * @return success json string
     */
    public String updateProductRatePlan(String planId, String ratePlanInfoJson) throws CloudBillingException;

    /**
     * Method to delete a specific Product rate plan
     *
     * @param ratePlanId rate-plan Id
     * @return success json string
     */
    public String deleteProductRatePlan(String ratePlanId) throws CloudBillingException;

    /**
     * Method to retrieve all the product rate-plans
     *
     * @param ratePlanInfoJson rate-plan details
     * @return a list of rate plans
     */
    public String getAllProductRatePlans(String ratePlanInfoJson) throws CloudBillingException;

    /**
     * Create a subscription
     *
     * @param subscriptionInfoJson subscription details. This includes customer id and the product rate-plan id
     * @return success Json string
     */
    public String createSubscription(String subscriptionInfoJson) throws CloudBillingException;

    /**
     * Retrieve a subscription
     *
     * @param subscriptionId subscription Id
     * @return success Json string
     */
    public String getSubscription(String subscriptionId) throws CloudBillingException;

    /**
     * Method to retrieve all the subscriptions
     *
     * @param subscriptionInfoJson subscription details.
     * @return a list of subscriptions
     */
    public String getAllSubscriptions(String subscriptionInfoJson) throws CloudBillingException;

    /**
     * Update subscription
     *
     * @param subscriptionId       subscription Id
     * @param subscriptionInfoJson subscription details for downgrade or upgrade. This includes customer id and the
     *                             product rate-plan id
     * @return success Json string
     */
    public String updateSubscription(String subscriptionId, String subscriptionInfoJson) throws CloudBillingException;

    /**
     * Cancel subscription by the subscription id
     *
     * @param subscriptionId       subscription id
     * @param subscriptionInfoJson subscription information in json
     * @return success jason string
     */
    public String cancelSubscription(String subscriptionId, String subscriptionInfoJson) throws CloudBillingException;

    /**
     * Add a payment method to a specific customer
     *
     * @param customerId            customer Id
     * @param paymentMethodInfoJson payment method details
     * @return success Json string
     */
    public String addPaymentMethod(String customerId, String paymentMethodInfoJson) throws CloudBillingException;

    /**
     * Set specific payment method of a specific customer as default
     *
     * @param customerId            customer Id
     * @param paymentMethodInfoJson payment method details
     * @return success Json string
     */
    public String setDefaultPaymentMethod(String customerId, String paymentMethodInfoJson) throws
                                                                                           CloudBillingException;

    /**
     * Update default payment method
     *
     * @param customerId            customer Id
     * @param paymentMethodId       payment method id
     * @param paymentMethodInfoJson payment method details
     * @return success Json string
     */
    public String updatePaymentMethod(String customerId, String paymentMethodId, String paymentMethodInfoJson)
            throws CloudBillingException;

    /**
     * Retrieve all payment methods of a specific customer
     *
     * @param customerId            customer Id
     * @param paymentMethodInfoJson payment method details
     * @return success Json string
     */
    public String getAllPaymentMethods(String customerId, String paymentMethodInfoJson) throws CloudBillingException;

    /**
     * Remove payment method/ card info
     *
     * @param customerId      customer Id
     * @param paymentMethodId payment method id
     * @return success jason string
     */
    public String removePaymentMethod(String customerId, String paymentMethodId) throws CloudBillingException;

    /**
     * Method to create Monetization account
     *
     * @param customerId                  monetization customer id
     * @param monetizationAccountInfoJson monetization account info
     * @return success jason string
     */
    public String createMonetizationAccount(String customerId, String monetizationAccountInfoJson)
            throws CloudBillingException;

    /**
     * Retrieve invoices associated with a customer
     *
     * @param invoiceInfoJson invoice retrieval info for a specific customer
     * @return Json String of invoices
     */
    public String getInvoices(String invoiceInfoJson) throws CloudBillingException;

    /**
     * Retrieve invoice details
     *
     * @param invoiceId invoice id
     * @return json string of invoice information
     */
    public String getInvoiceDetails(String invoiceId) throws CloudBillingException;

    /**
     * Get current plan subscribed to a service
     *
     * @param customerId  customer id
     * @return current active rate plan
     * @throws CloudBillingException
     */
    public String getCurrentRatePlan(String customerId) throws CloudBillingException;

    /**
     * Get customer coupons
     *
     * @param customerId customer id
     * @return current coupons
     * @throws CloudBillingException
     */
    public String getCustomerCoupons(String customerId) throws CloudBillingException;

    /**
     * Get a specific coupon details
     *
     * @param couponID coupon id
     * @return coupon data
     * @throws CloudBillingException
     */
    public String retrieveCouponInfo(String couponID) throws CloudBillingException;

    /**
     * Get a specific account details
     *
     * @param customerId customer id
     * @return coupon data
     * @throws CloudBillingException
     */
    public String retrieveAccountInfo(String customerId) throws CloudBillingException;

}
