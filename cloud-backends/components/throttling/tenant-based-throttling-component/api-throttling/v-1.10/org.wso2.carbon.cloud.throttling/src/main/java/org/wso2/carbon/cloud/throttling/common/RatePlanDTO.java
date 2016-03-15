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
package org.wso2.carbon.cloud.throttling.common;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Bean class for rate Plan info.
 */
public class RatePlanDTO {
    private String ratePlan;
    private AtomicLong lastAccessTime;

    private RatePlanDTO() {
    }

    /*
    * Initialises the instance
    * @param String rate plan
    * @param long last access time in milli seconds
    * */
    public RatePlanDTO(String ratePlan, long lastAccessTime) {
        this.ratePlan = ratePlan;
        this.lastAccessTime = new AtomicLong(lastAccessTime);
    }

    /*
    * @return ratePlan : String
    * */
    public String getRatePlan() {
        return ratePlan;
    }

    public void setRetaPlan(String retaPlan) {
        this.ratePlan = retaPlan;
    }

    public long getLastAccessTime() {
        return lastAccessTime.get();
    }

    public void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime.addAndGet(lastAccessTime);
    }

    /*
    * Calculates whether the current instance is idle for 1 hour
    * @param long last access current time stamp
    * @return isIdle :boolean
    * */
    public boolean isTenantIdle(long timestamp) {
        boolean isIdle = false;
        if (timestamp > 0) {
            isIdle = (timestamp >= (System.currentTimeMillis() - (Constants.CACHE_TIME_TO_LIVE)));
        }
        return isIdle;
    }

    @Override
    public int hashCode() {
        return this.ratePlan.hashCode() + new Random().nextInt(1000);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RatePlanDTO)) {
            return false;
        }
        RatePlanDTO ratePlanDTO = (RatePlanDTO) obj;
        return this.ratePlan.equalsIgnoreCase(ratePlanDTO.ratePlan);
    }
}
