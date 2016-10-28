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

<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.Constants" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%
    String cloudMgtUrl = application.getInitParameter("cloudMgtUrl");
    String stat = request.getParameter(Constants.STATUS);
    String statusMessage = request.getParameter(Constants.STATUS_MSG);
    if (stat == null || statusMessage == null) {
        stat = "Authentication Error !";
        statusMessage = "Something went wrong during the authentication process. Please try signing in again.";
    }
    session.invalidate();
%>
<style>
    .info-box {
        background-color: #EEF3F6;
        border: 1px solid #ABA7A7;
        font-size: 13px;
        font-weight: bold;
        margin-bottom: 10px;
        padding: 10px;
    }
</style>

<fmt:bundle basename="org.wso2.carbon.identity.application.authentication.endpoint.i18n.Resources">
    <html>
    <head>
        <meta http-equiv="content-type" content="text/html;charset=utf-8"/>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>WSO2 Public Cloud</title>
        <script src="assets/js/jquery-1.7.1.min.js" type="text/javascript"></script>
        <script src="js/scripts.js" type="text/javascript"></script>
        <link type="text/css" rel="stylesheet" media="all" href="css/cloud-styles.css">
        <link href="css/custom-common.css" rel="stylesheet">
        <link href='https://fonts.googleapis.com/css?family=Noto+Sans:400,700,400italic,700italic' rel='stylesheet'
              type='text/css'>
        <!--[if lt IE 9]>
        <script src="js/html5shiv.min.js"></script>
        <script src="js/respond.min.js"></script>
        <![endif]-->
        <style>
            .info-box {
                background-color: #cdecf5;
                border: 1px solid #101010;
                font-size: 13px;
                font-weight: bold;
                margin-bottom: 10px;
                padding: 10px;
            }
        </style>
    </head>
    <body>
    <div class="cMainScreen">
        <div id="local_auth_div" class="container main-login-container">
            <div id="middle">
                <div id="workArea">
                    <div class="info-box">
                        <h2>SSO <%=stat%>
                        </h2>

                        <h3><%=statusMessage%><a href="<%=cloudMgtUrl%>">[Go to Login Page]</a></h3>
                    </div>
                </div>
            </div>
        </div>
    </div>
    </body>
    </html>
</fmt:bundle>



