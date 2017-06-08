package io.mangoo.managers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import org.junit.Test;

import io.mangoo.core.Application;
import io.mangoo.test.utils.ConcurrentRunner;

/**
 * 
 * @author svenkubiak
 *
 */
public class BusManagerTest {

    @Test
    public void testEventBus() {
        //given
        TestListener testListener = new TestListener();
        BusManager busManager = Application.getInstance(BusManager.class);
        busManager.register(testListener);
        
        //when
        busManager.publish("This is a test");
        busManager.publish("This is a test");
        busManager.publish("This is a test");
        
        //then
        assertThat(testListener.getCount(), equalTo(3));
        assertThat(busManager.getNumListeners(), equalTo(1L));
        assertThat(busManager.getNumEvents(), equalTo(3L));
    }
    
    @Test
    public void testConcurrentEventBus() throws InterruptedException {
        Runnable runnable = () -> {
            for (int j=0; j < 50; j++) {
                //given
                TestListener testListener = new TestListener();
                BusManager busManager = Application.getInstance(BusManager.class);
                busManager.register(testListener);
                
                //when
                busManager.publish("This is a test");
                
                //then
                assertThat(testListener.getCount(), greaterThan(3));
            }
        };
        
        ConcurrentRunner.create()
            .withRunnable(runnable)
            .withThreads(50)
            .run();
    }
}