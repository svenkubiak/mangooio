package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.mangoo.TestExtension;
import io.mangoo.test.http.TestBrowser;
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;

/**
 *
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
public class AuthenticationControllerTest {
    private static final String USERNAME = "foo";

    @Test
    public void testNotAuthenticated() {
        //given
        TestResponse response = TestRequest.get("/authenticationrequired")
                .withDisableRedirects(true)
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getContent(), not(equalTo(USERNAME)));
    }
    
    @Test
    public void testSubject() {
        //given
        TestResponse response = TestRequest.get("/subject")
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("not authenticated"));
        
        //given
        TestBrowser instance = TestBrowser.open();
        response = instance.withUri("/dologin")
                .withMethod(Methods.POST)
                .withDisableRedirects(true)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        
        //given
        instance.withUri("/subject")
                .withMethod(Methods.GET)
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("authenticated"));
    }

    @Test
    public void testAuthenticated() {
        //given
        TestBrowser instance = TestBrowser.open();

        //when
        TestResponse response = instance.withUri("/dologin")
                .withMethod(Methods.POST)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));

        //when
        response = instance.withUri("/authenticationrequired")
                .withDisableRedirects(true)
                .withMethod(Methods.GET)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent().length(), equalTo(9));

        //when
        response = instance.withUri("/logout")
                .withMethod(Methods.GET)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));

        //when
        response = instance.withUri("/authenticationrequired")
                .withMethod(Methods.GET)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
    }
}