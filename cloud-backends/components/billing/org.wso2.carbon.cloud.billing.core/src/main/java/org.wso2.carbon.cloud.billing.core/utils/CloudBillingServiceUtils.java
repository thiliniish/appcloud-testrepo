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

package org.wso2.carbon.cloud.billing.core.utils;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.wso2.carbon.cloud.billing.core.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.core.commons.config.BillingConfigManager;
import org.wso2.carbon.cloud.billing.core.commons.config.model.CloudType;
import org.wso2.carbon.cloud.billing.core.commons.config.model.Plan;
import org.wso2.carbon.cloud.billing.core.commons.notifications.EmailNotifications;

import java.io.IOException;

/**
 * Model to represent Utilities for Cloud Billing core module
 */
public final class CloudBillingServiceUtils {

    private static final Log LOGGER = LogFactory.getLog(CloudBillingServiceUtils.class);
    private static volatile String configObj;

    private CloudBillingServiceUtils() {
    }

    /**
     * Get configuration on json
     *
     * @return json object
     */
    public static String getConfigInJson() {
        if (configObj == null) {
            synchronized (CloudBillingServiceUtils.class) {
                if (configObj == null) {
                    Gson gson = new Gson();
                    configObj = gson.toJson(BillingConfigManager.getBillingConfiguration());
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Configuration read to json is successfully completed.");
                    }
                }
            }
        }
        return configObj;
    }

    /**
     * Get plans for Id
     *
     * @param cloudId Unique ID for the cloud (i.e api_cloud)
     * @return rate plans in billing.xml
     */
    public static Plan[] getSubscriptions(String cloudId) {
        CloudType cloudType = BillingConfigManager.getBillingConfiguration().getCloudTypeById(cloudId);
        return cloudType.getSubscription().getPlans();
    }

    /**
     * Retrieve the billing vendor class name
     *
     * @return Billing Vendor service class name
     */
    public static String getBillingVendorServiceUtilClass() {
        return BillingConfigManager.getBillingConfiguration().getBillingVendorClass();
    }

    /**
     * Validate rate plan id
     *
     * @param serviceId         service Id
     * @param productRatePlanId rate plan id
     * @return validation boolean
     */
    public static boolean validateRatePlanId(String serviceId, String productRatePlanId) {
        Plan[] plans = getSubscriptions(serviceId);
        for (Plan plan : plans) {
            if (plan.getId().equals(productRatePlanId)) {
                return true;
            }
        }
        return false;
    }

//    /**
//     * Validate service Id
//     *
//     * @param cloudId service id
//     * @return validation boolean
//     */
//    public static boolean validateServiceId(String cloudId) {
//        Subscription[] subscriptions = BillingConfigUtils.getBillingConfiguration().getSubscriptions();
//        for (Subscription subscription : subscriptions) {
//            if (cloudId.equals(subscription.getId())) {
//                return true;
//            }
//        }
//        return false;
//    }

    /**
     * Method to get that the billing functionality enable/disable status
     * @param cloudId Unique ID for the cloud (i.e api_cloud)
     * @return billing enable/disable status
     */
    public static boolean isBillingEnabled(String cloudId) {
        return BillingConfigManager.getBillingConfiguration().getCloudTypeById(cloudId).isBillingEnabled();
    }

    /**
     * This is to send notification mails to cloud. Receiver mail
     * address will be set as cloud
     *
     * @param messageBody    message body
     * @param messageSubject message subject
     */
    public static void sendNotificationToCloud(String messageBody, String messageSubject) {
        String receiver =
                BillingConfigManager.getBillingConfiguration().getNotificationsConfig().getEmailNotification()
                                  .getSender();
        EmailNotifications.getInstance().sendMail(messageBody, messageSubject, receiver,
                                                  BillingConstants.TEXT_PLAIN_CONTENT_TYPE);
    }

    /**
     * Get the Json List of the response string
     *
     * @param responseObject jsonString response
     * @return Json node list
     */
    public static JsonNode getJsonList(String responseObject) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        // convert JSON string to JsonNode list
        return mapper.readTree(responseObject);
    }
}
