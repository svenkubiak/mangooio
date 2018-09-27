package app;

import controllers.ApplicationController;
import controllers.AuthenticationController;
import controllers.AuthenticityController;
import controllers.AuthorizationController;
import controllers.BasicAuthenticationController;
import controllers.FilterController;
import controllers.FlashController;
import controllers.FormController;
import controllers.I18nController;
import controllers.JsonController;
import controllers.ParameterController;
import controllers.SessionController;
import controllers.WebSocketController;
import controllers.subcontrollers.SubController;
import io.mangoo.interfaces.MangooBootstrap;
import io.mangoo.routing.Bind;
import io.mangoo.routing.On;

public class Bootstrap implements MangooBootstrap {

    @Override
    public void initializeRoutes() {
        // SessionController
        Bind.controller(SessionController.class).withRoutes(
                On.get().to("/session").respondeWith("session"),
                On.get().to("/session/valued/{uuid}").respondeWith("valued")
        );
    
        // AuthenticityController
        Bind.controller(AuthenticityController.class).withRoutes(
                On.get().to("/authenticityform").respondeWith("form"),
                On.get().to("/authenticitytoken").respondeWith("token"),
                On.get().to("/valid").respondeWith("valid"),
                On.get().to("/invalid").respondeWith("invalid")
        );
        
        // AuthorizationController
        Bind.controller(AuthorizationController.class).withRoutes(
                On.get().to("/authorize/{subject}").respondeWith("authorize"),
                On.get().to("/read").respondeWith("read").requireAuthorization(),
                On.post().to("/write").respondeWith("write").requireAuthorization()
        );
        
        // FilterController
        Bind.controller(FilterController.class).withRoutes(
                On.get().to("/filter").respondeWith("filter"),
                On.get().to("/headerfilter").respondeWith("headerfilter"),
                On.get().to("/filters").respondeWith("filters")
        );
        
        // I18nController
        Bind.controller(I18nController.class).withRoutes(
                On.get().to("/translation").respondeWith("translation"),
                On.get().to("/messages").respondeWith("messages"),
                On.get().to("/special").respondeWith("special"),
                On.get().to("/umlaute").respondeWith("umlaute"),
                On.get().to("/localize").respondeWith("localize")
        );
        
        // FlashController
        Bind.controller(FlashController.class).withRoutes(
                On.get().to("/flash").respondeWith("flash"),
                On.get().to("/flashed").respondeWith("flashed")
        );
        
        // JsonController
        Bind.controller(JsonController.class).withRoutes(
                On.get().to("/render").respondeWith("render"),
                On.post().to("/parse").respondeWith("parse"),
                On.put().to("/parse").respondeWith("parse"),
                On.patch().to("/parse").respondeWith("parse"),
                On.post().to("/body").respondeWith("body"),
                On.post().to("/requestAndJson").respondeWith("requestAndJson")
        );
        
        // FormController
        Bind.controller(FormController.class).withRoutes(
                On.post().to("/form").respondeWith("form"),
                On.post().to("/multivalued").respondeWith("multivalued"),
                On.post().to("/submit").respondeWith("submit"),
                On.get().to("/flashify").respondeWith("flashify"),
                On.post().to("/singlefile").respondeWith("singlefile"),
                On.post().to("/multifile").respondeWith("multifile"),
                On.post().to("/validateform").respondeWith("validateform")
        );
        
        // AuthenticationController
        Bind.controller(AuthenticationController.class)
        .withRoutes(
                On.post().to("/dologin").respondeWith("doLogin"),
                On.post().to("/login").respondeWith("login"),
                On.get().to("/login").respondeWith("login"),
                On.get().to("/subject").respondeWith("subject"),
                On.get().to("/authenticationrequired").respondeWith("notauthenticated").requireAuthentication(),
                On.get().to("/authenticate").respondeWith("authenticate"),
                On.get().to("/logout").respondeWith("logout")
        );
        
        // ParameterController
        Bind.controller(ParameterController.class)
        .withRoutes(
                On.get().to("/string/{foo}").respondeWith("stringParam"),
                On.get().to("/optional/{foo}").respondeWith("optionalParam"),
                On.get().to("/string").respondeWith("stringParam"),
                On.get().to("/int/{foo}").respondeWith("intParam"),
                On.get().to("/integer/{foo}").respondeWith("integerParam"),
                On.get().to("/doublePrimitive/{foo}").respondeWith("doublePrimitiveParam"),
                On.get().to("/double/{foo}").respondeWith("doubleParam"),
                On.get().to("/float/{foo}").respondeWith("floatParam"),
                On.get().to("/floatPrimitive/{foo}").respondeWith("floatPrimitiveParam"),
                On.get().to("/longPrimitive/{foo}").respondeWith("longPrimitiveParam"),
                On.get().to("/long/{foo}").respondeWith("longParam"),
                On.get().to("/multiple/{foo}/{bar}").respondeWith("multipleParam"),
                On.get().to("/path").respondeWith("pathParam"),
                On.get().to("/localdate/{localDate}").respondeWith("localdate"),
                On.get().to("/localdatetime/{localDateTime}").respondeWith("localdatetime")
         );
        
         // ApplicationController
         Bind.controller(ApplicationController.class).withRoutes(
                On.get().to("/").respondeWith("index").canBlock(),
                On.get().to("/route").respondeWith("route"),
                On.post().to("/").respondeWith("index"),
                On.put().to("/put").respondeWith("put"),
                On.patch().to("/").respondeWith("index"),
                On.head().to("/").respondeWith("index"),
                On.delete().to("/").respondeWith("index"),
                On.options().to("/").respondeWith("index"),
                On.get().to("/reverse").respondeWith("reverse"),
                On.get().to("/location").respondeWith("location"),
                On.get().to("/prettytime").respondeWith("prettytime"),
                On.get().to("/location/{myloca}").respondeWith("location"),
                On.get().to("/redirect").respondeWith("redirect"),
                On.get().to("/limit").respondeWith("limit").withRequestLimit(10),
                On.get().to("/text").respondeWith("text"),
                On.get().to("/forbidden").respondeWith("forbidden"),
                On.get().to("/badrequest").respondeWith("badrequest"),
                On.get().to("/unauthorized").respondeWith("unauthorized"),
                On.get().to("/binary").respondeWith("binary"),
                On.get().to("/header").respondeWith("header").canBlock(),
                On.get().to("/etag").respondeWith("etag"),
                On.get().to("/request").respondeWith("request"),
                On.post().to("/post").respondeWith("post"),
                On.put().to("/post").respondeWith("put"),
                On.post().to("/jsonpathpost").respondeWith("jsonPathPost"),
                On.put().to("/jsonpathput").respondeWith("jsonPathPut"),
                On.post().to("/jsonboonpost").respondeWith("jsonBoonPost"),
                On.put().to("/jsonboonput").respondeWith("jsonBoonPut"),
                On.get().to("/freemarker").respondeWith("freemarker")
         );
         
         // SubController
         Bind.controller(SubController.class).withRoutes(
                 On.get().to("/subcontroller").respondeWith("check")
         );
         
         // BasicAuthenticationController
         Bind.controller(BasicAuthenticationController.class).withRoutes(
                 On.get().to("/basicauth").respondeWith("basicauth").withBasicAuthentication("foo", "bar")
         );
         
         Bind.serverSentEvent().to("/sse");
         Bind.serverSentEvent().to("/sseauth").requireAuthentication();
         
         Bind.webSocket().onController(WebSocketController.class).to("/websocket");
         Bind.webSocket().onController(WebSocketController.class).to("/websocketauth").requireAuthentication();

         Bind.pathResource().to("/assets/");
         Bind.fileResource().to("/robots.txt");
    }
    
    @Override
    public void applicationInitialized() {
        // TODO Auto-generated method stub
    }

    @Override
    public void applicationStarted() {
        // TODO Auto-generated method stub
    }

    @Override
    public void applicationStopped() {
        // TODO Auto-generated method stub
    }
}