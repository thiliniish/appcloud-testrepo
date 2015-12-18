/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.cloud.tenantdeletion.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Represent the data access layer to access database and execute mysql queries
 */
public class DataAccessManager {

	private final static Log log = LogFactory.getLog(DataAccessManager.class);

	/**
	 * Updates database table (USER_LOGIN) with tenant details, when a Tenant login happens
	 * @param id          Tenant id
	 * @param domainName  Tenant Domain name
	 * @param currentDate Current Date -format(yyyy-MM-dd)
	 */
	public void updateUserLogin(int id, String domainName, String currentDate) {
		PreparedStatement preparedStatement = null;
		try {
			String query = "INSERT INTO `USER_LOGIN` (TENANT_ID, TENANT_DOMAIN, LAST_LOGIN_DATE) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE LAST_LOGIN_DATE = ? ";
			preparedStatement = DataConnectionManager.getInstance().getConnect().prepareStatement(query);
			preparedStatement.setInt(1, id);
			preparedStatement.setString(2, domainName);
			preparedStatement.setString(3, currentDate);
			preparedStatement.setString(4, currentDate);
			preparedStatement.executeUpdate();
			log.debug("Updated Tenant Login to database...");
		} catch (SQLException e) {
			log.error("SQL Exception occurred while executing query", e);
		} finally {
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					log.error("Failed to close preparedStatement", e);
				}
			}
			DataConnectionManager.getInstance().closeConnection();
		}
	}
}