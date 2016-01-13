package io.mangoo.models;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

import io.mangoo.core.Application;

/**
 * 
 * @author svenkubiak
 *
 */
public class MetricsTest {
    
    @Test
    public void testIncrement() {
        //given
        Metrics metrics = Application.getInstance(Metrics.class);
        
        //when
        metrics.inc(418);
        metrics.inc(418);
        metrics.inc(420);
        
        //then
        assertThat(metrics.getMetrics().get(500), equalTo(null));
        assertThat(metrics.getMetrics().get(418).intValue(), equalTo(2));
        assertThat(metrics.getMetrics().get(420).intValue(), equalTo(1));
    }
}