package conf;

import com.google.inject.Singleton;

import io.mangoo.interfaces.MangooLifecycle;

@Singleton
public class Lifecycle implements MangooLifecycle {
    private int initialize;
    private int started;

    @Override
    public void applicationInitialized() {
        this.initialize++;
    }

    @Override
    public void applicationStarted() {
        this.started++;
    }

    @Override
    public void applicationStopped() {
        // do nothing for now
    }

    public int getInitialize() {
        return initialize;
    }

    public int getStarted() {
        return started;
    }
}