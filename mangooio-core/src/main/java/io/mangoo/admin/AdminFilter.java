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
import io.mangoo.utils.paseto.PasetoParser;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

public class AdminFilter implements PerRequestFilter {
    private static final String VERSION_TAG = MangooUtils.getVersion();
    private static final String[] ALLOWED = {
            "/@admin/login",
            "/@admin/logout",
            "/@admin/authenticate",
            "/@admin/twofactor",
            "/@admin/verify"};

    @Override
    public Response execute(Request request, Response response) {
        var config = Application.getInstance(Config.class);
        response.render("mangooioAdminLocale", config.getApplicationAdminLocale());

        var uri = request.getURI();
        if (StringUtils.isNotBlank(uri) && StringUtils.equalsAny(uri, ALLOWED)) {
            return response;
        }

        var cookie = request.getCookie(Default.ADMIN_COOKIE_NAME);
        if (cookie != null) {
            String value = cookie.getValue();
            if (StringUtils.isNotBlank(value)) {
                try {
                    var token = PasetoParser.create()
                        .withSecret(config.getApplicationSecret())
                        .withCookieValue(value)
                        .parse();

                    if (token.getExpires().isAfter(LocalDateTime.now())) {
                        if (token.containsClaim(ClaimKey.TWO_FACTOR) && token.getClaimAsBoolean(ClaimKey.TWO_FACTOR)) {
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