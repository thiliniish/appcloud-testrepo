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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for CloudMgt-QueriesType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="CloudMgt-QueriesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CloudMgt-Query" maxOccurs="unbounded" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="DELETE FROM ORGANIZATIONS WHERE tenantDomain = ?"/>
 *               &lt;enumeration value="DELETE FROM SUBSCRIPTIONS WHERE tenantDomain = ?"/>
 *               &lt;enumeration value="DELETE FROM TEMP_INVITEE WHERE tenantDomain = ?"/>
 *               &lt;enumeration value="DELETE FROM TENANT_USER_MAPPING WHERE tenantDomain = ?"/>
 *               &lt;enumeration value="DELETE FROM RIGHTWAVE_CLOUD_SUBSCRIPTION WHERE TENANT_DOMAIN = ?"/>
 *               &lt;enumeration value="DELETE FROM BILLING_ACCOUNT WHERE TENANT_DOMAIN = ?"/>
 *               &lt;enumeration value="DELETE FROM BILLING_STATUS WHERE TENANT_DOMAIN = ?"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD) @XmlType(name = "CloudMgt-QueriesType", propOrder = {
        "cloudMgtQuery" }) public class CloudMgtQueriesType {

    @XmlElement(name = "CloudMgt-Query") protected List<String> cloudMgtQuery;

    /**
     * Gets the value of the cloudMgtQuery property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cloudMgtQuery property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCloudMgtQuery().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getCloudMgtQuery() {
        if (cloudMgtQuery == null) {
            cloudMgtQuery = new ArrayList<String>();
        }
        return this.cloudMgtQuery;
    }

}
