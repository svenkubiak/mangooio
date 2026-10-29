package app;

import controllers.SmokeController;
import io.mangoo.interfaces.MangooBootstrap;
import io.mangoo.routing.Bind;
import io.mangoo.routing.On;
import jakarta.inject.Singleton;

@Singleton
public class Bootstrap implements MangooBootstrap {

    @Override
    public void initializeRoutes() {
        Bind.controller(SmokeController.class).withRoutes(
                On.get().to("/instance").respondeWith("instance"),
                On.post().to("/login").respondeWith("login"),
                On.post().to("/totp").respondeWith("totp"),
                On.get().to("/me").respondeWith("me"),
                On.get().to("/locked/{user}").respondeWith("locked"),
                On.get().to("/logout").respondeWith("logout"),
                On.post().to("/send").respondeWith("send"),
                On.get().to("/slow").respondeWith("slow")
        );

        Bind.serverSentEvent().to("/sse");
    }

    @Override
    public void applicationInitialized() {
        // Intentionally empty
    }

    @Override
    public void applicationStarted() {
        // Intentionally empty
    }

    @Override
    public void applicationStopped() {
        // Intentionally empty
    }
}
