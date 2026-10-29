package io.mangoo.cache;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class InMemoryOneTimeStoreTest {
    private static final Instant NOW = Instant.parse("2026-10-01T12:00:00Z");
    private final AtomicLong nanos = new AtomicLong();
    private final MutableClock clock = new MutableClock(NOW);
    private final InMemoryOneTimeStore store = new InMemoryOneTimeStore(nanos::get, clock);

    @Test
    void testClaimOnce() {
        //then
        assertThat(store.claim("key", NOW.plusSeconds(60)), equalTo(true));
        assertThat(store.claim("key", NOW.plusSeconds(60)), equalTo(false));
        assertThat(store.claim("other", NOW.plusSeconds(60)), equalTo(true));
    }

    @Test
    void testClaimIsPossibleAgainAfterExpiry() {
        //given
        store.claim("key", NOW.plusSeconds(60));

        //when
        advance(Duration.ofSeconds(61));

        //then
        assertThat(store.claim("key", NOW.plusSeconds(120)), equalTo(true));
    }

    @Test
    void testAdvanceAcceptsOnlyGreaterValues() {
        //then
        assertThat(store.advance("key", 5, NOW.plusSeconds(60)), equalTo(true));
        assertThat(store.advance("key", 5, NOW.plusSeconds(60)), equalTo(false));
        assertThat(store.advance("key", 4, NOW.plusSeconds(60)), equalTo(false));
        assertThat(store.advance("key", 6, NOW.plusSeconds(60)), equalTo(true));
    }

    private void advance(Duration duration) {
        nanos.addAndGet(duration.toNanos());
        clock.instant = clock.instant.plus(duration);
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
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
