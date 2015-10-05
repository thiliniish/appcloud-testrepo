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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Used to merge time intervals given as value pairs by comparing upper value and lower value of each adjacent value pairs
 */
public class IntervalMerger {

    public IntervalMerger() {

    }

    /**
     * @param intervals pair of time intervals
     * @return merged time pairs
     */
    public List<Pair> merge(List<Pair> intervals) {
        if (intervals.isEmpty()) {
            return intervals;
        }
        if (intervals.size() == 1) {
            return intervals;
        }
        Collections.sort(intervals, new IntervalComparator());

        Pair first = intervals.get(0);
        long start = first.getLeft();
        long end = first.getRight();

        List<Pair> result = new ArrayList<Pair>();

        for (int i = 1; i < intervals.size(); i++) {
            Pair current = intervals.get(i);
            if (current.getLeft() <= end) {
                end = Math.max(current.getRight(), end);
            } else {
                Long starter = start;
                Long ender = end;
                result.add(new Pair(starter, ender));
                start = current.getLeft();
                end = current.getRight();
            }
        }
        Long starter = start;
        Long ender = end;
        result.add(new Pair(starter, ender));

        return result;
    }
}
