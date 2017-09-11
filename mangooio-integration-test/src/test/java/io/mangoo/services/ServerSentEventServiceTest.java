package io.mangoo.services;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.sse.SseEventSource;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.sse.EventListener;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.junit.Test;
import org.mockito.Mockito;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.undertow.server.handlers.sse.ServerSentEventConnection;

/**
 *
 * @author svenkubiak
 *
 */
public class ServerSentEventServiceTest {
    private static String eventData;
    private static final String COOKIE_NAME = "TEST-AUTH";
    private static final String VALID_COOKIE_VALUE = "3372c6783fa8d223c700e9903b4e8037db710b4b60ee2ca129465fa0a12e0a0b1860019962ae04e4b329e4da03ce09eb347c97b5598085cc8530213b9b82f91f|2999-11-11T11:11:11.111|0#mangooio";
    private static final String INVALID_COOKIE_VALUE = "3372c6783fa8d223c700e9903b4e8037db710b4b60ee2ca129465fa0a12e0a0b1860019962ae04e4b329e4da03ce09eb347c97b5598085cc8530213b9b82f91f|2999-11-11T11:11:11.111|0#mangooiO";
    
    @Test
    public void testAddConnection() {
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
    public void testRemoveConnection() {
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
	public void testCloseConnection() throws InterruptedException {
		//given
		ServerSentEventService ServerSentEventService = Application.getInstance(ServerSentEventService.class);
		Config config = Application.getInstance(Config.class);
		Client client = ClientBuilder.newClient();

		//when
		WebTarget webTarget = client.target("http://" + config.getConnectorHttpHost() + ":" + config.getConnectorHttpPort() + "/sse");
		SseEventSource sseEventSource = SseEventSource.target(webTarget).build();
		sseEventSource.register((sseEvent) -> {eventData = sseEvent.readData();}, (e) -> e.printStackTrace());
		sseEventSource.open();
		ServerSentEventService.close("/sse");
		sseEventSource.close();
		client.close();

		//then
		assertThat(ServerSentEventService.getConnections("/sse"), not(nullValue()));
		assertThat(ServerSentEventService.getConnections("/sse").size(), equalTo(0));
	}

	@Test
	public void testSendData() throws InterruptedException {
		//given
		Config config = Application.getInstance(Config.class);
		Client client = ClientBuilder.newClient();
		String data = UUID.randomUUID().toString();
		
		//when
		WebTarget webTarget = client.target("http://" + config.getConnectorHttpHost() + ":" + config.getConnectorHttpPort() + "/sse");
		SseEventSource sseEventSource = SseEventSource.target(webTarget).build();
		sseEventSource.register((sseEvent) -> {eventData = sseEvent.readData();}, (e) -> e.printStackTrace());
		sseEventSource.open();
        
        //then
        Application.getInstance(ServerSentEventService.class).send("/sse", data);
        await().atMost(2,  TimeUnit.SECONDS).untilAsserted(() -> assertThat(eventData, equalTo(data)));
        sseEventSource.close();
        client.close();
	}

    @Test
    public void testSendDataWithValidAuthentication() throws InterruptedException {
        //given
        final ServerSentEventService serverSentEventService = Application.getInstance(ServerSentEventService.class);
        final Config config = Application.getInstance(Config.class);
        final String data = "Server sent data with authentication FTW!";

        //when
        final WebTarget target = ClientBuilder.newBuilder()
                .register(SseFeature.class)
                .build()
                .target("http://" + config.getConnectorHttpHost() + ":" + config.getConnectorHttpPort() + "/sseauth");

        final CustomWebTarget customWebTarget = new CustomWebTarget(target, new Cookie(COOKIE_NAME, VALID_COOKIE_VALUE));
        final EventSource eventSource = EventSource.target(customWebTarget).build();
        final EventListener listener = new EventListener() {
            @Override
            public void onEvent(InboundEvent inboundEvent) {
                if (StringUtils.isBlank(eventData)) {
                    eventData = inboundEvent.readData(String.class);
                }
            }
        };
        eventSource.register(listener);
        eventSource.open();
        serverSentEventService.send("/sseauth", data);

        //then
        await().atMost(2,  TimeUnit.SECONDS).untilAsserted(() -> assertThat(eventData, equalTo(data)));
        eventSource.close();
    }

    @Test
    public void testSendDataWithInvalidAuthentication() throws InterruptedException {
        //given
        final ServerSentEventService serverSentEventService = Application.getInstance(ServerSentEventService.class);
        final Config config = Application.getInstance(Config.class);
        final String data = "Server sent data with authentication FTW!";

        //when
        final WebTarget target = ClientBuilder.newBuilder()
                .register(SseFeature.class)
                .build()
                .target("http://" + config.getConnectorHttpHost() + ":" + config.getConnectorHttpPort() + "/sseauth");

        final CustomWebTarget customWebTarget = new CustomWebTarget(target, new Cookie(COOKIE_NAME, INVALID_COOKIE_VALUE));
        final EventSource eventSource = EventSource.target(customWebTarget).build();
        final EventListener listener = new EventListener() {
            @Override
            public void onEvent(InboundEvent inboundEvent) {
                if (StringUtils.isBlank(eventData)) {
                    eventData = inboundEvent.readData(String.class);
                }
            }
        };
        eventSource.register(listener);
        eventSource.open();
        serverSentEventService.send("/sseauth", data);

        //then
        await().atMost(2,  TimeUnit.SECONDS).untilAsserted(() -> assertThat(eventData, not(equalTo(data))));
        eventSource.close();
    }
}