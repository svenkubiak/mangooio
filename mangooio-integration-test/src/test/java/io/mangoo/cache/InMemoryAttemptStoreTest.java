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
import static org.hamcrest.Matchers.nullValue;

class InMemoryAttemptStoreTest {
    private static final Instant NOW = Instant.parse("2026-10-01T12:00:00Z");
    private static final Duration LOCK = Duration.ofMinutes(60);
    private final AtomicLong nanos = new AtomicLong();
    private final MutableClock clock = new MutableClock(NOW);
    private final InMemoryAttemptStore store = new InMemoryAttemptStore(nanos::get, clock);

    @Test
    void testAttemptThatUsesUpTheBudgetIsGranted() {
        //when
        for (var i = 0; i < 2; i++) {
            assertThat(store.tryAcquire("key", 3, LOCK), equalTo(true));
        }

        //then
        assertThat(store.isLocked("key"), equalTo(false));
        assertThat(store.tryAcquire("key", 3, LOCK), equalTo(true));
        assertThat(store.isLocked("key"), equalTo(true));
        assertThat(store.tryAcquire("key", 3, LOCK), equalTo(false));
    }

    @Test
    void testLockIsNotExtendedByFurtherAttempts() {
        //given
        store.tryAcquire("key", 1, LOCK);
        Instant lockedUntil = store.getLockedUntil("key");

        //when
        advance(Duration.ofMinutes(10));
        store.tryAcquire("key", 1, LOCK);

        //then
        assertThat(store.getLockedUntil("key"), equalTo(lockedUntil));
    }

    @Test
    void testElapsedLockResetsTheBudget() {
        //given
        store.tryAcquire("key", 1, LOCK);

        //when
        advance(LOCK);

        //then
        assertThat(store.isLocked("key"), equalTo(false));
        assertThat(store.tryAcquire("key", 2, LOCK), equalTo(true));
        assertThat(store.getAttempts("key"), equalTo(1));
    }

    @Test
    void testLockOutlivesTheRetention() {
        //given
        Duration longLock = Duration.ofHours(5);
        store.tryAcquire("key", 1, longLock);

        //when
        advance(Duration.ofHours(4));

        //then
        assertThat(store.isLocked("key"), equalTo(true));
    }

    @Test
    void testBudgetEndsAfterTheRetention() {
        //given
        store.tryAcquire("key", 5, LOCK);

        //when
        advance(Duration.ofMinutes(61));

        //then
        assertThat(store.getAttempts("key"), equalTo(0));
    }

    @Test
    void testReleaseGivesBackTheAttemptAndTheLock() {
        //given
        store.tryAcquire("key", 2, LOCK);
        store.tryAcquire("key", 2, LOCK);

        //when
        store.release("key", 2);

        //then
        assertThat(store.isLocked("key"), equalTo(false));
        assertThat(store.getAttempts("key"), equalTo(1));

        //when
        store.release("key", 2);

        //then
        assertThat(store.getLockedUntil("key"), nullValue());
        assertThat(store.getAttempts("key"), equalTo(0));
    }

    @Test
    void testReset() {
        //given
        store.tryAcquire("key", 1, LOCK);

        //when
        store.reset("key");

        //then
        assertThat(store.isLocked("key"), equalTo(false));
        assertThat(store.getAttempts("key"), equalTo(0));
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
