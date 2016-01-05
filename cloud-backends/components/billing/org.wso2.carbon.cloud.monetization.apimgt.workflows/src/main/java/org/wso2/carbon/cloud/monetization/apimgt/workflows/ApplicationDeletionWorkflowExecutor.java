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
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.workflow.GeneralWorkflowResponse;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;

import javax.xml.stream.XMLStreamException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 *
 */
public class ApplicationDeletionWorkflowExecutor extends WorkflowExecutor {

    private static final Log LOGGER = LogFactory.getLog(ApplicationDeletionWorkflowExecutor.class);
    private static final String ERROR_MSG = "Could not complete application deletion workflow.";

    private String serviceEndpoint;
    private String username;
    private String password;
    private String contentType;

    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_APPLICATION_DELETION;
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String s) throws WorkflowException {
        return null;
    }

    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {
        ApplicationWorkflowDTO applicationWorkflowDTO;
        if (workflowDTO instanceof ApplicationWorkflowDTO) {
            applicationWorkflowDTO = (ApplicationWorkflowDTO) workflowDTO;
        } else {
            throw new WorkflowException(ERROR_MSG + " WorkflowDTO doesn't match the required type");
        }
        try {
            JsonObject responseObj = WorkFlowUtils.getSubscriberInfo(applicationWorkflowDTO.getUserName(),
                    applicationWorkflowDTO.getTenantDomain(), serviceEndpoint, contentType, username, password);
            if (responseObj.get(CustomWorkFlowConstants.SUBSCRIBERS_OBJ).isJsonObject()) {
                if (responseObj.get(CustomWorkFlowConstants.SUBSCRIBERS_OBJ).isJsonObject()) {
                    JsonObject subscriber = responseObj.getAsJsonObject(CustomWorkFlowConstants.SUBSCRIBERS_OBJ)
                            .getAsJsonObject(CustomWorkFlowConstants.SUBSCRIBER_OBJ);
                    boolean isTestAccount = subscriber.get(CustomWorkFlowConstants.IS_TEST_ACCOUNT_PROPERTY).getAsBoolean();

                    //Check subscribers is a test/complementary subscriber
                    if (!isTestAccount) {

                    }
                }
            } else {
                throw new WorkflowException(ERROR_MSG + " Subscriber information not available.");
            }
            applicationWorkflowDTO.setStatus(WorkflowStatus.APPROVED);
            complete(applicationWorkflowDTO);
            super.publishEvents(applicationWorkflowDTO);
            return new GeneralWorkflowResponse();
        }  catch (AxisFault | XMLStreamException e) {
            throw new WorkflowException(ERROR_MSG, e);
        }
    }

    @Override
    public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {
        ApiMgtDAO apiMgtDAO = new ApiMgtDAO();
        ApplicationWorkflowDTO applicationWorkflowDTO = (ApplicationWorkflowDTO) workflowDTO;
        Application application = applicationWorkflowDTO.getApplication();
        Connection conn = null;
        String errorMsg;
        try {
            conn = APIMgtDBUtil.getConnection();
            conn.setAutoCommit(false);
            apiMgtDAO.deleteApplication(application, conn);
            conn.commit();
        } catch (APIManagementException e) {
            if (e.getMessage() == null) {
                errorMsg = "Couldn't complete simple application deletion workflow for application: " + application
                        .getName();
            } else {
                errorMsg = e.getMessage();
            }
            throw new WorkflowException(errorMsg, e);
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.error("Failed to rollback remove application ", ex);
                }
            }
            errorMsg = "Couldn't remove application entry for application: " + application.getName();
            throw new WorkflowException(errorMsg, e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                LOGGER.error("Couldn't close database connection of delete application workflow", e);
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
