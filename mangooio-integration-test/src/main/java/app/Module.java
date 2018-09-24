package app;

import com.google.inject.AbstractModule;

import filters.MyGlobalFilter;
import io.mangoo.interfaces.MangooBootstrap;
import io.mangoo.interfaces.MangooRequestFilter;

public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(MangooBootstrap.class).to(Bootstrap.class);
        bind(MangooRequestFilter.class).to(MyGlobalFilter.class);
    }
}