package conf;

import io.undertow.util.Methods;
import mangoo.io.interfaces.MangooRoutes;
import mangoo.io.routing.Router;
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

public class Routes implements MangooRoutes {
    @Override
    public void routify() {
        Router.mapRequest(Methods.GET).toUrl("/").onClassAndMethod(ApplicationController.class, "index");
        Router.mapRequest(Methods.GET).toUrl("/redirect").onClassAndMethod(ApplicationController.class, "redirect");
        Router.mapRequest(Methods.GET).toUrl("/text").onClassAndMethod(ApplicationController.class, "text");
        Router.mapRequest(Methods.GET).toUrl("/forbidden").onClassAndMethod(ApplicationController.class, "forbidden");
        Router.mapRequest(Methods.GET).toUrl("/badrequest").onClassAndMethod(ApplicationController.class, "badrequest");
        Router.mapRequest(Methods.GET).toUrl("/unauthorized").onClassAndMethod(ApplicationController.class, "unauthorized");
        Router.mapRequest(Methods.GET).toUrl("/binary").onClassAndMethod(ApplicationController.class, "binary");
        Router.mapRequest(Methods.GET).toUrl("/header").onClassAndMethod(ApplicationController.class, "header");

        Router.mapRequest(Methods.POST).toUrl("/form").onClassAndMethod(FormController.class, "form");

        Router.mapRequest(Methods.GET).toUrl("/render").onClassAndMethod(JsonController.class, "render");
        Router.mapRequest(Methods.POST).toUrl("/parse").onClassAndMethod(JsonController.class, "parse");

        Router.mapRequest(Methods.GET).toUrl("/authenticityform").onClassAndMethod(AuthenticityController.class, "form");
        Router.mapRequest(Methods.GET).toUrl("/authenticitytoken").onClassAndMethod(AuthenticityController.class, "token");
        Router.mapRequest(Methods.GET).toUrl("/valid").onClassAndMethod(AuthenticityController.class, "valid");
        Router.mapRequest(Methods.GET).toUrl("/invalid").onClassAndMethod(AuthenticityController.class, "invalid");

        Router.mapRequest(Methods.GET).toUrl("/string/{foo}").onClassAndMethod(ParameterController.class, "stringParam");
        Router.mapRequest(Methods.GET).toUrl("/double/{foo}").onClassAndMethod(ParameterController.class, "doubleParam");
        Router.mapRequest(Methods.GET).toUrl("/int/{foo}").onClassAndMethod(ParameterController.class, "intParam");
        Router.mapRequest(Methods.GET).toUrl("/float/{foo}").onClassAndMethod(ParameterController.class, "floatParam");
        Router.mapRequest(Methods.GET).toUrl("/multiple/{foo}/{bar}").onClassAndMethod(ParameterController.class, "multipleParam");
        Router.mapRequest(Methods.GET).toUrl("/path").onClassAndMethod(ParameterController.class, "pathParam");

        Router.mapRequest(Methods.GET).toUrl("/session").onClassAndMethod(SessionController.class, "session");

        Router.mapRequest(Methods.GET).toUrl("/authenticationrequired").onClassAndMethod(AuthenticationController.class, "notauthenticated");
        Router.mapRequest(Methods.POST).toUrl("/login").onClassAndMethod(AuthenticationController.class, "login");
        Router.mapRequest(Methods.GET).toUrl("/logout").onClassAndMethod(AuthenticationController.class, "logout");

        Router.mapRequest(Methods.GET).toUrl("/flash").onClassAndMethod(FlashController.class, "flash");
        Router.mapRequest(Methods.GET).toUrl("/flashed").onClassAndMethod(FlashController.class, "flashed");

        Router.mapRequest(Methods.GET).toUrl("/translation").onClassAndMethod(I18nController.class, "translation");

        Router.mapRequest(Methods.GET).toUrl("/filter").onClassAndMethod(FilterController.class, "filter");

        Router.mapWebSocket().toUrl("/websocket").onClass(WebSocketController.class);

        Router.mapResourceFile().toUrl("/robots.txt");
        Router.mapResourcePath().toUrl("/assets/");
    }
}