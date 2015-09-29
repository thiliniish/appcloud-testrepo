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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONException;
import org.wso2.carbon.cloud.nginx.vhost.conf.ConfigReader;
import org.wso2.carbon.cloud.nginx.vhost.util.TemplateManager;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.jms.JMSException;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

public class Agent {
    private static final String SETUP_MODE = "-Dsetup";
    private static Log log = LogFactory.getLog(Agent.class);

    private Agent() {
    }

    public static void main(String[] args) {
        initialize();
        try {
            TemplateManager templateManager = new TemplateManager();
            PropertyConfigurator.configure(NginxVhostConstants.LOG4J_PROPERTY_PATH);
            ConfigReader configReader = new ConfigReader();
            VHostManager vHostManager = new VHostManager(configReader, templateManager);

            if (args.length > 0 && SETUP_MODE.equals(args[0])) {
                    vHostManager.restoreVirtualHosts();
            }

            new MessageBrokerConsumer(vHostManager, templateManager, configReader);

        } catch (IOException | RegistryException e) {
            String errorMessage = "Error occurred when starting the Domain-Mapping agent";
            log.error(errorMessage, e);
            System.exit(1);
        } catch (JSONException | KeyStoreException | NoSuchAlgorithmException | CertificateException |
                UnrecoverableKeyException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException |
                InvalidKeyException | InvalidAlgorithmParameterException | InterruptedException ex) {
            String errorMessage = "Error occurred while restoring virtual hosts";
            log.error(errorMessage, ex);
            System.exit(1);
        } catch (JMSException e) {
            String errorMessage = "Error occurred when initializing Message consumer";
            log.error(errorMessage, e);
            System.exit(1);
        }

    }

    public static void initialize() {
        System.setProperty("javax.net.ssl.trustStore", NginxVhostConstants.KEY_STORE_FILE_PATH);
        System.setProperty("javax.net.ssl.keyStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.keyStoreType", "JKS");
        System.setProperty("carbon.repo.write.mode", "true");
    }

}
