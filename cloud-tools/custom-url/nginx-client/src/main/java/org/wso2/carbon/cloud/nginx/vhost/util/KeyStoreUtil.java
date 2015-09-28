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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;

public class KeyStoreUtil {
	private static final String KEY_STORE_ALGORITHM = "JCEKS";
	private static Log log = LogFactory.getLog(KeyStoreUtil.class);

	private KeyStoreUtil() {
	}

	public static Key getKeyFromStore(final String keyStoreLocation, final String keystorePass, final String alias,
	                                  final String keyPass)
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException,
			       UnrecoverableKeyException {
		try {
			InputStream keyStoreStream = new FileInputStream(keyStoreLocation);
			KeyStore keystore = KeyStore.getInstance(KEY_STORE_ALGORITHM);

			keystore.load(keyStoreStream, keystorePass.toCharArray());

			return keystore.getKey(alias, keyPass.toCharArray());
		} catch (FileNotFoundException e) {
			String errorMessage = "KeyStore file cannot be located in " + keyStoreLocation;
			log.error(errorMessage, e);
			throw new FileNotFoundException(errorMessage);
		} catch (NoSuchAlgorithmException e) {
			String errorMessage = "Provided algorithm is not supported in current environment. Provided algorithm is " +
			                      KEY_STORE_ALGORITHM;
			log.error(errorMessage, e);
			throw new NoSuchAlgorithmException(errorMessage);
		} catch (IOException | CertificateException | KeyStoreException e) {
			String errorMessage = "Error occurred when getting key from the keystore.";
			log.error(errorMessage, e);
			throw e;
		} catch (UnrecoverableKeyException e) {
			String errorMessage = "Key cannot be recovered from keystore located in " + keyStoreLocation;
			log.error(errorMessage, e);
			throw new UnrecoverableKeyException(errorMessage);
		}
	}
}
