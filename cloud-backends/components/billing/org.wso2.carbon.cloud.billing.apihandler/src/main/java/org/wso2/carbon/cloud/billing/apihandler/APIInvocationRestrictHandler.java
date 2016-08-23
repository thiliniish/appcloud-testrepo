/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
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

package org.wso2.carbon.cloud.billing.apihandler;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2Sender;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.cloud.billing.apihandler.utils.LRUCache;

import java.sql.SQLException;
import java.util.Map;
import javax.naming.NamingException;

/**
 * API handler to block api invocation after the tenant deactivate from API Cloud
 * This handler check the billing status related to the tenant and api_cloud and block
 * api calls if necessary
 */
public class APIInvocationRestrictHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(APIInvocationRestrictHandler.class);
    Boolean isValidAccount = false;
    DBConnector dbConnection = null;
    private static LRUCache lruCache = new LRUCache(APIInvocationRestrictHandlerConstants.CASH_SIZE);

    public boolean handleRequest(MessageContext messageContext) {
        return doApiInvocation(messageContext);
    }

    private Boolean checkUserAccountValidity(String tenantName) {

        String tenantStatus = lruCache.get(tenantName);
        //checking the user account validity for API call
        try {
            if (tenantStatus == null) {
                log.info("Tenant data is not available in cash");
                dbConnection = new DBConnector();
                tenantStatus = dbConnection.getTenantStatus(tenantName);
                if (tenantStatus != null && tenantStatus
                        .equals(APIInvocationRestrictHandlerConstants.BILLING_INVOCATION_RESTRICTED_STATUS)) {
                    log.info("Adding tenant data to cash");
                    lruCache.set(tenantName);
                    log.warn("Account is disabled for tenant " + tenantName);
                    return false;
                }

            }

            if (tenantStatus != null && tenantStatus.equals(tenantName)) {
                log.warn("Account is disabled for tenant " + tenantName);
                return false;
            }

        } catch (NamingException e) {
            log.error("Error while checking user account validity - " + tenantName + " " + e.getMessage(), e);
        } catch (SQLException e) {
            log.error("Error while checking user account Status - " + tenantName + " " + e.getMessage(), e);
        }
        //By default return true, if any db connection issue occurs user should be able to get the result in API CALL
        return true;
    }

    public boolean handleResponse(MessageContext messageContext) {
        return isValidAccount;
    }

    private boolean doApiInvocation(MessageContext messageContext) {
        String tenantName = (String) messageContext.getProperty("API_PUBLISHER");
        if (log.isDebugEnabled()) {
            log.debug("Invoking the APIInvocationHandler for tenant -" + tenantName);
        }
        isValidAccount = checkUserAccountValidity(tenantName);
        if (!isValidAccount) {
            inactiveTenantInvocationOut(messageContext);
        }

        return isValidAccount;
    }

    private void inactiveTenantInvocationOut(MessageContext messageContext) {
        messageContext.setProperty(SynapseConstants.ERROR_DETAIL,
                APIInvocationRestrictHandlerConstants.BILLING_OUT_ERROR_CODE_NAME);
        messageContext.setProperty(SynapseConstants.ERROR_MESSAGE, APIInvocationRestrictHandlerConstants.ERROR_MESSAGE);
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        axis2MC.setProperty(NhttpConstants.HTTP_SC, HttpStatus.SC_FORBIDDEN);
        messageContext.setResponse(true);
        messageContext.setProperty(APIInvocationRestrictHandlerConstants.MESSAGE_CONTEXT_PROPERTY, "true");
        messageContext.setTo(null);
        // By default we send a 403 response back
        if (messageContext.isDoingPOX() || messageContext.isDoingGET()) {
            Utils.setFaultPayload(messageContext, createFaultPayload());
        } else {
            Utils.setSOAPFault(messageContext, "Server", "API is blocked",
                    "This API is blocked. Please contact the API Owner");
        }

        if (Utils.isCORSEnabled()) {
            /* For CORS support adding required headers to the fault response */
            Map<String, String> headers = (Map) axis2MC
                    .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            headers.put(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                    Utils.getAllowedOrigin(headers.get(APIInvocationRestrictHandlerConstants.CORS_HEADERS_ORIGIN)));
            headers.put(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_METHODS, Utils.getAllowedMethods());
            headers.put(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_HEADERS, Utils.getAllowedHeaders());
            axis2MC.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
        }
        Utils.sendFault(messageContext, HttpStatus.SC_FORBIDDEN);
        Axis2Sender.sendBack(messageContext);
    }

    private OMElement createFaultPayload() {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = factory.createOMNamespace(APIInvocationRestrictHandlerConstants.API_BILLING_NS,
                APIInvocationRestrictHandlerConstants.API_BILLING_NS_PREFIX);
        OMElement payload = factory.createOMElement("fault", ns);
        OMElement errorCode = factory.createOMElement("code", ns);
        errorCode.setText(APIInvocationRestrictHandlerConstants.BILLING_OUT_ERROR_CODE_NAME);
        OMElement errorMessage = factory.createOMElement("message", ns);
        errorMessage.setText("Api invocation failed");
        OMElement errorDetail = factory.createOMElement("description", ns);
        errorDetail.setText("Please contact the API Owner");

        payload.addChild(errorCode);
        payload.addChild(errorMessage);
        payload.addChild(errorDetail);
        return payload;
    }

}

