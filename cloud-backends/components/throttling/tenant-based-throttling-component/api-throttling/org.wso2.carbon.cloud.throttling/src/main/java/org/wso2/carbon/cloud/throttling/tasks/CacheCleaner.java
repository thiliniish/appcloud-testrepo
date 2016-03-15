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
 * under the License
 */
package org.wso2.carbon.cloud.throttling.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.throttling.CloudMgtDAO;
import org.wso2.carbon.cloud.throttling.TenantCache;
import org.wso2.carbon.cloud.throttling.common.CloudThrottlingException;
import org.wso2.carbon.cloud.throttling.common.Constants;
import org.wso2.carbon.cloud.throttling.common.RatePlanDTO;
import org.wso2.carbon.cloud.throttling.internal.ThrottleDataHolder;

import java.util.Iterator;
import java.util.Map;
import java.util.TimerTask;

/**
 * This class is responsible for cleaning-up tenant cache
 * Tenants who are not accessed for <timeToLive> will be cleaned.
 */
public class CacheCleaner extends TimerTask {

    private static final Log LOG = LogFactory.getLog(CacheCleaner.class);

    public CacheCleaner() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Tenant Throttling - cache Cleaner Task is started.");
        }
    }


    public void run() {
        final long timeToLive = Constants.CACHE_TIME_TO_LIVE;
        Iterator<Map.Entry<String, RatePlanDTO>> itr = TenantCache.getUserSubscriptionMapping();
        while (itr.hasNext()) {
            Map.Entry<String, RatePlanDTO> mapEntry = itr.next();
            String tenantDomain = mapEntry.getKey();
            RatePlanDTO ratePlanDTO = mapEntry.getValue();
            if (ratePlanDTO != null && (System.currentTimeMillis() > (ratePlanDTO.getLastAccessTime() + timeToLive))) {
                //remove tenants that is not active for more that 1hour from the cache
                TenantCache.removeFromCache(tenantDomain);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Tenant '" + tenantDomain + "' is idle for one hour. Hence removing from the cache. ");
                }
            } else {
                //If the rate plan is changed then update to the current rate plan
                String productRatePlanId = getProductRatePlanId(tenantDomain);
                RatePlanDTO cachedRatePlan = TenantCache.getRatePlan(tenantDomain);
                if (!cachedRatePlan.getRatePlan().equals(productRatePlanId)) {
                    TenantCache.updateCache(tenantDomain, productRatePlanId);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Tenant '" + tenantDomain + "' has changed the rate plan from '" + cachedRatePlan.getRatePlan() + "' to '" + productRatePlanId + "'. Hence updating the cache.");
                    }
                }
            }
        }
    }

    /*
    * Use to get the product rate plan of a tenant
    * @param String tenantDomain
    * @return String productRatePlanId
    * */
    private String getProductRatePlanId(String tenantDomain) {
        // look for type in cloud mgt db
        CloudMgtDAO cloudMgtDAO = new CloudMgtDAO();
        String productRatePlanId = Constants.RATE_PLAN_DEFAULT;
        try {
            String type = cloudMgtDAO.getSubscriptionType(tenantDomain, ThrottleDataHolder.getCloudType());
            if (type != null && type.equalsIgnoreCase(ThrottleDataHolder.SubscriptionType.PAID.name())) {
                // if the type is PAID, get the product rate plan id from the cloudmgt db
                String accountNumber = cloudMgtDAO.getAccountNumber(tenantDomain);
                productRatePlanId = cloudMgtDAO.getProductRatePlanId(accountNumber, ThrottleDataHolder.getCloudType());
            }

        } catch (CloudThrottlingException e) {
            LOG.error("Exception while requesting data from cloudmgt db.", e);
        }
        return productRatePlanId;
    }

}
