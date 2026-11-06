package io.mangoo.models;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith({TestExtension.class})
class MetricsTest {
    
    @Test
    void testIncrementContentLength() {
        //given
        Metrics metrics = Application.getInstance(Metrics.class);
        
        //when
        metrics.reset();
        metrics.incrementDataSend(42);
        metrics.incrementDataSend(12);
        metrics.incrementDataSend(23);
        
        //then
        assertThat(metrics.getDataSend(), equalTo(77L));
    }
    
    @Test
    void testReset() {
        //given
        Metrics metrics = Application.getInstance(Metrics.class);
        
        //when
        metrics.reset();
        metrics.addStatusCode(418);
        metrics.addStatusCode(418);
        metrics.addStatusCode(420);
        
        //then
        assertThat(metrics.getResponseMetrics().get(301), equalTo(null));
        assertThat(metrics.getResponseMetrics().get(418).intValue(), equalTo(2));
        assertThat(metrics.getResponseMetrics().get(420).intValue(), equalTo(1));
        
        //when
        metrics.reset();

        //then
        assertThat(metrics.getResponseMetrics().get(418), equalTo(null));
    }
    
    @Test
    void testMinCount() {
        //given
        Metrics metrics = Application.getInstance(Metrics.class);
        
        //when
        metrics.reset();
        metrics.update(0);
        metrics.update(2000);
        metrics.update(1000);
        metrics.update(3000);
        
        //then
        assertThat(metrics.getMinRequestTime(), equalTo(1000));
    }
    
    @Test
    void testMaxCount() {
        //given
        Metrics metrics = Application.getInstance(Metrics.class);
        
        //when
        metrics.reset();
        metrics.update(0);
        metrics.update(2000);
        metrics.update(1000);
        metrics.update(3000);
        
        //then
        assertThat(metrics.getMaxRequestTime(), equalTo(3000));
    }
    
    @Test
    void testAvgCount() {
        //given
        Metrics metrics = Application.getInstance(Metrics.class);
        
        //when
        metrics.reset();
        metrics.update(0);
        metrics.update(2000);
        metrics.update(1000);
        metrics.update(3000);
        
        //then
        assertThat(metrics.getAvgRequestTime(), equalTo(1500L));
    }

    @Test
    void testWindowCountsRequestsErrorsAndTimes() {
        //given
        Metrics metrics = new Metrics(new AdjustableClock());

        //when
        metrics.update(10);
        metrics.addStatusCode(200);
        metrics.update(30);
        metrics.addStatusCode(503);

        //then
        assertThat(metrics.getWindow(), equalTo(new Metrics.Window(2, 1, 20, 30)));
    }

    @Test
    void testWindowDropsMinutesOlderThanFiveMinutes() {
        //given
        var clock = new AdjustableClock();
        Metrics metrics = new Metrics(clock);
        metrics.update(10);
        metrics.addStatusCode(500);

        //when
        clock.advance(Duration.ofMinutes(4));
        metrics.update(20);
        metrics.addStatusCode(200);
        Metrics.Window withinWindow = metrics.getWindow();
        clock.advance(Duration.ofMinutes(1));
        Metrics.Window afterWindow = metrics.getWindow();

        //then
        assertThat(withinWindow, equalTo(new Metrics.Window(2, 1, 15, 20)));
        assertThat(afterWindow, equalTo(new Metrics.Window(1, 0, 20, 20)));
    }

    @Test
    void testWindowReusesTheBucketOfAnExpiredMinute() {
        //given
        var clock = new AdjustableClock();
        Metrics metrics = new Metrics(clock);
        metrics.update(10);
        metrics.addStatusCode(500);

        //when the same bucket is used again six minutes later
        clock.advance(Duration.ofMinutes(6));
        metrics.update(40);
        metrics.addStatusCode(200);

        //then
        assertThat(metrics.getWindow(), equalTo(new Metrics.Window(1, 0, 40, 40)));
    }

    @Test
    void testResetClearsWindow() {
        //given
        Metrics metrics = new Metrics(new AdjustableClock());
        metrics.update(10);
        metrics.addStatusCode(500);

        //when
        metrics.reset();

        //then
        assertThat(metrics.getWindow(), equalTo(new Metrics.Window(0, 0, 0, -1)));
    }

    @Test
    void testResponsesAreCountedPerRoute() {
        //given
        Metrics metrics = new Metrics(new AdjustableClock());

        //when
        metrics.addRouteResponse("GET /user/{id}", 200);
        metrics.addRouteResponse("GET /user/{id}", 500);
        metrics.addRouteResponse("GET /user/{id}", 500);
        metrics.addRouteResponse("POST /user", 400);

        //then
        assertThat(metrics.getRouteResponses().get("GET /user/{id}").get(500).longValue(), equalTo(2L));
        assertThat(metrics.getRouteResponses().get("GET /user/{id}").get(200).longValue(), equalTo(1L));
        assertThat(metrics.getRouteResponses().get("POST /user").get(400).longValue(), equalTo(1L));
    }

    @Test
    void testRoutesBeyondTheLimitAreCountedTogether() {
        //given
        Metrics metrics = new Metrics(new AdjustableClock());
        for (var i = 0; i < 1000; i++) {
            metrics.addRouteResponse("GET /route" + i, 200);
        }

        //when
        metrics.addRouteResponse("GET /one-more", 500);
        metrics.addRouteResponse("GET /route0", 200);

        //then
        assertThat(metrics.getRouteResponses().size(), equalTo(1001));
        assertThat(metrics.getRouteResponses().get("Other routes").get(500).longValue(), equalTo(1L));
        assertThat(metrics.getRouteResponses().get("GET /route0").get(200).longValue(), equalTo(2L));
    }

    @Test
    void testOnlyTheLastFiftyFailuresAreKeptNewestFirst() {
        //given
        Metrics metrics = new Metrics(new AdjustableClock());

        //when
        for (var i = 0; i < 60; i++) {
            metrics.addFailure(new Metrics.Failure(Instant.EPOCH, "GET", "/route" + i, 500));
        }

        //then
        assertThat(metrics.getFailures().size(), equalTo(50));
        assertThat(metrics.getFailures().getFirst().route(), equalTo("/route59"));
        assertThat(metrics.getFailures().getLast().route(), equalTo("/route10"));
    }

    @Test
    void testResetClearsRoutesAndFailures() {
        //given
        Metrics metrics = new Metrics(new AdjustableClock());
        metrics.addRouteResponse("GET /", 500);
        metrics.addFailure(new Metrics.Failure(Instant.EPOCH, "GET", "/", 500));

        //when
        metrics.reset();

        //then
        assertThat(metrics.getRouteResponses().isEmpty(), equalTo(true));
        assertThat(metrics.getFailures().isEmpty(), equalTo(true));
    }

    private static final class AdjustableClock extends Clock {
        private Instant instant = Instant.parse("2026-01-01T10:00:30Z");

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
