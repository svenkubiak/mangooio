package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

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
public class AuthenticityControllerTest {
    
    @Test
    public void testAuthenticityForm() {
        //given
        WebResponse response = WebRequest.get("/authenticityform").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), startsWith("<input type=\"hidden\" value=\""));
        assertThat(response.getContent(), endsWith(" name=\"authenticityToken\" />"));
    }
    
    @Test
    public void testAuthenticityToken() {
        //given
        WebResponse response = WebRequest.get("/authenticitytoken").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent().length(), equalTo(16));
    }
    
    @Test
    public void testValidAuthenticity() {
        //given
    	WebBrowser instance = WebBrowser.open();
        
    	//when
        WebResponse response = instance.withUri("/authenticitytoken")
                .withMethod(Methods.GET)
                .execute();
        String token = response.getContent();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent().length(), equalTo(16));
        
        //when
        response = instance.withUri("/valid?authenticityToken=" + token)
                .withMethod(Methods.GET)
                .execute();
        
        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("bar"));
    }
    
    @Test
    public void testInvalidAuthenticity() {
        //when
        WebResponse response = WebRequest.get("/invalid?authenticityToken=fdjsklfjsd82jkfldsjkl").execute();
        
        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FORBIDDEN));
        assertThat(response.getContent(), not(containsString("bar")));
        assertThat(response.getContent(), containsString("You are not authorized"));
    }
}