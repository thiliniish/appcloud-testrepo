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

import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;

/**
 * Config element that represent the SSORelyingParty xml object
 */
@XmlRootElement(name = "SSORelyingParty")
public class SSOConfig {

    private String trustStore;
    private String trustStorePassword;

    @XmlElement(name = "TrustStore", nillable = false)
    public String getKeyStorePath() {
        return trustStore;
    }

    public void setKeyStorePath(String trustStore) {
        this.trustStore = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator
                          + "resources" + File.separator + "security" + File.separator + trustStore;
    }

    @XmlElement(name = "TrustStorePassword")
    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

}
