<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <!--<link rel="stylesheet" type="text/css" href="css/cart-styles.css">-->
  <title>WSO2</title>

  <meta http-equiv="content-type" content="text/html;charset=utf-8"/>
  <meta name="generator" content="Adobe GoLive"/>
  <title>WSO2 Cloud Service Health Dashboard</title>
  <link rel="stylesheet" href="css/redmond/jquery-ui-1.9.2.custom.min.css"/>
  <link type="text/css" href="css/style.css" rel="stylesheet" media="all"/>
  <link href="css/jquery.comiseo.daterangepicker.css" rel="stylesheet">
  <script type="text/javascript" src="js/jquery-1.8.3.min.js"></script>
  <script src="js/jquery-ui-1.9.2.custom.min.js"></script>
  <script type="text/javascript" src="js/gimiks.js"></script>
  <script language="javascript" type="text/javascript" src="js/jquery.flot.js"></script>
  <script language="javascript" type="text/javascript" src="js/jquery-ui.js"></script>
  <script src="js/moment.min.js"></script>
  <link href="css/flot.css" rel="stylesheet" type="text/css">
  <link href="css/jquery-ui.css" rel="stylesheet" type="text/css">
  <script src="js/jquery.comiseo.daterangepicker.js"></script>
  <script language="javascript" type="text/javascript" src="js/jquery.flot.time.js"></script>
  <!--[if lte IE 8]>
  <script language="javascript" type="text/javascript" src="../../excanvas.min.js"></script><![endif]-->
  <script type="text/javascript">
    var onFullScreen = false;

    $(document).ready(function () {
      $.get('service_uptime.jsp', function (responseText) {
        $('#status').html(responseText);
      });
      $.ajaxSetup({cache: true});

    });


  </script>

</head>
<%
    //allow access only if session exists
    String user = (String) session.getAttribute("user");
    String userName = null;
    String sessionID = null;
    Cookie[] cookies = request.getCookies();
    if(cookies !=null){
        for(Cookie cookie : cookies){
            if(cookie.getName().equals("user")) userName = (String) request.getSession(false).getAttribute("user");
            if(cookie.getName().equals("JSESSIONID")) sessionID = cookie.getValue();
        }
    }
%>

<body>
<div class="middle">
    <div class="header">
        <ul class="header-links">
            <li class="wso2">
                <a href="http://wso2.com" target="_blank"></a>
            </li>
            <li class="contact">
                <a href="http://wso2.com/contact/" target="_blank">Contact</a>
            </li>
            <li class="support">
                <a href="http://wso2.com/support/" target="_blank">Support</a>
            </li>

        </ul>

    </div>
</div>
<div class="title">
    <table class="title_table">
        <tr>
            <td class="title-image"><a target="_blank" href="https://cloudpreview.wso2.com/"><img
                    src="images/logo.png"/></a></td>
            <td class="title_text">Service Uptime Dashboard</td>
        </tr>
    </table>
</div>
<div class="full-screen" id="full-screen"></div>
<div id="main-content">

    <form action="LogoutServlet" method="post" style="float:right; margin-right:10px;">
        <input type="submit" value="Logout" style="margin-right:10px; background-color:transparent; font-size: 13px; border:none; text-decoration: none;">
    </form>

    <div id="content">
        <div id="status" class="status_div"></div>

    </div>



    <div class="footer">
        &copy;2015WSO2
    </div>
</div>
<script src="js/moment-timezone.min.js"></script>
</body>
</html>