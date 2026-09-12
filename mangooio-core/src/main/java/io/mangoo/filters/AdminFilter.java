package io.mangoo.filters;

import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.interfaces.filters.PerRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.mangoo.utils.internal.MangooUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

public class AdminFilter implements PerRequestFilter {
    private static final String VERSION_TAG = MangooUtils.getVersion();
    private static final String ADMIN_INDEX = "/@admin";
    private static final String ADMIN_LOGIN = "/@admin/login";
    private static final String ADMIN_TWO_FACTOR = "/@admin/twofactor";
    private static final String[] PUBLIC = {
            ADMIN_LOGIN,
            "/@admin/logout",
            "/@admin/authenticate"};
    private static final String[] PRE_AUTHENTICATED = {
            ADMIN_TWO_FACTOR,
            "/@admin/verify"};

    @Override
    public Response execute(Request request, Response response) {
        var config = Application.getInstance(Config.class);
        response.render("mangooioAdminLocale", config.getApplicationAdminLocale());

        var uri = request.getURI();
        if (StringUtils.isBlank(uri)) {
            return Response.redirect(ADMIN_LOGIN).end();
        }

        if (Strings.CI.equalsAny(uri, PUBLIC)) {
            return response;
        }

        var claims = MangooUtils.parseAdminCookie(request).orElse(null);
        if (claims == null) {
            return Response.redirect(ADMIN_LOGIN).end();
        }

        boolean twoFactorRoute = Strings.CI.equalsAny(uri, PRE_AUTHENTICATED);
        if (MangooUtils.isTwoFactorPending(claims)) {
            // Only the first factor has been passed, the two factor routes are the
            // only ones reachable until the second factor has been verified
            if (twoFactorRoute) {
                return response;
            }

            return Response.redirect(ADMIN_TWO_FACTOR).end();
        }

        if (twoFactorRoute) {
            return Response.redirect(ADMIN_INDEX).end();
        }

        response.render("version", VERSION_TAG);
        return response;
    }
}
