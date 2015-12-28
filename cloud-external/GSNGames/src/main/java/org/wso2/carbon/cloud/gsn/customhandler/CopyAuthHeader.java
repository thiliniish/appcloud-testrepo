/*
  * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
package org.wso2.carbon.cloud.gsn.customhandler;

import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;

import java.util.Map;

/**
 * This Handler is used by GSN Games to copy the authorization header to X-Authorization to avoid
 * Authorization header getting dropped from the APIAuthenticationHandler
 */
public class CopyAuthHeader extends AbstractHandler {

    @Override
    public boolean handleRequest(MessageContext messageContext) {
        String authorizationHeader = getAuthorizationHeader(getTransportHeaders(messageContext));
        if (authorizationHeader != null && !authorizationHeader.isEmpty()) {
            messageContext.setProperty("X-Preserved-Authorization", authorizationHeader);
        }
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }

    /**
     * Getting Transport headers from the Message Context
     *
     * @param messageContext
     * @return Map of Transport Headers
     */
    private Map getTransportHeaders(MessageContext messageContext) {
        return (Map) ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
    }

    /**
     * Getting Authorization header out of Map of headers
     *
     * @param headers Transport header map
     * @return String of Authorization header
     */
    private String getAuthorizationHeader(Map headers) {
        return (String) headers.get("Authorization");
    }
}
