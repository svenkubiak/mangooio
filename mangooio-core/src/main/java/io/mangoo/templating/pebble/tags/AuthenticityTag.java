package io.mangoo.templating.pebble.tags;

import io.mangoo.routing.bindings.Session;

/**
 * 
 * @author svenkubiak
 *
 */
public class AuthenticityTag {
    private final Session session;

    public AuthenticityTag(Session session) {
        this.session = session;
    }
    
    public String form() {
        return "<input type=\"hidden\" value=\"" + this.session.getAuthenticityToken() + "\" name=\"authenticityToken\" />";
    }

    public String token() {
        return this.session.getAuthenticityToken();
    }
}