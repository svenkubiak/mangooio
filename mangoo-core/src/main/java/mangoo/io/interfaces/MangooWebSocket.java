package mangoo.io.interfaces;

import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedBinaryMessage;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.CloseMessage;
import io.undertow.websockets.core.WebSocketChannel;

/**
 *
 * @author svenkubiak
 *
 */
public abstract class MangooWebSocket extends AbstractReceiveListener {
    @Override
    protected abstract void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message);

    @Override
    protected abstract void onFullBinaryMessage(WebSocketChannel channel, BufferedBinaryMessage message);

    @Override
    protected abstract void onFullPongMessage(WebSocketChannel channel, BufferedBinaryMessage message);

    @Override
    protected abstract void onCloseMessage(CloseMessage closeMessage, WebSocketChannel channel);
}