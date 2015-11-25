package io.mangoo.managers;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.mangoo.cache.Cache;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventConnection.EventCallback;

/**
 *
 * @author svenkubiak
 *
 */
@Singleton
public class ServerEventManager {
    private static final String URI_ERROR = "uri can not be null";
    private static final String PREFIX = "MANGOOIO-SSE-";

    @Inject
    private Cache cache;

    /**
     * Adds a new connection to the manager
     *
     * @param connection The connection to put
     */
    public void addConnection(ServerSentEventConnection connection) {
        Preconditions.checkNotNull(connection, "connection can not be null");

        String uri = connection.getRequestURI();
        if (StringUtils.isNotBlank(connection.getQueryString())) {
            uri = uri + "?" + connection.getQueryString();
        }

        Set<ServerSentEventConnection> uriConnections = getConnections(uri);
        if (uriConnections == null) {
            uriConnections = new HashSet<>();
            uriConnections.add(connection);
        } else {
            uriConnections.add(connection);
        }
        setConnections(uri, uriConnections);
    }

    /**
     * Sends data to all connections for a given URI resource
     *
     * @param uri The URI resource for the connection
     * @param data The event data
     */
    public void send(String uri, String data) {
        Preconditions.checkNotNull(uri, URI_ERROR);

        Set<ServerSentEventConnection> uriConnections = getConnections(uri);
        if (uriConnections != null) {
            uriConnections.forEach(connection -> {
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
        Preconditions.checkNotNull(uri, URI_ERROR);
        Preconditions.checkNotNull(eventCallback, "eventCallback can not be null");

        Set<ServerSentEventConnection> uriConnections = getConnections(uri);
        if (uriConnections != null) {
            uriConnections.forEach(connection -> {
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
        Preconditions.checkNotNull(uri, URI_ERROR);

        Set<ServerSentEventConnection> uriConnections = getConnections(uri);
        if (uriConnections != null) {
            uriConnections.forEach(connection -> {
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
        Preconditions.checkNotNull(uri, URI_ERROR);

        Set<ServerSentEventConnection> uriConnections = this.cache.get(PREFIX + uri);

        return (uriConnections == null) ? new HashSet<>() : uriConnections;
    }

    /**
     * Sets the URI resources for a given URL
     *
     * @param uri The URI resource for the connection
     * @param uriConnections The connections for the URI resource
     */
    public void setConnections(String uri, Set<ServerSentEventConnection> uriConnections) {
        Preconditions.checkNotNull(uri, URI_ERROR);
        Preconditions.checkNotNull(uriConnections, "uriConnections can not be null");

        this.cache.put(PREFIX + uri, uriConnections);
    }

    /**
     * Removes all URI resources for a given URL
     *
     * @param uri The URI resource for the connection
     */
    public void removeConnections(String uri) {
        Preconditions.checkNotNull(uri, URI_ERROR);

        this.cache.remove(PREFIX + uri);
    }
}