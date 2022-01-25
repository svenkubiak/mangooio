package io.mangoo.services;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import com.launchdarkly.eventsource.ReadyState;

import dev.paseto.jpaseto.Pasetos;
import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.enums.ClaimKey;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import okhttp3.Headers;

/**
 *
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
public class ServerSentEventServiceTest {
    
    @Test
    void testAddConnection() {
        //given
        final ServerSentEventService serverSentEventService = Application.getInstance(ServerSentEventService.class);
        final ServerSentEventConnection serverSentEventConnection = Mockito.mock(ServerSentEventConnection.class);
        when(serverSentEventConnection.getRequestURI()).thenReturn("/foo");
        when(serverSentEventConnection.getQueryString()).thenReturn(null);

        //when
        serverSentEventService.addConnection(serverSentEventConnection);

        //then
        assertThat(serverSentEventService.getConnections("/foo"), not(nullValue()));
        assertThat(serverSentEventService.getConnections("/foo").size(), equalTo(1));
    }

    @Test
    void testRemoveConnection() {
        //given
        final ServerSentEventService ServerSentEventService = Application.getInstance(ServerSentEventService.class);
        final ServerSentEventConnection serverSentEventConnection = Mockito.mock(ServerSentEventConnection.class);

        //when
        when(serverSentEventConnection.getRequestURI()).thenReturn("/foo");
        ServerSentEventService.addConnection(serverSentEventConnection);
        ServerSentEventService.removeConnections("/foo");
        
        //then
        assertThat(ServerSentEventService.getConnections("/foo"), not(nullValue()));
        assertThat(ServerSentEventService.getConnections("/foo").size(), equalTo(0));
    }

	@Test
	void testCloseConnection() throws InterruptedException {
		//given
		ServerSentEventService serverSentEventService = Application.getInstance(ServerSentEventService.class);
		Config config = Application.getInstance(Config.class);
		
		String url = String.format("http://" + config.getConnectorHttpHost() + ":" + config.getConnectorHttpPort() + "/sse");
		EventHandler eventHandler = new SimpleEventHandler();
		EventSource.Builder builder = new EventSource.Builder(eventHandler, URI.create(url)).reconnectTime(Duration.ofMillis(3000));
		
		try (EventSource eventSource = builder.build()) {
		      eventSource.start();
		      
		      await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> assertThat(eventSource.getState(), equalTo(ReadyState.OPEN)));
		      
		      //then
		      assertThat(serverSentEventService.getConnections("/sse"), not(nullValue()));
		      assertThat(serverSentEventService.getConnections("/sse").size(), equalTo(1));
		      
		      serverSentEventService.close("/sse");
		      
		      await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> assertThat(eventSource.getState(), equalTo(ReadyState.CLOSED)));
		      
		      //then
		      assertThat(serverSentEventService.getConnections("/sse"), not(nullValue()));
		      assertThat(serverSentEventService.getConnections("/sse").size(), equalTo(0));
		} 
	}

	@Test
	void testSendData() throws InterruptedException {
        //given
	    String data = UUID.randomUUID().toString();
        ServerSentEventService serverSentEventService = Application.getInstance(ServerSentEventService.class);
        Config config = Application.getInstance(Config.class);
        
        String url = String.format("http://" + config.getConnectorHttpHost() + ":" + config.getConnectorHttpPort() + "/sse");
        EventHandler eventHandler = new SimpleEventHandler();
        EventSource.Builder builder = new EventSource.Builder(eventHandler, URI.create(url)).reconnectTime(Duration.ofMillis(3000));
        
        try (EventSource eventSource = builder.build()) {
              eventSource.start();
              
              await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> assertThat(eventSource.getState(), equalTo(ReadyState.OPEN)));
              
              //then
              assertThat(serverSentEventService.getConnections("/sse"), not(nullValue()));
              assertThat(serverSentEventService.getConnections("/sse").size(), equalTo(1));
              
              Application.getInstance(ServerSentEventService.class).send("/sse", data);
              
              //then
              await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> assertThat(EventData.data, equalTo(data)));
              
              serverSentEventService.close("/sse");
              
              //then
              await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> assertThat(eventSource.getState(), equalTo(ReadyState.CLOSED)));
              
              //then
              assertThat(serverSentEventService.getConnections("/sse"), not(nullValue()));
              assertThat(serverSentEventService.getConnections("/sse").size(), equalTo(0));
        } 	    
	}

    @Test
    void testSendDataWithInvalidAuthentication() throws InterruptedException, IllegalArgumentException {
        //given
        String data = UUID.randomUUID().toString();
        ServerSentEventService serverSentEventService = Application.getInstance(ServerSentEventService.class);
        Config config = Application.getInstance(Config.class);
        
        
        String token = Pasetos.V1.LOCAL.builder()
                .setSubject("foo")
                .claim(ClaimKey.TWO_FACTOR.toString(), false)
                .setExpiration(LocalDateTime.now().plusHours(1).toInstant(ZoneOffset.UTC))
                .setSharedSecret(new SecretKeySpec("oskdlwsodkcmansjdkwsowekd5jfvsq2mckdkalsodkskajsfdsfdsfvvkdkcskdsqidsjk".getBytes(StandardCharsets.UTF_8), "AES"))
                .compact();    
        
        String cookie = config.getAuthenticationCookieName() + "=" + token;
        
        String url = String.format("http://" + config.getConnectorHttpHost() + ":" + config.getConnectorHttpPort() + "/sseauth");
        EventHandler eventHandler = new SimpleEventHandler();
        EventSource.Builder builder = new EventSource.Builder(eventHandler, URI.create(url)).reconnectTime(Duration.ofMillis(3000));
        Headers headers = new Headers.Builder()
            .add("Accept", "text/event-stream")
            .add("Cache-Control", "no-cache")
            .add("Set-Cookie", cookie)
            .build();
        
        builder.headers(headers);
        
        try (EventSource eventSource = builder.build()) {
              eventSource.start();
              
              await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> assertThat(eventSource.getState(), equalTo(ReadyState.CLOSED)));
              
              //then
              assertThat(serverSentEventService.getConnections("/sseauth"), not(nullValue()));
              assertThat(serverSentEventService.getConnections("/sseauth").size(), equalTo(0));
              
              Application.getInstance(ServerSentEventService.class).send("/sseauth", data);
              
              //then
              await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> assertThat(EventData.data, not(equalTo(data))));
              
              serverSentEventService.close("/sseauth");
              
              //then
              await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> assertThat(eventSource.getState(), equalTo(ReadyState.CLOSED)));
              
              //then
              assertThat(serverSentEventService.getConnections("/sseauth"), not(nullValue()));
              assertThat(serverSentEventService.getConnections("/sseauth").size(), equalTo(0));
        } 
    }
}