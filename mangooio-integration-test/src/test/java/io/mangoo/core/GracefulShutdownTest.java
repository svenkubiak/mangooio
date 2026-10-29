package io.mangoo.core;

import io.mangoo.manager.ServerSentEventManager;
import io.mangoo.routing.handlers.HealthHandler;
import io.mangoo.routing.handlers.ServerSentEventHandler;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.GracefulShutdownHandler;
import io.undertow.util.StatusCodes;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;

class GracefulShutdownTest {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private static Undertow start(HttpHandler handler) {
        var undertow = Undertow.builder()
                .addHttpListener(0, "localhost")
                .setHandler(handler)
                .build();
        undertow.start();

        return undertow;
    }

    private static URI uri(Undertow undertow, String scheme, String path) {
        var address = (InetSocketAddress) undertow.getListenerInfo().getFirst().getAddress();
        return URI.create(scheme + "://localhost:" + address.getPort() + path);
    }

    private static HttpResponse<String> get(Undertow undertow, String path) throws Exception {
        return CLIENT.send(HttpRequest.newBuilder(uri(undertow, "http", path)).build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void testOpenServerSentEventConnectionHoldsDrainUntilClosed() throws Exception {
        //given
        var manager = new ServerSentEventManager();
        var connected = new CountDownLatch(1);
        var graceful = new GracefulShutdownHandler(ServerSentEventHandler.wrap((connection, lastEventId) -> {
            manager.addConnection("/sse", connection);
            connected.countDown();
        }));
        var undertow = start(graceful);

        try {
            CompletableFuture<HttpResponse<InputStream>> stream = CLIENT.sendAsync(
                    HttpRequest.newBuilder(uri(undertow, "http", "/sse")).build(), HttpResponse.BodyHandlers.ofInputStream());
            assertThat(connected.await(5, TimeUnit.SECONDS), equalTo(true));

            //when
            graceful.shutdown();
            boolean drainedWhileOpen = graceful.awaitShutdown(500);
            manager.closeAll();
            boolean drainedAfterClose = graceful.awaitShutdown(5000);

            //then
            assertThat(drainedWhileOpen, equalTo(false));
            assertThat(drainedAfterClose, equalTo(true));
            stream.cancel(true);
        } finally {
            undertow.stop();
        }
    }

    @Test
    void testNewRequestIsRejectedAfterShutdown() throws Exception {
        //given
        var graceful = new GracefulShutdownHandler(exchange -> exchange.setStatusCode(StatusCodes.OK));
        var undertow = start(graceful);

        try {
            int before = get(undertow, "/").statusCode();

            //when
            graceful.shutdown();
            int after = get(undertow, "/").statusCode();

            //then
            assertThat(before, equalTo(StatusCodes.OK));
            assertThat(after, equalTo(StatusCodes.SERVICE_UNAVAILABLE));
        } finally {
            undertow.stop();
        }
    }

    @Test
    void testHealthIsAnsweredOutsideOfTheGracefulShutdown() throws Exception {
        //given
        var graceful = new GracefulShutdownHandler(exchange -> exchange.setStatusCode(StatusCodes.OK));
        var undertow = start(new HealthHandler(graceful, () -> false));

        try {
            //when
            graceful.shutdown();

            //then
            assertThat(get(undertow, "/@health/live").statusCode(), equalTo(StatusCodes.OK));
            assertThat(get(undertow, "/@health/ready").statusCode(), equalTo(StatusCodes.SERVICE_UNAVAILABLE));
            assertThat(get(undertow, "/").statusCode(), equalTo(StatusCodes.SERVICE_UNAVAILABLE));
        } finally {
            undertow.stop();
        }
    }

    @Test
    void testWebSocketsAreClosedWithGoingAway() throws Exception {
        //given
        var handler = Handlers.websocket((exchange, channel) -> channel.resumeReceives());
        var undertow = start(handler);
        var closed = new CompletableFuture<Integer>();

        try {
            CLIENT.newWebSocketBuilder()
                    .buildAsync(uri(undertow, "ws", "/"), new WebSocket.Listener() {
                        @Override
                        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                            closed.complete(statusCode);
                            return null;
                        }
                    })
                    .get(5, TimeUnit.SECONDS);
            await().atMost(Duration.ofSeconds(5)).until(() -> !handler.getPeerConnections().isEmpty());

            //when
            Application.closeWebSockets(List.of(handler));

            //then
            assertThat(closed.get(5, TimeUnit.SECONDS), equalTo(1001));
        } finally {
            undertow.stop();
        }
    }

    @Test
    void testProxyHeadersAreOnlyAppliedWhenEnabled() throws Exception {
        //given
        HttpHandler echo = exchange -> exchange.getResponseSender().send(exchange.getSourceAddress().getAddress().getHostAddress());
        var proxied = start(Application.proxy(echo, true));
        var direct = start(Application.proxy(echo, false));

        try {
            //when
            var request = HttpRequest.newBuilder().header("X-Forwarded-For", "203.0.113.9");
            String viaProxy = CLIENT.send(request.uri(uri(proxied, "http", "/")).build(), HttpResponse.BodyHandlers.ofString()).body();
            String withoutProxy = CLIENT.send(request.uri(uri(direct, "http", "/")).build(), HttpResponse.BodyHandlers.ofString()).body();

            //then
            assertThat(viaProxy, equalTo("203.0.113.9"));
            assertThat(withoutProxy, not(equalTo("203.0.113.9")));
            assertThat(withoutProxy, not(emptyString()));
        } finally {
            proxied.stop();
            direct.stop();
        }
    }
}
