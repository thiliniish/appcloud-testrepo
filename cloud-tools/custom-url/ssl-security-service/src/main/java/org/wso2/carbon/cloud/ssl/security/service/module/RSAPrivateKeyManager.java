/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.cloud.ssl.security.service.module;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.openssl.PEMReader;
import org.wso2.carbon.cloud.ssl.security.service.exceptions.SSLSecurityServiceException;

import java.io.IOException;
import java.io.StringReader;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;

/**
 * Class to manage RSA private key
 */
public class RSAPrivateKeyManager {
    private static final Log log = LogFactory.getLog(RSAPrivateKeyManager.class);
    private RSAPrivateKey privateKey;

    public RSAPrivateKeyManager(String keyContent) throws IOException, SSLSecurityServiceException {
        this.initRSAPrivateKey(keyContent);
    }

    private void initRSAPrivateKey(String keyContent) throws IOException, SSLSecurityServiceException {
        PEMReader pemReader = new PEMReader(new StringReader(keyContent));
        try {
            Object readerObject = pemReader.readObject();
            if (readerObject instanceof RSAPrivateKey) {  //PKCS#8 format
                privateKey = (RSAPrivateKey) readerObject;
            } else if (readerObject instanceof KeyPair) {  //PKCS#1 format
                Object keyObject = ((KeyPair) readerObject).getPrivate();
                if (keyObject instanceof RSAPrivateKey) {
                    privateKey = (RSAPrivateKey) keyObject;
                } else {
                    String message = "​​Provided private key is not supported in current environment. " +
                            "Supported formats : RSA PKCS1 and PKCS8.​";
                    throw new SSLSecurityServiceException(message);
                }
            } else {
                String message = "​​Provided private key is not supported in current environment. " +
                        "Supported formats : RSA PKCS1 and PKCS8.​";
                throw new SSLSecurityServiceException(message);
            }
        } catch (IOException ex) {
            String errorMessage = "IOException is thrown when reading the file content by the pemReader";
            log.error(errorMessage, ex);
            throw new IOException(errorMessage, ex);
        }
    }

    /**
     * Returns the private key.
     *
     * @return RSA Private Key
     */
    public RSAPrivateKey getPrivateKey() {
        return this.privateKey;
    }

}
