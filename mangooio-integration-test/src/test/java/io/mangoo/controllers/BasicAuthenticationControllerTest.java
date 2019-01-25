package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.mangoo.TestExtension;
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;
import io.mangoo.utils.TotpUtils;
import io.undertow.util.StatusCodes;

@ExtendWith({TestExtension.class})
public class BasicAuthenticationControllerTest {
    private static final String secret = "ODZ4DBS4ZAMQBKKHROILUES6CYPZMCGIOVZTFEXSOVSDK2NS54ZNPJDNKVJH7EME";
    
    @BeforeAll
    public static void before () {
        System.setProperty("jdk.httpclient.auth.retrylimit", "5");
    }

    @Test
    public void testBasicAuthenticationFail() {
        //given
        final TestResponse response = TestRequest.get("/basicauth").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
    }
    
    @Test
    public void testBasicAuthenticationSuccess() {
        //given
        final TestResponse response = TestRequest.get("/basicauth").withBasicAuthentication("foo", "bar").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("authenticated"));
    }
    
    @Test
    public void testBasicAuthentication2FASuccess() {
        //given
        String totp = TotpUtils.getTotp(secret);
        
        //when
        final TestResponse response = TestRequest.get("/basicauth2fa").withBasicAuthentication("foo", "bar" + totp).execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("authenticated"));
    }
}