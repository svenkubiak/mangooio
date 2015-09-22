package controllers;

import io.mangoo.annotations.FilterWith;
import io.mangoo.authentication.Authentication;
import io.mangoo.filters.AuthenticationFilter;
import io.mangoo.filters.OAuthCallbackFilter;
import io.mangoo.filters.OAuthLoginFilter;
import io.mangoo.models.OAuthUser;
import io.mangoo.routing.Response;

public class AuthenticationController {

    @FilterWith(AuthenticationFilter.class)
    public Response notauthenticated() {
        return Response.withOk().andEmptyBody();
    }

    @FilterWith(OAuthLoginFilter.class)
    public Response login(Authentication authentication) {
        authentication.login("user", false);
        return Response.withOk().andEmptyBody();
    }

    @FilterWith(OAuthCallbackFilter.class)
    public Response authenticate(Authentication authentication) {
        OAuthUser oAuthUser = authentication.getOAuthUser();
        if (oAuthUser != null) {
            authentication.login(oAuthUser.getUsername(), false);
            return Response.withRedirect("/authenticationrequired");
        }

        return Response.withOk().andEmptyBody();
    }

    public Response logout(Authentication authentication) {
        authentication.logout();
        return Response.withOk().andEmptyBody();
    }
}