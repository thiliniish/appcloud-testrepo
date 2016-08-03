/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.cloud.tenantdeletion.conf;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for Email-PropertiesType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="Email-PropertiesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="port" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="host" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="user-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="sender-email" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="sender-password" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="recipient-email" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD) @XmlType(name = "Email-PropertiesType", propOrder = { "port", "host", "userName",
                                                                                            "senderEmail",
                                                                                            "senderPassword",
                                                                                            "recipientEmail" }) public class EmailPropertiesType {
    @XmlElement(required = true) protected String port;
    @XmlElement(required = true) protected String host;
    @XmlElement(name = "user-name", required = true) protected String userName;
    @XmlElement(name = "sender-email", required = true) protected String senderEmail;
    @XmlElement(name = "sender-password", required = true) protected String senderPassword;
    @XmlElement(name = "recipient-email", required = true) protected String recipientEmail;

    /**
     * Gets the value of the port property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPort() {
        return port;
    }

    /**
     * Sets the value of the port property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPort(String value) {
        this.port = value;
    }

    /**
     * Gets the value of the host property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the value of the host property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setHost(String value) {
        this.host = value;
    }

    /**
     * Gets the value of the senderEmail property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSenderEmail() {
        return senderEmail;
    }

    /**
     * Sets the value of the senderEmail property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSenderEmail(String value) {
        this.senderEmail = value;
    }

    /**
     * Gets the value of the senderPassword property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSenderPassword() {
        return senderPassword;
    }

    /**
     * Sets the value of the senderPassword property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSenderPassword(String value) {
        this.senderPassword = value;
    }

    /**
     * Gets the value of the recipientEmail property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRecipientEmail() {
        return recipientEmail;
    }

    /**
     * Sets the value of the recipientEmail property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRecipientEmail(String value) {
        this.recipientEmail = value;
    }

    /**
     * Sets the value of username to authenticate host access
     *
     * @param value allowed object is
     *              {@link String}
     */
    public void setUserName(String value) {
        this.userName = value;
    }

    /**
     * Gets the value of authentication user name
     *
     * @return possible object is
     * {@link String}
     */
    public String getUserName() {
        return userName;
    }

}
