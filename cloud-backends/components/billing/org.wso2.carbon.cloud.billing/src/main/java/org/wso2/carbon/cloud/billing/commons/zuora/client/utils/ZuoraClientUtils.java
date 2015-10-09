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

package org.wso2.carbon.cloud.billing.commons.zuora.client.utils;

import com.zuora.api.AmendRequest;
import com.zuora.api.AmendResult;
import com.zuora.api.CallOptions;
import com.zuora.api.DeleteResult;
import com.zuora.api.ErrorCode;
import com.zuora.api.ID;
import com.zuora.api.LoginResult;
import com.zuora.api.QueryLocator;
import com.zuora.api.QueryOptions;
import com.zuora.api.QueryResult;
import com.zuora.api.SaveResult;
import com.zuora.api.SessionHeader;
import com.zuora.api.object.ZObject;
import com.zuora.api.wso2.stub.InvalidQueryLocatorFault;
import com.zuora.api.wso2.stub.InvalidTypeFault;
import com.zuora.api.wso2.stub.InvalidValueFault;
import com.zuora.api.wso2.stub.LoginFault;
import com.zuora.api.wso2.stub.MalformedQueryFault;
import com.zuora.api.wso2.stub.UnexpectedErrorFault;
import com.zuora.api.wso2.stub.ZuoraServiceStub;
import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.ADBBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.billing.commons.config.ZuoraConfig;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingZuoraException;
import org.wso2.carbon.cloud.billing.utils.CloudBillingUtils;

import java.rmi.RemoteException;

/**
 * Utility class for handle zuora stub
 */
public class ZuoraClientUtils {

    private static final Log LOGGER = LogFactory.getLog(ZuoraClientUtils.class);
    private static ZuoraConfig zuoraConfig = CloudBillingUtils.getBillingConfiguration().getZuoraConfig();
    /**
     * The header.
     */
    private ClientSession clientSession;
    /**
     * The call options.
     * {@link "https://knowledgecenter.zuora.com/
     * BC_Developers/SOAP_API/F_SOAP_API_Complex_Types/CallOptions"}
     */
    private CallOptions callOptions;
    private ZuoraServiceStub zuoraServiceStub;

    public ZuoraClientUtils() throws AxisFault {
        this.zuoraServiceStub = new ZuoraServiceStub();
        this.callOptions = new CallOptions();
        callOptions.setUseSingleTransaction(true);
    }

    public ZuoraClientUtils(ClientSession clientSession) throws AxisFault {
        this.clientSession = clientSession;
        this.zuoraServiceStub = new ZuoraServiceStub();
        this.callOptions = new CallOptions();
        callOptions.setUseSingleTransaction(true);
    }

    /**
     * Prepare ZQuery
     *
     * @param query  String query
     * @param params parameters
     * @return Prepared ZQuery
     */
    public static String prepareZQuery(String query, String[] params) {
        for (String param : params) {
            query = query.replaceFirst("\\?", param.trim());
        }
        return query;
    }

    /**
     * Zuora stub login
     * {@link "https://knowledgecenter.zuora.com/BC_Developers/SOAP_API/E_SOAP_API_Calls/login_call"}
     *
     * @throws CloudBillingZuoraException
     */
    public void login() throws CloudBillingZuoraException {
        try {
            LoginResult result = zuoraServiceStub.login(zuoraConfig.getUser(), zuoraConfig.getPassword());
            // create session for all subsequent calls
            clientSession = new ClientSession(result.getSession(), zuoraConfig.getSessionExpired());
        } catch (RemoteException e) {
            throw new CloudBillingZuoraException("Remote exception while login to zuora", e);
        } catch (LoginFault loginFault) {
            throw new CloudBillingZuoraException("LoginFault while login to zuora", loginFault);
        } catch (UnexpectedErrorFault unexpectedErrorFault) {
            throw new CloudBillingZuoraException("Unexpected exception while zuora login", unexpectedErrorFault);
        }
    }

    /**
     * Creates the ZObject.
     * {@link "https://knowledgecenter.zuora.com/BC_Developers/SOAP_API/E_SOAP_API_Calls/create_call"}
     *
     * @param obj the ZObject
     * @return SaveResult
     * @throws CloudBillingZuoraException
     * @throws UnexpectedErrorFault
     * @throws RemoteException
     * @throws InvalidTypeFault
     */
    public SaveResult create(ZObject obj)
            throws CloudBillingZuoraException, RemoteException, InvalidTypeFault, UnexpectedErrorFault {
        SaveResult[] response;
        try {
            response = zuoraServiceStub.create(new ZObject[]{obj}, this.callOptions, getSessionHeader());
        } catch (UnexpectedErrorFault unexpectedErrorFault) {
            checkInvalidSessionError(unexpectedErrorFault);
            response = zuoraServiceStub.create(new ZObject[]{obj}, this.callOptions, getSessionHeader());
        }
        SaveResult result = (SaveResult) getValidatedResponse(response);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Create result success: " + result.getSuccess() + ". Errors specified: "
                         + result.isErrorsSpecified());
        }
        return result;
    }

    /**
     * Updates the ZObject.
     * {@link "https://knowledgecenter.zuora.com/BC_Developers/SOAP_API/E_SOAP_API_Calls/update_call"}
     *
     * @param obj the ZObject
     * @return SaveResult
     * @throws UnexpectedErrorFault
     * @throws RemoteException
     * @throws InvalidTypeFault
     * @throws CloudBillingZuoraException
     */
    public SaveResult update(ZObject obj)
            throws RemoteException, InvalidTypeFault, CloudBillingZuoraException, UnexpectedErrorFault {
        SaveResult[] response;
        try {
            response = zuoraServiceStub.update(new ZObject[]{obj}, getSessionHeader());
        } catch (UnexpectedErrorFault unexpectedErrorFault) {
            checkInvalidSessionError(unexpectedErrorFault);
            response = zuoraServiceStub.update(new ZObject[]{obj}, getSessionHeader());
        }
        SaveResult result = (SaveResult) getValidatedResponse(response);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Update result success: " + result.getSuccess() + ". Errors specified: "
                         + result.isErrorsSpecified());
        }
        return result;
    }

    /**
     * Delete Zuora object for Type and ID.
     * {@link "https://knowledgecenter.zuora.com/BC_Developers/SOAP_API/E_SOAP_API_Calls/delete_call"}
     *
     * @param type the type
     * @param id   the ID
     * @return DeleteResult
     * @throws RemoteException
     * @throws InvalidValueFault
     * @throws InvalidTypeFault
     * @throws UnexpectedErrorFault
     * @throws CloudBillingZuoraException
     */
    public DeleteResult delete(String type, ID id)
            throws RemoteException, InvalidValueFault, InvalidTypeFault, CloudBillingZuoraException,
                   UnexpectedErrorFault {
        DeleteResult[] response;
        try {
            response = zuoraServiceStub.delete(type, new ID[]{id}, getSessionHeader());
        } catch (UnexpectedErrorFault unexpectedErrorFault) {
            checkInvalidSessionError(unexpectedErrorFault);
            response = zuoraServiceStub.delete(type, new ID[]{id}, getSessionHeader());
        }
        DeleteResult result = (DeleteResult) getValidatedResponse(response);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Delete result success: " + result.getSuccess() + ". Errors specified: "
                         + result.isErrorsSpecified());
        }
        return result;
    }

    /**
     * Amend.
     * {@link "https://knowledgecenter.zuora.com/BC_Developers/SOAP_API/E_SOAP_API_Calls/amend_call"}
     *
     * @param amendRequest the amend
     * @return the amend response
     * @throws UnexpectedErrorFault
     * @throws RemoteException
     * @throws CloudBillingZuoraException
     */
    public AmendResult[] amend(AmendRequest amendRequest)
            throws UnexpectedErrorFault, RemoteException, CloudBillingZuoraException {
        AmendResult[] results;
        try {
            results = zuoraServiceStub.amend(new AmendRequest[]{amendRequest}, getSessionHeader());
        } catch (UnexpectedErrorFault unexpectedErrorFault) {
            checkInvalidSessionError(unexpectedErrorFault);
            results = zuoraServiceStub.amend(new AmendRequest[]{amendRequest}, getSessionHeader());
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Amend results status for each amend request, in order");
            for (AmendResult result : results) {
                LOGGER.debug("Amend result success: " + result.getSuccess() + ". Errors specified: "
                             + result.isErrorsSpecified());
            }
        }
        return results;
    }

    /**
     * Zuora queries
     * {@link "https://knowledgecenter.zuora.com/BC_Developers/SOAP_API/E_SOAP_API_Calls/query_call"}
     *
     * @param queryString query string
     * @param options     {@link "https://knowledgecenter.zuora
     *                    .com/BC_Developers/SOAP_API/F_SOAP_API_Complex_Types/QueryOptions"}
     * @return Query Results
     * @throws CloudBillingZuoraException
     * @throws RemoteException
     * @throws InvalidQueryLocatorFault
     * @throws MalformedQueryFault
     * @throws UnexpectedErrorFault
     */
    public QueryResult query(String queryString, QueryOptions options)
            throws CloudBillingZuoraException, RemoteException, InvalidQueryLocatorFault, MalformedQueryFault,
                   UnexpectedErrorFault {
        QueryResult queryResult;
        try {
            queryResult = zuoraServiceStub.query(queryString, options, getSessionHeader());
        } catch (UnexpectedErrorFault unexpectedErrorFault) {
            checkInvalidSessionError(unexpectedErrorFault);
            queryResult = zuoraServiceStub.query(queryString, options, getSessionHeader());
        }
        return queryResult;
    }

    /**
     * Zuora queryMore
     * {@link "https://knowledgecenter.zuora.com/BC_Developers/SOAP_API/E_SOAP_API_Calls/queryMore_call"}
     *
     * @param queryString  query string
     * @param options      {@link "https://knowledgecenter.zuora
     *                     .com/BC_Developers/SOAP_API/F_SOAP_API_Complex_Types/QueryOptions"}
     * @param queryLocator If there are more than you request in a query; results, query() will return a boolean "done,"
     *                     which will be marked as false, and a queryLocator, which is a marker
     *                     you will pass to queryMore() to get the next set of results
     * @return Query Results
     * @throws CloudBillingZuoraException
     * @throws RemoteException
     * @throws InvalidQueryLocatorFault
     * @throws MalformedQueryFault
     * @throws UnexpectedErrorFault
     */
    public QueryResult queryMore(String queryString, QueryOptions options, QueryLocator queryLocator)
            throws CloudBillingZuoraException, RemoteException, InvalidQueryLocatorFault, MalformedQueryFault,
                   UnexpectedErrorFault {
        QueryResult queryResult;
        try {
            queryResult = zuoraServiceStub.queryMore(queryLocator, options, getSessionHeader());
        } catch (UnexpectedErrorFault unexpectedErrorFault) {
            checkInvalidSessionError(unexpectedErrorFault);
            queryResult = zuoraServiceStub.queryMore(queryLocator, options, getSessionHeader());
        }
        return queryResult;
    }

    /**
     * This for a single object hence returning the first object response
     *
     * @param response ADBBean array of reponse (i.e SaveResult/DeleteResult)
     * @return ADBBean
     * @throws CloudBillingZuoraException
     */
    private ADBBean getValidatedResponse(ADBBean[] response) throws CloudBillingZuoraException {
        if (response.length > 0) {
            return response[0];
        } else {
            throw new CloudBillingZuoraException("Invalid response length.");
        }
    }

    private SessionHeader getSessionHeader() throws CloudBillingZuoraException {
        if (clientSession == null || clientSession.isSessionExpired()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Client session expired. retrying login");
            }
            login();
        }
        return this.clientSession.getHeader();
    }

    private void checkInvalidSessionError(UnexpectedErrorFault unexpectedErrorFault)
            throws CloudBillingZuoraException, UnexpectedErrorFault {
        ErrorCode errorCode = unexpectedErrorFault.getFaultMessage().getUnexpectedErrorFault().getFaultCode();
        if (ErrorCode.INVALID_SESSION.equals(errorCode)) {
            LOGGER.warn("Invalid session. retrying login");
            login();
        } else {
            throw unexpectedErrorFault;
        }
    }

    public ClientSession getClientSession() {
        return clientSession;
    }
}