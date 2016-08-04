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

package org.wso2.carbon.cloud.tenantdeletion.listeners;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.cloud.tenantdeletion.constants.DeletionConstants;
import org.wso2.carbon.cloud.tenantdeletion.utils.DataAccessManager;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Represents class for Tenant Loading Listener.
 */
public class TenantLoaderObserver implements Axis2ConfigurationContextObserver {

    /**
     * Records data to the database when tenant loading occurs.
     * Before adding the record to database it checks whether the tenant is a paid account holder
     *
     * @param context Configuration context
     */
    @Override public void createdConfigurationContext(ConfigurationContext context) {
        DataAccessManager dbAccessor = new DataAccessManager();
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat date = new SimpleDateFormat(DeletionConstants.DATE_FORMAT);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);

        dbAccessor.insertCloudOperationInfo(tenantId, tenantDomain, date.format(currentTime));
    }

    @Override public void creatingConfigurationContext(int context) {
        //method from interface
    }

    @Override public void terminatedConfigurationContext(ConfigurationContext context) {
        //method from interface
    }

    @Override public void terminatingConfigurationContext(ConfigurationContext context) {
        //method from interface
    }
}