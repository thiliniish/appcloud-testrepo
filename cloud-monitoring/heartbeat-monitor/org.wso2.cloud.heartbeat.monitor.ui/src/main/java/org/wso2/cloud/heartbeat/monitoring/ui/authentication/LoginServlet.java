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
import org.wso2.cloud.heartbeat.monitoring.ui.configuration.parser.nginx.utils.Constants;
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

    /**
     * doPost method to authorize and authenicating the login
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @throws ServletException
     * @throws IOException
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String configPath = getServletConfig().getServletContext().getRealPath("/WEB-INF/heartbeat.conf");
        ConfigReader configurationInstance = ConfigReader.getInstance();
        try {
            configurationInstance.buildConfigurationNode(configPath);
            Node adminUserNode = configurationInstance.getAdminNode();
            basicAuthUserID = adminUserNode.getProperty(Constants.USER);
            basicAuthPassword = adminUserNode.getProperty(Constants.PASSWORD);
            serverUrl = "https://" + adminUserNode.getProperty("server_url");
            String authorisedRoleString = adminUserNode.getProperty("authorised_roles");
            String[] splitRoles = authorisedRoleString.split(",");
            authorisedRoles = new ArrayList<String>();
            if (splitRoles.length > 0) {
                Collections.addAll(authorisedRoles, splitRoles);
            } else {
                authorisedRoles.add(authorisedRoleString);
            }
        } catch (HeartbeatException heartbeatException) {
            log.error(heartbeatException);
            handleErrorResponse(request, response, Constants.LOGIN_PAGE, "Login failed due to internal server error.");
        }

        try {
            // get request parameters for userID and password
            if (request.getParameter(Constants.USER) != null && !request.getParameter(Constants.USER).isEmpty() &&
                request.getParameter(Constants.PASSWORD) != null && !request.getParameter(Constants.PASSWORD).isEmpty()) {
                String user = request.getParameter(Constants.USER).replace("@", ".");
                String pwd = request.getParameter(Constants.PASSWORD);
                if (authenticate(user, pwd)) {
                    HttpSession session = request.getSession();
                    session.setAttribute(Constants.USER, user);
                    //setting session to expire in 30 mins
                    session.setMaxInactiveInterval(30 * 60);
                    Cookie username = new Cookie(Constants.USER, user);
                    username.setSecure(true);
                    username.setMaxAge(30 * 60);
                    response.addCookie(username);
                    response.sendRedirect("index.jsp");
                } else {
                    handleErrorResponse(request, response, Constants.LOGIN_PAGE,
                                        "Either username or password is wrong or invalid.");
                }
            } else {
                handleErrorResponse(request, response, Constants.LOGIN_PAGE, "Username or password cannot be empty");
            }
        } catch (HeartbeatException heartbeatException) {
            log.error("Heartbeat - Monitor - login failure :" + heartbeatException);
            handleErrorResponse(request, response, Constants.LOGIN_PAGE, "Login failed due to internal server error.");
        }

    }

    /**
     * Authenticating the stub with given credentials and checks for the given heartbeat role
     *
     * @param username   Username of the login
     * @param credential credential Object
     * @return boolean value of authentication status
     * @throws HeartbeatException
     */
    private boolean authenticate(String username, Object credential) throws HeartbeatException {
        if (!(credential instanceof String)) {
            throw new HeartbeatException("Unsupported type of password");
        }
        try {
            RemoteUserStoreManagerServiceStub stub;
            stub = new RemoteUserStoreManagerServiceStub(null, serverUrl + "RemoteUserStoreManagerService");
            HttpTransportProperties.Authenticator basicAuth = new HttpTransportProperties.Authenticator();
            basicAuth.setUsername(basicAuthUserID);
            basicAuth.setPassword(basicAuthPassword);
            basicAuth.setPreemptiveAuthentication(true);

            final Options clientOptions = stub._getServiceClient().getOptions();
            clientOptions.setProperty(HTTPConstants.AUTHENTICATE, basicAuth);
            stub._getServiceClient().setOptions(clientOptions);

            //Check for heartbeat specific role availability
            String[] userRoles = stub.getRoleListOfUser(username);
            //boolean String to hold availability
            boolean roleIsAvailable = false;
            boolean loginSuccessful;
            for (String singleRole : userRoles) {
                if (authorisedRoles.contains(singleRole)) {
                    roleIsAvailable = true;
                }
            }
            loginSuccessful = stub.authenticate(username, (String) credential) && roleIsAvailable;
            return loginSuccessful;
        } catch (AxisFault axisFault) {
            throw new HeartbeatException(
                    "Heartbeat - Monitor - Axis Fault occurred while login for user [" + username + "] : " + axisFault);
        } catch (RemoteException e) {
            throw new HeartbeatException(
                    "Heartbeat - Monitor - Remote Exception occurred while login for user [" + username + "] : " + e);
        } catch (RemoteUserStoreManagerServiceUserStoreExceptionException e) {
            throw new HeartbeatException(
                    "Heartbeat - Monitor - Remote User Store Manager Services error occurred while login for user [" +
                    username + "] : " + e);
        }
    }

    /**
     * Formatting and sending the error related information to the intended page
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @param path     Path which the request dispatcher should redirect to
     * @param message  Message to be sent to front end
     * @throws ServletException
     * @throws IOException
     */
    private void handleErrorResponse(HttpServletRequest request, HttpServletResponse response, String path,
                                     String message) throws ServletException, IOException {
        RequestDispatcher rd = getServletContext().getRequestDispatcher(path);
        PrintWriter out = response.getWriter();
        out.println("<div style='width:30%; margin-left:38%; margin-top:10%; margin-bottom:-13%;'><font color=red>" +
                    message + "</font></div>");
        rd.include(request, response);
    }

}
