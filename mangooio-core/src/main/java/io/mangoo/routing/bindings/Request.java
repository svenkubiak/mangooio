package io.mangoo.routing.bindings;

import java.util.Map;

import io.mangoo.authentication.Authentication;
import io.mangoo.core.Application;
import io.mangoo.interfaces.MangooValidator;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;

/**
 *
 * @author svenkubiak
 *
 */
public class Request implements MangooValidator {
    private HttpServerExchange httpServerExchange;
    private Payload payload;
    private Session session;
    private String authenticityToken;
    private Authentication authentication;
    private Validator validator;
    private Map<String, String> parameter;

    public Request(HttpServerExchange httpServerExchange, Session session, String authenticityToken, Authentication authentication, Map<String, String> parameter) {
        this.httpServerExchange = httpServerExchange;
        this.session = session;
        this.authenticityToken = authenticityToken;
        this.authentication = authentication;
        this.payload = new Payload();
        this.validator = Application.getInjector().getInstance(Validator.class);
        this.parameter = parameter;
        this.validator.setValues(parameter);
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

    @Override
    public Validator validation() {
        return this.validator;
    }

    @Override
    public String getError(String name) {
        return this.validator.hasError(name) ? this.validator.getError(name) : "";
    }

    public HeaderMap getHeaders() {
        return this.httpServerExchange.getRequestHeaders();
    }

    public String getHeader(HttpString headerName) {
        return (this.httpServerExchange.getRequestHeaders().get(headerName) == null) ? null : this.httpServerExchange.getRequestHeaders().get(headerName).element();
    }
}