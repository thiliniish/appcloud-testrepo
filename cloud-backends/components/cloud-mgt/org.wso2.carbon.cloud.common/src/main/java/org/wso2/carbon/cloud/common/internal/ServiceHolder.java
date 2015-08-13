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

package org.wso2.carbon.cloud.common.internal;

import org.wso2.carbon.securevault.SecretCallbackHandlerService;
import org.wso2.carbon.user.core.service.RealmService;

public class ServiceHolder {
    private static RealmService realmService;
    private static SecretCallbackHandlerService secretCallbackHandlerService;

    public static RealmService getRealmService() {
        return realmService;
    }

    public static void setRealmService(RealmService realmService) {
        ServiceHolder.realmService = realmService;
    }

    public static SecretCallbackHandlerService getSecretCallbackHandlerService() {
        return secretCallbackHandlerService;
    }

    public static void setSecretCallbackHandlerService(SecretCallbackHandlerService secretCallbackHandlerService) {
        ServiceHolder.secretCallbackHandlerService = secretCallbackHandlerService;
    }
}
