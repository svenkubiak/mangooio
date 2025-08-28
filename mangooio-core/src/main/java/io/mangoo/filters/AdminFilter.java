package io.mangoo.filters;

import io.mangoo.constants.ClaimKey;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.exceptions.MangooJwtException;
import io.mangoo.interfaces.filters.PerRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.mangoo.utils.AdminUtils;
import io.mangoo.utils.JwtUtils;
import io.mangoo.utils.MangooUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;

public class AdminFilter implements PerRequestFilter {
    private static final Logger LOG = LogManager.getLogger(AdminFilter.class);
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

        var cookie = request.getCookie(AdminUtils.getAdminCookieName());
        if (cookie != null) {
            String value = cookie.getValue();
            if (StringUtils.isNotBlank(value)) {
                try {
                    var jwtData = JwtUtils.JwtData.create()
                            .withKey(config.getApplicationSecret())
                            .withSecret(config.getApplicationSecret().getBytes(StandardCharsets.UTF_8))
                            .withIssuer(config.getApplicationName())
                            .withAudience(AdminUtils.getAdminCookieName())
                            .withTtlSeconds(1800);

                    var jwtClaimSet = JwtUtils.parseJwt(value, jwtData);
                    if (("true").equals(jwtClaimSet.getClaimAsString(ClaimKey.TWO_FACTOR))) {
                        return Response.redirect("/@admin/twofactor").end();
                    }

                    response.render("version", VERSION_TAG);
                    return response;
                } catch (ParseException | MangooJwtException e) {
                    LOG.error("Failed to parse admin cookie -> {}", e.getCause(), e);
                }
            }
        }
        
        return Response.redirect("/@admin/login").end();
    }
}