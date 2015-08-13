package io.mangoo.routing.bindings;

import java.util.HashMap;
import java.util.Map;

import io.mangoo.authentication.Authentication;
import io.mangoo.routing.Payload;
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
    private Map<String, String> parameter = new HashMap<String, String>();

    public Request(HttpServerExchange httpServerExchange, Session session, String authenticityToken, Authentication authentication, Map<String, String> parameter) {
        this.httpServerExchange = httpServerExchange;
        this.session = session;
        this.authenticityToken = authenticityToken;
        this.authentication = authentication;
        this.payload = new Payload();
        this.parameter = parameter;
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

    public String getParameter(String key) {
        return this.parameter.get(key);
    }

    public Map<String, String> getParameter() {
        return this.parameter;
    }
}