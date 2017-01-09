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
package org.wso2.carbon.cloud.common.email;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.common.CloudMgtConstants;
import org.wso2.carbon.cloud.common.CloudMgtException;

import java.io.IOException;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;


/**
 * EmailSender class holds methods to send email.
 */
public class EmailSender {
    private static final Log log = LogFactory.getLog(EmailSender.class);
    private static final String MAIL_SMTP_HOST = "mail.smtp.host";
    private static final String MAIL_SMTP_PORT = "mail.smtp.port";
    private static final String MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";
    private static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    private Multipart multipart;
    private MimeMessage message;
    private String text;
    private String html;

    /**
     * Constructor to create instances.
     *
     * @param host       Host
     * @param port       Port
     * @param username   Username
     * @param password   Password
     * @param tlsEnabled if true Enable TLS for the communication
     */
    public EmailSender(String host, String port, String username, String password, boolean tlsEnabled) {
        if (log.isDebugEnabled()) {
            String msg = String.format("Initializing email sender.host:%s, port:%s, tlsEnabled:%b", host, port,
                                       tlsEnabled);
            log.debug(msg);
        }
        multipart = new MimeMultipart("related");
        Properties properties = new Properties();
        properties.put(MAIL_SMTP_HOST, host);
        properties.put(MAIL_SMTP_PORT, port);

        if (tlsEnabled) {
            properties.put(MAIL_SMTP_STARTTLS_ENABLE, "true");
        } else {
            properties.put(MAIL_SMTP_STARTTLS_ENABLE, "false");
        }
        properties.put(MAIL_SMTP_AUTH, "true");

        SMTPAuthenticator smtpAuthenticator = new SMTPAuthenticator(username, password);
        Session session = Session.getInstance(properties, smtpAuthenticator);
        message = new MimeMessage(session);
    }

    /**
     * Get the "From" address of the email.
     *
     * @return From Address
     * @throws CloudMgtException
     */
    public String getFrom() throws CloudMgtException {
        if (log.isDebugEnabled()) {
            log.debug("Get the From address.");
        }
        try {
            String from = null;
            Address[] addresses = message.getFrom();
            if (addresses != null && addresses.length > 0) {
                from = addresses[0].toString();
            }
            return from;
        } catch (MessagingException e) {
            String errorMsg = "Error while getting the From address of the email";
            throw new CloudMgtException(errorMsg, e);
        }
    }

    /**
     * Set the "From" address of the email.
     *
     * @param from From Address
     * @throws CloudMgtException
     */
    public void setFrom(String from) throws CloudMgtException {
        if (log.isDebugEnabled()) {
            log.debug("Set From address : " + from);
        }
        try {
            message.setFrom(new InternetAddress(from));
        } catch (MessagingException e) {
            String errorMsg = "Error while setting the From address of the email. Address :" + from;
            throw new CloudMgtException(errorMsg, e);
        }
    }

    /**
     * Get the "To" addresses of the mail.
     *
     * @return To Addresses
     * @throws CloudMgtException
     */
    public String[] getTo() throws CloudMgtException {
        if (log.isDebugEnabled()) {
            log.debug("Get To address");
        }
        return getRecipients(Message.RecipientType.TO);
    }

    /**
     * Set the "To" address/addresses for the email. Accept single address or multiple addresses.
     *
     * @param toObject Single Address(String)/Multiple Addresses(String Array)
     * @throws CloudMgtException
     */
    public void setTo(Object toObject) throws CloudMgtException {
        if (log.isDebugEnabled()) {
            log.debug("Set To address");
        }
        addRecipients(Message.RecipientType.TO, toObject);
    }

    /**
     * Get the "CC" addresses of the email.
     *
     * @return CC addresses
     * @throws CloudMgtException
     */
    public String[] getCC() throws CloudMgtException {
        if (log.isDebugEnabled()) {
            log.debug("Get CC addresses");
        }
        return getRecipients(Message.RecipientType.CC);
    }

    /**
     * Set the "CC" address/addresses for the email. Accept single address or multiple addresses.
     *
     * @param ccObject Single Address(String)/Multiple Addresses(String Array)
     * @throws CloudMgtException
     */
    public void setCC(Object ccObject) throws CloudMgtException {
        if (log.isDebugEnabled()) {
            log.debug("Set CC addresses");
        }
        addRecipients(Message.RecipientType.CC, ccObject);
    }

    /**
     * Get the "BCC" addresses of the email.
     *
     * @return BCC Addresses
     * @throws CloudMgtException
     */
    public String[] getBCC() throws CloudMgtException {
        if (log.isDebugEnabled()) {
            log.debug("Get BCC addresses");
        }
        return getRecipients(Message.RecipientType.BCC);
    }

    /**
     * Set the "BCC" address/addresses to the email.
     *
     * @param bccObject Single Address(String)/Multiple Addresses(String Array)
     * @throws CloudMgtException
     */
    public void setBCC(Object bccObject) throws CloudMgtException {
        if (log.isDebugEnabled()) {
            log.debug("Set BCC addresses");
        }
        addRecipients(Message.RecipientType.BCC, bccObject);
    }

    /**
     * Get the "Subject" of the email.
     *
     * @return Subject
     * @throws CloudMgtException
     */
    public String getSubject() throws CloudMgtException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Get email subject :" + message.getSubject());
            }
            return message.getSubject();
        } catch (MessagingException e) {
            String errorMsg = "Error while getting the Subject of the email";
            throw new CloudMgtException(errorMsg, e);
        }
    }

    /**
     * Set the "Subject" of the email.
     *
     * @param subject Subject
     * @throws CloudMgtException
     */
    public void setSubject(String subject) throws CloudMgtException {
        if (log.isDebugEnabled()) {
            log.debug("Set email subject :" + subject);
        }
        try {
            message.setSubject(subject);
        } catch (MessagingException e) {
            String errorMsg = "Error while getting the Subject of the email.Subject :" + subject;
            throw new CloudMgtException(errorMsg, e);
        }
    }

    /**
     * Get the text content of   email body. if body content is set as plain text content.
     *
     * @return content
     */
    public String getText() {
        if (log.isDebugEnabled()) {
            log.debug("Get email text content :" + text);
        }
        return text;
    }

    /**
     * Set the body content of email
     *
     * @param text Text
     * @throws CloudMgtException
     */
    public void setText(String text) throws CloudMgtException {
        if (log.isDebugEnabled()) {
            log.debug("Set email body text content :" + text);
        }
        try {
            this.text = text;
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(text);
            multipart.addBodyPart(messageBodyPart);
        } catch (MessagingException e) {
            String msg = "Error while setting the email body content. Content. Content :" + text;
            throw new CloudMgtException(msg, e);
        }
    }

    /**
     * Get the body contain of the email.if body content is set as HTML content.
     *
     * @return
     */
    public String getHTML() {
        if (log.isDebugEnabled()) {
            log.debug("Get email body html content");
        }
        return html;
    }

    /**
     * Set the body content of email by providing HTML.
     *
     * @param html HTML
     * @throws CloudMgtException
     */
    public void setHTML(String html) throws CloudMgtException {
        if (log.isDebugEnabled()) {
            log.debug("Set email body html content :" + html);
        }
        try {
            this.html = html;
            BodyPart messageBodyPart = new MimeBodyPart();
            DataHandler dataHandler = new DataHandler(
                    new ByteArrayDataSource(this.html, "text/html"));
            messageBodyPart.setDataHandler(dataHandler);
            multipart.addBodyPart(messageBodyPart);
        } catch (IOException e) {
            String errorMsg = "Error while setting the email body content. Content :" + html;
            throw new CloudMgtException(errorMsg, e);
        } catch (MessagingException e) {
            String errorMsg = "Error while setting the email body content. Content :" + html;
            throw new CloudMgtException(errorMsg, e);
        }
    }

    /**
     * Add an attachment to the mail.
     *
     * @param path        File Path of Attachment
     * @param cid         Content-ID
     * @param contentType Content-Type
     * @throws CloudMgtException
     */
    public void addAttachment(String path, String cid, String contentType) throws CloudMgtException {
        if (log.isDebugEnabled()) {
            String msg = String.format("Add attachment to email. Attachment : [path : %s, cid: %s, Content-Type: %s]",
                                       path, cid, contentType);
            log.debug(msg);
        }
        DataSource fds = new FileDataSource(path);
        BodyPart messageBodyPart = new MimeBodyPart();
        try {
            messageBodyPart.setDataHandler(new DataHandler(fds));
            messageBodyPart.setHeader(CloudMgtConstants.CONTENT_ID, cid);
            messageBodyPart.setHeader(CloudMgtConstants.CONTENT_TYPE, contentType);
            multipart.addBodyPart(messageBodyPart);
        } catch (MessagingException e) {
            String errorMsg = String.format("Error while add attachment to email.Attachment : [path : %s, cid: %s, " +
                                                    "Content-Type: %s]", path, cid, contentType);
            throw new CloudMgtException(errorMsg, e);
        }
    }

    /**
     * Send the mail with all content provided.
     *
     * @throws CloudMgtException
     */
    public void send() throws CloudMgtException {
        if (log.isDebugEnabled()) {
            log.debug("Sending email");
        }
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(javax.mail.Session.class.getClassLoader());
        try {
            message.setContent(multipart);
            Transport.send(message);
        } catch (MessagingException e) {
            String errorMsg = "Error while sending the email with subject : " + this.getSubject();
            throw new CloudMgtException(errorMsg, e);
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
    }

    private void addRecipients(Message.RecipientType recipientType, Object recipientObject)
            throws CloudMgtException {
        if (log.isDebugEnabled()) {
            log.debug("Adding recipients. Recipient Type : " + recipientType.toString());
        }
        try {
            if (recipientObject instanceof String[]) {
                String[] to = (String[]) recipientObject;
                InternetAddress[] recipientAddresses = new InternetAddress[to.length];
                for (int i = 0; i < to.length; i++) {
                    recipientAddresses[i] = new InternetAddress(to[i]);
                    if (log.isDebugEnabled()) {
                        log.debug("To : " + to[i]);
                    }
                }
                message.addRecipients(recipientType, recipientAddresses);
            } else if (recipientObject instanceof String) {
                message.addRecipient(recipientType, new InternetAddress((String) recipientObject));
                if (log.isDebugEnabled()) {
                    log.debug("To: " + recipientObject);
                }
            } else {
                String errorMsg =
                        "The argument to this method should be an array of email addresses or a " +
                                "single email address";
                throw new CloudMgtException(errorMsg);
            }
        } catch (MessagingException e) {
            String errorMsg = "Error while adding recipients. Recipient type: " + recipientType.toString();
            throw new CloudMgtException(errorMsg, e);
        }
    }

    private String[] getRecipients(Message.RecipientType recipientType) throws CloudMgtException {
        if (log.isDebugEnabled()) {
            log.debug("Get recipients. Recipient Type : " + recipientType.toString());
        }
        try {
            Address[] addresses = message.getRecipients(recipientType);
            String[] recipients = new String[addresses.length];
            for (int i = 0; i < recipients.length; i++) {
                recipients[i] = addresses[i].toString();
                if (log.isDebugEnabled()) {
                    log.debug(recipientType.toString() + " :" + recipients[i]);
                }
            }
            return recipients;
        } catch (MessagingException e) {
            String errorMsg = "Error while getting the recipients.Recipient type: " +
                    recipientType.toString();
            throw new CloudMgtException(errorMsg, e);
        }
    }

    private static class SMTPAuthenticator extends javax.mail.Authenticator {
        private String username, password;

        private SMTPAuthenticator(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
        }
    }
}

