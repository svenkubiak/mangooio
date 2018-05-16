package io.mangoo.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.MatcherAssert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.llorllale.cactoos.matchers.RunsInThreads;

import io.mangoo.TestSuite;
import io.mangoo.core.Application;
import io.mangoo.exceptions.MangooEventBusException;

/**
 * 
 * @author svenkubiak
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EventBusServiceTest {

    @Test(expected = MangooEventBusException.class)
    public void testAEventBus() throws MangooEventBusException {
        //given
        TestListener testListener = new TestListener();
        EventBusService busManager = Application.getInstance(EventBusService.class);
        busManager.register(testListener);
        
        //when
        busManager.publish("This is a test");
        busManager.publish("This is a test");
        busManager.publish("This is a test");
        
        //then
        assertThat(testListener.getCount(), equalTo(3));
        assertThat(busManager.getNumListeners(), equalTo(2L));
        
        //when
        busManager.unregister(testListener);
        busManager.unregister(testListener);
        
        //then
        assertThat(busManager.getNumListeners(), equalTo(0L));
    }
    
    @Test
    public void testBConcurrentEventBus() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            TestListener testListener = new TestListener();
            EventBusService busManager = Application.getInstance(EventBusService.class);
            busManager.register(testListener);
            
            //when
            busManager.publish("This is a test");
            
            // then
            return testListener.getCount() > 0;
        }, new RunsInThreads<>(new AtomicInteger(), TestSuite.THREADS));
    }
}