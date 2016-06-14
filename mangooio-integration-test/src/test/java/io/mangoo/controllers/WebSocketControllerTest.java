package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.junit.Test;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;

/**
 *
 * @author svenkubiak
 *
 */
public class WebSocketControllerTest {

    @Test
    public void testWebSocketConnection() throws Exception {
        //given
        final Config config = Application.getInstance(Config.class);
        final String uri = "ws://" + config.getConnectorHttpHost() + ":" + config.getConnectorHttpPort() + "/websocket";
        final WebSocketClientFactory factory = new WebSocketClientFactory();

        //when
        factory.start();
        final WebSocketClient client = new WebSocketClient(factory);
        final WebSocket.Connection connection = client.open(new URI(uri), new WebSocket.OnTextMessage() {
            @Override
            public void onOpen(Connection connection) {
                // open notification
            }

            @Override
            public void onClose(int closeCode, String message) {
                // close notification
            }

            @Override
            public void onMessage(String data) {
                // handle incoming message
            }
        }).get(5, TimeUnit.SECONDS);
        connection.sendMessage("Hello World");
        connection.sendMessage("Hello World".getBytes(), 0, 0);

        //then
        assertThat(connection.isOpen(), equalTo(true));
        connection.close();
    }
}