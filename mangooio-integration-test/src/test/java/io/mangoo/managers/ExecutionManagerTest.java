package io.mangoo.managers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Test;

import io.mangoo.managers.executors.TestCallable;
import io.mangoo.managers.executors.TestRunnable;
import io.mangoo.test.MangooInstance;

/**
 * 
 * @author Kubiak
 *
 */
public class ExecutionManagerTest {

    @Test
    public void testCallableWithFuture() throws InterruptedException, ExecutionException {
        //given
        ExecutionManager executionManager = MangooInstance.TEST.getInstance(ExecutionManager.class);
        
        //when
        Future<String> future = executionManager.submit(new TestCallable());
        
        //then
        assertThat(future.get(), equalTo("from callable"));
    }
    
    @Test
    public void testRunnableWithResult() throws InterruptedException, ExecutionException {
        //given
        ExecutionManager executionManager = MangooInstance.TEST.getInstance(ExecutionManager.class);
        
        //when
        String success = "SUCCESS";
        Future<String> future = executionManager.submit(new TestRunnable(), success);
        
        //then
        assertThat(future.get(), equalTo("SUCCESS"));
    }
    
    @Test
    public void testRunnableExecute() {
        //given
        ExecutionManager executionManager = MangooInstance.TEST.getInstance(ExecutionManager.class);
        
        //then
        executionManager.execute(new TestRunnable());
    }
}