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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import org.codehaus.jackson.map.ObjectMapper;
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.commons.zuora.client.utils.ZuoraClientUtils;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingZuoraException;

import java.io.IOException;
import java.rmi.RemoteException;

/**
 * Zuora account client extends the support of ZuoraServiceStub
 * to create/update/delete/query accounts
 */
public class ZuoraAccountClient extends ZuoraClient {

    private static final String ACCOUNT_CREATION_ERROR = "occurred while creating customer account";
    private static final String ACCOUNT_UPDATE_ERROR = "occurred while updating the customer account";
    private static final String ACCOUNT_DELETION_ERROR = "occurred while deleting customer account";
    private static final String ACCOUNT_QUERY_BY_NAME_ERROR = "occurred while querying customer account by name";
    private static final String ACCOUNT_QUERY_BY_ACC_NO_ERROR = "occurred while querying customer account by account no";

    private static final String ERROR_JSON_OBJ_INVALID_ACCOUNT = "{\"code\": null,\"codeSpecified\": true,\"field\": " +
            "null,\"fieldSpecified\": false,\"message\": \"Invalid account name. \",\"messageSpecified\": true}";

    public ZuoraAccountClient() throws CloudBillingZuoraException {
        super();
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
            String errorCode = e.getFaultMessage().getInvalidTypeFault().getFaultCode().toString();
            throw new CloudBillingZuoraException("Invalid type fault error " + ACCOUNT_CREATION_ERROR +
                    accountInfo.get("localName"), errorCode, e);
        } catch (UnexpectedErrorFault e) {
            String errorCode = e.getFaultMessage().getUnexpectedErrorFault().getFaultCode().toString();
            throw new CloudBillingZuoraException("Unexpected Error Fault error " + ACCOUNT_CREATION_ERROR
                    + accountInfo.get("localName"), errorCode, e);
        } catch (IOException e) {
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
            Account account = getAccountByName(accountName);
            if (account != null) {
                return objectToJson(account);
            } else {
                return objectToJson(new Account());
            }
        } catch (IOException e) {
            throw new CloudBillingZuoraException("IOException " + ACCOUNT_QUERY_BY_NAME_ERROR + accountName, e);
        }
    }

    /**
     * Query Account by Account No
     *
     * @param accountNumber account name
     * @return Account json object
     * @throws CloudBillingZuoraException
     */
    public JsonObject queryAccountByAccountNo(String accountNumber) throws CloudBillingZuoraException {
        try {
            Account account = getAccountByAccountNo(accountNumber);
            return objectToJson(account);
        } catch (IOException e) {
            throw new CloudBillingZuoraException("IOException " + ACCOUNT_QUERY_BY_ACC_NO_ERROR + accountNumber, e);
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
            Account account = getAccountByName(accountName);
            if (account != null) {
                DeleteResult result = zuoraClientUtils.delete(BillingConstants.ZUORA_ACCOUNT, account.getId());
                return objectToJson(result);
            } else {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("successSpecified", true);
                errorResponse.addProperty("errorsSpecified", true);

                JsonObject[] errorObjs = new JsonObject[]{
                        new JsonParser().parse(ERROR_JSON_OBJ_INVALID_ACCOUNT).getAsJsonObject()
                };
                errorResponse.add("errors", new Gson().toJsonTree(errorObjs));
                return errorResponse;
            }

        } catch (RemoteException e) {
            throw new CloudBillingZuoraException("Remote exception " + ACCOUNT_DELETION_ERROR + accountName, e);
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
            SaveResult saveResult = updateAccount(accountUpdated);
            return objectToJson(saveResult);
        } catch (IOException e) {
            throw new CloudBillingZuoraException("IOException " + ACCOUNT_UPDATE_ERROR + accountInfo.get("localName"), e);
        }
    }

    /**
     * @param childAccNo  child account name
     * @param parentAccNo child account number
     * @return json object
     * {
     *    "errors": null,
     *    "errorsSpecified": false,
     *    "id": {
     *        "id": "2c92c0fb5133f6380151439c0980718d"
     *    },
     *   "idSpecified": true,
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
    public JsonObject addAccountParent(String childAccNo, String parentAccNo) throws CloudBillingZuoraException {
        try {
            Account childAccount = getAccountByAccountNo(childAccNo);
            Account parentAccount = getAccountByAccountNo(parentAccNo);
            childAccount.setParentId(parentAccount.getId());
            SaveResult result = updateAccount(childAccount);
            return objectToJson(result);
        } catch (IOException e) {
            throw new CloudBillingZuoraException("IOException " + ACCOUNT_UPDATE_ERROR + childAccNo, e);
        }
    }

    /**
     * Query zuora account by name
     *
     * @param accountName account name
     * @return Zoura account
     * @throws CloudBillingZuoraException
     */
    private Account getAccountByName(String accountName) throws CloudBillingZuoraException {
        try {
            String query = ZuoraClientUtils.prepareZQuery(BillingConstants.QUERY_ZUORA_ACCOUNT_BY_NAME, new String[]{accountName});
            QueryResult result = zuoraClientUtils.query(query, null);
            if ((result.getRecords())[0] != null) {
                return (Account) result.getRecords()[0];
            } else {
                return null;
            }
        } catch (RemoteException e) {
            throw new CloudBillingZuoraException("Remote exception " + ACCOUNT_QUERY_BY_NAME_ERROR + accountName, e);
        } catch (InvalidQueryLocatorFault e) {
            String errorCode = e.getFaultMessage().getInvalidQueryLocatorFault().getFaultCode().toString();
            throw new CloudBillingZuoraException("InvalidQueryLocatorFault " + ACCOUNT_QUERY_BY_NAME_ERROR
                    + accountName, errorCode, e);
        } catch (MalformedQueryFault e) {
            String errorCode = e.getFaultMessage().getMalformedQueryFault().getFaultCode().toString();
            throw new CloudBillingZuoraException("MalformedQueryFault " + ACCOUNT_QUERY_BY_NAME_ERROR + accountName,
                    errorCode, e);
        } catch (UnexpectedErrorFault e) {
            String errorCode = e.getFaultMessage().getUnexpectedErrorFault().getFaultCode().toString();
            throw new CloudBillingZuoraException("UnexpectedErrorFault " + ACCOUNT_QUERY_BY_NAME_ERROR + accountName,
                    errorCode, e);
        }
    }

    /**
     * Query zuora account by account number
     *
     * @param accountNumber account number
     * @return Zoura account
     * @throws CloudBillingZuoraException
     */
    private Account getAccountByAccountNo(String accountNumber) throws CloudBillingZuoraException {
        try {
            String query = ZuoraClientUtils.prepareZQuery(BillingConstants.QUERY_ZUORA_ACCOUNT_BY_ACCOUNT_NO, new String[]{accountNumber});
            QueryResult result = zuoraClientUtils.query(query, null);
            return (Account) result.getRecords()[0];
        } catch (RemoteException e) {
            throw new CloudBillingZuoraException("Remote exception " + ACCOUNT_QUERY_BY_ACC_NO_ERROR + accountNumber, e);
        } catch (InvalidQueryLocatorFault e) {
            String errorCode = e.getFaultMessage().getInvalidQueryLocatorFault().getFaultCode().toString();
            throw new CloudBillingZuoraException("InvalidQueryLocatorFault " + ACCOUNT_QUERY_BY_ACC_NO_ERROR
                    + accountNumber, errorCode, e);
        } catch (MalformedQueryFault e) {
            String errorCode = e.getFaultMessage().getMalformedQueryFault().getFaultCode().toString();
            throw new CloudBillingZuoraException("MalformedQueryFault " + ACCOUNT_QUERY_BY_ACC_NO_ERROR + accountNumber,
                    errorCode, e);
        } catch (UnexpectedErrorFault e) {
            String errorCode = e.getFaultMessage().getUnexpectedErrorFault().getFaultCode().toString();
            throw new CloudBillingZuoraException("UnexpectedErrorFault " + ACCOUNT_QUERY_BY_ACC_NO_ERROR + accountNumber,
                    errorCode, e);
        }
    }

    /**
     * Update account in zuora
     *
     * @param accountUpdated content updated account
     * @return Result in SaveResult
     * @throws CloudBillingZuoraException
     */
    private SaveResult updateAccount(Account accountUpdated) throws CloudBillingZuoraException {
        try {
            if (accountUpdated.getId() == null) {
                ID id = getAccountByName(accountUpdated.getName()).getId();
                accountUpdated.setId(id);
            }
            return zuoraClientUtils.update(accountUpdated);
        } catch (RemoteException e) {
            throw new CloudBillingZuoraException("Remote exception " + ACCOUNT_UPDATE_ERROR
                    + accountUpdated.getName(), e);
        } catch (InvalidTypeFault e) {
            String errorCode = e.getFaultMessage().getInvalidTypeFault().getFaultCode().toString();
            throw new CloudBillingZuoraException("Invalid type fault error " + ACCOUNT_UPDATE_ERROR
                    + accountUpdated.getName(), errorCode, e);
        } catch (UnexpectedErrorFault e) {
            String errorCode = e.getFaultMessage().getUnexpectedErrorFault().getFaultCode().toString();
            throw new CloudBillingZuoraException("Unexpected Error Fault error " + ACCOUNT_UPDATE_ERROR
                    + accountUpdated.getName(), errorCode, e);
        }
    }
}
