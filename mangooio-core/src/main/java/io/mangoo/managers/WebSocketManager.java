package io.mangoo.managers;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Preconditions;
import com.google.inject.Singleton;

import io.undertow.connector.PooledByteBuffer;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSocketFrameType;

/**
 *
 * @author svenkubiak
 *
 */
@Singleton
public class WebSocketManager {
    private final Map<String, Set<WebSocketChannel>> channels = new ConcurrentHashMap<>(16, 0.9f, 1);

    /**
     * Adds a new channel to the manager
     *
     * @param uri The uri
     * @param queryString The query string
     * @param channel The channel to add
     */
    public void addConnection(String uri, String queryString, WebSocketChannel channel) {
        Preconditions.checkNotNull(channel, "connection can not be null");

        String url = uri + "?" + queryString;
        Set<WebSocketChannel> uriChannels = this.channels.get(url);
        if (uriChannels == null) {
            uriChannels = new HashSet<WebSocketChannel>();
            uriChannels.add(channel);
        } else {
            uriChannels.add(channel);
        }
        this.channels.put(uri, uriChannels);
    }

    public void send(String uri, String data, WebSocketFrameType type, PooledByteBuffer pooled) {
        Preconditions.checkNotNull(uri, "uri can not be null");
        Preconditions.checkNotNull(type, "type can not be null");
        Preconditions.checkNotNull(type, "type can not be null");

        Set<WebSocketChannel> uriChannels = this.channels.get(uri);
        if (uriChannels != null) {
            uriChannels.forEach(channel -> {
                if (channel.isOpen()) {
                    try {
                        channel.send(type).send(pooled);
                    } catch (Exception e) {
                        //intentionally left blank
                    }
                }
            });
        }
    }

    /**
     * Retrieves all URI and their containing channel resources
     *
     * @return A Map of URI resources and their channels or an empty map
     */
    public Map<String, Set<WebSocketChannel>> getChannels() {
        return this.channels;
    }

    /**
     * Retrieves all channels resources under a given URL
     *
     * @param uri The URI resource for the connections
     *
     * @return A Set of channels for the URI resource
     */
    public Set<WebSocketChannel> getChannels(String uri) {
        Preconditions.checkNotNull(uri, "uri can not be null");

        return this.channels.get(uri);
    }

    /**
     * Closes all channels for a given URI resource
     *
     * @param uri The URI resource for the connection
     */
    public void close(String uri) {
        Preconditions.checkNotNull(uri, "uri can not be null");

        Set<WebSocketChannel> uriChannels = this.channels.get(uri);
        if (uriChannels != null) {
            uriChannels.forEach(connection -> IOUtils.closeQuietly(connection));
        }
    }

    /**
     * Closes all channels for all URIs resources
     */
    public void closeAll() {
        this.channels.entrySet().forEach(entry -> entry.getValue().forEach(channel -> IOUtils.closeQuietly(channel)));
    }
}