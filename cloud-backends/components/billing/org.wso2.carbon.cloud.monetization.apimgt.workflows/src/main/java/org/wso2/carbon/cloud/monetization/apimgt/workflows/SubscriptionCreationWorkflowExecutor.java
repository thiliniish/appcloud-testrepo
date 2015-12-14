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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;

import javax.xml.stream.XMLStreamException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
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

    /*Data separator for the encrypting data string*/
    private static final String DATA_SEPARATOR = ":";

    private static final String IS_TEST_ACCOUNT_PROPERTY = "TestAccount";
    private static final String ACCOUNT_NUMBER_PROPERTY = "AccountNumber";
    private static final String SUBSCRIBERS_OBJ = "Subscribers";
    private static final String SUBSCRIBER_OBJ = "Subscriber";

    private static final String TIER_PLAN_COMMERCIAL = "COMMERCIAL";
    private static final String TIER_PLAN_FREE = "FREE";

    private static final String WORKFLOW_REF_PARAM = "workflowReference";

    private static final String ADD_PAYMENT_METHOD_PAGE_SUFFIX = "../site/pages/pricing/add-payment-method.jag";

    private String serviceEndpoint;
    private String username;
    private String password;
    private String contentType;

    /**
     * Handling the free plan subscription by completing the workflow
     *
     * @param subscriptionWorkflowDTO subscription workflow DTO
     * @return Workflow Response
     * @throws WorkflowException
     */
    private WorkflowResponse handleFreePlan(SubscriptionWorkflowDTO subscriptionWorkflowDTO) throws WorkflowException {
        subscriptionWorkflowDTO.setStatus(APPROVED);
        WorkflowResponse workflowResponse = complete(subscriptionWorkflowDTO);
        super.publishEvents(subscriptionWorkflowDTO);
        return workflowResponse;
    }

    /**
     * Handle commercial plan subscription
     *
     * @param subscriptionWorkflowDTO subscription workflow DTO
     * @return Workflow Response
     * @throws AxisFault
     * @throws XMLStreamException
     * @throws WorkflowException
     * @throws CryptoException
     * @throws UnsupportedEncodingException
     */
    private WorkflowResponse handleCommercialPlan(SubscriptionWorkflowDTO subscriptionWorkflowDTO)
            throws AxisFault, XMLStreamException, WorkflowException, CryptoException, UnsupportedEncodingException {
        JsonObject responseObj = getSubscriberInfo(subscriptionWorkflowDTO);
        if (responseObj == null || responseObj.get(SUBSCRIBERS_OBJ) == null) {
            throw new WorkflowException(ERROR_MSG + " Subscriber information not available.");
        }
        HttpWorkflowResponse httpworkflowResponse = new HttpWorkflowResponse();
        httpworkflowResponse.setRedirectConfirmationMsg(null);

        //Encrypt and base64 encode api data
        String apiInfo = URLEncoder.encode(getEncryptionInfo(subscriptionWorkflowDTO), CustomWorkFlowConstants.ENCODING);
        //Check subscriber information availability
        if (responseObj.get(SUBSCRIBERS_OBJ).isJsonObject()) {
            JsonObject subscriber = responseObj.getAsJsonObject(SUBSCRIBERS_OBJ).getAsJsonObject(SUBSCRIBER_OBJ);
            boolean isTestAccount = subscriber.get(IS_TEST_ACCOUNT_PROPERTY).getAsBoolean();

            //Check subscribers is a test/complementary subscriber
            if (!isTestAccount) {
                String accountNumber = subscriber.get(ACCOUNT_NUMBER_PROPERTY).isJsonPrimitive() ? subscriber.get
                        (ACCOUNT_NUMBER_PROPERTY).getAsString() : null;
                httpworkflowResponse.setAdditionalParameters(WORKFLOW_REF_PARAM, apiInfo);
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
        } else if (responseObj.get(SUBSCRIBERS_OBJ).isJsonPrimitive()) {
            addSubscriber(subscriptionWorkflowDTO);
            httpworkflowResponse.setRedirectUrl(ADD_PAYMENT_METHOD_PAGE_SUFFIX);
            httpworkflowResponse.setAdditionalParameters(WORKFLOW_REF_PARAM, apiInfo);
            return httpworkflowResponse;
        } else {
            throw new WorkflowException(ERROR_MSG + " Subscriber information not available.");
        }
    }

    /**
     * Encrypt the parameters using the default Crypto utility. and base 64 encode
     *
     * @param subscriptionWorkflowDTO Subscription Workflow DTO
     * @return base64encoded encrypted string
     * @throws CryptoException
     */
    private String getEncryptionInfo(SubscriptionWorkflowDTO subscriptionWorkflowDTO) throws CryptoException {
        String stringBuilder = subscriptionWorkflowDTO.getExternalWorkflowReference() + DATA_SEPARATOR +
                subscriptionWorkflowDTO.getTierName() + DATA_SEPARATOR + subscriptionWorkflowDTO.getApplicationName() +
                DATA_SEPARATOR + subscriptionWorkflowDTO.getApiName() + DATA_SEPARATOR + subscriptionWorkflowDTO.getApiVersion();
        return CryptoUtil.getDefaultCryptoUtil()
                .encryptAndBase64Encode(stringBuilder.getBytes(Charset.defaultCharset()));
    }

    /**
     * Retrieve subscribers information from the persistence
     *
     * @param subscriptionWorkflowDTO Subscription Workflow DTO
     * @return subscribers details as a JsonObject
     * @throws AxisFault
     * @throws XMLStreamException
     */
    private JsonObject getSubscriberInfo(SubscriptionWorkflowDTO subscriptionWorkflowDTO) throws AxisFault, XMLStreamException {
        ServiceClient client = WorkFlowUtils.getClient(CustomWorkFlowConstants.SOAP_ACTION_GET_SUBSCRIBER,
                serviceEndpoint, contentType, username, password);
        String payload = CustomWorkFlowConstants.SUBSCRIBER_INFO_PAYLOAD
                .replace("$1", subscriptionWorkflowDTO.getSubscriber())
                .replace("$2", subscriptionWorkflowDTO.getTenantDomain());
        OMElement element = client.sendReceive(AXIOMUtil.stringToOM(payload));
        OMTextImpl response = (OMTextImpl) (((OMElement) element.getFirstOMChild()).getFirstOMChild());

        if (StringUtils.isNotBlank(response.getText())) {
            return new JsonParser().parse(response.getText().trim()).getAsJsonObject();
        } else {
            return null;
        }
    }

    /**
     * Add subscriber information to persistence
     *
     * @param subscriptionWorkflowDTO Subscription Workflow DTO
     * @throws AxisFault
     * @throws XMLStreamException
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION;
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
        } catch (AxisFault | XMLStreamException | UnsupportedEncodingException | CryptoException e) {
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
            LOGGER.debug("Subscription Creation [Complete] Workflow Invoked. Workflow ID : " + workflowDTO
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
