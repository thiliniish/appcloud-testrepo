<%@ page import="java.net.URLEncoder" %>
<div class="cMainContent">
	<div class="container">
		<div class="row">
			<div class="col-lg-12 login-box-wrapper">
				<div class="login-box">
					<div class="login-box-top">
						<div class="login-box-top-inside">
							<h2 class="login-sub-title">Select an Organization</h2>

							<form method="post" action="../../commonauth" id="loginForm">
								<div class="domain-wrapper">
									<select name="tenant" id="tenant" style="width: 100%">
										<%
											for(String str: tenantDomains.keySet()){
										%>
										<option value="<%=str%>"><%=tenantDomains.get(str)%></option>
										<%
											}
										%>
									</select>
									<input type="hidden" name="username" id='username' value="<%=userName%>" />
									<input type="hidden" name="password" id='password'
										   value="<%=URLEncoder.encode((String)password,"UTF-8")%>" />
									<input type="hidden" name="sessionDataKey"
										   value='<%=CharacterEncoder.getSafeText(request.getParameter("sessionDataKey"))%>'/>
									<input type="hidden" name="chkRemember" id="chkRemember" value="on">
								</div>
								<div class="login-button-wrapper">
									<div class="loginError" id="loginError"></div>
									<button class="btn btn-primary login-button" type="submit"
										id="loginButton">Go</button>
								</div>
							</form>
						</div>
					</div>
				</div>
				<br />
				<div class="login-box-footer"></div>
			</div>
		</div>
	</div>
</div>

<script type="text/javascript">
	//We need to append the tenant domain and decode the password before submitting
	function doLogin() {
		var loginForm = document.getElementById('loginForm');
		var username = document.getElementById('username');
		var tenant = document.getElementById('tenant');
		var userNameWithDomain = username.value + "@" + tenant.value;
		username.value = userNameWithDomain;
		var password = document.getElementById('password');
		password.value = decodeURIComponent(password.value);
		loginForm.submit();
	}
</script>