<%@ page import="org.json.JSONObject" %>
<%@ page import="org.wso2.cloud.heartbeat.monitoring.ui.ServicesUptimeRetriever" %>
<%


    String jiraUrl = request.getParameter("jiraUrl");
    String arrayOfCheckedId = request.getParameter("checkedIds");

    int done;

    JSONObject json = new JSONObject();
    ServicesUptimeRetriever servicesUptimeRetriever = new ServicesUptimeRetriever();
    done = servicesUptimeRetriever.setJiraLink(jiraUrl, arrayOfCheckedId);

    if (done > 0) {
    json.put("Status","Changed as a false alarm");
  }
  else {
    json.put("Status","Error occurred while changing");
    }

    out.print(json);
%>