package io.mangoo.managers;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.mangoo.cache.Cache;
import io.mangoo.enums.Default;
import io.mangoo.utils.RequestUtils;
import io.undertow.websockets.core.WebSocketChannel;

/**
 *
 * @author svenkubiak
 *
 */
@Singleton
public class WebSocketManager {
    private static final String URI_ERROR = "uri can not be null";
    private final Cache cache;
        
    @Inject
    public WebSocketManager(Cache cache) {
    	this.cache = Objects.requireNonNull(cache, "cache ca not be null");
    }

    /**
     * Adds a new channel to the manager
     *
     * @param channel channel The channel to put
     */
    public void addChannel(WebSocketChannel channel) {
        Objects.requireNonNull(channel, "channel can not be null");

        String url = RequestUtils.getWebSocketURL(channel);
        Set<WebSocketChannel> channels = getChannels(url);
        if (channels == null) {
            channels = new HashSet<>();
            channels.add(channel);
        } else {
            channels.add(channel);
        }
        setChannels(url, channels);
    }

    /**
     * Sets the URI resources for a given URL
     *
     * @param uri The URI resource for the connection
     * @param channels The channels for the URI resource
     */
    public void setChannels(String uri, Set<WebSocketChannel> channels) {
        Objects.requireNonNull(uri, URI_ERROR);
        Objects.requireNonNull(channels, "uriConnections can not be null");

        this.cache.put(Default.WSS_CACHE_PREFIX.toString() + uri, channels);
    }

    /**
     * Retrieves all channels under a given URL
     *
     * @param uri The URI resource for the channels
     *
     * @return A Set of channels for the URI resource
     */
    public Set<WebSocketChannel> getChannels(String uri) {
        Objects.requireNonNull(uri, URI_ERROR);

        Set<WebSocketChannel> channels = this.cache.get(Default.WSS_CACHE_PREFIX.toString() + uri);

        return (channels == null) ? new HashSet<>() : channels;
    }

    /**
     * Removes all URI resources for a given URL
     *
     * @param uri The URI resource for the connection
     */
    public void removeChannels(String uri) {
        Objects.requireNonNull(uri, URI_ERROR);

        this.cache.remove(Default.WSS_CACHE_PREFIX.toString() + uri);
    }

    /**
     * Closes all connections for a given URI resource
     *
     * @param uri The URI resource for the connection
     */
    public void close(String uri) {
        Objects.requireNonNull(uri, URI_ERROR);

        Set<WebSocketChannel> channels = getChannels(uri);
        if (channels != null) {
            channels.forEach(channel -> {
                if (channel.isOpen()) {
                    IOUtils.closeQuietly(channel);
                }
            });
            removeChannels(uri);
        }
    }
}