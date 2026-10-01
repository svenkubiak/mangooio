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

class InMemoryTokenBlacklistTest {
    private static final Duration MAX_TOKEN_LIFETIME = Duration.ofDays(30);
    private static final Instant NOW = Instant.parse("2026-10-01T12:00:00.500Z");
    private final AtomicLong nanos = new AtomicLong();
    private final MutableClock clock = new MutableClock(NOW);
    private final InMemoryTokenBlacklist blacklist = new InMemoryTokenBlacklist(MAX_TOKEN_LIFETIME, nanos::get, clock);

    @Test
    void testRevokedTokenStaysRevokedBeyondSixtyMinutes() {
        //given
        blacklist.revoke("jti", NOW.plus(MAX_TOKEN_LIFETIME));

        //when
        advance(Duration.ofMinutes(61));

        //then
        assertThat(blacklist.isRevoked("jti", null, null), equalTo(true));
    }

    @Test
    void testRevocationEndsWithTokenExpiry() {
        //given
        blacklist.revoke("jti", NOW.plus(MAX_TOKEN_LIFETIME));

        //when
        advance(MAX_TOKEN_LIFETIME.plusSeconds(1));

        //then
        assertThat(blacklist.isRevoked("jti", null, null), equalTo(false));
    }

    @Test
    void testRevocationIsNotEvictedAfterFiveThousandEntries() {
        //given
        for (var i = 0; i < 6000; i++) {
            blacklist.revoke("jti-" + i, NOW.plus(MAX_TOKEN_LIFETIME));
        }

        //then
        assertThat(blacklist.isRevoked("jti-0", null, null), equalTo(true));
        assertThat(blacklist.isRevoked("jti-5999", null, null), equalTo(true));
    }

    @Test
    void testAlreadyExpiredTokenIsNotStored() {
        //given
        blacklist.revoke("jti", NOW.minusSeconds(1));

        //then
        assertThat(blacklist.isRevoked("jti", null, null), equalTo(false));
    }

    @Test
    void testRevokeSubject() {
        //given
        blacklist.revokeSubject("subject", NOW);

        //then
        assertThat(blacklist.isRevoked("other-jti", "subject", NOW.minusSeconds(60)), equalTo(true));
        assertThat(blacklist.isRevoked("other-jti", "subject", NOW.plusSeconds(60)), equalTo(false));
        assertThat(blacklist.isRevoked("other-jti", "other-subject", NOW.minusSeconds(60)), equalTo(false));
    }

    @Test
    void testTokenIssuedInSameSecondAsSubjectRevocationStaysValid() {
        //given
        blacklist.revokeSubject("subject", NOW);

        //then
        assertThat(blacklist.isRevoked("jti", "subject", Instant.parse("2026-10-01T12:00:00Z")), equalTo(false));
        assertThat(blacklist.isRevoked("jti", "subject", Instant.parse("2026-10-01T11:59:59Z")), equalTo(true));
    }

    @Test
    void testSubjectRevocationEndsAfterMaxTokenLifetime() {
        //given
        blacklist.revokeSubject("subject", NOW);

        //when
        advance(MAX_TOKEN_LIFETIME.plusSeconds(1));

        //then
        assertThat(blacklist.isRevoked("jti", "subject", NOW.minusSeconds(60)), equalTo(false));
    }

    @Test
    void testLaterSubjectRevocationWins() {
        //given
        blacklist.revokeSubject("subject", NOW.plusSeconds(120));
        blacklist.revokeSubject("subject", NOW);

        //then
        assertThat(blacklist.isRevoked("jti", "subject", NOW.plusSeconds(60)), equalTo(true));
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
