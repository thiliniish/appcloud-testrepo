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

package org.wso2.carbon.cloud.billing.vendor.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.securevault.SecretCallbackHandlerService;

/**
 * @scr.component name="cloud.billing.vender.component" immediate=true
 * @scr.reference name="secret.callback.handler.service"
 * interface="org.wso2.carbon.securevault.SecretCallbackHandlerService"
 * bind="setSecretCallbackHandlerService" unbind="unsetSecretCallbackHandlerService"
 * cardinality="1..1" policy="dynamic"
 */
public class CloudBillingVendorComponent {

    private static final Log LOGGER = LogFactory.getLog(CloudBillingVendorComponent.class);


    protected void activate(ComponentContext context) {
        LOGGER.info("###################### Billing Vendor Component activated. ##########################");

    }

    protected void deactivate(ComponentContext context) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("CloudUserStoreComponent is deactivated ");
        }
    }


    public void setSecretCallbackHandlerService(SecretCallbackHandlerService secretCallbackHandlerService) {
        ServiceDataHolder.getInstance().setSecretCallbackHandlerService(secretCallbackHandlerService);
    }

    public void unsetSecretCallbackHandlerService(SecretCallbackHandlerService secretCallbackHandlerService) {
        ServiceDataHolder.getInstance().setSecretCallbackHandlerService(null);
    }
}
