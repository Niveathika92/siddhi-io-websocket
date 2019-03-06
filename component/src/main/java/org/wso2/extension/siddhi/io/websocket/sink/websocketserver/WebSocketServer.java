/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.extension.siddhi.io.websocket.sink.websocketserver;

import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.ServerBootstrapConfiguration;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;


/**
 * {@code WebSocketServer } Handle the WebSocket server.
 */

class WebSocketServer {

    private final String host;
    private final int webSocketPort;
    private ServerConnector serverConnector = null;
    private String keystorePath;
    private String keystorePassword;
    private boolean isSslEnabled;
    private WebSocketServerSinkConnectorListener serverSinkConnectorListener;

    /**
     * @param host             host of the WebSocket server.
     * @param port             host of the WebSocket server.
     * @param subProtocols     Sub-Protocols which are allowed by the service.
     * @param idleTimeout      Idle timeout in milli-seconds for WebSocket connection.
     * @param isTlsEnabled     secure connection is enabled or not.
     * @param keystorePath     file path to the location of the keystore.
     * @param keystorePassword password for the keystore
     * @param streamDefinition containing stream definition
     */
    WebSocketServer(String host, int port, String[] subProtocols, int idleTimeout,
                    boolean isTlsEnabled, String keystorePath, String keystorePassword,
                    StreamDefinition streamDefinition) {
        this.host = host;
        this.webSocketPort = port;
        this.isSslEnabled = isTlsEnabled;
        this.keystorePath = keystorePath;
        this.keystorePassword = keystorePassword;
        serverSinkConnectorListener = new WebSocketServerSinkConnectorListener
                (subProtocols, idleTimeout, streamDefinition);
    }

    /**
     * Start the WebSocket server.
     */
    void start() throws InterruptedException {
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        if (isSslEnabled) {
            listenerConfiguration.setScheme("https");
            listenerConfiguration.setKeyStoreFile(keystorePath);
            listenerConfiguration.setKeyStorePass(keystorePassword);
        }
        listenerConfiguration.setHost(host);
        listenerConfiguration.setPort(webSocketPort);
        HttpWsConnectorFactory httpConnectorFactory = new DefaultHttpWsConnectorFactory();
        serverConnector = httpConnectorFactory.createServerConnector(new ServerBootstrapConfiguration(null),
                listenerConfiguration);
        ServerConnectorFuture connectorFuture = serverConnector.start();
        connectorFuture.setWebSocketConnectorListener(serverSinkConnectorListener);
        connectorFuture.sync();
    }

    /**
     * Stop the WebSocket server.
     */
    void stop() {
        if (serverConnector != null) {
            serverConnector.stop();
        }
    }

    /**
     * Send the events to the client.
     */
    void send(Object message) {
        serverSinkConnectorListener.send(message);
    }
}
