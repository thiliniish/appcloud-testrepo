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

<%@page import="org.wso2.carbon.identity.application.authentication.endpoint.util.Constants" %>
<%@page import="java.util.ArrayList" %>
<%@page import="java.util.Arrays" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.TenantDataManager" %>
<%@ page import="java.util.ResourceBundle" %>

<%!
    private static final String FIDO_AUTHENTICATOR = "FIDOAuthenticator";
    private static final String IWA_AUTHENTICATOR = "IWAAuthenticator";
    private static final String IS_SAAS_APP = "isSaaSApp";
    private static final String BASIC_AUTHENTICATOR = "BasicAuthenticator";
    private static final String BASIC_IDENTITY_CLOUD_AUTHENTICATOR = "BasicIdentityCloudAuthenticator";
    private static final String OPEN_ID_AUTHENTICATOR = "OpenIDAuthenticator";
%><fmt:bundle basename="org.wso2.carbon.identity.application.authentication.endpoint.i18n.Resources">

    <%
        String BUNDLE = "org.wso2.carbon.identity.application.authentication.endpoint.i18n.Resources";
        ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

        request.getSession().invalidate();
        String queryString = request.getQueryString();
        Map<String, String> idpAuthenticatorMapping = null;
        if (request.getAttribute(Constants.IDP_AUTHENTICATOR_MAP) != null) {
            idpAuthenticatorMapping = (Map<String, String>) request.getAttribute(Constants.IDP_AUTHENTICATOR_MAP);
        }

        String errorMessage = "Authentication Failed! Please Retry";
        String loginFailed = "false";

        if (Boolean.parseBoolean(request.getParameter(Constants.AUTH_FAILURE))) {
            loginFailed = "true";
            if (request.getParameter(Constants.AUTH_FAILURE_MSG) != null) {
                errorMessage = resourceBundle.getString(request.getParameter(Constants.AUTH_FAILURE_MSG));
            }
        }
    %>
    <%

        boolean hasLocalLoginOptions = false;
        List<String> localAuthenticatorNames = new ArrayList<String>();

        if (idpAuthenticatorMapping != null && idpAuthenticatorMapping.get(Constants.RESIDENT_IDP_RESERVED_NAME) != null) {
            String authList = idpAuthenticatorMapping.get(Constants.RESIDENT_IDP_RESERVED_NAME);
            if (authList != null) {
                localAuthenticatorNames = Arrays.asList(authList.split(","));
            }
        }


    %>
    <%
        String url = request.getRequestURL().toString();
        String baseURL = url.substring(0,
                                       url.indexOf("authenticationendpoint"));
        //This system property is set in startup script(wso2server.sh)
        String cloudMgtUrl = System.getProperty("cloudMgt.URL");
    %>

    <html>
    <head>
        <meta http-equiv="content-type" content="text/html;charset=utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>WSO2 Public Cloud</title>
        <script src="assets/js/jquery-1.7.1.min.js" type="text/javascript"></script>
        <script src="js/scripts.js" type="text/javascript"></script>
    	<link rel="icon" sizes="16x16 32x32" href="<%=baseURL%>/authenticationendpoint/images/cloud-images/favicon.ico" type="image/x-icon"/>
    	<link rel="shortcut icon" sizes="16x16 32x32" href="<%=baseURL%>/authenticationendpoint/images/cloud-images/favicon.ico" type="image/x-icon"/>
        <link type="text/css" rel="stylesheet" media="all" href="css/cloud-styles.css">
        <link href='https://fonts.googleapis.com/css?family=Noto+Sans:400,700,400italic,700italic' rel='stylesheet' type='text/css'>


        <!--[if lt IE 9]>
        <script src="js/html5shiv.min.js"></script>
        <script src="js/respond.min.js"></script>
        <![endif]-->
    </head>

    <body>
    <div class="cMainScreen">
        <div class="cHeader">
            <div>
                <div class="cHeaderContent">
                    <img src="<%=baseURL%>/authenticationendpoint/images/cloud-images/wso2-cloud-logo-2.png" alt="WSO2"
                         class="cHeaderLogo"/>
                    <ul>
                        <li><a href="mailto:cloud@wso2.com">Contact :
                                                            cloud@wso2.com</a></li>
                    </ul>
                </div>
                <div class="cClear"></div>
            </div>
        </div>
            <div id="local_auth_div" class="container main-login-container">
                        <!--div class="padding-double login-form"-->
                            <%
                                if (localAuthenticatorNames.size() > 0) {

                                    if (localAuthenticatorNames.size() > 0 && localAuthenticatorNames.contains(OPEN_ID_AUTHENTICATOR)) {
                                        hasLocalLoginOptions = true;
                            %>

                            <%@ include file="openid.jsp" %>

                            <%
                            } else if (localAuthenticatorNames.size() > 0 && (localAuthenticatorNames.contains(BASIC_AUTHENTICATOR) || localAuthenticatorNames.contains(BASIC_IDENTITY_CLOUD_AUTHENTICATOR))) {
                                hasLocalLoginOptions = true;
                            %>

                            <%
                                if (TenantDataManager.isTenantListEnabled() && Boolean.parseBoolean(request.getParameter(IS_SAAS_APP))) {
                            %>

                            <%@ include file="tenantauth.jsp" %>

                            <script>
                                //set the selected tenant domain in dropdown from the cookie value
                                window.onload = selectTenantFromCookie;
                            </script>
                            <%
                            } else {
                            %>
                            <%@ include file="basicauth.jsp" %>
                            <%
                                        }
                                    }
                                }
                            %>

                            <%if (idpAuthenticatorMapping != null &&
                                    idpAuthenticatorMapping.get(Constants.RESIDENT_IDP_RESERVED_NAME) != null) { %>

                            <%} %>
                            <%
                                if ((hasLocalLoginOptions && localAuthenticatorNames.size() > 1) || (!hasLocalLoginOptions)
                                        || (hasLocalLoginOptions && idpAuthenticatorMapping != null && idpAuthenticatorMapping.size() > 1)) {
                            %>
                            <div class="form-group">
                                <% if (hasLocalLoginOptions) { %>
                                <label class="font-large">Other login options:</label>
                                <%} %>
                            </div>
                            <div class="form-group">
                                <%
                                    int iconId = 0;
                                    if (idpAuthenticatorMapping != null) {
                                    for (Map.Entry<String, String> idpEntry : idpAuthenticatorMapping.entrySet()) {
                                        iconId++;
                                        if (!idpEntry.getKey().equals(Constants.RESIDENT_IDP_RESERVED_NAME)) {
                                            String idpName = idpEntry.getKey();
                                            boolean isHubIdp = false;
                                            if (idpName.endsWith(".hub")) {
                                                isHubIdp = true;
                                                idpName = idpName.substring(0, idpName.length() - 4);
                                            }
                                %>
                                <% if (isHubIdp) { %>
                                <div>
                                <a href="#" data-toggle="popover" data-placement="bottom"
                                   title="Sign in with <%=Encode.forHtmlAttribute(idpName)%>" id="popover" id="icon-<%=iconId%>">
                                    <img class="idp-image" src="images/login-icon.png"
                                         title="Sign in with <%=Encode.forHtmlAttribute(idpName)%>"/>

                                    <div id="popover-head" class="hide">
                                        <label class="font-large">Sign in with <%=Encode.forHtmlContent(idpName)%></label>
                                    </div>
                                    <div id="popover-content" class="hide">
                                        <form class="form-inline">
                                            <div class="form-group">
                                                <input id="domainName" class="form-control" type="text"
                                                       placeholder="Domain Name">
                                            </div>
                                            <input type="button" class="btn btn-primary go-btn"
                                                   onClick="javascript: myFunction('<%=idpName%>','<%=idpEntry.getValue()%>','domainName')"
                                                   value="Go"/>
                                        </form>

                                    </div>
                                </a>
                                    <label for="icon-<%=iconId%>"><%=Encode.forHtmlContent(idpName)%></label>
                                </div>
                                <%} else { %>
                                <div>
                                <a onclick="javascript: handleNoDomain('<%=Encode.forJavaScriptAttribute(Encode.
                                forUriComponent(idpName))%>',
                                        '<%=Encode.forJavaScriptAttribute(Encode.forUriComponent(idpEntry.getValue()))%>')"
                                   href="#" id="icon-<%=iconId%>">
                                    <img class="idp-image" src="images/login-icon.png" data-toggle="tooltip"
                                         data-placement="top" title="Sign in with <%=Encode.forHtmlAttribute(idpName)%>"/>
                                </a>
                                <label for="icon-<%=iconId%>"><%=Encode.forHtmlContent(idpName)%></label>
                                    </div>
                                <%} %>
                                <%
                                } else if (localAuthenticatorNames.size() > 0) {
                                    if (localAuthenticatorNames.contains(IWA_AUTHENTICATOR)) {
                                %>
                                <div>
                                <a onclick="javascript: handleNoDomain('<%=Encode.forJavaScriptAttribute(Encode.
                                forUriComponent(idpEntry.getKey()))%>',
                                        'IWAAuthenticator')" class="main-link" style="cursor:pointer" id="icon-<%=iconId%>">
                                    <img class="idp-image" src="images/login-icon.png" data-toggle="tooltip"
                                         data-placement="top" title="Sign in with IWA"/>
                                </a>
                                <label for="icon-<%=iconId%>">IWA</label>
                                </div>
                                <%
                                    }
                                    if (localAuthenticatorNames.contains(FIDO_AUTHENTICATOR)) {
                                %>
                                <div>
                                <a onclick="javascript: handleNoDomain('<%=Encode.forJavaScriptAttribute(Encode.
                                forUriComponent(idpEntry.getKey()))%>',
                                        'FIDOAuthenticator')" class="main-link" style="cursor:pointer" id="icon-<%=iconId%>">
                                    <img class="idp-image" src="images/login-icon.png" data-toggle="tooltip"
                                         data-placement="top" title="Sign in with FIDO"/>
                                </a>
                                <label for="icon-<%=iconId%>">FIDO</label>

                                </div>
                                <%
                                            }
                                        }

                                    }
                                    }%>

                            </div>


                            <% } %>

                            <div class="clearfix"></div>
                        </div>
                    </div>
                    <!-- /content -->

                </div>
            </div>
            <!-- /content/body -->

        </div>
    </div>

    <!-- footer -->
    <footer class="cFooter">
            <p style="font-size: 14px">&copy;
                <script>document.write(new Date().getFullYear());</script> WSO2
            </p>
        </div>
    </footer>

    <script src="libs/jquery_1.11.3/jquery-1.11.3.js"></script>
    <script src="libs/bootstrap_3.3.5/js/bootstrap.min.js"></script>

    <script>
        $(document).ready(function () {
            $('.main-link').click(function () {
                $('.main-link').next().hide();
                $(this).next().toggle('fast');
                var w = $(document).width();
                var h = $(document).height();
                $('.overlay').css("width", w + "px").css("height", h + "px").show();
            });
            $('[data-toggle="popover"]').popover();
            $('.overlay').click(function () {
                $(this).hide();
                $('.main-link').next().hide();
            });

        });
        function myFunction(key, value, name) {
            var object = document.getElementById(name);
            var domain = object.value;


            if (domain != "") {
                document.location = "../commonauth?idp=" + key + "&authenticator=" + value +
                        "&sessionDataKey=<%=Encode.forUriComponent(request.getParameter("sessionDataKey"))%>&domain=" +
                        domain;
            } else {
                document.location = "../commonauth?idp=" + key + "&authenticator=" + value +
                        "&sessionDataKey=<%=Encode.forUriComponent(request.getParameter("sessionDataKey"))%>";
            }
        }

        function handleNoDomain(key, value) {
            document.location = "../commonauth?idp=" + key + "&authenticator=" + value +
                    "&sessionDataKey=<%=Encode.forUriComponent(request.getParameter("sessionDataKey"))%>";
        }

        $('#popover').popover({
            html: true,
            title: function () {
                return $("#popover-head").html();
            },
            content: function () {
                return $("#popover-content").html();
            }
        });

    </script>

    </body>
    </html>


</fmt:bundle>

