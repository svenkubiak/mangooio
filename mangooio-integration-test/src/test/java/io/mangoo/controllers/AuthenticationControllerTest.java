package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

import io.mangoo.core.Application;
import io.mangoo.helpers.TwoFactorHelper;
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
    private static final String SECRET = "MyVoiceIsMySecret";

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
        assertThat(response.getContent(), equalTo("\tHello Guest!\n\t//Display navigation for not authenticated user\n"));
        
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
        assertThat(response.getContent(), equalTo("\tHello foo!\n\t//Display navigation for authenticated user\n"));
    }
    
    @Test
    public void testTwoFactorAuthentication() {
        //given
        WebBrowser instance = WebBrowser.open();
        WebResponse response = instance.withUri("/authenticationrequired")
                .withMethod(Methods.GET)
                .withDisableRedirects(true)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getContent(), not(equalTo(USERNAME)));
        
        //when
        response = instance.withUri("/twofactorlogin")
                .withMethod(Methods.GET)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        
        //given
        List<NameValuePair> parameter = new ArrayList<NameValuePair>();
        parameter.add(new BasicNameValuePair("twofactor", Application.getInstance(TwoFactorHelper.class).generateCurrentNumber(SECRET)));
        
        //when
        response = instance.withUri("/factorize")
                .withMethod(Methods.POST)
                .withPostParameters(parameter)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        
        //given
        response = instance.withUri("/authenticationrequired")
                .withMethod(Methods.GET)
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo(USERNAME));
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
        assertThat(response.getContent(), equalTo("foo"));
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