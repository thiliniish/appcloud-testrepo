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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Subscription specific rate plan holding element
 */
@XmlRootElement(name = "Subscription")
public class Subscription {

    //product id
    @XmlAttribute(name = "productId")
    private String productId;

    @XmlElement(name = "Plan")
    private Plan[] plans;

    private static volatile Map<String, Plan> planMap;

    private static final Log LOGGER = LogFactory.getLog(Subscription.class);

    public String getProductId() {
        return productId;
    }

    public Plan[] getPlans() {
        return plans;
    }

    public Plan getPlanByID(String id) {
        if (planMap == null) {
            initPlanMap();
        }
        Plan plan = planMap.get(id);
        if (plan == null) {
            LOGGER.warn("Plan with the id : " + id + " not found.");
        }
        return plan;
    }

    private void initPlanMap() {
        synchronized (Subscription.class) {
            if (planMap == null) {
                planMap = new HashMap<>();
                for (Plan plan : plans) {
                    planMap.put(plan.getId(), plan);
                }
            }
        }
    }
}
