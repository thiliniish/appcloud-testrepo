package org.wso2.cloud.heartbeat.monitor.modules.bam;

/*
*  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.RangeSlicesQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.exception.*;
import org.wso2.cloud.heartbeat.monitor.utils.ModuleUtils;
import org.wso2.cloud.heartbeat.monitor.utils.PlatformUtils;
import org.wso2.cloud.heartbeat.monitor.utils.TestInfo;
import org.wso2.cloud.heartbeat.monitor.utils.TestStateHandler;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DataPublishingTest implements Job {

    private static final Log log = LogFactory.getLog(DataPublishingTest.class);
    private final String TEST_NAME = "DataPublishingTest";
    private final int row_count = 100;
    private TestInfo testInfo;
    private TestStateHandler testStateHandler;
    private String severity;


    private String hostName;
    private int requestCount = 0;
    private String serviceName;
    private String adminUsername;
    private String adminPassword;
    private String cassandraRing;
    private String thriftHostWithPort;
    private String[] hostArray;
    private String[] cassandraHostArray;


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        log.info("Executing the DataPublishingTest");
        init();
        hostArray = ModuleUtils.getHostArray(thriftHostWithPort);
        cassandraHostArray = ModuleUtils.getHostArray(cassandraRing);

        for (int i = 0; i < hostArray.length; i++) {
            testSendingEvent(hostArray[i].trim(), cassandraHostArray[i].trim());
        }
    }

    public void init() {
        testInfo = new TestInfo(serviceName, TEST_NAME, ModuleUtils.hostWithoutPort(hostName), severity);
        testStateHandler = TestStateHandler.getInstance();
    }

    public void testSendingEvent(String thriftHost, String cassandraHost) {

        try {
            PlatformUtils.setTrustStoreParams();
            Thread.sleep(2000);

            //according to the convention the authentication port will be 7611+100= 7711 and its host will be the same
            DataPublisher dataPublisher = new DataPublisher("tcp://" + thriftHost, adminUsername, adminPassword);
            String streamId = dataPublisher.defineStream("{" +
                    "  'name':'org.wso2.cloud.hbmonitor'," +
                    "  'version':'1.0.0'," +
                    "  'nickName': 'Stock Quote Information'," +
                    "  'description': 'This is to test the data publishing functionality of the BAM'," +
                    "  'tags':['foo', 'bar']," +
                    "  'metaData':[" +
                    "          {'name':'ipAdd','type':'STRING'}" +
                    "  ]," +
                    "  'payloadData':[" +
                    "          {'name':'timestamp','type':'LONG'}," +
                    "          {'name':'symbol','type':'STRING'}," +
                    "          {'name':'price','type':'DOUBLE'}," +
                    "          {'name':'volume','type':'INT'}," +
                    "          {'name':'max','type':'DOUBLE'}," +
                    "          {'name':'min','type':'Double'}" +
                    "  ]" +
                    "}");
            log.info("Stream defined: " + streamId);

            if (!streamId.contains("org.wso2.cloud.hbmonitor")) {
                testStateHandler.onFailure(testInfo, "Stream not created");
            }

            //In this case correlation data is null
            Long timestamp = System.currentTimeMillis();
            dataPublisher.publish(streamId, new Object[]{ModuleUtils.hostWithoutPort(thriftHost)}, null,
                    new Object[]{timestamp, "IBM", 102.8, 1000, 120.6, 70.4});
            log.info("Event published to stream");

            Thread.sleep(3000);
            dataPublisher.stop();
            log.info("Data Publisher stopped");

            Thread.sleep(10000);

            if (verifyData(timestamp, cassandraHost)) {
                log.info("Published data verified on " + cassandraHost);
                testStateHandler.onSuccess(testInfo);
            } else {
                testStateHandler.onFailure(testInfo, "Data verification failed on " + cassandraHost);
            }

        } catch (InterruptedException e) {
            countNoOfRequests(testInfo, "Thread sleep interrupted", e);
        } catch (AuthenticationException e) {
            countNoOfRequests(testInfo, "Failed while Authenticating", e);
        } catch (StreamDefinitionException e) {
            countNoOfRequests(testInfo, "Error in stream definition phase", e);
        } catch (AgentException e) {
            countNoOfRequests(testInfo, "Error while connecting to service", e);
        } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
            countNoOfRequests(testInfo, "Different stream definition already exists", e);
        } catch (TransportException e) {
            countNoOfRequests(testInfo, "Error in transport", e);
        } catch (MalformedURLException e) {
            countNoOfRequests(testInfo, "Invalid URL definition", e);
        } catch (MalformedStreamDefinitionException e) {
            countNoOfRequests(testInfo, "valid Stream definition", e);
        }
    }

    /**
     * This method verifies whether the published data actually resides in the cassandra DB.
     *
     * @param timestamp timestamp published
     * @return Sucess State
     */
    public boolean verifyData(long timestamp, String cassandraHost) {
        log.info("Verifying published data on " + cassandraHost);
        Map<String, String> credentials = new HashMap<String, String>();
        credentials.put("username", adminUsername);
        credentials.put("password", adminPassword);

        try {
            CassandraHostConfigurator configurator = new CassandraHostConfigurator(ModuleUtils.hostWithoutPort(cassandraHost));
            configurator.setPort(Integer.parseInt(ModuleUtils.portWithoutHost(cassandraHost)));
            Cluster hectorCluster = HFactory.getOrCreateCluster("cluster-1", configurator, credentials);
            Keyspace keyspace = HFactory.createKeyspace("EVENT_KS", hectorCluster);

            RangeSlicesQuery<String, String, Long> rangeSlicesQuery = HFactory
                    .createRangeSlicesQuery(keyspace, StringSerializer.get(), StringSerializer.get(), LongSerializer.get())
                    .setColumnFamily("org_wso2_cloud_hbmonitor")
                    .setRowCount(row_count)
                    .setColumnNames("payload_timestamp");

            String last_key = null;
            while (true) {
                rangeSlicesQuery.setKeys(last_key, null);

                QueryResult<OrderedRows<String, String, Long>> result = rangeSlicesQuery.execute();
                OrderedRows<String, String, Long> rows = result.get();
                Iterator<Row<String, String, Long>> rowsIterator = rows.iterator();

                // we'll skip this first one, since it is the same as the last one from previous time we executed
                if (last_key != null && rowsIterator != null) rowsIterator.next();

                while (rowsIterator.hasNext()) {
                    Row<String, String, Long> row = rowsIterator.next();
                    last_key = row.getKey();
                    if (row.getColumnSlice().getColumns().isEmpty()) {
                        continue;
                    }
                    if (row.getColumnSlice().getColumnByName("payload_timestamp").getValue() == timestamp) {
                        //hectorCluster.getConnectionManager().shutdown();
                        return true;
                    }
                }

                if (rows.getCount() < row_count)
                    break;

            }
        } catch (Exception e) {
            countNoOfRequests(testInfo, "Data verifying failed", e);
        }
        //hectorCluster.getConnectionManager().shutdown();
        return false;
    }

    /**
     * This method retry the request for 3 times on a failure.
     *
     * @param testInfo TestInformation.
     * @param logMsg   Log message.
     */
    private void countNoOfRequests(TestInfo testInfo, String logMsg, Exception e) {
        requestCount++;
        if (requestCount == 3) {
            testStateHandler.onFailure(testInfo, logMsg, e);
            requestCount = 0;
        } else {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                //Exception ignored
            }
            for (int i = 0; i < hostArray.length; i++) {
                testSendingEvent(hostArray[i], cassandraHostArray[i]);
            }
        }
    }

    /**
     * Sets thrift port
     *
     * @param thriftHostWithPort Thrift Host with Port
     */
    public void setThriftHostWithPort(String thriftHostWithPort) {
        this.thriftHostWithPort = thriftHostWithPort;
    }

    /**
     * Sets Service name
     *
     * @param serviceName Service name
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Sets service host
     *
     * @param hostName Service host
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * Sets BAM Admin user name
     *
     * @param adminUsername admin user name
     */

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    /**
     * Sets BAM Admin user password
     *
     * @param adminPassword admin user password
     */
    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    /**
     * Sets BAM Cassandra ring
     *
     * @param cassandraRing ring of cassandra nodes
     */
    public void setCassandraRing(String cassandraRing) {
        this.cassandraRing = cassandraRing;
    }

    /**
     * Sets severity value
     * @param severity severity
     */
    public void setSeverity(String severity) {
        this.severity = severity;
    }

}

