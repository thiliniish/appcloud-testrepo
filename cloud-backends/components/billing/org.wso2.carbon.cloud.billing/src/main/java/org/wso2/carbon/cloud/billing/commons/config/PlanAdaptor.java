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

import org.wso2.carbon.cloud.billing.exceptions.CloudBillingException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Plan adaptor modify this class in introduction of rate plans
 */
public class PlanAdaptor extends XmlAdapter<PlanAdaptor.AdaptedPlan, Plan> {

    @Override
    public Plan unmarshal(PlanAdaptor.AdaptedPlan adaptedPlan) throws CloudBillingException {
        if (null == adaptedPlan) {
            return null;
        }

        //use plan specific unique value to identify the plan type.
        //Change this as necessary for identify API Cloud plans uniquely
        if (null != adaptedPlan.overUsage) {
            APICloudPlan plan = new APICloudPlan();
            plan.id = adaptedPlan.id;
            plan.name = adaptedPlan.name;
            plan.maxDailyUsage = adaptedPlan.maxDailyUsage;
            plan.maxAccounts = adaptedPlan.maxAccounts;
            plan.overUsage = adaptedPlan.overUsage;
            plan.monthlyRental = adaptedPlan.monthlyRental;
            plan.premiumLevel = adaptedPlan.premiumLevel;
            plan.visibility = adaptedPlan.visibility;
            return plan;
        } else {
            throw new CloudBillingException("Error while un-marshalling");
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Else try something specific for app cloud configs
     *
     * @throws CloudBillingException
     */
    @Override
    public PlanAdaptor.AdaptedPlan marshal(Plan plan) throws CloudBillingException {
        if (null == plan) {
            return null;
        }
        AdaptedPlan adaptedPlan = new AdaptedPlan();
        if (plan instanceof APICloudPlan) {
            APICloudPlan apiCloudPlan = (APICloudPlan) plan;
            adaptedPlan.id = apiCloudPlan.id;
            adaptedPlan.name = apiCloudPlan.name;
            adaptedPlan.maxDailyUsage = apiCloudPlan.maxDailyUsage;
            adaptedPlan.maxAccounts = apiCloudPlan.maxAccounts;
            adaptedPlan.overUsage = apiCloudPlan.overUsage;
            adaptedPlan.monthlyRental = apiCloudPlan.monthlyRental;
            adaptedPlan.premiumLevel = apiCloudPlan.premiumLevel;
        } else {
            throw new CloudBillingException("Error while marshalling.");
        }
        return adaptedPlan;
    }

    /**
     * Static class for AdaptedPlan
     */
    public static class AdaptedPlan {

        @XmlElement(name = "Id")
        public String id;

        @XmlElement(name = "Name")
        public String name;

        @XmlElement(name = "MaxDailyUsage")
        public int maxDailyUsage;

        @XmlElement(name = "MaxAccounts")
        public String maxAccounts;

        @XmlElement(name = "OverUsage")
        public String overUsage;

        @XmlElement(name = "MonthlyRental")
        public String monthlyRental;

        @XmlElement(name = "PremiumLevel")
        public int premiumLevel;

        @XmlElement(name = "Visibility")
        public boolean visibility;
    }
}
