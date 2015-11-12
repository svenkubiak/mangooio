package conf;

import controllers.ApplicationController;
import controllers.AuthenticationController;
import controllers.AuthenticityController;
import controllers.FilterController;
import controllers.FlashController;
import controllers.FormController;
import controllers.I18nController;
import controllers.JsonController;
import controllers.ParameterController;
import controllers.SessionController;
import controllers.WebSocketController;
import io.mangoo.interfaces.MangooRoutes;
import io.mangoo.routing.Router;
import io.undertow.util.Methods;

public class Routes implements MangooRoutes {
    @Override
    public void routify() {
        Router.mapRequest(Methods.GET).toUrl("/").onController(ApplicationController.class, "index").build();
        Router.mapRequest(Methods.GET).toUrl("/redirect").onController(ApplicationController.class, "redirect").build();
        Router.mapRequest(Methods.GET).toUrl("/text").onController(ApplicationController.class, "text").build();
        Router.mapRequest(Methods.GET).toUrl("/forbidden").onController(ApplicationController.class, "forbidden").build();
        Router.mapRequest(Methods.GET).toUrl("/badrequest").onController(ApplicationController.class, "badrequest").build();
        Router.mapRequest(Methods.GET).toUrl("/unauthorized").onController(ApplicationController.class, "unauthorized").build();
        Router.mapRequest(Methods.GET).toUrl("/binary").onController(ApplicationController.class, "binary").build();
        Router.mapRequest(Methods.GET).toUrl("/header").onController(ApplicationController.class, "header").build();
        Router.mapRequest(Methods.GET).toUrl("/etag").onController(ApplicationController.class, "etag").build();
        Router.mapRequest(Methods.GET).toUrl("/request").onController(ApplicationController.class, "request").build();
        Router.mapRequest(Methods.POST).toUrl("/post").onController(ApplicationController.class, "post").build();
        Router.mapRequest(Methods.PUT).toUrl("/put").onController(ApplicationController.class, "put").build();
        Router.mapRequest(Methods.POST).toUrl("/jsonpathpost").onController(ApplicationController.class, "post").build();
        Router.mapRequest(Methods.PUT).toUrl("/jsonpathput").onController(ApplicationController.class, "put").build();
        Router.mapRequest(Methods.POST).toUrl("/jsonboonpost").onController(ApplicationController.class, "post").build();
        Router.mapRequest(Methods.PUT).toUrl("/jsonboonput").onController(ApplicationController.class, "put").build();   
        
        Router.mapRequest(Methods.POST).toUrl("/form").onController(FormController.class, "form").build();

        Router.mapRequest(Methods.GET).toUrl("/render").onController(JsonController.class, "render").build();
        Router.mapRequest(Methods.POST).toUrl("/parse").onController(JsonController.class, "parse").build();
        Router.mapRequest(Methods.POST).toUrl("/body").onController(JsonController.class, "body").build();
        Router.mapRequest(Methods.POST).toUrl("/requestAndJson").onController(JsonController.class, "requestAndJson").build();

        Router.mapRequest(Methods.GET).toUrl("/authenticityform").onController(AuthenticityController.class, "form").build();
        Router.mapRequest(Methods.GET).toUrl("/authenticitytoken").onController(AuthenticityController.class, "token").build();
        Router.mapRequest(Methods.GET).toUrl("/valid").onController(AuthenticityController.class, "valid").build();
        Router.mapRequest(Methods.GET).toUrl("/invalid").onController(AuthenticityController.class, "invalid").build();

        Router.mapRequest(Methods.GET).toUrl("/string/{foo}").onController(ParameterController.class, "stringParam").build();
        Router.mapRequest(Methods.GET).toUrl("/int/{foo}").onController(ParameterController.class, "intParam").build();
        Router.mapRequest(Methods.GET).toUrl("/integer/{foo}").onController(ParameterController.class, "integerParam").build();
        Router.mapRequest(Methods.GET).toUrl("/doublePrimitive/{foo}").onController(ParameterController.class, "doublePrimitiveParam").build();
        Router.mapRequest(Methods.GET).toUrl("/double/{foo}").onController(ParameterController.class, "doubleParam").build();
        Router.mapRequest(Methods.GET).toUrl("/float/{foo}").onController(ParameterController.class, "floatParam").build();
        Router.mapRequest(Methods.GET).toUrl("/floatPrimitive/{foo}").onController(ParameterController.class, "floatPrimitiveParam").build();
        Router.mapRequest(Methods.GET).toUrl("/longPrimitive/{foo}").onController(ParameterController.class, "longPrimitiveParam").build();
        Router.mapRequest(Methods.GET).toUrl("/long/{foo}").onController(ParameterController.class, "longParam").build();
        Router.mapRequest(Methods.GET).toUrl("/multiple/{foo}/{bar}").onController(ParameterController.class, "multipleParam").build();
        Router.mapRequest(Methods.GET).toUrl("/path").onController(ParameterController.class, "pathParam").build();
        Router.mapRequest(Methods.GET).toUrl("/localdate/{localDate}").onController(ParameterController.class, "localdate").build();
        Router.mapRequest(Methods.GET).toUrl("/localdatetime/{localDateTime}").onController(ParameterController.class, "localdatetime").build();
        
        Router.mapRequest(Methods.GET).toUrl("/session").onController(SessionController.class, "session").build();

        Router.mapRequest(Methods.POST).toUrl("/dologin").onController(AuthenticationController.class, "doLogin").build();
        Router.mapRequest(Methods.GET).toUrl("/authenticationrequired").onController(AuthenticationController.class, "notauthenticated").build();
        Router.mapRequest(Methods.GET).toUrl("/login").onController(AuthenticationController.class, "login").build();
        Router.mapRequest(Methods.GET).toUrl("/authenticate").onController(AuthenticationController.class, "authenticate").build();
        Router.mapRequest(Methods.POST).toUrl("/login").onController(AuthenticationController.class, "login").build();
        Router.mapRequest(Methods.GET).toUrl("/logout").onController(AuthenticationController.class, "logout").build();

        Router.mapRequest(Methods.GET).toUrl("/flash").onController(FlashController.class, "flash").build();
        Router.mapRequest(Methods.GET).toUrl("/flashed").onController(FlashController.class, "flashed").build();

        Router.mapRequest(Methods.GET).toUrl("/translation").onController(I18nController.class, "translation").build();

        Router.mapRequest(Methods.GET).toUrl("/filter").onController(FilterController.class, "filter").build();
        Router.mapRequest(Methods.GET).toUrl("/headerfilter").onController(FilterController.class, "headerfilter").build();

        Router.mapRequest(Methods.POST).toUrl("/validateform").onController(FormController.class, "validateform").build();

        Router.mapWebSocket().toUrl("/websocket").onController(WebSocketController.class).build();
        Router.mapWebSocket().toUrl("/websocketauth").withAuthentication().onController(WebSocketController.class).build();
        
        Router.mapServerSentEvent().withAuthentication().toUrl("/sseauth").build();
        Router.mapServerSentEvent().toUrl("/sse").build();
        
        Router.mapResourceFile().toUrl("/robots.txt").build();
        Router.mapResourcePath().toUrl("/assets/").build();
    }
}