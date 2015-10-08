/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.cloud.heartbeat.monitoring.ui.utils;

import java.util.List;

/**
 * Class to create objects to hold failure intervals and their reletive failure counts
 */
public class FailureIntervals {
    private List<Pair> listPair;
    private List<Long> listLong;
    private int failureCount;

    /**
     * Creates the failure interval data structure to hold failure interval list pairs,
     * failure information and failure count for each clous / server / test
     *
     * @param listPair pair list
     * @param listLong  long list
     * @param failureCount failure count
     */
    public FailureIntervals(List<Pair> listPair, List<Long> listLong, int failureCount) {
        this.listPair = listPair;
        this.listLong = listLong;
        this.failureCount = failureCount;
    }

    /**
     * Returns the list of pairs
     * @return list of pairs
     */
    public List<Pair> getListPair() {
        return listPair;
    }

    /**
     * returns the list of long pairs
     * @return List of long type
     */
    public List<Long> getListLong() {
        return listLong;
    }

    /**
     * returns the failure count
     * @return integer failure count
     */
    public int getFailureCount() {
        return failureCount;
    }
}
