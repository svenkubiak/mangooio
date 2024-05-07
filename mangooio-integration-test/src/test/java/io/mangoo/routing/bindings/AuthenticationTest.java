package io.mangoo.routing.bindings;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
class AuthenticationTest {
    private static final String SALT = "6IeDg6szfIfJKOsuhfZHtqWU4W7O6BpvNFhfI8Kjb64p9Pi";
    private static final String VALID_HASH = "$2a$12$Vyb9AT6IeDg6szfIfJKOsuhfZHtqWU4W7O6BpvNFhfI8Kjb64p9Pi";

    @Test
    void testUserLock() {
        //given
        Authentication authentication = Application.getInstance(Authentication.class);
        
        //when
        authentication.validLogin("foo", "bar", SALT, VALID_HASH);
        
        //then
        assertThat(authentication.userHasLock("foo"), equalTo(false));
        
        //when
        for (int i=1; i <= 20; i++) {
            authentication.validLogin("foobar", "bla", SALT, VALID_HASH);   
        }
        
        //then
        assertThat(authentication.userHasLock("foobar"), equalTo(true));
    }

    @Test
    void testRemeberMe() {
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