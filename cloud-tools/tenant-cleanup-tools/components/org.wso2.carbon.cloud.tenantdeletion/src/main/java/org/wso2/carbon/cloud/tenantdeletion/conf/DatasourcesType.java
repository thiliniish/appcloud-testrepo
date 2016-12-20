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
 * <p>Java class for datasourcesType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="datasourcesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cloudMgt-datasource" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD) @XmlType(name = "datasourcesType", propOrder = { "cloudMgtDatasource",
                                                                                       "userMgtDatasource" }) public
class DatasourcesType {

    @XmlElement(name = "cloudMgt-datasource", required = true) protected String cloudMgtDatasource;

    @XmlElement(name = "userMgt-datasource") protected String userMgtDatasource;

    /**
     * Gets the value of the cloudMgtDatasource property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCloudMgtDatasource() {
        return cloudMgtDatasource;
    }

    /**
     * Sets the value of the cloudMgtDatasource property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCloudMgtDatasource(String value) {
        this.cloudMgtDatasource = value;
    }

    /**
     * Gets the value of the userMgtDatasource property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getUserMgtDatasource() {
        return userMgtDatasource;
    }

    /**
     * Sets the value of the userMgtDatasource property.
     *
     * @param userMgtDatasource allowed object is
     *                          {@link String }
     */
    public void setUserMgtDatasource(String userMgtDatasource) {
        this.userMgtDatasource = userMgtDatasource;
    }
}
