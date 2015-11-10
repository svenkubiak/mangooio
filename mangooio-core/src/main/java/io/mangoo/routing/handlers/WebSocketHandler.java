package io.mangoo.routing.handlers;

import org.apache.commons.io.IOUtils;
import org.xnio.ChannelListener;

import com.google.common.base.Preconditions;

import io.mangoo.core.Application;
import io.mangoo.managers.WebSocketManager;
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
    private final boolean requiresAuthentication;
    private final Class<?> controllerClass;

    public WebSocketHandler(Class<?> controllerClass, boolean requiresAuthentication) {
        Preconditions.checkNotNull(controllerClass, "controllerClass can not be null");

        this.controllerClass = controllerClass;
        this.requiresAuthentication = requiresAuthentication;
    }

    @Override
    public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
        String uri = exchange.getRequestURI();
        String queryString = exchange.getQueryString();

        if (this.requiresAuthentication) {
            String header = null;
            if (exchange.getRequestHeader(Headers.SET_COOKIE_STRING) != null) {
                header = exchange.getRequestHeader(Headers.SET_COOKIE_STRING);
            } 

            if (RequestUtils.hasValidAuthentication(header)) {
                channel.getReceiveSetter().set((ChannelListener<? super WebSocketChannel>) Application.getInstance(this.controllerClass));
                channel.resumeReceives();
                Application.getInstance(WebSocketManager.class).addChannel(uri, queryString, channel);
            } else {
                IOUtils.closeQuietly(channel);
            }
        }

        channel.getReceiveSetter().set((ChannelListener<? super WebSocketChannel>) Application.getInstance(this.controllerClass));
        channel.resumeReceives();
        Application.getInstance(WebSocketManager.class).addChannel(uri, queryString, channel);
    }
}