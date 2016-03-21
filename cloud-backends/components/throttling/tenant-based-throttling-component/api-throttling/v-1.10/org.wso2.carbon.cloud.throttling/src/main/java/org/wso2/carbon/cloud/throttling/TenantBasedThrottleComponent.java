/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License
 */
package org.wso2.carbon.cloud.throttling;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cloud.throttling.common.CloudThrottlingException;
import org.wso2.carbon.cloud.throttling.internal.ThrottleDataHolder;

/**
 * This is a service component class
 * Sole purpose of this class is to act as the component for the osgi bundle.
 *
 * @scr.component name="org.wso2.carbon.cloud.throttling" immediate="true"
 */
public class TenantBasedThrottleComponent {
    private static final Log LOG = LogFactory.getLog(TenantBasedThrottleComponent.class);

    /*
    * activate() method that is invoked at the bundle activation.
    * */
    protected void activate(ComponentContext context) {
        LOG.debug("Cloud throttle Service  component is activating.");
        try {
            initThrottleHandler();
            LOG.debug("Cloud throttle Service  component is activated.");
        } catch (CloudThrottlingException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /*
    * deactivate() method that is invoked at the bundle deactivation
    * */
    protected void deactivate(ComponentContext context) {
        ThrottleDataHolder.getInstance().stopCacheCleaner();
        LOG.debug("Cloud throttle Service component is deactivated.");
    }

    /*
    * This method does all the initialization tasks at the bundle actication
    * and update the DataHolder appropriately.
    * @throws CloudThrottlingException
    *
    * */
    private void initThrottleHandler() throws CloudThrottlingException {
        ThrottleDataHolder.getInstance().initCacheCleaner();
        ThrottleDataHolder.getInstance().initThrottleOutHandler();
    }
}
