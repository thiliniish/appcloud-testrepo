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

package org.wso2.cloud.heartbeat.monitor.modules.apimanager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.wso2.carbon.automation.core.utils.HttpRequestUtil;
import org.wso2.carbon.automation.core.utils.HttpResponse;
import org.wso2.cloud.heartbeat.monitor.utils.Constants;
import org.wso2.cloud.heartbeat.monitor.utils.DbConnectionManager;
import org.wso2.cloud.heartbeat.monitor.utils.TestInfo;
import org.wso2.cloud.heartbeat.monitor.utils.TestStateHandler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * api invoking and checking Statistic are implemented in this class.
 */
public class ApiStatisticTest implements Job {

    final static Log log = LogFactory.getLog(ApiStatisticTest.class);

    private final static String TEST_NAME = "ApiStatisticTest";

    private int invokeApimethodRetryCount;
    private int getStatsDBmethodRetryCount;
    private PreparedStatement preparedStatement;
    private Connection statDbConnection;
    private String serviceName;
    private String publisherUrl;
    private String gatewayUrl;
    private String databaseHostName;
    private String user;
    private String accessToken;
    private String apiName;
    private String apiVersion;
    private String password;
    private String databaseName;
    private int timeInterval;
    private String severity;

    TestStateHandler testStateHandler;
    TestInfo testInfo;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        log.info("API Statistic Test started.");

        //test handler for report status of the test
        testInfo= new TestInfo(serviceName, TEST_NAME, publisherUrl, severity);
        testStateHandler = TestStateHandler.getInstance();

        //creating database connection instance
        DbConnectionManager dbConnectionManager = DbConnectionManager.getInstance();
        statDbConnection = dbConnectionManager.dynamicDBConnection(databaseHostName, databaseName, user, password);

        testApiStat();
    }

    /**
     * method to execute main procedures in the class
     * *
     */
    public void testApiStat() {

        try {

            getAPIStat();
            invokeAPI();

        } catch (SQLException e) {
            String msg = "Error occurred while API Statistic Test. ";
            log.error(msg, e);

        } catch (IOException e) {
            String msg = "Error occurred while API Statistic Test. ";
            log.error(msg, e);
        }

    }

    /**
     * method to execute sql query and get data from the table
     * *
     */
    private void getAPIStat() throws SQLException, IOException {

        // attributes for the execute db operation
        ResultSet resultSet;
        String requestCount;

        try {

            preparedStatement = statDbConnection.prepareStatement(Constants.QUERY_SQLSTAT);
            preparedStatement.setInt(1, timeInterval);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                //checking for the result set
                requestCount = resultSet.getString("RequestCount");

                if (Integer.parseInt(requestCount) <= 0) {
                    log.error("Number of API calls are Zero.");
                    testStateHandler.onFailure(testInfo, "Number of API calls are Zero.");

                } else {
                    log.info("API calls has been Received.");
                    testStateHandler.onSuccess(testInfo);
                }

            } else {

                log.error(" Number of API calls are Zero. No Records. ");
                testStateHandler.onFailure(testInfo, "Number of API calls are Zero.");
            }

        } catch (SQLException e) {

            //retrying the method for any exception
            log.error("Failed to execute SQL query.", e);
            testStateHandler.onFailure(testInfo, e.getMessage(), e);
            retryCurrentMethod("getApiStat");

        } finally {

            //closing database connection
            statDbConnection.close();
        }
    }

    /**
     * method to invoke an api per hour
     * *
     */
    public void invokeAPI() throws SQLException, IOException {

        Map<String, String> requestHeaders = new HashMap<String, String>();

        //request header
        requestHeaders.put("Authorization", "Bearer " + accessToken);

        //getting the http response
        HttpResponse apiResponse;

        apiResponse = HttpRequestUtil.doGet(gatewayUrl + "/" + apiName + "/" + apiVersion, requestHeaders);

        if (apiResponse.getData() != null) {

            //api url with which api url
            log.info(gatewayUrl  + "/" + apiName + " API invoked Successfully");

        } else {

            //retrying method api url
            log.error(gatewayUrl  + "/" + apiName + " Invoking API failed");
            retryCurrentMethod("InvokeAPi");
        }

    }

    /**
     * method to retry any method on fail
     * *
     */
    private void retryCurrentMethod(String methodName) throws SQLException, IOException {


        if (invokeApimethodRetryCount == 3 || getStatsDBmethodRetryCount == 3) {

            invokeApimethodRetryCount = 0;
            getStatsDBmethodRetryCount = 0;

            if ("getApiStat".equals(methodName)) {

                testStateHandler.onFailure(testInfo, "Number of API calls are Zero.");
            }
        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                //Exception ignored
            }
            if ("InvokeAPi".equals(methodName)) {
                invokeApimethodRetryCount++;
                invokeAPI();

            } else if ("getApiStat".equals(methodName)) {
                getStatsDBmethodRetryCount++;
                getAPIStat();
            }
        }
    }

    /**
     * these setters will read the .conf file and set the values.
     * *
     */

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setDatabaseHostName(String databaseHostName) {
        this.databaseHostName = databaseHostName;
    }

    public void setPublisherUrl(String publisherUrl) {
        this.publisherUrl = publisherUrl;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setGatewayUrl(String gatewayUrl) {
        this.gatewayUrl = gatewayUrl;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public void setTimeInterval(int timeInterval) {
        this.timeInterval = timeInterval;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

}



