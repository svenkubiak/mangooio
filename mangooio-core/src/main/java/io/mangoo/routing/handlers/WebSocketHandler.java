package io.mangoo.routing.handlers;

import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.xnio.ChannelListener;

import com.google.inject.Inject;

import io.mangoo.core.Application;
import io.mangoo.enums.Header;
import io.mangoo.enums.Required;
import io.mangoo.helpers.RequestHelper;
import io.mangoo.managers.WebSocketManager;
import io.mangoo.routing.listeners.WebSocketCloseListener;
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
    private final RequestHelper requestHelper;
    private boolean hasAuthentication;
    private Class<?> controllerClass;
    
    @Inject
    public WebSocketHandler(RequestHelper requestHelper) {
        this.requestHelper = Objects.requireNonNull(requestHelper, Required.REQUEST_HELPER.toString());
    }
    
    public WebSocketHandler withAuthentication(boolean hasAuthentication) {
        this.hasAuthentication = hasAuthentication;
        
        return this;
    }
    
    public WebSocketHandler withControllerClass(Class<?> controllerClass) {
        this.controllerClass = Objects.requireNonNull(controllerClass, Required.CONTROLLER_CLASS.toString());
        
        return this;
    }

    @Override
    public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
        if (this.hasAuthentication) {
            String header = null;
            if (exchange.getRequestHeader(Header.COOKIE.toString()) != null) {
                header = exchange.getRequestHeader(Header.COOKIE.toString());
            }

            if (this.requestHelper.hasValidAuthentication(header)) {
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