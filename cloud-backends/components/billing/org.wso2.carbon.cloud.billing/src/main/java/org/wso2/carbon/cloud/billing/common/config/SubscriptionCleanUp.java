/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.cloud.billing.common.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Config element that represent the SubscriptionCleanUp xml object
 */
@XmlRootElement(name = "SubscriptionCleanUp")
public class SubscriptionCleanUp {

    private String roles;
    private boolean isEnabled;
    private String cron;

    @XmlElement(name = "Roles", nillable = false, required = true)
    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    @XmlElement(name = "EnableCleanUp", nillable = false, required = true)
    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    @XmlElement(name = "Cron", nillable = false)
    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }
}