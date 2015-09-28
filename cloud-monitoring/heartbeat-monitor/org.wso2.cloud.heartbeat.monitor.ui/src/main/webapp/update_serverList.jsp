<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="org.wso2.cloud.heartbeat.monitoring.ui.ServicesUptimeRetriever" %>
<%@ page
        import="org.wso2.cloud.heartbeat.monitoring.ui.configuration.parser.nginx.utils.Constants" %>
<%@ page import="org.wso2.cloud.heartbeat.monitoring.ui.utils.ConfigReader" %>
<%@ page import="java.util.ArrayList" %>
<%@ page
        import="java.util.List" %>
<%

    String cloudName = (String) request.getParameter("cloudName");
    String serverName = (String) request.getParameter("serverName");
    JSONObject json = new JSONObject();
    List<String> qualifiedServices = new ArrayList<String>();
    ServicesUptimeRetriever serviceUptimeRetriever = new ServicesUptimeRetriever();
    List<String> serviceList =
            ConfigReader.getInstance().getCloudStructure().get(cloudName).getServiceList();
    json.put("Services", "Default");

    for (String qualifiedServicesFor : serviceList) {
        if (serviceUptimeRetriever.hasRecords(qualifiedServicesFor)) {
            qualifiedServices.add(qualifiedServicesFor);
        }
    }
    if (qualifiedServices.size() > 0) {
        json.remove("Services");
        if (serverName.equals(Constants.AGGREGATION_CLAUSE)) {
            qualifiedServices.add(0, Constants.AGGREGATION_CLAUSE);
        } else {
            qualifiedServices.remove(serverName);
            qualifiedServices.add(0, serverName);
            qualifiedServices.add(1, Constants.AGGREGATION_CLAUSE);
        }
        json.put("Services", qualifiedServices);
    }
    out.print(json);
%>