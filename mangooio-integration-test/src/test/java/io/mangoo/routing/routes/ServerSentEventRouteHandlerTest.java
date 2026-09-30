package io.mangoo.routing.routes;

import com.launchdarkly.eventsource.EventSource;
import com.launchdarkly.eventsource.MessageEvent;
import com.launchdarkly.eventsource.ReadyState;
import com.launchdarkly.eventsource.background.BackgroundEventHandler;
import com.launchdarkly.eventsource.background.BackgroundEventSource;
import handlers.ClientServerSentEventHandler;
import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.manager.ServerSentEventManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

@ExtendWith({TestExtension.class})
class ServerSentEventRouteHandlerTest {
    private static final String DEFAULT_URL = "/sse";
    private static final String CLIENT_URL = "/sse/client";

    /**
     * Records every event of a single connection, so that a targeted send can be told
     * apart from a broadcast
     */
    private static class RecordingEventHandler implements BackgroundEventHandler {
        private final List<String> events = new CopyOnWriteArrayList<>();

        @Override
        public void onOpen() {
            // Nothing to do here
        }

        @Override
        public void onClosed() {
            // Nothing to do here
        }

        @Override
        public void onMessage(String event, MessageEvent messageEvent) {
            events.add(messageEvent.getData());
        }

        @Override
        public void onComment(String comment) {
            // Nothing to do here
        }

        @Override
        public void onError(Throwable t) {
            // Nothing to do here
        }
    }

    private String url(String path) {
        Config config = Application.getInstance(Config.class);

        return "http://" + config.getConnectorHttpHost() + ":" + config.getConnectorHttpPort() + path;
    }

    private String clientUrl(String client) {
        return CLIENT_URL + "?" + ClientServerSentEventHandler.PARAMETER + "=" + client;
    }

    private BackgroundEventSource connect(String path, RecordingEventHandler eventHandler) {
        var backgroundEventSource = new BackgroundEventSource.Builder(eventHandler,
                new EventSource.Builder(URI.create(url(path)))).build();

        backgroundEventSource.start();
        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(backgroundEventSource.getEventSource().getState(), equalTo(ReadyState.OPEN)));

        return backgroundEventSource;
    }

    @Test
    void testRouteWithHandlerUsesThatHandler() {
        //given
        var manager = Application.getInstance(ServerSentEventManager.class);
        var client = UUID.randomUUID().toString();
        var eventHandler = new RecordingEventHandler();

        //when
        try (BackgroundEventSource eventSource = connect(clientUrl(client), eventHandler)) {
            var data = UUID.randomUUID().toString();
            manager.send(client, data);

            //then the custom handler held the connection under the client key
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(eventHandler.events, hasItem(data)));

            //when
            var byUrl = UUID.randomUUID().toString();
            manager.send(CLIENT_URL, byUrl);

            //then and not under the request URI, as the default handler would have
            assertThat(eventHandler.events, not(hasItem(byUrl)));
        }
    }

    @Test
    void testCustomHandlerSendsToASingleClient() {
        //given
        var manager = Application.getInstance(ServerSentEventManager.class);
        var first = UUID.randomUUID().toString();
        var second = UUID.randomUUID().toString();
        var firstEventHandler = new RecordingEventHandler();
        var secondEventHandler = new RecordingEventHandler();

        //when
        try (BackgroundEventSource firstEventSource = connect(clientUrl(first), firstEventHandler);
             BackgroundEventSource secondEventSource = connect(clientUrl(second), secondEventHandler)) {

            var data = UUID.randomUUID().toString();
            manager.send(first, data);

            //then only the addressed client receives the event
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(firstEventHandler.events, hasItem(data)));
            assertThat(secondEventHandler.events, not(hasItem(data)));
        }
    }

    @Test
    void testRouteWithoutHandlerStillBroadcasts() {
        //given
        var firstEventHandler = new RecordingEventHandler();
        var secondEventHandler = new RecordingEventHandler();

        //when
        try (BackgroundEventSource firstEventSource = connect(DEFAULT_URL, firstEventHandler);
             BackgroundEventSource secondEventSource = connect(DEFAULT_URL, secondEventHandler)) {

            var data = UUID.randomUUID().toString();
            Application.getInstance(ServerSentEventManager.class).send(DEFAULT_URL, data);

            //then every connection of that URL receives the event
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                assertThat(firstEventHandler.events, hasItem(data));
                assertThat(secondEventHandler.events, hasItem(data));
            });
        }
    }

    @Test
    void testBothRouteVariantsWorkSideBySide() {
        //given
        var client = UUID.randomUUID().toString();
        var defaultEventHandler = new RecordingEventHandler();
        var clientEventHandler = new RecordingEventHandler();

        //when
        try (BackgroundEventSource defaultEventSource = connect(DEFAULT_URL, defaultEventHandler);
             BackgroundEventSource clientEventSource = connect(clientUrl(client), clientEventHandler)) {

            var broadcast = UUID.randomUUID().toString();
            Application.getInstance(ServerSentEventManager.class).send(DEFAULT_URL, broadcast);

            //then the broadcast of the default route does not reach the custom route
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(defaultEventHandler.events, hasItem(broadcast)));
            assertThat(clientEventHandler.events, not(hasItem(broadcast)));

            //when
            var targeted = UUID.randomUUID().toString();
            Application.getInstance(ServerSentEventManager.class).send(client, targeted);

            //then the targeted send does not reach the default route
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(clientEventHandler.events, hasItem(targeted)));
            assertThat(defaultEventHandler.events, not(hasItem(targeted)));
        }
    }
}
