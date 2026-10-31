package io.mangoo.routing.listeners;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.models.Metrics;
import io.mangoo.test.http.TestRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith({TestExtension.class})
class MetricsListenerTest {

    // Other tests reset the shared metrics, so the request is sent again on every attempt
    private static void requestUntil(String path, Runnable assertion) {
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            TestRequest.get(path).execute();
            assertion.run();
        });
    }

    @Test
    void testResponseIsCountedForTheRouteTemplate() {
        requestUntil("/error", () -> {
            Map<Integer, LongAdder> codes = Application.getInstance(Metrics.class).getRouteResponses().get("GET /error");
            assertThat(codes, hasKey(500));
        });
    }

    @Test
    void testPathParametersAreNotKept() {
        String id = "secret-id-" + System.nanoTime();
        requestUntil("/string/" + id, () -> {
            var routes = Application.getInstance(Metrics.class).getRouteResponses();
            assertThat(routes, hasKey("GET /string/{foo}"));
            assertThat(routes.keySet().stream().noneMatch(route -> route.contains(id)), equalTo(true));
        });
    }

    @Test
    void testRequestWithoutRouteIsCountedTogether() {
        requestUntil("/does-not-exist-" + System.nanoTime(), () -> {
            Map<Integer, LongAdder> codes = Application.getInstance(Metrics.class).getRouteResponses().get("GET (no route)");
            assertThat(codes, hasKey(404));
        });
    }

    @Test
    void testServerErrorIsKept() {
        requestUntil("/error", () -> assertThat(Application.getInstance(Metrics.class).getFailures().stream()
                .anyMatch(failure -> "/error".equals(failure.route()) && failure.status() == 500), equalTo(true)));
    }
}
