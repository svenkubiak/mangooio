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
import io.mangoo.routing.Routing;

public class Routes implements MangooRoutes {
    @Override
    public void routify() {
        applicationControllerRoutes();  
        formControllerRoutes();
        jsonControllerRoutes();      
        authenticityControllerRoutes();                  
        parameterControllerRoutes();
        sessionControllerRoutes();
        authenticationContrllerRoutes();              
        flashControllerRoutes();
        i18nControllerRoutes();
        filterControllerRoutes();
        webSocketRoutes();
        serverSentEventRoutes();
        resourceFileRoutes();
        resourcePathRoutes();
    }

    private void resourcePathRoutes() {
        Routing.ofResourcePath()
            .to("/assets/").add();
    }

    private void resourceFileRoutes() {
        Routing.ofResourceFile()
            .to("/robots.txt").add();
    }

    private void serverSentEventRoutes() {
        Routing.ofServerSentEvent()
            .to("/sse").add()
            .to("/sseauth").withAuthentication().add();
    }

    private void webSocketRoutes() {
        Routing.ofWebSocket(WebSocketController.class)
            .to("/websocket").add()
            .to("/websocketauth").withAuthentication().add();
    }

    private void filterControllerRoutes() {
        Routing.ofController(FilterController.class)
            .get().to("/filter").call("filter").add()
            .get().to("/headerfilter").call("headerfilter").add();
    }

    private void i18nControllerRoutes() {
        Routing.ofController(I18nController.class)
            .get().to("/translation").call("translation").add();
    }

    private void flashControllerRoutes() {
        Routing.ofController(FlashController.class)
            .get().to("/flash").call("flash").add()
            .get().to("/flashed").call("flashed").add();
    }

    private void authenticationContrllerRoutes() {
        Routing.ofController(AuthenticationController.class)
            .post()
                .to("/dologin").call("doLogin").add()
            .get()
                .to("/authenticationrequired").call("notauthenticated").add()       
            .get()
                .to("/login").call("login").add()
            .post()
                .to("/login").call("login").add()                
            .get()
                .to("/authenticate").call("authenticate").add()                
            .get()
                .to("/logout").call("logout").add();
    }

    private void sessionControllerRoutes() {
        Routing.ofController(SessionController.class)
            .get()
                .to("/session").call("session").add();
    }

    private void parameterControllerRoutes() {
        Routing.ofController(ParameterController.class)
            .get()
                .to("/string/{foo}").call("stringParam").add()
            .get()
                .to("/int/{foo}").call("intParam").add()    
            .get()
                .to("/integer/{foo}").call("integerParam").add()                     
            .get()
                .to("/doublePrimitive/{foo}").call("doublePrimitiveParam").add()  
            .get()
                .to("/double/{foo}").call("doubleParam").add()                  
             .get()
                .to("/float/{foo}").call("floatParam").add()
             .get()
                .to("/floatPrimitive/{foo}").call("floatPrimitiveParam").add()  
             .get()
                .to("/longPrimitive/{foo}").call("longPrimitiveParam").add()
             .get()
                .to("/long/{foo}").call("longParam").add()               
             .get()
                .to("/multiple/{foo}/{bar}").call("multipleParam").add()     
             .get()
                .to("/path").call("pathParam").add()
             .get()
                .to("/localdate/{localDate}").call("localdate").add()
             .get()
                .to("/localdatetime/{localDateTime}").call("localdatetime").add();
    }

    private void authenticityControllerRoutes() {
        Routing.ofController(AuthenticityController.class)
            .get()
                .to("/authenticityform").call("form").add()
            .get()
                .to("/authenticitytoken").call("token").add()
            .get()
                .to("/valid").call("valid").add()
            .get()
                .to("/invalid").call("invalid").add();
    }

    private void jsonControllerRoutes() {
        Routing.ofController(JsonController.class)
            .get()
                .to("/render").call("render").add()
            .post()
                .to("/parse").call("parse").add()
            .post()   
                .to("/body").call("body").add()
            .post()
                .to("/requestAndJson").call("requestAndJson").add();
    }

    private void formControllerRoutes() {
        Routing.ofController(FormController.class)
            .post()
                .to("form").call("form").add()
            .post()
                .to("/validateform").call("validateform").add();                
    }

    private void applicationControllerRoutes() {
        Routing.ofController(ApplicationController.class)
            .get()
                .to("/").call("index").add()
            .get()
                .to("/redirect").call("redirect").add()
            .get()
                .to("/text").call("text").add()
            .get()
                .to("/forbidden").call("forbidden").add()
            .get()
                .to("/badrequest").call("badrequest").add()
            .get()
                .to("/unauthorized").call("unauthorized").add()
            .get()
                .to("/binary").call("binary").add()
            .get()
                .to("/header").call("header").add()
            .get()
                .to("/etag").call("etag").add()
            .get()
                .to("/request").call("request").add()
            .post()
                .to("/post").call("post").add()               
            .put()
                .to("/put").call("put").add()
            .post()
                .to("/jsonpathpost").call("post").add()
            .put()
                .to("/jsonpathput").call("put").add()  
            .post()
                .to("/jsonboonpost").call("post").add()        
            .put()
                .to("/jsonboonput").call("put").add();
    }
}