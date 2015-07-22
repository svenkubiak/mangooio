package conf;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import filters.MyGlobalFilter;
import io.mangoo.interfaces.MangooAuthenticator;
import io.mangoo.interfaces.MangooGlobalFilter;
import io.mangoo.interfaces.MangooLifecycle;

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