package conf;

import com.google.inject.AbstractModule;

import filters.MyGlobalFilter;
import io.mangoo.interfaces.MangooLifecycle;
import io.mangoo.interfaces.MangooRequestFilter;

public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(MangooRequestFilter.class).to(MyGlobalFilter.class);
        bind(MangooLifecycle.class).to(Lifecycle.class);
    }
}