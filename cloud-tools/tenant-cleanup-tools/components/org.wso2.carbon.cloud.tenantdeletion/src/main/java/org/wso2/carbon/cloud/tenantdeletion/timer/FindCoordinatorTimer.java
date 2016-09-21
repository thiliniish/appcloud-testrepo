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

package org.wso2.carbon.cloud.tenantdeletion.timer;

import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.tenantdeletion.DeletionManager;
import org.wso2.carbon.cloud.tenantdeletion.constants.DeletionConstants;
import org.wso2.carbon.cloud.tenantdeletion.internal.ServiceHolder;
import org.wso2.carbon.cloud.tenantdeletion.utils.DataAccessManager;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.TimerTask;

/**
 * Represent Timer class to find coordinator in conf clustered environment
 */
public class FindCoordinatorTimer extends TimerTask {
    private static final Log LOG = LogFactory.getLog(FindCoordinatorTimer.class);

    /**
     * Checks coordinator to start deletion process
     */
    @Override public void run() {
        ConfigurationContextService contextService = ServiceHolder.getInstance().getConfigurationContextService();
        AxisConfiguration serverAxisConfig = contextService.getServerConfigContext().getAxisConfiguration();
        ClusteringAgent agent = serverAxisConfig.getClusteringAgent();
        //Clustering agent will be null if the server is not clustered
        if (agent != null) {
            boolean isCoordinator = agent.isCoordinator();
            if (isCoordinator) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Server got elected as the coordinator");
                }
                DeletionManager.getInstance().startDeletion();
            } else {
                //If the server is not the coordinator, then the server looks at the tenant deletion start flag. If the
                // flag is lowered, the server gets back to the first stage, which is starting conf timer to check the
                // deletion flag again to identify conf new tenant deletion round
                boolean isDeletionFinished = DataAccessManager.getInstance().getDeletionStatus(DeletionConstants.START);
                if (!isDeletionFinished) {
                    DeletionManager.getInstance().resetStartFlagTimer();
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Server is not the coordinator");
                }
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Clustering is disabled hence not starting the deletion on this node");
            }
            DeletionManager.getInstance().startDeletion();
        }
    }
}
