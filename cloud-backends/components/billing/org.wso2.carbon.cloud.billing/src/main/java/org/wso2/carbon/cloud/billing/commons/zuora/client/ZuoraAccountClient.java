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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zuora.api.Error;
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
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
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
     *                    "localBatch": "Batch1",
     *                    "localAccountNumber": "T-1444195138269",
     *                    "localAllowInvoiceEdit": true,
     *                    "localAutoPay": false,
     *                    "localBillCycleDay": 1,
     *                    "localCrmId": "SFDC-1444195138269",
     *                    "localCurrency": "USD",
     *                    "localName": "ACC-1444195138269",
     *                    "localPaymentTerm": "Due Upon Receipt",
     *                    "localPurchaseOrderNumber": "PO-1444195138269",
     *                    "localStatus": "Draft"
     *                    }
     * @return JsonObject
     * {
     * "errorsSpecified": false,
     * "id": "2c92c0f8501d44050150424e8c771eec",
     * "idSpecified": true,
     * "success": true
     * }
     *
     * or
     *
     * {
     * "errorCodes": [
     * "MISSING_REQUIRED_VALUE"
     * ],
     * "errorMessages": [
     * "Missing required value: Name"
     * ],
     * "errorsSpecified": true,
     * "success": false
     * }
     * @throws CloudBillingZuoraException
     */
    public JsonObject createAccount(JsonObject accountInfo) throws CloudBillingZuoraException {
        try {
            //Gson gson = new Gson();
            //Account account = gson.fromJson(accountInfo, Account.class);
            ObjectMapper mapper = new ObjectMapper();
            Account account = mapper.readValue(accountInfo.toString(), Account.class);
            SaveResult result = zuoraClientUtils.create(account);
            return getResultJsonObject(result, account.getName());
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

    private JsonObject getResultJsonObject(SaveResult result, String accountName) {
        JsonObject resultObj = new JsonObject();

        resultObj.addProperty(BillingConstants.RESPONSE_SUCCESS, result.getSuccess());
        resultObj.addProperty(BillingConstants.RESPONSE_ERRORS_SPECIFIED, result.isErrorsSpecified());
        resultObj.addProperty(BillingConstants.RESPONSE_ID_SPECIFIED, result.isIdSpecified());

        if (result.isIdSpecified()) {
            resultObj.addProperty(BillingConstants.RESPONSE_ID, result.getId().getID());
        }

        if (result.isErrorsSpecified()) {
            Error[] errors = result.getErrors();
            JsonArray errorCodes = new JsonArray();
            JsonArray errorMessages = new JsonArray();
            JsonParser parser = new JsonParser();
            for (Error error : errors) {
                errorCodes.add(parser.parse(error.getCode().getValue()));
                errorMessages.add(parser.parse(error.getMessage()));
            }
            LOGGER.error("Zuora customer account creation success: " + result.getSuccess()
                         + " with errors while creating account: " + accountName
                         + ". Error codes are: " + errorCodes);
            resultObj.add(BillingConstants.RESPONSE_ERROR_CODES, errorCodes);
            resultObj.add(BillingConstants.RESPONSE_ERROR_MESSAGES, errorMessages);
        }
        return resultObj;
    }
}
