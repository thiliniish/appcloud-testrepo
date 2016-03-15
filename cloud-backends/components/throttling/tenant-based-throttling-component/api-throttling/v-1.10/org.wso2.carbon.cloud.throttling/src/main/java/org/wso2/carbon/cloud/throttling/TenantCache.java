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
package org.wso2.carbon.cloud.throttling;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.throttling.common.RatePlanDTO;
import org.wso2.carbon.cloud.throttling.tasks.throttleout.ThrottleOutDTO;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This class caches the tenant rate plan info.
 */
public class TenantCache {

    private static final Log LOG = LogFactory.getLog(TenantCache.class);

    /**
     * keeps track of subscribers' rate plans
     */
    private static final ConcurrentMap<String, RatePlanDTO> userSubscriptionMapping = new ConcurrentHashMap<String, RatePlanDTO>();
    /*
    * keeps throttleOut tenant info to be written to db
    * */
    private static ConcurrentMap<String, ConcurrentMap<String, ThrottleOutDTO>> throttleOutTenantsMap = new ConcurrentHashMap<String, ConcurrentMap<String, ThrottleOutDTO>>();

    /*
    * Removes an element from Map
    *
    * @param tenantDomain
    */
    public static void removeFromCache(String tenantDomain) {
        if (userSubscriptionMapping.containsKey(tenantDomain)) {
            userSubscriptionMapping.remove(tenantDomain);
        }
    }

    /*
    * update tenants map with the ratePlan info
    *
    * @param tenantDomain tenant domain
    * @param ratePlan subscribed rate plan
    */
    public static synchronized void updateCache(final String tenantDomain, String ratePlan) {
        if (!userSubscriptionMapping.containsKey(tenantDomain)) {
            RatePlanDTO ratePlanDTO = new RatePlanDTO(ratePlan, System.currentTimeMillis());
            userSubscriptionMapping.put(tenantDomain, ratePlanDTO);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Tenant domain is not found in the cache. Hence added. " + tenantDomain + " : " + ratePlan);
            }
        } else {
            RatePlanDTO ratePlanDTO = userSubscriptionMapping.get(tenantDomain);
            ratePlanDTO.setRetaPlan(ratePlan);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Tenant domain is found in the cache. Hence updated. " + tenantDomain + " : " + ratePlan);
            }
        }
    }

    /*
    * @return Iterator object of the tenant vs rate plan mapping
    */
    public static Iterator<Map.Entry<String, RatePlanDTO>> getUserSubscriptionMapping() {
        return userSubscriptionMapping.entrySet().iterator();
    }

    /*
    * @param tenantDomain
    * @return rate plan
    */
    public static RatePlanDTO getRatePlan(String tenantDomain) {
        return userSubscriptionMapping.get(tenantDomain);
    }

    /*
    * @return True if tenantDomain is cached in the MAp
    */
    public static boolean isCached(String tenantDomain) {
        return userSubscriptionMapping.containsKey(tenantDomain);
    }

    /*
* Adds throttleOut tenant info to cache
* @param ThrottleOutDTO
*
* */
    public static void addToThrottleOutTenantsCache(ThrottleOutDTO throttleOutDTO) {
        ConcurrentMap<String, ThrottleOutDTO> throttleOutAPIs = null;
        if (throttleOutTenantsMap.containsKey(throttleOutDTO.getTenantDomain())) {
            throttleOutAPIs = throttleOutTenantsMap.get(throttleOutDTO.getTenantDomain());
            if (throttleOutAPIs.containsKey(throttleOutDTO.getApi())) {
                throttleOutDTO = throttleOutAPIs.get(throttleOutDTO.getApi());
                throttleOutDTO.increaseAccessCount();
                throttleOutDTO.setLastAccessTime(System.currentTimeMillis());
            } else {
                throttleOutTenantsMap.get(throttleOutDTO.getTenantDomain()).put(throttleOutDTO.getApi(), throttleOutDTO);
            }

        } else {
            throttleOutAPIs = new ConcurrentHashMap<String, ThrottleOutDTO>();
            throttleOutAPIs.put(throttleOutDTO.getApi(), throttleOutDTO);
            throttleOutTenantsMap.put(throttleOutDTO.getTenantDomain(), throttleOutAPIs);
        }
    }

    public static Iterator<Map.Entry<String, ConcurrentMap<String, ThrottleOutDTO>>> getThrottleOutTenantsMap() {
        return throttleOutTenantsMap.entrySet().iterator();
    }
}
