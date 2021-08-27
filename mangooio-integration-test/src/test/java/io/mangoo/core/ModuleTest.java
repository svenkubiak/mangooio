package io.mangoo.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Stage;

import io.mangoo.TestExtension;
import io.mangoo.cache.Cache;
import io.mangoo.interfaces.MangooBootstrap;
import io.mangoo.interfaces.filters.OncePerRequestFilter;
import io.mangoo.templating.TemplateEngine;

/**
 * 
 * @author sven.kubiak
 *
 */
@ExtendWith({TestExtension.class})
public class ModuleTest {
	@Test
	public void testBindings() {
        //given
		Injector guice = Application.getInjector();
		
        //when
		Binding<Stage> stage = guice.getBinding(Stage.class);
		Binding<Injector> injector = guice.getBinding(Injector.class);
		Binding<Logger> logger = guice.getBinding(Logger.class);
		Binding<Config> config = guice.getBinding(Config.class);
		Binding<Cache> cache = guice.getBinding(Cache.class);
		Binding<TemplateEngine> templateEngine = guice.getBinding(TemplateEngine.class);		
		Binding<OncePerRequestFilter> mangooRequestFilter = guice.getBinding(OncePerRequestFilter.class);
		Binding<MangooBootstrap> mangooBootstrap = guice.getBinding(MangooBootstrap.class);
		
		//then
		assertThat(stage.getKey().getTypeLiteral().getType().getTypeName(), equalTo("com.google.inject.Stage"));
		assertThat(injector.getKey().getTypeLiteral().getType().getTypeName(), equalTo("com.google.inject.Injector"));
		assertThat(logger.getKey().getTypeLiteral().getType().getTypeName(), equalTo("java.util.logging.Logger"));
		assertThat(config.getKey().getTypeLiteral().getType().getTypeName(), equalTo("io.mangoo.core.Config"));
		assertThat(cache.getKey().getTypeLiteral().getType().getTypeName(), equalTo("io.mangoo.cache.Cache"));
		assertThat(templateEngine.getKey().getTypeLiteral().getType().getTypeName(), equalTo("io.mangoo.templating.TemplateEngine"));
		assertThat(mangooRequestFilter.getKey().getTypeLiteral().getType().getTypeName(), equalTo("io.mangoo.interfaces.filters.OncePerRequestFilter"));
		assertThat(mangooBootstrap.getKey().getTypeLiteral().getType().getTypeName(), equalTo("io.mangoo.interfaces.MangooBootstrap"));
	}
}