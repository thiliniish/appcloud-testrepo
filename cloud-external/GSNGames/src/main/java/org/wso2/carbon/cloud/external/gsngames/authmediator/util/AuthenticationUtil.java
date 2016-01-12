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

package org.wso2.carbon.cloud.external.gsngames.authmediator.util;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.cloud.external.gsngames.authmediator.AuthenticationBean;
import org.wso2.carbon.cloud.external.gsngames.authmediator.exception.AuthenticationException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

/**
 * Utility Class used for authenticating and validating the keys
 */
public class AuthenticationUtil {

    public final static Log log = LogFactory.getLog(AuthenticationUtil.class);

    private AuthenticationBean authBean;
    private MessageContext synapseMessageContext;

    /**
     * Constructor
     *
     * @param authBean AuthenticationBean object to be used
     */
    public AuthenticationUtil(AuthenticationBean authBean, MessageContext messageContext) {
        this.authBean = authBean;
        this.synapseMessageContext = messageContext;
    }

    /**
     * Creating the md5 String
     *
     * @param content byte array of content to hash
     * @return 32 character Hexdecimal String
     * @throws AuthenticationException
     */
    public String md5(byte[] content) throws AuthenticationException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(content);
            return String.format("%032x", new BigInteger(1, messageDigest.digest()));
        } catch (NoSuchAlgorithmException e) {
            throw new AuthenticationException("NoSuchAlgorithmException was thrown :", e);
        }
    }

    /**
     * Getting Signature and Session Id from the Authentication Custom header
     *
     * @param customHeader Authentication header passed in "GameKey Parameter=Value,Parameter=Value"
     */
    public void setAuthParameters(String customHeader) {

        String nameSpaceAuthString;
        List<String> paramList = Arrays.asList(customHeader.split("\\s+"));
        if (paramList.size() > 1) {
            nameSpaceAuthString = paramList.get(1).trim();
        } else {
            nameSpaceAuthString = paramList.get(0).trim();
        }
        List<String> authValues = Arrays.asList(nameSpaceAuthString.split(","));
        for (String authNameValue : authValues) {
            authNameValue = authNameValue.trim();
            if (authNameValue.contains("signature=")) {
                authBean.setSignature(authNameValue.split("=", 2)[1]);
            } else if (authNameValue.contains("sessionId=")) {
                String sessionId = authNameValue.split("=", 2)[1];
                authBean.appendSecretKey(sessionId);
                authBean.setSessionId(sessionId);
            }
        }

    }

    /**
     * * Validating the signature with the generated secret key
     *
     * @param payload payload body byte array
     * @return validation success status
     * @throws AuthenticationException
     */
    public boolean validateSignature(byte[] payload) throws AuthenticationException {
        String signature = authBean.getSignature();
        try {
            if (signature != null && !signature.isEmpty()) {
                String algorithm = "HMACSHA256";
                SecretKeySpec secretKeySpec = new SecretKeySpec(authBean.getSecretKey().getBytes(), algorithm);
                Mac mac = Mac.getInstance(algorithm);
                mac.init(secretKeySpec);
                byte[] byteString = mac.doFinal(payload);
                String calculatedSignature = new String(Hex.encodeHex(byteString));
                return signature.equals(calculatedSignature);
            } else {
                setReturnStatus(false, "Authentication Signature not found");
                return false;
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new AuthenticationException("Authentication Exception Occurred :", e);
        }
    }

    /**
     * Setting Returning Status code and message to the MessageContext
     *
     * @param statusCode    boolean status code
     * @param statusMessage message to be set as the status
     */
    public void setReturnStatus(boolean statusCode, String statusMessage) {
        synapseMessageContext.setProperty(MediatorConstants.VALIDATION_STATUS, statusCode);
        synapseMessageContext.setProperty(MediatorConstants.VALIDATION_RESPONSE, statusMessage);
        if (log.isDebugEnabled()) {
            log.debug("Return status for authentication :" + statusCode + " Status Message :" + statusMessage +
                      " for -  Signature : " + authBean.getSignature() +
                      " SessionId : " + authBean.getSessionId() + " Message Content : " + authBean.getMessageContent());
        }
    }

    /**
     * Setting Returning Status code and message to the MessageContext
     *
     * @param statusCode    boolean status code
     * @param statusMessage message to be set as the status
     */
    public void setExceptionStatus(boolean statusCode, String statusMessage, Exception exception) {
        if (log.isDebugEnabled()) {
            log.debug("Exception Occurred in Custom mediator Authentication - Signature : " + authBean.getSignature() +
                      " SessionId : " + authBean.getSessionId() + " Message Content : " + authBean.getMessageContent() +
                      " Exception : " + exception);
        }
        setReturnStatus(statusCode, statusMessage);
    }
}
