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
 * TrustStore related configurations
 */
@XmlRootElement(name = "TrustStore") public class TrustStore {

    @XmlElement(name = "Name", nillable = false) private String name;

    @XmlElement(name = "Type", nillable = false) private String type;

    @XmlElement(name = "Password", nillable = false) private String password;

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getPassword() {
        return password;
    }

}
