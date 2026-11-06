package io.mangoo.core;

import com.google.inject.Binding;
import com.google.inject.Injector;
import io.mangoo.TestExtension;
import io.mangoo.cache.Cache;
import io.mangoo.interfaces.MangooBootstrap;
import io.mangoo.interfaces.filters.OncePerRequestFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith({TestExtension.class})
class ModuleTest {
	@Test
	void testBindings() {
        //given
		Injector guice = Application.getInjector();
		
        //when
		Binding<Cache> cache = guice.getBinding(Cache.class);
		Binding<OncePerRequestFilter> mangooRequestFilter = guice.getBinding(OncePerRequestFilter.class);
		Binding<MangooBootstrap> mangooBootstrap = guice.getBinding(MangooBootstrap.class);
		
		//then
		assertThat(cache.getKey().getTypeLiteral().getType().getTypeName(), equalTo("io.mangoo.cache.Cache"));
		assertThat(mangooRequestFilter.getKey().getTypeLiteral().getType().getTypeName(), equalTo("io.mangoo.interfaces.filters.OncePerRequestFilter"));
		assertThat(mangooBootstrap.getKey().getTypeLiteral().getType().getTypeName(), equalTo("io.mangoo.interfaces.MangooBootstrap"));
	}
}