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
import org.wso2.carbon.user.api.*;
import org.wso2.carbon.user.core.Permission;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
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
     */
    public void manage() {
        //Get role details to be updated
        Set<RoleBean> roleBeanList = new HashSet<RoleBean>();
        roleBeanList.addAll(getRoleConfigurations(RoleManagerConstants.TENANT_ROLES_ROLE));
        if (roleBeanList.isEmpty()) {
            log.warn("Please update " + RoleManagerConstants.CONFIG_FILE_NAME + " with role configurations to be "
                    + "updated.");
            return;
        }
        updateRoles(roleBeanList);
    }

    /**
     * Method to update Roles
     *
     * @param roleBeanList Set of RoleBean
     */
    private static void updateRoles(Set<RoleBean> roleBeanList) {
        TenantManager tenantManager = ServiceHolder.getRealmService().getTenantManager();
        Tenant[] tenants;
        try {
            tenants = tenantManager.getAllTenants();
        } catch (UserStoreException e) {
            String message = "Failed to get all tenants from tenant manager.";
            log.error(message);
            return;
        }
        if (tenants == null || tenants.length == 0) {
            if (log.isDebugEnabled()) {
                log.debug("Exiting as no tenants are available to be updated.");
            }
        } else {
            //Get the user specified range of tenants to update
            //Specify it as -Drange=lowerbound:upperBound
            String range = System.getProperty(RoleManagerConstants.TENANT_RANGE);
            boolean isRangeSpecified = false;
            int noOfTenantsToUpdate = 0;
            int lowerBound = 0;
            int upperBound = 0;
            if (range != null) {
                String[] bounds = range.split(":");
                if (bounds.length >= 2) {
                    lowerBound = Integer.parseInt(bounds[0]);
                    upperBound = Integer.parseInt(bounds[1]);
                    isRangeSpecified = true;
                    noOfTenantsToUpdate = upperBound + 1 - lowerBound;
                }
            } else {
                noOfTenantsToUpdate = tenants.length;
            }
            log.info("Starting the process to update tenant roles during server start up. " + noOfTenantsToUpdate +
                    " tenants will be updated.");
            int updatedTenantCount = 0;
            boolean isSuccessful = false;
            for (Tenant tenant : tenants) {
                int tenantId = tenant.getId();
                String tenantDomain = tenant.getDomain();
                //Check if tenant is within the specified range or ignore if a range is not specified
                if (((tenantId >= lowerBound) && (tenantId <= upperBound)) || !isRangeSpecified) {
                    //Start a new tenant flow
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
                    //Services are loaded from the service holder
                    ServiceHolder.getTenantRegLoader().loadTenantRegistry(tenantId);
                    try {
                        UserStoreManager userStoreManager = PrivilegedCarbonContext.getThreadLocalCarbonContext().
                                getUserRealm().getUserStoreManager();
                        String tenantAdmin = PrivilegedCarbonContext.getThreadLocalCarbonContext().
                                getUserRealm().getRealmConfiguration().getAdminUserName();
                        AuthorizationManager authorizationManager = PrivilegedCarbonContext
                                .getThreadLocalCarbonContext().
                                        getUserRealm().getAuthorizationManager();
                        isSuccessful = updateRolesPerTenant(userStoreManager, authorizationManager, roleBeanList,
                                tenantDomain, tenantAdmin);
                        if (isSuccessful){
                            //add to the count of successfully updated tenants
                            updatedTenantCount++;
                            log.info("Role update process is completed for tenant: " + tenantDomain + "[" + tenant
                                    .getId() + "]");
                        }
                    } catch (UserStoreException e) {
                        String message =
                                "Failed to update roles of tenant: " + tenantDomain + "[" + tenantId + "]";
                        log.error(message, e);
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug(
                                "Skipping the role update for " + tenantDomain + "[" + tenantId + "] as it "
                                        + "is not in specified range " + lowerBound + ":" + upperBound);
                    }
                }
            }
            log.info("Role update process completed for " + updatedTenantCount + " tenants.");
        }
    }

    /**
     * Method to get Role Configurations
     *
     * @param roleConfigPath String
     * @return Set of RoleBean
     */
    private static Set<RoleBean> getRoleConfigurations(String roleConfigPath) {
        Set<RoleBean> roleBeanList = new HashSet<RoleBean>();
        //Get XML configuration file
        String roleMgtConfigFileLocation = CarbonUtils.getCarbonConfigDirPath() +
                File.separator + RoleManagerConstants.CONFIG_FOLDER +
                File.separator + RoleManagerConstants.CONFIG_FILE_NAME;
        File roleConfigFile = new File(roleMgtConfigFileLocation);
        if (!roleConfigFile.exists()) {
            log.error("Unable to load configuration file. Please check whether '" + roleMgtConfigFileLocation +
                    "' exists!");
            return roleBeanList;
        }
        RoleMgtConfigurationBuilder configBuilder = new RoleMgtConfigurationBuilder(roleConfigFile);
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
     * @return String replaced permission action for REGISTRY_ACTION
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
     * Method to update roles per tenant
     *
     * @param userStoreManager     UserStoreManager
     * @param authorizationManager AuthorizationManager
     * @param roleBeanList         Set of RoleBean
     * @throws UserStoreException
     */
    private static boolean updateRolesPerTenant(UserStoreManager userStoreManager,
            AuthorizationManager authorizationManager, Set<RoleBean> roleBeanList, String tenantDomain, String
            tenantAdmin) {
        boolean isSuccessful = false;
        boolean isAuthorizedPermissions;
        boolean isRoleAdd;
        boolean isRoleUpdate;
        boolean isRoleDelete;
        for (RoleBean roleBean : roleBeanList) {
            //Initialize the variables for role
            isRoleAdd = false;
            isRoleUpdate = false;
            isRoleDelete = false;
            if (RoleManagerConstants.ROLE_ADD.equals(roleBean.getAction())) {
                isRoleAdd = true;
                //Set tenant admin as default user
                roleBean.addUser(tenantAdmin);
            } else if (RoleManagerConstants.ROLE_UPDATE.equals(roleBean.getAction())) {
                isRoleUpdate = true;
            } else if (RoleManagerConstants.ROLE_DELETE.equals(roleBean.getAction())) {
                isRoleDelete = true;
            }
            try {
                if (isRoleDelete) {
                    if (userStoreManager.isExistingRole(roleBean.getRoleName())) {
                        userStoreManager.deleteRole(roleBean.getRoleName());
                        isSuccessful = true;
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("The role '" + roleBean.getRoleName() + "' does not exist or has already been "
                                    + "deleted for tenant: " + tenantDomain);
                        }
                    }
                    continue;
                } else if ((isRoleAdd || isRoleUpdate) && userStoreManager.isExistingRole(roleBean.getRoleName())) {
                    isAuthorizedPermissions = true;
                    // authorize given authorized permission list
                    for (Permission permission : roleBean.getPermissions(isAuthorizedPermissions)) {
                        if (!authorizationManager.isRoleAuthorized(roleBean.getRoleName(), permission.getResourceId(),
                                permission.getAction())) {
                            authorizationManager.authorizeRole(roleBean.getRoleName(), permission.getResourceId(),
                                    permission.getAction());
                            if (log.isDebugEnabled()) {
                                log.debug("Role:" + roleBean.getRoleName() + " is authorized with permission;\n" +
                                        RoleManagerConstants.RESOURCE + permission.getResourceId() + " action:"
                                        + permission.getAction() +
                                        "\n");
                            }
                        }
                    }
                } else if (isRoleAdd) {
                    isAuthorizedPermissions = true;
                    // add role and authorize given authorized permission list
                    userStoreManager.addRole(roleBean.getRoleName(), roleBean.getUsers().toArray(new String[0]),
                            roleBean.getPermissions(isAuthorizedPermissions).toArray(new Permission[0]));

                    if (log.isDebugEnabled()) {
                        StringBuilder permissionLog = new StringBuilder(
                                "Role:" + roleBean.getRoleName() + " is added with below permissions;");
                        List<Permission> permissions = roleBean.getPermissions(isAuthorizedPermissions);
                        for (Permission permission : permissions) {
                            permissionLog.append(RoleManagerConstants.RESOURCE).append(permission.getResourceId())
                                    .append(" " + "action:").append(permission.getAction()).append("\n");
                        }
                        log.debug(permissionLog.toString());
                    }
                } else {
                    String message =
                            "The specified action '" + roleBean.getAction() + "' is not a valid action to " + "update"
                                    + " the role '" + roleBean.getRoleName() + "' in tenant: " + tenantDomain;
                    log.error(message);
                    continue;
                }
                isAuthorizedPermissions = false;
                // deny given denied permission list for new additions and updates
                for (Permission permission : roleBean.getPermissions(isAuthorizedPermissions)) {
                    authorizationManager
                            .denyRole(roleBean.getRoleName(), permission.getResourceId(), permission.getAction());
                    if (log.isDebugEnabled()) {
                        log.debug("Role:" + roleBean.getRoleName() + " is denied with permissions;\n" +
                                RoleManagerConstants.RESOURCE + permission.getResourceId() + " action:" + permission
                                .getAction() + "\n");
                    }
                }
                isSuccessful = true;
            } catch (UserStoreException e) {
                log.error("An error occurred while updating role '" + roleBean.getRoleName() + " in tenant: "
                        + tenantDomain, e);
            }
        }//End of for loop iterating roleBean List
        return isSuccessful;
    }
}
