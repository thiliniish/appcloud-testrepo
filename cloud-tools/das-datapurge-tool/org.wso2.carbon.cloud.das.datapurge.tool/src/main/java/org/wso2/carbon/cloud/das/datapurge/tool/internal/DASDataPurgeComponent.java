/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.cloud.das.datapurge.tool.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.core.ServerStartupHandler;

/**
 * Represents the OSGI service component used to expose DAS data purge tool
 *
 * @scr.component name="org.wso2.carbon.cloud.das.datapurge.tool"
 * @scr.reference name="analytics.component" interface="org.wso2.carbon.analytics.api.AnalyticsDataAPI"
 * cardinality="1..1" policy="dynamic" bind="setAnalyticsDataAPI" unbind="unsetAnalyticsDataAPI"
 */
public class DASDataPurgeComponent {
    private static Log log = LogFactory.getLog(DASDataPurgeComponent.class);

    /**
     * Method to activate bundle.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {
        try {
            //Register the server start up handler which hold the execution of its invoke method until the server starts
            context.getBundleContext()
                    .registerService(ServerStartupHandler.class.getName(), new DASPurgeToolServerStartListener(), null);
            if (log.isDebugEnabled()) {
                log.debug("DAS data purge tool is activated");
            }
        } catch (Throwable e) {
            log.error("Error while activating DAS data purge tool.", e);
        }
    }

    /**
     * Method to deactivate bundle.
     *
     * @param context
     */
    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("DAS data purge tool is deactivated ");
        }
    }

    /**
     * Method to unset AnayticsDataAPI
     *
     * @param analyticsDataAPI
     */
    protected void unsetAnalyticsDataAPI(AnalyticsDataAPI analyticsDataAPI) {
        ServiceHolder.setAnalyticsDataAPI(null);
    }

    /**
     * Method to set AnayticsDataAPI
     *
     * @param analyticsDataAPI
     */
    protected void setAnalyticsDataAPI(AnalyticsDataAPI analyticsDataAPI) {
        ServiceHolder.setAnalyticsDataAPI(analyticsDataAPI);
    }

}
