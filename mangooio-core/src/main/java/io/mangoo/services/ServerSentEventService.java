package io.mangoo.services;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.mangoo.cache.Cache;
import io.mangoo.enums.CacheName;
import io.mangoo.enums.Default;
import io.mangoo.enums.Required;
import io.mangoo.providers.CacheProvider;
import io.mangoo.utils.IOUtils;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventConnection.EventCallback;

/**
 *
 * @author svenkubiak
 *
 */
@Singleton
public class ServerSentEventService {
    private final Cache cache;
    
    @Inject
    private ServerSentEventService(CacheProvider cacheProvider) {
        Objects.requireNonNull(cacheProvider, Required.CACHE_PROVIDER.toString());
        
        this.cache = cacheProvider.getCache(CacheName.SSE);
    }

    /**
     * Adds a new connection to the manager
     *
     * @param connection The connection to put
     */
    @SuppressWarnings("all")
    public void addConnection(ServerSentEventConnection connection) {
        Objects.requireNonNull(connection, Required.CONNECTION.toString());

        final String url = RequestUtils.getServerSentEventURL(connection);
        Set<ServerSentEventConnection> uriConnections = getConnections(url);
        if (uriConnections == null) {
            uriConnections = new HashSet<>();
            uriConnections.add(connection);
        } else {
            uriConnections.add(connection);
        }
        setConnections(url, uriConnections);
    }

    /**
     * Sends data to all connections for a given URI resource
     *
     * @param uri The URI resource for the connection
     * @param data The event data
     */
    public void send(String uri, String data) {
        Objects.requireNonNull(uri, Required.URI.toString());

        final Set<ServerSentEventConnection> uriConnections = getConnections(uri);
        if (uriConnections != null) {
            uriConnections.forEach((ServerSentEventConnection connection) -> {
                if (connection.isOpen()) {
                    connection.send(data);
                }
            });
        }
    }

    /**
     * Sends data to all connections for a given URI and invokes the callback
     * on Success or failure
     *
     * @param uri The URI resource for the connection
     * @param data The event data
     * @param eventCallback A callback that is notified on Success or failure
     */
    public void send(String uri, String data, EventCallback eventCallback) {
        Objects.requireNonNull(uri, Required.URI.toString());
        Objects.requireNonNull(eventCallback, Required.EVENT_CALLBACK.toString());

        final Set<ServerSentEventConnection> uriConnections = getConnections(uri);
        if (uriConnections != null) {
            uriConnections.forEach((ServerSentEventConnection connection) -> {
                if (connection.isOpen()) {
                    connection.send(data, eventCallback);
                }
            });
        }
    }

    /**
     * Closes all connections for a given URI resource
     *
     * @param uri The URI resource for the connection
     */
    public void close(String uri) {
        Objects.requireNonNull(uri, Required.URI.toString());

        final Set<ServerSentEventConnection> uriConnections = getConnections(uri);
        if (uriConnections != null) {
            uriConnections.forEach((ServerSentEventConnection connection) -> {
                if (connection.isOpen()){
                    IOUtils.closeQuietly(connection);
                }
           });
           removeConnections(uri);
        }
    }

    /**
     * Retrieves all connection resources under a given URL
     *
     * @param uri The URI resource for the connections
     *
     * @return A Set of connections for the URI resource
     */
    public Set<ServerSentEventConnection> getConnections(String uri) {
        Objects.requireNonNull(uri, Required.URI.toString());

        final Set<ServerSentEventConnection> uriConnections = this.cache.get(Default.SSE_CACHE_PREFIX.toString() + uri);

        return (uriConnections == null) ? new HashSet<>() : uriConnections;
    }

    /**
     * Sets the URI resources for a given URL
     *
     * @param uri The URI resource for the connection
     * @param uriConnections The connections for the URI resource
     */
    public void setConnections(String uri, Set<ServerSentEventConnection> uriConnections) {
        Objects.requireNonNull(uri, Required.URI.toString());
        Objects.requireNonNull(uriConnections, Required.URI_CONNECTIONS.toString());

        this.cache.put(Default.SSE_CACHE_PREFIX.toString() + uri, uriConnections);
    }

    /**
     * Removes all URI resources for a given URL
     *
     * @param uri The URI resource for the connection
     */
    public void removeConnections(String uri) {
        Objects.requireNonNull(uri, Required.URI.toString());

        this.cache.remove(Default.SSE_CACHE_PREFIX.toString() + uri);
    }
}