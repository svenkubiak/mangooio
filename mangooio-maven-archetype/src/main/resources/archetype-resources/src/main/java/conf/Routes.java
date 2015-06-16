package conf;

import io.undertow.util.Methods;
import mangoo.io.interfaces.MangooRoutes;
import mangoo.io.routing.Router;
import controllers.ApplicationController;

public class Routes implements MangooRoutes {
    @Override
    public void routify() {
        Router.mapRequest(Methods.GET).toUrl("/").onClassAndMethod(ApplicationController.class, "index");
        
        Router.mapResourceFile().toUrl("/robots.txt");
        Router.mapResourcePath().toUrl("/assets/");
    }
}