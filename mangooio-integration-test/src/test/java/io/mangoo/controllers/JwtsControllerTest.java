package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import io.mangoo.test.utils.WebRequest;
import io.mangoo.test.utils.WebResponse;
import io.undertow.util.StatusCodes;

/**
 * 
 * @author svenkubiak
 *
 */
public class JwtsControllerTest {
    private static final String VALID_BEARER = "zcACe7mxq6DPTK1XXpoekqp2yNirjhXso+nkzsCnMw5r9r6Dg3oiabHsQDt7l2KsfxqSpYIH2FJ/Zl5Mkh3H7Z3LC1FvfIyNhL1jsbQq70XeTdcQVKO/HH4Evjv7daU9UARBVKVTgGei6Cbw8EAy+FmrkCbgUnk5jtKpo3pWkgU=";
    private static final String INVALID_BEARER = "zcACe7mxq6DPTK1XXpoekqp2yNirjhXso+nkzsCnMw5r9r6Dg3oiabHsQDt7l2KsfxqSpYIH2FJ/Zl5Mkh3H7Z3LC1FvfIyNhL1jsbQq70V0S9C58S1ZiCxBcZ5TMUI/ExuaGxzAJWzslCnBsdHnEJvg4lBk9uLb8gGfxNt/AQ8=";
    
    @Test
    public void testNoBearerToken() {
        //given
        WebResponse response = WebRequest.get("/jwts/validate")
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.BAD_REQUEST));
    }
    
    @Test
    public void testInvalidBearerToken() {
        //given
        WebResponse response = WebRequest.get("/jwts/validate")
                .withHeader("Authorization", "Bearer " + INVALID_BEARER)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
    }
    
    @Test
    public void testValidBearerToken() {
        //given
        WebResponse response = WebRequest.get("/jwts/validate")
                .withHeader("Authorization", "Bearer " + VALID_BEARER)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    }
    
    @Test
    public void testBearerTokenInRequest() {
        //given
        WebResponse response = WebRequest.get("/jwts/retrieve")
                .withHeader("Authorization", "Bearer " + VALID_BEARER)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), containsString("true"));
    }
}