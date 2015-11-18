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

package org.wso2.carbon.cloud.billing.commons.Notifications;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.billing.commons.config.BillingConfig;
import org.wso2.carbon.cloud.billing.commons.config.EmailConfig;
import org.wso2.carbon.cloud.billing.commons.utils.BillingConfigUtils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Mail notifications implemented in this class
 */
public class EmailNotifications {

    private final Log LOGGER = LogFactory.getLog(EmailNotifications.class);
    private String host;
    private String port;
    private String userName;
    private String password;
    private String sender;
    private String tls;

    public EmailNotifications() {
        BillingConfig billingConfig = BillingConfigUtils.getBillingConfiguration();
        EmailConfig emailConfig = billingConfig.getUtilsConfig().getNotifications().getEmailNotification();
        host = emailConfig.getHost();
        port = emailConfig.getPort();
        userName = emailConfig.getUserName();
        password = emailConfig.getPassword();
        sender = emailConfig.getSender();
        tls = emailConfig.getTls();
    }

    /**
     * Sending the email Notification
     *
     * @param messageBody
     * @param subject
     * @param receiver
     */
    public void send(String messageBody, String subject, String receiver) {

        Properties props = new Properties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        Session session;
        if ("true".equals(tls)) {
            props.put("mail.smtp.auth", "true");
            session = Session.getInstance(props, new Authenticator() {
                @Override protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(userName, password);
                }
            });
        } else {
            session = Session.getDefaultInstance(props);
        }
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiver));
            message.setSubject(subject);
            message.setText(messageBody);

            Transport.send(message);
        } catch (MessagingException e) {
            LOGGER.error("Error while sending the email notification - " + e);
        }

    }
}
