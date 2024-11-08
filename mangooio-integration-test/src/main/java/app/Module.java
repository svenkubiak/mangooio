package app;

import com.google.inject.AbstractModule;
import filters.MyGlobalFilter;
import io.mangoo.interfaces.MangooBootstrap;
import io.mangoo.interfaces.filters.OncePerRequestFilter;

public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(MangooBootstrap.class).to(Bootstrap.class);
        bind(OncePerRequestFilter.class).to(MyGlobalFilter.class);
    }
}