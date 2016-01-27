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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

/**
 * Class used to create database connection
 */
public class DataConnectionManager {
	private static final Log log = LogFactory.getLog(DataConnectionManager.class);
	private static final DataConnectionManager instance = new DataConnectionManager();
	private Connection connection;

	/**
	 * Returns database connection instance
	 * @return Database Connection
	 */
	public static DataConnectionManager getInstance() {
		return instance;
	}

	/**
	 * Initializes MySQL database connection and returns MySQL database connection
	 * @return Database connection
	 */
	public Connection getDbConnection() {
		ConfigReader configReader = ConfigReader.getInstance();
		String datasourceName = configReader.getDatasourceName("Configurations/datasources/carbon-datasource");
		try {
			Hashtable<String, String> environment = new Hashtable<String, String>();
			environment.put("java.naming.factory.initial", "org.wso2.carbon.tomcat.jndi.CarbonJavaURLContextFactory");
			Context initContext = new InitialContext(environment);
			DataSource dataSource = (DataSource) initContext.lookup(datasourceName);
			if (dataSource != null) {
				connection = dataSource.getConnection();
			} else {
				String msg = "Cannot Find a data source with the name";
				log.error(msg);
			}
		} catch (NamingException e) {
			log.error("Naming Exception has occurred while initializing the context", e);
		} catch (SQLException e) {
			log.error("SQL Exception occurred while getting connection", e);
		}
		return connection;
	}

	/**
	 * Closing the connection
	 */
	public void closeDbConnection() {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				log.error("SQL Exception occurred while Closing connection ", e);
			}
		}
	}
}