package io.mangoo.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Preconditions;
import com.google.inject.Singleton;

import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventConnection.EventCallback;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class ServerEventManager {
    private Map<String, Set<ServerSentEventConnection>> connections = new HashMap<String, Set<ServerSentEventConnection>>();

    /**
     * Adds a new connection to the manager
     * 
     * @param connection The connection to add
     */
    public void addConnection(ServerSentEventConnection connection) {
        Preconditions.checkNotNull(connection, "connection can not be null");
        
        String uri = connection.getRequestURI() + "?" + connection.getQueryString();
        Set<ServerSentEventConnection> uriConnections = this.connections.get(uri);
        if (uriConnections == null) {
            uriConnections = new HashSet<ServerSentEventConnection>();
            uriConnections.add(connection);
        } else {
            uriConnections.add(connection);
        }
        this.connections.put(uri, uriConnections);
    }
    
    /**
     * Retrieves all URI and their containing connection resources
     * 
     * @return A Map of URI resources and their connections or an empty map
     */
    public Map<String, Set<ServerSentEventConnection>> getConnections() {
        return this.connections;
    }

    /**
     * Retrieves all connection resources under a given URL
     * 
     * @param uri The URI resource for the connections
     * 
     * @return A Set of connections for the URI resource
     */
    public Set<ServerSentEventConnection> getConnections(String uri) {
        Preconditions.checkNotNull(uri, "uri can not be null");
        
        return this.connections.get(uri);
    }
    
    /**
     * Sends data to all connections for a given URI resource
     * 
     * @param uri The URI resource for the connection
     * @param data The event data
     */
    public void send(String uri, String data) {
        Preconditions.checkNotNull(uri, "uri can not be null");
        
        Set<ServerSentEventConnection> uriConnections = this.connections.get(uri);
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
        Preconditions.checkNotNull(uri, "uri can not be null");
        
        Set<ServerSentEventConnection> uriConnections = this.connections.get(uri);
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
        Preconditions.checkNotNull(uri, "uri can not be null");
        
        Set<ServerSentEventConnection> uriConnections = this.connections.get(uri);
        if (uriConnections != null) {
            uriConnections.forEach(connection -> IOUtils.closeQuietly(connection));
        }
    }
    
    /**
     * Closes all connections for all URIs resources
     */
    public void closeAll() {
        this.connections.entrySet().forEach(entry -> {
            entry.getValue().forEach(connection -> IOUtils.closeQuietly(connection));
        });
    }
}