package io.mangoo.controllers;

import io.mangoo.TestExtension;
import io.mangoo.constants.Default;
import io.mangoo.test.http.TestBrowser;
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
class I18nControllerTest {
    
    @Test
    void testWithOutAdditionalHeader() {
        //given
        TestResponse response = TestRequest.get("/translation").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("welcome"));
    }
    
    @Test
    void testSpecialCharacters() {
        //given
        TestResponse response = TestRequest.get("/special")
                .withHeader("Accept-Language", "fr-FR")
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("Côte d&#39;Ivoire"));
    }
    
    @Test
    void testUmlaute() {
        //given
        TestResponse response = TestRequest.get("/umlaute")
                .withHeader("Accept-Language", "de-DE")
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("Umlaute test : äöü und special (§&amp;! charcter- . te;;st"));
    }
    
    @Test
    void testWithAdditionalHeaderDe() {
        //given
        TestResponse response = TestRequest.get("/translation")
                .withHeader("Accept-Language", "de-DE")
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("willkommen"));
    }
    
    @Test
    void testWithAdditionalHeaderEn() {
        //given
        TestResponse response = TestRequest.get("/translation")
                .withHeader("Accept-Language", "en-US")
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("welcome"));
    }
    
    @Test
    void testWithInjectedAdditionalHeaderDe() {
        //given
        TestResponse response = TestRequest.get("/messages")
                .withHeader("Accept-Language", "de-DE")
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("willkommen"));
    }
    
    @Test
    void testWithInjectedMessagesDefaultLanguage() {
        //given
        TestResponse response = TestRequest.get("/messages")
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("welcome"));
    }
    
    @Test
    void testWithI18nCookie() {
        //given
        TestBrowser browser = TestBrowser.open();
        TestResponse response = browser.withHTTPMethod(Methods.GET.toString()).to("/localize").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getCookie(Default.I18N_COOKIE_NAME.toString()), not(nullValue()));
        
        //given
        response = browser.to("/translation").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("welcome"));
    }
}