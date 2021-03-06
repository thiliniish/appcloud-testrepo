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
package org.wso2.carbon.cloud.signup.email.sender;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.cloud.signup.config.reader.ConfigFileReader;
import org.wso2.carbon.cloud.signup.constants.SignUpWorkflowConstants;
import org.wso2.carbon.cloud.signup.file.reader.FileContentReader;
import org.wso2.carbon.cloud.signup.internal.ServiceReferenceHolder;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;


/**
 * The EmailSender class contains the configurations and sends the emails to the relevant users.
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings(
        value = "SIC_INNER_SHOULD_BE_STATIC_ANON",
        justification = "SInce it is not necessary to convert this to an inner class")
public class EmailManager implements Serializable {
    private static final long serialVersionUID = 1L;
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
     * @param recepient            the user who will be receiving the email
     * @param senderEmail          the user who is sending the email
     * @param senderEmailSignature the sender's signature
     * @param subject              the subject of the email
     * @param messageBody          the content being sent in the email
     * @param isCustomized         indicates whether sending a customized email or not
     * @throws WorkflowException
     */
    public void sendEmail(String recepient, String senderEmail, String senderEmailSignature,
                          String subject, String messageBody, boolean isCustomized, String tenantDomain)
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

            Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(emailAddress, emailPassword);
                }
            });
            Message message = new MimeMessage(session);
            message.setSubject(subject);
            message.setFrom(new InternetAddress(fromEmailAddress, senderEmailSignature));
            message.setReplyTo(InternetAddress.parse(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recepient));

            if (isCustomized) {
                MimeMultipart multipart = getCustomizedImage(messageBody, tenantDomain);
                message.setContent(multipart);
            } else {
                message.setContent(messageBody, "text/html");
            }
            //Checking if the email contents is empty or not
            if (" ".equalsIgnoreCase(messageBody)) {
                errorMessage = "Error sending email to " + signedUpUser + " for the tenant " + tenantDomain +
                               ". Email content is empty";
                throw new WorkflowException(errorMessage);
            } else {
                ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
                //Added to load the latest javax.mail.Message class
                Thread.currentThread().setContextClassLoader(javax.mail.Message.class.getClassLoader());
                message.saveChanges();
                Transport.send(message);
                Thread.currentThread().setContextClassLoader(currentClassLoader);
            }
        } catch (AddressException e) {
            //will handle a wrongly formatted addresses
            errorMessage =
                    "Error in the recipient " + recepient + " address for the tenant domain " +
                    tenantDomain;
            throw new WorkflowException(errorMessage, e);

        } catch (MessagingException e) {
            errorMessage =
                    "Error sending email to " + recepient + " of the tenant domain " + tenantDomain;
            throw new WorkflowException(errorMessage, e);
        } catch (UnsupportedEncodingException e) {
            errorMessage =
                    "Error in parsing the from address for the tenant domain " +
                    tenantDomain;
            throw new WorkflowException(errorMessage, e);
        }
    }

    /**
     * Returns a customized message with an embedded image
     *
     * @param messageBody the message to be sent in email
     * @return MimeMultipart with embedded image
     * @throws MessagingException
     */
    private MimeMultipart getCustomizedImage(String messageBody, String tenantDomain) throws MessagingException {
        // This HTML mail has to 2 parts, the BODY and the embedded image
        MimeMultipart multipart = new MimeMultipart("related");

        // first part  (the html)
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(messageBody, "text/html");
        // add it
        multipart.addBodyPart(messageBodyPart);

        try {
            //Get image path specific to tenant
            TenantManager tenantManager = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager();
            int tenantId = tenantManager.getTenantId(tenantDomain);
            String imagePath = CarbonUtils.getCarbonHome() + File.separator + "repository" +
                               File.separator + "tenants" + File.separator +
                               tenantId + File.separator + "customizations" + File.separator +
                               "emailTemplates" +
                               File.separator + "customLogo";
            DataSource fds = new FileDataSource(imagePath);
            // second part (the image)
            messageBodyPart = new MimeBodyPart();
            messageBodyPart.setDataHandler(new DataHandler(fds));
            messageBodyPart.setHeader(SignUpWorkflowConstants.CONTENT_ID, "<header>");
            messageBodyPart.setHeader(SignUpWorkflowConstants.CONTENT_TYPE, "image/png");
            // add it
            multipart.addBodyPart(messageBodyPart);
        } catch (UserStoreException e) {
            log.error("Unable to get the customized email for tenant domain " + tenantDomain);
        }
        return multipart;
    }

    /**
     * This method configures the needed parameters to configure the email to be sent to the tenant.
     *
     * @param tenantDomain     the teant domain to which user is signing up
     * @param fromEmailAddress the user who is sending the email
     * @param tenantEmail      the email address of the tenant to whom the email needs to be sent to
     * @param user             the name of the user who signed up to the tenant
     * @param notifyAllAdmins  the flag indicating whether or not to inform admin users
     * @throws WorkflowExceptionreplaceValuesOfEmailContent
     */
    public void sendTenantEmail(String tenantDomain, String fromEmailAddress,
                                String tenantEmail, String user, boolean notifyAllAdmins)
            throws WorkflowException {
        try {
            emailSubject = ConfigFileReader
                    .retrieveConfigAttribute("EmailSubjects", "TENANT_EMAIL_SUBJECT");
            fromSignature =
                    ConfigFileReader.retrieveConfigAttribute("emailProperties", "fromSignature");
            setEmailFilesBaseDirectory(tenantDomain);
            boolean isCustomized = isCustomizedEmail(SignUpWorkflowConstants.TENANT_EMAIL_PATH);
            setEmailMessage(SignUpWorkflowConstants.TENANT_EMAIL_PATH, isCustomized);
            sendEmail(tenantEmail, fromEmailAddress, fromSignature, emailSubject, emailMessage, isCustomized,
                      tenantDomain);
            log.info("Sent email to notify the tenant " + tenantEmail + " of the user " + user +
                     " sign up to the tenant domain " + tenantDomain);
            if (notifyAllAdmins) {
                List<String> adminUserEmails = getAdminUsersofTenant(tenantDomain);
                for (String emailAddress : adminUserEmails) {
                    if (emailAddress != null && !emailAddress.equals(tenantEmail)) {
                        sendEmail(emailAddress, fromEmailAddress, fromSignature, emailSubject, emailMessage,
                                  isCustomized, tenantDomain);
                        log.info("Sent email to notify the admin user " + emailAddress + " of the user " + user +
                                 " sign up to the tenant domain " + tenantDomain);
                    }
                }
            }
        } catch (WorkflowException e) {
            errorMessage = "Could not configure the email for the tenant " + tenantDomain +
                           "for the self signed up user " + signedUpUser;
            throw new WorkflowException(errorMessage, e);
        } catch (UserStoreException e) {
            errorMessage = "Error occurred while getting admin users of the tenant " + tenantDomain +
                           "for the self signed up user " + signedUpUser;
            throw new WorkflowException(errorMessage, e);
        }
    }

    /**
     * This method configures the needed parameters to configure the email to be sent to the user informing about the
     * status of the signup request
     *
     * @param tenantDomain         the tenant domain to which the user signed up
     * @param tenantContactEmail   the contact email for tenant
     * @param tenantEmailSignarure the signature of tenant
     * @param userEmail            the email of the user to whom the email needs to be sent.
     * @throws WorkflowException
     */
    public void sendUserNotificationEmail(String tenantDomain, String tenantContactEmail, String tenantEmailSignarure,
                                          String userEmail) throws WorkflowException {
        try {
            emailSubject =
                    ConfigFileReader.retrieveConfigAttribute("EmailSubjects", "USER_EMAIL_SUBJECT");
            setEmailFilesBaseDirectory(tenantDomain);
            boolean isCustomized = isCustomizedEmail(SignUpWorkflowConstants.USER_EMAIL_PATH);
            setEmailMessage(SignUpWorkflowConstants.USER_EMAIL_PATH, isCustomized);
            sendEmail(userEmail, tenantContactEmail, tenantEmailSignarure, emailSubject,
                      emailMessage, isCustomized, tenantDomain);
            log.info("Sent email to notify the user " + userEmail +
                     " of the sign up status for the tenant " + tenantDomain);
        } catch (WorkflowException e) {
            errorMessage =
                    "Could not configure the email for the user " + userEmail + " of the tenant " +
                    tenantDomain;
            throw new WorkflowException(errorMessage, e);
        }
    }

    /**
     * This method configures the needed parameters to configure the email sent to the user if the signup request was
     * approved/rejected
     *
     * @param tenantDomain         the tenant domain to which the user signed up
     * @param tenantContactEmail   the contact email for tenant
     * @param tenantEmailSignarure the signature of tenant
     * @param userEmail            the email of the user to whom the email needs to be sent.
     * @param emailFile            the name of the file which includes the email
     * @param status               the status of the approval of signup
     * @throws WorkflowException
     */
    public void sendApprovalStatusEmail(String tenantDomain, String tenantContactEmail,
                                        String tenantEmailSignarure, String userEmail,
                                        String emailFile, String status)
            throws WorkflowException {
        try {
            if ("approved".equals(status)) {
                emailSubject = ConfigFileReader.retrieveConfigAttribute("EmailSubjects",
                                                                        "SIGNUP_REQUEST_APPROVAL_EMAIL");
            } else {
                emailSubject = ConfigFileReader.retrieveConfigAttribute("EmailSubjects",
                                                                        "SIGNUP_REQUEST_REJECTION_EMAIL");
            }
            setEmailFilesBaseDirectory(tenantDomain);
            boolean isCustomized = isCustomizedEmail(emailFile);
            setEmailMessage(emailFile, isCustomized);
            sendEmail(userEmail, tenantContactEmail, tenantEmailSignarure, emailSubject, emailMessage, isCustomized,
                      tenantDomain);
            log.info("Email sent to user " + userEmail + " of the tenant " + tenantDomain +
                     " regarding the sign up approval");

        } catch (WorkflowException e) {
            errorMessage =
                    "Could not configure the email for the user " + userEmail + " of the tenant " +
                    tenantDomain;
            throw new WorkflowException(errorMessage, e);

        }

    }

    /**
     * @param tenantDomain     the tenant domain to which the user signed up
     * @param fromEmailAddress the user who is sending the email
     * @param tenantEmail      the email address of the tenant to whom the email needs to be sent to
     * @param user             the user who has sent the sign up request
     * @param notifyAllAdmins  the flag indicating whether or not to inform admin users
     * @throws WorkflowException
     */
    public void sendTenantNotificationEmail(String tenantDomain, String fromEmailAddress, String tenantEmail,
                                            String user, boolean notifyAllAdmins) throws WorkflowException {
        try {
            emailSubject = ConfigFileReader.retrieveConfigAttribute("EmailSubjects", "TENANT_EMAIL_SUBJECT");
            fromSignature = ConfigFileReader.retrieveConfigAttribute("emailProperties", "fromSignature");
            setEmailFilesBaseDirectory(tenantDomain);
            boolean isCustomized = isCustomizedEmail(SignUpWorkflowConstants.TENANT_NOTIFICATION_EMAIL_PATH);
            setEmailMessage(SignUpWorkflowConstants.TENANT_NOTIFICATION_EMAIL_PATH, isCustomized);
            sendEmail(tenantEmail, fromEmailAddress, fromSignature, emailSubject, emailMessage, isCustomized,
                      tenantDomain);
            log.info("Sent email to notify the tenant " + tenantEmail + " of the user " + user +
                     " sign up to the tenant domain " + tenantDomain);
            if (notifyAllAdmins) {
                List<String> adminUserEmails = getAdminUsersofTenant(tenantDomain);
                for (String emailAddress : adminUserEmails) {
                    if (emailAddress != null && !emailAddress.equals(tenantEmail)) {
                        sendEmail(emailAddress, fromEmailAddress, fromSignature, emailSubject, emailMessage,
                                  isCustomized, tenantDomain);
                        log.info("Sent email to notify the admin user " + emailAddress + " of the user " + user +
                                 " sign up to the tenant domain " + tenantDomain);
                    }
                }
            }
        } catch (WorkflowException e) {
            errorMessage = "Could not configure the email for the tenant " + tenantDomain +
                           "for the self signed up user " + signedUpUser;
            throw new WorkflowException(errorMessage, e);
        } catch (UserStoreException e) {
            errorMessage = "Error occurred while getting admin users of the tenant " + tenantDomain +
                           "for the self signed up user " + signedUpUser;
            throw new WorkflowException(errorMessage, e);
        }
    }

    /**
     * Populates the map with the key value pairs needed to replace the placeholders for the emails
     *
     * @param contactAddress     the tenant's contact address
     * @param cloudmgtLink       the link of the cloud mgt verificaiton
     * @param tenantDomain       the domain of the tenant of the sign up flow
     * @param fromAddress        the from address of the tenant
     * @param adminDashboardLink the link of the admin dashboard in the API manager
     * @param user               the user who signed up to the tenant
     */
    public void setEmailProperyKeyValueMap(String contactAddress, String cloudmgtLink,
                                           String tenantDomain, String fromAddress,
                                           String adminDashboardLink, String user) {
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
     * @param textToReplace the email content containing the placeholders
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

    /**
     * Sets the base directory for the email files
     */
    private void setEmailFilesBaseDirectory(String tenantDomain) {
        try {
            TenantManager tenantManager = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager();
            int tenantId = tenantManager.getTenantId(tenantDomain);
            String customizedEmailFilesBaseDirectory = CarbonUtils.getCarbonHome() + File.separator + "repository" +
                                                       File.separator + "tenants" + File.separator +
                                                       tenantId + File.separator + "customizations" +
                                                       File.separator +
                                                       "emailTemplates";
            File file = new File(customizedEmailFilesBaseDirectory);
            //The directory exists only if the emails are customized
            if (file.isDirectory()) {
                emailFilesBaseDirectory = customizedEmailFilesBaseDirectory + File.separator;
            } else {
                //If emails are not customized set the base directory to default location
                emailFilesBaseDirectory = CarbonUtils.getCarbonHome() + File.separator + "resources" +
                                          File.separator + "signUpConfig" + File.separator +
                                          "emailFiles" + File.separator;
            }
        } catch (UserStoreException e) {
            log.error("Unable to check whether emails are customized for tenant domain " + tenantDomain);
            //Set the email file base directory to default location
            emailFilesBaseDirectory = CarbonUtils.getCarbonHome() + File.separator + "resources" +
                                      File.separator + "signUpConfig" + File.separator +
                                      "emailFiles" + File.separator;
        }
    }

    /**
     * This method sets the email message and returns a flag indicating whether the email is customized
     *
     * @param emailPath the path to email template
     * @throws WorkflowException
     */
    private void setEmailMessage(String emailPath, boolean isCustomized) throws WorkflowException {
        if (isCustomized) {
            String customEmailPath = emailFilesBaseDirectory + SignUpWorkflowConstants.CUSTOMIZED + emailPath;
            emailMessage = replaceValuesOfEmailContent(reader.fileReader(customEmailPath));
        } else {
            emailFile = emailFilesBaseDirectory + emailPath;
            emailMessage = replaceValuesOfEmailContent(reader.fileReader(emailFile));
        }
    }

    /**
     * This method returns a flag indicating whether the email is customized
     *
     * @param emailPath the path to email template
     * @return boolean indicating whether email is customized
     */
    private boolean isCustomizedEmail(String emailPath) {
        String customEmailPath = emailFilesBaseDirectory + SignUpWorkflowConstants.CUSTOMIZED + emailPath;
        File customEmailFile = new File(customEmailPath);
        return customEmailFile.exists();
    }

    /**
     * Get a list of email addresses of the admins of given tenant domain
     *
     * @param tenantDomain the domain of the tenant of the sign up flow
     * @return List of Strings
     * @throws UserStoreException
     */
    public List<String> getAdminUsersofTenant(String tenantDomain) throws UserStoreException {
        TenantManager tenantManager = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager();
        int tenantId = tenantManager.getTenantId(tenantDomain);
        List<String> adminUserEmails = new ArrayList<String>();

        UserStoreManager userStoreManager = ServiceReferenceHolder.getInstance().getRealmService().getTenantUserRealm(
                tenantId).getUserStoreManager();
        String[] users = userStoreManager.getUserListOfRole("admin");
        for (String user : users) {
            String[] claims = { SignUpWorkflowConstants.EMAIL_CLAIM_URI };
            if (userStoreManager.isExistingUser(user)) {
                Map<String, String> userClaims = userStoreManager.getUserClaimValues(user, claims, null);
                String email = userClaims.get(SignUpWorkflowConstants.EMAIL_CLAIM_URI);
                adminUserEmails.add(email);
            }
        }
        return adminUserEmails;
    }
}

