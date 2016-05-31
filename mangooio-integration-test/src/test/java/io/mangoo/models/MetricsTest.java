package io.mangoo.models;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;

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
    
    @Test
    public void testUpdate() {
        //given
        Metrics metrics = Application.getInstance(Metrics.class);
        
        //when
        metrics.update(2000);
        metrics.update(1000);
        
        //then
        assertThat(metrics.getMaxRequestTime(), equalTo(2000));
        assertThat(metrics.getMinRequestTime(), lessThan(2000));
        assertThat(metrics.getAvgRequestTime(), lessThan(2000));
    }
}