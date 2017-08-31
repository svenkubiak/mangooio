package io.mangoo.services.executors;

import java.util.concurrent.Callable;

/**
 * 
 * @author svenkubiak
 *
 */
public class TestCallable implements Callable<String> {
    @Override
    public String call() throws Exception {
        return "from callable";
    }
}