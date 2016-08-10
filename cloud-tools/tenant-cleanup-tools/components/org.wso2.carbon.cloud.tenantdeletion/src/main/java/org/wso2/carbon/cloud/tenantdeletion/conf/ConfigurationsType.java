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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for ConfigurationsType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="ConfigurationsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="datasources" type="{}datasourcesType"/>
 *         &lt;element name="Deletion-Order" type="{}Deletion-OrderType"/>
 *         &lt;element name="ServerKeys" type="{}ServerKeysType"/>
 *         &lt;element name="CloudMgt-Queries" type="{}CloudMgt-QueriesType"/>
 *         &lt;element name="Email-Properties" type="{}Email-PropertiesType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD) @XmlType(name = "ConfigurationsType", propOrder = { "datasources",
                                                                                          "deletionOrder", "serverKeys",
                                                                                          "cloudMgtQueries",
                                                                                          "emailProperties" })
@XmlRootElement(name = "Configurations") public class ConfigurationsType {

    @XmlElement(required = true) protected DatasourcesType datasources;
    @XmlElement(name = "Deletion-Order", required = true) protected DeletionOrderType deletionOrder;
    @XmlElement(name = "ServerKeys", required = true) protected ServerKeysType serverKeys;
    @XmlElement(name = "CloudMgt-Queries", required = true) protected CloudMgtQueriesType cloudMgtQueries;
    @XmlElement(name = "Email-Properties", required = true) protected EmailPropertiesType emailProperties;

    /**
     * Gets the value of the datasources property.
     *
     * @return possible object is
     * {@link DatasourcesType }
     */
    public DatasourcesType getDatasources() {
        return datasources;
    }

    /**
     * Sets the value of the datasources property.
     *
     * @param value allowed object is
     *              {@link DatasourcesType }
     */
    public void setDatasources(DatasourcesType value) {
        this.datasources = value;
    }

    /**
     * Gets the value of the deletionOrder property.
     *
     * @return possible object is
     * {@link DeletionOrderType }
     */
    public DeletionOrderType getDeletionOrder() {
        return deletionOrder;
    }

    /**
     * Sets the value of the deletionOrder property.
     *
     * @param value allowed object is
     *              {@link DeletionOrderType }
     */
    public void setDeletionOrder(DeletionOrderType value) {
        this.deletionOrder = value;
    }

    /**
     * Gets the value of the serverKeys property.
     *
     * @return possible object is
     * {@link ServerKeysType }
     */
    public ServerKeysType getServerKeys() {
        return serverKeys;
    }

    /**
     * Sets the value of the serverKeys property.
     *
     * @param value allowed object is
     *              {@link ServerKeysType }
     */
    public void setServerKeys(ServerKeysType value) {
        this.serverKeys = value;
    }

    /**
     * Gets the value of the cloudMgtQueries property.
     *
     * @return possible object is
     * {@link CloudMgtQueriesType }
     */
    public CloudMgtQueriesType getCloudMgtQueries() {
        return cloudMgtQueries;
    }

    /**
     * Sets the value of the cloudMgtQueries property.
     *
     * @param value allowed object is
     *              {@link CloudMgtQueriesType }
     */
    public void setCloudMgtQueries(CloudMgtQueriesType value) {
        this.cloudMgtQueries = value;
    }

    /**
     * Gets the value of the emailProperties property.
     *
     * @return possible object is
     * {@link EmailPropertiesType }
     */
    public EmailPropertiesType getEmailProperties() {
        return emailProperties;
    }

    /**
     * Sets the value of the emailProperties property.
     *
     * @param value allowed object is
     *              {@link EmailPropertiesType }
     */
    public void setEmailProperties(EmailPropertiesType value) {
        this.emailProperties = value;
    }
}
