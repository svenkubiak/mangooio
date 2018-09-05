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
import io.undertow.websockets.core.WebSocketChannel;

/**
 *
 * @author svenkubiak
 *
 */
@Singleton
public class WebSocketCloseListener implements ChannelListener<WebSocketChannel> {
    private final Cache cache;
    
    @Inject
    private WebSocketCloseListener(CacheProvider cacheProvider) {
        Objects.requireNonNull(cacheProvider, Required.CACHE_PROVIDER.toString());
        
        this.cache = cacheProvider.getCache(CacheName.WSS);
    }

    @Override
    public void handleEvent(WebSocketChannel channel) {
        final String url = RequestUtils.getWebSocketURL(channel);
        final Set<WebSocketChannel> channels = this.cache.get(Default.WSS_CACHE_PREFIX.toString() + url);
        if (channels != null) {
            channels.remove(channel);
            this.cache.put(Default.WSS_CACHE_PREFIX.toString() + url, channels);
        }
    }
}