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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.cloud.heartbeat.monitor.core.notification.Mailer;
import org.wso2.cloud.heartbeat.monitor.core.notification.SMSSender;
import org.wso2.cloud.heartbeat.monitor.utils.fileutils.CaseConverter;

import java.sql.Connection;

public class TestStateHandler {

    private static TestStateHandler instance;
    private static final Log log = LogFactory.getLog(TestStateHandler.class);

    /**
     * This method returns the TestStateHandler instance.
     * @return TestStateHandler
     */
    public static TestStateHandler getInstance(){
        if(instance==null){
            instance = new TestStateHandler();
        }
        return instance;
    }

    private TestStateHandler() {
        /*To avoid the instantiation*/
    }

    /**
     * On test success.
     * @param testInfo Test Test information
     */
    public void onSuccess(TestInfo testInfo) {
        boolean success = true;
        DbConnectionManager dbConnectionManager = DbConnectionManager.getInstance();
        Connection connection = dbConnectionManager.getConnection();

        long timestamp = System.currentTimeMillis();
        DbConnectionManager.insertLiveStatus(connection, timestamp, testInfo.getServiceName(), testInfo.getTestName(), success, Integer.parseInt(testInfo.getSeverity()));

        log.info(CaseConverter.splitCamelCase(testInfo.getServiceName())+ " - "+testInfo.getTestName()+": SUCCESS");
    }

    /**
     * On test failure
     * @param msg error message
     * @param testInfo Test information
     */
    public void onFailure(TestInfo testInfo, String msg) {
        log.error(CaseConverter.splitCamelCase(testInfo.getServiceName()) +" (" + testInfo.getHostname()+ ") - " + testInfo.getTestName() + ": FAILURE, " + msg);
        DbConnectionManager dbConnectionManager = DbConnectionManager.getInstance();
        Connection connection = dbConnectionManager.getConnection();

        long timestamp = System.currentTimeMillis();
        DbConnectionManager.insertLiveStatus(connection, timestamp, testInfo.getServiceName(), testInfo.getTestName(), false, Integer.parseInt(testInfo.getSeverity()));
        DbConnectionManager.insertFailureDetail(connection, timestamp, testInfo.getServiceName(), testInfo.getTestName(), msg);

        Mailer mailer = Mailer.getInstance();
        mailer.send(CaseConverter.splitCamelCase(testInfo.getServiceName()) + " : FAILURE",
                CaseConverter.splitCamelCase(testInfo.getTestName()) + ": " + msg, "");
        SMSSender smsSender = SMSSender.getInstance();
        smsSender.send(CaseConverter.splitCamelCase(testInfo.getServiceName()) + ": " +
                CaseConverter.splitCamelCase(testInfo.getTestName()) + ": FAILURE");

    }

    /**
     * On test failure
     * @param e Exception
     * @param testInfo Test information
     */
    public void onFailure(TestInfo testInfo, String msg, Exception e ) {
        log.error(CaseConverter.splitCamelCase(testInfo.getServiceName()) +" (" + testInfo.getHostname()+ ") - " + testInfo.getTestName() + ": FAILURE, " + msg + " " + e);
        DbConnectionManager dbConnectionManager = DbConnectionManager.getInstance();
        Connection connection = dbConnectionManager.getConnection();

        long timestamp = System.currentTimeMillis();
        DbConnectionManager.insertLiveStatus(connection, timestamp, testInfo.getServiceName(), testInfo.getTestName(), false, Integer.parseInt(testInfo.getSeverity()));
        DbConnectionManager.insertFailureDetail(connection, timestamp, testInfo.getServiceName(), testInfo.getTestName(), e.getMessage());

        Mailer mailer = Mailer.getInstance();
        mailer.send(CaseConverter.splitCamelCase(testInfo.getServiceName()) + " : FAILURE",
                CaseConverter.splitCamelCase(testInfo.getTestName()) + ": " + msg, "");
        SMSSender smsSender = SMSSender.getInstance();
        smsSender.send(CaseConverter.splitCamelCase(testInfo.getServiceName()) + ": " +
                CaseConverter.splitCamelCase(testInfo.getTestName()) + ": FAILURE");

    }
}
