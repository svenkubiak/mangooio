package app;

import controllers.ApplicationController;
import io.mangoo.routing.Bind;
import io.mangoo.routing.On;

public class Bootstrap implements MangooBootstrap {

    @Override
    public void initializeRoutes() {
        // ApplicationController
        Bind.controller(ApplicationController.class).withRoutes(
                On.get().to("/").respondeWith("index"),
        );
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