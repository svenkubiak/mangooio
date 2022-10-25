package io.mangoo.admin;

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;

import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.enums.Default;
import io.mangoo.exceptions.MangooTokenException;
import io.mangoo.interfaces.filters.PerRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.mangoo.utils.MangooUtils;
import io.mangoo.utils.token.TokenParser;

/**
 * 
 * @author svenkubiak
 *
 */
public class AdminFilter implements PerRequestFilter {
    private static final String VERSION_TAG = MangooUtils.getVersion();
    
    @Override
    public Response execute(Request request, Response response) {
        var config = Application.getInstance(Config.class);
        var cookie = request.getCookie(Default.ADMIN_COOKIE_NAME.toString());
        
        if (cookie != null) {
            String value = cookie.getValue();
            if (StringUtils.isNotBlank(value)) {
                try {
                    var token = TokenParser.create()
                        .withSharedSecret(config.getApplicationSecret())
                        .withCookieValue(value)
                        .parse();

                    if (token.expirationIsAfter(LocalDateTime.now())) {
                        if (token.containsClaim("twofactor") && token.getClaim("twofactor", Boolean.class)) {
                            return Response.withRedirect("/@admin/twofactor").andEndResponse();
                        }
                        
                        response.andContent("version", VERSION_TAG);
                        return response;
                    }
                } catch (MangooTokenException e) {
                    //NOSONAR Ignore catch
                }
            }
        }
        
        return Response.withRedirect("/@admin/login").andEndResponse();
    }
}