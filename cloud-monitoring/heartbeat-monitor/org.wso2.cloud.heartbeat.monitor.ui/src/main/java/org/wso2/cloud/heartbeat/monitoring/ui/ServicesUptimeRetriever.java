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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.cloud.heartbeat.monitoring.ui.configuration.parser.nginx.utils.Constants;
import org.wso2.cloud.heartbeat.monitoring.ui.utils.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Data retrieval Class for all the uptime related information, acts as the broker between
 * UI and database to retrieve process and transfer data.
 */

public class ServicesUptimeRetriever {
    private static final Log log = LogFactory.getLog(ServicesUptimeRetriever.class);
    ConfigReader configObject;
    private List<ServiceUptime> serviceUptimes;
    private DataAccessManager dataAccessManager;
    private Long fromDateTime;
    private Long toDateTime;

    /**
     * Constructor
     */
    public ServicesUptimeRetriever() {
        this.serviceUptimes = new ArrayList<ServiceUptime>();
        configObject = ConfigReader.getInstance();
    }

    /**
     * getServiceUptimes for dynamic result retrieval
     *
     * @param serviceName String Name of the Service
     * @param testName    String Name of the Expected test
     * @return List of service Uptime Details
     */
    public List<ServiceUptime> getServiceUptimes(String serviceName, String testName,
                                                 String severityLevel)
            throws HeartbeatExceptions, SQLException {
        dataAccessManager = new DataAccessManager(configObject.getDataSourceFromNode());
        try {
            getUptimeInfo(serviceName, testName, severityLevel);
        } catch (SQLException e) {
            log.error(Constants.SQL_EXCEPTION, e);
            throw new HeartbeatExceptions(Constants.SQL_EXCEPTION, e);
        } catch (ParseException e) {
            log.error(Constants.PARSE_EXCEPTION, e);
            throw new HeartbeatExceptions(Constants.PARSE_EXCEPTION, e);
        } finally {
            dataAccessManager.closeConnection();
        }
        return serviceUptimes;
    }

    /**
     * Populate ArrayList from database using
     *
     * @param testName String Test Name to retrieve
     * @throws SQLException
     */
    public void getUptimeInfo(String serviceName, String testName, String severityLevel)
            throws SQLException, ParseException {
        ResultSet resultSet;
        ServiceUptime serviceUptime;
        int failureCount = 0;

        FailureIntervals failureIntervals;

        List<Pair> negativeIntervals = new ArrayList<Pair>();
        List<String> queryParameters = new ArrayList<String>();
        List<String> qualifiedTests = new LinkedList<String>();
        List<Long> positiveList = new ArrayList<Long>();
        List<Long> negativeList = new ArrayList<Long>();
        List<Long> valuesToRemove = new ArrayList<Long>();
        IntervalMerger mergeIntervals = new IntervalMerger();
        Map<Map<String, Map>, List<Pair>> failureSummary =
                new HashMap<Map<String, Map>, List<Pair>>();

        if (testName.equals((Constants.AGGREGATION_CLAUSE))) {
            qualifiedTests.addAll(getTestsForServer(serviceName, severityLevel));
        } else {
            qualifiedTests.add(testName);
        }
        log.info("Heartbeat - Monitor - Retrieving Test Results for : " + serviceName);
        queryParameters
                .addAll(Arrays.asList(serviceName, toDateTime.toString(), fromDateTime.toString()));
        for (String individualTest : qualifiedTests) {
            Map<String, Map> failureKey = new HashMap<String, Map>();
            Map<String, String> testToSeverity = new HashMap<String, String>();
            String severity = "2";
            queryParameters.add(1, individualTest);
            dataAccessManager = new DataAccessManager(configObject.getDataSourceFromNode());
            resultSet =
                    dataAccessManager.runQuery(Constants.GET_UPTIME_INFO_QUERY, queryParameters);
            queryParameters.remove(1);
            log.info("Heartbeat - Monitor - Obtaining down time intervals for test : " +
                     individualTest);
            Map<Long, Byte> individualTestUptime = new TreeMap<Long, Byte>();

            while (resultSet.next()) {
                individualTestUptime.put(resultSet.getLong(Constants.DB_TIMESTAMP),
                                         resultSet.getByte(Constants.DB_STATUS));
                severity = resultSet.getString(Constants.DB_SEVERITY);
            }
            resultSet.close();
            dataAccessManager.closeConnection();

            failureIntervals = getDownTimeIntervals(individualTestUptime);

            negativeIntervals.addAll(failureIntervals.getListPair());
            positiveList.addAll(failureIntervals.getListLong());
            failureCount = failureCount + failureIntervals.getFailureCount();

            if (failureIntervals.getFailureCount() > 0) {
                testToSeverity.put(individualTest, severity);
                failureKey.put(serviceName, testToSeverity);
                failureSummary.put(failureKey, failureIntervals.getListPair());
            }
        }

        List<Pair> mergedNegativeTimeIntervals = mergeIntervals.merge(negativeIntervals);

        //Removing Values within the Time Interval to emphasize downtime of a particular server
        for (Pair individualInterval : mergedNegativeTimeIntervals) {
            negativeList.addAll(Arrays.asList(individualInterval.getLeft(),
                                              individualInterval.getRight()));
            for (Long positiveEntry : positiveList) {
                if (individualInterval.getLeft() < positiveEntry &&
                    individualInterval.getRight() > positiveEntry) {
                    valuesToRemove.add(positiveEntry);
                }
            }
        }
        positiveList.removeAll(valuesToRemove);

        //Building the merged uptime Information Graph and Service Uptime object for all tests
        serviceUptime = new ServiceUptime(serviceName, testName);
        serviceUptime.setPairedFailureDetail(failureSummary);
        serviceUptime.setMergedNegativeIntervals(mergedNegativeTimeIntervals);
        serviceUptime.setFilteredPositiveList(positiveList);
        for (Long postiveRecord : positiveList) {
            serviceUptime.addUptimeInfo(new Timestamp(postiveRecord), true);
        }
        for (Long negativeRecord : negativeList) {
            serviceUptime.addUptimeInfo(new Timestamp(negativeRecord), false);
        }
        //serviceUptime.setFailureCount(failureCount);
        serviceUptime.setFailureCount(negativeIntervals.size());
        if (negativeIntervals.size() > 0 || positiveList.isEmpty()) {
            //serviceUptime.setFailureCount(mergedNegativeTimeIntervals.size());
            serviceUptime.countFailureTime();
        } else {
            serviceUptime.setPositiveUptime(100);
        }
        serviceUptimes.add(serviceUptime);
    }

    /**
     * Getting Downtime interval gaps and Uptime Information Map out of result set
     *
     * @return Map with a Uptime Information Map and List of interval pairs
     * @throws SQLException
     * @throws ParseException
     */
    public FailureIntervals getDownTimeIntervals(Map<Long, Byte> testUptime)
            throws SQLException, ParseException {
        int failureCount = 0;
        Long finalZeroAt;
        Long newTestRecordedAt;
        List<Long> positiveList = new LinkedList<Long>();
        List<Pair> negativePairs = new ArrayList<Pair>();

        Map<Long, Byte> individualTestUptime = testUptime;
        int uptimeMapSize = individualTestUptime.keySet().size();
        Object firstElement = individualTestUptime.keySet().toArray()[0];
        Object lastElement = individualTestUptime.keySet().toArray()[uptimeMapSize - 1];
        byte currentStatus = individualTestUptime.get(firstElement);
        Long lastStatusRecordedAt = (Long) firstElement;

        if (individualTestUptime.get(firstElement) == 0) {
            finalZeroAt = (Long) firstElement;
        } else {
            finalZeroAt = null;
        }

        for (Map.Entry<Long, Byte> entry : individualTestUptime.entrySet()) {
            newTestRecordedAt = entry.getKey();
            if (entry.getValue() == currentStatus) {
                if (currentStatus == 0) {
                    lastStatusRecordedAt = newTestRecordedAt;
                    failureCount++;
                } else if (currentStatus == 1) {
                    lastStatusRecordedAt = newTestRecordedAt;
                    positiveList.add(newTestRecordedAt);
                }
            } else {
                if (currentStatus == 0) {
                    Pair pair = new Pair(finalZeroAt, newTestRecordedAt);
                    negativePairs.add(pair);
                    lastStatusRecordedAt = newTestRecordedAt;
                    positiveList.add(newTestRecordedAt);
                    currentStatus = 1;
                } else {
                    finalZeroAt = newTestRecordedAt;
                    lastStatusRecordedAt = newTestRecordedAt;
                    currentStatus = 0;
                    failureCount++;
                }
            }
        }

        if (individualTestUptime.get(lastElement) == 0) {
            Pair pair = new Pair(finalZeroAt, (Long) lastElement);
            negativePairs.add(pair);
        }
        if (positiveList.isEmpty() && individualTestUptime.get(firstElement) == 0 &&
            negativePairs.isEmpty()) {
            Pair pair = new Pair((Long) firstElement, (Long) lastElement);
            negativePairs.add(pair);
        }
        FailureIntervals failureIntervals =
                new FailureIntervals(negativePairs, positiveList, failureCount);
        return failureIntervals;
    }

    /**
     * Check availability of records for a particular server
     *
     * @param serverName Name of the server to be checked
     * @return boolean result of availability of records
     * @throws SQLException
     */
    public Boolean hasRecords(String serverName) throws SQLException {
        boolean hasRecord = false;
        ResultSet resultSet;
        dataAccessManager = new DataAccessManager(configObject.getDataSourceFromNode());
        List<String> queryParameters = new ArrayList<String>();
        queryParameters.add(serverName);
        resultSet = dataAccessManager.runQuery(Constants.COUNT_SERVER_RECORDS, queryParameters);
        while (resultSet.next()) {
            hasRecord = resultSet.getInt("count") > 0;
        }
        dataAccessManager.closeConnection();
        return hasRecord;
    }

    /**
     * Getting Test List for a particular server
     *
     * @param serverName name of the server
     * @return String Map of Test Names with severity
     * @throws SQLException
     */
    public List<String> getTestsForServer(String serverName, String severityLevel)
            throws SQLException {
        ResultSet resultSet;
        List<String> queryParameters = new ArrayList<String>();
        List<String> testsForServer = new LinkedList<String>();
        queryParameters.addAll(Arrays.asList(serverName, severityLevel, toDateTime.toString(),
                                             fromDateTime.toString()));
        dataAccessManager = new DataAccessManager(configObject.getDataSourceFromNode());
        resultSet = dataAccessManager.runQuery(Constants.GET_TEST_LIST, queryParameters);
        while (resultSet.next()) {
            testsForServer.add(resultSet.getString(Constants.DB_TEST));
        }
        resultSet.close();
        dataAccessManager.closeConnection();
        return testsForServer;
    }

    /**
     * set time interval for data retrieval
     *
     * @param fromDateTime String for start of time interval (nearest time) default format "yyyy-MM-dd HH:mm:ss"
     * @param toDateTime   String for end of time interval (farthest time) default format "yyyy-MM-dd HH:mm:ss"
     * @throws HeartbeatExceptions
     */
    public void setDateTime(String fromDateTime, String toDateTime) {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date toDate = null;
        Date fromDate = null;
        try {
            toDate = formatter.parse(toDateTime);
            fromDate = formatter.parse(fromDateTime);
        } catch (ParseException e) {

        }
        // Timestamp toTimeStampDate = new Timestamp(toDate.getTime());
        // Timestamp fromTimeStampDate = new Timestamp(fromDate.getTime());

        this.toDateTime = toDate.getTime();
        this.fromDateTime = fromDate.getTime();
    }

    /**
     * Getting testname and failure detail for server name and time stamp
     *
     * @param serverName name of the server
     * @param testName   test Name for the server
     * @return
     * @throws SQLException
     */
    public Map<Timestamp, List<String>> getFailureDetail(String serverName, String testName,
                                                         String toTime, String fromTime)
            throws SQLException {
        Map<Timestamp, List<String>> failuredetail = new TreeMap<Timestamp, List<String>>();
        ResultSet resultSet;
        List<String> queryParameters = new ArrayList<String>();
        dataAccessManager = new DataAccessManager(configObject.getDataSourceFromNode());
        queryParameters.addAll(Arrays.asList(serverName, testName, fromTime, toTime));
        resultSet = dataAccessManager.runQuery(Constants.GET_FAILURE_DETAIL_QUERY, queryParameters);
        while (resultSet.next()) {
            List<String> failureInfo = new ArrayList<String>();
            failureInfo.addAll(Arrays.asList(resultSet.getString("DETAIL"),
                                             resultSet.getString("FAILUREINDEX"),
                                             resultSet.getString("JIRALINK")));
            failuredetail.put(new Timestamp(resultSet.getLong("TIMESTAMP")), failureInfo);
        }
        resultSet.close();
        dataAccessManager.closeConnection();
        return failuredetail;
    }

    /**
     * @param failureIndexes
     * @return
     * @throws SQLException
     */
    public int setFalseToTrue(String failureIndexes) throws SQLException {
        dataAccessManager = new DataAccessManager(configObject.getDataSourceFromNode());
        int resultSet = 0;
        ResultSet indexesForLiveStatus;
        List<String> queryParametersToAlarmStatus = new ArrayList<String>();
        List<String> queryParametersToGetLiveStatus = new ArrayList<String>();

        String multipleUpdateQuery = Constants.SET_ALARM_STATUS_FALSE + failureIndexes + ")";
        dataAccessManager.updateQuery(multipleUpdateQuery, queryParametersToAlarmStatus);

        String getIndexForLiveStatus =
                "SELECT SERVICE, TEST, TIMESTAMP FROM FAILURE_DETAIL WHERE FAILUREINDEX IN (" +
                failureIndexes + ")";
        indexesForLiveStatus =
                dataAccessManager.runQuery(getIndexForLiveStatus, queryParametersToGetLiveStatus);

        while (indexesForLiveStatus.next()) {
            List<String> queryParameters = new ArrayList<String>();
            queryParameters.addAll(Arrays.asList(indexesForLiveStatus.getString("SERVICE"),
                                                 indexesForLiveStatus.getString("TEST"),
                                                 Long.toString(indexesForLiveStatus
                                                                       .getLong("TIMESTAMP"))));
            resultSet =
                    dataAccessManager.updateQuery(Constants.SET_FAILURE_TO_TRUE, queryParameters);
        }

        dataAccessManager.closeConnection();
        return resultSet;
    }

    /**
     * Input the reason to make a failure flag as a false alarm
     *
     * @param failureIndex index of the failure detail
     * @param userId       user Id of the person who is making the change
     * @param changeReason reason to make the the change
     * @return status of reults query
     * @throws SQLException
     */

    public int inputFalseFailureReason(String failureIndex, String userId, String changeReason)
            throws SQLException {
        dataAccessManager = new DataAccessManager(configObject.getDataSourceFromNode());
        int resultSet = 0;

        String[] indexesToAdd = failureIndex.split(",");
        Date date = new Date();
        Timestamp timeStamp = new Timestamp(date.getTime());

        for (int count = 0; count < indexesToAdd.length; count++) {
            List<String> queryParameters = new ArrayList<String>();
            queryParameters.addAll(Arrays.asList(indexesToAdd[count], timeStamp.toString(), userId,
                                                 changeReason));
            resultSet = dataAccessManager
                    .updateQuery(Constants.FALSE_FAILURE_REASON_INPUT_QUERY, queryParameters);
        }

        dataAccessManager.closeConnection();
        return resultSet;
    }

    /**
     * @param jiraUrl
     * @param failureIdList
     * @return
     * @throws SQLException
     */
    public int setJiraLink(String jiraUrl, String failureIdList) throws SQLException {
        dataAccessManager = new DataAccessManager(configObject.getDataSourceFromNode());
        int resultSet;
        List<String> queryParameters = new ArrayList<String>();
        String multipleUpdateQuery = Constants.UPDATE_JIRA_URL + failureIdList + ")";
        queryParameters.addAll(Arrays.asList(jiraUrl));
        resultSet = dataAccessManager.updateQuery(multipleUpdateQuery, queryParameters);
        dataAccessManager.closeConnection();
        return resultSet;
    }

}


