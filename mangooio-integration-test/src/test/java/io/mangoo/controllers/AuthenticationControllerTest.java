package io.mangoo.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import io.mangoo.test.MangooBrowser;
import io.mangoo.test.MangooRequest;
import io.mangoo.test.MangooResponse;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;

/**
 *
 * @author svenkubiak
 *
 */
public class AuthenticationControllerTest {

    @Test
    public void notAuthenticatedTest() {
        MangooResponse response = MangooRequest.GET("/authenticationrequired").withDisableRedirects(true).execute();

        assertNotNull(response);
        assertEquals(StatusCodes.FOUND, response.getStatusCode());
    }

    @Test
    public void authenticatedTest() {
        MangooBrowser instance = MangooBrowser.open();

        MangooResponse response = instance.withUri("/dologin").withMethod(Methods.POST).execute();
        assertNotNull(response);
        assertEquals(StatusCodes.FOUND, response.getStatusCode());

        response = instance.withUri("/authenticationrequired").withDisableRedirects(true).withMethod(Methods.GET).execute();
        assertNotNull(response);
        assertEquals(StatusCodes.OK, response.getStatusCode());

        response = instance.withUri("/logout").withMethod(Methods.GET).execute();
        assertNotNull(response);
        assertEquals(StatusCodes.OK, response.getStatusCode());

        response = instance.withUri("/authenticationrequired").withMethod(Methods.GET).execute();
        assertNotNull(response);
        assertEquals(StatusCodes.FOUND, response.getStatusCode());
    }
}