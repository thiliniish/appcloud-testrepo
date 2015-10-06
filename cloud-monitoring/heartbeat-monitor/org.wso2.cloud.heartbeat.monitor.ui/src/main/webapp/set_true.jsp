<%@ page import="org.json.JSONObject" %>
<%@ page import="org.wso2.cloud.heartbeat.monitoring.ui.ServicesUptimeRetriever" %><%

  String failureIndexes = request.getParameter("failureIndexes");
  String changeInfo = request.getParameter("changeInfo");
  String userId;
  int done;

  HttpSession thisSession = request.getSession(false);
  userId = thisSession.getAttribute("user").toString();

  JSONObject json = new JSONObject();
  ServicesUptimeRetriever servicesUptimeRetriever = new ServicesUptimeRetriever();

  done = servicesUptimeRetriever.setFalseToTrue(failureIndexes);
  done = servicesUptimeRetriever.inputFalseFailureReason(failureIndexes, userId, changeInfo);
  if(done > 0){
    json.put("Status","Changed as a false alarm");
  }
  else {
    json.put("Status","Error occurred while changing");
  }

out.print(json);
%>