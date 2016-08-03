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
 * <p>Java class for DeleterType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="DeleterType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Class">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="org.wso2.carbon.cloud.tenantdeletion.deleter.APIDeleter"/>
 *               &lt;enumeration value="org.wso2.carbon.cloud.tenantdeletion.deleter.APPDeleter"/>
 *               &lt;enumeration value="org.wso2.carbon.cloud.tenantdeletion.deleter.RegistryDataDeleter"/>
 *               &lt;enumeration value="org.wso2.carbon.cloud.tenantdeletion.deleter.CloudMgtDataDeleter"/>
 *               &lt;enumeration value="org.wso2.carbon.cloud.tenantdeletion.deleter.LDAPDataDeleter"/>
 *               &lt;enumeration value="org.wso2.carbon.cloud.tenantdeletion.deleter.UMDataDeleter"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="Dependency">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value=""/>
 *               &lt;enumeration value="API"/>
 *               &lt;enumeration value="API,CONFIG,LDAP,CLOUDMGT"/>
 *               &lt;enumeration value="API,CONFIG"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="ServerKey">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="AM"/>
 *               &lt;enumeration value="AF"/>
 *               &lt;enumeration value="AF,AM,IS,Gateway,KeyManager,CloudMgt,BPS,Storage Server,UES,BAM"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD) @XmlType(name = "DeleterType", propOrder = { "clazz", "dependency",
                                                                                   "serverKey" }) public class DeleterType {

    @XmlElement(name = "Class", required = true) protected String clazz;
    @XmlElement(name = "Dependency", required = true) protected String dependency;
    @XmlElement(name = "ServerKey", required = true) protected String serverKey;

    /**
     * Gets the value of the clazz property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getClazz() {
        return clazz;
    }

    /**
     * Sets the value of the clazz property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setClazz(String value) {
        this.clazz = value;
    }

    /**
     * Gets the value of the dependency property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDependency() {
        return dependency;
    }

    /**
     * Sets the value of the dependency property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDependency(String value) {
        this.dependency = value;
    }

    /**
     * Gets the value of the serverKey property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getServerKey() {
        return serverKey;
    }

    /**
     * Sets the value of the serverKey property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setServerKey(String value) {
        this.serverKey = value;
    }

}
