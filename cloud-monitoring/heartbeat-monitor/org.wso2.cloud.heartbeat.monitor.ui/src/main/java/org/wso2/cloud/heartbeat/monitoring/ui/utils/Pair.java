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

/**
 * Manage Long Pairs used for list interfaces
 */
public class Pair {

    private Long left;
    private Long right;

    /**
     * Construct the pair
     * @param left  Long value for left value <<b>left</b>,right>
     * @param right <left,<b>right</b>>
     */
    public Pair(Long left, Long right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Returns the left value
     * @return left long value
     */
    public Long getLeft() {
        return left;
    }

    /**
     * returns the right value
     * @return  right long value
     */
    public Long getRight() {
        return right;
    }

    /**
     * converts the pair into a string
     * @return converted string pair
     */
    public String toString() {
        return "[" + left + ", " + right + "]";
    }

}
