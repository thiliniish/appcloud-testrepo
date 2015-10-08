/*
 * Copyright 2005-2013 WSO2, Inc. http://www.wso2.org
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.cloud.heartbeat.monitor.core.status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.cloud.heartbeat.monitor.core.configuration.Node;
import org.wso2.cloud.heartbeat.monitor.utils.Constants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LiveStatusConnector implements Runnable {

    private static final Log log = LogFactory.getLog(LiveStatusConnector.class);

    private int socket;
    private boolean portOpen;
    private Node root;

    public LiveStatusConnector(Node root){

        Node liveStatusNode = root.findChildNodeByName(Constants.LIVE_STATUS);
        this.socket = Integer.valueOf(liveStatusNode.getProperty(Constants.LIVE_STATUS_PORT));
        this.root = root;
        this.portOpen = true;
    }

    @Override
    public void run() {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        try {
            ServerSocket serverSocket = new ServerSocket(socket);
            while(portOpen){
                Socket clientSocket = serverSocket.accept();
                Runnable connectionHandler = new ConnectionHandler(clientSocket, root);
                executor.execute(connectionHandler);
            }
        } catch (IOException e) {
            log.error("Live status Heartbeat service failure: " + e.getMessage());
        }
    }

    public void closePort(){
        portOpen = false;
    }
}
