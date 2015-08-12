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

package org.wso2.carbon.cloud.tenant.creation.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.cloud.common.CloudMgtConfiguration;
import org.wso2.carbon.cloud.common.CloudMgtConstants;
import org.wso2.carbon.cloud.common.CloudMgtException;
import org.wso2.carbon.cloud.tenant.creation.listener.internal.ServiceHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.Permission;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CloudTenantCreationListener implements TenantMgtListener {
    private static final Log log = LogFactory.getLog(CloudTenantCreationListener.class);

    public void onTenantCreate(TenantInfoBean tenantInfoBean) throws StratosException {
        log.info("Adding Tenant Roles on tenant creation.");
        Set<RoleBean> roleBeanList = new HashSet<RoleBean>();
        try {
            roleBeanList.addAll(getRolePermissionConfigurations(CloudMgtConstants.TENANT_ROLES_ROLE, tenantInfoBean.getAdmin()));
        } catch (CloudMgtException e) {
            String message = "Failed to read default roles from cloud-mgt configuration.";
            log.error(message);
            throw new StratosException(message, e);
        }

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            privilegedCarbonContext.setTenantId(tenantInfoBean.getTenantId());
            privilegedCarbonContext.setTenantDomain(tenantInfoBean.getTenantDomain());
            UserStoreManager userStoreManager = PrivilegedCarbonContext.getThreadLocalCarbonContext().
                    getUserRealm().getUserStoreManager();
            AuthorizationManager authorizationManager = PrivilegedCarbonContext.getThreadLocalCarbonContext().
                    getUserRealm().getAuthorizationManager();
            addRolePermissions(userStoreManager, authorizationManager, roleBeanList);

        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            String message = "Failed to add default roles to tenant : " + tenantInfoBean.getTenantDomain()
                    + "(" + tenantInfoBean.getTenantId() + ")";
            log.error(message);
            throw new StratosException(message, e);
        } catch (UserStoreException e) {
            String message = "Failed to add default roles to tenant : " + tenantInfoBean.getTenantDomain()
                    + "(" + tenantInfoBean.getTenantId() + ")";
            log.error(message);
            throw new StratosException(message, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

    }

    public void onTenantUpdate(TenantInfoBean tenantInfoBean) throws StratosException {

    }

    public void onTenantRename(int i, String s, String s1) throws StratosException {

    }

    public void onTenantInitialActivation(int i) throws StratosException {

    }

    public void onTenantActivation(int i) throws StratosException {

    }

    public void onTenantDeactivation(int i) throws StratosException {

    }

    public void onSubscriptionPlanChange(int i, String s, String s1) throws StratosException {

    }

    public int getListenerOrder() {
        return 0;
    }

    private static Set<RoleBean> getRolePermissionConfigurations(String rolePermissionConfigPath, String defaultUser)
            throws CloudMgtException {
        Set<RoleBean> roleBeanList = new HashSet<RoleBean>();
        CloudMgtConfiguration configuration = ServiceHolder.getCloudConfiguration();
        String[] roles = configuration.getProperties(rolePermissionConfigPath);
        if (roles == null || roles.length == 0) {
            log.warn("No roles permissions are configured for " + rolePermissionConfigPath + " path in cloud-mgt.xml");
        } else {
            for (String role : roles) {
                String permissionIdString =
                        configuration.getFirstProperty(rolePermissionConfigPath + "." + role +
                                ".Permission");
                String[] permissionIds = permissionIdString.split(",");
                RoleBean roleBean = new RoleBean(role.trim());
                roleBean.addUser(defaultUser);
                for (String permissionId : permissionIds) {
                    permissionId = permissionId.trim();
                    boolean isDeniedPermission = permissionId.startsWith(CloudMgtConstants.DENY);
                    if (isDeniedPermission) {
                        permissionId = permissionId.substring(CloudMgtConstants.DENY.length(), permissionId.length());
                    }

                    String[] resourceAndActionParts = permissionId.split(":");
                    if (resourceAndActionParts.length == 2) {
                        Permission permission =
                                new Permission(
                                        resourceAndActionParts[0],
                                        replaceRegistryPermissionAction(resourceAndActionParts[1]));
                        roleBean.addPermission(permission, !isDeniedPermission);

                    } else if (resourceAndActionParts.length == 1) {
                        Permission permission =
                                new Permission(resourceAndActionParts[0],
                                        CarbonConstants.UI_PERMISSION_ACTION);
                        roleBean.addPermission(permission, !isDeniedPermission);
                    }
                }
                roleBeanList.add(roleBean);
            }
        }

        return roleBeanList;
    }

    /**
     * This is to replace registry action constants with short action names, to avoid urls as action
     *
     * @param action - REGISTRY_GET,REGISTRY_PUT,REGISTRY_DELETE or any other action
     * @return - replaced permission action for REGISTRY_ACTION
     */
    private static String replaceRegistryPermissionAction(String action) {
        if (CloudMgtConstants.REGISTRY_GET.equals(action)) {
            return ActionConstants.GET;
        } else if (CloudMgtConstants.REGISTRY_PUT.equals(action)) {
            return ActionConstants.PUT;
        } else if (CloudMgtConstants.REGISTRY_DELETE.equals(action)) {
            return ActionConstants.DELETE;
        } else {
            return action;
        }
    }

    private static void addRolePermissions(UserStoreManager userStoreManager, AuthorizationManager authorizationManager,
                                           Set<RoleBean> roleBeanList) throws UserStoreException {
        for (RoleBean roleBean : roleBeanList) {
            if (!userStoreManager.isExistingRole(roleBean.getRoleName())) {
                // add role and authorize given authorized permission list
                userStoreManager.addRole(roleBean.getRoleName(),
                        roleBean.getUsers().toArray(new String[roleBean.getUsers().size()]),
                        roleBean.getPermissions(true)
                                .toArray(new Permission[roleBean.getPermissions(true).size()]));
                if (log.isDebugEnabled()) {
                    StringBuilder permissionLog = new StringBuilder("Role:" + roleBean.getRoleName()
                            + " is added with below permissions;");
                    List<Permission> permissions = roleBean.getPermissions(true);
                    for (Permission permission : permissions) {
                        permissionLog.append("resource:").append(permission.getResourceId()).append(" action:")
                                .append(permission.getAction()).append("\n");
                    }
                    log.debug(permissionLog.toString());
                }
            } else {
                // authorize given authorized permission list
                for (Permission permission : roleBean.getPermissions(true)) {
                    if (!authorizationManager.isRoleAuthorized(roleBean.getRoleName(), permission.getResourceId(),
                            permission.getAction())) {
                        authorizationManager.authorizeRole(roleBean.getRoleName(), permission.getResourceId(),
                                permission.getAction());
                        if (log.isDebugEnabled()) {
                            log.debug("Role:" + roleBean.getRoleName() + " is authorized with permission;\n" +
                                    "resource:" + permission.getResourceId() + " action:" + permission.getAction() + "\n");
                        }
                    }
                }
            }

            // deny given denied permission list
            for (Permission permission : roleBean.getPermissions(false)) {
                authorizationManager.denyRole(roleBean.getRoleName(), permission.getResourceId(),
                        permission.getAction());
                if (log.isDebugEnabled()) {
                    log.debug("Role:" + roleBean.getRoleName() + " is denied with permissions;\n" +
                            "resource:" + permission.getResourceId() + " action:" + permission.getAction() + "\n");
                }
            }
        }

    }
}
