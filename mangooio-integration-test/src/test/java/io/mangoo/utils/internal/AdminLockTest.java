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
        MangooUtils.resetAdminAttempts(MangooUtils.ADMIN_PASSWORD);
        MangooUtils.resetAdminAttempts(MangooUtils.ADMIN_SECOND_FACTOR);
    }

    private static void attempts(String step, int times) {
        for (var i = 0; i < times; i++) {
            MangooUtils.acquireAdminAttempt(step);
        }
    }

    @Test
    void testSecondFactorLockDoesNotLockPassword() {
        //given
        attempts(MangooUtils.ADMIN_SECOND_FACTOR, 10);

        //then
        assertThat(MangooUtils.isAdminLocked(MangooUtils.ADMIN_SECOND_FACTOR), equalTo(true));
        assertThat(MangooUtils.isAdminLocked(MangooUtils.ADMIN_PASSWORD), equalTo(false));
    }

    @Test
    void testPasswordLockDoesNotLockSecondFactor() {
        //given
        attempts(MangooUtils.ADMIN_PASSWORD, 10);

        //then
        assertThat(MangooUtils.isAdminLocked(MangooUtils.ADMIN_PASSWORD), equalTo(true));
        assertThat(MangooUtils.isAdminLocked(MangooUtils.ADMIN_SECOND_FACTOR), equalTo(false));
    }

    @Test
    void testReleasedAttemptIsGivenBack() {
        //given
        attempts(MangooUtils.ADMIN_PASSWORD, 9);

        //when the tenth attempt could not be verified
        MangooUtils.acquireAdminAttempt(MangooUtils.ADMIN_PASSWORD);
        MangooUtils.releaseAdminAttempt(MangooUtils.ADMIN_PASSWORD);

        //then
        assertThat(MangooUtils.isAdminLocked(MangooUtils.ADMIN_PASSWORD), equalTo(false));
    }
}
