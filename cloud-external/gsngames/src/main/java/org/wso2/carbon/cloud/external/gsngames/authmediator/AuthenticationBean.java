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

public class AuthenticationBean {
    private String signature;
    private StringBuilder secretKey;
    private String sessionId;
    private String messageContent;

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = new StringBuilder(secretKey);
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public void appendSecretKey(String stringToAppend) {
        this.secretKey.append(stringToAppend);
    }

    public String getSecretKey() {
        return secretKey.toString();
    }

    public String getSignature() {
        return signature;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getMessageContent() {
        return messageContent;
    }
}
