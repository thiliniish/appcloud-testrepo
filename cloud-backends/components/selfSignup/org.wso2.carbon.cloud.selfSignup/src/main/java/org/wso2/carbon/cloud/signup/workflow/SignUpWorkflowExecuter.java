/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.cloud.signup.workflow;

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.UserRegistrationConfigDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.SelfSignUpUtil;
import org.wso2.carbon.apimgt.impl.workflow.GeneralWorkflowResponse;
import org.wso2.carbon.apimgt.impl.workflow.UserSignUpWorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;
import org.wso2.carbon.cloud.signup.configReader.ConfigFileReader;
import org.wso2.carbon.cloud.signup.constants.SignUpWorkflowConstants;
import org.wso2.carbon.cloud.signup.dbAccess.DatabaseAccessor;
import org.wso2.carbon.cloud.signup.emailSender.EmailManager;
import org.wso2.carbon.cloud.signup.internal.ServiceReferenceHolder;
import org.wso2.carbon.cloud.signup.util.Util;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.tenant.mgt.services.TenantMgtAdminService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.naming.AuthenticationException;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the class that gets triggered once the sign up button is clicked on the API Manager end,
 * for the self sign up feature for the tenant stores. This is the class where the tenant approval is required for
 * self sign up
 */
public class SignUpWorkflowExecuter extends UserSignUpWorkflowExecutor {
    private static final Log LOGGER = LogFactory.getLog(SignUpWorkflowExecuter.class);

    //initializing workflow specific variables
    private String username;
    private String password;

    private String fromAddress;
    private String contentType;
    private String serviceEndpoint;
    private String contactEmail;
    private String tenantDomain;
    private String userEmail;
    private String tenantAwareUserName;

    private String errorMessage;
    private String uuid;
    private String fromEmailAddress;

    //creating common instances of classes
    EmailManager emailManager;
    Util utilObject;

    public SignUpWorkflowExecuter() {
        uuid = "";

        //initializing the email configuration class
        emailManager = new EmailManager();

        //initializing the Util class
        utilObject = new Util();
    }

    /**
     * This methods gives the type of the workflow
     *
     * @return the workflow type
     */
    public String getWorkflowType() {
        return SignUpWorkflowConstants.WORKFLOW_TYPE;
    }

    /**
     * This method gets executed when the user provides the email and signs up to the tenant.
     *
     * @param workflowDTO is the workflow object containg the workflow specific details.
     * @throws WorkflowException
     */
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Executing User SignUp Webservice Workflow for " +
                         workflowDTO.getWorkflowReference());
        }
        TenantMgtAdminService service = new TenantMgtAdminService();

        try {

            //sender email address to be set for the emails.
            fromEmailAddress =
                    ConfigFileReader.retrieveConfigAttribute("emailProperties", "fromEmail");

            //Getting the tenant Domain of the workflow
            tenantDomain = workflowDTO.getTenantDomain();

            //setting the tenant Domain of the workflow
            setTenantDomain(tenantDomain);

            //Getting the tenant's email address from the tenant Domain
            TenantInfoBean tenantInfo = service.getTenant(tenantDomain);
            String tenantEmail = tenantInfo.getEmail();
            tenantAwareUserName =
                    MultitenantUtils.getTenantAwareUsername(workflowDTO.getWorkflowReference());

            //getting the username of the user that has signed up from the custom method
            userEmail = utilObject.splitSlashes(tenantAwareUserName);

            //setting the user of the workflow
            setUserEmail(userEmail);

            emailManager.setEmailProperyKeyValueMap(getContactAddress(getTenantDomain()),
                                                    getCloudmgtLink(), getTenantDomain(),
                                                    getFromAddress(), getAdminDashboardURL(),
                                                    getUserEmail());

            LOGGER.info("The tenant domain of the sign up workflow for the tenant " + tenantEmail +
                        " is " + tenantDomain);
            LOGGER.info("Calling the Service client instance");

            //Getting the Service reference for the sign up service
            //Existing code block
            ServiceClient client = new ServiceClient(
                    ServiceReferenceHolder.getInstance().getContextService()
                                          .getClientConfigContext(), null);
            Options options = new Options();

            LOGGER.info("Setting the workflow constants");
            options.setAction(WorkflowConstants.REGISTER_USER_WS_ACTION);
            options.setTo(new EndpointReference(serviceEndpoint));

            if (contentType != null) {
                options.setProperty(Constants.Configuration.MESSAGE_TYPE, contentType);
            }

            HttpTransportProperties.Authenticator auth =
                    new HttpTransportProperties.Authenticator();

            //Consider this as a secured service if username and password are not null. Unsecured if not.
            //This grants the access to the endpoint where the workflow serivce is hosted

            if (username != null && password != null) {
                auth.setUsername(username);
                auth.setPassword(password);
                auth.setPreemptiveAuthentication(true);
                List<String> authSchemes = new ArrayList<String>();
                authSchemes.add(HttpTransportProperties.Authenticator.BASIC);
                auth.setAuthSchemes(authSchemes);

                if (contentType == null) {
                    options.setProperty(Constants.Configuration.MESSAGE_TYPE,
                                        HTTPConstants.MEDIA_TYPE_APPLICATION_XML);
                }
                options.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE,
                                    auth);
                options.setManageSession(true);
            }

            client.setOptions(options);

            //get the default empty payload
            String payload = WorkflowConstants.REGISTER_USER_PAYLOAD;

            String callBackURL = workflowDTO.getCallbackUrl();

            //Passing the values to the workflow payload accessed by the sign up human task

            //modified the payload to pass only the user name ommiting the PRIMARY attribute
            payload = payload.replace("$1", userEmail);
            payload = payload.replace("$2", workflowDTO.getTenantDomain());
            payload = payload.replace("$3", workflowDTO.getExternalWorkflowReference());
            payload = payload.replace("$4", callBackURL != null ? callBackURL : "?");

            client.fireAndForget(AXIOMUtil.stringToOM(payload));

            //configuring the properties and content needed for the email sent to the tenant
            emailManager
                    .sendTenantEmail(tenantDomain, fromEmailAddress, tenantEmail, userEmail);

            //configuring the properties and content needed for the email sent to the user regarding the status of the approval.
            emailManager.sendUserNotificationEmail(tenantDomain, getContactEmail(),
                                                        getFromAddress(), userEmail);
            super.execute(workflowDTO);

            LOGGER.info("Workflow execution completed for the user " + userEmail +
                        " of the the tenant " +
                        tenantDomain + ", Awaiting the administrator approval");
        } catch (AuthenticationException authError) {
            errorMessage =
                    "Error Authenticating the self sign up workflow for tenant " + tenantDomain +
                    " and the user " + userEmail;
            LOGGER.error(errorMessage, authError);
            throw new WorkflowException(errorMessage, authError);
        } catch (APIManagementException e) {
            errorMessage = "Error while accessing signup configuration for tenant " + tenantDomain +
                           " and the user " + userEmail;
            LOGGER.error(errorMessage, e);
            throw new WorkflowException(errorMessage, e);
        } catch (AxisFault axisFault) {
            errorMessage = "Error sending out message for the self sign up workflow for tenant " +
                           tenantDomain + " and the user " + userEmail;
            LOGGER.error(errorMessage, axisFault);
            throw new WorkflowException(errorMessage, axisFault);
        } catch (XMLStreamException e) {
            errorMessage = "Error converting String to OMElement for tenant " + tenantDomain +
                           " and the user " + userEmail;
            LOGGER.error(errorMessage, e);
            throw new WorkflowException(errorMessage, e);
        } catch (Exception e) {
            errorMessage = "Exception occurred for the tenant " + tenantDomain + " and the user " +
                           userEmail;
            LOGGER.error(e.getMessage());
            throw new WorkflowException(e.getMessage(), e);
        }
        return new GeneralWorkflowResponse();
    }

    /**
     * This method gets executed once the tenant approves/rejects the request to sign up to the API store
     *
     * @param workflowDTO consists the workflow related details.
     * @throws WorkflowException
     */
    public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {

        //Changing status of the workflow after admin changes status
        workflowDTO.setStatus(workflowDTO.getStatus());
        workflowDTO.setUpdatedTime(System.currentTimeMillis());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("User Sign Up [Complete] Workflow Invoked. Workflow ID : " +
                         workflowDTO.getExternalWorkflowReference() + "Workflow State : " +
                         workflowDTO.getStatus());
        }

        try {

            super.complete(workflowDTO);
            tenantDomain = workflowDTO.getTenantDomain();
            fromEmailAddress =
                    ConfigFileReader.retrieveConfigAttribute("emailProperties", "fromEmail");
            //getting the api manager configuration
            APIManagerConfiguration config =
                    ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                                          .getAPIManagerConfiguration();
            String serverURL = config.getFirstProperty(APIConstants.AUTH_MANAGER_URL);

            String tenantDomain = workflowDTO.getTenantDomain();
            setTenantDomain(tenantDomain);

            tenantAwareUserName =
                    MultitenantUtils.getTenantAwareUsername(workflowDTO.getWorkflowReference());
            userEmail = utilObject.splitSlashes(tenantAwareUserName);
            setUserEmail(userEmail);
            uuid = utilObject.generateUUID();
            setUuid(uuid);

            try {

                //getting the configurations of the tenant
                int tenantId =
                        ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                                              .getTenantId(tenantDomain);
                APIUtil.loadTenantRegistry(tenantId);
                LOGGER.info("Loaded the tenant registry for the tenant " + tenantDomain);
                UserRegistrationConfigDTO signupConfig =
                        SelfSignUpUtil.getSignupConfiguration(tenantDomain);

                String adminUsername = signupConfig.getAdminUserName();
                String adminPassword = signupConfig.getAdminPassword();
                LOGGER.info(
                        "Connecting to the Authentication Manager with server url " + serverURL +
                        ",adminUsername " + adminUsername + " and adminPassword:xxxxxxxx");
                if (serverURL == null || adminUsername == null || adminPassword == null) {
                    errorMessage = "Required parameter missing to connect to the"
                                   + " authentication manager";
                    LOGGER.error(errorMessage);
                    throw new WorkflowException("Required parameter missing to connect to the"
                                                + " authentication manager");
                }

                //method to be executed if the workflow is approved
                if (WorkflowStatus.APPROVED.equals(workflowDTO.getStatus())) {

                    LOGGER.info("Getting the roles assigned to user " + userEmail);
                    List<String> roles = SelfSignUpUtil.getRoleNames(signupConfig);

                    //Retriving the user roles in the database compatible format
                    ArrayList<String> userRolesList = utilObject.getRoles(roles);
                    DatabaseAccessor dbAccess = new DatabaseAccessor();

                    //adding the temp registration

                    dbAccess.insertToTempInviteTable(userEmail, getUuid(), tenantDomain,
                                                     userRolesList);

                }
            } catch (APIManagementException e) {
                errorMessage =
                        "Error while accessing signup configuration for tenant " + tenantDomain;
                LOGGER.error(errorMessage, e);
                throw new WorkflowException(errorMessage, e);
            }

            emailManager.setEmailProperyKeyValueMap(getContactAddress(getTenantDomain()),
                                                    getCloudmgtLink(), getTenantDomain(),
                                                    getFromAddress(), getAdminDashboardURL(),
                                                    getUserEmail());

            if (WorkflowStatus.APPROVED.equals(workflowDTO.getStatus())) {

                LOGGER.info("Sign Up Request has been approved for the user " + userEmail);

                emailManager.sendApprovalStatusEmail(tenantDomain, getContactEmail(),
                                                          getFromAddress(), userEmail,
                                                          SignUpWorkflowConstants.TENANT_APPROVAL_EMAIL_PATH,
                                                          "approved");
            } else {

                emailManager.sendApprovalStatusEmail(tenantDomain, getContactEmail(),
                                                          getFromAddress(), userEmail,
                                                          SignUpWorkflowConstants.TENANT_REJECTION_EMAIL_PATH,
                                                          "rejected");
            }
            LOGGER.info("Completed the Sign Up Workflow for the self signed up user " + userEmail +
                        " of the tenant domain " + tenantDomain);
        } catch (WorkflowException e) {
            errorMessage = "Workflow Exception has occurred for tenant " + tenantDomain +
                           " and the user " + userEmail;
            LOGGER.error(errorMessage, e);
            throw new WorkflowException(errorMessage, e);
        } catch (Exception e) {
            errorMessage = "Exception occurred for the tenant " + tenantDomain + " and the user " +
                           userEmail;
            LOGGER.error(e.getMessage());
            throw new WorkflowException(e.getMessage(), e);
        }
        return new GeneralWorkflowResponse();
    }

    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {
        return null;
    }

    /**
     * This method gives the contact address of the tenant
     *
     * @param tenantDomain is the tenant domain
     * @return the contact email of the tenant.
     */
    public String getContactAddress(String tenantDomain) {
        String contactEmail = getContactEmail();
        if (contactEmail.equalsIgnoreCase("")) {
            contactEmail = tenantDomain + " store owner";
        }
        return contactEmail;

    }

    /**
     * This method retrieves the admin dashboard link
     *
     * @return the url of the admin dashboard
     * @throws WorkflowException
     */
    public String getAdminDashboardURL() throws WorkflowException {

        String adminDashboardUrl;
        try {
            adminDashboardUrl = ConfigFileReader.retrieveConfigAttribute("URLs",
                                                                         "adminDashboardUrl");
        } catch (WorkflowException e) {
            errorMessage = "";
            LOGGER.error(errorMessage, e);
            throw new WorkflowException(errorMessage, e);
        }
        return adminDashboardUrl;
    }

    /**
     * @return the cloud mgt url to send the verification link to the user
     * @throws WorkflowException
     */
    public String getCloudmgtLink() throws WorkflowException {
        String cloudmgtUrl;
        try {
            LOGGER.info("Obtaining the UUID for the user " + getUserEmail());
            cloudmgtUrl = ConfigFileReader.retrieveConfigAttribute("URLs",
                                                                   "cloudMgtVerificationUrl");
            cloudmgtUrl = cloudmgtUrl + "?confirmation=" + uuid + "&isStoreInvitee=true";
        } catch (WorkflowException e) {
            errorMessage = "Error in setting the cloudmgt url for the tenant " + tenantDomain;
            LOGGER.error(errorMessage, e);
            throw new WorkflowException(errorMessage, e);
        }
        return cloudmgtUrl;

    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    protected String getUuid() {
        if (uuid == null) {
            uuid = utilObject.generateUUID();
        }
        return this.uuid;
    }

    protected void setUuid(String uuid) {
        this.uuid = uuid;
    }

    //getters and setters for the sign up configuration specific details
    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServiceEndpoint() {
        return this.serviceEndpoint;
    }

    public void setServiceEndpoint(String serviceEndpoint) {
        this.serviceEndpoint = serviceEndpoint;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

}
