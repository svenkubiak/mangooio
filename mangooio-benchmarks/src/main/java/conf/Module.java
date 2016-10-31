package conf;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import io.mangoo.interfaces.MangooLifecycle;

/**
 *
 * @author svenkubiak
 *
 */
@Singleton
public class Module extends AbstractModule {

    @Override
    protected void configure() {
        bind(MangooLifecycle.class).to(Lifecycle.class);
    }
}