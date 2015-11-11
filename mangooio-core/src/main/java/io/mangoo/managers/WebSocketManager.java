package io.mangoo.managers;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.mangoo.cache.Cache;
import io.undertow.websockets.core.WebSocketChannel;

/**
 *
 * @author svenkubiak
 *
 */
@Singleton
public class WebSocketManager {
    private static final String URI_ERROR = "uri can not be null";
    private static final String PREFIX = "MANGOOIO-WS-";

    @Inject
    private Cache cache;
    
    /**
     * Adds a new channel to the manager
     * 
     * @param uri The uri of the request
     * @param queryString The query string of the request
     * @param channel channel The channel to add
     */
    public void addChannel(String uri, String queryString, WebSocketChannel channel) {
        Preconditions.checkNotNull(channel, "channel can not be null");

        if (StringUtils.isNotBlank(queryString)) {
            uri = uri + "?" + queryString;
        }
        
        Set<WebSocketChannel> channels = getChannels(uri);
        if (channels == null) {
            channels = new HashSet<>();
            channels.add(channel);
        } else {
            channels.add(channel);
        }
        setChannels(uri, channels);
    }

    /**
     * Sets the URI resources for a given URL
     * 
     * @param uri The URI resource for the connection
     * @param channels The channels for the URI resource
     */
    public void setChannels(String uri, Set<WebSocketChannel> channels) {
        Preconditions.checkNotNull(uri, URI_ERROR);
        Preconditions.checkNotNull(channels, "uriConnections can not be null");
        
        this.cache.add(PREFIX + uri, channels);
    }

    /**
     * Retrieves all channels under a given URL
     *
     * @param uri The URI resource for the channels
     *
     * @return A Set of channels for the URI resource
     */
    public Set<WebSocketChannel> getChannels(String uri) {
        Preconditions.checkNotNull(uri, URI_ERROR);

        Set<WebSocketChannel> channels = this.cache.get(PREFIX + uri);
        
        return (channels == null) ? new HashSet<>() : channels;
    }

    /**
     * Removes all URI resources for a given URL
     * 
     * @param uri The URI resource for the connection
     */
    public void removeChannels(String uri) {
        Preconditions.checkNotNull(uri, URI_ERROR);
        
        this.cache.remove(PREFIX + uri);
    }

    /**
     * Closes all connections for a given URI resource
     *
     * @param uri The URI resource for the connection
     */
    public void close(String uri) {
        Preconditions.checkNotNull(uri, URI_ERROR);

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