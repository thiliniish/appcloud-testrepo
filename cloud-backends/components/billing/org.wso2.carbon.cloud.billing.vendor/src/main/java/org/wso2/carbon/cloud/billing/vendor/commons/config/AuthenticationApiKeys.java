/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.cloud.billing.vendor.commons.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Config element that represent the AuthenticationApiKeys xml object
 */
@XmlRootElement(name = "AuthenticationApiKeys") public class AuthenticationApiKeys {

    private String apiVersion;
    private String secretKey;
    private String publishableKey;

    @XmlElement(name = "ApiVersion", nillable = false, required = true) public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    @XmlElement(name = "SecretKey", nillable = false, required = true) public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    @XmlElement(name = "PublishableKey", nillable = false, required = true) public String getPublishableKey() {
        return publishableKey;
    }

    public void setPublishableKey(String publishableKey) {
        this.publishableKey = publishableKey;
    }

}
