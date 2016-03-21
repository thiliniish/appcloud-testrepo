/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License
 */

package org.wso2.carbon.cloud.billing.common.utils;

import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Custom protocol factory which enables specifically mentioned ssl protocols versions
 */
public class CustomHTTPSSocketFactory implements SecureProtocolSocketFactory {

    private final SecureProtocolSocketFactory base;
    private String[] sslEnabledProtocols;

    /**
     * CustomHttpSocket Factory
     *
     * @param base Base protocol socket factory
     * @param sslEnabledProtocols ssl enabled protocols
     */
    public CustomHTTPSSocketFactory(ProtocolSocketFactory base, String[] sslEnabledProtocols) {
        if (base == null || !(base instanceof SecureProtocolSocketFactory)) {
            throw new IllegalArgumentException();
        }
        this.base = (SecureProtocolSocketFactory) base;
        this.sslEnabledProtocols = sslEnabledProtocols.clone();
    }

    /**
     * Add enabled protocols only for the SSL socket
     *
     * @param socket socket
     * @return modified socket
     */
    private Socket insertEnabledProtocols(Socket socket) {
        if (!(socket instanceof SSLSocket)) {
            return socket;
        }
        SSLSocket sslSocket = (SSLSocket) socket;
        sslSocket.setEnabledProtocols(sslEnabledProtocols);
        return sslSocket;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return insertEnabledProtocols(base.createSocket(host, port));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Socket createSocket(String host, int port, InetAddress localAddress, int localPort) throws IOException {
        return insertEnabledProtocols(base.createSocket(host, port, localAddress, localPort));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Socket createSocket(String host, int port, InetAddress localAddress, int localPort,
                               HttpConnectionParams params) throws IOException {
        return insertEnabledProtocols(base.createSocket(host, port, localAddress, localPort, params));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        return insertEnabledProtocols(base.createSocket(socket, host, port, autoClose));
    }
}
