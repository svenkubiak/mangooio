package controllers;

import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Authentication;
import io.mangoo.routing.bindings.Form;

public class AuthenticationController {
    private static final String SUBJECT = "mysubject";
    private static final String SECRET = "MyVoiceIsMySecret";
    private static final String AUTHENTICATIONREQUIRED = "/authenticationrequired";

    public Response notauthenticated(Authentication authentication) {
        return Response.ok()
                .bodyText(authentication.getSubject());
    }

    public Response login() {
        return Response.ok();
    }

    public Response authenticate(Authentication authentication) {
        if (authentication.isValid()) {
            authentication.login(SUBJECT);
            return Response.redirect(AUTHENTICATIONREQUIRED);
        }

        return Response.ok();
    }

    public Response doLogin(Authentication authentication) {
        authentication.login(SUBJECT);
        return Response.redirect(AUTHENTICATIONREQUIRED);
    }
    
    public Response doLoginTwoFactor(Authentication authentication) {
        authentication.login(SUBJECT).twoFactorAuthentication(true);
        
        return Response.redirect("/");
    }
    
    public Response factorize(Form form, Authentication authentication) {
        if (authentication.isValid() && authentication.validSecondFactor(SECRET, form.getString("twofactor").orElse(""))) {
            return Response.redirect(AUTHENTICATIONREQUIRED);
        }
        
        return Response.redirect("/");
    }

    public Response logout(Authentication authentication) {
        authentication.logout();
        return Response.ok();
    }
    
    public Response subject(Authentication authentication) {
        return Response.ok().render("identifier", authentication.getSubject());
    }
}