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
import org.w3c.dom.Document;
import org.wso2.cloud.heartbeat.monitor.core.configuration.Node;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.SQLException;

public class ConnectionHandler implements Runnable {

    private static final Log log = LogFactory.getLog(ConnectionHandler.class);

    private Socket clientSocket;
    private Node root;

    public ConnectionHandler(Socket clientSocket, Node root){
        this.clientSocket = clientSocket;
        this.root = root;
    }

    @Override
    public void run() {
        try {
            Document document = XMLBuilder.createXML(root);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            OutputStream bos = clientSocket.getOutputStream();

            StreamResult result = new StreamResult(bos);
            transformer.transform(source, result);
            bos.flush();
            bos.close();

        } catch (ParserConfigurationException e) {
            log.error("Live status Heartbeat service failure: " + e.getMessage());
        } catch (SQLException e) {
            log.error("Live status Heartbeat service failure: " + e.getMessage());
        } catch (IOException e) {
            log.error("Live status Heartbeat service failure: " + e.getMessage());
        } catch (TransformerConfigurationException e) {
            log.error("Live status Heartbeat service failure: " + e.getMessage());
        } catch (TransformerException e) {
            log.error("Live status Heartbeat service failure: " + e.getMessage());
        }

    }
}


