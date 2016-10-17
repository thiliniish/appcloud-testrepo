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

package org.wso2.carbon.cloud.billing.vendor.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.securevault.SecretCallbackHandlerService;

/**
 * Represent the data holder for the service component
 */
public class ServiceDataHolder {
    private static final ServiceDataHolder SERVICE_DATA_HOLDER = new ServiceDataHolder();
    private static final Log LOGGER = LogFactory.getLog(ServiceDataHolder.class);
    private SecretCallbackHandlerService secretCallbackHandlerService;


    private ServiceDataHolder() {
    }

    /**
     * Get service data holder instance
     *
     * @return service data holder instance
     */
    public static ServiceDataHolder getInstance() {
        return SERVICE_DATA_HOLDER;
    }


    /**
     * Get secret callback handler service
     *
     * @return SecretCallbackHandlerService
     */
    public SecretCallbackHandlerService getSecretCallbackHandlerService() {
        return secretCallbackHandlerService;
    }

    /**
     * Set secret callback handler service
     *
     * @param secretCallbackHandlerService service available
     */
    public void setSecretCallbackHandlerService(SecretCallbackHandlerService secretCallbackHandlerService) {
        this.secretCallbackHandlerService = secretCallbackHandlerService;
    }

}
