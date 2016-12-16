<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.CharacterEncoder"%>
<div class="cMainContent">
    <div class="container">
        <div class="row">
            <div class="col-lg-12 login-box-wrapper">

                <div class="login-box">
                    <div class="login-box-top">
                        <div class="login-box-top-inside">
                            <h2 class="login-sub-title">Log in to WSO2 Cloud</h2>
                                <%
//                                    loginFailed = CharacterEncoder.getSafeText(request.getParameter("loginFailed"));
                                    if ("true".equals(loginFailed)) {

                                %>
                                        <div class="message-error">
                                            <ul>
                                                <li><%=errorMessage%></li>
                                            </ul>
                                        </div>
                                <% } %>

                                <%--<% if (CharacterEncoder.getSafeText(request.getParameter("username")) == null || "".equals--%>
                                <%--(CharacterEncoder.getSafeText(request.getParameter("username")).trim())) { %>--%>

                                    <!-- Username -->
                                    <div class="username-wrapper">
                                        <input name="username" id="username" class="input-username"
                                               type="email" placeholder="Email" />
                                    </div>

                                <%--<%} else { %>--%>

                                    <%--<input type="hidden" id='username' name='username' value='<%=CharacterEncoder.getSafeText--%>
                                    <%--(request.getParameter("username"))%>'/>--%>

                                <%--<% } %>--%>

                                <!--Password-->
                                <div class="password-wrapper">
                                    <input name="password" id="password" class="input-password"
                                           type="password" placeholder="Password" />
                                    <input type="hidden" name="sessionDataKey"
                                           value='<%=CharacterEncoder.getSafeText(request.getParameter("sessionDataKey"))%>'/>
                                    <%--<label class="checkbox" style="margin-top:10px"><input type="checkbox" id="chkRemember" name="chkRemember"><fmt:message key='remember.me'/></label>--%>
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
    <div class="cFooter">&copy;2015 WSO2</div>
</div>


