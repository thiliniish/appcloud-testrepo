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

package org.wso2.carbon.cloud.billing.common.utils;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.lang.StringUtils;

/**
 *
 */
public class BillingUtils {

    /**
     * Return custom protocol with added protocol versions for a scheme
     *
     * @param scheme              scheme
     * @param enabledProtocolVersions protocol versions
     * @return modified protocol
     */
    public static Protocol getCustomProtocol(String scheme, String enabledProtocolVersions) {

        if (StringUtils.isBlank(scheme)) {
            throw new IllegalArgumentException("Schema for protocol cannot be null or empty");
        }
        Protocol baseProtocol = Protocol.getProtocol(scheme);

        if (StringUtils.isBlank(enabledProtocolVersions)) {
            return baseProtocol;
        }
        int defaultPort = baseProtocol.getDefaultPort();
        ProtocolSocketFactory baseFactory = baseProtocol.getSocketFactory();
        ProtocolSocketFactory customFactory =
                new CustomHTTPSSocketFactory(baseFactory, enabledProtocolVersions.trim().split("\\s*,\\s*"));

        return new Protocol(scheme, customFactory, defaultPort);
    }
}
