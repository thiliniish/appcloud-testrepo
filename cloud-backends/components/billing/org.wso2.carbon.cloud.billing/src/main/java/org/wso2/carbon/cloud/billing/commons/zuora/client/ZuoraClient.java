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

package org.wso2.carbon.cloud.billing.commons.zuora.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.wso2.carbon.cloud.billing.commons.zuora.client.utils.ZuoraClientUtils;

import java.io.IOException;

/**
 * abstract class for zuora clients
 */
public abstract class ZuoraClient {

    protected ZuoraClientUtils zuoraClientUtils;

    public ZuoraClientUtils getZuoraClientUtils() {
        return zuoraClientUtils;
    }

    /**
     * Response as a json object
     *
     * @param obj Results from the stub
     * @return results as a json object
     * @throws IOException
     */
    protected JsonObject objectToJson(Object obj) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String result = mapper.writeValueAsString(obj);
        return (JsonObject) (new JsonParser().parse(result));
    }
}
