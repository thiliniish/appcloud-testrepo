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

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for workflows
 */
public class WorkFlowUtils {

    private WorkFlowUtils() {
    }

    /**
     * Get axis2 client
     *
     * @param action soap action
     * @param serviceEndpoint service endpoint
     * @param contentType content type
     * @param username username
     * @param password password
     * @return service client
     * @throws AxisFault
     */
    public static ServiceClient getClient(String action, String serviceEndpoint, String contentType,
                                   String username, String password) throws AxisFault {
        ServiceClient client = new ServiceClient(ServiceReferenceHolder.getContextService().getClientConfigContext(),
                                                 null);
        Options options = new Options();
        options.setAction(action);
        options.setTo(new EndpointReference(serviceEndpoint));

        if (contentType != null) {
            options.setProperty(Constants.Configuration.MESSAGE_TYPE, contentType);
        } else {
            options.setProperty(Constants.Configuration.MESSAGE_TYPE,
                                HTTPConstants.MEDIA_TYPE_APPLICATION_XML);
        }

        HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();

        // Assumes authentication is required if username and password is given
        if (username != null && password != null) {
            auth.setUsername(username);
            auth.setPassword(password);
            auth.setPreemptiveAuthentication(true);
            List<String> authSchemes = new ArrayList<String>();
            authSchemes.add(HttpTransportProperties.Authenticator.BASIC);
            auth.setAuthSchemes(authSchemes);

            if (contentType == null) {
                options.setProperty(Constants.Configuration.MESSAGE_TYPE, HTTPConstants.MEDIA_TYPE_APPLICATION_XML);
            }
            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, auth);
            options.setManageSession(true);
        }
        client.setOptions(options);

        return client;
    }
}
