package mangoo.io.routing.bindings;

import io.undertow.server.HttpServerExchange;
import mangoo.io.authentication.Authentication;

/**
 *
 * @author svenkubiak
 *
 */
public class Exchange {
    private HttpServerExchange httpServerExchange;
    private Session session;
    private String authenticityToken;
    private Authentication authentication;

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
}