package io.mangoo.services;

import com.launchdarkly.eventsource.MessageEvent;
import com.launchdarkly.eventsource.background.BackgroundEventHandler;

public class SimpleEventHandler implements BackgroundEventHandler {
    @Override
    public void onOpen() throws Exception {
    }

    @Override
    public void onClosed() throws Exception {
    }

    @Override
    public void onMessage(String event, MessageEvent messageEvent) throws Exception {
        EventData.data = messageEvent.getData();
    }

    @Override
    public void onComment(String comment) throws Exception {
    }

    @Override
    public void onError(Throwable t) {
    }
}