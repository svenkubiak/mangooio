package io.mangoo.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

import conf.Lifecycle;
import io.mangoo.core.Application;

/**
 * 
 * @author svenkubiak
 *
 */
public class LifecycleTest {
	@Test
	public void testLifecycleInvocation() {
        //given
        final Lifecycle lifecycle = Application.getInstance(Lifecycle.class);

        //then
        assertThat(lifecycle.getInitialize(), equalTo(1));
        assertThat(lifecycle.getStarted(), equalTo(1));
	}
}