package io.mangoo.utils;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.interfaces.OneTimeStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith({TestExtension.class})
class TotpReplayTest {

    @Test
    void testCodeIsAcceptedOncePerIdentifier() {
        //given
        String secret = TotpUtils.createSecret();
        String code = TotpUtils.getTotp(secret);
        String identifier = CommonUtils.uuidV6();

        //when
        boolean first = TotpUtils.verifyTotp(identifier, secret, code);
        boolean replay = TotpUtils.verifyTotp(identifier, secret, code);

        //then
        assertThat(first, equalTo(true));
        assertThat(replay, equalTo(false));
    }

    @Test
    void testCodeWithoutIdentifierCanBeReused() {
        //given
        String secret = TotpUtils.createSecret();
        String code = TotpUtils.getTotp(secret);

        //then
        assertThat(TotpUtils.verifyTotp(secret, code), equalTo(true));
        assertThat(TotpUtils.verifyTotp(secret, code), equalTo(true));
    }

    @Test
    void testInvalidCodeDoesNotUseTheStep() {
        //given
        String secret = TotpUtils.createSecret();
        String code = TotpUtils.getTotp(secret);
        String invalid = "000000".equals(code) ? "111111" : "000000";
        String identifier = CommonUtils.uuidV6();

        //when
        boolean wrong = TotpUtils.verifyTotp(identifier, secret, invalid);

        //then
        assertThat(wrong, equalTo(false));
        assertThat(TotpUtils.verifyTotp(identifier, secret, code), equalTo(true));
    }

    @Test
    void testUsedStepOutlivesItsEndByOnePeriod() {
        //then step 0 covers [0, 30) and step 10 covers [300, 330)
        assertThat(TotpUtils.usedUntil(0), equalTo(Instant.ofEpochSecond(60)));
        assertThat(TotpUtils.usedUntil(10), equalTo(Instant.ofEpochSecond(360)));
    }

    @Test
    void testStepIsStoredInTheOneTimeStore() {
        //given
        String secret = TotpUtils.createSecret();
        String identifier = CommonUtils.uuidV6();
        long counter = Instant.now().getEpochSecond() / 30;
        Application.getInstance(OneTimeStore.class).advance("totp:" + identifier, counter + 1, Instant.now().plusSeconds(60));

        //then a code of an older step than the stored one is refused, as by another instance that used a later code
        assertThat(TotpUtils.verifyTotp(identifier, secret, TotpUtils.getTotp(secret)), equalTo(false));
    }
}
