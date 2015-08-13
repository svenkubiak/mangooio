package io.mangoo.routing.bindings;

import java.util.Map;

import io.mangoo.authentication.Authentication;
import io.undertow.server.HttpServerExchange;

/**
 *
 * @author svenkubiak
 *
 * @deprecated As of release 1.1.0, replaced by {@link #Payload}
 *
 */
@Deprecated
public class Exchange {
    private HttpServerExchange httpServerExchange;
    private Session session;
    private String authenticityToken;
    private Authentication authentication;

    public Exchange(HttpServerExchange httpServerExchange, Session session, String authenticityToken, Authentication authentication, Map<String, String> parameter) {
        this.httpServerExchange = httpServerExchange;
        this.session = session;
        this.authenticityToken = authenticityToken;
        this.authentication = authentication;
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
}