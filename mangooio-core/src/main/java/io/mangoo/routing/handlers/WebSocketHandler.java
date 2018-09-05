package io.mangoo.routing.handlers;

import java.util.Objects;

import org.xnio.ChannelListener;

import io.mangoo.core.Application;
import io.mangoo.enums.Header;
import io.mangoo.enums.Required;
import io.mangoo.routing.listeners.WebSocketCloseListener;
import io.mangoo.services.WebSocketService;
import io.mangoo.utils.IOUtils;
import io.mangoo.utils.RequestUtils;
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
    private boolean hasAuthentication;
    private Class<?> controllerClass;
    
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
            
            if (RequestUtils.hasValidAuthentication(header)) {
                channel.getReceiveSetter().set((ChannelListener<? super WebSocketChannel>) Application.getInstance(this.controllerClass));
                channel.resumeReceives();
                channel.addCloseTask(Application.getInstance(WebSocketCloseListener.class));
                Application.getInstance(WebSocketService.class).addChannel(channel);
            } else {
                IOUtils.closeQuietly(channel);
            }
        } else {
            channel.getReceiveSetter().set((ChannelListener<? super WebSocketChannel>) Application.getInstance(this.controllerClass));
            channel.resumeReceives();
            channel.addCloseTask(Application.getInstance(WebSocketCloseListener.class));
            Application.getInstance(WebSocketService.class).addChannel(channel);
        }
    }
}