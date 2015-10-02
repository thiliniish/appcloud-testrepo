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

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.cloud.nginx.vhost.conf.ConfigReader;
import org.wso2.carbon.cloud.nginx.vhost.modules.VHostEntry;
import org.wso2.carbon.cloud.nginx.vhost.util.RegistryManager;
import org.wso2.carbon.cloud.nginx.vhost.util.SSLFileHandler;
import org.wso2.carbon.cloud.nginx.vhost.util.TemplateManager;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * VHost Manager is used manages vhost configuration of nginx
 */
public class VHostManager {

	private static final Log log = LogFactory.getLog(VHostManager.class);
	private static final String GATEWAY_NODE = "gateway";
	private static final String STORE_NODE = "store";
	ConfigReader configReader;
	private String apiStoreVHostTemplate;
	private String apiHttpGatewayVHostTemplate;
	private String apiHttpsGatewayVHostTemplate;

	public VHostManager(ConfigReader config, TemplateManager templateManager) throws IOException {
		this.configReader = config;
		//Reading store vhost template
		apiStoreVHostTemplate = templateManager.getTemplate(NginxVhostConstants.API_STORE_TEMPLATE_NAME);
		//Reading http gateway vhost template
		apiHttpGatewayVHostTemplate = templateManager.getTemplate(NginxVhostConstants.HTTP_API_GATEWAY_TEMPLATE_NAME);
		//Reading https gateway vhost template
		apiHttpsGatewayVHostTemplate = templateManager.getTemplate(NginxVhostConstants.HTTPS_API_GATEWAY_TEMPLATE_NAME);
	}

	/**
	 * Create vhost entry
	 * @param cloudType Cloud name
	 * @param tenantDomain tenant domain
	 * @param customUrl customUrl
	 * @param sslFilePath SSL certificate file path
	 * @param privateKeyFilePath private key file path
	 * @param template template file content
	 * @return
	 */
	public VHostEntry buildVhostEntry(String cloudType, String tenantDomain, String customUrl, String sslFilePath,
	                                  String privateKeyFilePath, String template){
		VHostEntry vHostEntry = new VHostEntry();
		vHostEntry.setCloudName(cloudType);
		vHostEntry.setTenantDomain(tenantDomain);
		vHostEntry.setCustomDomain(customUrl);
		vHostEntry.setSecurityCertificateFilePath(sslFilePath);
		vHostEntry.setSecurityCertificateKeyFilePath(privateKeyFilePath);
		vHostEntry.setTemplate(template);
		return vHostEntry;
	}

	/**
	 * @param vhostEntry VHost entry
	 * @param filePath   File path for configuration file
	 * @throws IOException
	 */
	public void addHostToNginxConfig(VHostEntry vhostEntry, String filePath) throws IOException {
		String template = buildVHostConfig(vhostEntry);
		File file = new File(filePath);

		if (!file.exists()) {
			String errorMessage = "Cannot find the Nginx Configuration file for VHost in " + file.getAbsolutePath();
			log.error(errorMessage);
			throw new FileNotFoundException(errorMessage);
		}
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		try {
			fileWriter = new FileWriter(file.getAbsoluteFile(), true);
			bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(template);
		} catch (IOException e) {
			String errorMessage = "Error thrown when writing contents to VHOST file.";
			log.error(errorMessage, e);
			throw new IOException(errorMessage, e);
		} finally {
			if (bufferedWriter != null) {
				bufferedWriter.close();
			}
			if (fileWriter != null) {
				fileWriter.close();
			}
		}
		log.info("Updated Nginx config file ");
	}

	/**
	 * This method will add the vhost entries to the configuration file
	 *
	 * @param vHostEntriesList List of vhost entries that needed to be add.
	 * @param filePath         File path of the configuration file
	 * @throws IOException
	 */
	public void addHostToNginxConfig(List<VHostEntry> vHostEntriesList, String filePath) throws IOException {

		String templateStack = "";
		for (VHostEntry vhostEntry : vHostEntriesList) {
			if (vhostEntry.getCloudName().equals(NginxVhostConstants.API_CLOUD_TYPE)) {
				templateStack += buildVHostConfig(vhostEntry);
			}
		}

		File configFile = new File(filePath);
		if (!configFile.exists()) {
			log.error("Cannot find the Nginx Configuration file for VHost");
			throw new FileNotFoundException("Cannot find the Nginx Configuration file for VHost");
		}
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		try {
			fileWriter = new FileWriter(configFile.getAbsoluteFile(), true);
			bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(templateStack);
		} catch (IOException e) {
			String errorMessage = "Error occurred while writing the configuration file";
			log.error(errorMessage, e);
			throw new IOException(errorMessage, e);
		} finally {
			if (bufferedWriter != null) {
				bufferedWriter.close();
			}
			if (fileWriter != null) {
				fileWriter.close();
			}
		}
		log.info("Updated Nginx config file for all the tenants");
	}

	/**
	 * Build the vhost entry content
	 *
	 * @param vhostEntry Particular vhost bean that needed to be build
	 * @return Constructed vhost entry content
	 */
	private String buildVHostConfig(VHostEntry vhostEntry) {
		String tenantDomain = vhostEntry.getTenantDomain();
		String customDomain = vhostEntry.getCustomDomain();
		String sslCertificatePath = vhostEntry.getSecurityCertificateFilePath();
		String sslKeyFilePath = vhostEntry.getSecurityCertificateKeyFilePath();

		String template = vhostEntry.getTemplate();
		log.info("Building VHost for tenant : " + tenantDomain + " with Domain : " + customDomain);

		//Setting vales to template
		template = template.replace("${tenant-domain}", tenantDomain);
		template = template.replace("${custom-domain}", customDomain);
		if(template.contains("${ssl_certificate_path}")) {
			template = template.replace("${ssl_certificate_path}", sslCertificatePath);
			template = template.replace("${ssl_key_path}", sslKeyFilePath);
		}

		return template;
	}

	protected void removeHostMapping(String domainName, String cloudType, String node) throws IOException {
		String matchingKeyword = "## Tenant Domain: " + domainName + "".trim();
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		File file = null;

		if (NginxVhostConstants.API_CLOUD_TYPE.equals(cloudType)) {
			String[] configFileLocations;
			if (STORE_NODE.equals(node)) {
				configFileLocations = new String[] { configReader.getProperty("nginx.api.store.config.path") };
			} else {
				configFileLocations = new String[] { configReader.getProperty("nginx.api.gateway.config.path"),
				                                     configReader.getProperty("nginx.api.gateway.https.config.path") };
			}

			for (String configFileLocation : configFileLocations) {
				try {
					file = new File(configFileLocation);
					String fileContent = new Scanner(file).useDelimiter("//z").next();

					if (fileContent.trim().contains(matchingKeyword)) {
						String endOfMatchingContent = "##@";
						int index1 = fileContent.indexOf(matchingKeyword);
						int index2 = fileContent.indexOf(endOfMatchingContent, index1);

						String preContent = fileContent.substring(0, index1);
						String postContent = fileContent.substring(index2 + 3, fileContent.length());

						fileWriter = new FileWriter(file.getPath());
						bufferedWriter = new BufferedWriter(fileWriter);

						bufferedWriter.write("");
						bufferedWriter.write(preContent.concat(postContent));
					}
				} catch (FileNotFoundException ex) {
					String errorMessage = "Nginx virtual host configuration cannot be found at " + file.getPath();
					log.error(errorMessage, ex);
					throw new FileNotFoundException(errorMessage);
				} catch (IOException ex) {
					String errorMessage = "Error occurred during the deletion of url-mapping of " + domainName;
					log.error(errorMessage, ex);
					throw new IOException(errorMessage, ex);
				} finally {
					if (bufferedWriter != null) {
						bufferedWriter.close();
					}

					if (fileWriter != null) {
						fileWriter.close();
					}
				}
			}
		}
	}

	/**
	 * Restarting nginx server
	 *
	 * @throws IOException
	 */
	protected void restartNginX() throws IOException {
		try {
			Runtime.getRuntime().exec(NginxVhostConstants.NGINX_RELOAD_CMD);
			log.info("Reloaded Nginx Configurations");
		} catch (IOException e) {
			String errorMessage = "Error occurred when reloading the nginx configurations";
			log.error(errorMessage, e);
			throw new IOException(errorMessage, e);
		}
	}

	/**
	 * Restore to a fresh vhost configuration
	 *
	 * @throws RegistryException
	 * @throws JSONException
	 * @throws IOException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws UnrecoverableKeyException
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws InterruptedException
	 */
	void restoreVirtualHosts()
			throws RegistryException, JSONException, IOException, KeyStoreException, NoSuchAlgorithmException,
			       CertificateException, UnrecoverableKeyException, NoSuchPaddingException, IllegalBlockSizeException,
			       BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, InterruptedException {
		String registryPath = this.configReader.getProperty("remoteregistry.path");
		RegistryManager registryManager = new RegistryManager(configReader, NginxVhostConstants.AXIS2_CONF_FILE_PATH);
		SSLFileHandler sslFileHandler = new SSLFileHandler(registryManager, configReader);

		List<VHostEntry> apiGatewayHosts = new ArrayList<VHostEntry>();
		List<VHostEntry> apiStoreHosts = new ArrayList<VHostEntry>();
		List<VHostEntry> apiHttpsGatewayHosts = new ArrayList<VHostEntry>();

		try {

			if (registryManager.resourceExists(registryPath)) {

				Collection cloudCollection = (Collection) registryManager.getResourceFromRegistry(registryPath);

				for (int i = 0; i < cloudCollection.getChildCount(); i++) {

					String cloudName = cloudCollection.getChildren()[i]
							.substring(cloudCollection.getChildren()[i].lastIndexOf("/") + 1,
							           cloudCollection.getChildren()[i].length());

					Collection tenantCollection =
							(Collection) registryManager.getResourceFromRegistry(cloudCollection.getChildren()[i]);

					for (int z = 0; z < tenantCollection.getChildCount(); z++) {

						String tenantId = tenantCollection.getChildren()[z]
								.substring(tenantCollection.getChildren()[z].lastIndexOf("/") + 1,
								           tenantCollection.getChildren()[z].length());

						String urlMappingPath = tenantCollection.getChildren()[z] + "/urlMapping/" + tenantId;
						Resource resource = registryManager.getResourceFromRegistry(urlMappingPath);
						byte[] r = (byte[]) resource.getContent();
						try {
							JSONObject jsonObject = new JSONObject(new String(r));
							if (NginxVhostConstants.API_CLOUD_TYPE.equals(cloudName)) {

								//Defining store virtual hosts
								VHostEntry storeEntry = new VHostEntry();
								storeEntry.setTenantDomain(jsonObject.getString(NginxVhostConstants.PAYLOAD_TENANT_DOMAIN));
								storeEntry.setCustomDomain(((JSONObject) jsonObject.get(STORE_NODE))
										                           .getString(NginxVhostConstants.PAYLOAD_CUSTOM_URL));
								storeEntry.setCloudName(cloudName);
								storeEntry.setTemplate(apiStoreVHostTemplate);
								storeEntry.setSecurityCertificateFilePath(sslFileHandler.storeFileInLocal(
										                                          NginxVhostConstants.CERTIFICATE_FILE, jsonObject
										                                          .getString(
												                                          NginxVhostConstants.PAYLOAD_TENANT_DOMAIN),
										                                          STORE_NODE).getAbsolutePath());

								storeEntry.setSecurityCertificateKeyFilePath(sslFileHandler.storeFileInLocal(
										                                             NginxVhostConstants.KEY_FILE, jsonObject
										                                             .getString(
												                                             NginxVhostConstants.PAYLOAD_TENANT_DOMAIN),
										                                             STORE_NODE).getAbsolutePath());

								//Defining gateway virtual hosts
								VHostEntry gatewayEntry = new VHostEntry();
								gatewayEntry.setTenantDomain(jsonObject.getString(NginxVhostConstants.PAYLOAD_TENANT_DOMAIN));
								gatewayEntry.setCustomDomain(((JSONObject) jsonObject.get(GATEWAY_NODE))
										                             .getString(NginxVhostConstants.PAYLOAD_CUSTOM_URL));
								gatewayEntry.setCloudName(cloudName);
								gatewayEntry.setTemplate(apiHttpGatewayVHostTemplate);
								gatewayEntry.setSecurityCertificateFilePath(null);
								gatewayEntry.setSecurityCertificateKeyFilePath(null);

								//Defining https gateway virtual hosts
								VHostEntry httpsGatewayEntry = new VHostEntry();
								httpsGatewayEntry
										.setTenantDomain(jsonObject.getString(NginxVhostConstants.PAYLOAD_TENANT_DOMAIN));
								httpsGatewayEntry.setCustomDomain(((JSONObject) jsonObject.get(GATEWAY_NODE))
										                                  .getString(NginxVhostConstants.PAYLOAD_CUSTOM_URL));
								httpsGatewayEntry.setCloudName(cloudName);
								httpsGatewayEntry.setTemplate(apiHttpsGatewayVHostTemplate);
								httpsGatewayEntry.setSecurityCertificateFilePath(sslFileHandler.storeFileInLocal(
										                                                 NginxVhostConstants.CERTIFICATE_FILE,
										                                                 jsonObject.getString(
												                                                 NginxVhostConstants.PAYLOAD_TENANT_DOMAIN),
										                                                 GATEWAY_NODE)
								                                                               .getAbsolutePath());
								httpsGatewayEntry.setSecurityCertificateKeyFilePath(sslFileHandler.storeFileInLocal(
										                                                    NginxVhostConstants.KEY_FILE,
										                                                    jsonObject.getString(
												                                                    NginxVhostConstants.PAYLOAD_TENANT_DOMAIN),
										                                                    GATEWAY_NODE)
								                                                                  .getAbsolutePath());

								apiStoreHosts.add(storeEntry);
								apiGatewayHosts.add(gatewayEntry);
								apiHttpsGatewayHosts.add(httpsGatewayEntry);

							}
						} catch (JSONException e) {
							String errorMessage = "Error occurred when parsing json url-mapping of " + tenantId;
							log.error(errorMessage, e);
							throw new JSONException(errorMessage);
						} catch (NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException |
								NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException |
								InvalidKeyException | InvalidAlgorithmParameterException | InterruptedException e) {
							String errorMessage = "Error occurred when setting security files for " + tenantId;
							log.error(errorMessage, e);
							throw e;
						}
					}
				}

				//Adding gateway nodes
				addHostToNginxConfig(apiGatewayHosts, configReader.getProperty("nginx.api.gateway.config.path"));

				//Adding store nodes
				addHostToNginxConfig(apiStoreHosts, configReader.getProperty("nginx.api.store.config.path"));

				//Adding https gateway nodes
				addHostToNginxConfig(apiHttpsGatewayHosts,
				                     configReader.getProperty("nginx.api.gateway.https.config.path"));

				this.restartNginX();
			}
		} catch (RegistryException ex) {
			String errorMessage =
					"Error occurred when getting url-mappings from registry while restoring nginx configuration.";
			log.error(errorMessage, ex);
			throw new RegistryException(errorMessage, ex);
		} catch (AxisFault ex) {
			String errorMessage = "Error occurred when restoring nginx configuration.";
			log.error(errorMessage, ex);
			throw new AxisFault(errorMessage, ex);
		} catch (JSONException ex) {
			String errorMessage = "Error occurred when parsing json object.";
			log.error(errorMessage, ex);
			throw new JSONException(errorMessage);
		} catch (IOException e) {
			String errorMessage = "Error occurred while restoring nginx configuration locally.";
			log.error(errorMessage);
			throw new IOException(errorMessage, e);
		} catch (KeyStoreException e) {
			String errorMessage =
					"Error occured when setting ssl files while restoring nginx " + "configuration locally.";
			log.error(errorMessage, e);
			throw new KeyStoreException(errorMessage, e);
		}
	}

}
