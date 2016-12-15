package conf;

import com.google.inject.Singleton;

import io.mangoo.interfaces.MangooLifecycle;

@Singleton
public class Lifecycle implements MangooLifecycle {

	@Override
    public void applicationStarted() {
	    // Do nothing for now
    }

    @Override
    public void applicationInitialized() {
        // Do nothing for now
    }

    @Override
    public void applicationStopped() {
        // Do nothing for now
    }
}