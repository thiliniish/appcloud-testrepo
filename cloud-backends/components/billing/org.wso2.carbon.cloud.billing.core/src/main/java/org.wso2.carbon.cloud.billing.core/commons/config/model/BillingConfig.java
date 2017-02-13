/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.cloud.billing.core.commons.config.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Config element that represent the CloudBilling xml object
 */
@XmlRootElement(name = "CloudBilling") public class BillingConfig {

    @XmlElement(name = "BillingVendorClass", nillable = false, required = true) private String billingVendorClass;

    @XmlElement(name = "BillingVendorMonetizationClass", nillable = false, required = true) private String
            billingVendorMonetizationClass;
    
    @XmlElement(name = "MgtModeEnabled", nillable = false, required = true) private boolean mgtModeEnabled;

    @XmlElement(name = "DataServiceAPI", nillable = false, required = true) private DataServiceConfig dataServiceConfig;

    @XmlElement(name = "APIMRestAPI", nillable = false, required = true) private APIMRestAPIConfig apimRestAPIConfig;

    @XmlElement(name = "Security", nillable = true, required = false) private SecurityConfig securityConfig;

    @XmlElement(name = "Notifications", nillable = false) private NotificationConfig notificationsConfig;

    @XmlElement(name = "CloudTypes", nillable = false, required = true) private CloudTypes cloudTypes;

    @XmlElement(name = "Crons", nillable = false, required = true) private Crons crons;

    @XmlElement(name = "InvoiceFileLocation", nillable = false, required = true) private String invoiceFileLocation;

    public String getBillingVendorClass() {
        return billingVendorClass;
    }

    public String getBillingVendorMonetizationClass() {
        return billingVendorMonetizationClass;
    }

    public boolean isMgtModeEnabled() {
        return mgtModeEnabled;
    }

    public DataServiceConfig getDataServiceConfig() {
        return dataServiceConfig;
    }

    public APIMRestAPIConfig getApimRestAPIConfig() {
        return apimRestAPIConfig;
    }

    public SecurityConfig getSecurityConfig() {
        return securityConfig;
    }

    public NotificationConfig getNotificationsConfig() {
        return notificationsConfig;
    }

    public CloudType[] getCloudTypes() {
        return cloudTypes.getAllCloudTypes();
    }

    public CloudType getCloudTypeById(String cloudId) {
        return cloudTypes.getCloudTypeById(cloudId);
    }

    public Crons getCrons() {
        return crons;
    }

    public String getInvoiceFileLocation() {
        return invoiceFileLocation;
    }
}
