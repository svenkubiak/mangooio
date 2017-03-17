package controllers;

import io.mangoo.annotations.FilterWith;
import io.mangoo.filters.AuthenticationFilter;
import io.mangoo.filters.oauth.OAuthCallbackFilter;
import io.mangoo.filters.oauth.OAuthLoginFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Authentication;
import io.mangoo.routing.bindings.Form;
import io.mangoo.utils.CodecUtils;

public class AuthenticationController {
    private static final String SECRET = "MyVoiceIsMySecret";

    @FilterWith(AuthenticationFilter.class)
    public Response notauthenticated(Authentication authentication) {
        return Response.withOk()
                .andTextBody(authentication.getAuthenticatedUser());
    }

    @FilterWith(OAuthLoginFilter.class)
    public Response login() {
        return Response.withOk().andEmptyBody();
    }

    @FilterWith(OAuthCallbackFilter.class)
    public Response authenticate(Authentication authentication) {
        if (authentication.hasAuthenticatedUser()) {
            authentication.validLogin(authentication.getAuthenticatedUser(), "bar", CodecUtils.hexJBcrypt("bar"));
            return Response.withRedirect("/authenticationrequired");
        }

        return Response.withOk().andEmptyBody();
    }

    public Response doLogin(Authentication authentication) {
        authentication.validLogin("foo", "bar", CodecUtils.hexJBcrypt("bar"));
        return Response.withRedirect("/authenticationrequired");
    }
    
    public Response doLoginTwoFactor(Authentication authentication) {
        authentication.validLogin("foo", "bar", CodecUtils.hexJBcrypt("bar"));
        authentication.twoFactorAuthentication(true);
        
        return Response.withRedirect("/");
    }
    
    public Response factorize(Form form, Authentication authentication) {
        if (authentication.hasAuthenticatedUser() && authentication.validSecondFactor(SECRET, form.getInteger("twofactor").orElse(0))) {
            System.out.println("tadatatatatat");
            return Response.withRedirect("/authenticationrequired");
        }
        
        return Response.withRedirect("/");
    }

    public Response logout(Authentication authentication) {
        authentication.logout();
        return Response.withOk().andEmptyBody();
    }
}