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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.nginx.vhost.NginxVhostConstants;
import org.wso2.carbon.cloud.nginx.vhost.conf.ConfigReader;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

public class SSLFileHandler {

	Log log = LogFactory.getLog(SSLFileHandler.class);
	private RegistryManager registryManager;
	private ConfigReader configReader;
	private byte[] indexingVectorArray;

	public SSLFileHandler(RegistryManager registryManager, ConfigReader configReader) {
		this.registryManager = registryManager;
		this.configReader = configReader;
	}

	private void writeFile(String filePath, String fileContent) throws IOException {
		try {
			FileWriter writer = new FileWriter(filePath);
			writer.write(fileContent);
			writer.close();

		} catch (IOException ex) {
			String errorMessage = "Error occurred when writing ssl files in local environment: " + ex.getMessage();
			log.error(errorMessage, ex);
			throw new IOException(errorMessage, ex);
		}
	}

	public File storeFileInLocal(String fileType, String tenantDomain, String type)
			throws IOException, RegistryException, KeyStoreException, NoSuchAlgorithmException, CertificateException,
			       UnrecoverableKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
			       InvalidKeyException, InvalidAlgorithmParameterException, InterruptedException,
			       DomainMapperException {

		String defaultRegistryLocation =
				configReader.getProperty("remoteregistry.path") + "api-cloud/" + tenantDomain + "/" +
				"securityCertificates/" + type + "/" + tenantDomain + "-" + type;

		String defaultFilePath =
				configReader.getProperty("api_cloud_security_certificate_file_location") + "/" + tenantDomain +
				"/" + type + "/SSL-File/" + tenantDomain + "-" + type;

		String filePath;
		String registryLocation;
		String fileContent;

		String indexingVectorFilePath = defaultRegistryLocation + ".iv";

		try {
			byte[] byteContentOfIVResource = this.retrieveFileFromRegistry(indexingVectorFilePath);
			indexingVectorArray = new Base64().decode(byteContentOfIVResource);
			if (NginxVhostConstants.CERTIFICATE_FILE.equals(fileType)) {
				filePath = defaultFilePath + "-certificate.pem";
				String publicKeyFileLocation = defaultRegistryLocation + ".pub";
				registryLocation = defaultRegistryLocation + ".pem";

				String sslFileContent = getDecryptContent(new String(this.retrieveFileFromRegistry(registryLocation)));
				String sslPublicKeyContent =
						getDecryptContent(new String(this.retrieveFileFromRegistry(publicKeyFileLocation)));
				//Appending chain file content to ssl file
				fileContent = sslFileContent + "\n" + sslPublicKeyContent;

			} else if (NginxVhostConstants.KEY_FILE.equals(fileType)) {
				registryLocation = defaultRegistryLocation + ".key";
				filePath = defaultFilePath + ".key";
				fileContent = getDecryptContent(new String(this.retrieveFileFromRegistry(registryLocation)));

			} else {
				log.error("fileType should be cert or key. Provided is : " + fileType);
				throw new IllegalArgumentException("fileType should be cert or key. Provided is : " + fileType);
			}

			File file = new File(filePath);
			file.getParentFile().mkdirs();

			Thread.sleep(Long.parseLong(configReader.getProperty("sslFileHandler.filecreation.sleeptime")));
			this.writeFile(file.getAbsolutePath(), fileContent);

			return file;
		} catch (DomainMapperException ex){
			String errorMessage = "Error occurred while retrieving file from registry ";
			log.error(errorMessage, ex);
			throw new DomainMapperException(errorMessage);
		} catch (RegistryException ex) {
			String errorMessage = "Error occurred when retrieving ssl files from registry";
			log.error(errorMessage);
			throw new RegistryException(errorMessage);
		} catch (IOException ex) {
			String errorMessage = "Error is thrown when storing ssl file in local";
			log.error(errorMessage);
			throw new IOException(errorMessage);
		} catch (InterruptedException ex) {
			throw ex;
		} catch (NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException | NoSuchPaddingException |
				IllegalBlockSizeException | BadPaddingException | InvalidKeyException |
				InvalidAlgorithmParameterException e) {
			String errorMessage = "Error occurred when getting decrypted content";
			log.error(errorMessage, e);
			throw e;
		}
	}

	public void removeSecurityFilesFromLocal(String tenantDomain, String node) throws IOException {
		String filePath = configReader.getProperty("api_cloud_security_certificate_file_location") + "/" +
		                  tenantDomain + "/" + node;
		File file = new File(filePath);
		if (file.exists()) {
			FileUtils.deleteDirectory(file);
		}
	}

	public String getDecryptContent(String fileContent)
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException,
			       UnrecoverableKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
			       InvalidKeyException, InvalidAlgorithmParameterException {
		try {
			Key key = KeyStoreUtil.getKeyFromStore(configReader.getProperty("keystore.location"),
			                                       configReader.getProperty("keystore.password"),
			                                       configReader.getProperty("keystore.alias"),
			                                       configReader.getProperty("keystore.key.password"));
			AESCipher aesCipher = new AESCipher(key);
			return aesCipher.getDecryptedMessage(fileContent, indexingVectorArray);
		} catch (KeyStoreException e) {
			String errorMessage = "Error occurred when getting key from the key store.";
			log.error(errorMessage, e);
			throw e;
		} catch (NoSuchAlgorithmException | CertificateException | IOException | UnrecoverableKeyException e) {
			String errorMessage = "Error occurred when obtaining key from the keystore.";
			log.error(errorMessage, e);
			throw e;
		} catch (NoSuchPaddingException e) {
			String errorMessage = "Error occurred when initializing cipher object";
			log.error(errorMessage, e);
			throw e;
		} catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException |
				InvalidAlgorithmParameterException e) {
			String errorMessage = "Error occurred when getting decrypted content.";
			log.error(errorMessage, e);
			throw e;
		}
	}

	protected byte[] getFileFromRegistry(String filePath) throws RegistryException {
		try {
			return (byte[]) registryManager.getResourceFromRegistry(filePath).getContent();
		} catch (RegistryException ex) {
			String errorMessage = "Error occurred when getting ssl file from " + filePath + " in registry.";
			log.error(errorMessage, ex);
			throw new RegistryException(errorMessage, ex);
		}
	}

	protected boolean isFileAvailableInRegistry(String registryPath) throws RegistryException {
		try {
			return registryManager.resourceExists(registryPath);
		} catch (RegistryException ex) {
			String errorMessage = "Error occurred while checking existance of resource in " + registryPath;
			log.error(errorMessage, ex);
			throw new RegistryException(errorMessage, ex);
		}
	}

	protected byte[] retrieveFileFromRegistry(String registryPath) throws RegistryException, DomainMapperException {
		int retryCount = 0;
		try {
			while (!this.isFileAvailableInRegistry(registryPath) && retryCount < 3) {
				try {
					//Main thread will get sleeps to pause until resources available in registry
					Thread.sleep(Long.parseLong(configReader.getProperty("messagebroker.sleeptime")));
					retryCount++;
				} catch (InterruptedException ex) {

				}
			}

			if (this.isFileAvailableInRegistry(registryPath)) {
				return this.getFileFromRegistry(registryPath);
			} else {
				String errorMessage = "Requested resource is not available in " + registryPath;
				log.error(errorMessage);
				throw new DomainMapperException(errorMessage);
			}
		} catch (RegistryException ex) {
			String errorMessage = "Error occurred when accessing registry location " + registryPath;
			log.error(errorMessage, ex);
			throw new RegistryException(errorMessage, ex);
		}
	}

}
