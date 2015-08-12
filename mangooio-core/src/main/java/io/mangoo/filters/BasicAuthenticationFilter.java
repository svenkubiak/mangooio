package io.mangoo.filters;

import org.bouncycastle.util.encoders.Base64;

import com.google.common.base.Charsets;
import com.google.inject.Inject;

import io.mangoo.configuration.Config;
import io.mangoo.enums.Key;
import io.mangoo.interfaces.MangooAuthenticator;
import io.mangoo.interfaces.MangooFilter;
import io.mangoo.routing.bindings.Request;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

/**
 *
 * @author skubiak
 *
 */
public class BasicAuthenticationFilter implements MangooFilter {
    private static final int CREDENTIALS_LENGTH = 2;

    @Inject
    private Config config;

    @Inject
    private MangooAuthenticator mangooAuthenticator;

    @Override
    public boolean continueRequest(Request request) {
        HeaderValues headerValues = request.getHttpServerExchange().getRequestHeaders().get(Headers.AUTHORIZATION_STRING);

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
            request.getHttpServerExchange().setResponseCode(StatusCodes.UNAUTHORIZED);
            request.getHttpServerExchange().getResponseHeaders().add(Headers.WWW_AUTHENTICATE, "Basic realm=" + config.getString(Key.APPLICATION_NAME));
            request.getHttpServerExchange().getResponseSender().send("");

            return false;
        }

        return true;
    }
}