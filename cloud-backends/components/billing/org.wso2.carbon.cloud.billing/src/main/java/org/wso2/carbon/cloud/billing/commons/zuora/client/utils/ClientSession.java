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

package org.wso2.carbon.cloud.billing.commons.zuora.client.utils;

import com.zuora.api.SessionHeader;

/**
 * Client session maintained to reduce number of login calls. you have to configure
 * session expiration time in billing.xml. it should be less than actual session expiration
 * time.
 * <p/>
 * <SessionExpired>3000000</SessionExpired>
 */
public class ClientSession {

    /**
     * The header.
     */
    private SessionHeader header;

    /**
     * Managing client session.
     */
    private long maxInactive;
    private long sessionInitTime;

    public ClientSession(String session, long maxInactive) {
        this.header = new SessionHeader();
        this.header.setSession(session);
        this.sessionInitTime = System.currentTimeMillis();
        this.maxInactive = maxInactive;
    }

    /**
     * Get session header
     *
     * @return authenticated header
     */
    public SessionHeader getHeader() {
        return this.header;
    }

    /**
     * Check for session expiration
     *
     * @return expired boolean
     */
    public boolean isSessionExpired() {
        return System.currentTimeMillis() >= sessionInitTime + maxInactive;
    }
}
