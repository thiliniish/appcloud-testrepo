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
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.workflow.GeneralWorkflowResponse;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;

import javax.xml.stream.XMLStreamException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * API Cloud monetization specific subscription deletion workflow
 * Check if monetization is enabled for tenant: this workflow should only be deployed for monetization
 * enabled tenants. Once they enable monetization for a tenant, this workflow should be automatically deployed
 */
public class SubscriptionDeletionWorkflowExecutor extends AbstractSubscriptionWorkflowExecutor {

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
            if (responseObj.get(CustomWorkFlowConstants.SUBSCRIBERS_OBJ).isJsonObject()) {
                JsonObject subscriber = responseObj.getAsJsonObject(CustomWorkFlowConstants.SUBSCRIBERS_OBJ)
                        .getAsJsonObject(CustomWorkFlowConstants.SUBSCRIBER_OBJ);
                boolean isTestAccount = subscriber.get(CustomWorkFlowConstants.IS_TEST_ACCOUNT_PROPERTY).getAsBoolean();

                //Check subscribers is a test/complementary subscriber
                if (!isTestAccount) {
                    String accountNumber = subscriber.get(CustomWorkFlowConstants.ACCOUNT_NUMBER_PROPERTY).isJsonPrimitive()
                            ? subscriber.get(CustomWorkFlowConstants.ACCOUNT_NUMBER_PROPERTY).getAsString() : null;
                    if (StringUtils.isNotBlank(accountNumber)) {
                        return cancelSubscription(accountNumber, subscriptionWorkflowDTO);
                    } else {
                        throw new WorkflowException(ERROR_MSG + " Subscriber information invalid. account number cannot " +
                                "be null or empty");
                    }
                } else {
                    return handleFreePlan(subscriptionWorkflowDTO);
                }
            } else {
                throw new WorkflowException(ERROR_MSG + " Subscriber information not available.");
            }
        } catch (AxisFault | XMLStreamException e) {
            throw new WorkflowException(ERROR_MSG + " Error while cancelling the subscription .", e);
        }
    }

    /**
     * Handle free tier plan
     *
     * @param subscriptionWorkflowDTO Subscription workflow DTO
     * @return workflow response
     * @throws WorkflowException
     */
    protected WorkflowResponse handleFreePlan(SubscriptionWorkflowDTO subscriptionWorkflowDTO) throws WorkflowException {
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
    private WorkflowResponse cancelSubscription(String accountNumber, SubscriptionWorkflowDTO subscriptionWorkflowDTO) throws
            AxisFault, XMLStreamException, WorkflowException {
        String payload;
        ServiceClient client;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Cancelling subscription for Account: " + accountNumber + " Application name: "
                    + subscriptionWorkflowDTO.getApplicationName() + " Api name: " + subscriptionWorkflowDTO
                    .getApiName() + " Api version: " + subscriptionWorkflowDTO.getApiVersion());
        }

        payload = CustomWorkFlowConstants.CANCEL_SUBSCRIPTION_PAYLOAD
                .replace("$1", accountNumber).replace("$2", subscriptionWorkflowDTO.getApplicationName())
                .replace("$3", subscriptionWorkflowDTO.getApiName()).replace("$4", subscriptionWorkflowDTO.getApiVersion());
        client = WorkFlowUtils.getClient(CustomWorkFlowConstants.SOAP_ACTION_CANCEL_SUBSCRIPTION, serviceEndpoint,
                contentType, username, password);
        OMElement response = client.sendReceive(AXIOMUtil.stringToOM(payload));
        if (response.getChildrenWithLocalName("return").hasNext()) {
            OMElement returnElement = (OMElement) response.getChildrenWithLocalName("return").next();
            JsonElement responseElement = new JsonParser().parse(returnElement.getText());
            if (responseElement.isJsonObject()) {
                JsonObject resultObj = responseElement.getAsJsonObject();
                if (resultObj.get("subscriptionInfoNotAvailable") != null && resultObj.get
                        ("subscriptionInfoNotAvailable").getAsBoolean()) {
                    return deleteApiSubscription(subscriptionWorkflowDTO);
                }
                if (resultObj.get(CustomWorkFlowConstants.ZUORA_RESPONSE_SUCCESS) != null && resultObj.get
                        (CustomWorkFlowConstants.ZUORA_RESPONSE_SUCCESS).getAsBoolean()) {
                    if (resultObj.get(CustomWorkFlowConstants.MONETIZATION_TABLES_UPDATED) == null || !resultObj.get
                            (CustomWorkFlowConstants.MONETIZATION_TABLES_UPDATED).getAsBoolean()) {
                        LOGGER.warn("Zuora subscription has been removed successfully. But Monetization database " +
                                "table update failure indicated for the subscription deletion process. ");
                        //ToDo send email to correct the behaviour
                    }
                    return deleteApiSubscription(subscriptionWorkflowDTO);
                } else {
                    throw new WorkflowException("Cancel subscription failure. unsuccessful removal of zuora " +
                            "subscription. response: " + response.toString());
                }
            } else {
                throw new WorkflowException("Cancel subscription failure. response: " + response.toString());
            }
        } else {
            throw new WorkflowException("Cancel subscription information cannot be empty. response: " + response.toString());
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
        ApiMgtDAO apiMgtDAO = new ApiMgtDAO();
        SubscriptionWorkflowDTO subWorkflowDTO;
        if (workflowDTO instanceof SubscriptionWorkflowDTO) {
            subWorkflowDTO = (SubscriptionWorkflowDTO) workflowDTO;
        } else {
            throw new WorkflowException(ERROR_MSG + " WorkflowDTO doesn't match the required type");
        }
        Connection conn = null;
        String errorMsg;

        try {
            APIIdentifier identifier = new APIIdentifier(subWorkflowDTO.getApiProvider(),
                    subWorkflowDTO.getApiName(), subWorkflowDTO.getApiVersion());
            int applicationIdID = apiMgtDAO.getApplicationId(subWorkflowDTO.getApplicationName(), subWorkflowDTO.getSubscriber());

            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            apiMgtDAO.removeSubscription(identifier, applicationIdID, conn);
            conn.commit();
        } catch (APIManagementException e) {
            errorMsg = "Could not complete subscription deletion workflow for api: " + subWorkflowDTO.getApiName();
            throw new WorkflowException(errorMsg, e);
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.error("Failed to rollback remove subscription ", ex);
                }
            }
            errorMsg = "Couldn't remove subscription entry for api: " + subWorkflowDTO.getApiName();
            throw new WorkflowException(errorMsg, e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                LOGGER.error("Couldn't close database connection for subscription deletion workflow", e);
            }
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
