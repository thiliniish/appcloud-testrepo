/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.cloud.git;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.appfactory.git.AppFactoryAuthenticationClient;
import org.wso2.carbon.appfactory.git.AppFactoryRepositoryAuthorizationClient;
import org.wso2.carbon.appfactory.git.GitBlitConfiguration;
import org.wso2.carbon.appfactory.git.GitBlitConstants;

import java.sql.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service client for repository authentication service
 */
public class CloudRepositoryAuthorizationClient extends AppFactoryRepositoryAuthorizationClient {
    private static final Logger log =
            LoggerFactory.getLogger(CloudRepositoryAuthorizationClient.class);
    GitBlitConfiguration configuration;
    private String userName;
    private char[] password;
    private String[] tenantsOfUser;
    private String authenticatedTenant;
    private AppFactoryAuthenticationClient appFactoryAuthenticationClient;
    private Map<String,AppFactoryRepositoryAuthorizationClient> authClients
            = new ConcurrentHashMap<String, AppFactoryRepositoryAuthorizationClient>();
    private Map<String,Boolean> authCache = new ConcurrentHashMap<String, Boolean>();


    public CloudRepositoryAuthorizationClient(String userName, char[] password, GitBlitConfiguration configuration,
                                              AppFactoryAuthenticationClient appFactoryAuthenticationClient) {
        super(configuration);
        this.userName = userName;
        this.password = password;
        this.configuration = configuration;
        this.appFactoryAuthenticationClient = appFactoryAuthenticationClient;
        tenantsOfUser = getTenantsOfUser();
        authenticatedTenant = "carbon.super";
    }

    /**
     * @param userName
     * @param repositoryName
     * @return
     */
    public boolean authorize(String userName, String repositoryName, String repositoryAction,
                             String fullRepoName) {

        String tenantDomain = fullRepoName.split("/")[0];

        if(tenantDomain.contains("~")){
            tenantDomain = tenantDomain.substring(1);
        }
        if(!isUserInTenant(tenantDomain)){
            return false;
        }

        if(userName.contains("@")){
            userName = userName.substring(0,userName.indexOf("@"));
        }

        String cacheKey = userName + "/" + tenantDomain +"/" + fullRepoName;
        log.info("CacheKey : " + cacheKey);
        if(authCache.containsKey(cacheKey)){
            return authCache.get(cacheKey);
        }

        if(!authClients.containsKey(tenantDomain)){
            appFactoryAuthenticationClient.logout();
            String cookie =  appFactoryAuthenticationClient.authenticate(userName, String.valueOf(password));
            authenticatedTenant = tenantDomain;
            AppFactoryRepositoryAuthorizationClient authClient =
                    new AppFactoryRepositoryAuthorizationClient(configuration);
            authClient.setCookie(cookie);
            authClients.put(tenantDomain,authClient);
            authenticatedTenant = tenantDomain;
        }
        userName = userName + "@" + tenantDomain;
        boolean isAuth = authClients.get(tenantDomain).authorize(userName, repositoryName, repositoryAction, fullRepoName);
        authCache.put(cacheKey,isAuth);
        return isAuth;
    }

    private String[] getTenantsOfUser(){
        if (!isAdminUser()) {
            String authenticatedUserName = userName;
            if(authenticatedUserName.contains("@")){
                authenticatedUserName = authenticatedUserName.substring(0,authenticatedUserName.indexOf("@"));
            }
            log.info("Getting Tenants of user: " + authenticatedUserName);
            String database = configuration.getProperty("cloud.database", "");
            String databaseUser = configuration.getProperty("cloud.database.user", "");
            String databaseUserPass = configuration.getProperty("cloud.database.password", "");
            Connection conn = null;
            PreparedStatement preparedStatement = null;
            ArrayList<String> tenantDomains = new ArrayList<String>();
            try {
                conn = DriverManager.getConnection(database, databaseUser, databaseUserPass);
                preparedStatement = conn.prepareStatement("SELECT tenantDomain FROM TENANT_USER_MAPPING WHERE userName=?");
                preparedStatement.setString(1, authenticatedUserName);
                ResultSet rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    String tenantDomain = rs.getString("tenantDomain");
                    tenantDomains.add(tenantDomain);
                }
            } catch (SQLException e) {
                log.error("Error while getting tenants of user", e);
            } finally {
                try {
                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    log.error("Error while getting tenants of user", e);
                }
            }
            return tenantDomains.toArray(new String[tenantDomains.size()]);
        }
        return new String[0];
    }

    private boolean isAdminUser() {
        return userName.equals(configuration.getProperty(GitBlitConstants.APPFACTORY_GITBLIT_ADMIN_USERNAME,
                GitBlitConstants.APPFACTORY_GITBLIT_ADMIN_USERNAME_DEFAULT_VALUE));
    }

    private boolean isUserInTenant(String tenantDomain) {
        for(String tenant : tenantsOfUser){
            if(tenant.equals(tenantDomain)){
                return true;
            }
        }
        return false;
    }
}
