<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%--
  ~ Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file except
  ~ in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<head>

    <%
        String uptimeServiceUrl = application.getInitParameter("uptimeService");
    %>
    <meta http-equiv="content-type" content="text/html;charset=utf-8"/>
    <meta name="generator" content="Adobe GoLive"/>
    <title>WSO2 Cloud Monitoring Dashboard</title>

    <link rel="icon" sizes="16x16 32x32" href="images/favicon.ico" type="image/x-icon"/>

    <link type="text/css" href="css/style.css" rel="stylesheet" media="all"/>
    <link rel="stylesheet" href="css/font-wso2/css/font-wso2.css">

    <script src="js/jquery/jquery-3.1.0.min.js" type="text/javascript"></script>
    <script src="js/pages/index.js" type="text/javascript"></script>

    <link rel="stylesheet" href="css/jquery-ui/jquery-ui.css">
    <script src="js/jquery/ui/jquery-ui.js" type="text/javascript"></script>
</head>

<body>
<input type="hidden" name="uptime-service-url" id='uptime-service-url' value="<%= uptimeServiceUrl %>" />
<div class="header">
    <ul class="header-links">
        <li class="logo">
            <a href="https://cloud.wso2.com">
                <img class="logo" src="images/logo-inverse.svg" alt="WSO2"/>
            </a>
        </li>
        <li class="cloud">
            Cloud
        </li>
        <li class="contact">
            <a href="mailto:cloud@wso2.com">Contact : cloud@wso2.com</a>
        </li>
    </ul>
</div>
<div class="title">
    <table class="title_table">
        <tr>
            <td class="title-text">WSO2 Cloud Services - Current Status</td>
            <td class="title-date" id="title-date">
                <p id="overview-time" class="overview-time"></p>
                <p id="client-timezone-offset" class="client-timezone-offset"></p>
            </td>
        </tr>
    </table>
</div>

<div id="accordion">
</div>


    <div class="line-separator"></div>
    <div class="legend">
        <table cellspacing="0">
            <tr>
                <td><img class="legend-status-image" src="images/status/up.png" alt="up"/></td>
                <td>Service operating normally</td>
                <td><img class="legend-status-image" src="images/status/down.png" alt="failure"/></td>
                <td>Service Down</td>
                <td><img class="legend-status-image" src="images/status/disruptions.png" alt="live-with-error"/></td>
                <td>Service Disruptions</td>
                <td><img class="legend-status-image" src="images/status/maintenance.png" alt="maintenance"/></td>
                <td>Service Maintenance</td>
                <td><img class="legend-status-image" src="images/status/not-available.png" alt="maintenance"/></td>
                <td>Not Data Available</td>
            </tr>
        </table>
    </div>


<div id="footer">
    <div class="footer-text">WSO2 Cloud &copy; 2016 <i class="fw fw-wso2 fw-2x"></i> All Rights Reserved.</div>
</div>
</body>
