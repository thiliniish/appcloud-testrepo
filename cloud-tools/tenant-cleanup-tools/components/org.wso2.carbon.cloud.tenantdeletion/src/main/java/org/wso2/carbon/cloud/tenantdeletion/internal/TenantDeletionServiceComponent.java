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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.cloud.tenantdeletion.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cloud.tenantdeletion.listeners.CloudUserOperationListener;
import org.wso2.carbon.cloud.tenantdeletion.listeners.UserStoreConfgurationContextObserver;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;

/**
 * @scr.component name="wso2.cloud.tenantdeletion" immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService" cardinality="1..1"
 * policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 */

public class TenantDeletionServiceComponent {
	private final static Log logger = LogFactory.getLog(TenantDeletionServiceComponent.class);

	protected void activate(ComponentContext ctx) {
		try {
			CloudUserOperationListener cloudUserOperationListener = new CloudUserOperationListener();
			UserStoreConfgurationContextObserver userStoreConfgurationContextObserver = new
					UserStoreConfgurationContextObserver();
			ctx.getBundleContext().registerService(UserOperationEventListener.class.getName(),
					cloudUserOperationListener, null);
			ctx.getBundleContext().registerService(Axis2ConfigurationContextObserver.class.getName(),
					userStoreConfgurationContextObserver, null);
		} catch (Exception e) {
			logger.error("Failed to activate the Tenant Deletion service.", e);
		}
		logger.info("Tenant Deletion service component activated successfully.");
	}

	protected void setRealmService(RealmService realmService) {
		ServiceHolder.getInstance().setRealmService(realmService);
		logger.debug("Realm service initialized");
	}

	protected void unsetRealmService(RealmService realmService) {
		ServiceHolder.getInstance().setRealmService(null);
	}
}
