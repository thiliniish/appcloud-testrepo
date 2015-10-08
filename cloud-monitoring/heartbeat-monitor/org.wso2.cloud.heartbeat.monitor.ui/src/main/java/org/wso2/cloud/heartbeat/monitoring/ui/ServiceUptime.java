/*
  * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  *
  * WSO2 Inc. licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
package org.wso2.cloud.heartbeat.monitoring.ui;

import org.wso2.cloud.heartbeat.monitoring.ui.configuration.parser.nginx.utils.Constants;
import org.wso2.cloud.heartbeat.monitoring.ui.utils.Pair;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Uptime Information Bean for each server to testname specific detail
 * Contains total incident failure and success count, server up time, test severity,
 * uptime information map for timestamp to failure status
 */
public class ServiceUptime {
    private List<Long> filteredPositiveList;
    private Map<Timestamp, Byte> upTimeInfo;
    private List<Timestamp> positiveUpTimeInfo;
    private List<Pair> mergedNegativeIntervals;
    private Map<Map<String, Map>, List<Pair>> pairedFailureDetail;
    private int failureCount;
    private int successCount;
    private String serviceName;
    private String testName;
    private int severity;
    private long negativeUptime;
    private long positiveUptime;
    private float uptimePercentage;
    private int totalCount;
    private float totalTime;

    public ServiceUptime(String serviceName, String testName) {
        this.serviceName = serviceName;
        this.testName = checkConventions(testName);
        this.upTimeInfo = new TreeMap<Timestamp, Byte>();
        this.positiveUpTimeInfo = new ArrayList<Timestamp>();
    }

    public void addUptimeInfo(Timestamp dateTime, byte status) {
        upTimeInfo.put(dateTime, status);
    }

    public List<Timestamp> getPositiveUpTimeInfo() {
        return positiveUpTimeInfo;
    }

    public List<Pair> getMergedNegativeIntervals() {
        return mergedNegativeIntervals;
    }

    public void setMergedNegativeIntervals(List<Pair> mergedNegativeIntervals) {
        this.mergedNegativeIntervals = mergedNegativeIntervals;
    }

    public Map<Map<String, Map>, List<Pair>> getPairedFailureDetail() {
        return pairedFailureDetail;
    }

    public void setPairedFailureDetail(Map<Map<String, Map>, List<Pair>> pairedFailureDetail) {
        this.pairedFailureDetail = pairedFailureDetail;
    }

    public List<Long> getFilteredPositiveList() {
        return filteredPositiveList;
    }

    public void setFilteredPositiveList(List<Long> filteredPositiveList) {
        this.filteredPositiveList = filteredPositiveList;
    }

    public long getNegativeUptime() {
        return negativeUptime;
    }

    public long getPositiveUptime() {
        return positiveUptime;
    }

    public void setPositiveUptime(long positiveUptime) {
        this.positiveUptime = positiveUptime;

    }

    public void countFailureTime() throws ParseException {
        countNegativeUpTime(upTimeInfo);
    }

    public void addFailureCount() {
        failureCount++;
    }

    public void addSuccessCount() {
        successCount++;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getTestName() {
        return testName;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;

    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severityVal) {
        severity = severityVal;
    }

    public void setPositiveUpTimeInfo(Timestamp timeStamp){
        this.positiveUpTimeInfo.add(timeStamp);
    }

    public String getUptimePercentage() {
        totalTime = negativeUptime + positiveUptime;
        uptimePercentage = (positiveUptime / totalTime) * 100;
        DecimalFormat df = new DecimalFormat(Constants.DECIMALFORMAT);

        return df.format(uptimePercentage);
    }

    public int getCount() {
        totalCount = successCount + failureCount;
        return totalCount;
    }

    public Map<Timestamp, Byte> getUptimeInfo() {
        return upTimeInfo;
    }

    private String checkConventions(String testName) {
        Set<String> conventionSet = getConventionSet();
        if (conventionSet.contains(testName)) {
            return testName.toUpperCase();
        } else {
            return testName;
        }
    }

    private Set<String> getConventionSet() {
        Set<String> conventionCodeSet = new HashSet<String>();
        conventionCodeSet.add("Axis 2");
        conventionCodeSet.add("Jaxrs");
        conventionCodeSet.add("Jaxws");
        conventionCodeSet.add("Bpel");
        conventionCodeSet.add("Jms");
        return conventionCodeSet;

    }

    /**
     * Calculate downtime for the service using the uptime information map
     *
     * @param upTimeInfoMap Map of Timestamp to server uptime status
     * @return total downtime for the requested service uptime
     * @throws ParseException
     */
    public long countNegativeUpTime(Map<Timestamp, Byte> upTimeInfoMap) throws ParseException {
        Object firstElement = upTimeInfoMap.keySet().toArray()[0];
        byte currentStatus = upTimeInfoMap.get(firstElement);
        positiveUptime = 0;
        negativeUptime = 0;
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATEFORMAT);
        long lastStatusRecordedAt = dateFormat.parse(firstElement.toString()).getTime();
        long newTestRecordedAt;

        for (Map.Entry<Timestamp, Byte> entry : upTimeInfoMap.entrySet()) {
            newTestRecordedAt = dateFormat.parse(entry.getKey().toString()).getTime();
            if (entry.getValue() == currentStatus) {
                if (currentStatus == 0) {
                    negativeUptime = negativeUptime + (newTestRecordedAt - lastStatusRecordedAt);
                } else if (currentStatus == 1) {
                    positiveUptime = positiveUptime + (newTestRecordedAt - lastStatusRecordedAt);
                }
                lastStatusRecordedAt = newTestRecordedAt;

            } else {
                if (currentStatus == 0) {
                    negativeUptime = negativeUptime + (newTestRecordedAt - lastStatusRecordedAt);
                    currentStatus = 1;
                } else {
                    positiveUptime = positiveUptime + (newTestRecordedAt - lastStatusRecordedAt);
                    currentStatus = 0;
                }
                lastStatusRecordedAt = newTestRecordedAt;
            }
        }
        return negativeUptime;
    }
}
