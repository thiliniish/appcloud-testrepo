<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<!--
~ Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~    http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied.  See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@page import="java.util.Arrays"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.CharacterEncoder"%>
<%@ page import="org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants"%>

<fmt:bundle basename="org.wso2.carbon.identity.application.authentication.endpoint.i18n.Resources">
    <%
        String url = request.getRequestURL().toString();
        String baseURL = url.substring(0,
                url.indexOf("authenticationendpoint"));
        String cloudMgtUrl = application
                .getInitParameter("cloudMgtUrl");
    %>
    <html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="content-type" content="text/html;charset=utf-8" />
        <title>WSO2 Public Cloud</title>
        <script src="assets/js/jquery-1.7.1.min.js" type="text/javascript"></script>
        <script src="js/scripts.js" type="text/javascript"></script>
        <link type="text/css" rel="stylesheet" media="all" href="css/cloud-styles.css">
        <link href='https://fonts.googleapis.com/css?family=Noto+Sans:400,700,400italic,700italic' rel='stylesheet' type='text/css'>
    </head>

    <body>
    <div class="cMainScreen">
        <div class="cHeader">
            <div>
                <div class="cHeaderContent">
                    <img src="<%=baseURL%>/authenticationendpoint/images/cloud-images/wso2-cloud-logo-2.png" alt="WSO2"
                         class="cHeaderLogo" />
                    <ul>
                        <li><a href="mailto:cloud@wso2.com">Contact :
                            cloud@wso2.com</a></li>
                    </ul>
                </div>
                <div class="cClear"></div>
            </div>
        </div>
        <!-- container -->
        <%@ page import="java.util.Map" %>
        <%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.CharacterEncoder" %>
        <%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.Constants" %>
        <%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.TenantDataManager" %>

        <%

            request.getSession().invalidate();
            String queryString = request.getQueryString();
            Map<String, String> idpAuthenticatorMapping = null;
            if (request.getAttribute("idpAuthenticatorMap") != null) {
                idpAuthenticatorMapping = (Map<String, String>)request.getAttribute("idpAuthenticatorMap");
            }

            String errorMessage = "Login failed. Please recheck the username and password and try again.";
            String loginFailed = "false";

            if (CharacterEncoder.getSafeText(request.getParameter(Constants.AUTH_FAILURE)) != null &&
                    "true".equals(CharacterEncoder.getSafeText(request.getParameter(Constants
                            .AUTH_FAILURE)))) {
                loginFailed = "true";

                if(CharacterEncoder.getSafeText(request.getParameter(Constants.AUTH_FAILURE_MSG)) !=
                        null){
                    errorMessage = (String) CharacterEncoder.getSafeText(request.getParameter
                            (Constants.AUTH_FAILURE_MSG));

                    if (errorMessage.equalsIgnoreCase("login.fail.message")) {
                        errorMessage = "Login failed. Please recheck the username and password and try again.";
                    }
                }
            }
            String queryString1 = "../authenticationendpoint/samlsso_login_redirect.jsp?SAMLRequest="
                    + CharacterEncoder.getSafeText(request.getParameter("SAMLRequest"))
                    + "&relyingParty="
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
                    + CharacterEncoder.getSafeText(request.getParameter("authenticators"));
        %>

        <script type="text/javascript">
            function doLogin() {
                var loginForm = document.getElementById('loginForm');
                loginForm.submit();
            }
        </script>

        <%

            boolean hasLocalLoginOptions = false;
            List<String> localAuthenticatorNames = new ArrayList<String>();

            if (idpAuthenticatorMapping.get(IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME) != null){
                String authList = idpAuthenticatorMapping.get(IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME);
                if (authList!=null){
                    localAuthenticatorNames = Arrays.asList(authList.split(","));
                }
            }
        %>

        <%if(localAuthenticatorNames.contains("BasicAuthenticator")){ %>
        <div id="local_auth_div" class="container main-login-container">
        <%} %>

            <% if ("true".equals(loginFailed)) { %>
            <div class="alert alert-error">
                <%=errorMessage%>
            </div>
            <% } %>

            <form action="<%=queryString1%>" method="post" id="loginForm" class="form-horizontal" >
                        <%
                if(localAuthenticatorNames.size()>0) {

                    if(localAuthenticatorNames.size()>0 && localAuthenticatorNames.contains("OpenIDAuthenticator")){
                    	hasLocalLoginOptions = true;
            %>

                <div class="row">
                    <div class="span6">

                        <%@ include file="openid.jsp" %>

                    </div>
                </div>

                        <%
            } else if(localAuthenticatorNames.size()>0 && localAuthenticatorNames.contains("BasicAuthenticator")) {
            	hasLocalLoginOptions = true;
            %>

                        <%
                  if(TenantDataManager.isTenantListEnabled() && "true".equals(CharacterEncoder
                  .getSafeText(request.getParameter("isSaaSApp")))){
            %>
                <div class="row">
                    <div class="span6">

                        <%@ include file="tenantauth.jsp" %>

                    </div>
                </div>

                <script>
                    //set the selected tenant domain in dropdown from the cookie value
                    window.onload=selectTenantFromCookie;
                </script>

                        <%
                } else{
            %>
                <div class="row">
                    <div class="span6">
                        <%@ include file="basicauth.jsp" %>

                    </div>
                </div>

                        <%
                }
            }
            }
            %>

                        <%if(idpAuthenticatorMapping.get(IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME) != null){ %>
        </div>
        <%} %>
        <%
            if ((hasLocalLoginOptions && localAuthenticatorNames.size() > 1) || (!hasLocalLoginOptions)
                    || (hasLocalLoginOptions && idpAuthenticatorMapping.size() > 1)) {
        %>
        <div class="container">
            <div class="row">
                <div class="span12">
                    <% if(hasLocalLoginOptions) { %>
                    <h2>Other login options:</h2>
                    <%} else { %>
                    <script type="text/javascript">
                        document.getElementById('local_auth_div').style.display = 'block';
                    </script>
                    <%} %>
                </div>
            </div>
        </div>

        <div class="container different-login-container">
            <div class="row">

                <%
                    for (Map.Entry<String, String> idpEntry : idpAuthenticatorMapping.entrySet())  {
                        if(!idpEntry.getKey().equals(IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME)) {
                            String idpName = idpEntry.getKey();
                            boolean isHubIdp = false;
                            if (idpName.endsWith(".hub")){
                                isHubIdp = true;
                                idpName = idpName.substring(0, idpName.length()-4);
                            }
                %>
                <div class="span3">
                    <% if (isHubIdp) { %>
                    <a href="#"  class="main-link"><%=idpName%></a>
                    <div class="slidePopper" style="display:none">
                        <input type="text" id="domainName" name="domainName"/>
                        <input type="button" class="btn btn-primary go-btn" onClick="javascript: myFunction('<%=idpName%>','<%=idpEntry.getValue()%>','domainName')" value="Go" />
                    </div>
                    <%}else{ %>
                    <a onclick="javascript: handleNoDomain('<%=idpName%>','<%=idpEntry.getValue()%>')"  class="main-link truncate" style="cursor:pointer" title="<%=idpName%>"><%=idpName%></a>
                    <%} %>
                </div>
                <%}else if(localAuthenticatorNames.size()>0 && localAuthenticatorNames.contains("IWAAuthenticator")) {
                %>
                <div class="span3">
                    <a onclick="javascript: handleNoDomain('<%=idpEntry.getKey()%>','IWAAuthenticator')"  class="main-link" style="cursor:pointer">IWA</a>
                </div>
                <%
                        }

                    }%>



            </div>
            <% } %>
            </form>
        </div>


        <script>
            $(document).ready(function(){
                $('.main-link').click(function(){
                    $('.main-link').next().hide();
                    $(this).next().toggle('fast');
                    var w = $(document).width();
                    var h = $(document).height();
                    $('.overlay').css("width",w+"px").css("height",h+"px").show();
                });
                $('.overlay').click(function(){$(this).hide();$('.main-link').next().hide();});

            });
            function myFunction(key, value, name)
            {
                var object = document.getElementById(name);
                var domain = object.value;


                if (domain != "")
                {
                    document.location = "../commonauth?idp=" + key + "&authenticator=" + value + "&sessionDataKey=<%=CharacterEncoder.getSafeText(request.getParameter("sessionDataKey"))%>&domain=" + domain;
                } else {
                    document.location = "../commonauth?idp=" + key + "&authenticator=" + value + "&sessionDataKey=<%=CharacterEncoder.getSafeText(request.getParameter("sessionDataKey"))%>";
                }
            }

            function handleNoDomain(key, value)
            {


                document.location = "../commonauth?idp=" + key + "&authenticator=" + value + "&sessionDataKey=<%=CharacterEncoder.getSafeText(request.getParameter("sessionDataKey"))%>";

            }

        </script>
    </div>


    </body>
    </html>

</fmt:bundle>

