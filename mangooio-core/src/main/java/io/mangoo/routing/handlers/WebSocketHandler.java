package io.mangoo.routing.handlers;

import org.xnio.ChannelListener;

import com.google.common.base.Preconditions;

import io.mangoo.core.Application;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.spi.WebSocketHttpExchange;

/**
 *
 * @author svenkubiak
 *
 */
@SuppressWarnings("unchecked")
public class WebSocketHandler implements WebSocketConnectionCallback {
    private Class<?> controllerClass;

    public WebSocketHandler(Class<?> controllerClass) {
        Preconditions.checkNotNull(controllerClass, "controllerClass can not be null");
        
        this.controllerClass = controllerClass;
    }

    @Override
    public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
        channel.getReceiveSetter().set((ChannelListener<? super WebSocketChannel>) Application.getInstance(this.controllerClass));
        channel.resumeReceives();
    }
}