package conf;

import io.mangoo.interfaces.MangooLifecycle;

import com.google.inject.Singleton;

@Singleton
public class Lifecycle implements MangooLifecycle {

	@Override
    public void applicationStarted() {
    }

    @Override
    public void applicationInitialized() {
    }
}