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

package org.wso2.carbon.cloud.ssl.securitycomponent.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.ssl.securitycomponent.Constants;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.*;

/**
 * AES Cipher. Encrypt the given content to
 */
public class AESCipher {
	private SecretKeySpec secretKeySpec;
	private Cipher cipher;
	private IvParameterSpec ivParameterSpec;
	private Log log = LogFactory.getLog(AESCipher.class);

	public AESCipher(Key key) throws NoSuchAlgorithmException, NoSuchPaddingException {
		try {
			this.secretKeySpec = new SecretKeySpec(key.getEncoded(), Constants.SECRET_KEY_SPEC_ALGORITHM);
			//Generating random iv(indexing vector)
			SecureRandom secureRandom = new SecureRandom();
			byte[] iv = new byte[16];
			secureRandom.nextBytes(iv);
			this.ivParameterSpec = new IvParameterSpec(iv);
			this.cipher = Cipher.getInstance(Constants.AES256_ALGORITHM);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			String errorMessage =
					"Error occurred while initializing the cipher.Provided algorithm or padding mechanism" +
					" is not supported by the environment. Provide algorithm/padding is : " +
					Constants.AES256_ALGORITHM;
			log.error(errorMessage, e);
			throw e;
		}
	}

	public byte[] getIv() {
		return ivParameterSpec.getIV();
	}

	/**
	 * Get cipher for encrypt.
	 *
	 * @param ivParameterSpec Indexing vector parameterspec object.
	 * @return Cipher object to encryption
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 */
	private Cipher getCipher(IvParameterSpec ivParameterSpec)
			throws InvalidKeyException, InvalidAlgorithmParameterException {
		try {
			cipher.init(Cipher.ENCRYPT_MODE, getSecretKeySpec(), ivParameterSpec);
			return cipher;
		} catch (InvalidKeyException e) {
			String errorMessage = "Error occurred while getting cipher. Provided key is invalid : ";
			log.error(errorMessage, e);
			throw new InvalidKeyException(errorMessage);
		} catch (InvalidAlgorithmParameterException e) {
			String errorMessage = "Error occurred while getting cipher.";
			log.error(errorMessage, e);
			throw new InvalidAlgorithmParameterException(errorMessage);
		}
	}

	private SecretKeySpec getSecretKeySpec() {
		return secretKeySpec;
	}

	/**
	 * This method will encrypt the given text
	 *
	 * @param message Text that need to be encrypt
	 * @return Encrypted text
	 * @throws InvalidAlgorithmParameterException
	 * @throws InvalidKeyException
	 * @throws UnsupportedEncodingException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public String getEncryptedMessage(String message)
			throws InvalidAlgorithmParameterException, InvalidKeyException, UnsupportedEncodingException,
			       IllegalBlockSizeException, BadPaddingException {
		try {
			Cipher cipher = getCipher(ivParameterSpec);
			byte[] encryptedTextBytes = cipher.doFinal(message.getBytes(Constants.ENCODING_MECHANISM));
			return new String(new Base64().encode(encryptedTextBytes));
		} catch (UnsupportedEncodingException e) {
			String errorMessage = "Error occurred when encrypting the data. Provided encoding mechanism " +
			                      Constants.ENCODING_MECHANISM + " is not supporting in the environment.";
			log.error(errorMessage, e);
			throw new UnsupportedEncodingException(errorMessage);
		} catch (IllegalBlockSizeException e) {
			String errorMessage = "Error occurred when encrypting the data.  Provided data does not match the block " +
			                      "size of the cipher. Block size of the cipher  " + cipher.getBlockSize();
			log.error(errorMessage, e);
			throw new IllegalBlockSizeException(errorMessage);

		} catch (BadPaddingException e) {
			String errorMessage =
					"Error occurred when encrypting the data. The data is not padded properly to expected " +
					"padding mechanism. ";
			log.error(errorMessage, e);
			throw new BadPaddingException(errorMessage);
		}
	}
}
