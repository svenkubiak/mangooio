package io.mangoo.routing.bindings;

import io.mangoo.TestExtension;
import io.mangoo.cache.Cache;
import io.mangoo.cache.CacheProvider;
import io.mangoo.constants.CacheName;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.models.AuthenticationLock;
import io.mangoo.utils.TotpUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
@Execution(ExecutionMode.SAME_THREAD)
class AuthenticationTest {
    private static final String SALT = "6IeDg6szfIfJKOsuhfZHtqWU4W7O6BpvNFhfI8Kjb64p9Pi";
    private static final String VALID_HASH = "$2a$12$Vyb9AT6IeDg6szfIfJKOsuhfZHtqWU4W7O6BpvNFhfI8Kjb64p9Pi";

    private static String identifier() {
        return UUID.randomUUID().toString();
    }

    private static Cache authCache() {
        return Application.getInstance(CacheProvider.class).getCache(CacheName.AUTH);
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
        AuthenticationLock lock = authCache().get(CacheName.AUTH_SECOND_FACTOR_PREFIX + identifier);
        LocalDateTime lockedUntil = lock.getLockedUntil();

        //then
        assertThat(lockedUntil, notNullValue());

        //when
        for (int i = 1; i <= 20; i++) {
            authentication.isValidSecondFactor(identifier, secret, invalidTotp(secret));
        }

        //then the absolute unlock timestamp did not move
        AuthenticationLock current = authCache().get(CacheName.AUTH_SECOND_FACTOR_PREFIX + identifier);
        assertThat(current.getLockedUntil(), equalTo(lockedUntil));
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
        AuthenticationLock lock = authCache().get(CacheName.AUTH_PASSWORD_PREFIX + identifier);
        LocalDateTime lockedUntil = lock.getLockedUntil();

        //when
        for (int i = 1; i <= 5; i++) {
            authentication.isValidLogin(identifier, "bla", SALT, VALID_HASH);
        }

        //then the absolute unlock timestamp did not move
        AuthenticationLock current = authCache().get(CacheName.AUTH_PASSWORD_PREFIX + identifier);
        assertThat(current.getLockedUntil(), equalTo(lockedUntil));
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
}
