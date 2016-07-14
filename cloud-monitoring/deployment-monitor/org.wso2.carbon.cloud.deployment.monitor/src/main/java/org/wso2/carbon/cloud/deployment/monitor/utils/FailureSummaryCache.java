/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.cloud.deployment.monitor.utils;

import org.wso2.carbon.cloud.deployment.monitor.utils.dto.FailureSummary;

import java.util.Hashtable;
import java.util.Map;

/**
 * A caching implementation based on a Map for {@link FailureSummary}
 */
public class FailureSummaryCache {

    private Map<String, FailureSummary> failureSummaryMap;

    private static volatile FailureSummaryCache failureSummaryCache = null;

    private FailureSummaryCache() {
        failureSummaryMap = new Hashtable<>();
    }

    private static void initialize() {
        synchronized (FailureSummaryCache.class) {
            if (failureSummaryCache == null) {
                failureSummaryCache = new FailureSummaryCache();
            }
        }
    }

    public static FailureSummaryCache getInstance() {
        if (failureSummaryCache == null) {
            initialize();
        }
        return failureSummaryCache;
    }

    /**
     * Adds an entry to the cache.
     *
     * @param key            {@link String} key - serverGroupName:taskName
     * @param failureSummary {@link FailureSummary}
     */
    public void addToCache(String key, FailureSummary failureSummary) {
        failureSummaryMap.put(key, failureSummary);
    }

    /**
     * Returns an entry matching the {@link String} key
     *
     * @param key {@link String}
     * @return {@link FailureSummary}
     */
    public FailureSummary getCacheEntry(String key) {
        if (failureSummaryMap.containsKey(key)) {
            return failureSummaryMap.get(key);
        }
        return null;
    }

    /**
     * Removes an entry matching the {@link String} key
     *
     * @param key {@link String}
     */
    public void clearCacheEntry(String key) {
        failureSummaryMap.remove(key);
    }

}
