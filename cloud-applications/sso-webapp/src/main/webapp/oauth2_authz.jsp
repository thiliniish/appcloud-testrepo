<!--
~ Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.CharacterEncoder"%>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.Constants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    String loggedInUser = CharacterEncoder.getSafeText(request.getParameter("loggedInUser"));
    if(loggedInUser.contains("carbon.super")){
        loggedInUser = loggedInUser.substring(0,loggedInUser.indexOf("@"));
    }
    String scopeString = CharacterEncoder.getSafeText(request.getParameter("scope"));
    String url = request.getRequestURL().toString();
    String baseURL = url.substring(0,
            url.indexOf("authenticationendpoint"));
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="content-type" content="text/html;charset=utf-8" />
    <title>WSO2 Public Cloud</title>

    <link type="text/css" rel="stylesheet" media="all" href="css/cloud-styles.css">
    <link href='http://fonts.googleapis.com/css?family=Noto+Sans:400,700,400italic,700italic' rel='stylesheet' type='text/css'>
    <script src="assets/js/jquery-1.7.1.min.js"></script>
    <script src="js/scripts.js"></script>
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
    <script type="text/javascript">
        function approved() {
            document.getElementById('consent').value="approve";
            document.getElementById("oauth2_authz").submit();
        }
        function approvedAlways() {
            document.getElementById('consent').value="approveAlways";
            document.getElementById("oauth2_authz").submit();
        }
        function deny() {
            document.getElementById('consent').value="deny";
            document.getElementById("oauth2_authz").submit();
        }
    </script>
    <div class="cMainContent">
        <div class="container">
            <div class="row">
                <div class="col-lg-12 login-box-wrapper">

                    <div class="login-box">
                        <div class="login-box-top">
                            <div class="login-box-top-inside">
                                <h2 class="login-sub-title">You are logged in as <p><%=loggedInUser%></p>
                                    <%=CharacterEncoder.getSafeText(request.getParameter("application"))%> requests access to <%=scopeString%></h2>
                                <form id="oauth2_authz" name="oauth2_authz" method="post" action="../oauth2/authorize">
                                    <div class="login-button-wrapper">
                                        <input type="button" class="btn btn-primary btn-large" id="approve" name="approve"
                                               onclick="javascript: approved(); return false;"
                                               value="Approve"/>
                                        <input type="button" class="btn btn-primary btn-large" id="approveAlways" name="approveAlways"
                                               onclick="javascript: approvedAlways(); return false;"
                                               value="Approve Always"/>
                                        <input class="btn btn-primary btn-large btn-danger" type="reset"
                                               value="Deny" onclick="javascript: deny(); return false;" />
                                        <input type="hidden" name="<%=Constants.SESSION_DATA_KEY_CONSENT%>"
                                               value="<%=CharacterEncoder.getSafeText(request
                                                            .getParameter(Constants.SESSION_DATA_KEY_CONSENT))%>" />
                                        <input type="hidden" name="consent" id="consent"
                                               value="deny" />
                                    </div>
                                </form>

                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="cFooter">&copy;2014 WSO2</div>
</div>
</body>
</html>

