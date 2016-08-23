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
package org.wso2.carbon.cloud.ssl.security.service.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cloud.ssl.security.service.SSLFileAnalyzer;
import org.wso2.carbon.cloud.ssl.security.service.SSLFileEncryptService;
import org.wso2.carbon.cloud.ssl.security.service.util.ConfigReader;
import org.wso2.carbon.cloud.ssl.security.service.util.KeyStoreUtil;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * @scr.component name="org.wso2.carbon.cloud.ssl.security.service"
 * immediate="true"
 */
public class SSLSecurityServiceComponent {

    private static Log log = LogFactory.getLog(SSLSecurityServiceComponent.class);
    private ServiceRegistration sslFileAnalyzerService;
    private ServiceRegistration sslFileEncryptorService;

    protected void activate(ComponentContext context) {
        BundleContext bundleContext = context.getBundleContext();
        try {
            ConfigReader configReader = new ConfigReader();
            String keyStoreLocation = configReader.getProperty("keyStore.location");
            String keyStorePassword = configReader.getProperty("keyStore.password");
            String alias = configReader.getProperty("keyStore.alias");
            String keyPass = configReader.getProperty("keyStore.keyPass");
            KeyStoreUtil.getKeyFromStore(keyStoreLocation, keyStorePassword, alias, keyPass);

            sslFileAnalyzerService =
                    bundleContext.registerService(SSLFileAnalyzer.class.getName(), new SSLFileAnalyzer(), null);
            sslFileEncryptorService = bundleContext
                    .registerService(SSLFileEncryptService.class.getName(), new SSLFileEncryptService(), null);
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | UnrecoverableKeyException |
                IOException e) {
            String errorMessage = "Error occurred when obtaining key from keystore.";
            log.error(errorMessage, e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Activating SSL Security Service component");
        }
    }

    protected void deactivate() {
        sslFileAnalyzerService.unregister();
        sslFileEncryptorService.unregister();
        if (log.isDebugEnabled()) {
            log.debug("Deactivating SSL Security Service component");
        }
    }

}
