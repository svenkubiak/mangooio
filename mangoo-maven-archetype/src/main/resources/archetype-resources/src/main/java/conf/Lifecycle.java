package conf;

import mangoo.io.interfaces.MangooLifecycle;

import com.google.inject.Singleton;

@Singleton
public class Lifecycle implements MangooLifecycle {
    
    @Override
    public void applicationStarted() {
    }
}