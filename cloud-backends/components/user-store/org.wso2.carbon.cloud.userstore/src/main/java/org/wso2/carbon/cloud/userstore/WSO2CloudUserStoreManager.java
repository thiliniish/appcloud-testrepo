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

package org.wso2.carbon.cloud.userstore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.naming.directory.BasicAttributes;
import java.util.Date;
import java.util.Map;

/**
 * Custom LDAP based user store implementation for wso2 cloud this class is used to replace the '@' symbol.
 */
public class WSO2CloudUserStoreManager extends CloudUserStoreManager {

    private static Log log = LogFactory.getLog(WSO2CloudUserStoreManager.class);
    private static CloudUserEmailCache cloudUserEmailCache = CloudUserEmailCache.getInstance();
    private static final String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";

    public WSO2CloudUserStoreManager() {
    }

    public WSO2CloudUserStoreManager(RealmConfiguration realmConfig, Map<String, Object> properties,
            ClaimManager claimManager, ProfileConfigurationManager profileManager, UserRealm realm, Integer tenantId)
            throws UserStoreException {
        super(realmConfig, properties, claimManager, profileManager, realm, tenantId);
    }

    public WSO2CloudUserStoreManager(RealmConfiguration realmConfig, ClaimManager claimManager,
            ProfileConfigurationManager profileManager) throws UserStoreException {
        super(realmConfig, claimManager, profileManager);
    }

    // =============================================================================================================
    // ==================================== DOWN WAY METHODS =======================================================
    // =============================================================================================================

    @Override
    public void doAddUser(String userName, Object credential, String[] roleList, Map<String, String> claims,
            String profileName) throws UserStoreException {
        super.doAddUser(doConvert(userName), credential, roleList, claims, profileName);
    }

    @Override
    public void doAddUser(String userName, Object credential, String[] roleList, Map<String, String> claims,
            String profileName, boolean requirePasswordChange) throws UserStoreException {
        super.doAddUser(doConvert(userName), credential, roleList, claims, profileName, requirePasswordChange);
    }

    /**
     * We convert back to email because we use EmailUserNameEnabled=true, if the email is null we ignore the check
     * since this check is already performed in AbstractUserStoreManager
     */
    @Override
    protected void doAddUserValidityChecks(String userName, Object credential) throws UserStoreException {
        if(doConvertUserNameToEmail(userName) != null){
            super.doAddUserValidityChecks(doConvertUserNameToEmail(userName), credential);
        }
    }

    @Override
    protected BasicAttributes getAddUserBasicAttributes(String userName) {
        return super.getAddUserBasicAttributes(doConvert(userName));
    }

    @Override
    protected void setUserClaims(Map<String, String> claims, BasicAttributes basicAttributes, String userName)
            throws UserStoreException {
        super.setUserClaims(claims, basicAttributes, doConvert(userName));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void doDeleteUser(String userName) throws UserStoreException {
        super.doDeleteUser(doConvert(userName));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void doUpdateCredential(String userName, Object newCredential,
            Object oldCredential) throws UserStoreException {
        super.doUpdateCredential(doConvert(userName), newCredential, oldCredential);
    }

    @Override
    public void doUpdateCredentialByAdmin(String userName, Object newCredential) throws UserStoreException {
        if (newCredential != null && !newCredential.equals("")) {
            super.doUpdateCredentialByAdmin(doConvert(userName), newCredential);
        }
    }

    @Override
    protected void doUpdateCredentialsValidityChecks(String userName, Object newCredential)
            throws UserStoreException {
        super.doUpdateCredentialsValidityChecks(doConvert(userName), newCredential);
    }

    @Override
    public void doSetUserClaimValues(String userName, Map<String, String> claims, String profileName)
            throws UserStoreException {
        super.doSetUserClaimValues(doConvert(userName), claims, profileName);
    }

    @Override
    public void doSetUserClaimValue(String userName, String claimURI, String value, String profileName)
            throws UserStoreException {
        super.doSetUserClaimValue(doConvert(userName), claimURI, value, profileName);
    }

    @Override
    public void doDeleteUserClaimValue(String userName, String claimURI, String profileName)
            throws UserStoreException {

        super.doDeleteUserClaimValue(doConvert(userName), claimURI, profileName);
    }

    @Override
    public void doDeleteUserClaimValues(String userName, String[] claims, String profileName)
            throws UserStoreException {
        super.doDeleteUserClaimValues(doConvert(userName), claims, profileName);
    }

    @Override
    public void doAddRole(String roleName, String[] userList, boolean shared) throws UserStoreException {

        super.doAddRole(roleName, doConvertUserList(userList), shared);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void doUpdateRoleListOfUser(String userName,
            String[] deletedRoles, String[] newRoles) throws UserStoreException {
        super.doUpdateRoleListOfUser(doConvert(userName), deletedRoles, newRoles);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void doUpdateUserListOfRole(String roleName,
            String[] deletedUsers, String[] newUsers) throws UserStoreException {
        super.doUpdateUserListOfRole(roleName, doConvertUserList(deletedUsers), doConvertUserList(newUsers));
    }

    @Override
    public boolean doAuthenticate(String userName, Object credential) throws UserStoreException {
        userName = doConvert(userName);
        if (!(isUserInRole(userName, "default")) && (getTenantId() == MultitenantConstants.SUPER_TENANT_ID)) {
            String[] roles = { "default" };
            updateRoleListOfUser(userName, null, roles);
            log.info("Default Role assigned for user : " + userName);
        }
        return super.doAuthenticate(userName, credential);
    }

    @Override
    public String[] getProfileNames(String userName) throws UserStoreException {
        return super.getProfileNames(doConvert(userName));
    }

    @Override
    public Map<String, String> getUserPropertyValues(String userName, String[] propertyNames,
            String profileName) throws UserStoreException {
        return super.getUserPropertyValues(doConvert(userName), propertyNames, profileName);
    }

    @Override
    public boolean doCheckExistingUser(String userName) throws UserStoreException {
        return super.doCheckExistingUser(doConvert(userName));
    }

    @Override
    protected String[] doGetDisplayNamesForInternalRole(String[] userNames) throws UserStoreException {
        return super.doGetDisplayNamesForInternalRole(doConvertUserList(userNames));
    }

    @Override
    protected String[] getLDAPRoleListOfUser(String userName, String filter, String searchBase,
            boolean shared) throws UserStoreException {
        return super.getLDAPRoleListOfUser(doConvert(userName), filter, searchBase, shared);
    }

    @Override
    protected String[] doGetExternalRoleListOfUser(String userName, String filter) throws UserStoreException {
        return super.doGetExternalRoleListOfUser(doConvert(userName), filter);
    }

    @Override
    protected String[] doGetSharedRoleListOfUser(String userName, String tenantDomain, String filter)
            throws UserStoreException {
        return super.doGetSharedRoleListOfUser(doConvert(userName), tenantDomain, filter);
    }

    @Override
    public Date getPasswordExpirationTime(String username) throws UserStoreException {
        return super.getPasswordExpirationTime(doConvert(username));
    }

    @Override
    protected String getNameInSpaceForUserName(String userName) throws UserStoreException {
        return super.getNameInSpaceForUserName(doConvert(userName));
    }

    @Override
    protected String getNameInSpaceForUserName(String userName, String searchBase, String searchFilter)
            throws UserStoreException {
        return super.getNameInSpaceForUserName(doConvert(userName), searchBase, searchFilter);
    }

    @Override
    public boolean doCheckIsUserInRole(String userName, String roleName) throws UserStoreException {
        return super.doCheckIsUserInRole(doConvert(userName), roleName);
    }

    @Override
    public void addRememberMe(String userName, String token)
            throws org.wso2.carbon.user.api.UserStoreException {
        super.addRememberMe(doConvert(userName), token);
    }

    @Override
    public boolean isValidRememberMeToken(String userName, String token)
            throws org.wso2.carbon.user.api.UserStoreException {
        return super.isValidRememberMeToken(doConvert(userName), token);
    }

    // =============================================================================================================
    // ====================================== UP WAY METHODS =======================================================
    // =============================================================================================================

    @Override
    public String[] doGetUserListOfRole(String roleName, String filter) throws UserStoreException {
        String[] users = super.doGetUserListOfRole(roleName, filter);
        if(MultitenantUtils.isEmailUserName()){
            doConvertUserNameListToEmail(users);
        }
        return users;
    }

    @Override
    public String[] getUserListFromProperties(String property, String value, String profileName)
            throws UserStoreException {
        String[] users = super.getUserListFromProperties(property, value, profileName);
        if(MultitenantUtils.isEmailUserName()){
            doConvertUserNameListToEmail(users);
        }
        return users;
    }

    @Override
    public String[] doListUsers(String filter, int maxItemLimit) throws UserStoreException {
        String[] users = super.doListUsers(filter, maxItemLimit);
        if(MultitenantUtils.isEmailUserName()){
            doConvertUserNameListToEmail(users);
        }
        return users;
    }

    //=============================================================================================================
    //============================================= Helper Methods ================================================
    //=============================================================================================================

    /**
     * Converts the <code>@</code> symbol in the user name to a <code>.</code> symbol
     *
     * @param userName - user name to be converted
     * @return converted user Name
     */
    private String doConvert(String userName) {
        StringBuilder convertedUser = new StringBuilder(userName);
        if (userName.contains("@")) {
            int index = userName.indexOf("@");
            convertedUser.setCharAt(index, '.');
            cloudUserEmailCache.addToCache(convertedUser.toString(), userName);
        }
        return convertedUser.toString();
    }

    /**
     * Converts the <code>@</code> symbol in a list of user names to a <code>.</code> symbol
     *
     * @param users - list of user names to be converted
     * @return - converted list of user names
     * @throws UserStoreException
     */
    private String[] doConvertUserList(String[] users) throws UserStoreException {
        if ((users != null) && (users.length > 0)) {
            for (int i = 0; i < users.length; i++) {
                users[i] = doConvert(users[i]);
            }
        }
        return users;
    }

    /**
     * Converts the user name to email, uses a cache to keep converted names
     *
     * @param userName - user name to be converted
     * @return email of the particular use name
     */
    private String doConvertUserNameToEmail(String userName) throws UserStoreException {
        String email = cloudUserEmailCache.getEmail(userName);
        if (email != null && !email.isEmpty()) {
            return email;
        }
        email = getUserClaimValue(userName, EMAIL_CLAIM_URI, null);
        if(email != null){
            cloudUserEmailCache.addToCache(userName, email);
            return email;
        } else {
            log.warn("Email is null for user : " + userName);
        }
        return userName;
    }

    /**
     * Converts a list of user names into emails
     *
     * @param users list of users
     * @return list of emails
     * @throws UserStoreException
     */
    private String[] doConvertUserNameListToEmail(String[] users) throws UserStoreException {
        if ((users != null) && (users.length > 0)) {
            for (int i = 0; i < users.length; i++) {
                users[i] = doConvertUserNameToEmail(users[i]);
            }
        }
        return  users;
    }

}
