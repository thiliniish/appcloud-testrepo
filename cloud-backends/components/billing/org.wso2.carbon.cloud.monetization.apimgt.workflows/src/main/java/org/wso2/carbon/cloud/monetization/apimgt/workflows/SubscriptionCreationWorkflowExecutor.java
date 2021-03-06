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
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.workflow.GeneralWorkflowResponse;
import org.wso2.carbon.apimgt.impl.workflow.HttpWorkflowResponse;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import static org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus.APPROVED;
import static org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus.REJECTED;

/**
 * API Cloud monetization specific subscription creation workflow
 * Check if monetization is enabled for tenant: this workflow should only be deployed for monetization
 * enabled tenants. Once they enable monetization for a tenant, this workflow should be automatically deployed
 * <p/>
 * Add the configuration to
 * /_system/governance/apimgt/applicationdata/workflow-extensions.xml
 * <p/>
 * ex config:
 *    <SubscriptionCreation
 *          executor="org.wso2.carbon.cloud.monetization.apimgt.workflows.SubscriptionCreationWorkflowExecutor">
 *         <Property name="serviceEndpoint">
 *              https://milestones.appfactory.wso2.com:9443/services/APICloudMonetizationService/</Property>
 *         <Property name="username">rajith.siriw.ardana.gmail.com@mustanggt350</Property>
 *         <Property name="password">Admin</Property>
 *    </SubscriptionCreation>
 */
public class SubscriptionCreationWorkflowExecutor extends AbstractSubscriptionWorkflowExecutor {

    private static final long serialVersionUID = 2409089612001513775L;

    private static final Log LOGGER = LogFactory.getLog(SubscriptionCreationWorkflowExecutor.class);
    private static final String ERROR_MSG = "Could not complete the subscription creation workflow.";

    /*Data separator for the encrypting data string*/
    private static final String DATA_SEPARATOR = ":";

    private static final String WORKFLOW_REF_PARAM = "workflowReference";
    private static final String TENANT_PARAM = "tenant";
    private static final String SELECTED_APP_PARAM = "selectedApp";
    private static final String ACTION_PARAM = "action";
    private static final String ADD_PAYMENT_PARAM_VALUE = "paymentMethod";
    private static final String SUBSCRIPTION_SUCCESS_PARAM_VALUE = "subscribed";

    private static final String MANAGE_ACCOUNT_SUFFIX = "../site/pages/pricing/manage-account.jag";

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
    protected WorkflowResponse handleFreePlan(SubscriptionWorkflowDTO subscriptionWorkflowDTO)
            throws WorkflowException {
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
     * @throws WorkflowException
     */
    protected WorkflowResponse handleCommercialPlan(SubscriptionWorkflowDTO subscriptionWorkflowDTO)
            throws WorkflowException {
        try {
            JsonObject responseObj = WorkFlowUtils.getSubscriberInfo(subscriptionWorkflowDTO.getSubscriber(),
                    subscriptionWorkflowDTO.getTenantDomain(), serviceEndpoint, contentType, username, password);
            HttpWorkflowResponse httpworkflowResponse = new HttpWorkflowResponse();
            httpworkflowResponse.setAdditionalParameters(TENANT_PARAM, subscriptionWorkflowDTO.getTenantDomain());
            httpworkflowResponse.setRedirectUrl(MANAGE_ACCOUNT_SUFFIX);
            httpworkflowResponse.setRedirectConfirmationMsg(null);

            //Encrypt and base64 encode api data
            String apiInfo = URLEncoder
                    .encode(getEncryptionInfo(subscriptionWorkflowDTO), CustomWorkFlowConstants.ENCODING);
            //Check subscriber information availability
            if (responseObj.get(CustomWorkFlowConstants.SUBSCRIBER_OBJ) != null) {
                JsonObject subscriberJsonObj = responseObj.get(CustomWorkFlowConstants.SUBSCRIBER_OBJ)
                        .getAsJsonObject();
                boolean isTestAccount = subscriberJsonObj.get(CustomWorkFlowConstants.IS_TEST_ACCOUNT_PROPERTY)
                        .getAsBoolean();

                //Check subscribers is a test/complementary subscriber
                if (!isTestAccount) {
                    String accountNumber = subscriberJsonObj.get(CustomWorkFlowConstants.ACCOUNT_NUMBER_PROPERTY)
                            .isJsonPrimitive() ?
                            subscriberJsonObj.get(CustomWorkFlowConstants.ACCOUNT_NUMBER_PROPERTY).getAsString() :
                            null;
                    httpworkflowResponse.setAdditionalParameters(WORKFLOW_REF_PARAM, apiInfo);
                    if (StringUtils.isNotBlank(accountNumber)) {
                        return createSubscription(accountNumber, subscriptionWorkflowDTO, httpworkflowResponse);
                    } else {
                        httpworkflowResponse.setAdditionalParameters(ACTION_PARAM, ADD_PAYMENT_PARAM_VALUE);
                        return httpworkflowResponse;
                    }
                } else {
                    return handleFreePlan(subscriptionWorkflowDTO);
                }
            } else if (responseObj.get(CustomWorkFlowConstants.SUBSCRIPTION_INFO_NOT_AVAILABLE).isJsonPrimitive()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Subscriber information is not available. adding subscriber");
                }
                addSubscriber(subscriptionWorkflowDTO);
                httpworkflowResponse.setAdditionalParameters(ACTION_PARAM, ADD_PAYMENT_PARAM_VALUE);
                httpworkflowResponse.setAdditionalParameters(WORKFLOW_REF_PARAM, apiInfo);
                return httpworkflowResponse;
            } else {
                throw new WorkflowException(ERROR_MSG + " Subscriber information is not available.");
            }
        } catch (AxisFault | XMLStreamException | UnsupportedEncodingException | CryptoException e) {
            throw new WorkflowException(ERROR_MSG, e);
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
        String stringBuilder =
                subscriptionWorkflowDTO.getExternalWorkflowReference() + DATA_SEPARATOR + subscriptionWorkflowDTO
                        .getTierName() + DATA_SEPARATOR + subscriptionWorkflowDTO.getApplicationName() + DATA_SEPARATOR
                        + subscriptionWorkflowDTO.getApiName() + DATA_SEPARATOR + subscriptionWorkflowDTO
                        .getApiVersion() + DATA_SEPARATOR + subscriptionWorkflowDTO.getApiProvider();
        return CryptoUtil.getDefaultCryptoUtil()
                .encryptAndBase64Encode(stringBuilder.getBytes(Charset.defaultCharset()));
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

        payload = CustomWorkFlowConstants.ADD_SUBSCRIBER_PAYLOAD.replace("$1", subscriptionWorkflowDTO.getSubscriber
                ()).replace("$2", "false");
        client = WorkFlowUtils
                .getClient(CustomWorkFlowConstants.SOAP_ACTION_UPDATE_SUBSCRIBER, serviceEndpoint, contentType,
                        username, password);
        client.fireAndForget(AXIOMUtil.stringToOM(payload));
    }

    /**
     * Create subscription for subscribers already having a paid account
     *
     * @param accountNumber           account number
     * @param subscriptionWorkflowDTO workflow DTO
     * @param httpworkflowResponse    workflow response
     * @return workflow response with added redirection url
     * @throws AxisFault
     * @throws XMLStreamException
     * @throws WorkflowException
     */
    private WorkflowResponse createSubscription(String accountNumber, SubscriptionWorkflowDTO subscriptionWorkflowDTO,
            HttpWorkflowResponse httpworkflowResponse) throws AxisFault, XMLStreamException, WorkflowException {
        String payload = CustomWorkFlowConstants.CREATE_API_SUBSCRIPTION_PAYLOAD.replace("$1", accountNumber)
                .replace("$2", subscriptionWorkflowDTO.getTierName())
                .replace("$3", subscriptionWorkflowDTO.getApplicationName())
                .replace("$4", subscriptionWorkflowDTO.getApiName())
                .replace("$5", subscriptionWorkflowDTO.getApiVersion())
                .replace("$6", subscriptionWorkflowDTO.getApiProvider());

        ServiceClient client = WorkFlowUtils
                .getClient(CustomWorkFlowConstants.SOAP_ACTION_CREATE_API_SUBSCRIPTION, serviceEndpoint, contentType,
                        username, password);

        OMElement element = client.sendReceive(AXIOMUtil.stringToOM(payload));
        OMTextImpl response = (OMTextImpl) (((OMElement) element.getFirstOMChild()).getFirstOMChild());

        if (StringUtils.isNotBlank(response.getText())) {
            JsonObject responseObj = new JsonParser().parse(response.getText().trim()).getAsJsonObject();
            if (responseObj == null) {
                throw new WorkflowException("Could not complete workflow. Subscription creation failure.");
            }

            JsonObject dataObj = responseObj.get(CustomWorkFlowConstants.RESPONSE_DATA).getAsJsonObject();
            if (responseObj.get(CustomWorkFlowConstants.RESPONSE_SUCCESS) != null
                    && dataObj.get(CustomWorkFlowConstants.MONETIZATION_TABLES_UPDATED) != null && responseObj
                    .get(CustomWorkFlowConstants.RESPONSE_SUCCESS).getAsBoolean() && dataObj.get(
                            CustomWorkFlowConstants.MONETIZATION_TABLES_UPDATED).getAsBoolean()) {
                subscriptionWorkflowDTO.setStatus(APPROVED);
                complete(subscriptionWorkflowDTO);
                httpworkflowResponse
                        .setAdditionalParameters(SELECTED_APP_PARAM, subscriptionWorkflowDTO.getApplicationName());
                httpworkflowResponse.setAdditionalParameters(ACTION_PARAM, SUBSCRIPTION_SUCCESS_PARAM_VALUE);
                return httpworkflowResponse;
            } else {
                LOGGER.error(
                        "Failure to create subscription. Tenant: " + subscriptionWorkflowDTO.getTenantDomain()
                                + " subscriber: " + accountNumber + " application: " + subscriptionWorkflowDTO
                                .getApplicationName() + " api: " + subscriptionWorkflowDTO.getApiName()
                                + " api version: " + subscriptionWorkflowDTO.getApiVersion());
                subscriptionWorkflowDTO.setStatus(REJECTED);
                return complete(subscriptionWorkflowDTO);
            }
        } else {
            throw new WorkflowException("Could not complete workflow. Subscription creation failure.");
        }
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
            throw new WorkflowException(ERROR_MSG + " WorkflowDTO doesn't match the required type.");
        }
        return handleTierPlan(subscriptionWorkflowDTO);
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
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
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
