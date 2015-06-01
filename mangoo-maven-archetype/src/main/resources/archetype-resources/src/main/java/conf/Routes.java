package conf;

import io.undertow.util.Methods;
import mangoo.io.interfaces.MangooRoutes;
import mangoo.io.routing.Router;
import controllers.ApplicationController;

public class Routes implements MangooRoutes {
    @Override
    public void routify(Router router) {
        router.mapRequest(Methods.GET).to("/").on(ApplicationController.class, "index");
    }
}