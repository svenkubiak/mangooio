package controllers;

import io.mangoo.annotations.FilterWith;
import io.mangoo.filters.AuthenticationFilter;
import io.mangoo.filters.oauth.OAuthCallbackFilter;
import io.mangoo.filters.oauth.OAuthLoginFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Authentication;
import io.mangoo.routing.bindings.Form;

public class AuthenticationController {
    private static final String IDENTIFIER = "jfljfdklöjklöjkfdEweje2kqe";
    private static final String SECRET = "MyVoiceIsMySecret";
    private static final String AUTHENTICATIONREQUIRED = "/authenticationrequired";

    @FilterWith(AuthenticationFilter.class)
    public Response notauthenticated(Authentication authentication) {
        return Response.withOk()
                .andTextBody(authentication.getIdentifier());
    }

    @FilterWith(OAuthLoginFilter.class)
    public Response login() {
        return Response.withOk().andEmptyBody();
    }

    @FilterWith(OAuthCallbackFilter.class)
    public Response authenticate(Authentication authentication) {
        if (authentication.isValid()) {
            authentication.login(IDENTIFIER);
            return Response.withRedirect(AUTHENTICATIONREQUIRED);
        }

        return Response.withOk().andEmptyBody();
    }

    public Response doLogin(Authentication authentication) {
        authentication.login(IDENTIFIER);
        return Response.withRedirect(AUTHENTICATIONREQUIRED);
    }
    
    public Response doLoginTwoFactor(Authentication authentication) {
        authentication.login(IDENTIFIER).twoFactorAuthentication(true);
        
        return Response.withRedirect("/");
    }
    
    public Response factorize(Form form, Authentication authentication) {
        if (authentication.isValid() && authentication.validSecondFactor(SECRET, form.getString("twofactor").orElse(""))) {
            return Response.withRedirect(AUTHENTICATIONREQUIRED);
        }
        
        return Response.withRedirect("/");
    }

    public Response logout(Authentication authentication) {
        authentication.logout();
        return Response.withOk().andEmptyBody();
    }
    
    public Response subject(Authentication authentication) {
        return Response.withOk()
                .andContent("identifier", authentication.getIdentifier());
    }
}