<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!--
~ Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<!-- This is the page which has the tenant drop down. When user submits the credentials, this page will authenticate
the user and create a drop down containing the tenant display names. When the user selects a particular tenant he will be
logged into that tenant
-->
<%@ page import="org.apache.commons.lang.StringUtils"%>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.Constants" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.samlsso.AuthenticationClient" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.samlsso.DBClient"%>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.UUID" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<fmt:bundle
	basename="org.wso2.carbon.identity.application.authentication.endpoint.i18n.samlsso.Resources">
	<%
        boolean singleTenant = false;

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

        //This system property is set in startup script(wso2server.sh)
        String cloudMgtUrl = System.getProperty("cloudMgt.URL");
        String cloudMgtDataSource = application.getInitParameter("cloudMgtDataSource");
        String useEmail = application.getInitParameter("useEmail");
        String replaceWith = application.getInitParameter("replaceWith");

        Map<String, String> tenantDomains = null;
        String tenantDomain = null;

        String url = request.getRequestURL().toString();
        String baseURL = url.substring(0, url.indexOf("authenticationendpoint"));
        String storeTenantDomain = request.getParameter("storeTenantDomain");

        //Username & password
        String userName = request.getParameter("username");
        Object password = request.getParameter("password");

        if(StringUtils.isEmpty(userName) || password == null || password.equals("")) {
            loginFailed = "true";
            errorMessage = "Login failed. username and password cannot be empty.";
        } else {

            //Authenticates the user
            if (!StringUtils.isEmpty(storeTenantDomain) && !storeTenantDomain.equals("null")) {
                // Authentication request initiated for user portal.
                singleTenant = true;
                tenantDomain = storeTenantDomain;
            } else {
                // Authentication request initiated for cloud or admin portal

                String email = userName;

                //If useEmail is not enabled in web.xml convert '@' sign from '.'
                if ("false".equals(useEmail) && userName.contains("@")) {
                    userName = userName.replace("@", replaceWith);
                }
                //Only if authenticated, get tenants the user belonging to
                AuthenticationClient authenticationClient = new AuthenticationClient(baseURL);
                if (authenticationClient.login(userName + "@carbon.super", password, "localhost", null)) {
                    DBClient dbClient = new DBClient(cloudMgtDataSource);
                    tenantDomains = dbClient.getTenantDisplayNames(userName);
                    if (tenantDomains.size() == 1) {
                        singleTenant = true;
                        tenantDomain = tenantDomains.keySet().iterator().next();
                    } else if (tenantDomains.size() == 0) { //User without a tenant (From OT), we will generate a uuid and redirect to registration page
                        UUID uuid = UUID.randomUUID();
                        dbClient.storeTempRegistration(email, uuid.toString());
                        String redirectUrl = cloudMgtUrl + "/site/pages/confirm-verification.jag?confirmation=" + uuid.toString();
                        response.sendRedirect(redirectUrl);
                    }
                } else {
                    loginFailed = "true";
                }
            }
        }
    %>

	<html xmlns="http://www.w3.org/1999/xhtml">

<head>
<meta http-equiv="content-type" content="text/html;charset=utf-8" />
<title>WSO2 Public Cloud</title>
<link type="text/css" rel="stylesheet" media="all"
	href="css/cloud-styles.css">
<link
	href='https://fonts.googleapis.com/css?family=Noto+Sans:400,700,400italic,700italic'
	rel='stylesheet' type='text/css'>
    <link href="js/select2/select2.css" rel="stylesheet"/>
    <link rel="icon" sizes="16x16 32x32" href="<%=baseURL%>/authenticationendpoint/images/cloud-images/favicon.ico" type="image/x-icon"/>
    <link rel="shortcut icon" sizes="16x16 32x32" href="<%=baseURL%>/authenticationendpoint/images/cloud-images/favicon.ico" type="image/x-icon"/>
</head>

<body>
	<div class="cMainScreen">
		<div class="cHeader">
			<div>
				<div class="cHeaderContent">
					<img src="<%=baseURL%>/authenticationendpoint/images//cloud-images/wso2-cloud-logo-2.png" alt="WSO2"
						class="cHeaderLogo" />
					<ul>
						<li><a href="mailto:cloud@wso2.com">Contact :
								cloud@wso2.com</a></li>
					</ul>
				</div>
				<div class="cClear"></div>
			</div>
		</div>
        <%
            if ("true".equals(loginFailed)) {
                String loginForm2ActionUrl = "../authenticationendpoint/login_redirect.jsp";
                if (StringUtils.isNotEmpty(queryString)) {
                    loginForm2ActionUrl = loginForm2ActionUrl.concat('?' + queryString);
                }

        %>      <form action="<%=Encode.forHtmlAttribute(loginForm2ActionUrl)%>" method="post" id="loginForm2" class="form-horizontal" >
                    <div class="row">
                        <div class="span6">
                            <%@ include file="basicauth.jsp"%>
                        </div>
                    </div>
                </form>
                <script type="text/javascript">
                    function doLogin() {
                        var loginForm = document.getElementById('loginForm2');
                        loginForm.submit();
                    }
                </script>
        <%
            } else {
                if(singleTenant){
                    userName  =  userName + "@" + tenantDomain;
        %>
                    <div class="row">
                        <div class="span6">
                            <%@ include file="redirect.jsp"%>
                        </div>
                    </div>
        <%
                }else {
        %>
                    <div class="row">
                        <div class="span6">
                            <%@ include file="tenant.jsp"%>
                        </div>
                    </div>

        <%
                }
            }
        %>
        <footer class="cFooter">
            <p style="font-size: 14px">&copy;
                <script>document.write(new Date().getFullYear());</script> WSO2
            </p>
        </footer>
	</div>

	<script type="text/javascript"
		src="assets/js/jquery-1.7.1.min.js"></script>
	<script type="text/javascript" src="js/jquery.validate.min.js"></script>
	<script type="text/javascript" src="assets/js/start.js"></script>
	<script src="js/select2/select2.js"></script>
	<script>
		$(document).ready(function() {
			$("#tenant").select2({
				placeholder : "",
				allowClear : false
			});
			jQuery.validator.setDefaults({
				errorElement : 'span'
			});
			$('#loginForm').validate({
				rules : {
					tenant : {
						required : true
					}
				},
				messages : {
					tenant : {
						required : 'Please select a domain'
					}
				},
				submitHandler : function(form) {
					doLogin();
				}
			});
		});
	</script>
</body>
	</html>

</fmt:bundle>
