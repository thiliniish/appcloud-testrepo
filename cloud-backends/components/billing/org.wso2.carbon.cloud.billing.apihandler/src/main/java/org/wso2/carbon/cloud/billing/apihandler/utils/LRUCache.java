/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
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
package org.wso2.carbon.cloud.billing.apihandler.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * API handler to block api invocation after the tenant deactivate from API Cloud
 * LRU cache mechanism which will keep the cashed tenant IDs of the disable tenants.
 */
public class LRUCache {
    private Map<String, DoubleLinkedListNode> map = new HashMap<>();
    private DoubleLinkedListNode head;
    private DoubleLinkedListNode end;
    private int capacity;
    private int length;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        length = 0;
    }

    public String get(String key) {
        if (map.containsKey(key)) {
            DoubleLinkedListNode latest = map.get(key);
            removeNode(latest);
            setHead(latest);
            return latest.key;
        } else {
            return null;
        }
    }

    public void removeNode(DoubleLinkedListNode node) {
        DoubleLinkedListNode cur = node;
        DoubleLinkedListNode pre = cur.pre;
        DoubleLinkedListNode post = cur.next;

        if (pre != null) {
            pre.next = post;
        } else {
            head = post;
        }

        if (post != null) {
            post.pre = pre;
        } else {
            end = pre;
        }
    }

    public void setHead(DoubleLinkedListNode node) {
        node.next = head;
        node.pre = null;
        if (head != null) {
            head.pre = node;
        }

        head = node;
        if (end == null) {
            end = node;
        }
    }

    public void set(String key) {
        if (map.containsKey(key)) {
            DoubleLinkedListNode oldNode = map.get(key);
            removeNode(oldNode);
            setHead(oldNode);
        } else {
            DoubleLinkedListNode newNode = new DoubleLinkedListNode(key);
            if (length < capacity) {
                setHead(newNode);
                map.put(key, newNode);
                length++;
            } else {
                if (end != null) {
                    map.remove(end.key);
                    end = end.pre;
                    end.next = null;
                }
                setHead(newNode);
                map.put(key, newNode);
            }
        }
    }
}
