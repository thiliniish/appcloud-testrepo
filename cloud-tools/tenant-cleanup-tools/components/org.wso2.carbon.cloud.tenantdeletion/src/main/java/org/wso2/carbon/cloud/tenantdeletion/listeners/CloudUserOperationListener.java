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

package org.wso2.carbon.cloud.tenantdeletion.listeners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.cloud.tenantdeletion.internal.ServiceHolder;
import org.wso2.carbon.cloud.tenantdeletion.utils.DataAccessManager;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Represents class for Tenant Login Listener.
 */
public class CloudUserOperationListener implements UserOperationEventListener {

	private final static Log log = LogFactory.getLog(DataAccessManager.class);

	@Override
	public boolean doPreAuthenticate(java.lang.String string, java.lang.Object object,
	                                 UserStoreManager userStoreManager) throws UserStoreException {
		return true;
	}

	/**
	 * records data to the database when a user login happens
	 * @param username         String
	 * @param b                boolean
	 * @param userStoreManager UserStoreManager
	 * @return true
	 * @throws UserStoreException
	 */
	@Override
	public boolean doPostAuthenticate(String username, boolean b, UserStoreManager userStoreManager)
			throws UserStoreException {
		Date currentTime = Calendar.getInstance().getTime();
		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
		DataAccessManager dbAccessor = new DataAccessManager();
		int tenantId = userStoreManager.getTenantId();
		if (MultitenantConstants.SUPER_TENANT_ID != tenantId) {
			try {
				String tenantDomain = ServiceHolder.getInstance().getRealmService().getTenantManager().getTenant
						(tenantId).getDomain();
				dbAccessor.updateUserLogin(tenantId, tenantDomain, date.format(currentTime));
			} catch (org.wso2.carbon.user.api.UserStoreException e) {
				log.error("UserStoreException has been occurred", e);
			}
		} else {
			dbAccessor.updateUserLogin(MultitenantConstants.SUPER_TENANT_ID, MultitenantConstants
					.SUPER_TENANT_DOMAIN_NAME, date.format(currentTime));
		}
		return true;
	}

	@Override
	public boolean doPreAddUser(java.lang.String string, java.lang.Object object, java.lang.String[] strings,
	                            java.util.Map<java.lang.String, java.lang.String> stringStringMap,
	                            java.lang.String string2, UserStoreManager userStoreManager) throws
			UserStoreException {
		return true;
	}

	@Override
	public boolean doPostAddUser(java.lang.String string, java.lang.Object object, java.lang.String[] strings,
	                             java.util.Map<java.lang.String, java.lang.String> stringStringMap,
	                             java.lang.String string2, UserStoreManager userStoreManager)
			throws UserStoreException {
		return true;
	}

	@Override
	public boolean doPreUpdateCredential(java.lang.String string, java.lang.Object object, java.lang.Object object2,
	                                     UserStoreManager userStoreManager) throws UserStoreException {
		return true;
	}

	@Override
	public boolean doPostUpdateCredential(java.lang.String string, java.lang.Object object,
	                                      UserStoreManager userStoreManager) throws UserStoreException {
		return true;
	}

	@Override
	public boolean doPreUpdateCredentialByAdmin(java.lang.String string, java.lang.Object object,
	                                            UserStoreManager userStoreManager) throws UserStoreException {
		return true;
	}

	@Override
	public boolean doPostUpdateCredentialByAdmin(java.lang.String string, java.lang.Object object,
	                                             UserStoreManager userStoreManager) throws UserStoreException {
		return true;
	}

	@Override
	public boolean doPreDeleteUser(java.lang.String string, UserStoreManager userStoreManager)
			throws UserStoreException {
		return true;
	}

	@Override
	public boolean doPostDeleteUser(java.lang.String string, UserStoreManager userStoreManager)
			throws UserStoreException {
		return true;
	}

	@Override
	public boolean doPreSetUserClaimValue(java.lang.String string, java.lang.String string2, java.lang.String string3,
	                                      java.lang.String string4, UserStoreManager userStoreManager)
			throws UserStoreException {
		return true;
	}

	@Override
	public boolean doPostSetUserClaimValue(java.lang.String string, UserStoreManager userStoreManager)
			throws UserStoreException {
		return true;
	}

	@Override
	public boolean doPreSetUserClaimValues(java.lang.String string,
	                                       java.util.Map<java.lang.String, java.lang.String> stringStringMap,
	                                       java.lang.String string2, UserStoreManager userStoreManager)
			throws UserStoreException {
		return true;
	}

	@Override
	public int getExecutionOrderId() {
		return 3;
	}

	public boolean doPostSetUserClaimValues(java.lang.String string,
	                                        java.util.Map<java.lang.String, java.lang.String> stringStringMap,
	                                        java.lang.String string2, UserStoreManager userStoreManager)
			throws UserStoreException {
		return true;
	}

	@Override
	public boolean doPreDeleteUserClaimValues(java.lang.String string, java.lang.String[] strings,
	                                          java.lang.String string2, UserStoreManager userStoreManager)
			throws UserStoreException {
		return true;
	}

	@Override
	public boolean doPostDeleteUserClaimValues(java.lang.String string, UserStoreManager userStoreManager)
			throws UserStoreException {
		return true;
	}

	@Override
	public boolean doPreDeleteUserClaimValue(java.lang.String string, java.lang.String string2,
	                                         java.lang.String string3, UserStoreManager userStoreManager)
			throws UserStoreException {
		return true;
	}

	@Override
	public boolean doPostDeleteUserClaimValue(java.lang.String string, UserStoreManager userStoreManager)
			throws UserStoreException {
		return true;
	}

	@Override
	public boolean doPreAddRole(java.lang.String string, java.lang.String[] strings, Permission[] permissions,
	                            UserStoreManager userStoreManager) throws UserStoreException {
		return true;
	}

	@Override
	public boolean doPostAddRole(java.lang.String string, java.lang.String[] strings, Permission[] permissions,
	                             UserStoreManager userStoreManager) throws UserStoreException {
		return true;
	}

	@Override
	public boolean doPreDeleteRole(java.lang.String string, UserStoreManager userStoreManager)
			throws UserStoreException {
		return true;
	}

	@Override
	public boolean doPostDeleteRole(java.lang.String string, UserStoreManager userStoreManager)
			throws UserStoreException {
		return true;
	}

	@Override
	public boolean doPreUpdateRoleName(java.lang.String string, java.lang.String string2,
	                                   UserStoreManager userStoreManager) throws UserStoreException {
		return true;
	}

	@Override
	public boolean doPostUpdateRoleName(java.lang.String string, java.lang.String string2,
	                                    UserStoreManager userStoreManager) throws UserStoreException {
		return true;
	}

	@Override
	public boolean doPreUpdateUserListOfRole(java.lang.String string, java.lang.String[] strings,
	                                         java.lang.String[] strings2, UserStoreManager userStoreManager)
			throws UserStoreException {
		return true;
	}

	@Override
	public boolean doPostUpdateUserListOfRole(java.lang.String string, java.lang.String[] strings,
	                                          java.lang.String[] strings2, UserStoreManager userStoreManager)
			throws UserStoreException {
		return true;
	}

	@Override
	public boolean doPreUpdateRoleListOfUser(java.lang.String string, java.lang.String[] strings,
	                                         java.lang.String[] strings2, UserStoreManager userStoreManager)
			throws UserStoreException {
		return true;
	}

	@Override
	public boolean doPostUpdateRoleListOfUser(java.lang.String string, java.lang.String[] strings,
	                                          java.lang.String[] strings2, UserStoreManager userStoreManager)
			throws UserStoreException {
		return true;
	}
}