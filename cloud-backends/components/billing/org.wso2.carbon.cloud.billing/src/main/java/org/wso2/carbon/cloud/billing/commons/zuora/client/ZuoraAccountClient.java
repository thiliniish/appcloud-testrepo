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

import com.google.gson.JsonObject;
import com.zuora.api.SaveResult;
import com.zuora.api.object.Account;
import com.zuora.api.wso2.stub.InvalidTypeFault;
import com.zuora.api.wso2.stub.UnexpectedErrorFault;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.wso2.carbon.cloud.billing.commons.zuora.client.utils.ClientSession;
import org.wso2.carbon.cloud.billing.commons.zuora.client.utils.ZuoraClientUtils;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingZuoraException;

import java.io.IOException;
import java.rmi.RemoteException;

/**
 *
 */
public class ZuoraAccountClient extends ZuoraClient {

    private static final Log LOGGER = LogFactory.getLog(ZuoraAccountClient.class);
    private static final String INIT_ERROR_MSG = "Error while initializing Zuora Account client";
    private static final String ACCOUNT_CREATION_ERROR = "occurred while creating customer account";


    public ZuoraAccountClient() throws CloudBillingZuoraException {
        try {
            zuoraClientUtils = new ZuoraClientUtils();
        } catch (AxisFault axisFault) {
            LOGGER.error(INIT_ERROR_MSG, axisFault);
            throw new CloudBillingZuoraException(INIT_ERROR_MSG, axisFault);
        }
    }

    public ZuoraAccountClient(ClientSession clientSession) throws CloudBillingZuoraException {
        try {
            zuoraClientUtils = new ZuoraClientUtils(clientSession);
        } catch (AxisFault axisFault) {
            LOGGER.error(INIT_ERROR_MSG, axisFault);
            throw new CloudBillingZuoraException(INIT_ERROR_MSG, axisFault);
        }
    }

    /**
     * @param accountInfo json object
     *                    Converting Json String to Account object. Json string should be as follows
     *                    parameters should have the "local" prefix
     *                    {
     *                          "batch": "Batch1",
     *                          "accountNumber": "T-1444195138269",
     *                          "allowInvoiceEdit": true,
     *                          "autoPay": false,
     *                          "billCycleDay": 1,
     *                          "crmId": "SFDC-1444195138269",
     *                          "currency": "USD",
     *                          "name": "ACC-1444195138269",
     *                          "paymentTerm": "Due Upon Receipt",
     *                          "purchaseOrderNumber": "PO-1444195138269",
     *                          "status": "Draft"
     *                    }
     * @return JsonObject
     * {
     *    "errors": null,
     *    "errorsSpecified": false,
     *    "id": {
     *        "id": "2c92c0f9501d4f330150464f02ef0312"
     *    },
     *    "idSpecified": true,
     *    "success": true,
     *    "successSpecified": true
     * }
     *
     * or
     *
     * {
     *    "errors": [
     *        {
     *            "code": {
     *                "value": "INVALID_VALUE"
     *            },
     *            "codeSpecified": true,
     *            "field": null,
     *            "fieldSpecified": false,
     *            "message": "The account number T-1444288585567 is invalid.",
     *            "messageSpecified": true
     *        }
     *    ],
     *    "errorsSpecified": true,
     *    "id": null,
     *    "idSpecified": false,
     *    "success": false,
     *    "successSpecified": true
     * }
     * @throws CloudBillingZuoraException
     */
    public JsonObject createAccount(JsonObject accountInfo) throws CloudBillingZuoraException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Account account = mapper.readValue(accountInfo.toString(), Account.class);
            SaveResult result = zuoraClientUtils.create(account);
            return objectToJson(result);
        } catch (RemoteException e) {
            String error = "Remote exception " + ACCOUNT_CREATION_ERROR + accountInfo.get("localName");
            LOGGER.error(error, e);
            throw new CloudBillingZuoraException(e);
        } catch (InvalidTypeFault invalidTypeFault) {
            String error = "Invalid type fault error " + ACCOUNT_CREATION_ERROR +
                           accountInfo.get("localName") + ". ErrorCode: " + invalidTypeFault.getFaultMessage()
                                   .getInvalidTypeFault().getFaultCode().toString();
            LOGGER.error(error, invalidTypeFault);
            throw new CloudBillingZuoraException(error, invalidTypeFault);
        } catch (UnexpectedErrorFault unexpectedErrorFault) {
            String error = "Unexpected Error Fault error " + ACCOUNT_CREATION_ERROR +
                           accountInfo.get("localName") + ". ErrorCode: " + unexpectedErrorFault.getFaultMessage()
                                   .getUnexpectedErrorFault().getFaultCode().toString();
            LOGGER.error(error, unexpectedErrorFault);
            throw new CloudBillingZuoraException(error, unexpectedErrorFault);
        } catch (JsonMappingException e) {
            String error = "JsonMappingException " + ACCOUNT_CREATION_ERROR + accountInfo.get("localName");
            LOGGER.error(error, e);
            throw new CloudBillingZuoraException(e);
        } catch (JsonParseException e) {
            String error = "JsonParseException " + ACCOUNT_CREATION_ERROR + accountInfo.get("localName");
            LOGGER.error(error, e);
            throw new CloudBillingZuoraException(e);
        } catch (IOException e) {
            String error = "IOException " + ACCOUNT_CREATION_ERROR + accountInfo.get("localName");
            LOGGER.error(error, e);
            throw new CloudBillingZuoraException(e);
        }
    }
}
