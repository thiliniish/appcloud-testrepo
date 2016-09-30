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

package org.wso2.carbon.cloud.ssl.security.service;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.ssl.security.service.util.AESCipher;
import org.wso2.carbon.cloud.ssl.security.service.util.KeyStoreUtil;
import org.wso2.carbon.core.AbstractAdmin;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Admin Service to Encrypt SSLFiles with the given algorithm
 */
public class SSLFileEncryptService extends AbstractAdmin {

    private static Log log = LogFactory.getLog(SSLFileEncryptService.class);
    private Key key = null;
    private AESCipher aesCipher = null;

    /**
     * Initialize the  File encryption service
     *
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     */
    public void init() throws NoSuchAlgorithmException, NoSuchPaddingException {
        this.key = KeyStoreUtil.getKey();
        try {
            aesCipher = new AESCipher(key);
        } catch (NoSuchAlgorithmException ex) {
            String errorMessage = "Error occurred when initializing SSLFileEncryptService.";
            log.error(errorMessage, ex);
            throw new NoSuchAlgorithmException(errorMessage);
        } catch (NoSuchPaddingException ex) {
            String errorMessage = "Error occurred when initializing SSLFileEncryptService.";
            log.error(errorMessage, ex);
            throw new NoSuchPaddingException(errorMessage);
        }
    }

    /**
     * This method will encrypt the provide string value
     *
     * @param message text that needed to be encrypt
     * @return Encrypted Text
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws UnsupportedEncodingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public String encryptData(String message)
            throws InvalidAlgorithmParameterException, InvalidKeyException, UnsupportedEncodingException,
                   IllegalBlockSizeException, BadPaddingException {
        try {
            return aesCipher.getEncryptedMessage(message);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException |
                UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException ex) {
            String errorMessage = "Error occurred when decrypting ssl files.";
            log.error(errorMessage, ex);
            throw ex;
        }
    }

    /**
     * Get the indexing vector(IV) that used to encrypt
     *
     * @return String representation of indexing vector
     */
    public String getIndexingVector() throws UnsupportedEncodingException {
        return new String(new Base64().encode(aesCipher.getIv()), StandardCharsets.UTF_8);
    }
}
