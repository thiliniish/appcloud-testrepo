/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.endpoint.samlsso;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DBClient {
    private static final Log log = LogFactory.getLog(DBClient.class);
    private String dataSourceName;

    public DBClient(String dataSource) {
        this.dataSourceName = dataSource;
    }

    /**
     * Returns the tenant domains, the given users belongs to
     * @param user username
     * @return an array of tenant domains
     * @throws SQLException
     */
    public String[] getTenantDomains(String user) throws SQLException {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        ArrayList<String> tenantDomains = new ArrayList<String>();
        try {
            Hashtable<String, String> environment = new Hashtable<String, String>();
            environment.put("java.naming.factory.initial", "org.wso2.carbon.tomcat.jndi.CarbonJavaURLContextFactory");
            Context initContext = new InitialContext(environment);
            DataSource dataSource = (DataSource) initContext.lookup("jdbc/" + dataSourceName);
            if (dataSource != null) {
                conn = dataSource.getConnection();
                preparedStatement = conn.prepareStatement("SELECT tenantDomain FROM TENANT_USER_MAPPING WHERE userName=?");
                preparedStatement.setString(1,user);
                ResultSet rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    String tenantDomain = rs.getString("tenantDomain");
                    tenantDomains.add(tenantDomain);
                }
            } else {
                log.info("Cannot Find a data source with the name : " + dataSourceName);
            }
        } catch (NamingException e) {
            log.error(e.getMessage());
        } catch (SQLException e) {
            log.error(e.getMessage());
        } finally {
            if(preparedStatement != null){
                preparedStatement.close();
            }
            if(conn != null){
                conn.close();
            }
        }
        return tenantDomains.toArray(new String[tenantDomains.size()]);
    }

    /**
     * Returns the tenant domains, and their display names to which a user belongs
     * @param user username
     * @return an map of tenant domains and their display names
     * @throws SQLException
     */
    public Map<String,String > getTenantDisplayNames(String user) throws SQLException {
        Map<String, String> map =  new ConcurrentHashMap<String, String>();
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try {
            Hashtable<String, String> environment = new Hashtable<String, String>();
            environment.put("java.naming.factory.initial", "org.wso2.carbon.tomcat.jndi.CarbonJavaURLContextFactory");
            Context initContext = new InitialContext(environment);
            DataSource dataSource = (DataSource) initContext.lookup("jdbc/" + dataSourceName);
            if (dataSource != null) {
                conn = dataSource.getConnection();
                preparedStatement = conn.prepareStatement("SELECT * FROM ORGANIZATIONS WHERE" +
                        " tenantDomain IN (SELECT tenantDomain FROM TENANT_USER_MAPPING WHERE userName=?)");
                preparedStatement.setString(1,user);
                ResultSet rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    String tenantDomain = rs.getString("tenantDomain");
                    String tenantDisplayName = rs.getString("displayName");
                    map.put(tenantDomain, tenantDisplayName);
                }
            } else {
                log.info("Cannot Find a data source with the name : " + dataSourceName);
            }
        } catch (NamingException e) {
            log.error(e.getMessage());
        } catch (SQLException e) {
            log.error(e.getMessage());
        } finally {
            if(preparedStatement != null){
                preparedStatement.close();
            }
            if(conn != null){
                conn.close();
            }
        }
        return map;
    }

    /**
     * Stores the given uuid which is used for tenant registering in the case of user already existing in the LDAP (From OT)
     * In this case user can log in using his credentials and once he is logged in he'll be redirected to Tenant Registration
     * page in CloudMgt app
     * @param email email of the user
     * @param uuid  generated uuid
     * @throws SQLException
     */
    public void storeTempRegistration(String email, String uuid) throws SQLException {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try {
            Hashtable<String, String> environment = new Hashtable<String, String>();
            environment.put("java.naming.factory.initial", "org.wso2.carbon.tomcat.jndi.CarbonJavaURLContextFactory");
            Context initContext = new InitialContext(environment);
            DataSource dataSource = (DataSource) initContext.lookup("jdbc/" + dataSourceName);
            if (dataSource != null) {
                conn = dataSource.getConnection();
                preparedStatement = conn.prepareStatement("INSERT INTO TEMP_REGISTRATION (email, uuid, isInvitee, dateTime) VALUES (?,?,?,CURRENT_TIMESTAMP) ON DUPLICATE KEY UPDATE uuid=?, dateTime = CURRENT_TIMESTAMP;");
                preparedStatement.setString(1,email);
                preparedStatement.setString(2,uuid);
                preparedStatement.setString(3,"0");
                preparedStatement.setString(4,uuid);
                boolean rs = preparedStatement.execute();
            } else {
                log.info("Cannot Find a data source with the name : " + dataSourceName);
            }
        } catch (NamingException e) {
            log.error(e.getMessage());
        } catch (SQLException e) {
            log.error(e.getMessage());
        } finally {
            if(preparedStatement != null){
                preparedStatement.close();
            }
            if(conn != null){
                conn.close();
            }
        }
    }

}
