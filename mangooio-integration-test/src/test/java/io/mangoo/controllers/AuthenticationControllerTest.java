package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import io.mangoo.test.utils.WebBrowser;
import io.mangoo.test.utils.WebRequest;
import io.mangoo.test.utils.WebResponse;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;

/**
 *
 * @author svenkubiak
 *
 */
public class AuthenticationControllerTest {
    private static final String USERNAME = "foo";

    @Test
    public void testNotAuthenticated() {
        //given
        WebResponse response = WebRequest.get("/authenticationrequired")
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
        WebResponse response = WebRequest.get("/subject")
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("not authenticated"));
        
        //given
        WebBrowser instance = WebBrowser.open();
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
        WebBrowser instance = WebBrowser.open();

        //when
        WebResponse response = instance.withUri("/dologin")
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
        assertThat(response.getContent().length(), equalTo(26));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));

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