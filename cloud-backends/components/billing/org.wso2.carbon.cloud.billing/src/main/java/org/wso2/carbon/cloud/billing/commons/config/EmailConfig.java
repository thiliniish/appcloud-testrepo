/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.cloud.billing.commons.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Config element that represent the Email xml object
 */

@XmlRootElement(name = "Email") public class EmailConfig {

    private String host;
    private String port;
    private String userName;
    private String password;
    private String sender;
    private String tls;

    @XmlElement(name = "Host", nillable = false) public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @XmlElement(name = "Port", nillable = false) public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    @XmlElement(name = "UserName", nillable = false) public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @XmlElement(name = "Password", nillable = false) public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @XmlElement(name = "Sender", nillable = false) public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    @XmlElement(name = "Tls", nillable = false) public String getTls() {
        return tls;
    }

    public void setTls(String tls) {
        this.tls = tls;
    }

}


