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
package org.wso2.carbon.cloud.signup.emailSender;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.cloud.signup.configReader.ConfigFileReader;
import org.wso2.carbon.cloud.signup.constants.SignUpWorkflowConstants;
import org.wso2.carbon.cloud.signup.fileReader.FileContentReader;
import org.wso2.carbon.utils.CarbonUtils;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.BodyPart;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.DataHandler;
import javax.activation.MailcapCommandMap;
import javax.activation.CommandMap;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * The EmailSender class contains the configurations and sends the emails to the relevant users.
 */
public class EmailManager implements Serializable {
    private static final Log log = LogFactory.getLog(EmailManager.class);
    static HashMap<String, String> emailConfigurationMap = new HashMap<String, String>();

    String errorMessage;
    String emailSubject;
    String emailMessage;
    String emailFile;
    String tenantDomain;
    String signedUpUser;
    String fromSignature;

    private String emailFilesBaseDirectory;

    FileContentReader reader = new FileContentReader();

    public EmailManager() {

    }

    /**
     * This method sends the email to the user.
     *
     * @param recepient   is the user who will be receiving the email
     * @param subject     is the subject of the email.
     * @param messageBody is the content being sent in the email
     * @throws WorkflowException
     */
    public void sendEmail(String recepient, String senderEmail, String senderEmailSignature,
                          String subject, String messageBody)
            throws WorkflowException {

        //Retrieving the mail properties

        String port = ConfigFileReader.retrieveConfigAttribute("emailProperties", "port");
        String host = ConfigFileReader.retrieveConfigAttribute("emailProperties", "host");
        final String emailAddress = ConfigFileReader.retrieveConfigAttribute("emailProperties",
                                                                             "username");
        final String emailPassword = ConfigFileReader.retrieveConfigAttribute("emailProperties",
                                                                              "password");
        String fromEmailAddress =
                ConfigFileReader.retrieveConfigAttribute("emailProperties", "fromEmail");
        try {

            //Setting the email properties
            log.info("Initializing the email sending properties");
            Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.port", port);
            //properties.put("mail.smtp.from", InternetAddress.parse(senderEmail));
            log.info(messageBody);

            log.info("adding the properties");
            Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                            emailAddress,
                            emailPassword);
                }
            });

            Message message = new MimeMessage(session);
            message.setSubject(subject);
            message.setFrom(new InternetAddress(fromEmailAddress, senderEmailSignature));
            message.setReplyTo(InternetAddress.parse(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recepient));

            //
            // This HTML mail have to 2 part, the BODY and the embedded image
            //
            MimeMultipart multipart = new MimeMultipart("related");

            // first part  (the html)
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(messageBody, "text/html");
            // add it
            multipart.addBodyPart(messageBodyPart);

            // second part (the image)
            messageBodyPart = new MimeBodyPart();
            DataSource fds = new FileDataSource
                                     ("/home/dilhasha/sheniCloud/wso2am-1.10.0/repository/tenants/40/customizations/customLogo.png");
            messageBodyPart.setDataHandler(new DataHandler(fds));
            messageBodyPart.setHeader("Content-ID","<header>");
            messageBodyPart.setHeader("Content-Type", "image/png");
            // add it
            multipart.addBodyPart(messageBodyPart);

            // put everything together
            message.setContent(multipart);

            //Checking if the email contents is empty or not
            if (" ".equalsIgnoreCase(messageBody)) {
                errorMessage = "Error sending email to " + signedUpUser + " for the tenant " +
                               tenantDomain + ". Email content is empty";
                log.error(errorMessage);
                throw new WorkflowException(errorMessage);
            } else {
                //Added to load the latest javax.mail.Message class
                Thread.currentThread().setContextClassLoader(javax.mail.Message.class.getClassLoader());
                message.saveChanges();
                Transport.send(message);
            }
        }

        //will handle a wrongly formatted addresses
        catch (AddressException e) {
            errorMessage =
                    "Error in the recipient " + recepient + " address for the tenant domain " +
                    tenantDomain;
            log.error(errorMessage, e);
            throw new WorkflowException(errorMessage, e);

        } catch (MessagingException e) {
            errorMessage =
                    "Error sending email to " + recepient + " of the tenant domain " + tenantDomain;
            log.error(errorMessage, e);
            throw new WorkflowException(errorMessage, e);
        } catch (UnsupportedEncodingException e) {
            errorMessage =
                    "Error in parsing the from address for the tenant domain " +
                    tenantDomain;
            log.error(errorMessage, e);
            throw new WorkflowException(errorMessage, e);
        }
    }

    /**
     * This method configures the needed parameters to configure the email to be sent to the tenant.
     *
     * @param tenantDomain is the domain from which the user signed up to
     * @param tenantEmail  is the email address of the tenant to whom the email needs to be sent to
     * @param user         is the name of the user who signed up to the tenant
     * @throws WorkflowException
     */
    public void configureTenantEmail(String tenantDomain, String fromEmailAddress,
                                     String tenantEmail, String user)
            throws WorkflowException {
        try {
            log.info("In the email manager");
            emailSubject = ConfigFileReader
                    .retrieveConfigAttribute("EmailSubjects", "TENANT_EMAIL_SUBJECT");
            emailFilesBaseDirectory = CarbonUtils.getCarbonHome() + File.separator + "resources" +
                                      File.separator + "signUpConfig" + File.separator +
                                      "emailFiles" +
                                      File.separator;
            emailFile = emailFilesBaseDirectory + SignUpWorkflowConstants.TENANT_EMAIL_PATH;
            emailMessage = replaceValuesOfEmailContent(reader.fileReader(emailFile));
            fromSignature =
                    ConfigFileReader.retrieveConfigAttribute("emailProperties", "fromSignature");
            sendEmail(tenantEmail, fromEmailAddress, fromSignature, emailSubject, emailMessage);
            log.info("Sent email to notify the tenant " + tenantEmail + " of the user " + user +
                     " sign up to the tenant domain " + tenantDomain);
        } catch (WorkflowException e) {
            errorMessage = "Could not configure the email for the tenant " + tenantDomain +
                           "for the self signed up user " + signedUpUser;
            log.error(errorMessage, e);
            throw new WorkflowException(errorMessage, e);
        }
    }

    /**
     * This method configures the needed parameters to configure the email to be sent to the user informing about the status of the signup request
     *
     * @param tenantDomain is the tenant domain to which the user sighned up to
     * @param userEmail    is the email of the user to whom the email needs to be sent.
     * @throws WorkflowException
     */
    public void configureUserNotificationEmail(String tenantDomain, String tenantContactEmail,
                                               String tenantEmailSignarure, String userEmail)
            throws WorkflowException {
        try {

            emailFilesBaseDirectory = CarbonUtils.getCarbonHome() + File.separator + "resources" +
                                      File.separator + "signUpConfig" + File.separator +
                                      "emailFiles" +
                                      File.separator;
            emailFile = emailFilesBaseDirectory + SignUpWorkflowConstants.USER_EMAIL_PATH;
            emailMessage = replaceValuesOfEmailContent(reader.fileReader(emailFile));
            emailSubject =
                    ConfigFileReader.retrieveConfigAttribute("EmailSubjects", "USER_EMAIL_SUBJECT");
            sendEmail(userEmail, tenantContactEmail, tenantEmailSignarure, emailSubject,
                      emailMessage);
            log.info("Sent email to notify the user " + userEmail +
                     " of the sign up status for the tenant " + tenantDomain);
        } catch (WorkflowException e) {
            errorMessage =
                    "Could not configure the email for the user " + userEmail + " of the tenant " +
                    tenantDomain;
            log.error(errorMessage, e);
            throw new WorkflowException(errorMessage, e);
        }
    }

    /**
     * This method configures the needed parameters to configure the email sent to the user if the signup request was approved/rejected
     *
     * @param tenantDomain
     * @param userEmail
     * @throws WorkflowException
     */
    public void configureApprovalStatusEmail(String tenantDomain, String tenantContactEmail,
                                             String tenantEmailSignarure, String userEmail,
                                             String emailFile, String status)
            throws WorkflowException {
        try {
            emailFilesBaseDirectory = CarbonUtils.getCarbonHome() + File.separator + "resources" +
                                      File.separator + "signUpConfig" + File.separator +
                                      "emailFiles" +
                                      File.separator;
            emailFile =
                    emailFilesBaseDirectory + emailFile;
            if ("approved".equals(status)) {
                emailSubject = ConfigFileReader.retrieveConfigAttribute("EmailSubjects",
                                                                        "SIGNUP_REQUEST_APPROVAL_EMAIL");
            } else {
                emailSubject = ConfigFileReader.retrieveConfigAttribute("EmailSubjects",
                                                                        "SIGNUP_REQUEST_REJECTION_EMAIL");
            }
            emailMessage =
                    replaceValuesOfEmailContent(
                            reader.fileReader(
                                    emailFile));

            sendEmail(userEmail, tenantContactEmail, tenantEmailSignarure, emailSubject,
                      emailMessage);
            log.info("Email sent to user " + userEmail + " of the tenant " + tenantDomain +
                     " regarding the sign up approval");

        } catch (WorkflowException e) {
            errorMessage =
                    "Could not configure the email for the user " + userEmail + " of the tenant " +
                    tenantDomain;
            log.error(errorMessage, e);
            throw new WorkflowException(errorMessage, e);

        }

    }

    public void configureTenantNotificationEmail(String tenantDomain, String fromEmailAddress,
                                                 String tenantEmail,
                                                 String user)
            throws WorkflowException {
        try {
            emailFilesBaseDirectory = CarbonUtils.getCarbonHome() + File.separator + "resources" +
                                      File.separator + "signUpConfig" + File.separator +
                                      "emailFiles" +
                                      File.separator;
            emailSubject = ConfigFileReader
                    .retrieveConfigAttribute("EmailSubjects", "TENANT_EMAIL_SUBJECT");
            log.info("The carbon home being set inside the email manager class is " +
                     CarbonUtils.getCarbonHome());
            emailFile = emailFilesBaseDirectory +
                        SignUpWorkflowConstants.TENANT_NOTIFICATION_EMAIL_PATH;
            emailMessage = replaceValuesOfEmailContent(reader.fileReader(emailFile));
            fromSignature =
                    ConfigFileReader.retrieveConfigAttribute("emailProperties", "fromSignature");
            sendEmail(tenantEmail, fromEmailAddress, fromSignature, emailSubject, emailMessage);
            log.info("Sent email to notify the tenant " + tenantEmail + " of the user " + user +
                     " sign up to the tenant domain " + tenantDomain);
        } catch (WorkflowException e) {
            errorMessage = "Could not configure the email for the tenant " + tenantDomain +
                           "for the self signed up user " + signedUpUser;
            log.error(errorMessage, e);
            throw new WorkflowException(errorMessage, e);
        }
    }

    /**
     * Populates the map with the key value pairs needed to replace the placeholders for the emails
     *
     * @param contactAddress     is the tenant's contact address
     * @param cloudmgtLink       is the link of the cloud mgt verificaiton
     * @param tenantDomain       is the domain of the tenant of the sign up flow
     * @param fromAddress        is the from address of the tenant
     * @param adminDashboardLink is the link of the admin dashboard in the API manager
     * @param user               is the user who signed up to the tenant
     */
    public void setEmailProperyKeyValueMap(String contactAddress, String cloudmgtLink,
                                           String tenantDomain, String fromAddress,
                                           String adminDashboardLink, String user) {

        log.info("setting the email properties");
        emailConfigurationMap.put("%%CONTACT", contactAddress);
        emailConfigurationMap.put("%%LINK", cloudmgtLink);
        emailConfigurationMap.put("%%TENANT", tenantDomain);
        emailConfigurationMap.put("%%FROMADDRESS", fromAddress);
        emailConfigurationMap
                .put("%%ADMINDASHBOARD", adminDashboardLink);
        emailConfigurationMap.put("%%USERNAME", user);
        log.info("set the email properties");

    }

    /**
     * This method returns the key value map to be replaced in the email content.
     *
     * @return the key value map
     */
    private static Map<String, String> getEmailProperyKeyValueMap() {
        return emailConfigurationMap;
    }

    /**
     * This method replaces the placeholders in the email content.
     *
     * @param textToReplace is the email content containing the placeholders
     * @return the replaced email content
     */
    private static String replaceValuesOfEmailContent(String textToReplace) {
        Map<String, String> emailPropertyMap = getEmailProperyKeyValueMap();
        String key;
        String value;
        for (Map.Entry<String, String> entry : emailPropertyMap.entrySet()) {
            value = entry.getValue();
            key = entry.getKey();
            if (value != null) {
                textToReplace = textToReplace.replaceAll(key, value);
            }
        }
        return textToReplace;
    }

}

