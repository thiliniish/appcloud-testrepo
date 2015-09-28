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
package org.wso2.carbon.cloud.nginx.vhost.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sun.misc.BASE64Decoder;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class AESCipher {
    private static final String ALGORITHM_AES256 = "AES/CBC/PKCS5Padding";

    private SecretKeySpec secretKeySpec;
    private Cipher cipher;
    private Log log = LogFactory.getLog(AESCipher.class);

    public AESCipher(Key key) throws NoSuchAlgorithmException, NoSuchPaddingException {
        try {
            this.secretKeySpec = new SecretKeySpec(key.getEncoded(), "AES");
            this.cipher = Cipher.getInstance(ALGORITHM_AES256);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            String errorMessage =
                    "Error occurred when initializing AES Cipher. Provided algorithm " + ALGORITHM_AES256 +
                    " is not supporting in current environment";
            log.error(errorMessage, e);
            throw e;
        }
    }

    public String getDecryptedMessage(String message, byte[] ivArray)
            throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException,
                   InvalidAlgorithmParameterException, IOException {
        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivArray);
            cipher.init(Cipher.DECRYPT_MODE, this.secretKeySpec, ivParameterSpec);
            byte[] encryptedTextBytes = new BASE64Decoder().decodeBuffer(message);
            byte[] decryptedTextBytes = cipher.doFinal(encryptedTextBytes);

            return new String(decryptedTextBytes);
        } catch (IllegalBlockSizeException e) {
            String errorMessage = "Error occurred when decrypting the encrypted text. Provided encrypted text " +
                                  "does not match the block size of the cipher. : " + e.getMessage();
            log.error(errorMessage, e);
            throw e;
        } catch (BadPaddingException e) {
            String errorMessage = "Error occurred when decrypting the encrypted text. Provided encrypted text " +
                                  "does not match the block size of the cipher. : " + e.getMessage();
            log.error(errorMessage);
            throw e;
        } catch (InvalidKeyException e) {
            String errorMessage =
                    "Error occurred when decrypting the encrypted text. Provided key is invalid. " + e.getMessage();
            log.error(errorMessage, e);
            throw e;
        } catch (InvalidAlgorithmParameterException e) {
            String errorMessage = "Error occurred when initializing the cipher. " + e.getMessage();
            log.error(errorMessage);
            throw e;
        } catch (IOException e) {
            String errorMessage = "Error occurred when decoding encoded test while decrypting encrypted ssl files : " +
                                  e.getMessage();
            log.error(errorMessage, e);
            throw new IOException(errorMessage, e);
        }
    }
}
