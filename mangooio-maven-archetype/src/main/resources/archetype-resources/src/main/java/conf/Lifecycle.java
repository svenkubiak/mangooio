package conf;

import com.google.inject.Singleton;

import io.mangoo.interfaces.MangooLifecycle;

@Singleton
public class Lifecycle implements MangooLifecycle {

	@Override
    public void applicationStarted() {
    }

    @Override
    public void applicationInitialized() {
    }

    @Override
    public void applicationStopped() {
    }

    @Override
    public void requestCompleted(String url, int status, int processTime, long bytesSend) {
    }
}
