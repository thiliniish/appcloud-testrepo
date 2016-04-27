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

package org.wso2.carbon.cloud.rolemgt.tool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.cloud.rolemgt.tool.internal.RoleMgtConfigurationBuilder;
import org.wso2.carbon.cloud.rolemgt.tool.internal.ServiceHolder;
import org.wso2.carbon.cloud.rolemgt.tool.util.RoleBean;
import org.wso2.carbon.cloud.rolemgt.tool.util.RoleManagerConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.Permission;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents the thread which updates role configurations
 */
public class RoleManager implements Runnable {

    private static final Log log = LogFactory.getLog(RoleManager.class);

    /**
     * Method to override run
     */
    @Override public void run() {
        manage();
        if (log.isDebugEnabled()) {
            log.debug("Role Manager started Successfully.");
        }
    }

    /**
     * Method to update manage tenant roles
     *
     * @throws StratosException
     */
    public void manage() {
        if (log.isDebugEnabled()) {
            log.debug("Starting the process to update tenant roles during server start up.");
        }
        //Get role details to be updated
        Set<RoleBean> roleBeanList = new HashSet<RoleBean>();

        roleBeanList.addAll(getRoleConfigurations(RoleManagerConstants.TENANT_ROLES_ROLE));
        if (roleBeanList.isEmpty()) {
            log.error("Please update role-mgt.xml with role configurations to be updated");
        }
        updateRoles(roleBeanList);
    }

    /**
     * Method to get Role Configurations
     *
     * @param roleConfigPath String
     * @return Set of RoleBean
     */
    private static Set<RoleBean> getRoleConfigurations(String roleConfigPath) {
        Set<RoleBean> roleBeanList = new HashSet<RoleBean>();
        RoleMgtConfigurationBuilder configBuilder = new RoleMgtConfigurationBuilder();
        //Reads role-mgt.xml file into a map of list of strings
        configBuilder.buildRoleMgtConfiguration();
        String[] roles = configBuilder.getProperties(roleConfigPath);
        if (roles == null || roles.length == 0) {
            log.warn("No roles are configured for " + roleConfigPath + " in role-mgt.xml");
        } else {
            for (String role : roles) {
                String permissionIdString = configBuilder.getFirstProperty(roleConfigPath + "." + role +
                        ".Permission");
                String[] permissionIds = permissionIdString.split(",");
                //Setting Role Name
                RoleBean roleBean = new RoleBean(role.trim());
                //Setting Permissions
                for (String permissionId : permissionIds) {
                    permissionId = permissionId.trim();
                    boolean isDeniedPermission = permissionId.startsWith(RoleManagerConstants.DENY);
                    if (isDeniedPermission) {
                        permissionId = permissionId
                                .substring(RoleManagerConstants.DENY.length(), permissionId.length());
                    }

                    String[] resourceAndActionParts = permissionId.split(":");
                    if (resourceAndActionParts.length == 2) {
                        Permission permission = new Permission(resourceAndActionParts[0],
                                replaceRegistryPermissionAction(resourceAndActionParts[1]));
                        roleBean.addPermission(permission, !isDeniedPermission);

                    } else if (resourceAndActionParts.length == 1) {
                        Permission permission = new Permission(resourceAndActionParts[0],
                                CarbonConstants.UI_PERMISSION_ACTION);
                        roleBean.addPermission(permission, !isDeniedPermission);
                    }
                }
                //Setting Role action
                String roleActionString = configBuilder.getFirstProperty(roleConfigPath + "." + role + ".Action");
                roleBean.setAction(roleActionString);
                roleBeanList.add(roleBean);
            }
        }
        return roleBeanList;
    }

    /**
     * Method to replace registry action constants with short action names, to avoid urls as action
     *
     * @param action - REGISTRY_GET,REGISTRY_PUT,REGISTRY_DELETE or any other action
     * @return - replaced permission action for REGISTRY_ACTION
     */
    private static String replaceRegistryPermissionAction(String action) {
        if (RoleManagerConstants.REGISTRY_GET.equals(action)) {
            return ActionConstants.GET;
        } else if (RoleManagerConstants.REGISTRY_PUT.equals(action)) {
            return ActionConstants.PUT;
        } else if (RoleManagerConstants.REGISTRY_DELETE.equals(action)) {
            return ActionConstants.DELETE;
        } else {
            return action;
        }
    }

    /**
     * Method to update Roles
     *
     * @param roleBeanList Set of RoleBean
     * @throws StratosException
     */
    private static void updateRoles(Set<RoleBean> roleBeanList) {
        TenantManager tenantManager = ServiceHolder.getRealmService().getTenantManager();
        Tenant[] tenants = null;
        try {
            tenants = tenantManager.getAllTenants();
        } catch (UserStoreException e) {
            String message = "Failed to get all tenants from tenant manager.";
            log.error(message);
        }
        //<TODO>starting log
        if (tenants == null) {
            log.info("There are no tenants to be updated.");
        } else {
            for (Tenant tenant : tenants) {
                //Start a new tenant flow
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenant.getDomain());
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenant.getId());
                //Services are loaded from the service holder
                ServiceHolder.getTenantRegLoader().loadTenantRegistry(tenant.getId());
                try {
                    UserStoreManager userStoreManager = PrivilegedCarbonContext.getThreadLocalCarbonContext().
                            getUserRealm().getUserStoreManager();
                    AuthorizationManager authorizationManager = PrivilegedCarbonContext.getThreadLocalCarbonContext().
                            getUserRealm().getAuthorizationManager();
                    updateRolesPerTenant(userStoreManager, authorizationManager, roleBeanList);

                } catch (UserStoreException e) {
                    String message =
                            "Failed to update roles of tenant : " + tenant.getDomain() + "[" + tenant.getId() + "]";
                    log.error(message);
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                    if (log.isDebugEnabled()) {
                        log.debug("Role updation process is completed for tenant: " + tenant.getDomain() + "[" + tenant
                                .getId() + "]");
                    }
                }
            }
            log.info("Role updation process completed for all existing tenants.");
        }
    }

    /**
     * Method to update roles per tenant
     *
     * @param userStoreManager     UserStoreManager
     * @param authorizationManager AuthorizationManager
     * @param roleBeanList         Set of RoleBean
     * @throws UserStoreException
     */
    private static void updateRolesPerTenant(UserStoreManager userStoreManager,
            AuthorizationManager authorizationManager, Set<RoleBean> roleBeanList) throws UserStoreException {

        Boolean isRoleAddition = false, isRoleUpdation = false, isRoleDeletion = false;
        for (RoleBean roleBean : roleBeanList) {
            if (RoleManagerConstants.ROLE_ADDITION.equals(roleBean.getAction())) {
                isRoleAddition = true;
            } else if (RoleManagerConstants.ROLE_UPDATION.equals(roleBean.getAction())) {
                isRoleUpdation = true;
            } else if (RoleManagerConstants.ROLE_DELETION.equals(roleBean.getAction())) {
                isRoleDeletion = true;
            }

            if (isRoleDeletion) {
                userStoreManager.deleteRole(roleBean.getRoleName());
                continue;
            } else if (isRoleAddition && !userStoreManager.isExistingRole(roleBean.getRoleName())) {
                // add role and authorize given authorized permission list
                userStoreManager.addRole(roleBean.getRoleName(), roleBean.getUsers().toArray(new String[0]),
                        roleBean.getPermissions(true).toArray(new Permission[0]));
                if (log.isDebugEnabled()) {
                    StringBuilder permissionLog = new StringBuilder(
                            "Role:" + roleBean.getRoleName() + " is added with below permissions;");
                    List<Permission> permissions = roleBean.getPermissions(true);
                    for (Permission permission : permissions) {
                        permissionLog.append("resource:").append(permission.getResourceId()).append(" action:")
                                .append(permission.getAction()).append("\n");
                    }
                    log.debug(permissionLog.toString());
                }
            } else if (isRoleAddition || isRoleUpdation) {
                // authorize given authorized permission list
                for (Permission permission : roleBean.getPermissions(true)) {
                    if (!authorizationManager.isRoleAuthorized(roleBean.getRoleName(), permission.getResourceId(),
                            permission.getAction())) {
                        authorizationManager.authorizeRole(roleBean.getRoleName(), permission.getResourceId(),
                                permission.getAction());
                        if (log.isDebugEnabled()) {
                            log.debug("Role:" + roleBean.getRoleName() + " is authorized with permission;\n" +
                                    "resource:" + permission.getResourceId() + " action:" + permission.getAction() +
                                    "\n");
                        }
                    }
                }
            } else {
                String message = "The specified action '" + roleBean.getAction() + "' is not a valid action.";
                log.error(message);
                continue;
            }
            // deny given denied permission list for new additions and updations
            for (Permission permission : roleBean.getPermissions(false)) {
                authorizationManager
                        .denyRole(roleBean.getRoleName(), permission.getResourceId(), permission.getAction());
                if (log.isDebugEnabled()) {
                    log.debug("Role:" + roleBean.getRoleName() + " is denied with permissions;\n" +
                            "resource:" + permission.getResourceId() + " action:" + permission.getAction() + "\n");
                }
            }
        }//End of for loop iterating roleBean List
    }
}

