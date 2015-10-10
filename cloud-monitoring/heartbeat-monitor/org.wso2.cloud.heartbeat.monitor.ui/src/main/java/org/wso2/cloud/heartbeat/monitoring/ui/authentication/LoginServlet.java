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

package org.wso2.cloud.heartbeat.monitoring.ui.authentication;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.cloud.heartbeat.monitoring.ui.configuration.Node;
import org.wso2.cloud.heartbeat.monitoring.ui.utils.ConfigReader;
import org.wso2.cloud.heartbeat.monitoring.ui.utils.HeartbeatException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@WebServlet("/LoginServlet") public class LoginServlet extends HttpServlet {
    private static final Log log = LogFactory.getLog(LoginServlet.class);

    private static final long serialVersionUID = 1L;
    private String basicAuthUserID;
    private String basicAuthPassword;
    private String serverUrl;
    private List<String> authorisedRoles;

    private static RemoteUserStoreManagerServiceStub stub = null;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String configPath = getServletConfig().getServletContext().getRealPath("/WEB-INF/heartbeat.conf");
        ConfigReader configurationInstance = ConfigReader.getInstance();
        try {
            configurationInstance.buildConfigurationNode(configPath);
            Node adminUserNode = configurationInstance.getAdminNode();
            basicAuthUserID = adminUserNode.getProperty("user");
            basicAuthPassword = adminUserNode.getProperty("password");
            serverUrl = "https://" + adminUserNode.getProperty("server_url");
            String authorisedRoleString = adminUserNode.getProperty("authorised_roles");
            String[] splitRoles = authorisedRoleString.split(",");
            authorisedRoles = new ArrayList<String>();
            if (splitRoles.length > 0) {
                Collections.addAll(authorisedRoles, splitRoles);
            } else {
                authorisedRoles.add(authorisedRoleString);
            }

        } catch (HeartbeatException heartbeatExceptions) {
            log.error(heartbeatExceptions);
        }

        // get request parameters for userID and password
        String user = request.getParameter("user").replace("@", ".");
        String pwd = request.getParameter("pwd");

        try {
            if (authenticate(user, pwd)) {
                HttpSession session = request.getSession();
                session.setAttribute("user", user);
                //setting session to expire in 30 mins
                session.setMaxInactiveInterval(30 * 60);
                Cookie userName = new Cookie("user", user);
                userName.setMaxAge(30 * 60);
                response.addCookie(userName);
                response.sendRedirect("index.jsp");
            } else {
                RequestDispatcher rd = getServletContext().getRequestDispatcher("/login.html");
                PrintWriter out = response.getWriter();
                out.println(
                        "<div style='width:30%; margin-left:38%; margin-top:10%; margin-bottom:-13%;'><font color=red>Either user name or password is wrong or invalid.</font></div>");
                rd.include(request, response);
            }
        } catch (Exception e) {
            log.error(e);
        }

    }

    private boolean authenticate(String userName, Object credential)
            throws HeartbeatException, RemoteException, RemoteUserStoreManagerServiceUserStoreExceptionException {
        if (!(credential instanceof String)) {
            log.error("Heartbeat - Monitor - Login error : Unsupported type of password");
            throw new HeartbeatException("Unsupported type of password");
        }
        try {
            if (stub == null) {
                stub = new RemoteUserStoreManagerServiceStub(null, serverUrl + "RemoteUserStoreManagerService");
                HttpTransportProperties.Authenticator basicAuth = new HttpTransportProperties.Authenticator();
                basicAuth.setUsername(basicAuthUserID);
                basicAuth.setPassword(basicAuthPassword);
                basicAuth.setPreemptiveAuthentication(true);

                final Options clientOptions = stub._getServiceClient().getOptions();
                clientOptions.setProperty(HTTPConstants.AUTHENTICATE, basicAuth);
                stub._getServiceClient().setOptions(clientOptions);
            }
            //Check for heartbeat specific role availability
            String[] userRoles = stub.getRoleListOfUser(userName);

            //boolean String to hold availability
            boolean roleIsAvailable = false;
            boolean loginSuccessful;
            for (String singleRole : userRoles) {
                if (authorisedRoles.contains(singleRole)) {
                    roleIsAvailable = true;
                }
            }
            loginSuccessful = stub.authenticate(userName, (String) credential) && roleIsAvailable;
            return loginSuccessful;
        } catch (AxisFault axisFault) {

        } catch (RemoteException e) {

        } catch (RemoteUserStoreManagerServiceUserStoreExceptionException e) {

        }
        return false;
    }

    private String[] handleException(String msg, Exception e) throws Exception {
        throw new HeartbeatException("Heartbeat - Monitor :" + msg, e);
    }

}
