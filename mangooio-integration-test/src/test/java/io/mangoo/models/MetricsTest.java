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
    public void testResponseIncrement() {
        //given
        Metrics metrics = Application.getInstance(Metrics.class);
        metrics.reset();
        
        //when
        metrics.increment(418);
        metrics.increment(418);
        metrics.increment(420);
        
        //then
        assertThat(metrics.getResponseMetrics().get(301), equalTo(null));
        assertThat(metrics.getResponseMetrics().get(418).intValue(), equalTo(2));
        assertThat(metrics.getResponseMetrics().get(420).intValue(), equalTo(1));
    }
    
    @Test
    public void testUriIncrement() {
        //given
        Metrics metrics = Application.getInstance(Metrics.class);
        metrics.reset();
        
        //when
        metrics.increment("/bar");
        metrics.increment("/foo");
        metrics.increment("/foo");
        
        //then
        assertThat(metrics.getUriMetrics().get("/"), equalTo(null));
        assertThat(metrics.getUriMetrics().get("/bar").intValue(), equalTo(1));
        assertThat(metrics.getUriMetrics().get("/foo").intValue(), equalTo(2));
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
        assertThat(metrics.getAvgRequestTime(), lessThan(2000L));
    }
}