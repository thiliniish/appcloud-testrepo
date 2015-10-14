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

package org.wso2.carbon.cloud.billing.commons.zuora.client;

import com.google.gson.JsonObject;
import com.zuora.api.DeleteResult;
import com.zuora.api.ID;
import com.zuora.api.QueryResult;
import com.zuora.api.SaveResult;
import com.zuora.api.object.Account;
import com.zuora.api.wso2.stub.InvalidQueryLocatorFault;
import com.zuora.api.wso2.stub.InvalidTypeFault;
import com.zuora.api.wso2.stub.InvalidValueFault;
import com.zuora.api.wso2.stub.MalformedQueryFault;
import com.zuora.api.wso2.stub.UnexpectedErrorFault;
import org.apache.axis2.AxisFault;
import org.codehaus.jackson.map.ObjectMapper;
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.commons.zuora.client.utils.ClientSession;
import org.wso2.carbon.cloud.billing.commons.zuora.client.utils.ZuoraClientUtils;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingZuoraException;

import java.io.IOException;
import java.rmi.RemoteException;

/**
 * Zuora account client extends the support of ZuoraServiceStub
 * to create/update/delete/query accounts
 */
public class ZuoraAccountClient extends ZuoraClient {

    private static final String INIT_ERROR_MSG = "Error while initializing Zuora Account client";
    private static final String ACCOUNT_CREATION_ERROR = "occurred while creating customer account";
    private static final String ACCOUNT_UPDATE_ERROR = "occurred while updating the customer account";
    private static final String ACCOUNT_DELETION_ERROR = "occurred while deleting customer account";
    private static final String ACCOUNT_QUERY_BY_NAME_ERROR = "occurred while querying customer account by name";


    public ZuoraAccountClient() throws CloudBillingZuoraException {
        try {
            zuoraClientUtils = new ZuoraClientUtils();
        } catch (AxisFault axisFault) {
            throw new CloudBillingZuoraException(INIT_ERROR_MSG, axisFault);
        }
    }

    public ZuoraAccountClient(ClientSession clientSession) throws CloudBillingZuoraException {
        try {
            zuoraClientUtils = new ZuoraClientUtils(clientSession);
        } catch (AxisFault axisFault) {
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
     * <p/>
     * or
     * <p/>
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
            throw new CloudBillingZuoraException("Remote exception " + ACCOUNT_CREATION_ERROR +
                                                 accountInfo.get("localName"), e);
        } catch (InvalidTypeFault e) {
            String errorCode =  e.getFaultMessage().getInvalidTypeFault().getFaultCode().toString();
            throw new CloudBillingZuoraException("Invalid type fault error " + ACCOUNT_CREATION_ERROR +
                                                 accountInfo.get("localName"), errorCode , e);
        } catch (UnexpectedErrorFault e) {
            String errorCode = e.getFaultMessage().getUnexpectedErrorFault().getFaultCode().toString();
            throw new CloudBillingZuoraException("Unexpected Error Fault error " + ACCOUNT_CREATION_ERROR
                                                 + accountInfo.get("localName"), errorCode, e);
        } catch (IOException  e) {
            throw new CloudBillingZuoraException("IOError " + ACCOUNT_CREATION_ERROR + accountInfo.get("localName"), e);
        }
    }

    /**
     * Query Account by name
     *
     * @param accountName account name
     * @return Account json object
     * @throws CloudBillingZuoraException
     */
    public JsonObject queryAccountByName(String accountName) throws CloudBillingZuoraException {
        try {
            Account account = getAccount(accountName);
            return objectToJson(account);
        } catch (RemoteException e) {
            throw new CloudBillingZuoraException("Remote exception " + ACCOUNT_QUERY_BY_NAME_ERROR + accountName, e);
        } catch (InvalidQueryLocatorFault e) {
            String errorCode = e.getFaultMessage().getInvalidQueryLocatorFault() .getFaultCode().toString();
            throw new CloudBillingZuoraException("InvalidQueryLocatorFault " + ACCOUNT_QUERY_BY_NAME_ERROR
                                                 + accountName, errorCode, e);
        } catch (MalformedQueryFault e) {
            String errorCode = e.getFaultMessage().getMalformedQueryFault() .getFaultCode().toString();
            throw new CloudBillingZuoraException("MalformedQueryFault " + ACCOUNT_QUERY_BY_NAME_ERROR + accountName,
                                                 errorCode, e);
        } catch (UnexpectedErrorFault e) {
            String errorCode = e.getFaultMessage().getUnexpectedErrorFault().getFaultCode().toString();
            throw new CloudBillingZuoraException("UnexpectedErrorFault " + ACCOUNT_QUERY_BY_NAME_ERROR + accountName,
                                                 errorCode, e);
        } catch (IOException e) {
            throw new CloudBillingZuoraException("IOException " + ACCOUNT_QUERY_BY_NAME_ERROR + accountName, e);
        }
    }

    /**
     * Delete account by name
     *
     * @param accountName account name
     * @return JsonObject
     * {
     *     "errors": null,
     *     "errorsSpecified": false,
     *     "id": {
     *         "id": "2c92c0f8501d4405015046de02cf0542"
     *     },
     *     "idSpecified": true,
     *     "success": true,
     *     "successSpecified": true
     *}
     * @throws CloudBillingZuoraException
     */
    public JsonObject deleteAccount(String accountName) throws CloudBillingZuoraException {
        try {
            Account account = getAccount(accountName);
            DeleteResult result = zuoraClientUtils.delete(BillingConstants.ZUORA_ACCOUNT, account.getId());
            return objectToJson(result);
        } catch (RemoteException e) {
            throw new CloudBillingZuoraException("Remote exception " + ACCOUNT_DELETION_ERROR + accountName, e);
        } catch (InvalidQueryLocatorFault e) {
            String errorCode = e.getFaultMessage().getInvalidQueryLocatorFault().getFaultCode().toString();
            throw new CloudBillingZuoraException("InvalidQueryLocatorFault " + ACCOUNT_DELETION_ERROR + accountName,
                                                 errorCode, e);
        } catch (MalformedQueryFault e) {
            String errorCode = e.getFaultMessage().getMalformedQueryFault().getFaultCode().toString();
            throw new CloudBillingZuoraException("MalformedQueryFault " + ACCOUNT_DELETION_ERROR + accountName,
                                                 errorCode, e);
        } catch (UnexpectedErrorFault e) {
            String errorCode = e.getFaultMessage().getUnexpectedErrorFault().getFaultCode().toString();
            throw new CloudBillingZuoraException("UnexpectedErrorFault " + ACCOUNT_DELETION_ERROR + accountName,
                                                 errorCode, e);
        } catch (InvalidValueFault e) {
            String errorCode = e.getFaultMessage().getInvalidValueFault().getFaultCode().toString();
            throw new CloudBillingZuoraException("InvalidValueFault " + ACCOUNT_DELETION_ERROR + accountName,
                                                 errorCode, e);
        } catch (InvalidTypeFault e) {
            String errorCode = e.getFaultMessage().getInvalidTypeFault().getFaultCode().toString();
            throw new CloudBillingZuoraException("InvalidTypeFault " + ACCOUNT_DELETION_ERROR + accountName,
                                                 errorCode, e);
        } catch (IOException e) {
            throw new CloudBillingZuoraException("IOException " + ACCOUNT_DELETION_ERROR + accountName, e);
        }
    }

    /**
     * @param accountInfo json object
     *                    Converting Json String to Account object. Json string should be as follows
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
     * <p/>
     * or
     * <p/>
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
    public JsonObject updateAccount(JsonObject accountInfo) throws CloudBillingZuoraException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Account accountUpdated = mapper.readValue(accountInfo.toString(), Account.class);
            ID id = getAccount(accountUpdated.getName()).getId();
            accountUpdated.setId(id);
            SaveResult saveResult = zuoraClientUtils.update(accountUpdated);
            return objectToJson(saveResult);
        } catch (RemoteException e) {
            throw new CloudBillingZuoraException("Remote exception " + ACCOUNT_UPDATE_ERROR
                                                 + accountInfo.get("localName"), e);
        } catch (InvalidTypeFault e) {
            String errorCode = e.getFaultMessage().getInvalidTypeFault().getFaultCode().toString();
            throw new CloudBillingZuoraException("Invalid type fault error " + ACCOUNT_UPDATE_ERROR
                                                 + accountInfo.get("localName"), errorCode, e);
        } catch (UnexpectedErrorFault e) {
            String errorCode = e.getFaultMessage().getUnexpectedErrorFault().getFaultCode().toString();
            throw new CloudBillingZuoraException("Unexpected Error Fault error " + ACCOUNT_UPDATE_ERROR
                                                 + accountInfo.get("localName"), errorCode, e);
        }  catch (InvalidQueryLocatorFault e) {
            String errorCode = e.getFaultMessage().getInvalidQueryLocatorFault().getFaultCode().toString();
            throw new CloudBillingZuoraException("InvalidQueryLocatorFault " + ACCOUNT_UPDATE_ERROR
                                                 + accountInfo.get("localName"), errorCode, e);
        } catch (MalformedQueryFault e) {
            String errorCode = e.getFaultMessage().getMalformedQueryFault().getFaultCode().toString();
            throw new CloudBillingZuoraException("MalformedQueryFault " + ACCOUNT_UPDATE_ERROR
                                                 + accountInfo.get("localName"), errorCode, e);
        } catch (IOException e) {
            throw new CloudBillingZuoraException("IOException " + ACCOUNT_UPDATE_ERROR + accountInfo.get("localName"), e);
        }
    }

    /**
     * Query zuora account by name
     *
     * @param accountName account name
     * @return Zoura account
     * @throws CloudBillingZuoraException
     * @throws RemoteException
     * @throws InvalidQueryLocatorFault
     * @throws MalformedQueryFault
     * @throws UnexpectedErrorFault
     */
    private Account getAccount(String accountName)
            throws CloudBillingZuoraException, RemoteException, InvalidQueryLocatorFault, MalformedQueryFault,
                   UnexpectedErrorFault {
        String query = ZuoraClientUtils
                .prepareZQuery(BillingConstants.QUERY_ZUORA_ACCOUNT_BY_NAME, new String[]{accountName});
        QueryResult result = zuoraClientUtils.query(query, null);
        return (Account) result.getRecords()[0];
    }
}
