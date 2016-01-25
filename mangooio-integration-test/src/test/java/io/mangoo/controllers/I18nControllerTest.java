package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import io.mangoo.enums.Default;
import io.mangoo.test.utils.Browser;
import io.mangoo.test.utils.Request;
import io.mangoo.test.utils.Response;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;

/**
 * 
 * @author svenkubiak
 *
 */
public class I18nControllerTest {
    
    @Test
    public void testWithOutAdditionalHeader() {
        //given
        Response response = Request.get("/translation").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("welcome"));
    }
    
    @Test
    public void testWithAdditionalHeaderDe() {
        //given
        Response response = Request.get("/translation")
                .withHeader("Accept-Language", "de-DE")
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("willkommen"));
    }
    
    @Test
    public void testWithAdditionalHeaderEn() {
        //given
        Response response = Request.get("/translation")
                .withHeader("Accept-Language", "en-US")
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("welcome"));
    }
    
    @Test
    public void testWithI18nCookie() {
        //given
        Browser browser = Browser.open();
        Response response = browser.withMethod(Methods.GET).withUri("/localize").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getCookie(Default.COOKIE_I18N_NAME.toString()), not(nullValue()));
        
        //given
        response = browser.withUri("/translation").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("welcome"));
    }
}