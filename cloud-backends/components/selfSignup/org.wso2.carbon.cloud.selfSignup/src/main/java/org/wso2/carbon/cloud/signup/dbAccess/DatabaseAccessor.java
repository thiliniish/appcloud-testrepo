/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.cloud.signup.dbAccess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.cloud.signup.configReader.ConfigFileReader;
import org.wso2.carbon.cloud.signup.constants.SignUpWorkflowConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * This Class consists of the database operations required for the self sign up of the users to the API Store.
 */
public class DatabaseAccessor {
    private static final Log log = LogFactory.getLog(DatabaseAccessor.class);
    private static Connection connection = null;
    private String errorMessage;

    public DatabaseAccessor() {

    }

    /**
     * Connects to the requested database and returns the connection object
     *
     * @return connection object after connecting to the database
     * @throws WorkflowException
     */
    protected Connection getConnection(String dataSourceName) throws WorkflowException {

        try {

            log.info("Configuring the connection to be created in the super tenant's space");
            //super tenant details
            int tenantId = MultitenantConstants.SUPER_TENANT_ID;
            String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
            Connection conn = null;

            //changing the tenant flow to the supper tenant
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext privilegedCarbonContext =
                    PrivilegedCarbonContext.getThreadLocalCarbonContext();
            privilegedCarbonContext.setTenantId(tenantId);
            privilegedCarbonContext.setTenantDomain(tenantDomain);

            //getting the cloud-mgt data source connection
            DataSource dataSource = (DataSource) privilegedCarbonContext.getJNDIContext()
                                                                        .lookup("jdbc/" +
                                                                                dataSourceName);
            if (dataSource != null) {
                connection = dataSource.getConnection();
                log.info("Successfully connected to the database " + dataSourceName);
            }
        } catch (NamingException e) {
            errorMessage = "Unable to connect to the datasource " + dataSourceName;
            log.error(errorMessage, e);
            throw new WorkflowException(errorMessage, e);
        } catch (SQLException e) {
            errorMessage = "Error in initializing the database connection for the datasource " +
                           dataSourceName;
            log.error(errorMessage, e);
            throw new WorkflowException(errorMessage, e);
        }

        return connection;
    }

    /**
     * This method inserts a temporary entry to the database for the user until confirmation.
     *
     * @param email        is the email of the self signed up user.
     * @param uuid         is the unique identifier for the signed up user
     * @param tenantDomain is the tenant domain that the user signed up from
     * @param roles        are the roles assigned to that user
     * @throws WorkflowException
     */
    public void insertToTempInviteTable(String email, String uuid, String tenantDomain,
                                        ArrayList<String> roles)
            throws WorkflowException {

        String dataSource =
                ConfigFileReader.retrieveConfigAttribute("databaseProperties", "dataSource");
        DatabaseAccessor dbAccessor = new DatabaseAccessor();
        Connection databaseConnection = dbAccessor.getConnection(dataSource);

        //getting the length os the roles passed
        int lengthOfUserRoles = roles.size();
        String userRoles = null;

        //constructing the roles to list of comma seperated user roles
        if (lengthOfUserRoles == 1) {
            userRoles = roles.get(0);

        }

        //since the list of roles need to be a comma seperated string.
        else {
            for (int counter = 0; counter < lengthOfUserRoles - 1; counter++) {
                userRoles = roles.get(counter) + ",";

            }
            userRoles = userRoles + roles.get(lengthOfUserRoles - 1);
        }
        try {

            DateFormat dateFormat = new SimpleDateFormat(SignUpWorkflowConstants.DATE_FORMAT);
            Date dateTime = new Date();
            String newDate = dateFormat.format(dateTime);
            int isSelfSigned = 1;

            String tempInsertQuery =
                    "INSERT INTO TEMP_INVITEE(tenantDomain, email, uuid, roles, dateTime, isSelfSigned) VALUES(?,?,?,?,?,?) ON DUPLICATE KEY UPDATE uuid = ?, roles = ?, isSelfSigned = ? , dateTime = ?";

            //Constructing the prepared statement
            PreparedStatement preparedStmt =
                    databaseConnection.prepareStatement(tempInsertQuery);
            preparedStmt.setString(1, tenantDomain);
            preparedStmt.setString(2, email);
            preparedStmt.setString(3, uuid);
            preparedStmt.setString(4, userRoles);
            preparedStmt.setString(5, newDate);
            preparedStmt.setInt(6, isSelfSigned);
            preparedStmt.setString(7, uuid);
            preparedStmt.setString(8, userRoles);
            preparedStmt.setInt(9, isSelfSigned);
            preparedStmt.setString(10, newDate);

            // execute the preparedstatement
            preparedStmt.execute();
            log.info("Entered a temporary entry for the user " + email +
                     " of the tenant domain" + tenantDomain + " to the database");
            preparedStmt.close();

        } catch (SQLException e) {
            errorMessage = "Unable to add user " + email + " of the tenant domain" + tenantDomain +
                           " to temp user mapping";
            log.error(errorMessage, e);
            throw new WorkflowException(errorMessage, e);
        } finally {

            try {
                if (databaseConnection != null) {
                    databaseConnection.close();
                }
            } catch (SQLException e) {
                errorMessage = "Error while closing the connection";
                log.error(errorMessage, e);

            }
        }
    }

}

