package io.mangoo.filters;

import io.mangoo.constants.ClaimKey;
import io.mangoo.constants.Default;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.exceptions.MangooJwtExeption;
import io.mangoo.interfaces.filters.PerRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.mangoo.utils.JwtUtils;
import io.mangoo.utils.MangooUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.text.ParseException;

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
        if (StringUtils.isNotBlank(uri) && Strings.CI.equalsAny(uri, ALLOWED)) {
            return response;
        }

        var cookie = request.getCookie(Default.APPLICATION_ADMIN_COOKIE_NAME);
        if (cookie != null) {
            String value = cookie.getValue();
            if (StringUtils.isNotBlank(value)) {
                try {
                    var jwtData = JwtUtils.JwtData.create()
                            .withSecret(config.getApplicationSecret())
                            .withIssuer(config.getApplicationName())
                            .withAudience(Default.APPLICATION_ADMIN_COOKIE_NAME)
                            .withTtlSeconds(1800);

                    var jwtClaimSet = JwtUtils.parseJwt(value, jwtData);
                    if (jwtClaimSet.getClaim(ClaimKey.TWO_FACTOR) != null && jwtClaimSet.getBooleanClaim(ClaimKey.TWO_FACTOR)) {
                        return Response.redirect("/@admin/twofactor").end();
                    }

                    response.render("version", VERSION_TAG);
                    return response;
                } catch (ParseException | MangooJwtExeption e) {
                    //NOSONAR Ignore catch
                }
            }
        }
        
        return Response.redirect("/@admin/login").end();
    }
}