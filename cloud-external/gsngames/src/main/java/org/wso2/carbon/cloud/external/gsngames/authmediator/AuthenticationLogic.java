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

package org.wso2.carbon.cloud.external.gsngames.authmediator;

import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.cloud.external.gsngames.authmediator.exception.AuthenticationException;
import org.wso2.carbon.cloud.external.gsngames.authmediator.util.AuthenticationUtil;
import org.wso2.carbon.cloud.external.gsngames.authmediator.util.MediatorConstants;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

/**
 * Authentication Logic for the Custom Mediator using resource path values and custom header send over the request
 */
public class AuthenticationLogic extends AbstractMediator {

    /**
     * This is the custom mediator method to find and do the authentication using synapse message context properties
     * @param synapseMessageContext
     * @return true if the mediation sequence is true
     */
    @Override
    public boolean mediate(org.apache.synapse.MessageContext synapseMessageContext) {
        AuthenticationBean authBean = new AuthenticationBean();
        AuthenticationUtil authUtil = new AuthenticationUtil(authBean, synapseMessageContext);
        Boolean validate = false;
        String acknowledgeList = null;
        Axis2MessageContext axis2MessageContext = (Axis2MessageContext) synapseMessageContext;
        org.apache.axis2.context.MessageContext msgContext = axis2MessageContext.getAxis2MessageContext();

        //Getting HTTP Method and Custom Header
        String httpMethod = msgContext.getProperty("HTTP_METHOD").toString();
        String transportInUrl = msgContext.getProperty("TransportInURL").toString();
        String customHeader = axis2MessageContext.getProperty("xAuthentication").toString();
        authBean.setSecretKey(axis2MessageContext.getProperty("secretKey").toString());

        if (customHeader != null && !customHeader.isEmpty()) {
            authUtil.setAuthParameters(customHeader);
        } else {
            authUtil.setReturnStatus(false, "Missing Authentication Header");
            return true;
        }

        //Retrieving timestamp from query parameter
        if (transportInUrl != null && !transportInUrl.isEmpty()) {
            String queryParamString = transportInUrl.split("\\?")[1].trim();
            List<String> queryStrings = Arrays.asList(queryParamString.split("&"));

            //Appending timestamp to secret key
            for (String queryString : queryStrings) {
                if (queryString.contains("timestamp=")) {
                    authBean.appendSecretKey(queryString.split("=", 2)[1]);
                }
                if (queryString.contains("ack=")) {
                    acknowledgeList = queryString.split("=", 2)[1];
                }
            }
        }

        //Checking Http Method
        if ("POST".equals(httpMethod) || "PUT".equals(httpMethod)) {
            String jsonString = (String) axis2MessageContext.getProperty("jsonMessage");
            authBean.setMessageContent(jsonString);
            if (acknowledgeList != null && !acknowledgeList.isEmpty()) {
                authBean.appendSecretKey(acknowledgeList);
            }
            try {
                String md5String = authUtil.md5(authBean.getMessageContent().getBytes(MediatorConstants.ENCODING));
                byte[] md5Bytes = md5String.getBytes();
                validate = authUtil.validateSignature(md5Bytes);
            } catch (AuthenticationException | UnsupportedEncodingException e) {
                authUtil.setExceptionStatus(false, MediatorConstants.AUTHORIZATION_EXCEPTION, e);
            }

        } else if ("GET".equals(httpMethod) || "DELETE".equals(httpMethod) || "HEAD".equals(httpMethod)) {
            try {
                if (transportInUrl != null && !transportInUrl.isEmpty()) {
                    authBean.setMessageContent(URLDecoder.decode(transportInUrl, MediatorConstants.ENCODING));
                    byte[] pathBytes =
                            authUtil.md5(authBean.getMessageContent().getBytes(MediatorConstants.ENCODING)).getBytes();
                    validate = authUtil.validateSignature(pathBytes);
                } else {
                    authUtil.setReturnStatus(false, "Missing Resource Path");
                    return true;
                }
            } catch (AuthenticationException | UnsupportedEncodingException e) {
                authUtil.setExceptionStatus(false, MediatorConstants.AUTHORIZATION_EXCEPTION, e);
            }
        }

        if (!validate) {
            authUtil.setReturnStatus(false, MediatorConstants.AUTHORIZATION_FAILED);
        }
        return true;
    }
}
