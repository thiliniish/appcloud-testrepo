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

import java.sql.Timestamp;
import java.util.ArrayList;
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
	 * failure information and failure count for each clouds / server / test
	 *
	 * @param listPair     timestamp pairs for failed test records
	 * @param listLong     list of timestamps for success records
	 * @param failureCount total number of failure count
	 */
	public FailureIntervals(List<Pair> listPair, List<Long> listLong, int failureCount) {
		this.listPair = listPair;
		this.listLong = listLong;
		this.failureCount = failureCount;
	}

	/**
	 * Returns the list of failure timestamp pairs
	 *
	 * @return list of pairs
	 */
	public List<Pair> getListPair() {
		return listPair;
	}

	/**
	 * returns the list of positive timestamp pairs
	 *
	 * @return List of long type
	 */
	public List<Long> getListLong() {
		return listLong;
	}

	/**
	 * returns the failure count
	 *
	 * @return integer failure count
	 */
	public int getFailureCount() {
		return failureCount;
	}

	public List<TimeStampPair> mapTimeStamp() {
		List<TimeStampPair> listTimeStampPair = new ArrayList<TimeStampPair>();
		for(Pair singlePair : listPair){
			TimeStampPair
					tmPair = new TimeStampPair(new Timestamp(singlePair.getLeft()), new Timestamp(singlePair.getRight()));
			listTimeStampPair.add(tmPair);
		}
		return listTimeStampPair;
	}

}
