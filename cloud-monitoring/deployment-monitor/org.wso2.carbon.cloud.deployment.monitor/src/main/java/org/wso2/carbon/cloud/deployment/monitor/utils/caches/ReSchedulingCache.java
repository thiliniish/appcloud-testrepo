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

package org.wso2.carbon.cloud.deployment.monitor.utils.caches;

import java.util.Hashtable;
import java.util.Map;

/**
 * A caching implementation based on a Map for Re-Scheduling Tasks
 */
public class ReSchedulingCache {

    private Map<String, Boolean> reSchedulingMap;

    private static volatile ReSchedulingCache reSchedulingCache = null;

    private ReSchedulingCache() {
        reSchedulingMap = new Hashtable<>();
    }

    private static void initialize() {
        synchronized (ReSchedulingCache.class) {
            if (reSchedulingCache == null) {
                reSchedulingCache = new ReSchedulingCache();
            }
        }
    }

    public static ReSchedulingCache getInstance() {
        if (reSchedulingCache == null) {
            initialize();
        }
        return reSchedulingCache;
    }

    /**
     * Adds an entry to the cache.
     *
     * @param key         {@link String} key - serverGroupName:taskName
     * @param reScheduled {@link Boolean} whether rescheduled
     */
    public void addToCache(String key, boolean reScheduled) {
        reSchedulingMap.put(key, reScheduled);
    }

    /**
     * Returns an entry matching the {@link String} key
     *
     * @param key {@link String}
     * @return {@link Boolean}
     */
    public boolean getCacheEntry(String key) {
        if (reSchedulingMap.containsKey(key)) {
            return reSchedulingMap.get(key);
        }
        return false;
    }

    /**
     * Removes an entry matching the {@link String} key
     *
     * @param key {@link String}
     */
    public void clearCacheEntry(String key) {
        reSchedulingMap.remove(key);
    }

}
