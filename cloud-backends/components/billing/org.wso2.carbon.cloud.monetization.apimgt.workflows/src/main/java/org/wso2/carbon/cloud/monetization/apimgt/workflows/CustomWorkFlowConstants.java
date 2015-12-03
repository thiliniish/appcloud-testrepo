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

/**
 *
 */
public class CustomWorkFlowConstants {

    public static final String SUBSCRIBER_INFO_PAYLOAD =
            "<ser:getAPISubscriberInfo xmlns:ser=\"http://service.billing.cloud.carbon.wso2.org\">\n" +
                    "  <ser:username>$1</ser:username>\n" +
                    "  <ser:tenantDomain>$2</ser:tenantDomain>\n" +
                    "</ser:getAPISubscriberInfo>";

    public static final String ADD_SUBSCRIBER_PAYLOAD =
            "<ser:addAPISubscriberInfo xmlns:ser=\"http://service.billing.cloud.carbon.wso2.org\">\n" +
                    "  <ser:username>$1</ser:username>\n" +
                    "  <ser:tenantDomain>$2</ser:tenantDomain>\n" +
                    "  <ser:isTestAccount>$3</ser:isTestAccount>\n" +
                    "  <ser:accountNumber ser:nil=\"true\"/>\n" +
                    "</ser:addAPISubscriberInfo>";

    /*SOAP actions*/
    public static final String SOAP_ACTION_GET_SUBSCRIBER = "urn:getAPISubscriberInfo";
    public static final String SOAP_ACTION_UPDATE_SUBSCRIBER = "urn:addAPISubscriberInfo";

    private CustomWorkFlowConstants() {
    }
}
