/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.cloudmgt.bam.integration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloudmgt.common.CloudConstants;
import org.wso2.carbon.cloudmgt.common.CloudMgtException;
import org.wso2.carbon.cloudmgt.users.service.UserManagementException;
import org.wso2.carbon.cloudmgt.users.util.AppFactoryConfiguration;
import org.wso2.carbon.cloudmgt.users.util.UserMgtUtil;
import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;

/**
 * Data Publishing class to BAM from Cloud management
 */
public class BamDataPublisher {

    private static Log log = LogFactory.getLog(BamDataPublisher.class);




    private boolean enableDataPublishing;
    private AsyncDataPublisher asyncDataPublisher;
    private String tenantUserStream =
            "{" + " 'name': '" + CloudConstants.TENANT_USER_STREAM + "'," + " 'version': '" +
            CloudConstants.TENANT_USER_STREAM_VERSION + "'," +
            " 'nickName': 'Tenant User Information'," +
            " 'description': 'This stream will store tenant users to BAM'," + "   'payloadData':[" +
            "    {'name':'tenantId', 'type':'string'}," + "    {'name':'user',  'type':'string' }," +
            "    {'name':'action', 'type':'string'}," + "    {'name':'timeStamp','type':'double'}" + "    ]" + "    }";
    private String appUserStream =
            "{" + " 'name': '" + CloudConstants.APP_USER_STREAM + "'," + " 'version': '" +
            CloudConstants.APP_USER_STREAM_VERSION +
            "'," +
            " 'nickName': 'Application User Information'," +
            " 'description': 'This stream will store app users to BAM'," + "   'payloadData':[" +
            "    {'name':'applicationName','type':'string'}," + "    {'name':'applicationKey','type':'string'}," +
            "    {'name':'timeStamp','type':'double'}," + "    {'name':'tenantId', 'type':'string'}," +
            "    {'name':'action', 'type':'string'}," + "    {'name':'user',  'type':'string' }" + "    ]" + "    }";

    public BamDataPublisher() {
        try {
            AppFactoryConfiguration config;

            config = UserMgtUtil.getConfiguration();

            String enableStatPublishing = config.getFirstProperty("BAM.EnableStatPublishing");
            String bamServerURLS = config.getFirstProperty("BAM.BAMServerURL");

            // Check for multiple URLS separated by ","
            String bamServerURL[] = null;
            if (bamServerURLS != null) {
                bamServerURL = bamServerURLS.split(",");
            }

            String bamServerUserName = config.getFirstProperty("BAM.BAMUserName");
            String bamServerPassword = config.getFirstProperty("BAM.BAMPassword");

            if (enableStatPublishing != null && enableStatPublishing.equals("true")) {
                enableDataPublishing = true;

                if (bamServerURL == null || bamServerURLS.length() <= 0) {
                    throw new CloudMgtException("Can not find BAM Server URL");
                } else {
                    for (String url : bamServerURL) {
                        asyncDataPublisher = new AsyncDataPublisher(url, bamServerUserName, bamServerPassword);

                    }
                }

            }

        } catch (CloudMgtException e) {
            String errorMsg = "Unable to create Data publisher " + e.getMessage();
            log.error(errorMsg, e);
        } catch (UserManagementException e) {
            String errorMsg = "Unable to create Data publisher " + e.getMessage();
            log.error(errorMsg, e);
        }
    }

    public void publishTenantUserUpdateEvent(String tenantId, String username, String action, double timestamp)
            throws CloudMgtException {

        if (!enableDataPublishing) {
            return;
        }

        Event event = new Event();
        if (!asyncDataPublisher.isStreamDefinitionAdded(CloudConstants.TENANT_USER_STREAM,
                                                        CloudConstants.TENANT_USER_STREAM_VERSION)) {

            asyncDataPublisher.addStreamDefinition(tenantUserStream, CloudConstants.TENANT_USER_STREAM,
                                                   CloudConstants.TENANT_USER_STREAM_VERSION);
        }

        event.setTimeStamp(System.currentTimeMillis());
        event.setMetaData(null);
        event.setCorrelationData(null);
        event.setPayloadData(new Object[] { tenantId, username, action, timestamp });

        try {

            publishEvents(event, CloudConstants.TENANT_USER_STREAM, CloudConstants.TENANT_USER_STREAM_VERSION);

        } catch (AgentException e) {
            String msg = "Failed to publish tenant user update event";
            log.error(msg, e);
            throw new CloudMgtException(msg, e);
        } catch (InterruptedException e) {
            String msg = "Failed to publish tenant user update event";
            log.error(msg, e);
            throw new CloudMgtException(msg, e);
        }

    }

    public void publishUserUpdateEvent(String appName, String appKey, double timestamp, String tenantId, String action,
                                       String username) throws CloudMgtException {

        if (!enableDataPublishing) {
            return;
        }

        Event event = new Event();
        if (!asyncDataPublisher.isStreamDefinitionAdded(CloudConstants.APP_USER_STREAM, CloudConstants
                .APP_USER_STREAM_VERSION)) {

            asyncDataPublisher.addStreamDefinition(appUserStream, CloudConstants.APP_USER_STREAM,
                                                   CloudConstants.APP_USER_STREAM_VERSION);
        }

        event.setTimeStamp(System.currentTimeMillis());
        event.setMetaData(null);
        event.setCorrelationData(null);
        event.setPayloadData(new Object[] { appName, appKey, timestamp, tenantId, username, action });

        try {

            publishEvents(event, CloudConstants.APP_USER_STREAM, CloudConstants.APP_USER_STREAM_VERSION);

        } catch (AgentException e) {
            String msg = "Failed to publish app user update event";
            log.error(msg, e);
            throw new CloudMgtException(msg, e);
        } catch (InterruptedException e) {
            String msg = "Failed to publish app user update event";
            log.error(msg, e);
            throw new CloudMgtException(msg, e);
        }

    }

    public void publishEvents(Event event, String stream, String version) throws AgentException, InterruptedException {

        asyncDataPublisher.publish(stream, version, event);
        asyncDataPublisher.stop();

    }

}
