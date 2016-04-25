package org.wso2.carbon.cloud.rolemgt.tool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.cloud.rolemgt.common.RoleMgtConfiguration;
import org.wso2.carbon.cloud.rolemgt.common.RoleMgtException;
import org.wso2.carbon.cloud.rolemgt.tool.internal.ServiceHolder;
import org.wso2.carbon.cloud.rolemgt.tool.util.RoleBean;
import org.wso2.carbon.cloud.rolemgt.common.RoleMgtConstants;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by dilhasha on 4/25/16.
 */
public class RoleManager implements Runnable {

    private static final Log log = LogFactory.getLog(RoleManager.class);

    @Override public void run() {
        RoleManager roleManager = new RoleManager();
        log.info("Role Manager started.");
        try {
            manage();
        } catch (StratosException e) {
            log.error("Error occurred while updating roles.", e);
        }

    }

    public void manage() throws StratosException {
        //Get role details to be updated
        log.info("Updating tenant roles during server start up.");
        Set<RoleBean> roleBeanList = new HashSet<RoleBean>();
        try {
            roleBeanList.addAll(getRoleConfigurations(RoleMgtConstants.TENANT_ROLES_ROLE));
        } catch (RoleMgtException e) {
            String message = "Failed to read role updates from role-mgt configuration.";
            log.error(message);
            throw new StratosException(message, e);
        }
        if(roleBeanList.isEmpty()){
            log.error("Please update role-mgt.xml with role details to be updated");
        }
        updateRoles(roleBeanList);
    }

    private static Set<RoleBean> getRoleConfigurations(String roleConfigPath)
            throws RoleMgtException {
        Set<RoleBean> roleBeanList = new HashSet<RoleBean>();
        RoleMgtConfiguration configuration = ServiceHolder.getRoleConfiguration();
        String[] roles = configuration.getProperties(roleConfigPath);
        if (roles == null || roles.length == 0) {
            log.warn("No roles are configured for " + roleConfigPath + " in role-mgt.xml");
        } else {
            for (String role : roles) {
                String permissionIdString =
                        configuration.getFirstProperty(roleConfigPath + "." + role +
                                ".Permission");
                String[] permissionIds = permissionIdString.split(",");
                RoleBean roleBean = new RoleBean(role.trim());
                for (String permissionId : permissionIds) {
                    permissionId = permissionId.trim();
                    boolean isDeniedPermission = permissionId.startsWith(RoleMgtConstants.DENY);
                    if (isDeniedPermission) {
                        permissionId = permissionId.substring(RoleMgtConstants.DENY.length(), permissionId.length());
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
                String roleActionString =
                        configuration.getFirstProperty(roleConfigPath + "." + role +
                                ".Action");
                roleBean.setAction(roleActionString);

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
        if (RoleMgtConstants.REGISTRY_GET.equals(action)) {
            return ActionConstants.GET;
        } else if (RoleMgtConstants.REGISTRY_PUT.equals(action)) {
            return ActionConstants.PUT;
        } else if (RoleMgtConstants.REGISTRY_DELETE.equals(action)) {
            return ActionConstants.DELETE;
        } else {
            return action;
        }
    }

    private static void updateRoles(Set<RoleBean> roleBeanList) throws StratosException{
        TenantManager tenantManager = ServiceHolder.getRealmService().getTenantManager();
        Tenant[] tenants = new Tenant[0];
        try {
            tenants = tenantManager.getAllTenants();
        } catch (UserStoreException e) {
            String message = "Failed to get all tenants from tenant manager.";
            log.error(message);
            throw new StratosException(message, e);
        }

        //get Roles to be added and deleted
        ArrayList<String> rolesToAdd = new ArrayList<String>();
        ArrayList<String> rolesToDelete = new ArrayList<String>();
        for (RoleBean role : roleBeanList) {
            if(role.getAction() == RoleManagerConstants.ROLE_ADDITION){
                rolesToAdd.add(role.getRoleName());
            }
            else if(role.getAction() == RoleManagerConstants.ROLE_DELETION){
                rolesToDelete.add(role.getRoleName());
            }
        }
        for(Tenant tenant : tenants){
            //Start a new tenant flow
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenant.getDomain());
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenant.getId());
            //Services are loaded from the service holder
            ServiceHolder.getTenantRegLoader().loadTenantRegistry(tenant.getId());
            try {
                UserStoreManager userStoreManager = PrivilegedCarbonContext.getThreadLocalCarbonContext().
                        getUserRealm().getUserStoreManager();
                AuthorizationManager authorizationManager = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm().getAuthorizationManager();
                updateRolesPerTenant(userStoreManager,authorizationManager,roleBeanList);
            } catch (UserStoreException e) {
                String message = "Failed to update roles of tenant : " + tenant.getDomain()
                        + "(" + tenant.getId() + ")";
                log.error(message);
                throw new StratosException(message, e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
                log.info("Role updation process is completed for tenant: " + tenant.getDomain() + "[" + tenant.getId() + "]");
            }

        }

    }

    private static void updateRolesPerTenant(UserStoreManager userStoreManager, AuthorizationManager authorizationManager,
            Set<RoleBean> roleBeanList) throws UserStoreException {
        Boolean isRoleAddition = false, isRoleUpdation = false, isRoleDeletion = false;

        for (RoleBean roleBean : roleBeanList) {
            if(RoleManagerConstants.ROLE_ADDITION.equals(roleBean.getAction())){
                isRoleAddition = true;
            }else if(RoleManagerConstants.ROLE_UPDATION.equals(roleBean.getAction() )){
                isRoleUpdation = true;
            }else if(RoleManagerConstants.ROLE_DELETION.equals(roleBean.getAction())){
                isRoleDeletion = true;
            }
            if(isRoleDeletion){
                userStoreManager.deleteRole(roleBean.getRoleName());
                continue;
            } else if (isRoleAddition && !userStoreManager.isExistingRole(roleBean.getRoleName())) {
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
            } else if (isRoleUpdation){
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

