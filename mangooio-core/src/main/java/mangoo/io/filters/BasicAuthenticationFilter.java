package mangoo.io.filters;

import org.bouncycastle.util.encoders.Base64;

import com.google.common.base.Charsets;
import com.google.inject.Inject;

import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import mangoo.io.configuration.Config;
import mangoo.io.enums.Key;
import mangoo.io.interfaces.MangooAuthenticator;
import mangoo.io.interfaces.MangooControllerFilter;
import mangoo.io.routing.bindings.Exchange;

/**
 *
 * @author skubiak
 *
 */
public class BasicAuthenticationFilter implements MangooControllerFilter {
    private static final int CREDENTIALS_LENGTH = 2;

    @Inject
    private Config config;

    @Inject
    private MangooAuthenticator mangooAuthenticator;

    @Override
    public boolean filter(Exchange exchange) {
        HeaderValues headerValues = exchange.getHttpServerExchange().getRequestHeaders().get(Headers.AUTHORIZATION_STRING);

        String username = null;
        String password = null;
        String authInfo = null;
        if (headerValues != null) {
            authInfo = headerValues.get(0);
            authInfo = authInfo.replace("Basic", "");
            authInfo = authInfo.trim();
            authInfo = new String(Base64.decode(authInfo), Charsets.UTF_8);

            String [] credentials = authInfo.split(":");
            if (credentials != null && credentials.length == CREDENTIALS_LENGTH) {
                username = credentials[0];
                password = credentials[1];
            }
        }

        if (!mangooAuthenticator.validCredentials(username, password)) {
            exchange.getHttpServerExchange().setResponseCode(StatusCodes.UNAUTHORIZED);
            exchange.getHttpServerExchange().getResponseHeaders().add(Headers.WWW_AUTHENTICATE, "Basic realm=" + config.getString(Key.APPLICATION_NAME));
            exchange.getHttpServerExchange().getResponseSender().send("");

            return false;
        }

        return true;
    }
}