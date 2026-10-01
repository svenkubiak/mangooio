package io.mangoo.utils.internal;

import io.mangoo.TestExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith({TestExtension.class})
class AdminLockTest {

    @AfterEach
    void tearDown() {
        MangooUtils.resetLockCounter();
        MangooUtils.resetSecondFactorLockCounter();
    }

    @Test
    void testPasswordLoginDoesNotResetSecondFactorAttempts() {
        //given
        for (var i = 0; i < 9; i++) {
            MangooUtils.invalidSecondFactor();
        }

        //when
        MangooUtils.resetLockCounter();
        MangooUtils.invalidSecondFactor();

        //then
        assertThat(MangooUtils.isSecondFactorNotLocked(), equalTo(false));
    }

    @Test
    void testSecondFactorLockDoesNotLockPassword() {
        //given
        for (var i = 0; i < 10; i++) {
            MangooUtils.invalidSecondFactor();
        }

        //then
        assertThat(MangooUtils.isSecondFactorNotLocked(), equalTo(false));
        assertThat(MangooUtils.isNotLocked(), equalTo(true));
    }

    @Test
    void testPasswordLockDoesNotLockSecondFactor() {
        //given
        for (var i = 0; i < 10; i++) {
            MangooUtils.invalidAuthentication();
        }

        //then
        assertThat(MangooUtils.isNotLocked(), equalTo(false));
        assertThat(MangooUtils.isSecondFactorNotLocked(), equalTo(true));
    }

    @Test
    void testResetSecondFactorLockCounter() {
        //given
        for (var i = 0; i < 10; i++) {
            MangooUtils.invalidSecondFactor();
        }

        //when
        MangooUtils.resetSecondFactorLockCounter();

        //then
        assertThat(MangooUtils.isSecondFactorNotLocked(), equalTo(true));
    }
}
