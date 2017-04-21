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
import io.undertow.websockets.core.WebSocketChannel;

/**
 *
 * @author svenkubiak
 *
 */
@Singleton
public class WebSocketCloseListener implements ChannelListener<WebSocketChannel> {
    private final Cache cache;
    private final RequestHelper requestHelper;
    
    @Inject
    private WebSocketCloseListener(CacheProvider cacheProvider, RequestHelper requestHelper) {
        Objects.requireNonNull(cacheProvider, Required.CACHE_PROVIDER.toString());
        Objects.requireNonNull(cacheProvider, Required.REQUEST_HELPER.toString());
        
        this.cache = cacheProvider.getCache(CacheName.WSS);
        this.requestHelper = requestHelper;
    }

    @Override
    public void handleEvent(WebSocketChannel channel) {
        final String url = this.requestHelper.getWebSocketURL(channel);
        final Set<WebSocketChannel> channels = this.cache.get(Default.WSS_CACHE_PREFIX.toString() + url);
        if (channels != null) {
            channels.remove(channel);
            this.cache.put(Default.WSS_CACHE_PREFIX.toString() + url, channels);
        }
    }
}