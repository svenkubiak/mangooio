package io.mangoo.services;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import dev.paseto.jpaseto.PasetoV1LocalBuilder;
import dev.paseto.jpaseto.Pasetos;
import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.enums.ClaimKey;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;

/**
 *
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
public class WebSocketServiceTest {
    private static String eventData;

    @Test
    public void testAddChannel() {
        //given
        final WebSocketService webSocketService = Application.getInstance(WebSocketService.class);
        final WebSocketChannel channel = Mockito.mock(WebSocketChannel.class);
        when(channel.getUrl()).thenReturn("/websocket");

        //when
        webSocketService.removeChannels("/websocket");
        webSocketService.addChannel(channel);

        //then
        assertThat(webSocketService.getChannels("/websocket"), not(nullValue()));
        assertThat(webSocketService.getChannels("/websocket").size(), equalTo(1));
    }

    @Test
    public void testRemoveChannel() {
        //given
        final WebSocketService webSocketService = Application.getInstance(WebSocketService.class);
        final WebSocketChannel channel = Mockito.mock(WebSocketChannel.class);
        when(channel.getUrl()).thenReturn("/websocket");

        //when
        webSocketService.addChannel(channel);
        webSocketService.removeChannels("/websocket");

        //then
        assertThat(webSocketService.getChannels("/websocket"), not(nullValue()));
        assertThat(webSocketService.getChannels("/websocket").size(), equalTo(0));
    }

    @Test
    public void testCloseChannel() throws Exception {
        //given
        final HttpClient httpClient = HttpClient.newHttpClient();
        final Config config = Application.getInstance(Config.class);
        final String url = "ws://" + config.getConnectorHttpHost() + ":" + config.getConnectorHttpPort() + "/websocket";
        final WebSocketService webSocketService = Application.getInstance(WebSocketService.class);
        webSocketService.removeChannels("/websocket");

        // when
        Listener listener = new Listener() {
            @Override
            public void onOpen(WebSocket webSocket) {
            }
        };
        httpClient.newWebSocketBuilder().buildAsync(new URI(url), listener).join();

        webSocketService.close("/websocket");

        //then
        assertThat(webSocketService.getChannels("/websocket"), not(nullValue()));
        assertThat(webSocketService.getChannels("/websocket").size(), equalTo(0));
    }

    @Test
    public void testSendData() throws Exception {
        // given
        final HttpClient httpClient = HttpClient.newHttpClient();
        final Config config = Application.getInstance(Config.class);
        final String url = "ws://" + config.getConnectorHttpHost() + ":" + config.getConnectorHttpPort() + "/websocket";
        final String data = UUID.randomUUID().toString();
        eventData = null;

        // when
        Listener listener = new Listener() {
            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence message, boolean last) {
                eventData = message.toString();
                return null;
            }
        };
        httpClient.newWebSocketBuilder().buildAsync(new URI(url), listener).get();

        Application.getInstance(WebSocketService.class).getChannels("/websocket").forEach(channel -> {
            try {
                if (channel.isOpen()) {
                    WebSockets.sendTextBlocking(data, channel);
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
         });

        //then
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> assertThat(eventData, equalTo(data)));
    }

    @Test
    public void testSendDataWithValidAuthentication() throws Exception {
        // given
        final HttpClient httpClient = HttpClient.newHttpClient();
        final Config config = Application.getInstance(Config.class);
        final String url = "ws://" + config.getConnectorHttpHost() + ":" + config.getConnectorHttpPort() + "/websocketauth";
        final String data = UUID.randomUUID().toString();
        eventData = null;

        // then
        PasetoV1LocalBuilder token = Pasetos.V1.LOCAL.builder().setSubject("foo")
                .setExpiration(LocalDateTime.now().plusHours(1).atZone(ZoneId.systemDefault()).toInstant())
                .claim(ClaimKey.TWO_FACTOR.toString(), "false")
                .setSharedSecret(new SecretKeySpec(config.getAuthenticationCookieSecret().getBytes(StandardCharsets.UTF_8), "AES"));
        
        Listener listener = new Listener() {
            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence message, boolean last) {
                eventData = message.toString();
                return null;
            }
        };
        httpClient.newWebSocketBuilder()
            .header("Cookie", config.getAuthenticationCookieName() + "=" + token.compact())
            .buildAsync(new URI(url), listener);
        
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> assertThat(eventData, not(equalTo(data))));
        
        Application.getInstance(WebSocketService.class).getChannels("/websocketauth").forEach(channel -> {
            try {
                if (channel.isOpen()) {
                    WebSockets.sendTextBlocking(data, channel);
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
         });
        
        // then
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> assertThat(eventData, equalTo(data)));
    }

    @Test
    public void testSendDataWithInvalidAuthentication() throws Exception {
        // given
        final HttpClient httpClient = HttpClient.newHttpClient();
        final Config config = Application.getInstance(Config.class);
        final String url = "ws://" + config.getConnectorHttpHost() + ":" + config.getConnectorHttpPort() + "/websocketauth";
        final String data = UUID.randomUUID().toString();
        eventData = null;
        
        PasetoV1LocalBuilder token = Pasetos.V1.LOCAL.builder().setSubject("foo")
                .setExpiration(LocalDateTime.now().plusHours(1).atZone(ZoneId.systemDefault()).toInstant())
                .claim(ClaimKey.TWO_FACTOR.toString(), "false")
                .setSharedSecret(new SecretKeySpec("oskdlwsodkcmansjdkwsowekd5jfvsq2mckdkalsodkskajsfdsfdsfvvkdkcskdsqidsjk".getBytes(StandardCharsets.UTF_8), "AES"));
        
        Listener listener = new Listener() {
            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence message, boolean last) {
                eventData = message.toString();
                return null;
            }
        };
        httpClient.newWebSocketBuilder()
            .header("Cookie", config.getAuthenticationCookieName() + "=" + token.compact())
            .buildAsync(new URI(url), listener);
        
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> assertThat(eventData, not(equalTo(data))));
        
        Application.getInstance(WebSocketService.class).getChannels("/websocketauth").forEach(channel -> {
            try {
                if (channel.isOpen()) {
                    WebSockets.sendTextBlocking(data, channel);
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
         });
        
        // then
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> assertThat(eventData, not(equalTo(data)))); 
    }
}