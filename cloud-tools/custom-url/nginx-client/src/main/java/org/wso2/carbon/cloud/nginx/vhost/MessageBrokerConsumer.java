/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.cloud.nginx.vhost;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.cloud.nginx.vhost.conf.ConfigReader;
import org.wso2.carbon.cloud.nginx.vhost.modules.VHostEntry;
import org.wso2.carbon.cloud.nginx.vhost.util.DomainMapperException;
import org.wso2.carbon.cloud.nginx.vhost.util.RegistryManager;
import org.wso2.carbon.cloud.nginx.vhost.util.SSLFileHandler;
import org.wso2.carbon.cloud.nginx.vhost.util.TemplateManager;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

/**
 * Message Consumer for the topics published to message brokers
 */
public class MessageBrokerConsumer implements MessageListener {

    private static final Log log = LogFactory.getLog(MessageBrokerConsumer.class);
    private static final String UPDATE_STATUS = "update";
    private static final String STORE = "store";
    private static final String GATEWAY = "gateway";
    private static volatile Connection connection;
    private VHostManager vHostManager;
    private TemplateManager templateManager;
    private ConfigReader configReader;
    private String messageBrokerTopicName;

    public MessageBrokerConsumer(VHostManager vHostManager, TemplateManager templateManager, ConfigReader configReader)
            throws JMSException {
        this.vHostManager = vHostManager;
        this.templateManager = templateManager;
        this.configReader = configReader;
        this.messageBrokerTopicName = configReader.getProperty("messageBrokerTopicName");
        try {
            setConnection(configReader.getProperty("messageBrokerUrl"));
            init();
        } catch (JMSException ex) {
            String errorMessage = "Error occurred when initializing Message Consumer";
            log.error(errorMessage, ex);
            throw ex;
        }
    }

    /**
     * Creating the MessageBroker connection
     *
     * @param url URL of ActiveMQ message broker
     */
    private static void setConnection(String url) throws JMSException {
        synchronized (MessageBrokerConsumer.class) {
            if (connection == null) {
                try {
                    ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
                    connection = connectionFactory.createConnection();
                    connection.start();
                } catch (JMSException e) {
                    String errorMessage = "";
                    log.error("Error occurred when establishing the connection.", e);
                    throw new JMSException(errorMessage);
                }
            }
        }
    }

    /**
     * Initializing the required components to consume messages
     */
    public void init() throws JMSException {
        try {
            Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            Topic topic = session.createTopic(messageBrokerTopicName);
            MessageConsumer messageConsumer = session.createConsumer(topic);
            messageConsumer.setMessageListener(this);
        } catch (JMSException e) {
            String errorMessage = "Error occurred when establishing session.";
            log.error(errorMessage, e);
            throw new JMSException(errorMessage);
        }
    }

    /**
     * Listener method which will invoke when a topic is published.
     *
     * @param message Published message to the topic.
     */
    @Override public void onMessage(Message message) {
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;

            try {
                RegistryManager registryManager =
                        new RegistryManager(configReader, NginxVhostConstants.AXIS2_CONF_FILE_PATH);
                JSONObject jsonObject = new JSONObject(textMessage.getText());
                if (jsonObject.getString(NginxVhostConstants.CLOUD_TYPE).equals(NginxVhostConstants.API_CLOUD_TYPE)) {
                    String status = jsonObject.getString(NginxVhostConstants.PAYLOAD_STATUS);
                    String node = jsonObject.getString(NginxVhostConstants.PAYLOAD_NODE);

                    log.info("Custom Url-mapping request received : " + jsonObject.toString());
                    SSLFileHandler sslFileHandler = new SSLFileHandler(registryManager, configReader);
                    if (UPDATE_STATUS.equals(status)) {
                        vHostManager.removeHostMapping(jsonObject.getString(NginxVhostConstants.PAYLOAD_TENANT_DOMAIN),
                                                       jsonObject.getString(NginxVhostConstants.CLOUD_TYPE), node);
                        sslFileHandler.removeSecurityFilesFromLocal(
                                jsonObject.getString(NginxVhostConstants.PAYLOAD_TENANT_DOMAIN), node);
                    }

                    File securityCertificate = sslFileHandler.storeFileInLocal(NginxVhostConstants.CERTIFICATE_FILE,
                                                                               jsonObject.getString(
                                                                                       NginxVhostConstants
                                                                                               .PAYLOAD_TENANT_DOMAIN),
                                                                               node);
                    File securityPrivateKey = sslFileHandler.storeFileInLocal(NginxVhostConstants.KEY_FILE, jsonObject
                            .getString(NginxVhostConstants.PAYLOAD_TENANT_DOMAIN), node);
                    String cloudType = jsonObject.getString(NginxVhostConstants.PAYLOAD_CLOUD_TYPE);
                    String tenantDomain = jsonObject.getString(NginxVhostConstants.PAYLOAD_TENANT_DOMAIN);
                    String customUrl = jsonObject.getString(NginxVhostConstants.PAYLOAD_CUSTOM_URL);
                    String template;

                    //Creating store
                    if (STORE.equals(node)) {
                        template = templateManager.getTemplate(NginxVhostConstants.API_STORE_TEMPLATE_NAME);
                        VHostEntry storeEntry = vHostManager.buildVhostEntry(cloudType, tenantDomain, customUrl,
                                                                             securityCertificate.getAbsolutePath(),
                                                                             securityPrivateKey.getAbsolutePath(),
                                                                             template);
                        vHostManager.addHostToNginxConfig(storeEntry,
                                                          configReader.getProperty("nginx.api.store.config.path"));
                    } else if (GATEWAY.equals(node)) {
                        //Creating gateway
                        template = templateManager.getTemplate(NginxVhostConstants.HTTP_API_GATEWAY_TEMPLATE_NAME);
                        VHostEntry gatewayEntry =
                                vHostManager.buildVhostEntry(cloudType, tenantDomain, customUrl, "", "", template);

                        //Creating gateway https
                        template = templateManager.getTemplate(NginxVhostConstants.HTTPS_API_GATEWAY_TEMPLATE_NAME);
                        VHostEntry gatewayHttpsEntry = vHostManager.buildVhostEntry(cloudType, tenantDomain, customUrl,
                                                                                    securityCertificate
                                                                                            .getAbsolutePath(),
                                                                                    securityPrivateKey
                                                                                            .getAbsolutePath(),
                                                                                    template);

                        vHostManager.addHostToNginxConfig(gatewayEntry,
                                                          configReader.getProperty("nginx.api.gateway.config.path"));
                        vHostManager.addHostToNginxConfig(gatewayHttpsEntry, configReader
                                .getProperty("nginx.api.gateway.https.config.path"));
                    }

                    vHostManager.restartNginX();

                }
            } catch (DomainMapperException ex) {
                String errorMessage = "Error occurred while retrieving file from registry";
                log.error(errorMessage, ex);
            } catch (JSONException ex) {
                String errorMessage = "Error occurred when parsing json object.";
                log.error(errorMessage, ex);
            } catch (IOException ex) {
                String errorMessage = "Error occurred when adding nginx configuration.";
                log.error(errorMessage, ex);
            } catch (RegistryException ex) {
                String errorMessage = "Error occurred when accessing ssl files on registry.";
                log.error(errorMessage, ex);
            } catch (KeyStoreException ex) {
                String errorMessage = "Error occurred when decrypting ssl files.";
                log.error(errorMessage, ex);
            } catch (JMSException ex) {
                String errorMessage = "Error occurred when parsing consumed message from the topic ";
                log.error(errorMessage, ex);
            } catch (NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException |
                    NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException |
                    InvalidAlgorithmParameterException | InterruptedException ex) {
                String errorMessage = "Error occurred when storing ssl files";
                log.error(errorMessage, ex);
            }
        }
    }
}
