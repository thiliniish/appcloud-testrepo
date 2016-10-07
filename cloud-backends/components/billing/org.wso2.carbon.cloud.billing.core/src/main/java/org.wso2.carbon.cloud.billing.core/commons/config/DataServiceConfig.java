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

package org.wso2.carbon.cloud.billing.core.commons.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Config element that represent the DataServiceAPI xml object
 */
@XmlRootElement(name = "DataServiceAPI")
public class DataServiceConfig {

    private String cloudBillingServiceUri;
    private String cloudMonetizationServiceUri;
    private String apiCloudMonetizationServiceUri;
    private String user;
    private String password;
    private HttpClientConfig httpClientConfig;

    @XmlElement(name = "CloudBillingServiceURI", nillable = false, required = true)
    public String getCloudBillingServiceUri() {
        return cloudBillingServiceUri;
    }

    public void setCloudBillingServiceUri(String cloudBillingServiceUri) {
        this.cloudBillingServiceUri = cloudBillingServiceUri;
    }

    @XmlElement(name = "User", nillable = false, required = true)
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @XmlElement(name = "Password", nillable = false, required = true)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @XmlElement(name = "APICloudMonetizationServiceURI", nillable = false, required = true)
    public String getApiCloudMonetizationServiceUri() {
        return apiCloudMonetizationServiceUri;
    }

    public void setApiCloudMonetizationServiceUri(String apiCloudMonetizationServiceUri) {
        this.apiCloudMonetizationServiceUri = apiCloudMonetizationServiceUri;
    }

    @XmlElement(name = "CloudMonetizationServiceURI", nillable = false, required = true)
    public String getCloudMonetizationServiceUri() {
        return cloudMonetizationServiceUri;
    }

    public void setCloudMonetizationServiceUri(String cloudMonetizationServiceUri) {
        this.cloudMonetizationServiceUri = cloudMonetizationServiceUri;
    }

    @XmlElement(name = "HttpClientConfig", nillable = false)
    public HttpClientConfig getHttpClientConfig() {
        return httpClientConfig;
    }

    public void setHttpClientConfig(HttpClientConfig httpClientConfig) {
        this.httpClientConfig = httpClientConfig;
    }
}
