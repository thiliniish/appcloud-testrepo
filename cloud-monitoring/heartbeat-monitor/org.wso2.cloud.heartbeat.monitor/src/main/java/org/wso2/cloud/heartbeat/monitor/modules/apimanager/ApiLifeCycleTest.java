/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.cloud.heartbeat.monitor.modules.apimanager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.wso2.carbon.automation.core.utils.HttpRequestUtil;
import org.wso2.carbon.automation.core.utils.HttpResponse;
import org.wso2.cloud.heartbeat.monitor.core.clients.application.APIPublisherRestClient;
import org.wso2.cloud.heartbeat.monitor.core.clients.application.APIStoreRestClient;
import org.wso2.cloud.heartbeat.monitor.modules.apimanager.data.*;
import org.wso2.cloud.heartbeat.monitor.modules.apimanager.util.StatusCodes;
import org.wso2.cloud.heartbeat.monitor.utils.ModuleUtils;
import org.wso2.cloud.heartbeat.monitor.utils.PlatformUtils;
import org.wso2.cloud.heartbeat.monitor.utils.TestInfo;
import org.wso2.cloud.heartbeat.monitor.utils.TestStateHandler;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ApiLifeCycleTest implements Job {
    private static final Log log = LogFactory.getLog(ApiLifeCycleTest.class);

    public final String TEST_NAME = "ApiLifeCycleTest";
    public final String API_NAME = "APILifeCycleTestSampleAPI";
    public final String API_CONTEXT = "sampleAPI";
    public final String APPLICATION_NAME = "TestApplication";
    public final String TAGS = "youtube, video, media";
    public final String BACKEND_URL = "http://gdata.youtube.com/feeds/api/standardfeeds";
    public final String DESCRIPTION = "This is test API create by API manager integration test";
    public final String API_VERSION = "1.0.0";
    public final String VISIBILITY = "restricted";
    public final String ROLES = "admin";
    public final String WSDL_URL = "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/products/bps/modules/samples/product/src/main/resources/bpel/2.0/MyRoleMexTestProcess/echo.wsdl";

    public String userName = "admin@heartbeat-monitor.org";
    public String password = "admin123";
    public String providerName = "admin-AT-heartbeat-monitor.org";
    public String publisherUrl = "http://localhost:9763";
    public String storeUrl = "http://localhost:9763";
    public String gatewayUrl = "http://localhost:8280";
    public String serviceName;


    private APIPublisherRestClient apiPublisher;
    private APIStoreRestClient apiStore;
    private TestInfo testInfo;
    private TestStateHandler testStateHandler;
    private int deploymentWaitTime = 30000;

    private int status;

    public void loginPublisher() throws Exception {
        log.info("Executing method: loginPublisher");
        try {
            HttpResponse loginResponse = apiPublisher.login(userName, password);
            log.info("Logged into API Publisher Successfully");
        } catch (Exception e) {
            status = StatusCodes.PUBLISHER_LOGIN;
            log.error("Login into API Publisher failed", e);
            throw new Exception("Login into API Publisher failed : " + e.getMessage(), e);
        }
    }

    public void addAPI() throws Exception {
        log.info("Executing method: addAPI");
        try {
            APIRequest apiRequest = new APIRequest(API_NAME, API_CONTEXT, new URL(BACKEND_URL));
            apiRequest.setTags(TAGS);
            apiRequest.setDescription(DESCRIPTION);
            apiRequest.setVersion(API_VERSION);
            apiRequest.setWsdl(WSDL_URL);
            apiRequest.setVisibility(VISIBILITY);
            apiRequest.setRoles(ROLES);
            HttpResponse addAPIResponse = apiPublisher.addAPI(apiRequest);
            log.info("API Added Successfully");
        } catch (Exception e) {
            status = StatusCodes.ADD_API;
            log.error("Adding API failed", e);
            throw new Exception("Adding API failed : " + e.getMessage(), e);
        }
    }

    public void publishAPI() throws Exception {
        log.info("Executing method: publishAPI");
        try {
            Thread.sleep(deploymentWaitTime);//Waiting for the API addition
            APILifeCycleStateRequest updateRequest = new APILifeCycleStateRequest(API_NAME, providerName, APILifeCycleState.PUBLISHED);
            apiPublisher.changeAPILifeCycleStatusTo(updateRequest);
            log.info("API Published Successfully");
        } catch (Exception e) {
            status = StatusCodes.PUBLISH_API;
            log.error("Publishing API failed", e);
            throw new Exception("Publishing API failed : " + e.getMessage(), e);
        }
    }

    public void loginStore() throws Exception {
        log.info("Executing method: loginStore");
        try {
            Thread.sleep(deploymentWaitTime);//Waiting for the API to get published
            HttpResponse StoreLoginResponse = apiStore.login(userName, password);
            log.info("Logged into API Store Successfully");
        } catch (Exception e) {
            status = StatusCodes.STROE_LOGIN;
            log.error("Login to API Store failed", e);
            throw new Exception("Login to API Store failed : " + e.getMessage(), e);
        }
    }


    private void addApplication() throws Exception {
        log.info("Executing method: addApplication");
        try {
            Thread.sleep(deploymentWaitTime);
            HttpResponse subscribeResponse = apiStore.addApplication(APPLICATION_NAME, "Unlimited", "", "");
            log.info("Application added Successfully. Name: " + APPLICATION_NAME + "  ID: " + getApplicationID(APPLICATION_NAME));
        } catch (Exception e) {
            status = StatusCodes.ADD_APPLICATION;
            log.error("Adding application failed", e);
            throw new Exception("Adding application failed : " + e.getMessage(), e);

        }
    }

    public void subscribeAPI() throws Exception {
        log.info("Executing method: subscribeAPI");
        try {
            Thread.sleep(deploymentWaitTime);
            SubscriptionRequest subscriptionRequest = new SubscriptionRequest(API_NAME, providerName, "addSubscription", getApplicationID(APPLICATION_NAME));
            HttpResponse subscribeResponse = apiStore.subscribe(subscriptionRequest);
            log.info("Subscribed to API Successfully");
        } catch (Exception e) {
            status = StatusCodes.SUBSCRIBE_API;
            log.error("Subscribing to API failed", e);
            throw new Exception("Subscribing to API failed : " + e.getMessage(), e);

        }

    }

    private String getApplicationID(String applicationName) throws Exception {
        String appID = "1";
        try {
            String responseString = apiStore.getAllApplications().getData();
            JSONObject jsonObject = new JSONObject(responseString);
            JSONArray array = jsonObject.getJSONArray("applications");
            for (int i = 0; i < array.length(); i++) {
                String appName = array.getJSONObject(i).get("name").toString();
                if (appName.equals(applicationName)) {
                    appID = array.getJSONObject(i).get("id").toString();
                }
            }
        } catch (Exception e) {
            status = StatusCodes.GET_APP_ID;
            log.error("Getting Application failed", e);
            throw new Exception("Getting Application failed : " + e.getMessage(), e);
        }
        return appID;
    }

    public void invokeAPI() throws Exception {
        log.info("Executing method: invokeAPI");
        try {
            Thread.sleep(deploymentWaitTime);
            GenerateAppKeyRequest generateAppKeyRequest = new GenerateAppKeyRequest(APPLICATION_NAME);
            String responseString = apiStore.generateApplicationKey(generateAppKeyRequest).getData();
            JSONObject response = new JSONObject(responseString);
            String accessToken = response.getJSONObject("data").getJSONObject("key").get("accessToken").toString();
            Map<String, String> requestHeaders = new HashMap<String, String>();
            requestHeaders.put("Authorization", "Bearer " + accessToken);
            HttpResponse youTubeResponse = HttpRequestUtil.doGet(gatewayUrl +
                    "/t/" + ModuleUtils.getDomainName(userName) + "/" + API_CONTEXT + "/" + API_VERSION + "/most_popular", requestHeaders);
            if (youTubeResponse.getData() != null) {
                log.info("API invoked Successfully");
            } else {
                status = StatusCodes.INVOKE_API;
                log.error("Invoking API failed");
                throw new Exception("Invoking API failed");
            }
        } catch (Exception e) {
            status = StatusCodes.INVOKE_API;
            log.error("Invoking API failed", e);
            throw new Exception("Invoking API failed : " + e.getMessage(), e);

        }

    }

    public String getSubscribedApplicationID() throws Exception {
        log.info("Executing method: getSubscribedApplicationID");
        String applicationId = "1";
        try {
            GetSubscriptionsRequest subscriptionsRequest = new GetSubscriptionsRequest("getAllSubscriptions");
            HttpResponse getSubscriptionsResponse = apiStore.getSubscriptions(subscriptionsRequest);
            JSONObject jsonObject = new JSONObject(getSubscriptionsResponse.getData());
            applicationId = jsonObject.getJSONArray("subscriptions").getJSONObject(0).get("id").toString();
            log.info("Application ID retrieved Successfully. ApplicationID: " + applicationId);
        } catch (Exception e) {
            status = StatusCodes.GET_SUBSCRIPTION_ID;
            log.error("Getting Application failed", e);
            throw new Exception("Getting Application failed : " + e.getMessage(), e);
        }
        return applicationId;
    }

    public void unsubscribeAPI(String applicationId) throws Exception {
        log.info("Executing method: unsubscribeAPI");
        try {
            Thread.sleep(deploymentWaitTime);
            UnsubscriptionRequest unsubscriptionRequest = new UnsubscriptionRequest(API_NAME, providerName, "removeSubscription", applicationId);
            HttpResponse unsubscribeResponse = apiStore.unsubscribe(unsubscriptionRequest);
            log.info("Un-subscribed from API Successfully");
        } catch (Exception e) {
            status = StatusCodes.UN_SUBSCRIBE;
            log.error("Un-subscribing API failed", e);
            throw new Exception("Un-subscribing API failed : " + e.getMessage(), e);
        }
    }

    private void deleteApplication() throws Exception {
        log.info("Executing method: deleteApplication");
        try {
            Thread.sleep(deploymentWaitTime);
            HttpResponse subscribeResponse = apiStore.removeApplication(APPLICATION_NAME);
            log.info("Application deleted Successfully");
        } catch (Exception e) {
            status = StatusCodes.DELETE_APPLICATION;
            log.error("Application deletion failed", e);
            throw new Exception("Application deletion failed : " + e.getMessage(), e);
        }
    }

    public void deleteAPI() throws Exception {
        log.info("Executing method: deleteAPI");
        try {
            Thread.sleep(deploymentWaitTime);//wait for unsubscribing
            HttpResponse deleteResponse = apiPublisher.deleteApi(API_NAME, API_VERSION, providerName);
            log.info("API deleted Successfully");
        } catch (Exception e) {
            status = StatusCodes.DELETE_API;
            log.error("Deleting API failed", e);
            throw new Exception("Deleting API failed : " + e.getMessage(), e);
        }

    }

    public void rollback() {
        log.info("Executing method: rollback");

        try {
//            loginPublisher();
//            loginStore();
            log.info(" Failure Status " + status);
            switch (status){
                case StatusCodes.ADD_API : {
                    deleteAPI();
                    break;
                }case StatusCodes.PUBLISH_API : {
                    deleteAPI();
                    break;
                }case StatusCodes.STROE_LOGIN : {
                    deleteAPI();
                    break;
                }case StatusCodes.ADD_APPLICATION : {
                    deleteAPI();
                    break;
                }case StatusCodes.SUBSCRIBE_API : {
                    deleteApplication();
                    deleteAPI();
                    break;
                }case StatusCodes.INVOKE_API : {
                    unsubscribeAPI(getSubscribedApplicationID());
                    deleteApplication();
                    deleteAPI();
                    break;
                }case StatusCodes.UN_SUBSCRIBE : {
                    unsubscribeAPI(getSubscribedApplicationID());
                    deleteApplication();
                    deleteAPI();
                    break;
                }case StatusCodes.DELETE_APPLICATION : {
                    deleteAPI();
                    break;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }


    public void testAPILifeCycleTestCase() {

        testInfo = new TestInfo(serviceName, TEST_NAME, publisherUrl);
        testStateHandler = TestStateHandler.getInstance();
        apiPublisher = new APIPublisherRestClient(publisherUrl);
        apiStore = new APIStoreRestClient(storeUrl);

        PlatformUtils.setTrustStoreParams();

        try {
            loginPublisher();
            addAPI();
            publishAPI();
            loginStore();
            addApplication();
            subscribeAPI();
            invokeAPI();
            unsubscribeAPI(getSubscribedApplicationID());
            deleteApplication();
            deleteAPI();
            testStateHandler.onSuccess(testInfo);
        } catch (Exception e) {
            String msg = "Error occurred while API Life Cycle Test. ";
            log.error(msg, e);
            testStateHandler.onFailure(testInfo, msg + e.getMessage(), e);
            rollback();
        }
    }


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("Test started");
        testAPILifeCycleTestCase();
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public void setPublisherUrl(String publisherUrl) {
        this.publisherUrl = publisherUrl;
    }

    public void setStoreUrl(String storeUrl) {
        this.storeUrl = storeUrl;
    }

    public void setGatewayUrl(String gatewayUrl) {
        this.gatewayUrl = gatewayUrl;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Sets deployment waiting time
     *
     * @param deploymentWaitTime Deployment wait time
     */
    public void setDeploymentWaitTime(String deploymentWaitTime) {
        this.deploymentWaitTime = Integer.parseInt(deploymentWaitTime.split("s")[0].replace(" ", "")) * 1000;
    }
}
