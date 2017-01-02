/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License
 */

package org.wso2.carbon.cloud.throttling;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.commons.throttle.core.*;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.throttling.APIThrottleConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.cloud.throttling.common.CloudThrottlingException;
import org.wso2.carbon.cloud.throttling.common.Constants;
import org.wso2.carbon.cloud.throttling.common.RatePlanDTO;
import org.wso2.carbon.cloud.throttling.internal.ThrottleDataHolder;
import org.wso2.carbon.context.CarbonContext;

import java.util.Map;

/**
 * This is the custom handle class for implementing tenant based throttling
 */
public class CloudThrottleHandler extends AbstractHandler {

    private static final Log LOG = LogFactory.getLog(CloudThrottleHandler.class);
    /**
     * Role Based access rate controller for tenant level throttling
     */
    private RoleBasedAccessRateController tenantRoleBasedAccessController;
    private volatile Throttle throttle;
    /**
     * Does this env. support clustering
     */
    private boolean isClusteringEnable = false;

    public CloudThrottleHandler() {
        tenantRoleBasedAccessController = new RoleBasedAccessRateController();
    }

    /* setter method required by the synapse to create api
     * @param id
     */
    public static void setId(String id) {
        ThrottleDataHolder.setId(id);
    }

    public boolean handleRequest(MessageContext messageContext) {
        return doThrottle(messageContext);
    }

    public boolean handleResponse(MessageContext messageContext) {
        return doThrottle(messageContext);
    }

    private boolean doThrottle(MessageContext messageContext) {
        boolean canAccess = true;

        boolean isResponse = messageContext.isResponse();
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        ConfigurationContext cc = axis2MC.getConfigurationContext();
        if (cc == null) {
            LOG.error("Error while retrieving ConfigurationContext from messageContext");
            return true;
        }

        //get the tenant domain..
        final String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        final int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Check for clustering enabled. isClusteringEnable = " + isClusteringEnable);
        }
        try {
            throttle = ThrottleDataHolder.getInstance().getThrottle(tenantDomain, tenantId);
        } catch (CloudThrottlingException e) {
            LOG.warn("Exception in creating throttle from policy key " + Constants.POLICY_KEY, e);
        }
        //if this is a request message then do tenant based throttling
        if (!isResponse && throttle != null) {
            //look for subscription plan in the cache
            if (TenantCache.isCached(tenantDomain)) {
                //found in the cache
                RatePlanDTO ratePlanDTO = TenantCache.getRatePlan(tenantDomain);
                String productRatePlanId = ratePlanDTO.getRatePlan();
                ratePlanDTO.setLastAccessTime(System.currentTimeMillis());
                // Domain name based throttling
                //check whether a configuration has been defined for this role name or not
                //loads the ThrottleContext
                ThrottleContext roleBasedContext = throttle.getThrottleContext(Constants.ROLE_BASED_THROTTLE_KEY);

                if (roleBasedContext == null) {
                    LOG.warn("Unable to load throttle context");
                    return true;
                }
                //Loads the ThrottleConfiguration
                try {
                    // do role based throttling according to the ratePlan
                    //tenantDomain is used as a unique key
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Applying tenant based throttling policy for tenantDomain = " + tenantDomain + " and ratePlan " + productRatePlanId);
                    }

                    AccessInformation info = tenantRoleBasedAccessController.canAccess(roleBasedContext, tenantDomain, productRatePlanId);
                    if (info != null) {
                        canAccess = info.isAccessAllowed();
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Throttle by Tenant Domain " + tenantDomain);
                            LOG.debug("Is allowed to access the api = " + canAccess);
                        }
                    }
                } catch (ThrottleException e) {
                    LOG.warn("Exception occurred while performing tenant based throttling", e);
                    canAccess = false;
                }
            } else {
                //get the rate plan from cloudmgt db
                String productRatePlanId = getProductRatePlanId(tenantDomain);

                //Tenants adding to the cache
                TenantCache.updateCache(tenantDomain, productRatePlanId);
                canAccess = true; //allow initial request to pass through the throttle handler without waiting for the ratePlan info from zuora
            }
        }
        if (!canAccess) {
            handleThrottleOut(messageContext);
        }

        return canAccess;

    }

    /*
    * Get the product rate plan id from cloudmgt db for a given tenant domain
    * @param String tenantDomain
    * @return String productRatePlanId
    * */
    private String getProductRatePlanId(String tenantDomain) {
        // look for type in cloud mgt db
        CloudMgtDAO cloudMgtDAO = new CloudMgtDAO();
        String productRatePlanId = Constants.RATE_PLAN_DEFAULT;
        try {
            String type = cloudMgtDAO.getSubscriptionType(tenantDomain, ThrottleDataHolder.getCloudType());
            if (type != null && type.equalsIgnoreCase(ThrottleDataHolder.SubscriptionType.PAID.name())) {
                // if the type is PAID, get the product rate plan id from the cloudmgt db
                String accountNumber = cloudMgtDAO.getAccountNumber(tenantDomain);
                productRatePlanId = cloudMgtDAO.getProductRatePlanId(accountNumber, ThrottleDataHolder.getCloudType());
            }

        } catch (CloudThrottlingException e) {
            LOG.error("Exception while requesting data from cloudmgt db.", e);
        }
        return productRatePlanId;
    }

    /*
    * Create faulty payload
    * @return OMElement payload
    * */
    private OMElement getFaultPayload() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace(APIThrottleConstants.API_THROTTLE_NS,
                APIThrottleConstants.API_THROTTLE_NS_PREFIX);
        OMElement payload = fac.createOMElement("fault", ns);
        OMElement errorCode = fac.createOMElement("code", ns);
        errorCode.setText(String.valueOf(APIThrottleConstants.THROTTLE_OUT_ERROR_CODE));
        OMElement errorMessage = fac.createOMElement("message", ns);
        errorMessage.setText("Message Throttled Out");
        OMElement errorDetail = fac.createOMElement("description", ns);
        errorDetail.setText("Your request was blocked due to exceeding the allocated quota. Please contact the API Store owner to resolve this.");

        payload.addChild(errorCode);
        payload.addChild(errorMessage);
        payload.addChild(errorDetail);
        return payload;
    }

    /*
    * Handles actions after a throttle out
    * @param MessageContext messageContext
    * */
    private void handleThrottleOut(MessageContext messageContext) {
        messageContext.setProperty(SynapseConstants.ERROR_CODE, 900800);
        messageContext.setProperty(SynapseConstants.ERROR_MESSAGE, "Message throttled out");

        Mediator sequence = messageContext.getSequence(APIThrottleConstants.API_THROTTLE_OUT_HANDLER);
        // Invoke the custom error handler specified by the user
        if (sequence != null && !sequence.mediate(messageContext)) {
            // If needed user should be able to prevent the rest of the fault handling
            // logic from getting executed
            return;
        }
        // By default we send a 503 response back
        if (messageContext.isDoingPOX() || messageContext.isDoingGET()) {
            Utils.setFaultPayload(messageContext, getFaultPayload());
        } else {
            Utils.setSOAPFault(messageContext, "Server", "Message Throttled Out",
                    "Your request was blocked due to exceeding the allocated quota. Please contact the API Store owner to resolve this.");
        }
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();

        Utils.sendFault(messageContext, APIThrottleConstants.SC_TOO_MANY_REQUESTS);

    }

}













