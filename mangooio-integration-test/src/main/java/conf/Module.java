package conf;

import mangoo.io.interfaces.MangooAuthenticator;
import mangoo.io.interfaces.MangooGlobalFilter;
import mangoo.io.interfaces.MangooLifecycle;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import filters.MyGlobalFilter;

@Singleton
public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(MangooGlobalFilter.class).to(MyGlobalFilter.class);
        bind(MangooLifecycle.class).to(Lifecycle.class);
        bind(MangooAuthenticator.class).toInstance(
                (username, password) -> ("foo").equals(username) && ("bar").equals(password)
        );
    }
}