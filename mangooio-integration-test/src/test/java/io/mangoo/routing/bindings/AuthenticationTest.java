package io.mangoo.routing.bindings;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import io.mangoo.core.Application;

/**
 * 
 * @author svenkubiak
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuthenticationTest {
    private static final String VALID_HASH = "$2a$12$Vyb9AT6IeDg6szfIfJKOsuhfZHtqWU4W7O6BpvNFhfI8Kjb64p9Pi";

    @Test
    public void testNoLock() {
        //given
        Authentication authentication = Application.getInstance(Authentication.class);
        
        //when
        authentication.validLogin("foo", "bar", VALID_HASH);
        
        //then
        assertThat(authentication.userHasLock("foo"), equalTo(false));
    }
    
    @Test
    public void testLock() {
        //given
        Authentication authentication = Application.getInstance(Authentication.class);
        
        //when
        for (int i=1; i <= 20; i++) {
            authentication.validLogin("foobar", "bla", VALID_HASH);   
        }
        
        //then
        assertThat(authentication.userHasLock("foobar"), equalTo(true));
    }
}