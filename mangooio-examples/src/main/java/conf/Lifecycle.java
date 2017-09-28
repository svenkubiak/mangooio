package conf;

import com.google.inject.Singleton;

import io.mangoo.interfaces.MangooLifecycle;

/**
 *
 * @author svenkubiak
 *
 */
@Singleton
public class Lifecycle implements MangooLifecycle {
    @Override
    public void applicationInitialized() {
        // do nothing for now
    }

    @Override
    public void applicationStarted() {
        // do nothing for now
    }

    @Override
    public void applicationStopped() {
        // do nothing for now
    }
}