package conf;

import com.google.inject.Singleton;

import io.undertow.util.Methods;
import io.mangoo.interfaces.MangooRoutes;
import io.mangoo.routing.Router;
import controllers.ApplicationController;

@Singleton
public class Routes implements MangooRoutes {
    
	@Override
    public void routify() {
        Router.mapRequest(Methods.GET).toUrl("/").onClassAndMethod(ApplicationController.class, "index");
        
        Router.mapResourceFile().toUrl("/robots.txt");
        Router.mapResourcePath().toUrl("/assets/");
    }
}