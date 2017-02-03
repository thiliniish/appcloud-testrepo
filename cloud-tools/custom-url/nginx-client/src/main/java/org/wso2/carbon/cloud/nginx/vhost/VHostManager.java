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
import org.wso2.carbon.cloud.nginx.vhost.util.DomainMapperException;
import org.wso2.carbon.cloud.nginx.vhost.util.RegistryManager;
import org.wso2.carbon.cloud.nginx.vhost.util.SSLFileHandler;
import org.wso2.carbon.cloud.nginx.vhost.util.TemplateManager;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

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
     *
     * @param cloudType          Cloud name
     * @param tenantDomain       tenant domain
     * @param customUrl          customUrl
     * @param sslFilePath        SSL certificate file path
     * @param privateKeyFilePath private key file path
     * @param template           template file content
     * @return VHostEntry bean object
     */
    public VHostEntry buildVhostEntry(String cloudType, String tenantDomain, String customUrl, String sslFilePath,
                                      String privateKeyFilePath, String template) {
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
            if (file.getParentFile().mkdirs()) {
                if (log.isDebugEnabled()) {
                    log.debug("Directory structure " + file.getParent() + " created for nginx config files");
                }
            }
            if (file.createNewFile()) {
                log.info("Creating a new Nginx Configuration file " + filePath + " for VHost successful");
            } else {
                log.error("Creating a new Nginx Configuration file " + filePath + " for VHost failed");
            }
        }
        //Write the vhost configs to custom location
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getAbsoluteFile()),
                                                                       NginxVhostConstants.DEFAULT_ENCODING));
            bufferedWriter.write(template);
        } catch (IOException e) {
            String errorMessage = "Error thrown when writing contents to VHOST file.";
            log.error(errorMessage, e);
            throw new IOException(errorMessage, e);
        } finally {
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
        }
        log.info("Updated Nginx config file ");
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
        if (template.contains("${ssl_certificate_path}")) {
            template = template.replace("${ssl_certificate_path}", sslCertificatePath);
            template = template.replace("${ssl_key_path}", sslKeyFilePath);
        }
        return template;
    }

    protected void removeHostMapping(String domainName, String cloudType, String node) throws IOException {
        String filePath;
        if (NginxVhostConstants.API_CLOUD_TYPE.equals(cloudType)) {
            if (STORE_NODE.equals(node)) {
                filePath = configReader.getProperty(NginxVhostConstants.NGINX_CONFIG_PATH) + File.separator
                                   + domainName + NginxVhostConstants.STORE_CUSTOM_CONFIG;
                deleteFile(filePath);
            } else {
                //Remove http config file
                filePath = configReader.getProperty(NginxVhostConstants.NGINX_CONFIG_PATH) + File.separator
                                   + domainName + NginxVhostConstants.GATEWAY_CUSTOM_CONFIG;
                deleteFile(filePath);
                //Remove https config file
                filePath = configReader.getProperty(NginxVhostConstants.NGINX_CONFIG_PATH) + File.separator
                                   + domainName + NginxVhostConstants.GATEWAY_HTTPS_CUSTOM_CONFIG;
                deleteFile(filePath);
            }
        }
    }

    /**
     * delete a mapping in the given path
     *
     * @param filePath
     */
    private void deleteFile(String filePath) {
        boolean isSuccessful;
        File file = new File(filePath);
        if (file.exists()) {
            isSuccessful = file.delete();
            if (isSuccessful) {
                if (log.isDebugEnabled()) {
                    log.debug("The custom url mapping at " + filePath + " is deleted.");
                }
            } else {
                log.error("Error occurred while deleting config file at " + file.getAbsolutePath());
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
                           CertificateException, UnrecoverableKeyException, NoSuchPaddingException,
                           IllegalBlockSizeException, BadPaddingException, InvalidKeyException,
                           InvalidAlgorithmParameterException, InterruptedException {
        String registryPath = this.configReader.getProperty("remoteregistry.path");
        RegistryManager registryManager = new RegistryManager(configReader, NginxVhostConstants.AXIS2_CONF_FILE_PATH);
        SSLFileHandler sslFileHandler = new SSLFileHandler(registryManager, configReader);

        try {

            if (registryManager.resourceExists(registryPath)) {

                //Move existing configs to a backup directory
                String currentConfigPath = configReader.getProperty(NginxVhostConstants.NGINX_CONFIG_PATH);
                String currentCertPath = configReader.getProperty(NginxVhostConstants.API_SECURITY_CERTIFICATE_PATH);
                backupExistingConfigs(currentConfigPath);
                backupExistingConfigs(currentCertPath);
                Collection cloudCollection = (Collection) registryManager.getResourceFromRegistry(registryPath);

                for (int i = 0; i < cloudCollection.getChildCount(); i++) {
                    String cloudCollectionElement = cloudCollection.getChildren()[i];
                    String cloudName = cloudCollectionElement.substring(
                            cloudCollectionElement.lastIndexOf(File.separator) + 1,
                            cloudCollectionElement.length());
                    Collection tenantCollection =
                            (Collection) registryManager.getResourceFromRegistry(cloudCollectionElement);
                    if (NginxVhostConstants.API_CLOUD_TYPE.equals(cloudName)) {
                        for (int z = 0; z < tenantCollection.getChildCount(); z++) {
                            String tenantCollectionElement = tenantCollection.getChildren()[z];
                            String tenantDomain = tenantCollectionElement
                                                      .substring(tenantCollectionElement.lastIndexOf(File.separator)
                                                                         + 1, tenantCollectionElement.length());

                            String urlMappingPath = tenantCollectionElement + "/urlMapping/"
                                                            + configReader.getProperty("region") + "-" + tenantDomain;
                            if (!registryManager.resourceExists(urlMappingPath)) {
                                if (i == cloudCollection.getChildCount()) {
                                    log.info("There are no url mappings for this region yet.");
                                    break;
                                } else {
                                    //If the tenant mapping is not for this region
                                    continue;
                                }
                            }
                            Resource resource = registryManager.getResourceFromRegistry(urlMappingPath);
                            byte[] r = (byte[]) resource.getContent();
                            try {
                                JSONObject jsonObject = new JSONObject(new String(r,
                                                                                  NginxVhostConstants
                                                                                          .DEFAULT_ENCODING));

                                //Defining store virtual hosts
                                VHostEntry storeEntry = new VHostEntry();
                                String tenantName = jsonObject.getString(NginxVhostConstants.PAYLOAD_TENANT_DOMAIN);
                                storeEntry.setTenantDomain(tenantName);
                                String filePath = configReader.getProperty(NginxVhostConstants.NGINX_CONFIG_PATH) +
                                                          File.separator + tenantName;
                                storeEntry.setCustomDomain(((JSONObject) jsonObject.get(STORE_NODE))
                                                                   .getString(NginxVhostConstants.PAYLOAD_CUSTOM_URL));
                                storeEntry.setCloudName(cloudName);
                                storeEntry.setTemplate(apiStoreVHostTemplate);
                                try {
                                    storeEntry.setSecurityCertificateFilePath(sslFileHandler.storeFileInLocal(
                                            NginxVhostConstants.CERTIFICATE_FILE,
                                            jsonObject.getString(NginxVhostConstants.PAYLOAD_TENANT_DOMAIN), STORE_NODE)
                                                                                            .getAbsolutePath());

                                    storeEntry.setSecurityCertificateKeyFilePath(sslFileHandler.storeFileInLocal(
                                            NginxVhostConstants.KEY_FILE,
                                            jsonObject.getString(NginxVhostConstants.PAYLOAD_TENANT_DOMAIN), STORE_NODE)
                                                                                               .getAbsolutePath());
                                    addHostToNginxConfig(storeEntry,
                                                         filePath + NginxVhostConstants.STORE_CUSTOM_CONFIG);
                                } catch (DomainMapperException ex) {
                                    log.warn("Adding Vhost template avoided for STORE for TENANT DOMAIN " + tenantDomain
                                                     + " due to no STORE registry resource for certificates");
                                }

                                //Indicates whether to add gateway http virtual hosts
                                boolean addHttpVhost = true;
                                //Defining https gateway virtual hosts
                                VHostEntry httpsGatewayEntry = new VHostEntry();
                                httpsGatewayEntry.setTenantDomain(
                                        jsonObject.getString(NginxVhostConstants.PAYLOAD_TENANT_DOMAIN));
                                httpsGatewayEntry.setCustomDomain(((JSONObject) jsonObject.get(GATEWAY_NODE)).getString(
                                        NginxVhostConstants.PAYLOAD_CUSTOM_URL));
                                httpsGatewayEntry.setCloudName(cloudName);
                                httpsGatewayEntry.setTemplate(apiHttpsGatewayVHostTemplate);
                                try {
                                    httpsGatewayEntry.setSecurityCertificateFilePath(sslFileHandler.storeFileInLocal(
                                            NginxVhostConstants.CERTIFICATE_FILE,
                                            jsonObject.getString(NginxVhostConstants.PAYLOAD_TENANT_DOMAIN),
                                            GATEWAY_NODE).getAbsolutePath());
                                    httpsGatewayEntry.setSecurityCertificateKeyFilePath(sslFileHandler.storeFileInLocal(
                                            NginxVhostConstants.KEY_FILE,
                                            jsonObject.getString(NginxVhostConstants.PAYLOAD_TENANT_DOMAIN),
                                            GATEWAY_NODE).getAbsolutePath());
                                    addHostToNginxConfig(httpsGatewayEntry,
                                                         filePath + NginxVhostConstants.GATEWAY_HTTPS_CUSTOM_CONFIG);

                                } catch (DomainMapperException ex) {
                                    addHttpVhost = false;
                                    log.warn("Adding Vhost template avoided for GATEWAY for TENANT DOMAIN "
                                                     + tenantDomain
                                                     + " due to no GATEWAY registry resource for certificates");
                                }
                                if (addHttpVhost) {
                                    //Defining gateway virtual hosts
                                    VHostEntry gatewayEntry = new VHostEntry();
                                    gatewayEntry.setTenantDomain(
                                            jsonObject.getString(NginxVhostConstants.PAYLOAD_TENANT_DOMAIN));
                                    gatewayEntry.setCustomDomain(((JSONObject) jsonObject.get(GATEWAY_NODE)).getString(
                                            NginxVhostConstants.PAYLOAD_CUSTOM_URL));
                                    gatewayEntry.setCloudName(cloudName);
                                    gatewayEntry.setTemplate(apiHttpGatewayVHostTemplate);
                                    gatewayEntry.setSecurityCertificateFilePath(null);
                                    gatewayEntry.setSecurityCertificateKeyFilePath(null);
                                    //Adding gateway nodes
                                    addHostToNginxConfig(gatewayEntry,
                                                         filePath + NginxVhostConstants.GATEWAY_CUSTOM_CONFIG);
                                }

                            } catch (JSONException e) {
                                String errorMessage = "Error occurred when parsing json url-mapping of " + tenantDomain;
                                log.error(errorMessage, e);
                                throw new JSONException(errorMessage);
                            } catch (NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException |
                                             NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException |
                                             InvalidKeyException | InvalidAlgorithmParameterException |
                                             InterruptedException e) {
                                String errorMessage = "Error occurred when setting security files for " + tenantDomain;
                                log.error(errorMessage, e);
                                throw e;
                            }
                        }
                    }
                }
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
                    "Error occured when setting ssl files while restoring nginx configuration locally.";
            log.error(errorMessage, e);
            throw new KeyStoreException(errorMessage, e);
        }
    }

    private void backupExistingConfigs(String currentFilePath) {
        File file = new File(currentFilePath);
        if (file.exists()) {
            //Get current date
            SimpleDateFormat dtf = new SimpleDateFormat(NginxVhostConstants.DATE_FORMAT);
            Date localDate = Calendar.getInstance().getTime();
            String backupPath = currentFilePath + "-" + dtf.format(localDate);
            if (file.renameTo(new File(backupPath))) {
                log.info("Custom url configs are backed up at " + backupPath);
            } else {
                log.info("Custom url configs backup to " + backupPath + " failed");
            }
        } else {
            log.info("There are no custom url configs to backup");
        }
    }

}
