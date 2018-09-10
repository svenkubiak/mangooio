package io.mangoo.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.services.executors.TestCallable;
import io.mangoo.services.executors.TestRunnable;

/**
 * 
 * @author Kubiak
 *
 */
@ExtendWith({TestExtension.class})
public class ConcurrentServicesTest {

    @Test
    public void testCallableWithFuture() throws InterruptedException, ExecutionException {
        //given
        ConcurrentService concurrentService = Application.getInstance(ConcurrentService.class);
        
        //when
        Future<String> future = concurrentService.submit(new TestCallable());
        
        //then
        assertThat(future.get(), equalTo("from callable"));
    }
    
    @Test
    public void testRunnableWithResult() throws InterruptedException, ExecutionException {
        //given
    		ConcurrentService concurrentService = Application.getInstance(ConcurrentService.class);
        
        //when
        String success = "SUCCESS";
        Future<String> future = concurrentService.submit(new TestRunnable(), success);
        
        //then
        assertThat(future.get(), equalTo("SUCCESS"));
    }
    
    @Test
    public void testRunnableExecute() {
        //given
    		ConcurrentService concurrentService = Application.getInstance(ConcurrentService.class);
        
        //then
    		concurrentService.execute(new TestRunnable());
    }
}