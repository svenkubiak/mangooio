package controllers;

import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Authentication;
import io.mangoo.routing.bindings.Form;

public class AuthenticationController {
    private static final String SUBJECT = "mysubject";
    public static final String SECRET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
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
        if (authentication.hasSubject() && authentication.isValidSecondFactor(authentication.getSubject(), SECRET, form.getString("twofactor").orElse(""))) {
            authentication.twoFactorAuthentication(false);
            authentication.update();

            return Response.redirect(AUTHENTICATIONREQUIRED);
        }

        return Response.redirect("/");
    }

    /**
     * Reports the authentication state on a route that is not bound with
     * withAuthentication(), which is the path an application takes when it builds
     * its own filter on top of the Authentication object
     */
    public Response state(Authentication authentication) {
        return Response.ok().bodyText(authentication.hasSubject()
                + ":" + authentication.isValid()
                + ":" + authentication.isTwoFactor());
    }

    public Response logout(Authentication authentication) {
        authentication.logout();
        return Response.ok();
    }
    
    public Response subject(Authentication authentication) {
        return Response.ok().render("identifier", authentication.getSubject());
    }
}