package io.mangoo.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.llorllale.cactoos.matchers.RunsInThreads;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;

@ExtendWith({TestExtension.class})
public class AuthorizationServiceTest {

    @Test
    public void testInvalidAuthorization() {
        //given
        AuthorizationService authorizationService = Application.getInstance(AuthorizationService.class);
        
        //when
        boolean authorization = authorizationService.validAuthorization("foo", "bar", "foobar");
        
        //then
        assertThat(authorization, equalTo(false));
    }
    
    @Test
    public void testInvalidAuthorizationConcurrent() {
        MatcherAssert.assertThat(t -> {
            //given
            AuthorizationService authorizationService = Application.getInstance(AuthorizationService.class);
            
            // then
            return !authorizationService.validAuthorization("foo", "bar", "foobar");
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }
    
    @Test
    public void testValidAuthorizationConcurrent() {
        MatcherAssert.assertThat(t -> {
            //given
            AuthorizationService authorizationService = Application.getInstance(AuthorizationService.class);
            
            // then
            return authorizationService.validAuthorization("bob", "AuthorizationController:write", "write");
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }
}