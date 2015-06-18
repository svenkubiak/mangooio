package conf;

import mangoo.io.interfaces.MangooLifecycle;

import com.google.inject.Singleton;

@Singleton
public class Lifecycle implements MangooLifecycle {

    @Override
    public void applicationInitialized() {
        // Do nothing for now
    }

    @Override
    public void applicationStarted() {
        // Do nothing for now
    }
}