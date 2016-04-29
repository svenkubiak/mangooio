package io.mangoo.admin;

import java.util.Base64;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Charsets;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.Default;
import io.mangoo.enums.Template;
import io.mangoo.interfaces.MangooFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.undertow.util.Headers;

/**
 * Authentication filter for administrative URLs
 *
 * @author svenkubiak
 *
 */
public class AdminFilter implements MangooFilter {
    private static final Base64.Decoder base64Decoder = Base64.getDecoder();
    private static final Config CONFIG = Application.getConfig();
    
    @Override
    public Response execute(Request request, Response response) {
        if (!CONFIG.isAdminEnabled()) {
            return Response.withNotFound()
                    .andBody(Template.DEFAULT.notFound())
                    .end();
        }
        
        if (!isAuthenticated(request)) {
            return Response.withUnauthorized()
                    .andHeader(Headers.WWW_AUTHENTICATE, "Basic realm=Administration authentication")
                    .andEmptyBody()
                    .end();
        }

        return response;
    }

    /**
     * Checks via a basic HTTP authentication if user is authenticated
     *
     * @param request The current HTTP request
     * @return True if credentials are valid, false otherwise
     */
    private boolean isAuthenticated(Request request) {
        String username = null;
        String password = null;
        String authInfo = request.getHeader(Headers.AUTHORIZATION);
        if (StringUtils.isNotBlank(authInfo)) {
            authInfo = authInfo.replace("Basic", "");
            authInfo = authInfo.trim();
            authInfo = new String(base64Decoder.decode(authInfo), Charsets.UTF_8);

            final String [] credentials = authInfo.split(":");
            if (credentials != null && credentials.length == Default.BASICAUTH_CREDENTIALS_LENGTH.toInt()) {
                username = credentials[0];
                password = credentials[1];
            }
        }

        return StringUtils.isNotBlank(username) &&
               StringUtils.isNotBlank(password) &&
               StringUtils.isNotBlank(CONFIG.getAdminAuthenticationUser()) &&
               StringUtils.isNotBlank(CONFIG.getAdminAuthenticationPassword()) &&
               CONFIG.getAdminAuthenticationUser().equals(username) &&
               CONFIG.getAdminAuthenticationPassword().equals(DigestUtils.sha512Hex(password));
    }
}