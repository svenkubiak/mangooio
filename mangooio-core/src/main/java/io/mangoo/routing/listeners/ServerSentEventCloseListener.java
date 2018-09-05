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
import io.mangoo.providers.CacheProvider;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.handlers.sse.ServerSentEventConnection;

/**
 *
 * @author svenkubiak
 *
 */
@Singleton
public class ServerSentEventCloseListener implements ChannelListener<ServerSentEventConnection> {
    private final Cache cache;
    
    @Inject
    private ServerSentEventCloseListener(CacheProvider cacheProvider) {
        Objects.requireNonNull(cacheProvider, Required.CACHE_PROVIDER.toString());
        
        this.cache = cacheProvider.getCache(CacheName.SSE);
    }

    @Override
    public void handleEvent(ServerSentEventConnection connection) {
        final String uri = RequestUtils.getServerSentEventURL(connection);
        final Set<ServerSentEventConnection> connections = this.cache.get(Default.SSE_CACHE_PREFIX.toString() + uri);
        if (connections != null) {
            connections.remove(connection);
            this.cache.put(Default.SSE_CACHE_PREFIX.toString() + uri, connections);
        }
    }
}