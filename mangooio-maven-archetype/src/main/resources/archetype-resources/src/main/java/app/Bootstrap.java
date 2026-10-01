package app;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import controllers.ApplicationController;
import io.mangoo.interfaces.MangooBootstrap;
import io.mangoo.persistence.interfaces.Datastore;
import io.mangoo.routing.Bind;
import io.mangoo.routing.On;
import models.Person;

import java.util.Objects;

@Singleton
public class Bootstrap implements MangooBootstrap {
    private Datastore datastore;
    
    @Inject
    public Bootstrap(Datastore datastore) {
        this.datastore = Objects.requireNonNull(datastore, "datastore can not be null");
    }
    
    @Override
    public void initializeRoutes() {
        Bind.controller(ApplicationController.class).withRoutes(
                On.get().to("/").respondeWith("index"),
                On.get().to("/persons").respondeWith("persons")
        );
        
        Bind.pathResource().to("/assets/");
        Bind.fileResource().to("/robots.txt");
    }
    
    @Override
    public void applicationInitialized() {
        // Intentionally empty
    }

    @Override
    public void applicationStarted() {
        datastore.save(new Person("Richard M.", "Whittaker", 33));
        datastore.save(new Person("Kitty D.", "Glenn", 45));
        datastore.save(new Person("Raul E.", "Kuhn", 46));
    }

    @Override
    public void applicationStopped() {
        // Intentionally empty
    }
}