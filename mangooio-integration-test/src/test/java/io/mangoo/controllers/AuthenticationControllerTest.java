package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.mangoo.TestExtension;
import io.mangoo.test.http.Browser;
import io.mangoo.test.http.Request;
import io.mangoo.test.http.Response;
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
        Response response = Request.get("/authenticationrequired")
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
        Response response = Request.get("/subject")
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("not authenticated"));
        
        //given
        Browser instance = Browser.open();
        response = instance.to("/dologin")
                .withHTTPMethod(Methods.POST.toString())
                .withDisableRedirects(true)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        
        //given
        instance.to("/subject")
                .withHTTPMethod(Methods.GET.toString())
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("authenticated"));
    }

    @Test
    public void testAuthenticated() {
        //given
        Browser instance = Browser.open();

        //when
        Response response = instance
                .to("/dologin")
                .withDisableRedirects(true)
                .withHTTPMethod(Methods.POST.toString())
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));

        //when
        response = instance.to("/authenticationrequired")
                .withDisableRedirects(true)
                .withHTTPMethod(Methods.GET.toString())
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent().length(), equalTo(9));

        //when
        response = instance.to("/logout")
                .withHTTPMethod(Methods.GET.toString())
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));

        //when
        response = instance.to("/authenticationrequired")
                .withHTTPMethod(Methods.GET.toString())
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
    }
}