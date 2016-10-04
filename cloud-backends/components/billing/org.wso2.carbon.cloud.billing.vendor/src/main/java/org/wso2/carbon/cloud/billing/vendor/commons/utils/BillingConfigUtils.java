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

package org.wso2.carbon.cloud.billing.vendor.commons.utils;

import org.w3c.dom.Document;
import org.wso2.carbon.cloud.billing.core.service.CloudBillingService;
import org.wso2.carbon.cloud.billing.vendor.commons.config.BillingVendorConfig;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

/**
 * Billing configuration utility class
 */
public class BillingConfigUtils {
    private static volatile BillingVendorConfig billingVendorConfig;

    private BillingConfigUtils() {
    }

    /**
     * Get billing vendor configuration billing.vendor.xml
     *
     * @return Billing vendor config
     */
    public static BillingVendorConfig getBillingVendorConfiguration() {
        Document doc = CloudBillingService.getBillingVendorConfigDocument();
        if (billingVendorConfig == null) {
            synchronized (BillingConfigUtils.class) {
                if (billingVendorConfig == null) {
                    billingVendorConfig = loadBillingVendorConfig(doc);
                }
            }
        }
        return billingVendorConfig;
    }

    /**
     * Load billing vendor configuration
     *
     * @return Billing vendor config
     */
    private static BillingVendorConfig loadBillingVendorConfig(Document doc) {
        try {
            /* Un-marshaling Billing vendor Management configuration */
            JAXBContext cdmContext = JAXBContext.newInstance(BillingVendorConfig.class);
            Unmarshaller unmarshaller = cdmContext.createUnmarshaller();
            return (BillingVendorConfig) unmarshaller.unmarshal(doc);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error occurred while initializing Billing config", e);
        }
    }

}
