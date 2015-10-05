/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.cloud.heartbeat.monitor.modules.appfactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;
import org.wso2.cloud.heartbeat.monitor.core.clients.authentication.JaggeryAppAuthenticatorClient;
import org.wso2.cloud.heartbeat.monitor.core.clients.https.HttpsJaggeryClient;
import org.wso2.cloud.heartbeat.monitor.core.clients.utils.JagApiProperties;
import org.wso2.cloud.heartbeat.monitor.core.notification.Mailer;
import org.wso2.cloud.heartbeat.monitor.core.notification.SMSSender;
import org.wso2.cloud.heartbeat.monitor.modules.appfactory.entities.BuildInfo;
import org.wso2.cloud.heartbeat.monitor.utils.DbConnectionManager;
import org.wso2.cloud.heartbeat.monitor.utils.TestInfo;
import org.wso2.cloud.heartbeat.monitor.utils.TestStateHandler;
import org.wso2.cloud.heartbeat.monitor.utils.fileutils.CaseConverter;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Application Build test for Appfactory implemented in this class
 * Test runs with a Web application sample "SimpleServlet"
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ApplicationBuildTest implements Job {

    private static final Log log = LogFactory.getLog(ApplicationBuildTest.class);

    private final String TEST_NAME = "ApplicationBuildTest";

    private String hostName;
    private String tenantUser;
    private String tenantUserPwd;
    private int deploymentWaitTime;
    private String serviceName;
    private String applicationKey;
    private String completeTestName;
    private boolean errorsReported;
    private int requestCount = 0;
    private JaggeryAppAuthenticatorClient authenticatorClient;
    private BuildInfo lastBuildInfo;
    private BuildInfo currentBuildInfo;
    private boolean loginStatus = false;
    private String severity;

    TestStateHandler testStateHandler;
    TestInfo testInfo;

    /**
     * @param jobExecutionContext
     * "managementHostName", "hostName" ,"tenantUser", "tenantUserPwd" "httpPort"
     * "deploymentWaitTime" params passed via JobDataMap.
     * @throws org.quartz.JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        setCompleteTestName(CaseConverter.splitCamelCase(serviceName) + " - Application Build Test : ");
        initBuildTest();
        if(!errorsReported){
            triggerBuild();
        }
        if(!errorsReported){
            testBuildStatus();
        }
    }

    /**
     * Initializes Web application service test
     */
    private void initBuildTest() {
        errorsReported = false;
        hostName = "https://" + hostName;
        authenticatorClient = new JaggeryAppAuthenticatorClient(hostName);
        loginStatus = authenticatorClient.login(tenantUser,tenantUserPwd);
        testInfo = new TestInfo(serviceName, TEST_NAME, hostName, severity);
        testStateHandler = TestStateHandler.getInstance();
        if(loginStatus){
            log.info("Successfully logged in to appmgt");
            getBuildInfo();
            log.info("Initialized successfully ");
        } else {
            log.error("Login failure to appmgt. Retrying...");
            countNoOfRequests("InitLoginError");
        }
    }

    /**
     * Gets last build information of a application
     * @return BuildInfo Last Build Information
     */
    private void getBuildInfo() {

        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "getbuildandrepodata");
        params.put("applicationKey", applicationKey);
        params.put("buildable", "true");
        params.put("isRoleBasedPermissionAllowed", "false");
        params.put("metaDataNeed", "false");
        params.put("userName", tenantUser);
        String buildInfoUrl =   hostName + JagApiProperties.BUILD_INFO_URL_SFX;
        String result = HttpsJaggeryClient.httpPost(buildInfoUrl,params);
        if(result != null && !result.isEmpty()) {
            JsonParser parser = new JsonParser();
            JsonArray resultAsJsonArray = parser.parse(result).getAsJsonArray();
            if( resultAsJsonArray.size() == 0 ){
                log.error("Error while getting last build info. Retrying...");
                countNoOfRequests("BuildInfoError");
            }else{
                JsonObject resultAsJsonObject = resultAsJsonArray.get(0).getAsJsonObject();
                JsonObject buildJsonObject = resultAsJsonObject.get("build").getAsJsonObject();

                int lastBuildNo = buildJsonObject.get("lastBuildId").getAsInt();
                String lastBuildStatus = buildJsonObject.get("status").getAsString();
                currentBuildInfo = new BuildInfo(lastBuildNo, lastBuildStatus);
                log.info("Last Build Info retrieved. Build No : " + lastBuildNo + " , Build Status : " + lastBuildStatus);
            }
        }else {
            log.error("Error while getting last build info. Retrying...");
            countNoOfRequests("BuildInfoError");
        }
    }

    /**
     * Triggers a build
     */
    private void triggerBuild() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "createArtifact");
        params.put("applicationKey", applicationKey);
        params.put("doDeploy", "true");
        params.put("revision", "");
        params.put("stage", "Development");
        params.put("tagName", "");
        params.put("version", "trunk");
        String url = hostName + JagApiProperties.BUILD_APPLICATION_URL_SFX;
        String result = HttpsJaggeryClient.httpPost(url, params);
        if(result != null) {
            if(result.equals("")){
                log.info("Build was triggered.");
            }else{
                JsonParser parser = new JsonParser();
                JsonElement jsonElement = parser.parse(result);
                JsonObject resultAsJsonObject = jsonElement.getAsJsonObject();
                String message = resultAsJsonObject.get("message").getAsString();
                if(!message.equals("")){
                    log.error("Build Was not triggered. "+ message +". Retrying...");
                    countNoOfRequests("TriggerBuild");
                }
            }
        }else {
            log.error("Build Was not triggered. Retrying...");
            countNoOfRequests("TriggerBuild");
        }

    }

    /**
     * Test the build by comparing the last build and current build
     * Checks whether build no has incremented by 1 and build status is successful
     */
    private void testBuildStatus() {
        try {
            Thread.sleep(deploymentWaitTime);
            loginStatus = authenticatorClient.login(tenantUser,tenantUserPwd);
            lastBuildInfo = currentBuildInfo;
            getBuildInfo();
            if(currentBuildInfo.getBuildNo() >= lastBuildInfo.getBuildNo()+1 && currentBuildInfo.getBuildStatus().equals("successful")){
                authenticatorClient.logout();
                testStateHandler.onSuccess(testInfo);
            }else{
                String msg = "";
                if(currentBuildInfo.getBuildNo()== lastBuildInfo.getBuildNo()){
                    msg = " Build was not triggered.";
                }else if(!currentBuildInfo.getBuildStatus().equals("successful")) {
                    msg = " Build was not successful.";
                }
                testStateHandler.onFailure(testInfo, msg);
            }
        } catch (InterruptedException e) {
            log.error("Exception occurred while  testing build status", e);
            testStateHandler.onFailure(testInfo, "Exception occurred while  testing build status. " + e.getMessage());
        }
    }



    private void countNoOfRequests(String type) {
        requestCount++;
        if(requestCount == 3){
            handleError(type);
            requestCount = 0;
        }
        else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                //Exception ignored
            }
            if(type.equals("TriggerBuild")) {
                triggerBuild();
            }else if (type.equals("InitLoginError")){
                loginStatus = authenticatorClient.login(tenantUser, tenantUserPwd);
                initBuildTest();
            }else if (type.equals("BuildInfoError")){
                getBuildInfo();
            }
        }
    }

    private void handleError(String type) {
        if(type.equals("TriggerBuild")) {
            log.error("Error occurred while triggering the build.");
            testStateHandler.onFailure(testInfo, "Tenant login failure to appmgt jaggery app.");
        }else if(type.equals("InitLoginError")) {
            log.error("Login Error occurred while initializing the test.");
            testStateHandler.onFailure(testInfo,
                                       "Login Error occurred while initializing the test.");
        }else if(type.equals("BuildInfoError")) {
            log.error("Error occurred while getting last build info.");
            testStateHandler.onFailure(testInfo,"Error occurred while getting last build info.");
        }
    }

    /**
     * Sets service host
     * @param hostName Service host
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * Sets Tenant user name
     * @param tenantUser Tenant user name
     */
    public void setTenantUser(String tenantUser) {
        this.tenantUser = tenantUser;
    }

    /**
     * Sets Tenant user password
     * @param tenantUserPwd Tenant user password
     */
    public void setTenantUserPwd(String tenantUserPwd) {
        this.tenantUserPwd = tenantUserPwd;
    }

    /**
     * Sets deployment waiting time
     * @param deploymentWaitTime Deployment wait time
     */
    public void setDeploymentWaitTime(String deploymentWaitTime) {
        this.deploymentWaitTime = Integer.parseInt(deploymentWaitTime.split("s")[0].replace(" ", ""))*1000;
    }

    /**
     * Sets Service name
     * @param serviceName Service name
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Sets Application key
     * @param applicationKey Application key
     */
    public void setApplicationKey(String applicationKey) {
        this.applicationKey = applicationKey;
    }

    /**
     * Sets Display Service name
     * @param completeTestName Service name
     */
    public void setCompleteTestName(String completeTestName) {
        this.completeTestName = completeTestName;
    }

}