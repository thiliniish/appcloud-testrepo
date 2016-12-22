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
    String type = request.getParameter("type");
    if ("samlsso".equals(type)) {
	
		String redirectString = "../authenticationendpoint/samlsso_login_redirect.jsp?relyingParty="
                              + CharacterEncoder.getSafeText(request.getParameter("relyingParty"))
                              + "&sessionDataKey="
                              + CharacterEncoder.getSafeText(request.getParameter("sessionDataKey"))
                              + "&commonAuthCallerPath="
                              + CharacterEncoder.getSafeText(request.getParameter("commonAuthCallerPath"))
                              + "&forceAuth="
                              + CharacterEncoder.getSafeText(request.getParameter("forceAuth"))
                              + "&passiveAuth="
                              + CharacterEncoder.getSafeText(request.getParameter("passiveAuth"))
                              + "&RelayState="
                              + CharacterEncoder.getSafeText(request.getParameter("RelayState"))
                              + "&SSOAuthSessionID="
                              + CharacterEncoder.getSafeText(request.getParameter("SSOAuthSessionID"))
                              + "&commonAuthCallerPath="
                              + CharacterEncoder.getSafeText(request.getParameter("commonAuthCallerPath"))
                              + "&tenantDomain="
                              + CharacterEncoder.getSafeText(request.getParameter("tenantDomain"))
                              + "&type="
                              + CharacterEncoder.getSafeText(request.getParameter("type"))
                              + "&sp="
                              + CharacterEncoder.getSafeText(request.getParameter("sp"))
                              + "&isSaaSApp="
                              + CharacterEncoder.getSafeText(request.getParameter("isSaaSApp"))
                              + "&authenticators="
                              + CharacterEncoder.getSafeText(request.getParameter("authenticators"))
                              + "&storeTenantDomain="
                              + CharacterEncoder.getSafeText(request.getParameter("storeTenantDomain"));

%>
<form action="<%=redirectString%>" method="post" id="loginForm" class="form-horizontal">
    <input id="tocommonauth" name="tocommonauth" type="hidden" value="true">
<%
    } else if ("oauth2".equals(type)){
%>
    <form action="/oauth2/authorize" method="post" id="loginForm" class="form-horizontal">
        <input id="tocommonauth" name="tocommonauth" type="hidden" value="true">

<%
    } else {
%>

<form action="../commonauth" method="post" id="loginForm" class="form-horizontal">

    <%
        }
    %>

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
                                            <input id="username" name="username" type="text" class="input-username" tabindex="0"
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
                                <div class="login-box-bottom">
                                    <a class="pull-left" href="<%=cloudMgtUrl%>/site/pages/initiate.jag">Forgot Password?</a>
                                    <a class="pull-right" href="<%=cloudMgtUrl%>/site/pages/signup.jag">Create an account</a>
                                    <div style="clear: both"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="clearfix"></div>
</form>


