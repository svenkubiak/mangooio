package io.mangoo.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import io.mangoo.test.MangooBrowser;
import io.mangoo.test.MangooRequest;
import io.mangoo.test.MangooResponse;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;

import org.junit.Test;

/**
 * 
 * @author svenkubiak
 *
 */
public class AuthenticationControllerTest {
    
    @Test
    public void notAuthenticatedTest() {
       MangooResponse response = MangooRequest.get("/authenticationrequired").execute();

       assertNotNull(response);
       assertEquals(StatusCodes.UNAUTHORIZED, response.getStatusCode());
    }
    
    @Test
    public void authenticatedTest() {
       MangooBrowser instance = MangooBrowser.getInstance();
       
       MangooResponse response = instance.uri("/login").method(Methods.POST).execute();
       assertNotNull(response);
       assertEquals(StatusCodes.OK, response.getStatusCode());
       
       response = instance.uri("/authenticationrequired").method(Methods.GET).execute();
       assertNotNull(response);
       assertEquals(StatusCodes.OK, response.getStatusCode());
       
       response = instance.uri("/logout").method(Methods.GET).execute();
       assertNotNull(response);
       assertEquals(StatusCodes.OK, response.getStatusCode());
       
       response = instance.uri("/authenticationrequired").method(Methods.GET).execute();
       assertNotNull(response);
       assertEquals(StatusCodes.UNAUTHORIZED, response.getStatusCode());
    }
}