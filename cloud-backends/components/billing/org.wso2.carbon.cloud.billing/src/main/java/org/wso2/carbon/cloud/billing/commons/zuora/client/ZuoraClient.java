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

package org.wso2.carbon.cloud.billing.commons.zuora.client;

import com.zuora.api.AmendRequest;
import com.zuora.api.AmendResult;
import com.zuora.api.CallOptions;
import com.zuora.api.DeleteResult;
import com.zuora.api.ID;
import com.zuora.api.LoginResult;
import com.zuora.api.SaveResult;
import com.zuora.api.SessionHeader;
import com.zuora.api.object.ZObject;
import com.zuora.api.wso2.stub.ZuoraServiceStub;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingException;

public class ZuoraClient {

    /**
     * The header.
     */
    private SessionHeader header;

    /**
     * The call options.
     */
    private CallOptions callOptions;

    private ZuoraServiceStub zuoraServiceStub;


    public ZuoraClient(String endpoint) throws CloudBillingException {
        try {
            this.zuoraServiceStub = new ZuoraServiceStub();
            // set new ENDPOINT
            if (endpoint != null && endpoint.trim().length() > 0) {
                ServiceClient client = zuoraServiceStub._getServiceClient();
                client.getOptions().getTo().setAddress(endpoint);
            }
        } catch (AxisFault e) {
            throw new CloudBillingException(e);
        }
    }

    public void login(String username, String password) throws Exception {

        LoginResult result = zuoraServiceStub.login(username, password);
        // create session for all subsequent calls
        this.header = new SessionHeader();
        this.header.setSession(result.getSession());
    }

    /**
     * Creates the ZObject.
     *
     * @param obj the ZObject
     * @return the iD
     * @throws Exception the exception
     */
    public ID create(ZObject obj) throws Exception {

        SaveResult[] cResponse = zuoraServiceStub.create(new ZObject[]{obj}, this.callOptions, this.header);
        SaveResult result = cResponse[0];
        if (!result.getSuccess()) {

        } else {
            //
        }
        return result.getId();
    }

    /**
     * Updates the ZObject.
     *
     * @param obj the ZObject
     * @return the iD
     * @throws Exception the exception
     */
    public ID update(ZObject obj) throws Exception {

        SaveResult[] cResponse = zuoraServiceStub.update(new ZObject[]{obj}, this.header);
        SaveResult result = cResponse[0];
        if (!result.getSuccess()) {
            //
        } else {
            //
        }
        return result.getId();
    }

    /**
     * Delete Zuora object for Type and ID.
     *
     * @param type the type
     * @param id   the ID
     * @return true, if successful
     * @throws Exception the exception
     */
    public boolean delete(String type, ID id) throws Exception {

        DeleteResult[] cResponse = zuoraServiceStub.delete(type, new ID[]{id}, this.header);
        DeleteResult result = cResponse[0];
        if (!result.getSuccess()) {
            //
        } else {
            //
        }
        return result.getSuccess();
    }

    /**
     * Amend.
     *
     * @param amendRequest the amend
     * @return the amend response
     * @throws Exception the exception
     */
    public AmendResult[] amend(AmendRequest amendRequest) throws Exception {

        AmendResult[] resp = zuoraServiceStub.amend(new AmendRequest[]{amendRequest}, this.header);
        AmendResult amendResult = resp[0];
        if (amendResult.getErrors() == null) {
            //
        } else {
            //
        }
        return resp;
    }
}