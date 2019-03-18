package io.mangoo.models;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
public class MetricsTest {
    
    @Test
    public void testAddStatusCode() {
        //given
        Metrics metrics = Application.getInstance(Metrics.class);
        
        //when
        metrics.reset();
        metrics.addStatusCode(418);
        metrics.addStatusCode(418);
        metrics.addStatusCode(420);
        
        //then
        assertThat(metrics.getResponseMetrics().get(301), equalTo(null));
        assertThat(metrics.getResponseMetrics().get(418).intValue(), equalTo(2));
        assertThat(metrics.getResponseMetrics().get(420).intValue(), equalTo(1));
    }
    
    @Test
    public void testIncrementContentLength() {
        //given
        Metrics metrics = Application.getInstance(Metrics.class);
        
        //when
        metrics.reset();
        metrics.incrementDataSend(42);
        metrics.incrementDataSend(12);
        metrics.incrementDataSend(23);
        
        //then
        assertThat(metrics.getDataSend(), equalTo(77L));
    }
    
    @Test
    public void testReset() {
        //given
        Metrics metrics = Application.getInstance(Metrics.class);
        
        //when
        metrics.reset();
        metrics.addStatusCode(418);
        metrics.addStatusCode(418);
        metrics.addStatusCode(420);
        
        //then
        assertThat(metrics.getResponseMetrics().get(301), equalTo(null));
        assertThat(metrics.getResponseMetrics().get(418).intValue(), equalTo(2));
        assertThat(metrics.getResponseMetrics().get(420).intValue(), equalTo(1));
        
        //when
        metrics.reset();

        //then
        assertThat(metrics.getResponseMetrics().get(418), equalTo(null));
    }
    
    @Test
    public void testUpdate() {
        //given
        Metrics metrics = Application.getInstance(Metrics.class);
        
        //when
        metrics.reset();
        metrics.update(2000);
        metrics.update(1000);
        
        //then
        assertThat(metrics.getMaxRequestTime(), equalTo(2000));
        assertThat(metrics.getMinRequestTime(), lessThan(2000));
        assertThat(metrics.getAvgRequestTime(), lessThan(2000L));
    }
    
    @Test
    public void testMinCount() {
        //given
        Metrics metrics = Application.getInstance(Metrics.class);
        
        //when
        metrics.reset();
        metrics.update(0);
        metrics.update(2000);
        metrics.update(1000);
        metrics.update(3000);
        
        //then
        assertThat(metrics.getMinRequestTime(), equalTo(1000));
    }
    
    @Test
    public void testMaxCount() {
        //given
        Metrics metrics = Application.getInstance(Metrics.class);
        
        //when
        metrics.reset();
        metrics.update(0);
        metrics.update(2000);
        metrics.update(1000);
        metrics.update(3000);
        
        //then
        assertThat(metrics.getMaxRequestTime(), equalTo(3000));
    }
    
    @Test
    public void testAvgCount() {
        //given
        Metrics metrics = Application.getInstance(Metrics.class);
        
        //when
        metrics.reset();
        metrics.update(0);
        metrics.update(2000);
        metrics.update(1000);
        metrics.update(3000);
        
        //then
        assertThat(metrics.getAvgRequestTime(), equalTo(1500L));
    }
}