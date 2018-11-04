package app;

import controllers.ApplicationController;
import com.google.inject.Singleton;
import io.mangoo.interfaces.MangooBootstrap;
import io.mangoo.routing.Bind;
import io.mangoo.routing.On;

@Singleton
public class Bootstrap implements MangooBootstrap {

    @Override
    public void initializeRoutes() {
        Bind.controller(ApplicationController.class).withRoutes(
                On.get().to("/").respondeWith("index")
        );
        
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