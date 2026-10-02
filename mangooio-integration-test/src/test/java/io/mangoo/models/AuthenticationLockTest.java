package io.mangoo.models;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthenticationLockTest {
    private static final Duration ONE_HOUR = Duration.ofHours(1);

    @Test
    void testFreshLock() {
        //given
        var lock = new AuthenticationLock();

        //then
        assertThat(lock.isLocked(), equalTo(false));
        assertThat(lock.getAttempts(), equalTo(0));
        assertThat(lock.getLockedUntil(), nullValue());
    }

    @Test
    void testBudgetIsConsumed() {
        //given
        var lock = new AuthenticationLock();

        //when
        for (var i = 1; i < 10; i++) {
            lock.tryAcquire(10, ONE_HOUR);
        }

        //then
        assertThat(lock.getAttempts(), equalTo(9));
        assertThat(lock.isLocked(), equalTo(false));

        //when
        lock.tryAcquire(10, ONE_HOUR);

        //then
        assertThat(lock.isLocked(), equalTo(true));
        assertThat(lock.getLockedUntil(), notNullValue());
    }

    @Test
    void testLockIsNotExtended() {
        //given
        var lock = new AuthenticationLock();

        //when
        for (var i = 1; i <= 3; i++) {
            lock.tryAcquire(3, ONE_HOUR);
        }
        LocalDateTime lockedUntil = lock.getLockedUntil();

        //when
        for (var i = 1; i <= 100; i++) {
            lock.tryAcquire(3, ONE_HOUR);
        }

        //then
        assertThat(lock.getLockedUntil(), equalTo(lockedUntil));
        assertThat(lock.getAttempts(), equalTo(3));
    }

    @Test
    void testBudgetResetsAfterLockElapsed() {
        //given
        var lock = new AuthenticationLock();

        //when a lock is set that is released immediately
        lock.tryAcquire(1, Duration.ZERO);

        //then
        assertThat(lock.isLocked(), equalTo(false));
        assertThat(lock.getAttempts(), equalTo(0));
        assertThat(lock.getLockedUntil(), nullValue());
    }

    @Test
    void testDurationIsRequired() {
        //given
        var lock = new AuthenticationLock();

        //then
        assertThrows(NullPointerException.class, () -> lock.tryAcquire(10, null));
    }

    @Test
    void testTryAcquireStopsAtBudget() {
        //given
        var lock = new AuthenticationLock();

        //when
        for (var i = 0; i < 10; i++) {
            assertThat(lock.tryAcquire(10, ONE_HOUR), equalTo(true));
        }

        //then
        assertThat(lock.tryAcquire(10, ONE_HOUR), equalTo(false));
        assertThat(lock.getAttempts(), equalTo(10));
    }

    @Test
    void testConcurrentTryAcquireNeverExceedsBudget() throws InterruptedException {
        //given
        var lock = new AuthenticationLock();
        var acquired = new AtomicInteger();
        var start = new CountDownLatch(1);

        //when
        try (var executor = Executors.newFixedThreadPool(16)) {
            for (var i = 0; i < 100; i++) {
                executor.submit(() -> {
                    start.await();
                    if (lock.tryAcquire(10, ONE_HOUR)) {
                        acquired.incrementAndGet();
                    }
                    return null;
                });
            }
            start.countDown();
        }

        //then
        assertThat(acquired.get(), equalTo(10));
        assertThat(lock.getAttempts(), equalTo(10));
    }

    @Test
    void testReleaseGivesAttemptBack() {
        //given
        var lock = new AuthenticationLock();
        for (var i = 0; i < 10; i++) {
            lock.tryAcquire(10, ONE_HOUR);
        }

        //when
        lock.release(10);

        //then
        assertThat(lock.getAttempts(), equalTo(9));
        assertThat(lock.isLocked(), equalTo(false));
    }
}
