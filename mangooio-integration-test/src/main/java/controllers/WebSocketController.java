package controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.mangoo.interfaces.MangooWebSocket;
import io.undertow.websockets.core.BufferedBinaryMessage;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.CloseMessage;
import io.undertow.websockets.core.WebSocketChannel;

@SuppressWarnings("all")
public class WebSocketController extends MangooWebSocket {
    private static final Logger LOG = LogManager.getLogger(WebSocketController.class);
    
    @Override
    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
        LOG.info(message.toString());
    }

    @Override
    protected void onFullBinaryMessage(WebSocketChannel channel, BufferedBinaryMessage message) {
        LOG.info(message.toString());
    }

    @Override
    protected void onFullPongMessage(WebSocketChannel channel, BufferedBinaryMessage message) {
        LOG.info(message.toString());
    }

    @Override
    protected void onCloseMessage(CloseMessage closeMessage,  WebSocketChannel channel) {
        LOG.info(closeMessage.toString());
    }
}