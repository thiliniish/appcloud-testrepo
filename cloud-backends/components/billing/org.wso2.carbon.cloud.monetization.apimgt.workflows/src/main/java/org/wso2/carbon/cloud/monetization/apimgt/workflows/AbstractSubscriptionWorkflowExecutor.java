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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutor;

/**
 * Abstract workflow executor
 */
public abstract class AbstractSubscriptionWorkflowExecutor extends WorkflowExecutor {

    protected static final String ERROR_MSG = "Could not complete workflow.";

    protected abstract WorkflowResponse handleFreePlan(SubscriptionWorkflowDTO subscriptionWorkflowDTO) throws WorkflowException;

    protected abstract WorkflowResponse handleCommercialPlan(SubscriptionWorkflowDTO subscriptionWorkflowDTO) throws
            WorkflowException;

    /**
     * This is to handle the workflow according to the tier plan of the tier. Which is either COMMERCIAL or FREE
     *
     * @param subscriptionWorkflowDTO Subscription workflow DTO
     * @return Workflow response
     * @throws WorkflowException
     */
    protected WorkflowResponse handleTierPlan(SubscriptionWorkflowDTO subscriptionWorkflowDTO) throws WorkflowException {
        try {
            Tier tier = APIUtil.getTierFromCache(subscriptionWorkflowDTO.getTierName(),
                    subscriptionWorkflowDTO.getTenantDomain());
            //Check tier information
            if (tier != null && StringUtils.isNotBlank(tier.getTierPlan())) {
                String tierPlan = tier.getTierPlan();
                switch (tierPlan) {
                    case CustomWorkFlowConstants.TIER_PLAN_COMMERCIAL:
                        return handleCommercialPlan(subscriptionWorkflowDTO);
                    case CustomWorkFlowConstants.TIER_PLAN_FREE:
                        return handleFreePlan(subscriptionWorkflowDTO);
                    default:
                        throw new WorkflowException(ERROR_MSG + " Tier plan " + tierPlan + " not " + "available.");
                }
            } else {
                throw new WorkflowException(ERROR_MSG + " Tier " + subscriptionWorkflowDTO.getTierName() + " not " +
                        "available or tier plan not available.");
            }
        } catch (APIManagementException e) {
            throw new WorkflowException(ERROR_MSG + " Error occurred while querying the tier information. ", e);
        }
    }
}
