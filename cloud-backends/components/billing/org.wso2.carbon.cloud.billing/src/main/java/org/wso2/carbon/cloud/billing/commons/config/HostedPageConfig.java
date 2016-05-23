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
 * Config element that represent the HostedPage xml object
 */
@XmlRootElement(name = "HostedPage")
public class HostedPageConfig {

    private String publicKey;
    private String uri;
    private String endPointUri;
    private String pageId;
    private String paymentGateway;
    private String locale;


    @XmlElement(name = "PublicKey", nillable = false, required = true)
    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @XmlElement(name = "URI", nillable = false, required = true)
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @XmlElement(name = "EndPointURI", nillable = false, required = true)
    public String getEndPointUri() {
        return endPointUri;
    }

    public void setEndPointUri(String endPointUri) {
        this.endPointUri = endPointUri;
    }

    @XmlElement(name = "PageId", nillable = false, required = true)
    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    @XmlElement(name = "PaymentGateway", nillable = false, required = true)
    public String getPaymentGateway() {
        return paymentGateway;
    }

    public void setPaymentGateway(String paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    @XmlElement(name = "Locale", required = false)
    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }
}
