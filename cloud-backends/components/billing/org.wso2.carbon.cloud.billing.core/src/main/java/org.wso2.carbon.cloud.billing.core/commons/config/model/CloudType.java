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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * This represents a Billing Configurations unique to a Cloud Type (i.e APP Cloud, API Cloud etc)
 */
public class CloudType {

    //id uniquely identifies cloud type ex: api_cloud, app_cloud
    @XmlAttribute(name = "id") private String id;

    @XmlAttribute(name = "name") private String name;

    @XmlElement(name = "BillingEnabled", required = true) private boolean billingEnabled;

    @XmlElement(name = "TrialPeriod", required = true) private int trialPeriod;

    @XmlElement(name = "UsageDisplayPeriod", required = true) private int usageDisplayPeriod;

    @XmlElement(name = "Subscription", required = true) private Subscription subscription;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isBillingEnabled() {
        return billingEnabled;
    }

    public int getTrialPeriod() {
        return trialPeriod;
    }

    public int getUsageDisplayPeriod() {
        return usageDisplayPeriod;
    }

    public Subscription getSubscription() {
        return subscription;
    }

}
