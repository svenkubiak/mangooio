//package controllers;
//
//import io.mangoo.annotations.FilterWith;
//import io.mangoo.authentication.Authentication;
//import io.mangoo.filters.AuthenticationFilter;
//import io.mangoo.filters.oauth.OAuthCallbackFilter;
//import io.mangoo.filters.oauth.OAuthLoginFilter;
//import io.mangoo.routing.Response;
 //
//public class AuthenticationController {
//
//    @FilterWith(AuthenticationFilter.class)
//    public Response notauthenticated(Authentication authentication) {
//        return Response.withOk()
//                .andTextBody(authentication.getAuthenticatedUser());
//    }
//
//    @FilterWith(OAuthLoginFilter.class)
//    public Response login() {
//        return Response.withOk().andEmptyBody();
//    }
//
//    @FilterWith(OAuthCallbackFilter.class)
//    public Response authenticate(Authentication authentication) {
//        if (authentication.hasAuthenticatedUser()) {
//            authentication.login(authentication.getAuthenticatedUser(), false);
//            return Response.withRedirect("/authenticationrequired");
//        }
//
//        return Response.withOk().andEmptyBody();
//    }
//
//    public Response doLogin(Authentication authentication) {
//        authentication.login("foo", false);
//        return Response.withRedirect("/authenticationrequired");
//    }
//
//    public Response logout(Authentication authentication) {
//        authentication.logout();
//        return Response.withOk().andEmptyBody();
//    }
//}