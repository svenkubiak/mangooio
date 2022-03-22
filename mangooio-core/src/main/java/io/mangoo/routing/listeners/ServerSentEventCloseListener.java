package io.mangoo.routing.listeners;

import org.xnio.ChannelListener;

import io.mangoo.core.Application;
import io.mangoo.events.ServerSentEventDisconnected;
import io.mangoo.services.EventBusService;
import io.undertow.server.handlers.sse.ServerSentEventConnection;

/**
 *
 * @author svenkubiak
 *
 */
public class ServerSentEventCloseListener implements ChannelListener<ServerSentEventConnection> {
    @Override
    public void handleEvent(ServerSentEventConnection connection) {
        Application.getInstance(EventBusService.class).publish(new ServerSentEventDisconnected(connection));
    }
}