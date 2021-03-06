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

/**
 * API Cloud billing rate plans
 */
public class APICloudPlan extends Plan {

    protected String name;
    protected int maxDailyUsage;
    protected String maxAccounts;
    protected String overUsage;
    protected String monthlyRental;
    protected int premiumLevel;
    protected boolean visibility;

    public String getName() {
        return name;
    }

    public int getMaxDailyUsage() {
        return maxDailyUsage;
    }

    public String getMaxAccounts() {
        return maxAccounts;
    }

    public String getOverUsage() {
        return overUsage;
    }

    public String getMonthlyRental() {
        return monthlyRental;
    }

    public int getPremiumLevel() {
        return premiumLevel;
    }

    public boolean isVisible() {
        return visibility;
    }

}
