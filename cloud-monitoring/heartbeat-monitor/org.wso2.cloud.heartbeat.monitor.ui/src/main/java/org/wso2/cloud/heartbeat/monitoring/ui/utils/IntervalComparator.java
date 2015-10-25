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

import java.util.Comparator;

/**
 * Class to compare intervals to identify which is the largest interval pair
 */
public class IntervalComparator implements Comparator {

    public int compare(Object o1, Object o2) {
        Pair i1 = (Pair) o1;
        Pair i2 = (Pair) o2;
        long abc = i1.getLeft() - i2.getLeft();
        return (int) abc;
    }
}
