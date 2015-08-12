package io.mangoo.routing.bindings;

import io.mangoo.authentication.Authentication;
import io.undertow.server.HttpServerExchange;

/**
 *
 * @author svenkubiak
 *
 */
public class Request {
    private HttpServerExchange httpServerExchange;
    private Payload payload;
    private Session session;
    private String authenticityToken;
    private Authentication authentication;

    public Request(HttpServerExchange httpServerExchange, Session session, String authenticityToken, Authentication authentication) {
        this.httpServerExchange = httpServerExchange;
        this.session = session;
        this.authenticityToken = authenticityToken;
        this.authentication = authentication;
        this.payload = new Payload();
    }

    public HttpServerExchange getHttpServerExchange() {
        return httpServerExchange;
    }

    public Session getSession() {
        return session;
    }

    public boolean authenticityMatches() {
        return this.session.getAuthenticityToken().equals(this.authenticityToken);
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }
}