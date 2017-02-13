<%--
  ~ Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file except
  ~ in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.CharacterEncoder" %>

<%
    String loginFormActionUrl = "../authenticationendpoint/login_redirect.jsp";
    String queryParamString = request.getQueryString();
    if (StringUtils.isNotEmpty(queryParamString)) {
        loginFormActionUrl = loginFormActionUrl.concat('?' + queryParamString);
    }
%>
<form action="<%=Encode.forHtmlAttribute(loginFormActionUrl)%>" method="post" id="loginForm" class="form-horizontal">
    <input id="tocommonauth" name="tocommonauth" type="hidden" value="true">

    <% if (Boolean.parseBoolean(loginFailed)) { %>
    <%}else if((Boolean.TRUE.toString()).equals(request.getParameter("authz_failure"))){
        loginFailed = "true";
        errorMessage = "You are not authorized to login";
    %>
    <%}%>


    <div class="row">
        <div class="span6">
            <div class="cMainContent">
                <div class="container">
                    <div class="row">
                        <div class="col-lg-12 login-box-wrapper">
                            <div class="login-box">
                                <div class="login-box-top">
                                    <div class="login-box-top-inside">
                                        <h2 class="login-sub-title">Log in to WSO2 Cloud</h2>
                                        <%
                                            if ("true".equals(loginFailed)) {
                                        %>
                                        <div class="message-error">
                                            <ul>
                                                <li><%=errorMessage%>
                                                </li>
                                            </ul>
                                        </div>
                                        <% } %>
                                        <div class="username-wrapper">
                                            <input id="username" name="username" type="email" class="input-username"
                                                   tabindex="0"
                                                   placeholder="Email">
                                        </div>
                                        <div class="password-wrapper">
                                            <input id="password" name="password" type="password" class="input-password"
                                                   placeholder="Password" autocomplete="off">
                                        </div>
                                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                            <input type="hidden" name="sessionDataKey" value='<%=Encode.forHtmlAttribute
            (request.getParameter("sessionDataKey"))%>'/>
                                        </div>
                                        <div class="login-button-wrapper">
                                            <div class="loginError" id="loginError"></div>
                                            <button class="btn btn-primary login-button" type="submit" id="loginButton">Sign In</button>
                                        </div>
                                    </div>
                                </div>
                                <%
                                    if (StringUtils.isEmpty(request.getParameter("storeTenantDomain"))) {
                                %>
                                <div class="login-box-bottom">
                                    <a class="pull-left" href="<%=cloudMgtUrl%>/site/pages/initiate.jag">Forgot Password?</a>
                                    <a class="pull-right" href="<%=cloudMgtUrl%>/site/pages/signup.jag">Create an account</a>
                                    <div style="clear: both"></div>
                                </div>
                                <%
                                    }
                                %>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="clearfix"></div>
</form>


