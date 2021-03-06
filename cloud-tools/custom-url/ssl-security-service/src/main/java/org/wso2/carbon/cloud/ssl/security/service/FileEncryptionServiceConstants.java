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

/**
 * Constants for File Encryption
 */
public class FileEncryptionServiceConstants {

    public static final String AES256_ALGORITHM = "AES/CBC/PKCS5Padding";
    public static final String SECRET_KEY_SPEC_ALGORITHM = "AES";
    public static final String ENCODING_MECHANISM = "UTF-8";
    public static final String CERTIFICATE_ALGORITHM = "X.509";
    public static final String KEY_ENCRYPTION_ALGORITHM = "RSA";
    public static final String KEY_STORE_CONFIG_FILE =
            System.getProperty("user.dir") + "/repository/conf/cloud/file-encrypt-config.properties";
}
