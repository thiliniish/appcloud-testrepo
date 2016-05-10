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
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.internal.CloudBillingServiceComponent;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This class uses the email output event adapter to send email notifications.
 */
public class EmailNotifications extends Observable {

    private static final Log LOGGER = LogFactory.getLog(EmailNotifications.class);
    private static final String MESSAGE_BODY = BillingConstants.MESSAGE_BODY;
    private static final String MESSAGE_SUBJECT = BillingConstants.MESSAGE_SUBJECT;
    private static final String MESSAGE_RECEIVER = BillingConstants.MESSAGE_RECEIVER;
    private static final String MESSAGE_TYPE = BillingConstants.MESSAGE_TYPE;

    private Queue<Map> mailQueue;
    private ExecutorService executorService;
    private static boolean emailAdapterCreated = false;
    private static final String emailAdapterName = BillingConstants.EMAIL_ADAPTER_NAME;
    private static OutputEventAdapterConfiguration outputEventAdapterConfiguration = null;
    private static boolean emailAdapterCreatedResult = false;

    private static EmailNotifications instance = new EmailNotifications();

    private EmailNotifications() {

        mailQueue = new ConcurrentLinkedQueue<>();
        executorService = Executors.newFixedThreadPool(3);
        MailQueueObserver mailQueueObserver = new MailQueueObserver();
        addObserver(mailQueueObserver);
    }

    public static EmailNotifications getInstance() {
        return instance;
    }

    /**
     * Shutdown the thread pool
     */
    public void shutdownAndAwaitTermination() {
        executorService.shutdown();
        try {
            // Wait a while for existing tasks to terminate
            if (!executorService.awaitTermination(BillingConstants.DEFAULT_TIMEOUT_VALUE, TimeUnit.SECONDS)) {
                executorService.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executorService.awaitTermination(BillingConstants.DEFAULT_TIMEOUT_VALUE, TimeUnit.SECONDS)) {
                    LOGGER.error("email sender executor pool did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            LOGGER.error("An error occurred when shutting down and awaiting the termination of the existing tasks. " +
                         "Error received :", ie);
            // (Re-)Cancel if current thread also interrupted
            executorService.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Add email to the mail queue
     *
     * @param messageBody email body
     * @param subject     email subject
     * @param receiver    receivers
     * @param contentType content type of the email
     */
    public void sendMail(String messageBody, String subject, String receiver, String contentType) {
        try {
            InternetAddress emailAddr = new InternetAddress(receiver);
            emailAddr.validate();
            Map<String, String> mail = new HashMap<>();
            mail.put(MESSAGE_BODY, messageBody);
            mail.put(MESSAGE_SUBJECT, subject);
            mail.put(MESSAGE_RECEIVER, receiver);
            mail.put(MESSAGE_TYPE, contentType);
            mailQueue.add(mail);
            setChanged();
            notifyObservers();
        } catch (AddressException e) {
            LOGGER.error("Email sending failed. Invalid receiver address specified: " + receiver);
        }
    }

    /**
     * Create Output Event Adapter Configuration for given configuration.
     *
     * @param name      Output Event Adapter name
     * @param type      Output Event Adapter type
     * @param msgFormat Output Event Adapter message format
     * @return OutputEventAdapterConfiguration instance for given configuration
     */
    private static OutputEventAdapterConfiguration createOutputEventAdapterConfiguration(
            String name, String type, String msgFormat) {
        if (outputEventAdapterConfiguration == null) {
            outputEventAdapterConfiguration =
                    new OutputEventAdapterConfiguration();
            outputEventAdapterConfiguration.setName(name);
            outputEventAdapterConfiguration.setType(type);
            outputEventAdapterConfiguration.setMessageFormat(msgFormat);
        }
        return outputEventAdapterConfiguration;
    }

    /**
     * This method creates the output email adapter. For the successful creation of the adapter,
     * the file output-event-adapters.xml needs to be added to the repository/conf folder of the product.
     */
    protected static void createEmailAdapter() {
        outputEventAdapterConfiguration =
                createOutputEventAdapterConfiguration(emailAdapterName,
                                                      BillingConstants.RENDERING_TYPE_EMAIL,
                                                      BillingConstants.EMAIL_MESSAGE_FORMAT);
        while (!isEmailAdapterCreated()) {
            try {
                CloudBillingServiceComponent.getOutputEventAdapterService().create(
                        outputEventAdapterConfiguration);
                LOGGER.info("The email adapter " + emailAdapterName + " created Successfully");
                emailAdapterCreatedResult = true;
            } catch (OutputEventAdapterException e) {
                LOGGER.error("Unable to create the Output Event Adapter : " + emailAdapterName +
                             ". Error received :", e);
            }
            setEmailAdapterCreated(emailAdapterCreatedResult);
        }
    }

    /**
     * Method which returns the status of the email adapter creation.
     *
     * @return
     */
    protected static boolean isEmailAdapterCreated() {
        return emailAdapterCreated;
    }

    /**
     * Method which sets the status of the email adapter creation.
     *
     * @param emailAdapterCreated is the status of the email adapter creation.
     */
    protected static void setEmailAdapterCreated(boolean emailAdapterCreated) {
        EmailNotifications.emailAdapterCreated = emailAdapterCreated;
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
            while (!mailQueue.isEmpty()) {
                Map email = mailQueue.poll();
                MailSender mailSender = new MailSender(mailSenderErrorObserver, email);
                executorService.execute(mailSender);
            }
        }
    }

    /**
     * This is to keep the sending failed emails and try sending them again
     * once the error has been fixed
     */
    protected static class MailSenderErrorObserver implements Observer {

        private Queue<Map> failedEmailQueue;

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
                while (!failedEmailQueue.isEmpty()) {
                    Map email = failedEmailQueue.poll();
                    EmailNotifications.getInstance().sendMail(email.get(MESSAGE_BODY).toString(),
                                                              email.get(MESSAGE_SUBJECT).toString(),
                                                              email.get(MESSAGE_RECEIVER).toString(),
                                                              email.get(MESSAGE_TYPE).toString());
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
            Map<String, String> dynamicPropertiesForEmail = new HashMap<String, String>();
            dynamicPropertiesForEmail
                    .put(MESSAGE_RECEIVER, email.get(MESSAGE_RECEIVER).toString());
            dynamicPropertiesForEmail
                    .put(MESSAGE_SUBJECT, email.get(MESSAGE_SUBJECT).toString());
            dynamicPropertiesForEmail.put(MESSAGE_TYPE, email.get(MESSAGE_TYPE).toString());

            //Making sure that the email adapter has been created before sending the emails.
            try {
                if (!isEmailAdapterCreated()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.info("Creating the email adapter " + emailAdapterName + "since not created before");
                    }
                    createEmailAdapter();
                }

                if (isEmailAdapterCreated()) {
                    CloudBillingServiceComponent.getOutputEventAdapterService()
                                                .publish(emailAdapterName,
                                                         dynamicPropertiesForEmail,
                                                         email.get(MESSAGE_BODY).toString());
                    setChanged();
                    notifyObservers(true);
                } else {
                    setChanged();
                    notifyObservers(email);
                    LOGGER.error("Error while sending the email notification to: " +
                                 email.get(MESSAGE_RECEIVER).toString() +
                                 " under subject: " + email.get(MESSAGE_SUBJECT).toString() +
                                 ". Email is added " +
                                 "back to the queue since email adapter has not been created");
                }
            } catch (Exception e) {
                //Catching the generic exception here to avoid throwing exceptions while executing this sub task
                setChanged();
                notifyObservers(email);
                LOGGER.error("Error while sending the email notification to: " +
                             email.get(MESSAGE_RECEIVER).toString
                                     () + " under subject: " +
                             email.get(MESSAGE_SUBJECT).toString() + ". Email is added " +
                             "back to the queue. once the error is fixed it will try again ", e);
            }
        }
    }
}
