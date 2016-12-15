package io.mangoo.routing.handlers;

import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.xnio.ChannelListener;

import io.mangoo.core.Application;
import io.mangoo.enums.Required;
import io.mangoo.managers.WebSocketManager;
import io.mangoo.routing.listeners.WebSocketCloseListener;
import io.mangoo.utils.RequestUtils;
import io.undertow.util.Headers;
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
    private final boolean hasAuthentication;
    private final Class<?> controllerClass;
    
    public WebSocketHandler(Class<?> controllerClass, boolean hasAuthentication) {
        this.controllerClass = Objects.requireNonNull(controllerClass, Required.CONTROLLER_CLASS.toString());
        this.hasAuthentication = hasAuthentication;
    }

    @Override
    public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
        if (this.hasAuthentication) {
            String header = null;
            if (exchange.getRequestHeader(Headers.COOKIE_STRING) != null) {
                header = exchange.getRequestHeader(Headers.COOKIE_STRING);
            }

            if (RequestUtils.hasValidAuthentication(header)) {
                channel.getReceiveSetter().set((ChannelListener<? super WebSocketChannel>) Application.getInstance(this.controllerClass));
                channel.resumeReceives();
                channel.addCloseTask(Application.getInstance(WebSocketCloseListener.class));
                Application.getInstance(WebSocketManager.class).addChannel(channel);
            } else {
                IOUtils.closeQuietly(channel);
            }
        } else {
            channel.getReceiveSetter().set((ChannelListener<? super WebSocketChannel>) Application.getInstance(this.controllerClass));
            channel.resumeReceives();
            channel.addCloseTask(Application.getInstance(WebSocketCloseListener.class));
            Application.getInstance(WebSocketManager.class).addChannel(channel);
        }
    }
}