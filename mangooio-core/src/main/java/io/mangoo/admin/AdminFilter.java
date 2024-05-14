package io.mangoo.admin;

import io.mangoo.constants.ClaimKey;
import io.mangoo.constants.Default;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.exceptions.MangooTokenException;
import io.mangoo.interfaces.filters.PerRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.mangoo.utils.MangooUtils;
import io.mangoo.utils.token.TokenParser;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

public class AdminFilter implements PerRequestFilter {
    private static final String VERSION_TAG = MangooUtils.getVersion();
    
    @Override
    public Response execute(Request request, Response response) {
        var config = Application.getInstance(Config.class);
        var cookie = request.getCookie(Default.ADMIN_COOKIE_NAME);
        
        if (cookie != null) {
            String value = cookie.getValue();
            if (StringUtils.isNotBlank(value)) {
                try {
                    var token = TokenParser.create()
                        .withSharedSecret(config.getApplicationSecret())
                        .withCookieValue(value)
                        .parse();

                    if (token.expirationIsAfter(LocalDateTime.now())) {
                        if (token.containsClaim(ClaimKey.TWO_FACTOR) && token.getClaim(ClaimKey.TWO_FACTOR, Boolean.class)) {
                            return Response.redirect("/@admin/twofactor").end();
                        }
                        
                        response.render("version", VERSION_TAG);
                        return response;
                    }
                } catch (MangooTokenException e) {
                    //NOSONAR Ignore catch
                }
            }
        }
        
        return Response.redirect("/@admin/login").end();
    }
}