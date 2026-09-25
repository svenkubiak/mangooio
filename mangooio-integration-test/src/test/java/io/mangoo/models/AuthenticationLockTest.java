package io.mangoo.models;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 
 * @author svenkubiak
 *
 */
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
            lock.increment(10, ONE_HOUR);
        }

        //then
        assertThat(lock.getAttempts(), equalTo(9));
        assertThat(lock.isLocked(), equalTo(false));

        //when
        lock.increment(10, ONE_HOUR);

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
            lock.increment(3, ONE_HOUR);
        }
        LocalDateTime lockedUntil = lock.getLockedUntil();

        //when
        for (var i = 1; i <= 100; i++) {
            lock.increment(3, ONE_HOUR);
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
        lock.increment(1, Duration.ZERO);

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
        assertThrows(NullPointerException.class, () -> lock.increment(10, null));
    }
}
