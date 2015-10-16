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

package org.wso2.carbon.cloud.billing.commons.utils;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Iterator;

/**
 * Cloud billing common utilises
 */
public class CloudBillingUtils {

    /**
     * Checks the current carbon server is the coordinator of the cluster
     *
     * @return leader boolean
     */
    public static boolean isLeader() {

        /* Get all the Hazelcast instances in the current JVM.
           In case of carbon server this is always either one
           or zero. */
        Iterator<HazelcastInstance> iter = Hazelcast.getAllHazelcastInstances().iterator();

        if (iter.hasNext()) { // cluster mode
            HazelcastInstance instance = iter.next();
            return instance.getCluster().getMembers().iterator().next().localMember();
        } else {
            return true; // standalone mode
        }
    }
}
