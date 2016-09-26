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
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.cloud.signup.configReader.ConfigFileReader;
import org.wso2.carbon.cloud.signup.constants.SignUpWorkflowConstants;
import org.wso2.carbon.cloud.signup.dbAccess.DatabaseAccessor;
import org.wso2.carbon.cloud.signup.emailSender.EmailManager;
import org.wso2.carbon.cloud.signup.internal.ServiceReferenceHolder;
import org.wso2.carbon.cloud.signup.util.Util;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.tenant.mgt.services.TenantMgtAdminService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the class that gets triggered once the sign up button is clicked on the API Manager end,
 * for the self sign up feature for the tenant stores. This is the class when the tenant approval is not required for
 * self sign up
 */
public class SimpleSignUpWorkflowExecuter extends UserSignUpWorkflowExecutor {
    private static final Log LOGGER = LogFactory.getLog(SimpleSignUpWorkflowExecuter.class);

    private String contentType;
    private String serviceEndpoint;
    private String username;
    private String password;
    private String fromAddress;
    private String contactEmail;
    private String tenantDomain;
    private String tenantAwareUserName;
    private String errorMessage;
    private String uuid;
    private String userEmail;
    private String fromEmailAddress;
    private boolean notifyAllAdmins;

    //creating common instances of classes
    EmailManager emailManager;
    Util utilObject;

    public SimpleSignUpWorkflowExecuter() {

        uuid = "";
        //instantiating the email sender class
        emailManager = new EmailManager();

        //Instantiating the Util class
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

    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {
        return null;
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
            fromEmailAddress =
                    ConfigFileReader.retrieveConfigAttribute("emailProperties", "fromEmail");

            emailManager.setEmailProperyKeyValueMap(getContactAddress(getTenantDomain()),
                                                    getCloudmgtLink(), getTenantDomain(),
                                                    getFromAddress(), getAdminDashboardURL(),
                                                    getUserEmail());

            emailManager
                    .sendTenantNotificationEmail(tenantDomain, fromEmailAddress, tenantEmail,
                                                      userEmail, notifyAllAdmins);

            LOGGER.info("Workflow execution completed for the tenant domain " + tenantDomain +
                        " for the user " + userEmail);

            LOGGER.info(
                    "Calling the complete method of the self sign up workflow for " + tenantDomain +
                    " for the user " + userEmail);

            //since there is no approval directly the complete method is called.

            complete(workflowDTO);

        } catch (WorkflowException e) {
            errorMessage = "Error while carrying out the sign up workflow for the tenantDomain " +
                           tenantDomain + " and the user " + userEmail;

            LOGGER.error(errorMessage, e);
            throw new WorkflowException(errorMessage, e);
        } catch (Exception e) {
            errorMessage = "Error while carrying out the sign up workflow for the tenantDomain " +
                           tenantDomain + " and the user " + userEmail;
            LOGGER.error(errorMessage, e);
            throw new WorkflowException(errorMessage, e);
        }

        return new GeneralWorkflowResponse();
    }

    /**
     * This method gets executed once the workflow gets completed
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

            //getting the api manager configuration
            APIManagerConfiguration config =
                    ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                                          .getAPIManagerConfiguration();
            String serverURL = config.getFirstProperty(APIConstants.AUTH_MANAGER_URL);

            String tenantDomain = workflowDTO.getTenantDomain();
            setTenantDomain(tenantDomain);

            fromEmailAddress =
                    ConfigFileReader.retrieveConfigAttribute("emailProperties", "fromEmail");
            tenantAwareUserName =
                    MultitenantUtils.getTenantAwareUsername(workflowDTO.getWorkflowReference());
            userEmail = utilObject.splitSlashes(tenantAwareUserName);
            setUserEmail(userEmail);

            uuid = utilObject.generateUUID();
            LOGGER.info("Generating the UUID for the user " + userEmail + "of the tenant domain " +
                        tenantDomain);

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

                LOGGER.info("Getting the roles assigned to user " + userEmail);
                List<String> roles = SelfSignUpUtil.getRoleNames(signupConfig);

                //Retrieving the user roles in the database compatible format
                ArrayList<String> userRolesList = utilObject.getRoles(roles);
                DatabaseAccessor dbAccess = new DatabaseAccessor();

                //adding the temp registration
                dbAccess.insertToTempInviteTable(userEmail, getUuid(), tenantDomain, userRolesList);

            } catch (APIManagementException e) {
                errorMessage =
                        "Error while accessing signup configuration for tenant " + tenantDomain +
                        " and the user " + userEmail;
                LOGGER.error(errorMessage, e);
                throw new WorkflowException(errorMessage, e);
            }

            LOGGER.info("Setting the email configuration properties to be sent to the user " +
                        userEmail +
                        " of the tenant " + tenantDomain);

            emailManager.setEmailProperyKeyValueMap(getContactAddress(getTenantDomain()),
                                                    getCloudmgtLink(), getTenantDomain(),
                                                    getFromAddress(), getAdminDashboardURL(),
                                                    getUserEmail());

            LOGGER.info("Sign Up Request has been approved for the user " + userEmail);

            emailManager
                    .sendApprovalStatusEmail(tenantDomain, getContactEmail(), getFromAddress(),
                                                  userEmail,
                                                  SignUpWorkflowConstants.TENANT_APPROVAL_EMAIL_PATH,
                                                  "approved");
            LOGGER.info("Completed the Sign Up Workflow for the self signed up user " + userEmail +
                        "of the tenant domain " + tenantDomain);

        } catch (WorkflowException e) {
            errorMessage = "Workflow Exception has occurred for tenant " + tenantDomain +
                           " and the user " + userEmail;
            LOGGER.error(errorMessage, e);
            throw new WorkflowException(errorMessage, e);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new WorkflowException(e.getMessage(), e);
        }
        return new GeneralWorkflowResponse();
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

    public boolean isNotifyAllAdmins() {
        return notifyAllAdmins;
    }

    public void setNotifyAllAdmins(boolean notifyAllAdmins) {
        this.notifyAllAdmins = notifyAllAdmins;
    }

}
