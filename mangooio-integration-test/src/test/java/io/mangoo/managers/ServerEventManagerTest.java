package io.mangoo.managers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.media.sse.EventListener;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.mangoo.test.MangooInstance;
import io.mangoo.utils.ConfigUtils;
import io.undertow.server.handlers.sse.ServerSentEventConnection;

/**
 * 
 * @author svenkubiak
 *
 */
public class ServerEventManagerTest {
    private static String eventData;
    
    @Before
    public void init() {
        eventData = "";
    }
    
    @Test
    public void testAddConnection() {
        //given
        ServerEventManager serverEventManager = MangooInstance.TEST.getInstance(ServerEventManager.class);
        ServerSentEventConnection serverSentEventConnection = Mockito.mock(ServerSentEventConnection.class);
        when(serverSentEventConnection.getRequestURI()).thenReturn("/foo");
        when(serverSentEventConnection.getQueryString()).thenReturn(null);
        
        //when
        serverEventManager.addConnection(serverSentEventConnection);
        
        //then
        assertThat(serverEventManager.getConnections("/foo"), not(nullValue()));
        assertThat(serverEventManager.getConnections("/foo").size(), equalTo(1));
    }
    
    @Test
    public void testRemoveConnection() {
        //given
        ServerEventManager serverEventManager = MangooInstance.TEST.getInstance(ServerEventManager.class);
        ServerSentEventConnection serverSentEventConnection = Mockito.mock(ServerSentEventConnection.class);
        when(serverSentEventConnection.getRequestURI()).thenReturn("/foo");
        serverEventManager.addConnection(serverSentEventConnection);
        
        //when
        serverEventManager.removeConnections("/foo");
        
        //then
        assertThat(serverEventManager.getConnections("/foo"), not(nullValue()));
        assertThat(serverEventManager.getConnections("/foo").size(), equalTo(0));
    }
    
    @Test
    public void testSendData() {
        //given
        ServerEventManager serverEventManager = MangooInstance.TEST.getInstance(ServerEventManager.class);
        String data = "Server sent data FTW!";
        
        //when
        WebTarget target = ClientBuilder.newBuilder()
                .register(SseFeature.class)
                .build()
                .target("http://" + ConfigUtils.getApplicationHost() + ":" + ConfigUtils.getApplicationPort() + "/sse");
        EventSource eventSource = EventSource.target(target).build();
        EventListener listener = new EventListener() {
            @Override
            public void onEvent(InboundEvent inboundEvent) {
                eventData = inboundEvent.readData(String.class);
            }
        };
        eventSource.register(listener);
        eventSource.open();
        serverEventManager.send("/sse", data);
        eventSource.close();
        
        //then
        assertThat(eventData, not(nullValue()));
        assertThat(eventData, equalTo(data));
    }
}