package io.mangoo.routing.listeners;

import java.util.Objects;
import java.util.Set;

import javax.inject.Singleton;

import org.xnio.ChannelListener;

import com.google.inject.Inject;

import io.mangoo.cache.Cache;
import io.mangoo.enums.Default;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.handlers.sse.ServerSentEventConnection;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class ServerSentEventCloseListener implements ChannelListener<ServerSentEventConnection> {
	private Cache cache;

	@Inject
	public ServerSentEventCloseListener(Cache cache) {
		this.cache = Objects.requireNonNull(cache, "cache can not be null");
	}
	
	@Override
	public void handleEvent(ServerSentEventConnection connection) {
		String uri = RequestUtils.getServerSentEventURL(connection);
		Set<ServerSentEventConnection> connections = this.cache.get(Default.SSE_CACHE_PREFIX.toString() + uri);
		if (connections != null) {
			connections.remove(connection);
			this.cache.put(Default.SSE_CACHE_PREFIX.toString() + uri, connections);
		}
	}
}