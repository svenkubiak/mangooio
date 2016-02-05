package io.mangoo.routing.listeners;

import java.util.Set;

import javax.inject.Singleton;

import org.xnio.ChannelListener;

import io.mangoo.cache.Cache;
import io.mangoo.enums.Default;
import io.mangoo.utils.MangooUtils;
import io.mangoo.utils.RequestUtils;
import io.undertow.websockets.core.WebSocketChannel;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class WebSocketCloseListener implements ChannelListener<WebSocketChannel> {
    private Cache cache = MangooUtils.getInternalCache();
    
    @Override
    public void handleEvent(WebSocketChannel channel) {
        String url = RequestUtils.getWebSocketURL(channel);
        Set<WebSocketChannel> channels = this.cache.get(Default.WSS_CACHE_PREFIX.toString() + url);
        if (channels != null) {
            channels.remove(channel);
            this.cache.put(Default.WSS_CACHE_PREFIX.toString() + url, channels);            
        }
    }
}