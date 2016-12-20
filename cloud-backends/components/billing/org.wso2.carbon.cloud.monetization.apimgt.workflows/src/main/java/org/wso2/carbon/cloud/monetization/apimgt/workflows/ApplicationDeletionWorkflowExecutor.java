/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.cloud.monetization.apimgt.workflows;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.workflow.GeneralWorkflowResponse;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;

import java.util.List;
import java.util.Set;
import javax.xml.stream.XMLStreamException;

/**
 * API Cloud monetization specific Application deletion workflow
 * Check if monetization is enabled for tenant: this workflow should only be deployed for monetization
 * enabled tenants. Once they enable monetization for a tenant, this workflow should be automatically deployed
 * <p/>
 * Add the configuration to
 * /_system/governance/apimgt/applicationdata/workflow-extensions.xml
 * <p/>
 * ex config:
 * <ApplicationDeletion
 * executor="org.wso2.carbon.cloud.monetization.apimgt.workflows.ApplicationDeletionWorkflowExecutor">
 * <Property name="serviceEndpoint">
 * https://milestones.appfactory.wso2.com:9643/services/APICloudMonetizationService/</Property>
 * <Property name="username">rajith.siriw.ardana.gmail.com@mustanggt350</Property>
 * <Property name="password">Admin!23</Property>
 * </ApplicationDeletion>
 */
public class ApplicationDeletionWorkflowExecutor extends WorkflowExecutor {

    private static final long serialVersionUID = -560733990384130863L;

    private static final Log LOGGER = LogFactory.getLog(ApplicationDeletionWorkflowExecutor.class);
    private static final String ERROR_MSG = "Could not complete application deletion workflow.";

    private String serviceEndpoint;
    private String username;
    private String password;
    private String contentType;

    /**
     * Remove Application speciic subscriptions from the billing engine. if any of the removal of subscriptions failed
     * then the application would not be deleted. only the successfully deleted subscriptions from the billing engine
     * will get removed from the application
     *
     * @param accountNumber subscriber account number
     * @param workflowDTO   workflowDTO
     * @return workflow response
     * @throws AxisFault
     * @throws XMLStreamException
     * @throws WorkflowException
     */
    private WorkflowResponse removeAPISubscriptions(String accountNumber, ApplicationWorkflowDTO workflowDTO)
            throws AxisFault, XMLStreamException, WorkflowException {
        String payload;
        ServiceClient client;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Cancelling subscriptions for application. Account: " + accountNumber + " Application " + "name: " +
                    workflowDTO.getApplication().getName());
        }
        payload = CustomWorkFlowConstants.REMOVE_APP_SUBSCRIPTIONS_PAYLOAD
                .replace("$1", accountNumber)
                .replace("$2", workflowDTO.getApplication().getName());
        client = WorkFlowUtils
                .getClient(CustomWorkFlowConstants.SOAP_ACTION_REMOVE_APP_SUBSCRIPTIONS, serviceEndpoint, contentType,
                           username, password);
        OMElement response = client.sendReceive(AXIOMUtil.stringToOM(payload));
        if (response.getChildrenWithLocalName(CustomWorkFlowConstants.SOAP_RETURN_ELEMENT).hasNext()) {
            OMElement returnElement =
                    (OMElement) response.getChildrenWithLocalName(CustomWorkFlowConstants.SOAP_RETURN_ELEMENT).next();
            JsonElement responseElement = new JsonParser().parse(returnElement.getText());
            if (responseElement.isJsonObject()) {
                JsonObject resultObj = responseElement.getAsJsonObject();
                if (resultObj.get(CustomWorkFlowConstants.RESPONSE_SUCCESS) == null) {
                    throw new WorkflowException("Cancel application subscription failure.  Response status " +
                                                "cannot be null. response: " + response.toString());
                }
                JsonObject dataObj = resultObj.get(CustomWorkFlowConstants.RESPONSE_DATA).getAsJsonObject();
                if (resultObj.get(CustomWorkFlowConstants.RESPONSE_SUCCESS).getAsBoolean()) {
                    if (LOGGER.isDebugEnabled()) {
                        if (!dataObj.has(CustomWorkFlowConstants.REMOVED_SUBSCRIPTIONS)) {
                            LOGGER.debug("No paid subscriptions found.");
                        }
                    }
                    return finalizeAppDeletion(workflowDTO);
                } else {
                    return removeSuccessRemovals(dataObj.getAsJsonArray(CustomWorkFlowConstants.REMOVED_SUBSCRIPTIONS),
                                                 workflowDTO);
                }
            } else {
                throw new WorkflowException(
                        "Cancel application subscription failure. response: " + response.toString());
            }
        } else {
            throw new WorkflowException(
                    "Cancel application subscription response information cannot be empty. " + "response: " +
                    response.toString());
        }
    }

    /**
     * This executes once it fails to remove any of the paid subscriptions from the billing engine. this
     * Uses to only remove the successfully removed subscriptions from billing engine.
     * <p/>
     * Once it executes this, it will not delete the application and the free subscriptions.
     *
     * @param removedSubscriptions json array containing successfully removed subscription information
     * @param workflowDTO          workflowDTO
     * @return General workflow response
     * @throws WorkflowException
     */
    private WorkflowResponse removeSuccessRemovals(JsonArray removedSubscriptions, ApplicationWorkflowDTO workflowDTO)
            throws WorkflowException {
        String errorMsg;
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();

        if (removedSubscriptions.size() == 0) {
            return new GeneralWorkflowResponse();
        }
        for (JsonElement subscription : removedSubscriptions) {
            JsonObject subscriptionObj = subscription.getAsJsonObject();

            String apiName = subscriptionObj.get("ApiName").getAsString();
            String apiVersion = subscriptionObj.get("ApiVersion").getAsString();
            String apiProvider = subscriptionObj.get("ApiProvider").getAsString();

            APIIdentifier identifier = new APIIdentifier(apiProvider, apiName, apiVersion);
            int applicationIdID = workflowDTO.getApplication().getId();

            try {
                apiMgtDAO.removeSubscription(identifier, applicationIdID);
            } catch (APIManagementException e) {
                errorMsg = "Could not complete subscription deletion for Application: " +
                           workflowDTO.getApplication().getName() + ". Subscriber: " + workflowDTO.getUserName() +
                           ", ApiName: " + apiName + ", " + "ApiVersion: " + apiVersion + ", ApiProvider: " +
                           apiProvider;
                throw new WorkflowException(errorMsg, e);
            }
        }
        return new GeneralWorkflowResponse();
    }

    /**
     * Do the main application deletion tasks
     *
     * @param applicationWorkflowDTO application workflowDTO
     * @return general workflow response
     * @throws WorkflowException
     */
    private WorkflowResponse finalizeAppDeletion(ApplicationWorkflowDTO applicationWorkflowDTO)
            throws WorkflowException {
        applicationWorkflowDTO.setStatus(WorkflowStatus.APPROVED);
        complete(applicationWorkflowDTO);
        super.publishEvents(applicationWorkflowDTO);
        return new GeneralWorkflowResponse();
    }

    /**
     * {@inheritDoc}
     */
    @Override public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_APPLICATION_DELETION;
    }

    /**
     * {@inheritDoc}
     */
    @Override public List<WorkflowDTO> getWorkflowDetails(String s) throws WorkflowException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {
        ApplicationWorkflowDTO applicationWorkflowDTO;
        Set<SubscribedAPI> subscribedAPISet;
        if (workflowDTO instanceof ApplicationWorkflowDTO) {
            applicationWorkflowDTO = (ApplicationWorkflowDTO) workflowDTO;
        } else {
            throw new WorkflowException(ERROR_MSG + " WorkflowDTO doesn't match the required type");
        }

        Subscriber subscriber =
                new Subscriber(applicationWorkflowDTO.getUserName() + "@" + applicationWorkflowDTO.getTenantDomain());
        try {
            subscribedAPISet = ApiMgtDAO.getInstance().getSubscribedAPIs(subscriber,
                                                                         applicationWorkflowDTO.getApplication()
                                                                                               .getName(), null);

            //If 0 subscribed apis
            if (subscribedAPISet.isEmpty()) {
                return finalizeAppDeletion(applicationWorkflowDTO);
            }
        } catch (APIManagementException e) {
            throw new WorkflowException(ERROR_MSG + " Subscribed for API list not available. Subscriber: " +
                                        applicationWorkflowDTO.getUserName() + " Application: " +
                                        applicationWorkflowDTO.getApplication().getName());
        }

        try {
            JsonObject responseObj = WorkFlowUtils
                    .getSubscriberInfo(applicationWorkflowDTO.getUserName(), applicationWorkflowDTO.getTenantDomain(),
                                       serviceEndpoint, contentType, username, password);
            if (responseObj.isJsonObject()) {
                JsonObject subscriberJsonObj =
                        responseObj.get(CustomWorkFlowConstants.SUBSCRIBER_OBJ).getAsJsonObject();
                if (subscriberJsonObj.isJsonObject()) {
                    boolean isTestAccount =
                            subscriberJsonObj.get(CustomWorkFlowConstants.IS_TEST_ACCOUNT_PROPERTY).getAsBoolean();

                    //Check subscribers is a test/complementary subscriber
                    if (!isTestAccount) {
                        String accountNumber = subscriberJsonObj.get(CustomWorkFlowConstants.ACCOUNT_NUMBER_PROPERTY)
                                                                .isJsonPrimitive() ?
                                               subscriberJsonObj.get(CustomWorkFlowConstants.ACCOUNT_NUMBER_PROPERTY)
                                                                .getAsString() : null;
                        if (StringUtils.isBlank(accountNumber)) {
                            //Subscriber doesn't have any paid apis
                            return finalizeAppDeletion(applicationWorkflowDTO);
                        }
                        return removeAPISubscriptions(accountNumber, applicationWorkflowDTO);

                    } else {
                        return finalizeAppDeletion(applicationWorkflowDTO);
                    }
                } else {
                    throw new WorkflowException(ERROR_MSG + " Subscriber information is not available.");
                }
            } else if (responseObj.isJsonPrimitive() && responseObj.getAsString().isEmpty()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Subscriber information is not available. Subscriber assumed to be only using free " +
                                 "apis");
                }
                return finalizeAppDeletion(applicationWorkflowDTO);
            } else {
                throw new WorkflowException(ERROR_MSG + " Subscriber information is not available. Could be due to a " +
                                            "connection failure.");
            }
        } catch (AxisFault | XMLStreamException e) {
            throw new WorkflowException(ERROR_MSG, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        ApplicationWorkflowDTO applicationWorkflowDTO;
        if (workflowDTO instanceof ApplicationWorkflowDTO) {
            applicationWorkflowDTO = (ApplicationWorkflowDTO) workflowDTO;
        } else {
            throw new WorkflowException(
                    "Incompatible types. " + workflowDTO.getClass() + " cannot be cast to ApplicationWorkflowDTO");
        }
        Application application = applicationWorkflowDTO.getApplication();
        String errorMsg;
        try {
            apiMgtDAO.deleteApplication(application);
        } catch (APIManagementException e) {
            if (e.getMessage() == null) {
                errorMsg = "Couldn't complete simple application deletion workflow for application: " +
                           application.getName();
            } else {
                errorMsg = e.getMessage();
            }
            throw new WorkflowException(errorMsg, e);
        }
        return new GeneralWorkflowResponse();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getServiceEndpoint() {
        return serviceEndpoint;
    }

    public void setServiceEndpoint(String serviceEndpoint) {
        this.serviceEndpoint = serviceEndpoint;
    }
}
