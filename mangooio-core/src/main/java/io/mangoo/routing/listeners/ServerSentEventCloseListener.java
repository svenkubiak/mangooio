package io.mangoo.routing.listeners;

import java.util.Objects;
import java.util.Set;

import javax.inject.Singleton;

import org.xnio.ChannelListener;

import com.google.inject.Inject;

import io.mangoo.cache.Cache;
import io.mangoo.enums.CacheName;
import io.mangoo.enums.Default;
import io.mangoo.enums.Required;
import io.mangoo.helpers.RequestHelper;
import io.mangoo.providers.CacheProvider;
import io.undertow.server.handlers.sse.ServerSentEventConnection;

/**
 *
 * @author svenkubiak
 *
 */
@Singleton
public class ServerSentEventCloseListener implements ChannelListener<ServerSentEventConnection> {
    private final Cache cache;
    private final RequestHelper requestHelper;
    
    @Inject
    private ServerSentEventCloseListener(CacheProvider cacheProvider, RequestHelper requestHelper) {
        Objects.requireNonNull(cacheProvider, Required.CACHE_PROVIDER.toString());
        Objects.requireNonNull(cacheProvider, Required.REQUEST_HELPER.toString());
        
        this.cache = cacheProvider.getCache(CacheName.SSE);
        this.requestHelper = requestHelper;
    }

    @Override
    public void handleEvent(ServerSentEventConnection connection) {
        final String uri = this.requestHelper.getServerSentEventURL(connection);
        final Set<ServerSentEventConnection> connections = this.cache.get(Default.SSE_CACHE_PREFIX.toString() + uri);
        if (connections != null) {
            connections.remove(connection);
            this.cache.put(Default.SSE_CACHE_PREFIX.toString() + uri, connections);
        }
    }
}