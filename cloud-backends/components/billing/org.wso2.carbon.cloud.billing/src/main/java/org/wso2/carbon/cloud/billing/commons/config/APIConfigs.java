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

package org.wso2.carbon.cloud.billing.commons.config;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Config element that represent the APIs xml object
 */
@XmlRootElement(name = "APIs")
public class APIConfigs {

    private String usage;
    private String accountSummary;
    private String ratePlans;
    private String accounts;
    private String cancelSubscription;
    private String subscriptions;
    private String paymentMethods;
    private String removePaymentMethod;
    private String invoiceInfo;
    private String paymentInfo;
    private String products;

    @XmlElement(name = "Usage", nillable = false, required = true)
    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    @XmlElement(name = "AccountSummary", nillable = false, required = true)
    public String getAccountSummary() {
        return accountSummary;
    }

    public void setAccountSummary(String accountSummary) {
        this.accountSummary = accountSummary;
    }

    @XmlElement(name = "RatePlans", nillable = false, required = true)
    public String getRatePlans() {
        return ratePlans;
    }

    public void setRatePlans(String ratePlans) {
        this.ratePlans = ratePlans;
    }

    @XmlElement(name = "Accounts", nillable = false, required = true)
    public String getAccounts() {
        return accounts;
    }

    public void setAccounts(String accounts) {
        this.accounts = accounts;
    }

    @XmlElement(name = "CancelSubscription", nillable = false, required = true)
    public String getCancelSubscription() {
        return cancelSubscription;
    }

    public void setCancelSubscription(String cancelSubscription) {
        this.cancelSubscription = cancelSubscription;
    }

    @XmlElement(name = "Subscriptions", nillable = false, required = true)
    public String getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(String subscriptions) {
        this.subscriptions = subscriptions;
    }

    @XmlElement(name = "PaymentMethods", nillable = false, required = true)
    public String getPaymentMethods() {
        return paymentMethods;
    }

    public void setPaymentMethods(String paymentMethods) {
        this.paymentMethods = paymentMethods;
    }

    @XmlElement(name = "RemovePaymentMethod", nillable = false, required = true)
    public String getRemovePaymentMethod() {
        return removePaymentMethod;
    }

    public void setRemovePaymentMethod(String removePaymentMethod) {
        this.removePaymentMethod = removePaymentMethod;
    }

    @XmlElement(name = "InvoiceInfo", nillable = false, required = true)
    public String getInvoiceInfo() {
        return invoiceInfo;
    }

    public void setInvoiceInfo(String invoiceInfo) {
        this.invoiceInfo = invoiceInfo;
    }

    @XmlElement(name = "PaymentInfo", nillable = false, required = true)
    public String getPaymentInfo() {
        return paymentInfo;
    }

    public void setPaymentInfo(String paymentInfo) {
        this.paymentInfo = paymentInfo;
    }

    @XmlElement(name = "Products", nillable = false, required = true)
    public String getProducts() {
        return products;
    }

    public void setProducts(String products) {
        this.products = products;
    }

}
