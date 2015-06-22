/*
 * Copyright 2005-2013 WSO2, Inc. http://www.wso2.org
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.cloud.heartbeat.monitor.utils;

import java.io.File;

public class Constants {

	/**
	 * Frequently used relative paths
	 */
	public static final String HEARTBEAT_CONF_PATH = "heartbeat.conf";
	public static final String LOG4J_PROPERTY_PATH = "resources"
			+ File.separator + "log4j.properties";
	public static final String KEY_STORE_PATH = "resources" + File.separator
			+ "keystores" + File.separator + "wso2carbon.jks";
	public static final String TRUST_STORE_PATH = "resources" + File.separator
			+ "truststores" + File.separator + "client-truststore.jks";

	/**
	 * Frequently used configuration tags
	 */

    public static final String QUERY_SQLSTAT =  "SELECT api,SUM(total_request_count) AS RequestCount from "
            +"API_VERSION_USAGE_SUMMARY WHERE time >= DATE_SUB(NOW(),"
            +"INTERVAL ? MINUTE) GROUP BY 'api';";

    public static final String HEARTBEAT_TENANT = "heartbeat_tenant";
	public static final String DATA_SOURCE = "data_source";
	public static final String LIVE_STATUS = "live_status";
	public static final String NOTIFICATION = "notification";
	public static final String MODULES = "modules";
	public static final String MANAGER = "manager";
	public static final String GOVERNANCE_REGISTRY = "greg";
	public static final String BUSINESS_RULE_SERVER = "brs";
	public static final String BUSINESS_PROCESS_SERVER = "bps";
	public static final String DATA_SERVICE_SERVER = "dss";
	public static final String COMPLEX_EVENT_PROCESSOR = "cep";
	public static final String STORAGE_SERVER = "storage";
	public static final String BUSINESS_ACTIVITY_MONITOR = "bam";
	public static final String TASK_SERVER = "task_server";
	public static final String IDENTITY_SERVER = "identity";

	public static final String APPFACTORY = "appfactory";
	public static final String CLOUD_MGT = "cloud_management";

	public static final String STRATOS_CONTROLLER_DEV = "manager_dev";
	public static final String STRATOS_CONTROLLER_TEST = "manager_test";
	public static final String STRATOS_CONTROLLER_PROD = "manager_prod";
	public static final String ESB_DEV = "esb_dev";
	public static final String ESB_TEST = "esb_test";
	public static final String ESB_PROD = "esb_prod";
	public static final String APPLICATION_SERVER_DEV = "appserver_dev";
	public static final String APPLICATION_SERVER_TEST = "appserver_test";
	public static final String APPLICATION_SERVER_PROD = "appserver_prod";
	public static final String BUSINESS_PROCESS_SERVER_DEV = "bps_dev";
	public static final String BUSINESS_PROCESS_SERVER_TEST = "bps_test";
	public static final String BUSINESS_PROCESS_SERVER_PROD = "bps_prod";

	public static final String UES = "ues";
	public static final String API_MANAGER = "api_manager";
	public static final String CLOUD_CONTROLLER = "cloud_controller";
	public static final String GITBLIT = "gitblit";
	public static final String S2_GITBLIT = "s2gitblit";

	public static final String API_GATEWAY = "api_gateway";
	public static final String API_STORE = "api_store";
	public static final String API_PUBLISHER = "api_publisher";
	public static final String API_KEY_MANAGER = "api_key_manager";
	public static final String JENKINS = "jppserver";

	// for live status
	public static final String HOST_NAME = "host_name";
	public static final String LIVE_STATUS_PORT = "port";
	public static final String HB_VERSION = "heartbeat_version";

	public static final String SAMPLE_APP_FILE_PATH = "TestFile.txt";
	public static final String SIMPLE_DATE_TIME_FORMAT = "yyyy-MM-dd hh:mm:ss a";

	public static final String REPO_TYPE = ".git";
	public static final String GIT = "git";
	public static final String REPO_CREATE_PARAM = "/rpc?req=CREATE_REPOSITORY&name=/";
	public static final String REPO_DELETE_PARAM = "/rpc?req=DELETE_REPOSITORY&name=/";

}
