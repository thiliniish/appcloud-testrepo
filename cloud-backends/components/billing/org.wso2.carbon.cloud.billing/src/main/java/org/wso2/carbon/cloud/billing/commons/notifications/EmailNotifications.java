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

package org.wso2.carbon.cloud.billing.commons.notifications;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.billing.commons.config.BillingConfig;
import org.wso2.carbon.cloud.billing.commons.config.EmailConfig;
import org.wso2.carbon.cloud.billing.commons.utils.BillingConfigUtils;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Mail notifications implemented in this class
 */
public class EmailNotifications extends Observable {

    private static final Log LOGGER = LogFactory.getLog(EmailNotifications.class);
    private static final String MESSAGE_BODY = "messageBody";
    private static final String MESSAGE_SUBJECT = "subject";
    private static final String MESSAGE_RECEIVER = "receiver";

    private static EmailNotifications instance = new EmailNotifications();

    private ConcurrentLinkedQueue<Map> emails;
    private ExecutorService executorService;

    private String host;
    private String port;
    private String userName;
    private String password;
    private String sender;
    private String tls;

    private EmailNotifications() {
        BillingConfig billingConfig = BillingConfigUtils.getBillingConfiguration();
        EmailConfig emailConfig = billingConfig.getUtilsConfig().getNotifications().getEmailNotification();
        host = emailConfig.getHost();
        port = emailConfig.getPort();
        userName = emailConfig.getUsername();
        password = emailConfig.getPassword();
        sender = emailConfig.getSender();
        tls = emailConfig.getTls();

        emails = new ConcurrentLinkedQueue<>();
        executorService = Executors.newFixedThreadPool(2);
        MailQueueObserver mailQueueObserver = new MailQueueObserver();
        addObserver(mailQueueObserver);
    }

    public static EmailNotifications getInstance() {
        return instance;
    }

    public void addToMailQueue(String messageBody, String subject, String receiver) {
        Map<String, String> mail = new HashMap<>();
        mail.put(MESSAGE_BODY, messageBody);
        mail.put(MESSAGE_SUBJECT, subject);
        mail.put(MESSAGE_RECEIVER, receiver);
        emails.add(mail);
        setChanged();
        notifyObservers();
    }

    /**
     * Shutdown the thread pool
     */
    public void shutdownAndAwaitTermination() {
        executorService.shutdown();
        try {
            // Wait a while for existing tasks to terminate
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS))
                    LOGGER.error("email sender executor pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            executorService.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    protected class MailQueueObserver implements Observer {

        /**
         * {@inheritDoc}
         */
        @Override
        public void update(Observable o, Object arg) {
            do {
                Map email = emails.poll();
                MailSender mailSender = new MailSender(email);
                executorService.execute(mailSender);
            } while (!emails.isEmpty());
        }
    }

    protected class MailSender implements Runnable {

        Map email;

        MailSender(Map email) {
            this.email = email;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            Properties props = new Properties();
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);

            Session session;
            if ("true".equals(tls)) {
                props.put("mail.smtp.auth", "true");
                session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(userName, password);
                    }
                });
            } else {
                session = Session.getDefaultInstance(props);
            }
            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(sender));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email.get(MESSAGE_RECEIVER).toString()));
                message.setSubject(email.get(MESSAGE_SUBJECT).toString());
                message.setText(email.get(MESSAGE_BODY).toString());

                Transport.send(message);
                //Catching the generic exception here to avoid throwing exceptions while executing this sub task
            } catch (Exception e) {
                LOGGER.error("Error while sending the email notification - " + e);
            }
        }
    }
}
