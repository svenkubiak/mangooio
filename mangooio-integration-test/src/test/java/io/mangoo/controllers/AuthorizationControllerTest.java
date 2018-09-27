package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.mangoo.TestExtension;
import io.mangoo.test.utils.WebBrowser;
import io.mangoo.test.utils.WebResponse;
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
        WebBrowser instance = WebBrowser.open();
        WebResponse response = instance.withUri("/authorize/jack")
                .withMethod(Methods.GET)
                .withDisableRedirects(true)
                .execute();
        
        //given
        instance.withUri("/read")
            .withMethod(Methods.GET)
            .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContent(), not(equalTo("can read")));
    }
    
    @Test
    public void testReadAuthorized() {
        //given
        WebBrowser instance = WebBrowser.open();
        WebResponse response = instance.withUri("/authorize/alice")
                .withMethod(Methods.GET)
                .withDisableRedirects(true)
                .execute();
        
        //given
        instance.withUri("/read")
            .withMethod(Methods.GET)
            .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("can read"));
    }
    
    @Test
    public void testWriteUnAuthorized() {
        //given
        WebBrowser instance = WebBrowser.open();
        WebResponse response = instance.withUri("/authorize/peter")
                .withMethod(Methods.GET)
                .withDisableRedirects(true)
                .execute();
        
        //given
        instance.withUri("/write")
            .withMethod(Methods.POST)
            .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContent(), not(equalTo("can write")));
    }
    
    @Test
    public void testWriteAuthorized() {
        //given
        WebBrowser instance = WebBrowser.open();
        WebResponse response = instance.withUri("/authorize/bob")
                .withMethod(Methods.GET)
                .withDisableRedirects(true)
                .execute();
        
        //given
        instance.withUri("/write")
            .withMethod(Methods.POST)
            .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("can write"));
    }
}