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

package org.wso2.carbon.cloud.throttling.tasks.throttleout;

/**
 * This is the bean class for throttled out info.
 */
public class ThrottleOutDTO {
    private String tenantDomain;
    private String api;
    private int accessCount = 1;
    private long lastAccessTime;

    public ThrottleOutDTO(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public String getApi() {
        return api;
    }

    public ThrottleOutDTO setApi(String api) {
        this.api = api;
        return this;
    }

    public int getAccessCount() {
        return accessCount;
    }

    public ThrottleOutDTO increaseAccessCount() {
        this.accessCount = this.accessCount + 1;
        return this;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    @Override
    public int hashCode() {
        return tenantDomain.hashCode() * api.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ThrottleOutDTO)) {
            return false;
        }
        ThrottleOutDTO throttleOutDTO = (ThrottleOutDTO) obj;

        return this.tenantDomain.equalsIgnoreCase(throttleOutDTO.tenantDomain) && this.api.equalsIgnoreCase(throttleOutDTO.api);

    }
}
