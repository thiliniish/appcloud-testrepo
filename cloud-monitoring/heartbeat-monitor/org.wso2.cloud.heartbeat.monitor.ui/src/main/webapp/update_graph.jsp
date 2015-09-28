<%@ page import="org.json.JSONObject" %>
<%@ page import="org.wso2.cloud.heartbeat.monitoring.ui.ServiceUptime" %>
<%@ page import="org.wso2.cloud.heartbeat.monitoring.ui.ServicesUptimeRetriever" %>
<%@ page
        import="org.wso2.cloud.heartbeat.monitoring.ui.configuration.parser.nginx.utils.Constants" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%

    JSONObject json = new JSONObject();
    List<String> qualifiedTests;

    String serverName = request.getParameter("serverName");
    String testName = request.getParameter("testName");
    String timeIntervalStart = request.getParameter("timeIntervalStart");
    String timeIntervalEnd = request.getParameter("timeIntervalEnd");
    String severityLevel = request.getParameter("severity");

    ServicesUptimeRetriever servicesUptimeRetriever = new ServicesUptimeRetriever();
    servicesUptimeRetriever.setDateTime(timeIntervalEnd, timeIntervalStart);


    String testNameUnsplit = testName;
    qualifiedTests = servicesUptimeRetriever.getTestsForServer(serverName,severityLevel);
    if(qualifiedTests.contains(testNameUnsplit)) {
        String[] splittedTestName = testName.split(":");
        if(splittedTestName.length>0){
            testName = splittedTestName[0];
        }
    }else{
        testName = Constants.AGGREGATION_CLAUSE;
    }
        List<ServiceUptime> serviceUptimes =
            servicesUptimeRetriever.getServiceUptimes(serverName, testName, severityLevel);

        if (serviceUptimes.size() > 0 && qualifiedTests.size() > 0) {
            ServiceUptime serviceUptime = serviceUptimes.get(0);
            if (testName.equals(Constants.AGGREGATION_CLAUSE)) {
                qualifiedTests.add(0, Constants.AGGREGATION_CLAUSE);
            } else {
                qualifiedTests.remove(testNameUnsplit);
                qualifiedTests.add(0, testNameUnsplit);
                qualifiedTests.add(1, Constants.AGGREGATION_CLAUSE);
            }

            json.put(Constants.UPTIMEINFO, serviceUptime.getUptimeInfo());
            json.put(Constants.SUCCESSRATE, serviceUptime.getUptimePercentage());
            json.put(Constants.DOWNTIME, serviceUptime.getNegativeUptime());
            json.put(Constants.FAILURECOUNT, serviceUptime.getFailureCount());
            json.put(Constants.FAILUREDETAIL, serviceUptime.getPairedFailureDetail());
            json.put("Tests", qualifiedTests);
        } else {
            json.put(Constants.UPTIMEINFO, 0);
            json.put(Constants.SUCCESSRATE, Constants.NORECORDSFOUND);
            json.put(Constants.DOWNTIME, 0);
            json.put(Constants.FAILURECOUNT, Constants.NORECORDSFOUND);
            json.put(Constants.FAILUREDETAIL, Constants.NORECORDSFOUND);
            json.put("Tests", 0);
        }
    out.print(json);
%>

