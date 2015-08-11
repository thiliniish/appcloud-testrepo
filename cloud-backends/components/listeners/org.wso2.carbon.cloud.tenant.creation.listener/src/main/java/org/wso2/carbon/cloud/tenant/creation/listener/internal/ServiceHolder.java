/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.cloud.tenant.creation.listener.internal;

import org.wso2.carbon.cloud.common.CloudMgtConfiguration;
import org.wso2.carbon.user.core.service.RealmService;

public class ServiceHolder {
    private static CloudMgtConfiguration cloudConfiguration;
    private static RealmService realmService;


    public static CloudMgtConfiguration getCloudConfiguration() {
        return cloudConfiguration;
    }

    public static void setCloudMgtConfiguration(CloudMgtConfiguration cloudConfiguration) {
        ServiceHolder.cloudConfiguration = cloudConfiguration;
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    public static void setRealmService(RealmService realmService) {
        ServiceHolder.realmService = realmService;
    }
}
