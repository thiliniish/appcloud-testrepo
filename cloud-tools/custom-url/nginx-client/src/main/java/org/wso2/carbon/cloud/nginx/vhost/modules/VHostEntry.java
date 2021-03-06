/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.cloud.nginx.vhost.modules;

/**
 * VHost bean class
 */
public class VHostEntry {
    private String tenantDomain;
    private String cloudName;
    private String customDomain;
    private String securityCertificateFilePath;
    private String securityCertificateKeyFilePath;
    private String template;

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public String getCloudName() {
        return cloudName;
    }

    public void setCloudName(String cloudName) {
        this.cloudName = cloudName;
    }

    public String getCustomDomain() {
        return customDomain;
    }

    public void setCustomDomain(String customDomain) {
        this.customDomain = customDomain;
    }

    public String getSecurityCertificateFilePath() {
        return securityCertificateFilePath;
    }

    public void setSecurityCertificateFilePath(String securityCertificateFilePath) {
        this.securityCertificateFilePath = securityCertificateFilePath;
    }

    public String getSecurityCertificateKeyFilePath() {
        return securityCertificateKeyFilePath;
    }

    public void setSecurityCertificateKeyFilePath(String securityCertificateKeyFilePath) {
        this.securityCertificateKeyFilePath = securityCertificateKeyFilePath;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}
