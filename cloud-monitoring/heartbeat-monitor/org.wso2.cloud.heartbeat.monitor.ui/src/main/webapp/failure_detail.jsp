<%@ page import="org.json.JSONObject" %>
<%@ page import="org.wso2.cloud.heartbeat.monitoring.ui.ServicesUptimeRetriever" %>
<%@ page import="java.sql.Timestamp" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%

    String serverName = request.getParameter("serverName");
    String testName = request.getParameter("testName");
    String timeIntervalStart = request.getParameter("timeIntervalStart");
    String timeIntervalEnd = request.getParameter("timeIntervalEnd");

    /*Date endDate = new Date(Long.parseLong(timeIntervalEnd));
    Date startDate = new Date(Long.parseLong(timeIntervalStart));

    SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    String d1 = df2.format(endDate);
    String d2 = df2.format(startDate);
    servicesUptimeRetriever.setDateTime(d1, d2);*/

    JSONObject json = new JSONObject();
    ServicesUptimeRetriever servicesUptimeRetriever = new ServicesUptimeRetriever();

    Map<Timestamp, List<String>> failureDetail;

    failureDetail = servicesUptimeRetriever
            .getFailureDetail(serverName, testName, timeIntervalEnd, timeIntervalStart);

    json.put("failureInfo", failureDetail);

    out.print(json);

%>
