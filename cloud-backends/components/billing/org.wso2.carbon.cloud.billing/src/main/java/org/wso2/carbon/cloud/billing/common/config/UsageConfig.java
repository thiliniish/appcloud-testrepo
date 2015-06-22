/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.cloud.billing.common.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Config element that represent the Usage xml object
 */
@XmlRootElement(name = "Usage")
public class UsageConfig {

    private boolean enableUsageUploading;
    private String cron;
    private String usageUploadFileLocation;

    @XmlElement(name = "EnableUsageUploading", nillable = false)
    public boolean isEnableUsageUploading() {
        return enableUsageUploading;
    }

    public void setEnableUsageUploading(boolean enableUsageUploading) {
        this.enableUsageUploading = enableUsageUploading;
    }

    @XmlElement(name = "Cron", nillable = false)
    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    @XmlElement(name = "UsageUploadFileLocation", nillable = false)
    public String getUsageUploadFileLocation() {
        return usageUploadFileLocation;
    }

    public void setUsageUploadFileLocation(String usageUploadFileLocation) {
        this.usageUploadFileLocation = usageUploadFileLocation;
    }

}
