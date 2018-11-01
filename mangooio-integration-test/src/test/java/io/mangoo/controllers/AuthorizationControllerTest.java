package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.mangoo.TestExtension;
import io.mangoo.test.http.TestBrowser;
import io.mangoo.test.http.TestResponse;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
public class AuthorizationControllerTest {

    @Test
    public void testReadUnAuthorized() {
        //given
        TestBrowser instance = TestBrowser.open();
        TestResponse response = instance.to("/authorize/jack")
                .withHTTPMethod(Methods.GET.toString())
                .withDisabledRedirects()
                .execute();
        
        //given
        instance.to("/read")
            .withHTTPMethod(Methods.GET.toString())
            .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContent(), not(equalTo("can read")));
    }
    
    @Test
    public void testReadAuthorized() {
        //given
        TestBrowser instance = TestBrowser.open();
        TestResponse response = instance.to("/authorize/alice")
                .withHTTPMethod(Methods.GET.toString())
                .withDisabledRedirects()
                .execute();
        
        //given
        instance.to("/read")
            .withHTTPMethod(Methods.GET.toString())
            .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("can read"));
    }
    
    @Test
    public void testWriteUnAuthorized() {
        //given
        TestBrowser instance = TestBrowser.open();
        TestResponse response = instance.to("/authorize/peter")
                .withHTTPMethod(Methods.GET.toString())
                .withDisabledRedirects()
                .execute();
        
        //given
        instance.to("/write")
            .withHTTPMethod(Methods.POST.toString())
            .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContent(), not(equalTo("can write")));
    }
    
    @Test
    public void testWriteAuthorized() {
        //given
        TestBrowser instance = TestBrowser.open();
        TestResponse response = instance.to("/authorize/bob")
                .withHTTPMethod(Methods.GET.toString())
                .withDisabledRedirects()
                .execute();
        
        //given
        instance.to("/write")
            .withHTTPMethod(Methods.POST.toString())
            .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("can write"));
    }
}