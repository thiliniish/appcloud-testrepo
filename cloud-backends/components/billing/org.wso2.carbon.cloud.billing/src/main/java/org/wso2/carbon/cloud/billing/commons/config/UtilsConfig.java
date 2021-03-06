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
 * Config element that represent the Utils xml object
 */
@XmlRootElement(name = "Utils")
public class UtilsConfig {

    private APICloudUtils apiCloud;
    private NotificationConfig notifications;

    @XmlElement(name = "APICloud", nillable = false)
    public APICloudUtils getApiCloud() {
        return apiCloud;
    }

    public void setApiCloud(APICloudUtils apiCloud) {
        this.apiCloud = apiCloud;
    }

    @XmlElement(name = "Notification", nillable = false) public NotificationConfig getNotifications() {
        return notifications;
    }

    public void setNotifications(NotificationConfig notifications) {
        this.notifications = notifications;
    }

}
