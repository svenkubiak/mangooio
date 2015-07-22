package io.mangoo.controllers;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.junit.Before;
import org.junit.Test;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.Default;
import io.mangoo.enums.Key;

/**
 * 
 * @author svenkubiak
 *
 */
public class WebSocketControllerTest {
    private static String host;
    private static int port;
    
    @Before
    public void init() {
        Application.getInjector();
        Config config = Application.getInjector().getInstance(Config.class);
        
        host = config.getString(Key.APPLICATION_HOST, Default.APPLICATION_HOST.toString());
        port = config.getInt(Key.APPLICATION_PORT, Default.APPLICATION_PORT.toInt());
    }
  
    @Test
    public void testStartAndBuild() throws Exception {
        WebSocketClientFactory factory = new WebSocketClientFactory();
        factory.start();
        
        WebSocketClient client = new WebSocketClient(factory);
        
        WebSocket.Connection connection = client.open(new URI("ws://" + host + ":" + port + "/websocket"), new WebSocket.OnTextMessage() {
            public void onOpen(Connection connection) {
               // open notification
             }
        
             public void onClose(int closeCode, String message) {
               // close notification
             }
        
             public void onMessage(String data) {
               // handle incoming message
             }
         }).get(5, TimeUnit.SECONDS);
        
        connection.sendMessage("Hello World");
        assertThat(connection, is(notNullValue()));
        connection.close();
    }
}