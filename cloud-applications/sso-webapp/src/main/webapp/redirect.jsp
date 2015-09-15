<!--This is for users only with one tenant-->
<%@ page import="java.net.URLEncoder" %>
<body bgcolor="#272C38">
<form action="../../commonauth" method="post">

    <input type="hidden" name="username" id='username' value="<%=userName%>"/>
    <input type="hidden" name="password" id='password' value="<%=URLEncoder.encode((String)request.getParameter("password"),"UTF-8")%>"/>
    <input type="hidden" name="sessionDataKey" value='<%=CharacterEncoder.getSafeText(request.getParameter("sessionDataKey"))%>'/>
    <input type="hidden" id="chkRemember" name="chkRemember" value="on">

</form>
<body>
<script type="text/javascript">
    //we need to decode the password before submitting
    var password = document.getElementById('password');
    password.value = decodeURIComponent(password.value);
    document.forms[0].submit();
</script>




