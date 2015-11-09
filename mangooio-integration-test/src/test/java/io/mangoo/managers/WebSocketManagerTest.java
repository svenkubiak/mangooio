package io.mangoo.managers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.mangoo.test.MangooInstance;
import io.mangoo.utils.ConfigUtils;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;

/**
 * 
 * @author svenkubiak
 *
 */
public class WebSocketManagerTest {
    private static String eventData;
    
    @Before
    public void init() {
        eventData = null;
    }
    
    @Test
    public void testAddChannel() {
        //given
        WebSocketManager webSocketManager = MangooInstance.TEST.getInstance(WebSocketManager.class);
        WebSocketChannel channel = Mockito.mock(WebSocketChannel.class);
        
        //when
        webSocketManager.addChannel("/websocket", null, channel);
        
        //then
        assertThat(webSocketManager.getChannels("/websocket"), not(nullValue()));
        assertThat(webSocketManager.getChannels("/websocket").size(), equalTo(1));
    }
    
    @Test
    public void testRemoveChannel() {
        //given
        WebSocketManager webSocketManager = MangooInstance.TEST.getInstance(WebSocketManager.class);
        WebSocketChannel channel = Mockito.mock(WebSocketChannel.class);
        
        //when
        webSocketManager.addChannel("/websocket", null, channel);
        webSocketManager.removeChannels("/websocket");
        
        //then
        assertThat(webSocketManager.getChannels("/websocket"), not(nullValue()));
        assertThat(webSocketManager.getChannels("/websocket").size(), equalTo(0));
    }
    
    @Test
    public void testCloseChannel() throws Exception {
        //given
        WebSocketManager webSocketManager = MangooInstance.TEST.getInstance(WebSocketManager.class);
        webSocketManager.removeChannels("/websocket");
        WebSocketClientFactory factory = new WebSocketClientFactory();
        factory.start();
        String uri = "ws://" + ConfigUtils.getApplicationHost() + ":" + ConfigUtils.getApplicationPort() + "/websocket";
        
        //when
        WebSocketClient client = new WebSocketClient(factory);
        client.open(new URI(uri), new WebSocket.OnTextMessage() {
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
        
        webSocketManager.close("/websocket");
        
        //then
        assertThat(webSocketManager.getChannels("/websocket"), not(nullValue()));
        assertThat(webSocketManager.getChannels("/websocket").size(), equalTo(0));
    }
    
    @Test
    public void testSendData() throws Exception {
        //given
        WebSocketManager webSocketManager = MangooInstance.TEST.getInstance(WebSocketManager.class);
        webSocketManager.removeChannels("/websocket");
        WebSocketClientFactory factory = new WebSocketClientFactory();
        factory.start();
        String uri = "ws://" + ConfigUtils.getApplicationHost() + ":" + ConfigUtils.getApplicationPort() + "/websocket";
        String data = "Server sent data FTW!";
        
        //when
        WebSocketClient client = new WebSocketClient(factory);
        WebSocket.Connection connection = client.open(new URI(uri), new WebSocket.OnTextMessage() {
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
        
        webSocketManager.getChannels("/websocket").forEach(channel -> {
            try {
                WebSockets.sendTextBlocking(data, channel);                
            } catch (IOException e) {
                e.printStackTrace();
            }
         });

        Thread.sleep(500);
        
        //then
        assertThat(eventData, not(nullValue()));
        assertThat(eventData, equalTo(data));
        connection.close();
    }
}