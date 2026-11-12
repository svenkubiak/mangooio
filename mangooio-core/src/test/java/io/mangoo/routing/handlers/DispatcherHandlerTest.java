package io.mangoo.routing.handlers;

import io.mangoo.routing.bindings.Authentication;
import io.mangoo.routing.bindings.Flash;
import io.mangoo.routing.bindings.Request;
import io.mangoo.routing.bindings.Session;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DispatcherHandlerTest {

    @Test
    void testPlainRouteLeavesAuthenticationCookieUnopened() {
        assertFalse(DispatcherHandler.readsAuthenticationCookie(false, false, false, List.of(String.class)));
    }

    @Test
    void testAuthenticatedRouteOpensAuthenticationCookie() {
        assertTrue(DispatcherHandler.readsAuthenticationCookie(true, false, false, List.of()));
    }

    @Test
    void testRequestParameterOpensAuthenticationCookie() {
        assertTrue(DispatcherHandler.readsAuthenticationCookie(false, false, false, List.of(Request.class, String.class)));
    }

    @Test
    void testAuthenticationParameterOpensAuthenticationCookie() {
        assertTrue(DispatcherHandler.readsAuthenticationCookie(false, false, false, List.of(Authentication.class)));
    }

    @Test
    void testSessionAndFlashDoNotOpenAuthenticationCookie() {
        assertFalse(DispatcherHandler.readsAuthenticationCookie(false, false, false, List.of(Session.class, Flash.class)));
    }

    @Test
    void testRequestFilterOpensAuthenticationCookie() {
        assertTrue(DispatcherHandler.readsAuthenticationCookie(false, true, false, List.of()));
    }

    @Test
    void testFilterAnnotationOpensAuthenticationCookie() {
        assertTrue(DispatcherHandler.readsAuthenticationCookie(false, false, true, List.of()));
    }

    @Test
    void testGetStaysOnTheIoThread() {
        assertFalse(DispatcherHandler.leavesIoThread(false, false, false, false));
    }

    @Test
    void testGetMarkedNonBlockingLeavesTheIoThread() {
        assertTrue(DispatcherHandler.leavesIoThread(true, false, false, false));
    }

    @Test
    void testDeleteStaysOnTheIoThread() {
        assertFalse(DispatcherHandler.leavesIoThread(false, false, false, false));
    }

    @Test
    void testFormPostLeavesTheIoThread() {
        assertTrue(DispatcherHandler.leavesIoThread(false, true, true, false));
    }

    @Test
    void testPlainPostLeavesTheIoThread() {
        assertTrue(DispatcherHandler.leavesIoThread(false, true, false, false));
    }

    @Test
    void testJsonWithoutContentLengthLeavesTheIoThread() {
        assertTrue(DispatcherHandler.leavesIoThread(false, true, false, false));
    }

    @Test
    void testSmallJsonPostStaysOnTheIoThread() {
        assertFalse(DispatcherHandler.leavesIoThread(false, true, false, true));
    }

    @Test
    void testSmallJsonMarkedNonBlockingLeavesTheIoThread() {
        assertTrue(DispatcherHandler.leavesIoThread(true, true, false, true));
    }

    @Test
    void testFormWinsOverSmallJson() {
        assertTrue(DispatcherHandler.leavesIoThread(false, true, true, true));
    }
}
