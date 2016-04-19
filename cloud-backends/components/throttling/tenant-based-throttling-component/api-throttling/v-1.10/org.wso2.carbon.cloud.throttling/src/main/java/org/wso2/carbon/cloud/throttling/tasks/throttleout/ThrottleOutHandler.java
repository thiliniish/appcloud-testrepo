/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License
 */

package org.wso2.carbon.cloud.throttling.tasks.throttleout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.throttling.TenantCache;
import org.wso2.carbon.cloud.throttling.common.Constants;
import org.wso2.carbon.cloud.throttling.common.RatePlanDTO;
import org.wso2.carbon.context.CarbonContext;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

/**
 * Handles when a throttle out takes place todo.
 */
public class ThrottleOutHandler extends TimerTask {
    /*
    * keeps throttleOut tenant info to be written to db
    * */
    private static final Log log = LogFactory.getLog(ThrottleOutHandler.class);

    public ThrottleOutHandler() {
        log.debug("Tenant Throttling - cache Cleaner Task is started.");
    }

    @Override
    public void run() {
        //Get the throttled out tenants and send notification email to tenant admin
        final long timeToLive = Constants.EMAIL_ALERT_DELAY;
        Iterator<Map.Entry<String, ConcurrentMap<String, ThrottleOutDTO>>> itr = TenantCache.getThrottleOutTenantsMap();
        while (itr.hasNext()) {
            Map.Entry<String, ConcurrentMap<String, ThrottleOutDTO>> mapEntry = itr.next();
            String tenantDomain = mapEntry.getKey();
            ConcurrentMap<String, ThrottleOutDTO> throttleOutAPIs = mapEntry.getValue();
            sendEmail(tenantDomain, throttleOutAPIs);   //todo
        /*    if (throttleOutAPIs != null && (System.currentTimeMillis() > (ratePlanDTO.getLastAccessTime() + timeToLive))) {
                //remove tenants that is not active for more that 1hour from the cache
                TenantCache.removeFromCache(tenantDomain);
                if (log.isDebugEnabled()) {
                    log.debug("Tenant '" + tenantDomain + "' is idle for one hour. Hence removing from the cache. ");
                }
            } else {
                //If the rate plan is changed then update to the current rate plan
                String productRatePlanId = getProductRatePlanId(tenantDomain);
                RatePlanDTO cachedRatePlan = TenantCache.getRatePlan(tenantDomain);
                if (!cachedRatePlan.getRatePlan().equals(productRatePlanId)) {
                    TenantCache.updateCache(tenantDomain, productRatePlanId);
                    if (log.isDebugEnabled()) {
                        log.debug("Tenant '" + tenantDomain + "' has changed the rate plan from '" + cachedRatePlan.getRatePlan() + "' to '" + productRatePlanId + "'. Hence updating the cache.");
                    }
                }
            }*/
        }

    }

    private void sendEmail(String tenantDomain, ConcurrentMap<String, ThrottleOutDTO> throttleOutAPIs) {

        final String username = CarbonContext.getThreadLocalCarbonContext().getUsername();

        // Recipient's email ID needs to be mentioned.
        String to = "abcd@gmail.com";

        String from = Constants.SENDER_EMAIL;
        String host = Constants.SENDER_HOST;
        String port = Constants.SENDER_PORT;

        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", true);
        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties);
        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);
            // Set From: header field of the header.
            try {
                message.setFrom(new InternetAddress(from));
            } catch (MessagingException e) {
                log.error("Exception in setting sender email to MimeMessage", e);
            }
            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: header field
            message.setSubject("Your tenant '" + tenantDomain + "' has throttled out by WSO2 Cloud");

            // Send the actual HTML message, as big as you like
            message.setContent("<h1>Subscribe to a higher payment plan.</h1>", "text/html");

            // Send message
            Transport.send(message);
            log.debug("Sent throttle out email alert to " + to + " successfully.");
        } catch (MessagingException mex) {
            log.error("Exception in sending notification email to Tenant admin " + to + " of tenant domain " + tenantDomain, mex);
        }
    }

}
