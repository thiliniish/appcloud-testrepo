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
 * Config element that represent the Email xml object
 */

@XmlRootElement(name = "Email")
public class EmailConfig {

    @XmlElement(name = "Host", nillable = false)
    private String host;

    @XmlElement(name = "Port", nillable = false)
    private String port;

    @XmlElement(name = "Username", nillable = false)
    private String username;

    @XmlElement(name = "Password", nillable = false)
    private String password;

    @XmlElement(name = "Sender", nillable = false)
    private String sender;

    @XmlElement(name = "Tls", nillable = false)
    private String tls;

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getSender() {
        return sender;
    }

    public String getTls() {
        return tls;
    }

}


