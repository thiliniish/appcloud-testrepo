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

public class Constants {

    public static final String LOG4J_PROPERTY_PATH = "resources/log4j.properties";
    public static final String CONFIG_FILE_PATH = "resources/config.properties";
    public static final String VHOST_STORE_TEMPLATE_FILE_PATH = "resources/vhost-template.txt";
    public static final String VHOST_GATEWAY_HTTP_TEMPLATE_FILE_PATH = "resources/vhost-gateway-http-template.txt";
    public static final String VHOST_GATEWAY_HTTPS_TEMPLATE_FILE_PATH = "resources/vhost-gateway-https-template.txt";
    public static final String NGINX_RELOAD_CMD = "/etc/init.d/nginx restart";
    public static final String KEY_STORE_FILE_PATH = "resources/wso2carbon.jks";
    public static final String AXIS2_CONF_FILE_PATH = "resources/axis2_client.xml";
    public static final String TRUST_STORE_LOCATION = "resources/client-truststore.jks";
    public static final String PAYLOAD_NODE = "node";
    public static final String PAYLOAD_TENANT_DOMAIN = "tenantDomain";
    public static final String PAYLOAD_CUSTOM_URL = "customUrl";
    public static final String PAYLOAD_STATUS = "status";
    public static final String PAYLOAD_CLOUD_TYPE = "cloudType";
    public static final String CLOUD_TYPE = "cloudType";
    public static final String API_CLOUD_TYPE = "api-cloud";
    public static final String API_STORE_TEMPLATE_NAME = "api-cloud-template";
    public static final String HTTP_API_GATEWAY_TEMPLATE_NAME = "http-api-gateway-template";
    public static final String HTTPS_API_GATEWAY_TEMPLATE_NAME = "https-api-gateway-template";
    public static final String CERTIFICATE_FILE = "cert";
    public static final String KEY_FILE = "key";
}
