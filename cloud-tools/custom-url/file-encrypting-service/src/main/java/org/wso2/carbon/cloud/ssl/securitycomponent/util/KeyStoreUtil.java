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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;

public class KeyStoreUtil {
	private static final String KEYSTORE_INSTANCE = "JCEKS";
	private static Log log = LogFactory.getLog(KeyStoreUtil.class);
	private static Key key;

	private KeyStoreUtil() {
	}

	/**
	 * Get key from the given key-store.
	 *
	 * @param keyStoreLocation Location of key-store
	 * @param keyStorePass     Password of the key-store
	 * @param alias            Alias name of key-store
	 * @param keyPass          Password of key
	 * @throws IOException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws UnrecoverableKeyException
	 */
	public static void getKeyFromStore(final String keyStoreLocation, final String keyStorePass, final String alias,
	                                   final String keyPass)
			throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException,
			       UnrecoverableKeyException {
		InputStream keyStoreStream = null;

		try {
			keyStoreStream = new FileInputStream(keyStoreLocation);
			KeyStore keystore = KeyStore.getInstance(KEYSTORE_INSTANCE);

			keystore.load(keyStoreStream, keyStorePass.toCharArray());

			key = keystore.getKey(alias, keyPass.toCharArray());

		} catch (FileNotFoundException e) {
			String errorMessage = "Key store file cannot be located in " + keyStoreLocation;
			log.error(errorMessage, e);
			throw new FileNotFoundException(errorMessage);
		} catch (KeyStoreException | IOException e) {
			String errorMessage = "Error occurred while loading the key-store";
			log.error(errorMessage, e);
			throw e;
		} catch (NoSuchAlgorithmException e) {
			String errorMessage = "Specified algorithm is not supported in this environment." +
			                      " Provided algorithm is : " + KEYSTORE_INSTANCE;
			log.error(errorMessage, e);
			throw new NoSuchAlgorithmException(errorMessage);
		} catch (CertificateException e) {
			String errorMessage = "Error occurred while loading the key-store.";
			log.error(errorMessage, e);
			throw new CertificateException(errorMessage);
		} catch (UnrecoverableKeyException e) {
			String errorMessage = "Error when obtaining the key from the key-store.";
			log.error(errorMessage, e);
			throw new UnrecoverableKeyException(errorMessage);
		} finally {
			if (keyStoreStream != null) {
				keyStoreStream.close();
			}
		}
	}

	public static Key getKey() {
		if (key != null) {
			return key;
		} else {
			return null;
		}
	}
}
