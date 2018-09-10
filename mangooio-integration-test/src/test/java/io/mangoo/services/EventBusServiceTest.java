package io.mangoo.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.llorllale.cactoos.matchers.RunsInThreads;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.exceptions.MangooEventBusException;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
public class EventBusServiceTest {

    @Test
    public void testEventBus() throws MangooEventBusException  {
        //given
        EventBusService busManager = Application.getInstance(EventBusService.class);
        TestListener testListener = new TestListener();
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
        
        //then
        assertThat(busManager.getNumListeners(), equalTo(1L));
        
        MatcherAssert.assertThat(t -> {
            //given
            TestListener newtestListener = new TestListener();
            busManager.register(newtestListener);
            
            //when
            busManager.publish("This is a test");
            
            // then
            return testListener.getCount() > 0;
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }
}