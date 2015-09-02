package io.mangoo.routing.bindings;

import java.util.HashMap;
import java.util.Map;

import io.mangoo.authentication.Authentication;
import io.undertow.server.HttpServerExchange;

/**
 *
 * @author svenkubiak
 *
 * @deprecated As of release 1.1.0, replaced by Payload
 *
 */
@Deprecated
public class Exchange {
    private HttpServerExchange httpServerExchange;
    private Session session;
    private String authenticityToken;
    private Authentication authentication;
    private Map<String, Object> content = new HashMap<String, Object>();

    public Exchange(HttpServerExchange httpServerExchange, Session session, String authenticityToken, Authentication authentication) {
        this.httpServerExchange = httpServerExchange;
        this.session = session;
        this.authenticityToken = authenticityToken;
        this.authentication = authentication;
    }

    public Exchange(HttpServerExchange exchange) {
        this.httpServerExchange = exchange;
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

    public Map<String, Object> getContent() {
        return this.content;
    }

    public void addContent(String key, Object value) {
        this.content.put(key, value);
    }
}