package conf;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import controllers.MyLocalHandler;
import filters.MyGlobalFilter;
import io.mangoo.interfaces.MangooAuthenticator;
import io.mangoo.interfaces.MangooLifecycle;
import io.mangoo.interfaces.MangooRequestFilter;
import io.mangoo.routing.handlers.LocaleHandler;

@Singleton
public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(LocaleHandler.class).to(MyLocalHandler.class); 
        bind(MangooRequestFilter.class).to(MyGlobalFilter.class);
        bind(MangooLifecycle.class).to(Lifecycle.class);
        bind(MangooAuthenticator.class).toInstance(
                (username, password) -> ("foo").equals(username) && ("bar").equals(password)
                );
    }
}