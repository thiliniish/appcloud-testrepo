/*
*  Copyright (c) 2005-2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.cloud.ws.userstore;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.user.api.Properties;
import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.RoleContext;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;
import org.wso2.carbon.user.core.tenant.Tenant;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

public class CloudWSUserStoreManager extends AbstractUserStoreManager {
    private static Log log = LogFactory.getLog(CloudWSUserStoreManager.class);

    public static final String ENDPOINT = "endpoint";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String CONTENT_TYPE = "contentType";
    public static final String REQUEST_FORMAT = "requestFormat";
    public static final String RESPONSE_TYPE = "responseType";
    public static final String RESULT_ELEMENT = "resultElement";
    public static final String EXPECTED_RESULT = "expectedResult";

    public CloudWSUserStoreManager() {

    }

    public CloudWSUserStoreManager(RealmConfiguration realmConfig, Map<String, Object> properties,
                                   ClaimManager claimManager, ProfileConfigurationManager profileManager, UserRealm realm,
                                   Integer tenantId) throws UserStoreException {
        this.realmConfig = realmConfig;
        this.tenantId = tenantId;
        this.userRealm = realm;
    }

    protected boolean doAuthenticate(String userName, Object credential) throws UserStoreException {
        log.debug("Processing authentication request for tenantId  - [" + this.tenantId + "]");
        boolean authStatus = false;
        HttpClient client = new HttpClient();
        PostMethod postRequest = new PostMethod(realmConfig.getUserStoreProperty(ENDPOINT));
        postRequest.addParameter("Content-Type", realmConfig.getUserStoreProperty(CONTENT_TYPE));
        postRequest.addParameter("Accept", realmConfig.getUserStoreProperty(RESPONSE_TYPE));

        if (realmConfig.getUserStoreProperty(USERNAME) != null && realmConfig.getUserStoreProperty(PASSWORD) != null) {
            byte[] encodedArr = Base64.encodeBase64((realmConfig.getUserStoreProperty(USERNAME) +
                                                     ":" + realmConfig.getUserStoreProperty(PASSWORD)).getBytes());
            postRequest.addParameter("Authorization", "Basic " + new String(encodedArr));
        }

        String requestStr = realmConfig.getUserStoreProperty(REQUEST_FORMAT);
        String postData = requestStr.replace("$1", userName).replace("$2", credential.toString());
        log.debug("Authentication request payload data - " + postData);

        try {
            int response = client.executeMethod(postRequest);
            if (response == 200) {
                String respStr = new String(postRequest.getResponseBody());
                JSONObject resultObj = new JSONObject(respStr);
                String[] resultPathElements = realmConfig.getUserStoreProperty(RESULT_ELEMENT).split("\\.");
                JSONObject tmpObj = resultObj;
                for (int i = 0; i < resultPathElements.length - 1; i++) {
                    tmpObj = (JSONObject) tmpObj.get(resultPathElements[i]);
                }

                String loginResult = tmpObj.getString(resultPathElements[resultPathElements.length - 1]);
                log.debug("Authentication response - " + loginResult);

                if (loginResult.equalsIgnoreCase(realmConfig.getUserStoreProperty(EXPECTED_RESULT))) {
                    authStatus = true;
                }
            }
        } catch (IOException | JSONException e) {
            log.error("Error occurred while calling backed to authenticate request for tenantId - [" + this.tenantId + "]");
        }
        return authStatus;
    }

    public Properties getDefaultUserStoreProperties() {
        Properties properties = new Properties();
        Property[] mandatoryProperties = null;
        Property[] optionalProperties = null;

        Property endpoint = new Property(ENDPOINT, "", "Authentication endpoint#Authentication endpoint used to validate user credentials", null);
        Property contentType = new Property(CONTENT_TYPE, "", "Content type#Content type of the request payload", null);
        Property requestFormat = new Property(REQUEST_FORMAT, "", "Request format#Format of the request sent to the authentication endpoint", null);
        Property responseType = new Property(RESPONSE_TYPE, "", "Response format#Format of the response sent from the authentication endpoint", null);
        Property resultElement = new Property(RESULT_ELEMENT, "", "Result element#Element where the authentication result is stored", null);
        Property expectedResult = new Property(EXPECTED_RESULT, "", "Expected result#Authentication success result element", null);

        Property username = new Property(USERNAME, "", "Remote Sever Username#Username to authenticate to the endpoint", null);
        Property password = new Property(PASSWORD, "", "Remote Server Password#Password to authenticate to the endpoint", null);
        Property disabled = new Property("Disabled", "false", "Disabled#Check to disable the user store", null);

        mandatoryProperties = new Property[]{endpoint, contentType, requestFormat, responseType, resultElement, expectedResult};
        optionalProperties = new Property[]{username, password, disabled};

        properties.setOptionalProperties(optionalProperties);
        properties.setMandatoryProperties(mandatoryProperties);
        return properties;
    }

    protected Map<String, String> getUserPropertyValues(String userName, String[] propertyNames, String profileName) throws UserStoreException {
        return null;
    }

    protected boolean doCheckExistingRole(String roleName) throws UserStoreException {
        return false;
    }


    protected RoleContext createRoleContext(String roleName) throws UserStoreException {
        return null;
    }

    protected boolean doCheckExistingUser(String userName) throws UserStoreException {
        return false;
    }

    protected String[] getUserListFromProperties(String property, String value, String profileName) throws UserStoreException {
        return new String[0];
    }

    protected void doAddUser(String userName, Object credential, String[] roleList, Map<String, String> claims, String profileName, boolean requirePasswordChange) throws UserStoreException {

    }

    protected void doUpdateCredential(String userName, Object newCredential, Object oldCredential) throws UserStoreException {

    }

    protected void doUpdateCredentialByAdmin(String userName, Object newCredential) throws UserStoreException {

    }

    protected void doDeleteUser(String userName) throws UserStoreException {

    }

    protected void doSetUserClaimValue(String userName, String claimURI, String claimValue, String profileName) throws UserStoreException {

    }

    protected void doSetUserClaimValues(String userName, Map<String, String> claims, String profileName) throws UserStoreException {

    }

    protected void doDeleteUserClaimValue(String userName, String claimURI, String profileName) throws UserStoreException {

    }

    protected void doDeleteUserClaimValues(String userName, String[] claims, String profileName) throws UserStoreException {

    }

    protected void doUpdateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers) throws UserStoreException {

    }

    protected void doUpdateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles) throws UserStoreException {

    }

    protected String[] doGetExternalRoleListOfUser(String userName, String filter) throws UserStoreException {
        return new String[0];
    }

    protected String[] doGetSharedRoleListOfUser(String userName, String tenantDomain, String filter) throws UserStoreException {
        return new String[0];
    }

    protected void doAddRole(String roleName, String[] userList, boolean shared) throws UserStoreException {

    }

    protected void doDeleteRole(String roleName) throws UserStoreException {

    }

    protected void doUpdateRoleName(String roleName, String newRoleName) throws UserStoreException {

    }

    protected String[] doGetRoleNames(String filter, int maxItemLimit) throws UserStoreException {
        return new String[0];
    }

    protected String[] doListUsers(String filter, int maxItemLimit) throws UserStoreException {
        return new String[0];
    }

    protected String[] doGetDisplayNamesForInternalRole(String[] userNames) throws UserStoreException {
        return new String[0];
    }

    public boolean doCheckIsUserInRole(String userName, String roleName) throws UserStoreException {
        return false;
    }

    protected String[] doGetSharedRoleNames(String tenantDomain, String filter, int maxItemLimit) throws UserStoreException {
        return new String[0];
    }

    protected String[] doGetUserListOfRole(String roleName, String filter) throws UserStoreException {
        return new String[0];
    }

    public String[] getProfileNames(String userName) throws UserStoreException {
        return new String[0];
    }

    public String[] getAllProfileNames() throws UserStoreException {
        return new String[0];
    }

    public boolean isReadOnly() throws UserStoreException {
        return false;
    }

    public Date getPasswordExpirationTime(String username) throws UserStoreException {
        return null;
    }

    public int getUserId(String username) throws UserStoreException {
        return 0;
    }

    public int getTenantId(String username) throws UserStoreException {
        return 0;
    }

    public int getTenantId() throws UserStoreException {
        return this.tenantId;
    }

    public Map<String, String> getProperties(org.wso2.carbon.user.api.Tenant tenant) throws org.wso2.carbon.user.api.UserStoreException {
        return null;
    }

    public boolean isMultipleProfilesAllowed() {
        return false;
    }

    public void addRememberMe(String s, String s1) throws org.wso2.carbon.user.api.UserStoreException {

    }

    public boolean isValidRememberMeToken(String s, String s1) throws org.wso2.carbon.user.api.UserStoreException {
        return false;
    }

    public Map<String, String> getProperties(Tenant tenant) throws UserStoreException {
        return null;
    }

    public boolean isBulkImportSupported() throws UserStoreException {
        return false;
    }

    public RealmConfiguration getRealmConfiguration() {
        return this.realmConfig;
    }
}