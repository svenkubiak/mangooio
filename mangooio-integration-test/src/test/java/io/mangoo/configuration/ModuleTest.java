package io.mangoo.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.logging.Logger;

import org.junit.Test;
import org.quartz.spi.JobFactory;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Stage;

import io.mangoo.cache.Cache;
import io.mangoo.core.Application;
import io.mangoo.interfaces.MangooLifecycle;
import io.mangoo.interfaces.MangooRequestFilter;
import io.mangoo.interfaces.MangooTemplateEngine;

/**
 * 
 * @author sven.kubiak
 *
 */
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
		Binding<JobFactory> jobFactory = guice.getBinding(JobFactory.class);
		Binding<Cache> cache = guice.getBinding(Cache.class);
		Binding<MangooTemplateEngine> mangooTemplateEngine = guice.getBinding(MangooTemplateEngine.class);		
		Binding<MangooRequestFilter> mangooRequestFilter = guice.getBinding(MangooRequestFilter.class);
		Binding<MangooLifecycle> mangooLifecycle = guice.getBinding(MangooLifecycle.class);
		
		//then
		assertThat(stage.getKey().getTypeLiteral().getType().getTypeName(), equalTo("com.google.inject.Stage"));
		assertThat(injector.getKey().getTypeLiteral().getType().getTypeName(), equalTo("com.google.inject.Injector"));
		assertThat(logger.getKey().getTypeLiteral().getType().getTypeName(), equalTo("java.util.logging.Logger"));
		assertThat(config.getKey().getTypeLiteral().getType().getTypeName(), equalTo("io.mangoo.configuration.Config"));
		assertThat(jobFactory.getKey().getTypeLiteral().getType().getTypeName(), equalTo("org.quartz.spi.JobFactory"));
		assertThat(cache.getKey().getTypeLiteral().getType().getTypeName(), equalTo("io.mangoo.cache.Cache"));
		assertThat(mangooTemplateEngine.getKey().getTypeLiteral().getType().getTypeName(), equalTo("io.mangoo.interfaces.MangooTemplateEngine"));
		assertThat(mangooRequestFilter.getKey().getTypeLiteral().getType().getTypeName(), equalTo("io.mangoo.interfaces.MangooRequestFilter"));
		assertThat(mangooLifecycle.getKey().getTypeLiteral().getType().getTypeName(), equalTo("io.mangoo.interfaces.MangooLifecycle"));
		
	}
}