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
import javax.mail.internet.AddressException;
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

    private ConcurrentLinkedQueue<Map> mailQueue;
    private ExecutorService executorService;

    private String host;
    private String port;
    private String username;
    private String password;
    private String sender;
    private String tls;
    private Session session;

    private EmailNotifications() {
        BillingConfig billingConfig = BillingConfigUtils.getBillingConfiguration();
        EmailConfig emailConfig = billingConfig.getUtilsConfig().getNotifications().getEmailNotification();
        host = emailConfig.getHost().trim();
        port = emailConfig.getPort().trim();
        username = emailConfig.getUsername().trim();
        password = emailConfig.getPassword().trim();
        sender = emailConfig.getSender().trim();
        tls = emailConfig.getTls().trim();
        session = setSession();

        mailQueue = new ConcurrentLinkedQueue<>();
        executorService = Executors.newFixedThreadPool(3);
        MailQueueObserver mailQueueObserver = new MailQueueObserver();
        addObserver(mailQueueObserver);
    }

    public static EmailNotifications getInstance() {
        return instance;
    }

    /**
     * Add email to the mail queue
     *
     * @param messageBody email body
     * @param subject     email subject
     * @param receiver    receivers
     */
    public void addToMailQueue(String messageBody, String subject, String receiver) {
        try {
            InternetAddress emailAddr = new InternetAddress(receiver);
            emailAddr.validate();
            Map<String, String> mail = new HashMap<>();
            mail.put(MESSAGE_BODY, messageBody);
            mail.put(MESSAGE_SUBJECT, subject);
            mail.put(MESSAGE_RECEIVER, receiver);
            mailQueue.add(mail);
            setChanged();
            notifyObservers();
        } catch (AddressException e) {
            LOGGER.error("Email sending failed. Invalid receiver address specified: " + receiver);
        }
    }

    /**
     * Set email session
     *
     * @return Session obj
     */
    private Session setSession() {
        Properties props = new Properties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        Session session;
        if (Boolean.valueOf(tls)) {
            props.put("mail.smtp.auth", "true");
            session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
        } else {
            session = Session.getDefaultInstance(props);
        }
        return session;
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

    /**
     * Mail queue observer which allocates the threads and sends the email
     */
    protected class MailQueueObserver implements Observer {

        private MailSenderErrorObserver mailSenderErrorObserver;

        MailQueueObserver() {
            mailSenderErrorObserver = new MailSenderErrorObserver();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void update(Observable o, Object arg) {
            do {
                Map email = mailQueue.poll();
                MailSender mailSender = new MailSender(mailSenderErrorObserver, email);
                executorService.execute(mailSender);
            } while (!mailQueue.isEmpty());
        }
    }

    /**
     * This is to keep the sending failed emails and try sending them again
     * once the error has been fixed
     */
    protected class MailSenderErrorObserver implements Observer {

        private ConcurrentLinkedQueue<Map> failedEmailQueue;

        MailSenderErrorObserver() {
            failedEmailQueue = new ConcurrentLinkedQueue<>();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void update(Observable o, Object arg) {
            if (arg instanceof Map) {
                Map email = (Map) arg;
                failedEmailQueue.add(email);
            } else if (arg instanceof Boolean && (Boolean) arg) {
                for (Map email : failedEmailQueue) {
                    EmailNotifications.getInstance().addToMailQueue(email.get(MESSAGE_BODY).toString(), email.get
                            (MESSAGE_SUBJECT).toString(), email.get(MESSAGE_RECEIVER).toString());
                    failedEmailQueue.remove(email);
                }
            } else {
                LOGGER.error("No argument specified. ");
            }
        }
    }

    /**
     * Mail sending thread
     */
    protected class MailSender extends Observable implements Runnable {

        private Map email;

        MailSender(MailSenderErrorObserver errorObserver, Map email) {
            this.email = email;
            addObserver(errorObserver);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(sender));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email.get(MESSAGE_RECEIVER).toString()));
                message.setSubject(email.get(MESSAGE_SUBJECT).toString());
                message.setText(email.get(MESSAGE_BODY).toString());

                Transport.send(message);
                setChanged();
                notifyObservers(true);
            } catch (Exception e) {
                //Catching the generic exception here to avoid throwing exceptions while executing this sub task
                setChanged();
                notifyObservers(email);
                LOGGER.error("Error while sending the email notification to: " + email.get(MESSAGE_RECEIVER).toString
                        () + " under subject: " + email.get(MESSAGE_SUBJECT).toString() + ". Email is added " +
                        "back to the queue. once the error is fixed it will try again ", e);
            }
        }
    }
}
