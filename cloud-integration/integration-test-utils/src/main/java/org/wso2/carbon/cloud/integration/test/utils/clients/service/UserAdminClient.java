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

package org.wso2.carbon.cloud.integration.test.utils.clients.service;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.apache.axis2.client.Options;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

import java.rmi.RemoteException;

public class UserAdminClient {

    public static final Log log = LogFactory.getLog(UserAdminClient.class);

    private final String serviceName = "UserAdmin";
    private UserAdminStub userAdminStub;
    private String endPoint;

    public UserAdminClient(String backEndUrl, String sessionCookie) throws AxisFault {
        this.endPoint = backEndUrl + "/services/" + serviceName;
        userAdminStub = new UserAdminStub(endPoint);
        //Authenticate Your stub from sessionCooke
        ServiceClient serviceClient;
        Options option;
        serviceClient = userAdminStub._getServiceClient();
        option = serviceClient.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                           sessionCookie);
    }

    public void deleteUser(String username) throws RemoteException, UserAdminUserAdminException {
        userAdminStub.deleteUser(username);
    }

}
