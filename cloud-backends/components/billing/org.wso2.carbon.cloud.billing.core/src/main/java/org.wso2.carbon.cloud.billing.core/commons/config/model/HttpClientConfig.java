/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.cloud.billing.core.commons.config.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Config element that represent the HttpClientConfig xml object
 */
@XmlRootElement(name = "HttpClientConfig") public class HttpClientConfig {

    @XmlElement(name = "Hostname", required = false) private String hostname;

    @XmlElement(name = "Port", required = false) private int port;

    @XmlElement(name = "MaxConnectionsPerHost", nillable = false, required = true) private int maxConnectionsPerHost;

    @XmlElement(name = "MaxTotalConnections", nillable = false, required = true) private int maxTotalConnections;

    public HttpClientConfig() {
    }

    public HttpClientConfig(String hostname, int port, int maxConnectionsPerHost, int maxTotalConnections) {
        this.hostname = hostname;
        this.port = port;
        this.maxConnectionsPerHost = maxConnectionsPerHost;
        this.maxTotalConnections = maxTotalConnections;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public int getMaxConnectionsPerHost() {
        return maxConnectionsPerHost;
    }

    public int getMaxTotalConnections() {
        return maxTotalConnections;
    }

}
