package io.mangoo.services;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.keys.HmacKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import com.google.common.base.Charsets;

import io.mangoo.TestExtension;
import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.crypto.Crypto;
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
        final Config config = Application.getInstance(Config.class);
        final WebSocketService webSocketService = Application.getInstance(WebSocketService.class);
        webSocketService.removeChannels("/websocket");
        final WebSocketClientFactory factory = new WebSocketClientFactory();
        factory.start();
        final String url = "ws://" + config.getConnectorHttpHost() + ":" + config.getConnectorHttpPort() + "/websocket";

        //when
        final WebSocketClient client = new WebSocketClient(factory);
        client.open(new URI(url), new WebSocket.OnTextMessage() {
            @Override
            public void onOpen(Connection connection) {
                // intentionally left blank
            }

            @Override
            public void onClose(int closeCode, String message) {
                // intentionally left blank
            }

            @Override
            public void onMessage(String data) {
                // intentionally left blank
            }
        }).get(5, TimeUnit.SECONDS);

        webSocketService.close("/websocket");

        //then
        assertThat(webSocketService.getChannels("/websocket"), not(nullValue()));
        assertThat(webSocketService.getChannels("/websocket").size(), equalTo(0));
    }

    @Test
    public void testSendData() throws Exception {
        //given
        final Config config = Application.getInstance(Config.class);
        final WebSocketService webSocketService = Application.getInstance(WebSocketService.class);
        webSocketService.removeChannels("/websocket");
        final WebSocketClientFactory factory = new WebSocketClientFactory();
        factory.start();
        final String url = "ws://" + config.getConnectorHttpHost() + ":" + config.getConnectorHttpPort() + "/websocket";
        final String data = "Server sent data FTW!";
        eventData = null;

        //when
        Connection connection = new WebSocketClient(factory).open(new URI(url), new WebSocket.OnTextMessage() {
            @Override
            public void onOpen(Connection connection) {
                // intentionally left blank
            }

            @Override
            public void onClose(int closeCode, String message) {
                // intentionally left blank
            }

            @Override
            public void onMessage(String data) {
                eventData = data;
            }
        }).get(5, TimeUnit.SECONDS);

        //then
        await().atMost(1,  TimeUnit.SECONDS).untilAsserted(() -> assertThat(connection, not(equalTo(null))));
        webSocketService.getChannels("/websocket").forEach(channel -> {
            try {
                if (channel.isOpen()) {
                    WebSockets.sendTextBlocking(data, channel);
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
         });
        await().atMost(4,  TimeUnit.SECONDS).untilAsserted(() -> assertThat(eventData, equalTo(data)));
    }

    @Test
    public void testSendDataWithValidAuthentication() throws Exception {
        //given
        final WebSocketService webSocketService = Application.getInstance(WebSocketService.class);
        final Config config = Application.getInstance(Config.class);
        webSocketService.removeChannels("/websocketauth");
        final WebSocketClientFactory factory = new WebSocketClientFactory();
        factory.start();
        final String url = "ws://" + config.getConnectorHttpHost() + ":" + config.getConnectorHttpPort() + "/websocketauth";
        final String data = "Server sent data with authentication FTW!";
        eventData = null;

        JwtClaims jwtClaims = new JwtClaims();
        jwtClaims.setSubject("foo");
        jwtClaims.setClaim(ClaimKey.TWO_FACTOR.toString(), false);
        jwtClaims.setExpirationTime(NumericDate.fromMilliseconds(LocalDateTime.now().plusHours(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
        
        JsonWebSignature jsonWebSignature = new JsonWebSignature();
        jsonWebSignature.setKey(new HmacKey(config.getAuthenticationCookieSignKey().getBytes(Charsets.UTF_8)));
        jsonWebSignature.setPayload(jwtClaims.toJson());
        jsonWebSignature.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA512);
        
        String jwt = Application.getInstance(Crypto.class).encrypt(jsonWebSignature.getCompactSerialization(), config.getAuthenticationCookieEncryptionKey());
        
        //when
        final WebSocketClient client = new WebSocketClient(factory);
        client.getCookies().put(config.getAuthenticationCookieName(), jwt);
        client.open(new URI(url), new WebSocket.OnTextMessage() {
            @Override
            public void onOpen(Connection connection) {
                // intentionally left blank
            }

            @Override
            public void onClose(int closeCode, String message) {
                // intentionally left blank
            }

            @Override
            public void onMessage(String data) {
                if (StringUtils.isBlank(eventData)) {
                    eventData = data;
                }
            }
        }).get(5, TimeUnit.SECONDS);

        //then
        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> assertThat(client, not(equalTo(null))));
        webSocketService.getChannels("/websocketauth").forEach(channel -> {
            try {
                if (channel.isOpen()) {
                    WebSockets.sendTextBlocking(data, channel);
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
         });
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(eventData, equalTo(data)));
    }

    @Test
    public void testSendDataWithInvalidAuthentication() throws Exception {
        //given
        final WebSocketService webSocketService = Application.getInstance(WebSocketService.class);
        final Config config = Application.getInstance(Config.class);
        webSocketService.removeChannels("/websocketauth");
        final WebSocketClientFactory factory = new WebSocketClientFactory();
        factory.start();
        final String url = "ws://" + config.getConnectorHttpHost() + ":" + config.getConnectorHttpPort() + "/websocketauth";
        final String data = "Server sent data with authentication FTW!";
        eventData = null;
        
        JwtClaims jwtClaims = new JwtClaims();
        jwtClaims.setSubject("foo");
        jwtClaims.setClaim(ClaimKey.TWO_FACTOR.toString(), false);
        jwtClaims.setExpirationTime(NumericDate.fromMilliseconds(LocalDateTime.now().plusHours(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
        
        JsonWebSignature jsonWebSignature = new JsonWebSignature();
        jsonWebSignature.setKey(new HmacKey("oskdlwsodkcmansjdkwsowekd5jfvsq2mckdkalsodkskajsfdsfdsfvvkdkcskdsqidsjk".getBytes(Charsets.UTF_8)));
        jsonWebSignature.setPayload(jwtClaims.toJson());
        jsonWebSignature.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA512);
        
        String jwt = Application.getInstance(Crypto.class).encrypt(jsonWebSignature.getCompactSerialization(), config.getAuthenticationCookieEncryptionKey());

        //when
        final WebSocketClient client = new WebSocketClient(factory);
        client.getCookies().put(config.getAuthenticationCookieName(), jwt);
        client.open(new URI(url), new WebSocket.OnTextMessage() {
            @Override
            public void onOpen(Connection connection) {
                // intentionally left blank
            }

            @Override
            public void onClose(int closeCode, String message) {
                // intentionally left blank
            }

            @Override
            public void onMessage(String data) {
                if (StringUtils.isBlank(eventData)) {
                    eventData = data;
                }
            }
        }).get(5, TimeUnit.SECONDS);

        webSocketService.getChannels(url).forEach(channel -> {
            try {
                if (channel.isOpen()) {
                    WebSockets.sendTextBlocking(data, channel);
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
         });

        //then
        await().atMost(2,  TimeUnit.SECONDS).untilAsserted(() -> assertThat(eventData, not(equalTo(data))));
    }
}