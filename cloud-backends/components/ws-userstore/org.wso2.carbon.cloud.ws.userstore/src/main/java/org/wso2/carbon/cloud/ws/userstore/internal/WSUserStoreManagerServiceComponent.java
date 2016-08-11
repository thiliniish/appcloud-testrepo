/*
*  Copyright (c) 2005-2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.cloud.ws.userstore.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cloud.ws.userstore.CloudWSUserStoreManager;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="cloud.ws.user.store.manager" immediate=true
 */

public class WSUserStoreManagerServiceComponent {
    private static Log log = LogFactory.getLog(WSUserStoreManagerServiceComponent.class);
    private static RealmService realmService;


    protected void activate(ComponentContext ctxt) {
        try {
            UserStoreManager wsUserStoreManager = new CloudWSUserStoreManager();
            ctxt.getBundleContext().registerService(UserStoreManager.class.getName(), wsUserStoreManager, null);
        } catch (Exception e) {
            log.error("Failed to activate Cloud WS User Store Manager ", e);
        }
        log.info("Successfully activated Cloud WS User Store Manager");
    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Carbon UserStoreMgtDSComponent is deactivated ");
        }
    }

}
