package io.mangoo.admin;

import java.util.Base64;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Charsets;
import com.google.inject.Inject;

import io.mangoo.configuration.Config;
import io.mangoo.enums.Default;
import io.mangoo.enums.Header;
import io.mangoo.enums.Required;
import io.mangoo.enums.Template;
import io.mangoo.interfaces.MangooFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;

/**
 * Authentication filter for administrative area
 *
 * @author svenkubiak
 *
 */
public class AdminFilter implements MangooFilter {
    private static final Base64.Decoder decoder = Base64.getDecoder();
    private Config config;
    
    @Inject
    public AdminFilter (Config config) {
        this.config = Objects.requireNonNull(config, Required.CONFIG.toString());
    }
    
    @Override
    public Response execute(Request request, Response response) {
        if (!this.config.isApplicationAdminEnable()) {
            return Response.withNotFound()
                    .andBody(Template.DEFAULT.notFound())
                    .end();
        }
        
        if (!isAuthenticated(request)) {
            return Response.withUnauthorized()
                    .andHeader(Header.WWW_AUTHENTICATE.toHttpString(), "Basic realm=Administration authentication")
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
        String authInfo = request.getHeader(Header.AUTHORIZATION.toHttpString());
        if (StringUtils.isNotBlank(authInfo)) {
            authInfo = StringUtils.replace(authInfo, "Basic", "");
            authInfo = authInfo.trim();
            authInfo = new String(decoder.decode(authInfo), Charsets.UTF_8);

            final String [] credentials = authInfo.split(":");
            if (credentials != null && credentials.length == Default.BASICAUTH_CREDENTIALS_LENGTH.toInt()) {
                username = credentials[0];
                password = credentials[1];
            }
        }

        return StringUtils.isNotBlank(username) &&
               StringUtils.isNotBlank(password) &&
               StringUtils.isNotBlank(this.config.getApplicationAdminUsername()) &&
               StringUtils.isNotBlank(this.config.getApplicationAdminPassword()) &&
               this.config.getApplicationAdminUsername().equals(username);
    }
}