package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.core.Config;

/**
 *
 * @author svenkubiak
 *
 */
@ExtendWith({ TestExtension.class })
public class WebSocketControllerTest {
    private static boolean connected = false;

    @Test
    public void testWebSocketConnection() throws Exception {
        // given
        final HttpClient httpClient = HttpClient.newHttpClient();
        final Config config = Application.getInstance(Config.class);
        final String uri = "ws://" + config.getConnectorHttpHost() + ":" + config.getConnectorHttpPort() + "/websocket";

        // when
        Listener listener = new Listener() {
            @Override
            public void onOpen(WebSocket webSocket) {
                connected = true;
            }
        };
        httpClient.newWebSocketBuilder().buildAsync(new URI(uri), listener).join();
        
        // then
        assertThat(connected, equalTo(true));
    }
}