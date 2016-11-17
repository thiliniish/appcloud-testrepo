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
 * Config element that represent the CloudBillingVendor xml object
 */
@XmlRootElement(name = "CloudBillingVendor") public class BillingVendorConfig {

    private String user;
    private String password;
    private String currency;
    private AuthenticationApiKeys authenticationApiKeys;
    private OAuthEndpointConfig oAuthEndpointConfig;
    @XmlElement(name = "Security", nillable = true, required = false) private SecurityConfig securityConfig;

    @XmlElement(name = "User", nillable = false, required = true) public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @XmlElement(name = "Password", nillable = false, required = true) public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @XmlElement(name = "Currency", nillable = false, required = true) public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @XmlElement(name = "AuthenticationApiKeys", nillable = false, required = true) public AuthenticationApiKeys
    getAuthenticationApiKeys() {
        return authenticationApiKeys;
    }

    public void setAuthenticationApiKeys(AuthenticationApiKeys authenticationApiKeys) {
        this.authenticationApiKeys = authenticationApiKeys;
    }

    @XmlElement(name = "OAuthEndpoint", nillable = false, required = true) public OAuthEndpointConfig
    getOAuthEndpointConfig() {
        return oAuthEndpointConfig;
    }

    public void setOAuthEndpointConfig(OAuthEndpointConfig oAuthEndpointConfig) {
        this.oAuthEndpointConfig = oAuthEndpointConfig;
    }

    public SecurityConfig getSecurityConfig() {
        return securityConfig;
    }

}
