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

package org.wso2.carbon.cloud.userstore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;

/**
 * Cache for user email
 */
public class CloudUserEmailCache {
    private static final Log LOGGER = LogFactory.getLog(CloudUserEmailCache.class);

    public static final String USER_EMAIL_CACHE = "CLOUD_USER_EMAIL_CACHE";
    public static final String USER_EMAIL_CACHE_MANAGER = "CLOUD_USER_EMAIL_CACHE_MANAGER";

    private static CloudUserEmailCache cloudUserEmailCache = new CloudUserEmailCache();

    private CloudUserEmailCache() {
    }

    public static CloudUserEmailCache getInstance() {
        return cloudUserEmailCache;
    }

    private Cache<String, String> getUserEmailCache() {
        CacheManager cacheManager = Caching.getCacheManagerFactory().getCacheManager(USER_EMAIL_CACHE_MANAGER);
        return cacheManager.getCache(USER_EMAIL_CACHE);
    }

    /**
     * Avoiding NullPointerException when the cache is null
     *
     * @return boolean whether given cache is null
     */
    private boolean isCacheNull(Cache<String, String> cache) {
        if (cache == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("USER_EMAIL_CACHE doesn't exist in CacheManager : " + USER_EMAIL_CACHE_MANAGER);
            }
            return true;
        }
        return false;
    }

    /**
     * Adds an entry to the cache.
     *
     * @param userName Name of the user
     * @param email    Email of the user
     */
    protected void addToCache(String userName, String email) {
        Cache<String, String> cache = this.getUserEmailCache();
        //check for null
        if (isCacheNull(cache)) {
            return;
        }
        cache.put(userName, email);
    }

    protected String getEmail(String userName) {
        Cache<String, String> cache = this.getUserEmailCache();
        //check for null
        if (isCacheNull(cache)) {
            return null;
        }
        return cache.get(userName);
    }

    // clear full cache
    public void clearCache() {
        Cache<String, String> cache = this.getUserEmailCache();
        // check for null
        if (isCacheNull(cache)) {
            return;
        }
        cache.removeAll();
    }

    // Clear by user name
    public void clearCacheEntry(String userName) {
        Cache<String, String> cache = this.getUserEmailCache();
        // Check for null
        if (isCacheNull(cache)) {
            return;
        }
        if (cache.containsKey(userName)) {
            cache.remove(userName);
        }
    }

}
