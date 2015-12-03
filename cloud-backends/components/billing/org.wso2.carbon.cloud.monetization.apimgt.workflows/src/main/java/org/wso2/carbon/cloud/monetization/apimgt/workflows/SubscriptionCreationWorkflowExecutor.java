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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.workflow.GeneralWorkflowResponse;
import org.wso2.carbon.apimgt.impl.workflow.HttpWorkflowResponse;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutor;

import javax.xml.stream.XMLStreamException;
import java.util.List;

import static org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus.APPROVED;

/**
 * API Cloud monetization specific subscription creation workflow
 * Check if monetization is enabled for tenant: this workflow should only be deployed for monetization
 * enabled tenants. Once they enable monetization for a tenant, this workflow should be automatically deployed
 */
public class SubscriptionCreationWorkflowExecutor extends WorkflowExecutor {

    private static final Log LOGGER = LogFactory.getLog(SubscriptionCreationWorkflowExecutor.class);
    private static final String ERROR_MSG = "Could not complete subscription creation workflow.";

    private static final String IS_TEST_ACCOUNT_ELEMENT = "TestAccount";
    private static final String ACCOUNT_NUMBER_ELEMENT = "AccountNumber";
    private static final String SUBSCRIBERS_ELEMENT = "Subscribers";

    private static final String TIER_PLAN_COMMERCIAL = "COMMERCIAL";
    private static final String TIER_PLAN_FREE = "FREE";

    private static final String WORKFLOW_REF_PARAM = "workflowReference";

    private static final String ADD_PAYMENT_METHOD_PAGE_SUFFIX = "../site/pages/pricing/add-payment-method.jag";

    private String serviceEndpoint;
    private String username;
    private String password;
    private String contentType;

    private WorkflowResponse handleFreePlan(SubscriptionWorkflowDTO subscriptionWorkflowDTO) throws WorkflowException {
        subscriptionWorkflowDTO.setStatus(APPROVED);
        WorkflowResponse workflowResponse = complete(subscriptionWorkflowDTO);
        super.publishEvents(subscriptionWorkflowDTO);
        return workflowResponse;
    }

    private WorkflowResponse handleCommercialPlan(SubscriptionWorkflowDTO subscriptionWorkflowDTO)
            throws AxisFault, XMLStreamException, WorkflowException {
        OMElement subscribers = getSubscriberInfo(subscriptionWorkflowDTO);
        OMElement subscriberOM = (OMElement) subscribers.getFirstOMChild();
        HttpWorkflowResponse httpworkflowResponse = new HttpWorkflowResponse();
        //Check subscriber information availability
        if (SUBSCRIBERS_ELEMENT.equals(subscribers.getQName().getLocalPart()) && subscriberOM != null) {
            boolean isTestAccount = Boolean.parseBoolean(
                    ((OMElement) (subscriberOM.getChildrenWithLocalName(IS_TEST_ACCOUNT_ELEMENT).next())).getText());

            //Check subscribers is a test/complementary subscriber
            if (!isTestAccount) {
                String accountNumber = ((OMElement) (subscriberOM.getChildrenWithLocalName(ACCOUNT_NUMBER_ELEMENT).next())).getText();
                httpworkflowResponse.setAdditionalParameters(WORKFLOW_REF_PARAM, subscriptionWorkflowDTO.getExternalWorkflowReference());
                if (StringUtils.isNotBlank(accountNumber)) {
                    //TODO redirecting to create the subscription
                    httpworkflowResponse.setRedirectUrl("http://www.google.lk/");
                    return httpworkflowResponse;
                } else {
                    httpworkflowResponse.setRedirectUrl(ADD_PAYMENT_METHOD_PAGE_SUFFIX);
                    return httpworkflowResponse;
                }
            } else {
                return handleFreePlan(subscriptionWorkflowDTO);
            }
        } else if (SUBSCRIBERS_ELEMENT.equals(subscribers.getQName().getLocalPart())) {
            addSubscriber(subscriptionWorkflowDTO);
            httpworkflowResponse.setRedirectUrl(ADD_PAYMENT_METHOD_PAGE_SUFFIX);
            httpworkflowResponse.setAdditionalParameters(WORKFLOW_REF_PARAM, subscriptionWorkflowDTO.getExternalWorkflowReference());
            return httpworkflowResponse;
        } else {
            throw new WorkflowException(ERROR_MSG + " element should be: " + SUBSCRIBERS_ELEMENT + " not "
                    + subscribers.getQName().getLocalPart());
        }
    }

    private OMElement getSubscriberInfo(SubscriptionWorkflowDTO subscriptionWorkflowDTO) throws AxisFault, XMLStreamException {
        ServiceClient client = WorkFlowUtils.getClient(CustomWorkFlowConstants.SOAP_ACTION_GET_SUBSCRIBER,
                serviceEndpoint, contentType, username, password);
        String payload = CustomWorkFlowConstants.SUBSCRIBER_INFO_PAYLOAD
                .replace("$1", subscriptionWorkflowDTO.getSubscriber())
                .replace("$2", subscriptionWorkflowDTO.getTenantDomain());
        OMElement element = client.sendReceive(AXIOMUtil.stringToOM(payload));
        OMTextImpl response = (OMTextImpl) (((OMElement) element.getFirstOMChild()).getFirstOMChild());

        return AXIOMUtil.stringToOM(response.getText());
    }

    private void addSubscriber(SubscriptionWorkflowDTO subscriptionWorkflowDTO) throws AxisFault, XMLStreamException {
        String payload;
        ServiceClient client;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Subscriber not available. adding subscriber");
        }

        payload = CustomWorkFlowConstants.ADD_SUBSCRIBER_PAYLOAD
                .replace("$1", subscriptionWorkflowDTO.getSubscriber())
                .replace("$2", subscriptionWorkflowDTO.getTenantDomain()).replace("$3", "false");
        client = WorkFlowUtils.getClient(CustomWorkFlowConstants.SOAP_ACTION_UPDATE_SUBSCRIBER, serviceEndpoint,
                contentType, username, password);
        client.fireAndForget(AXIOMUtil.stringToOM(payload));
    }

    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION;
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String s) throws WorkflowException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {
        super.execute(workflowDTO);
        SubscriptionWorkflowDTO subscriptionWorkflowDTO;
        if (workflowDTO instanceof SubscriptionWorkflowDTO) {
            subscriptionWorkflowDTO = (SubscriptionWorkflowDTO) workflowDTO;
        } else {
            throw new WorkflowException(ERROR_MSG + " WorkflowDTO doesn't match the required type");
        }

        try {
            Tier tier = APIUtil.getTierFromCache(subscriptionWorkflowDTO.getTierName(),
                    subscriptionWorkflowDTO.getTenantDomain());
            //Check tier information
            if (tier != null && StringUtils.isNotBlank(tier.getTierPlan())) {
                String tierPlan = tier.getTierPlan();
                switch (tierPlan) {
                    case TIER_PLAN_COMMERCIAL:
                        return handleCommercialPlan(subscriptionWorkflowDTO);
                    case TIER_PLAN_FREE:
                        return handleFreePlan(subscriptionWorkflowDTO);
                    default:
                        throw new WorkflowException(ERROR_MSG + " Tier plan " + tierPlan + " not " + "available.");
                }
            } else {
                throw new WorkflowException(ERROR_MSG + " Tier " + subscriptionWorkflowDTO.getTierName() + " not " +
                        "available or tier plan not available.");
            }
        } catch (AxisFault | XMLStreamException e) {
            throw new WorkflowException(ERROR_MSG, e);
        } catch (APIManagementException e) {
            throw new WorkflowException(ERROR_MSG + " Error occurred while querying the tier information. ", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {
        workflowDTO.setUpdatedTime(System.currentTimeMillis());
        super.complete(workflowDTO);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.info("Subscription Creation [Complete] Workflow Invoked. Workflow ID : " + workflowDTO
                    .getExternalWorkflowReference() + "Workflow State : " + workflowDTO.getStatus());
        }
        ApiMgtDAO apiMgtDAO = new ApiMgtDAO();

        try {
            switch (workflowDTO.getStatus()) {
                case APPROVED:
                    apiMgtDAO.updateSubscriptionStatus(Integer.parseInt(workflowDTO.getWorkflowReference()),
                            APIConstants.SubscriptionStatus.UNBLOCKED);
                    break;
                case REJECTED:
                    apiMgtDAO.updateSubscriptionStatus(Integer.parseInt(workflowDTO.getWorkflowReference()),
                            APIConstants.SubscriptionStatus.REJECTED);
                    break;
                default:
                    throw new WorkflowException(ERROR_MSG + "workflow status undefined. " + workflowDTO.getStatus());
            }
            return new GeneralWorkflowResponse();
        } catch (APIManagementException e) {
            throw new WorkflowException(ERROR_MSG, e);
        }
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
