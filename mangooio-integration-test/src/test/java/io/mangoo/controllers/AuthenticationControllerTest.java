package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import io.mangoo.utils.http.HTTPBrowser;
import io.mangoo.utils.http.HTTPRequest;
import io.mangoo.utils.http.HTTPResponse;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;

/**
 *
 * @author svenkubiak
 *
 */
public class AuthenticationControllerTest {

    @Test
    public void testNotAuthenticated() {
        //given
        HTTPResponse response = HTTPRequest.get("/authenticationrequired")
                .withDisableRedirects(true)
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
    }

    @Test
    public void testAuthenticated() {
        //given
        HTTPBrowser instance = HTTPBrowser.open();

        //when
        HTTPResponse response = instance.withUri("/dologin")
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