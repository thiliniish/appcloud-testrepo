<%@ page contentType="application/json;charset=UTF-8" language="java" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="org.wso2.cloud.heartbeat.monitoring.ui.ServiceUptime"%>
<%@ page import="org.wso2.cloud.heartbeat.monitoring.ui.ServicesUptimeRetriever"%>
<%@ page import="org.wso2.cloud.heartbeat.monitoring.ui.configuration.parser.nginx.utils.Constants"%>
<%@ page import="org.wso2.cloud.heartbeat.monitoring.ui.utils.ConfigReader"%>
<%@ page import="org.wso2.cloud.heartbeat.monitoring.ui.utils.IntervalMerger"%>
<%@ page import="org.wso2.cloud.heartbeat.monitoring.ui.utils.Pair"%>
<%@ page import="java.sql.Timestamp"%>
<%@ page import="java.util.*"%>
<%
String cloudName = request.getParameter("cloudName");
String timeIntervalStart = request.getParameter("timeIntervalStart");
String timeIntervalEnd = request.getParameter("timeIntervalEnd");
String severityLevel = request.getParameter("severity");

JSONObject json = new JSONObject();
ServiceUptime serviceUptime;
List<ServiceUptime> serviceUptimes = new ArrayList<ServiceUptime>();
List<String> qualifiedServices = new ArrayList<String>();
List<Long> negativeList = new ArrayList<Long>();
List<Long> valuesToRemove = new ArrayList<Long>();
IntervalMerger mergeIntervals = new IntervalMerger();
List<Pair> allServerNegativeIntervals = new ArrayList<Pair>();
List<Long> allServerFilteredPositiveTimes  = new ArrayList<Long>();
Map<Map<String, Map>,List<Pair>>  failureSummary = new HashMap< Map<String, Map>,List<Pair>>();
int failureCount=0;

ServicesUptimeRetriever serviceUptimeRetriever = new ServicesUptimeRetriever();
serviceUptimeRetriever.setDateTime(timeIntervalEnd,timeIntervalStart);

List<String> serviceList = ConfigReader.getInstance().getCloudStructure().get(cloudName).getServiceList();

for(String qualifiedServicesFor : serviceList){
    if(serviceUptimeRetriever.hasRecords(qualifiedServicesFor, severityLevel)){
        qualifiedServices.add(qualifiedServicesFor);
    }
}

if(qualifiedServices.size()>0){
for(String serviceName:qualifiedServices){
    serviceUptimes.clear();
    serviceUptimes = serviceUptimeRetriever.getServiceUptimes(serviceName,Constants.AGGREGATION_CLAUSE, severityLevel);
    if(serviceUptimes.size() >0 ){
        ServiceUptime serviceUptimen = serviceUptimes.get(0);
        failureCount = failureCount + serviceUptimen.getFailureCount();
        allServerNegativeIntervals.addAll(serviceUptimen.getMergedNegativeIntervals());
        allServerFilteredPositiveTimes.addAll(serviceUptimen.getFilteredPositiveList());
        serviceUptimen.getPairedFailureDetail();
        failureSummary.putAll(serviceUptimen.getPairedFailureDetail());
    }
}
List<Pair> mergedNegativeTimeIntervals = mergeIntervals.merge(allServerNegativeIntervals);
    for (Pair individualInterval : mergedNegativeTimeIntervals) {
        negativeList.addAll(Arrays.asList(individualInterval.getLeft(),
                                          individualInterval.getRight()));
        for (Long positiveEntry : allServerFilteredPositiveTimes) {
            if (individualInterval.getLeft() < positiveEntry &&
                individualInterval.getRight() > positiveEntry) {
                valuesToRemove.add(positiveEntry);
            }
        }
    }
    allServerFilteredPositiveTimes.removeAll(valuesToRemove);

    //Building the merged uptime Information Graph and Service Uptime object for all Servers
    serviceUptime = new ServiceUptime(cloudName,Constants.AGGREGATION_CLAUSE);
    serviceUptime.setPairedFailureDetail(failureSummary);
    for (Long postiveRecord : allServerFilteredPositiveTimes) {
        Timestamp timestamp = new Timestamp(postiveRecord);
        serviceUptime.addUptimeInfo(timestamp, (byte) 1);
        serviceUptime.setPositiveUpTimeInfo(timestamp);
    }
    for (Long negativeRecord : negativeList) {
        serviceUptime.addUptimeInfo(new Timestamp(negativeRecord), (byte) 0);
    }
    serviceUptime.setFailureCount(failureCount);
    serviceUptime.countFailureTime();
    json.put(Constants.UPTIMEINFO,serviceUptime.getUptimeInfo());
    json.put(Constants.SUCCESSRATE,serviceUptime.getUptimePercentage());
    json.put(Constants.FAILURECOUNT,serviceUptime.getFailureCount());
    json.put(Constants.DOWNTIME,serviceUptime.getNegativeUptime());
    json.put(Constants.FAILUREDETAIL,serviceUptime.getPairedFailureDetail());
}
else {
    json.put(Constants.SUCCESSRATE, Constants.NORECORDSFOUND);
    json.put(Constants.DOWNTIME, 0);
    json.put(Constants.FAILURECOUNT, Constants.NORECORDSFOUND);
    json.put(Constants.FAILUREDETAIL, Constants.NORECORDSFOUND);
    json.put(Constants.UPTIMEINFO,0);
}
out.print(json);
%>
