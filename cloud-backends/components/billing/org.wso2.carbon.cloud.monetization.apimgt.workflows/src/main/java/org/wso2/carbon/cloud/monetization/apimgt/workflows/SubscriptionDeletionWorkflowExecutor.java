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
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.workflow.GeneralWorkflowResponse;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;

import java.util.List;

import javax.xml.stream.XMLStreamException;

import static org.hsqldb.HsqlDateTime.e;

/**
 * API Cloud monetization specific subscription deletion workflow
 * Check if monetization is enabled for tenant: this workflow should only be deployed for monetization
 * enabled tenants. Once they enable monetization for a tenant, this workflow should be automatically deployed
 */
public class SubscriptionDeletionWorkflowExecutor extends AbstractSubscriptionWorkflowExecutor {

    private static final long serialVersionUID = 7053789002502156679L;

    private static final Log LOGGER = LogFactory.getLog(SubscriptionDeletionWorkflowExecutor.class);
    private static final String ERROR_MSG = "Could not complete subscription deletion workflow.";

    private String serviceEndpoint;
    private String username;
    private String password;
    private String contentType;

    /**
     * Handle commercial tier plan
     *
     * @param subscriptionWorkflowDTO subscription workflow DTO
     * @return Workflow response
     * @throws WorkflowException
     */
    protected WorkflowResponse handleCommercialPlan(SubscriptionWorkflowDTO subscriptionWorkflowDTO)
            throws WorkflowException {
        try {
            JsonObject responseObj = WorkFlowUtils.getSubscriberInfo(subscriptionWorkflowDTO.getSubscriber(),
                    subscriptionWorkflowDTO.getTenantDomain(), serviceEndpoint, contentType, username, password);
            if (responseObj.isJsonObject()) {
                JsonObject subscriberJsonObj = responseObj.get(CustomWorkFlowConstants.SUBSCRIBER_OBJ)
                        .getAsJsonObject();

                if (subscriberJsonObj == null || !subscriberJsonObj.isJsonObject()) {
                    return handleSubscriptionIfInactive(subscriptionWorkflowDTO);
                }

                boolean isTestAccount = subscriberJsonObj.get(CustomWorkFlowConstants.IS_TEST_ACCOUNT_PROPERTY)
                        .getAsBoolean();

                //Check subscribers is a test/complementary subscriber
                if (!isTestAccount) {
                    String accountNumber = subscriberJsonObj.get(CustomWorkFlowConstants.ACCOUNT_NUMBER_PROPERTY)
                            .isJsonPrimitive() ?
                            subscriberJsonObj.get(CustomWorkFlowConstants.ACCOUNT_NUMBER_PROPERTY).getAsString() :
                            null;
                    if (StringUtils.isNotBlank(accountNumber)) {
                        return cancelSubscription(accountNumber, subscriptionWorkflowDTO);
                    } else {
                        return handleSubscriptionIfInactive(subscriptionWorkflowDTO);
                    }
                } else {
                    return handleFreePlan(subscriptionWorkflowDTO);
                }
            } else {
                return handleSubscriptionIfInactive(subscriptionWorkflowDTO);
            }
        } catch (AxisFault | APIManagementException | XMLStreamException e) {
            throw new WorkflowException(
                    ERROR_MSG + " Error while cancelling the subscription. Tenant: " + subscriptionWorkflowDTO
                            .getTenantDomain() + ", Subscriber: " + subscriptionWorkflowDTO.getSubscriber()
                            + ",  Application: " + subscriptionWorkflowDTO.getApplicationName() + ", Api: "
                            + subscriptionWorkflowDTO.getApiName() + ", API version: " + subscriptionWorkflowDTO
                            .getApiVersion(), e);
        }
    }

    /**
     * if the subscription is not in UNBLOCKED state, it will also be
     * handled as a free plan
     *
     * @param subscriptionWorkflowDTO subscriptionWorkflowDTO
     * @return workflow reference
     * @throws APIManagementException
     * @throws WorkflowException
     */
    private WorkflowResponse handleSubscriptionIfInactive(SubscriptionWorkflowDTO subscriptionWorkflowDTO)
            throws APIManagementException, WorkflowException {
        if (!isAnActiveSubscription(subscriptionWorkflowDTO)) {
            return handleFreePlan(subscriptionWorkflowDTO);
        } else {
            throw new WorkflowException(
                    ERROR_MSG + " Subscriber information is not available. Tenant: " + subscriptionWorkflowDTO
                            .getTenantDomain() + ", Subscriber: " + subscriptionWorkflowDTO.getSubscriber()
                            + ",  Application: " + subscriptionWorkflowDTO.getApplicationName() + ", Api: "
                            + subscriptionWorkflowDTO.getApiName() + ", API version: " + subscriptionWorkflowDTO
                            .getApiVersion());
        }
    }

    /**
     * Check whether the API subscription is in active state
     *
     * @param subscriptionWorkflowDTO subscriptionWorkflowDTO
     * @return is active boolean
     * @throws APIManagementException
     */
    private boolean isAnActiveSubscription(SubscriptionWorkflowDTO subscriptionWorkflowDTO)
            throws APIManagementException {
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        APIIdentifier apiIdentifier = new APIIdentifier(subscriptionWorkflowDTO.getApiProvider(),
                subscriptionWorkflowDTO.getApiName(), subscriptionWorkflowDTO.getApiVersion());
        int applicationId = apiMgtDAO.getApplicationId(subscriptionWorkflowDTO.getApplicationName(),
                subscriptionWorkflowDTO.getSubscriber());
        String subscriptionStatus = apiMgtDAO.getSubscriptionStatus(apiIdentifier, applicationId);

        if (StringUtils.isNotBlank(subscriptionStatus)) {
            return APIConstants.SubscriptionStatus.UNBLOCKED.equals(subscriptionStatus.trim());
        } else {
            throw new APIManagementException(
                    "Subscription status unavailable. Tenant: " + subscriptionWorkflowDTO.getTenantDomain()
                            + ", Subscriber: " + subscriptionWorkflowDTO.getSubscriber() + ",  Application: "
                            + subscriptionWorkflowDTO.getApplicationName() + ", Api: " + subscriptionWorkflowDTO
                            .getApiName() + ", API version: " + subscriptionWorkflowDTO.getApiVersion());
        }
    }

    /**
     * Handle free tier plan
     *
     * @param subscriptionWorkflowDTO Subscription workflow DTO
     * @return workflow response
     * @throws WorkflowException
     */
    protected WorkflowResponse handleFreePlan(SubscriptionWorkflowDTO subscriptionWorkflowDTO)
            throws WorkflowException {
        return deleteApiSubscription(subscriptionWorkflowDTO);
    }

    /**
     * Execute deletion of the API Manager side subscription
     *
     * @param workflowDTO Workflow DTO
     * @return Workflow response
     * @throws WorkflowException
     */
    private WorkflowResponse deleteApiSubscription(WorkflowDTO workflowDTO) throws WorkflowException {
        workflowDTO.setStatus(WorkflowStatus.APPROVED);
        complete(workflowDTO);
        super.publishEvents(workflowDTO);
        return new GeneralWorkflowResponse();
    }

    /**
     * Add subscriber information to persistence
     *
     * @param subscriptionWorkflowDTO Subscription Workflow DTO
     * @throws AxisFault
     * @throws XMLStreamException
     */
    private WorkflowResponse cancelSubscription(String accountNumber, SubscriptionWorkflowDTO subscriptionWorkflowDTO)
            throws AxisFault, XMLStreamException, WorkflowException {
        String payload;
        ServiceClient client;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Cancelling subscription for Account: " + accountNumber + " Application name: "
                    + subscriptionWorkflowDTO.getApplicationName() + " Api name: " + subscriptionWorkflowDTO
                    .getApiName() + " Api version: " + subscriptionWorkflowDTO.getApiVersion());
        }

        payload = CustomWorkFlowConstants.CANCEL_SUBSCRIPTION_PAYLOAD.replace("$1", accountNumber)
                .replace("$2", subscriptionWorkflowDTO.getApplicationName())
                .replace("$3", subscriptionWorkflowDTO.getApiName())
                .replace("$4", subscriptionWorkflowDTO.getApiVersion());
        client = WorkFlowUtils
                .getClient(CustomWorkFlowConstants.SOAP_ACTION_CANCEL_SUBSCRIPTION, serviceEndpoint, contentType,
                        username, password);
        OMElement response = client.sendReceive(AXIOMUtil.stringToOM(payload));
        if (response.getChildrenWithLocalName(CustomWorkFlowConstants.SOAP_RETURN_ELEMENT).hasNext()) {
            OMElement returnElement = (OMElement) response
                    .getChildrenWithLocalName(CustomWorkFlowConstants.SOAP_RETURN_ELEMENT).next();
            JsonElement responseElement = new JsonParser().parse(returnElement.getText());
            if (responseElement.isJsonObject()) {
                JsonObject resultObj = responseElement.getAsJsonObject();
                if (resultObj.get(CustomWorkFlowConstants.RESPONSE_SUCCESS) != null && resultObj
                        .get(CustomWorkFlowConstants.RESPONSE_SUCCESS).getAsBoolean()) {
                    JsonObject dataObj = resultObj.get(CustomWorkFlowConstants.RESPONSE_DATA).getAsJsonObject();
                    if (dataObj.get(CustomWorkFlowConstants.SUBSCRIPTION_INFO_NOT_AVAILABLE).getAsBoolean()) {
                        LOGGER.warn("Subscription information is not available. Account no: " + accountNumber
                                + ", Application name: " + subscriptionWorkflowDTO.getApplicationName() + ", Api name: "
                                + subscriptionWorkflowDTO.getApiName() + ", Version: " + subscriptionWorkflowDTO
                                .getApiVersion());
                    }

                    if (dataObj.get(CustomWorkFlowConstants.MONETIZATION_TABLES_UPDATED) == null || !resultObj
                            .get(CustomWorkFlowConstants.MONETIZATION_TABLES_UPDATED).getAsBoolean()) {
                        LOGGER.warn("Vendor subscription has been removed successfully. But Monetization database "
                                + "table update failure indicated for the subscription deletion process. ");
                        //ToDo send email to correct the behaviour
                    }
                    return deleteApiSubscription(subscriptionWorkflowDTO);
                } else {
                    throw new WorkflowException(
                            "Failed to cancel the Subscription. Response: "
                                    + response.toString());
                }
            } else {
                throw new WorkflowException("Cancel subscription failure. response: " + response.toString());
            }
        } else {
            throw new WorkflowException(
                    "Cancel subscription information cannot be empty. response: " + response.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WorkflowDTO> getWorkflowDetails(String s) throws WorkflowException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {
        // is not available
        SubscriptionWorkflowDTO subscriptionWorkflowDTO;
        if (workflowDTO instanceof SubscriptionWorkflowDTO) {
            subscriptionWorkflowDTO = (SubscriptionWorkflowDTO) workflowDTO;
        } else {
            throw new WorkflowException(ERROR_MSG + " WorkflowDTO doesn't match the required type");
        }
        return handleTierPlan(subscriptionWorkflowDTO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        SubscriptionWorkflowDTO subWorkflowDTO;
        if (workflowDTO instanceof SubscriptionWorkflowDTO) {
            subWorkflowDTO = (SubscriptionWorkflowDTO) workflowDTO;
        } else {
            throw new WorkflowException(ERROR_MSG + " WorkflowDTO doesn't match the required type");
        }
        String errorMsg;

        try {
            APIIdentifier identifier = new APIIdentifier(subWorkflowDTO.getApiProvider(), subWorkflowDTO.getApiName(),
                    subWorkflowDTO.getApiVersion());
            int applicationIdID = apiMgtDAO
                    .getApplicationId(subWorkflowDTO.getApplicationName(), subWorkflowDTO.getSubscriber());

            apiMgtDAO.removeSubscription(identifier, applicationIdID);
        } catch (APIManagementException e) {
            errorMsg = "Could not complete subscription deletion workflow for api: " + subWorkflowDTO.getApiName();
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
