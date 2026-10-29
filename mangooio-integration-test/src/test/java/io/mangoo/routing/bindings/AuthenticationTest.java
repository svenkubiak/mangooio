package io.mangoo.routing.bindings;

import io.mangoo.TestExtension;
import com.mongodb.client.model.Filters;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.exceptions.MangooHashingException;
import io.mangoo.persistence.interfaces.Datastore;
import io.mangoo.utils.CommonUtils;
import io.mangoo.utils.TotpUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import org.bson.Document;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith({TestExtension.class})
@Execution(ExecutionMode.SAME_THREAD)
class AuthenticationTest {
    private static final String SALT = "6IeDg6szfIfJKOsuhfZHtqWU4W7O6BpvNFhfI8Kjb64p9Pi";
    private static final String VALID_HASH = "$2a$12$Vyb9AT6IeDg6szfIfJKOsuhfZHtqWU4W7O6BpvNFhfI8Kjb64p9Pi";

    private static String identifier() {
        return UUID.randomUUID().toString();
    }

    // The integration tests run with persistence, so the budgets are kept by MongoAttemptStore
    private static Document attempts(String key) {
        return Application.getInstance(Datastore.class).getMongoDatabase()
                .getCollection("mangooio_attempts")
                .find(Filters.eq("_id", key))
                .first();
    }

    private static int attemptCount(String key) {
        Document document = attempts(key);
        return document == null ? 0 : document.getInteger("attempts");
    }

    private static Date lockedUntil(String key) {
        Document document = attempts(key);
        return document == null ? null : document.getDate("lockedUntil");
    }

    private static int lockAfter() {
        return Application.getInstance(Config.class).getAuthenticationLock();
    }

    private static String invalidTotp(String secret) {
        return "000000".equals(TotpUtils.getTotp(secret)) ? "111111" : "000000";
    }

    @Test
    void testUserLock() {
        //given
        Authentication authentication = Application.getInstance(Authentication.class);
        
        //when
        authentication.isValidLogin("foo", "bar", SALT, VALID_HASH);
        
        //then
        assertThat(authentication.userHasLock("foo"), equalTo(false));
        
        //when
        for (int i=1; i <= 20; i++) {
            authentication.isValidLogin("foobar", "bla", SALT, VALID_HASH);
        }
        
        //then
        assertThat(authentication.userHasLock("foobar"), equalTo(true));
    }

    @Test
    void testUserLockAppliesOnConfiguredAttempt() {
        //given
        Authentication authentication = Application.getInstance(Authentication.class);
        String identifier = identifier();
        int lock = lockAfter();

        //when
        for (int i = 1; i < lock; i++) {
            authentication.isValidLogin(identifier, "bla", SALT, VALID_HASH);
        }

        //then
        assertThat(authentication.userHasLock(identifier), equalTo(false));

        //when
        authentication.isValidLogin(identifier, "bla", SALT, VALID_HASH);

        //then
        assertThat(authentication.userHasLock(identifier), equalTo(true));
    }

    @Test
    void testHashingOverloadFailsClosedWithoutCountingAFailedAttempt() {
        //given
        Authentication authentication = Application.getInstance(Authentication.class);
        String identifier = identifier();

        //when the hashing is rejected because no slot became available
        boolean valid;
        try (MockedStatic<CommonUtils> commonUtils = Mockito.mockStatic(CommonUtils.class)) {
            commonUtils.when(() -> CommonUtils.matchArgon2(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                    .thenThrow(new MangooHashingException("No Argon2 hashing slot became available"));

            valid = authentication.isValidLogin(identifier, "bla", SALT, VALID_HASH);
        }

        //then the login fails closed
        assertThat(valid, equalTo(false));

        //and the failed attempt budget is untouched
        assertThat(attemptCount("pwd:" + identifier), equalTo(0));
        assertThat(authentication.userHasLock(identifier), equalTo(false));
    }

    @Test
    void testSecondFactorLock() {
        //given
        Authentication authentication = Application.getInstance(Authentication.class);
        String identifier = identifier();
        String secret = TotpUtils.createSecret();

        //when
        for (int i = 1; i < lockAfter(); i++) {
            authentication.isValidSecondFactor(identifier, secret, invalidTotp(secret));
        }

        //then
        assertThat(authentication.userHasSecondFactorLock(identifier), equalTo(false));

        //when
        authentication.isValidSecondFactor(identifier, secret, invalidTotp(secret));

        //then
        assertThat(authentication.userHasSecondFactorLock(identifier), equalTo(true));

        //when a correct number arrives after the budget is used up
        boolean valid = authentication.isValidSecondFactor(identifier, secret, TotpUtils.getTotp(secret));

        //then
        assertThat(valid, equalTo(false));
    }

    @Test
    void testValidSecondFactorResetsBudget() {
        //given
        Authentication authentication = Application.getInstance(Authentication.class);
        String identifier = identifier();
        String secret = TotpUtils.createSecret();

        //when
        for (int i = 1; i < lockAfter(); i++) {
            authentication.isValidSecondFactor(identifier, secret, invalidTotp(secret));
        }
        boolean valid = authentication.isValidSecondFactor(identifier, secret, TotpUtils.getTotp(secret));

        //then
        assertThat(valid, equalTo(true));
        assertThat(authentication.userHasSecondFactorLock(identifier), equalTo(false));

        //when the budget starts over
        for (int i = 1; i < lockAfter(); i++) {
            authentication.isValidSecondFactor(identifier, secret, invalidTotp(secret));
        }

        //then
        assertThat(authentication.userHasSecondFactorLock(identifier), equalTo(false));
    }

    @Test
    void testSecondFactorLockIsNotExtendedByFurtherAttempts() {
        //given
        Authentication authentication = Application.getInstance(Authentication.class);
        String identifier = identifier();
        String secret = TotpUtils.createSecret();

        //when
        for (int i = 1; i <= lockAfter(); i++) {
            authentication.isValidSecondFactor(identifier, secret, invalidTotp(secret));
        }

        //then
        assertThat(authentication.userHasSecondFactorLock(identifier), equalTo(true));

        //given
        Date lockedUntil = lockedUntil("2fa:" + identifier);

        //then
        assertThat(lockedUntil, notNullValue());

        //when
        for (int i = 1; i <= 20; i++) {
            authentication.isValidSecondFactor(identifier, secret, invalidTotp(secret));
        }

        //then the absolute unlock timestamp did not move
        assertThat(lockedUntil("2fa:" + identifier), equalTo(lockedUntil));
    }

    @Test
    void testPasswordLockIsNotExtendedByFurtherAttempts() {
        //given
        Authentication authentication = Application.getInstance(Authentication.class);
        String identifier = identifier();

        //when
        for (int i = 1; i <= lockAfter(); i++) {
            authentication.isValidLogin(identifier, "bla", SALT, VALID_HASH);
        }

        //then
        assertThat(authentication.userHasLock(identifier), equalTo(true));

        //given
        Date lockedUntil = lockedUntil("pwd:" + identifier);

        //when
        for (int i = 1; i <= 5; i++) {
            authentication.isValidLogin(identifier, "bla", SALT, VALID_HASH);
        }

        //then the absolute unlock timestamp did not move
        assertThat(lockedUntil, notNullValue());
        assertThat(lockedUntil("pwd:" + identifier), equalTo(lockedUntil));
    }

    @Test
    void testSecondFactorLockDoesNotLockPassword() {
        //given
        Authentication authentication = Application.getInstance(Authentication.class);
        String identifier = identifier();
        String secret = TotpUtils.createSecret();

        //when
        for (int i = 1; i <= lockAfter(); i++) {
            authentication.isValidSecondFactor(identifier, secret, invalidTotp(secret));
        }

        //then
        assertThat(authentication.userHasSecondFactorLock(identifier), equalTo(true));
        assertThat(authentication.userHasLock(identifier), equalTo(false));
    }

    @Test
    void testPasswordLockDoesNotLockSecondFactor() {
        //given
        Authentication authentication = Application.getInstance(Authentication.class);
        String identifier = identifier();
        String secret = TotpUtils.createSecret();

        //when
        for (int i = 1; i <= lockAfter(); i++) {
            authentication.isValidLogin(identifier, "bla", SALT, VALID_HASH);
        }

        //then
        assertThat(authentication.userHasLock(identifier), equalTo(true));
        assertThat(authentication.userHasSecondFactorLock(identifier), equalTo(false));

        //when
        boolean valid = authentication.isValidSecondFactor(identifier, secret, TotpUtils.getTotp(secret));

        //then
        assertThat(valid, equalTo(true));
    }

    @Test
    void testRememberMe() {
        //given
        Authentication authentication = Application.getInstance(Authentication.class);

        //then
        assertThat(authentication.isRememberMe(), equalTo(false));

        //when
        authentication.rememberMe();

        //then
        assertThat(authentication.isRememberMe(), equalTo(true));

        //when
        authentication.rememberMe(false);

        //then
        assertThat(authentication.isRememberMe(), equalTo(false));

        //when
        authentication.rememberMe(true);

        //then
        assertThat(authentication.isRememberMe(), equalTo(true));
    }

    @Test
    void testHasSubjectWithoutLogin() {
        //given
        Authentication authentication = Authentication.create();

        //then
        assertThat(authentication.hasSubject(), equalTo(false));
        assertThat(authentication.isValid(), equalTo(false));
    }

    @Test
    void testIsValidWithoutSecondFactor() {
        //given
        Authentication authentication = Authentication.create();

        //when
        authentication.login("foo");

        //then
        assertThat(authentication.hasSubject(), equalTo(true));
        assertThat(authentication.isTwoFactor(), equalTo(false));
        assertThat(authentication.isValid(), equalTo(true));
    }

    @Test
    void testIsValidWithPendingSecondFactor() {
        //given
        Authentication authentication = Authentication.create();

        //when
        authentication.login("foo").twoFactorAuthentication(true);

        //then
        assertThat(authentication.hasSubject(), equalTo(true));
        assertThat(authentication.isTwoFactor(), equalTo(true));
        assertThat(authentication.isValid(), equalTo(false));
    }

    @Test
    void testIsValidAfterSecondFactorVerified() {
        //given
        Authentication authentication = Authentication.create();
        authentication.login("foo").twoFactorAuthentication(true);

        //when
        authentication.twoFactorAuthentication(false);

        //then
        assertThat(authentication.hasSubject(), equalTo(true));
        assertThat(authentication.isValid(), equalTo(true));
    }

    @Test
    void testIsValidWithPendingSecondFactorAndNoSubject() {
        //given
        Authentication authentication = Authentication.create();

        //when
        authentication.twoFactorAuthentication(true);

        //then
        assertThat(authentication.hasSubject(), equalTo(false));
        assertThat(authentication.isValid(), equalTo(false));
    }

    @Test
    void testConcurrentSecondFactorAttemptsDoNotExceedBudget() throws InterruptedException {
        //given
        Authentication authentication = Application.getInstance(Authentication.class);
        String identifier = identifier();
        String secret = TotpUtils.createSecret();

        //when
        concurrently(50, () -> authentication.isValidSecondFactor(identifier, secret, invalidTotp(secret)));

        //then
        assertThat(attemptCount("2fa:" + identifier), equalTo(lockAfter()));
        assertThat(authentication.userHasSecondFactorLock(identifier), equalTo(true));
    }

    @Test
    void testConcurrentFailedAttemptsAreAllCounted() throws InterruptedException {
        //given
        Authentication authentication = Application.getInstance(Authentication.class);
        String identifier = identifier();
        String secret = TotpUtils.createSecret();
        int attempts = lockAfter() - 2;

        //when
        concurrently(attempts, () -> authentication.isValidSecondFactor(identifier, secret, invalidTotp(secret)));

        //then
        assertThat(attemptCount("2fa:" + identifier), equalTo(attempts));
        assertThat(authentication.userHasSecondFactorLock(identifier), equalTo(false));
    }

    private static void concurrently(int times, Runnable task) throws InterruptedException {
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(times)) {
            for (var i = 0; i < times; i++) {
                executor.submit(() -> {
                    start.await();
                    task.run();
                    return null;
                });
            }
            start.countDown();
        }
    }

    @Test
    void testMissingSaltIsRejectedWithoutConsumingAnAttempt() {
        //given
        Authentication authentication = Application.getInstance(Authentication.class);
        String identifier = identifier();

        //when
        for (String salt : new String[] {null, ""}) {
            assertThrows(RuntimeException.class, () -> authentication.isValidLogin(identifier, "bla", salt, VALID_HASH));
        }

        //then
        assertThat(attempts("pwd:" + identifier), nullValue());
    }

    @Test
    void testSecondFactorCodeIsAcceptedOnce() {
        //given
        Authentication authentication = Application.getInstance(Authentication.class);
        String identifier = identifier();
        String secret = TotpUtils.createSecret();
        String code = TotpUtils.getTotp(secret);

        //when
        boolean first = authentication.isValidSecondFactor(identifier, secret, code);
        boolean replay = authentication.isValidSecondFactor(identifier, secret, code);

        //then
        assertThat(first, equalTo(true));
        assertThat(replay, equalTo(false));
    }

    @Test
    void testSecondFactorCodeOfOneIdentifierDoesNotBlockAnother() {
        //given
        Authentication authentication = Application.getInstance(Authentication.class);
        String secret = TotpUtils.createSecret();
        String code = TotpUtils.getTotp(secret);

        //when
        boolean first = authentication.isValidSecondFactor(identifier(), secret, code);
        boolean other = authentication.isValidSecondFactor(identifier(), secret, code);

        //then
        assertThat(first, equalTo(true));
        assertThat(other, equalTo(true));
    }
}
