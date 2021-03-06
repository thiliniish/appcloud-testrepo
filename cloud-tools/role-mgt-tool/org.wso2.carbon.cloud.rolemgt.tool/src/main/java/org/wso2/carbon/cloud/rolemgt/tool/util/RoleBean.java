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

package org.wso2.carbon.cloud.rolemgt.tool.util;

import org.wso2.carbon.user.core.Permission;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the data holder for Role Configuration details
 */
public class RoleBean {
    private String roleName;
    private String newRoleName;
    private List<Permission> authorizedPermissions;
    private List<Permission> deniedPermissions;
    private String action;

    /**
     * Constructor for RoleBean
     *
     * @param roleName String
     */
    public RoleBean(String roleName) {
        this.roleName = roleName;
        authorizedPermissions = new ArrayList<Permission>();
        deniedPermissions = new ArrayList<Permission>();
    }

    /**
     * Method to get Role Name
     *
     * @return String
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * Method to get Permissions
     *
     * @param isAuthorizedPermissions
     * @return List of Permissions
     */
    public List<Permission> getPermissions(boolean isAuthorizedPermissions) {
        if (isAuthorizedPermissions) {
            return authorizedPermissions;
        } else {
            return deniedPermissions;
        }
    }

    /**
     * Method to add permission
     *
     * @param permission              Permission
     * @param isAuthorizedPermissions boolean
     */
    public void addPermission(Permission permission, boolean isAuthorizedPermissions) {
        if (permission != null) {
            if (isAuthorizedPermissions) {
                authorizedPermissions.add(permission);
            } else {
                deniedPermissions.add(permission);
            }
        }
    }

    /**
     * Method to get action
     *
     * @return String
     */
    public String getAction() {
        return action;
    }

    /**
     * Method to set Action
     *
     * @param action String
     */
    public void setAction(String action) {
        this.action = action;
    }

    public String getNewRoleName() {
        return newRoleName;
    }

    public void setNewRoleName(String newRoleName) {
        this.newRoleName = newRoleName;
    }
}
