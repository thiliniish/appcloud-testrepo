/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.cloud.billing.common.zuora.security.utils;

import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

import javax.net.SocketFactory;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Bypass self-signed certificate for testing purposes
 */
public class BypassSSLSocketFactory implements ProtocolSocketFactory {

    private TrustManager[] getTrustManager() {

        return new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };
    }

    public Socket createSocket(String host, int port) throws IOException {
        TrustManager[] trustAllCerts = getTrustManager();
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            SocketFactory socketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
            return socketFactory.createSocket(host, port);
        } catch (Exception ex) {
            throw new UnknownHostException("Problems to connect " + host + ex.toString());
        }
    }

    public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort) throws IOException {
        TrustManager[] trustAllCerts = getTrustManager();
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            SocketFactory socketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
            return socketFactory.createSocket(host, port, clientHost, clientPort);
        } catch (Exception ex) {
            throw new UnknownHostException("Problems to connect " + host + ex.toString());
        }
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localAddress, int localPort,
                               HttpConnectionParams arg4) throws IOException {
        TrustManager[] trustAllCerts = getTrustManager();
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            SocketFactory socketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();

            return socketFactory.createSocket(host, port);
        } catch (Exception ex) {
            throw new UnknownHostException("Problems to connect " + host + ex.toString());
        }
    }

}
