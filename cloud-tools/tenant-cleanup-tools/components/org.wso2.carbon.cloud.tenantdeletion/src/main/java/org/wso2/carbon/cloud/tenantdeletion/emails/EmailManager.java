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
 * under the License.
 */

package org.wso2.carbon.cloud.tenantdeletion.emails;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.tenantdeletion.beans.DeleteJob;
import org.wso2.carbon.cloud.tenantdeletion.beans.DeletedTenant;
import org.wso2.carbon.cloud.tenantdeletion.conf.EmailPropertiesType;
import org.wso2.carbon.cloud.tenantdeletion.constants.DeletionConstants;
import org.wso2.carbon.cloud.tenantdeletion.reader.ConfigReader;
import org.wso2.carbon.cloud.tenantdeletion.reader.FileContentReader;
import org.wso2.carbon.cloud.tenantdeletion.utils.DataAccessManager;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * The EmailSender class contains the configurations and sends the emails to the relevant users.
 */
public class EmailManager {
    private static final Log LOG = LogFactory.getLog(EmailManager.class);

    /**
     * This method configures the needed parameters to configure the email to be sent to the tenant.
     *
     * @param errorMessage error message
     * @param deleteObject delete object
     */
    public void sendDeletionErrorEmail(String errorMessage, DeleteJob deleteObject, String serverKey) {
        FileContentReader reader = new FileContentReader();
        String carbonHome = CarbonUtils.getCarbonHome() + File.separator;
        String emailFile = carbonHome + DeletionConstants.TENANT_DELETION_ERROR_EMAIL_FILE_PATH;
        ConfigReader configReader = ConfigReader.getInstance();
        String recipientEmail = configReader.getConfiguration().getEmailProperties().getRecipientEmail();
        String emailSubject = DeletionConstants.DELETION_ERROR_MAIL_SUBJECT;
        String emailMessage =
                replaceValuesOfDeletionErrorEmailContent(reader.fileReader(emailFile), errorMessage, deleteObject,
                                                         serverKey);
        sendEmail(recipientEmail, emailSubject, emailMessage);

        LOG.info("Sent email to notify deletion Error to " + recipientEmail);
    }

    /**
     * This method configures the needed parameters to configure the email to be sent to the tenant.
     *
     * @param deletedTenantMap conf map of deleted tenants
     */
    public void configureDeletionCompleteEmail(Map<String, Integer> deletedTenantMap) {
        FileContentReader reader = new FileContentReader();
        String carbonHome = CarbonUtils.getCarbonHome() + File.separator;
        String emailFile = carbonHome + DeletionConstants.TENANT_DELETION_COMPLETE_EMAIL_FILE_PATH;
        ConfigReader configReader = ConfigReader.getInstance();
        String recipientEmail = configReader.getConfiguration().getEmailProperties().getRecipientEmail();
        String emailSubject = DeletionConstants.DELETION_COMPLETE_MAIL_SUBJECT;
        String emailMessage =
                replaceValuesOfDeletionCompleteEmailContent(reader.fileReader(emailFile), deletedTenantMap);
        sendEmail(recipientEmail, emailSubject, emailMessage);

        LOG.info("Sent email to notify that Deletion is complete to " + recipientEmail);
    }

    public void sendEmail(String recepient, String subject, String messageBody) {

        ConfigReader configReader = ConfigReader.getInstance();
        EmailPropertiesType emailProperties = configReader.getConfiguration().getEmailProperties();

        //Retrieving the mail properties
        String port = emailProperties.getPort();
        String host = emailProperties.getHost();
        final String userName = emailProperties.getUserName();
        final String emailAddress = emailProperties.getSenderEmail();
        final String emailPassword = emailProperties.getSenderPassword();

        //Setting the email properties
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        Session session = Session.getInstance(properties, new MailAuthenticator(userName, emailPassword));
        try {
            //constructing the email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailAddress));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recepient));
            message.setSubject(subject);
            message.setContent(messageBody, "text/html");
            //Checking if the email contents is empty or not
            if (" ".equalsIgnoreCase(messageBody)) {
                String errorMessage = "Error sending email. Email content is empty";
                LOG.error(errorMessage);
            } else {
                message.saveChanges();
                Transport.send(message);
            }
        } catch (MessagingException e) {
            String errorMessage = "Error sending email to " + recepient + "";
            LOG.error(errorMessage, e);
        }
    }

    /**
     * This method replaces the placeholders in the email content.
     *
     * @param textToReplace is the email content containing the placeholders
     * @param deleteObject  delete object which contain class name, server keys and dependencies
     * @return the replaced email content
     */
    private String replaceValuesOfDeletionErrorEmailContent(String textToReplace, String exception,
                                                            DeleteJob deleteObject, String serverKey) {
        Map<String, String> emailPropertyMap = new HashMap<>();
        emailPropertyMap.put("%%deletionType", deleteObject.getClassName());
        emailPropertyMap.put("%%server", serverKey);
        emailPropertyMap.put("%%error", exception);

        String key;
        String value;
        String messageBody = textToReplace;
        for (Map.Entry<String, String> entry : emailPropertyMap.entrySet()) {
            value = entry.getValue();
            key = entry.getKey();
            if (value != null) {
                messageBody = messageBody.replaceAll(key, value);
            }
        }
        return messageBody;
    }

    /**
     * This method replaces the placeholders in the email content.
     *
     * @param textToReplace textToReplace is the email content containing the placeholders
     * @return the replaced email content
     */
    private String replaceValuesOfDeletionCompleteEmailContent(String textToReplace,
                                                               Map<String, Integer> deletedTenantMap) {
        List<DeletedTenant> deletedTenants = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : deletedTenantMap.entrySet()) {
            String tenantDomain = entry.getKey();
            //Gets an DeletedTenant object which contain deleted flag status
            DeletedTenant tenantObj = DataAccessManager.getInstance().getDeletedTenants(tenantDomain);
            deletedTenants.add(tenantObj);
        }
        int count = 1;
        String messageBody = textToReplace;
        //replaces email template with appropriate strings for each deleted tenants
        for (DeletedTenant tenant : deletedTenants) {
            String tenantDomain = tenant.getTenantDomain();
            String app = Integer.toString(tenant.getAppFlag());
            String api = Integer.toString(tenant.getApiFlag());
            String configPubstore = Integer.toString(tenant.getConfigPubstore());
            String configBps = Integer.toString(tenant.getConfigBps());
            String configCloudMgt = Integer.toString(tenant.getConfigCloudMgt());
            String configIs = Integer.toString(tenant.getConfigIs());
            String configSs = Integer.toString(tenant.getConfigSs());
            String configDas = Integer.toString(tenant.getConfigDas());
            String configAf = Integer.toString(tenant.getConfigAf());
            String governance = Integer.toString(tenant.getGovernanceFlag());
            String userMgt = Integer.toString(tenant.getUserMgtFlag());
            String cloudMgt = Integer.toString(tenant.getCloudMgtFlag());
            String ldap = Integer.toString(tenant.getLdapFlag());
            //replace text
            if (tenantDomain != null) {
                messageBody = messageBody.replaceAll("%%tenantDomain", tenantDomain);
                messageBody = messageBody.replaceAll("%%APP", app);
                messageBody = messageBody.replaceAll("%%API", api);
                messageBody = messageBody.replaceAll("%%CONFIG_PUBSTORE", configPubstore);
                messageBody = messageBody.replaceAll("%%CONFIG_BPS", configBps);
                messageBody = messageBody.replaceAll("%%CONFIG_CLOUD_MGT", configCloudMgt);
                messageBody = messageBody.replaceAll("%%CONFIG_IS", configIs);
                messageBody = messageBody.replaceAll("%%CONFIG_SS", configSs);
                messageBody = messageBody.replaceAll("%%CONFIG_DAS", configDas);
                messageBody = messageBody.replaceAll("%%CONFIG_AF", configAf);
                messageBody = messageBody.replaceAll("%%GOVERNANCE", governance);
                messageBody = messageBody.replaceAll("%%USER_MGT", userMgt);
                messageBody = messageBody.replaceAll("%%CLOUD_MGT", cloudMgt);
                messageBody = messageBody.replaceAll("%%LDAP", ldap);
            }
            String newRow =
                    "<tr>\n" + "<td align=\"center\";>%%tenantDomain</td>\n" + "<td align=\"center\";>%%APP</td>\n" +
                    "<td align=\"center\";>%%API</td>\n" + "<td align=\"center\";>%%CONFIG_PUBSTORE</td>\n" +
                    "<td align=\"center\";>%%CONFIG_BPS</td>\n" + "<td align=\"center\";>%%CONFIG_CLOUD_MGT</td>\n" +
                    "<td align=\"center\";>%%CONFIG_IS</td>\n" + "<td align=\"center\";>%%CONFIG_SS</td>\n" +
                    "<td align=\"center\";>%%CONFIG_DAS</td>\n" + "<td align=\"center\";>%%CONFIG_AF</td>\n" +
                    "<td align=\"center\";>%%GOVERNANCE</td>\n" + "<td align=\"center\";>%%USER_MGT</td>\n" +
                    "<td align=\"center\";>%%CLOUD_MGT</td>\n" + "<td align=\"center\";>%%LDAP</td>\n" + "</tr> \n" +
                    "%%nextRow";
            //After replacing one tenant details another row will be created for next tenant details except for last
            // tenant
            if (count != deletedTenants.size()) {
                messageBody = messageBody.replaceAll("%%nextRow", newRow);
                //After replacing last tenant details %%nextRow tag will be removed
            } else if (count == deletedTenants.size()) {
                messageBody = messageBody.replaceAll("%%nextRow", "");
            }
            count++;
        }
        return messageBody;
    }
}
