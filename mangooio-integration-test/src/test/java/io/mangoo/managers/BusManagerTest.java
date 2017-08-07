package io.mangoo.managers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import io.mangoo.core.Application;
import io.mangoo.exceptions.MangooBusException;
import io.mangoo.test.utils.ConcurrentRunner;

/**
 * 
 * @author svenkubiak
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BusManagerTest {

    @Test(expected = MangooBusException.class)
    public void testAEventBus() throws MangooBusException {
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
        
        //when
        busManager.unregister(testListener);
        busManager.unregister(testListener);
        
        //then
        assertThat(busManager.getNumListeners(), equalTo(0L));
    }
    
    @Test
    public void testBConcurrentEventBus() throws InterruptedException {
        Runnable runnable = () -> {
            //given
            TestListener testListener = new TestListener();
            BusManager busManager = Application.getInstance(BusManager.class);
            busManager.register(testListener);
            
            //when
            busManager.publish("This is a test");
            
            //then
            assertThat(testListener.getCount(), greaterThan(0));
        };
        
        ConcurrentRunner.create()
            .withRunnable(runnable)
            .withThreads(50)
            .run();
    }
}