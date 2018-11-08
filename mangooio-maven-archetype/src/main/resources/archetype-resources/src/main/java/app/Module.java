package app;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import io.mangoo.interfaces.MangooBootstrap;

@Singleton
public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(MangooBootstrap.class).to(Bootstrap.class);
    }
}