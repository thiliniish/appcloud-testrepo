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
import com.zuora.api.SaveResult;
import com.zuora.api.SessionHeader;
import com.zuora.api.object.ZObject;
import com.zuora.api.wso2.stub.InvalidTypeFault;
import com.zuora.api.wso2.stub.InvalidValueFault;
import com.zuora.api.wso2.stub.LoginFault;
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

    private static final String INVALID_RESPONSE_LENGTH = "Invalid response length.";
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
     * Zuora stub login
     *
     * @throws CloudBillingZuoraException
     */
    public void login() throws CloudBillingZuoraException {
        try {
            LoginResult result = zuoraServiceStub.login(zuoraConfig.getUser(), zuoraConfig.getPassword());
            // create session for all subsequent calls
            clientSession = new ClientSession(result.getSession(), zuoraConfig.getSessionExpired());
        } catch (RemoteException e) {
            String errorMsg = "Remote exception while login to zuora";
            LOGGER.error(errorMsg, e);
            throw new CloudBillingZuoraException(errorMsg, e);
        } catch (LoginFault loginFault) {
            String errorMsg = "LoginFault while login to zuora";
            LOGGER.error(errorMsg, loginFault);
            throw new CloudBillingZuoraException(errorMsg, loginFault);
        } catch (UnexpectedErrorFault unexpectedErrorFault) {
            String errorMsg = "Unexpected exception while zuora login";
            LOGGER.error(errorMsg, unexpectedErrorFault);
            throw new CloudBillingZuoraException(errorMsg, unexpectedErrorFault);
        }
    }

    /**
     * Creates the ZObject.
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
        return (SaveResult) getValidatedResponse(response);
    }

    /**
     * Updates the ZObject.
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
        return (SaveResult) getValidatedResponse(response);
    }

    /**
     * Delete Zuora object for Type and ID.
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
        return (DeleteResult) getValidatedResponse(response);
    }

    /**
     * Amend.
     *
     * @param amendRequest the amend
     * @return the amend response
     * @throws UnexpectedErrorFault
     * @throws RemoteException
     * @throws CloudBillingZuoraException
     */
    public AmendResult[] amend(AmendRequest amendRequest)
            throws UnexpectedErrorFault, RemoteException, CloudBillingZuoraException {
        try {
            return zuoraServiceStub.amend(new AmendRequest[]{amendRequest}, getSessionHeader());
        } catch (UnexpectedErrorFault unexpectedErrorFault) {
            checkInvalidSessionError(unexpectedErrorFault);
            return zuoraServiceStub.amend(new AmendRequest[]{amendRequest}, getSessionHeader());
        }
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
            LOGGER.error(INVALID_RESPONSE_LENGTH);
            throw new CloudBillingZuoraException(INVALID_RESPONSE_LENGTH);
        }
    }

    private SessionHeader getSessionHeader() throws CloudBillingZuoraException {
        if (clientSession == null || clientSession.isSessionExpired()) {
            login();
        }
        return this.clientSession.getHeader();
    }

    private void checkInvalidSessionError(UnexpectedErrorFault unexpectedErrorFault)
            throws CloudBillingZuoraException, UnexpectedErrorFault {
        ErrorCode errorCode = unexpectedErrorFault.getFaultMessage().getUnexpectedErrorFault().getFaultCode();
        if (ErrorCode.INVALID_SESSION.equals(errorCode)) {
            login();
        } else {
            throw unexpectedErrorFault;
        }
    }

    public ClientSession getClientSession() {
        return clientSession;
    }
}