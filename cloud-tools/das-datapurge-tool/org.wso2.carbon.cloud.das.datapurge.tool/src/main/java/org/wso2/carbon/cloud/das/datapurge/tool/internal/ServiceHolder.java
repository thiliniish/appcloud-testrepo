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

import org.wso2.carbon.analytics.api.AnalyticsDataAPI;

/**
 * Represents the data holder for the DAS data purge tool
 */
public class ServiceHolder {

    /**
     * Avoids instantiation of Service Holder
     */
    private ServiceHolder() {
    }

    //Analytics data API which is used to retrieve and purge data from DAS
    private static AnalyticsDataAPI analyticsDataAPI;

    /**
     * Method to get AnalayticsDataAPI
     *
     * @return AnalayticsDataAPI
     */
    public static AnalyticsDataAPI getAnalyticsDataAPI() {
        return analyticsDataAPI;
    }

    /**
     * Method to set AnalayticsDataAPI
     *
     * @param analyticsDataAPI
     */
    public static void setAnalyticsDataAPI(AnalyticsDataAPI analyticsDataAPI) {
        ServiceHolder.analyticsDataAPI = analyticsDataAPI;
    }

}
