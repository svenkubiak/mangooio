/**
 * Copyright (C) 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.mangoo.build;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This is a refactored version of
 * DelayedRestartTrigger.java from the Ninja Web Framework
 *
 * Original source code can be found here:
 * https://github.com/ninjaframework/ninja/blob/develop/ninja-maven-plugin/src/main/java/ninja/build/DelayedRestartTrigger.java
 *
 * @author svenkubiak
 *
 */
public class Trigger extends Thread {
    private static final Logger LOG = LogManager.getLogger(Trigger.class);
    private boolean shutdown;
    private final AtomicInteger restartCount;
    private final AtomicInteger triggerCount;
    private final ReentrantLock restartLock;
    private final Condition restartRequested;
    private final Runner runner;
    private long settleDownMillis = 500; //NOSONAR

    public Trigger(Runner runner) {
        this.shutdown = false;
        this.setDaemon(true);
        this.setName("DelayedRestartTrigger");
        this.restartCount = new AtomicInteger(0);
        this.triggerCount = new AtomicInteger(0);
        this.restartLock = new ReentrantLock();
        this.restartRequested = this.restartLock.newCondition();
        this.runner = runner;
    }

    public void doShutdown() {
        this.shutdown = true;
        this.interrupt();
    }

    public int getRestartCount() {
        return restartCount.get();
    }

    public int getAccumulatedTriggerCount() {
        return triggerCount.get();
    }

    public long getSettleDownMillis() {
        return settleDownMillis;
    }

    public void setSettleDownMillis(long settleDownMillis) {
        this.settleDownMillis = settleDownMillis;
    }

    @Override
    @SuppressWarnings("all")
    public void run() {
        while (!shutdown) {
            try {
                this.restartLock.lock();
                try {
                    if (this.triggerCount.get() <= 0) {
                        this.restartRequested.await();
                    }
                    this.restartCount.incrementAndGet();
                } finally {
                    this.restartLock.unlock();
                }

                LOG.info("------------------------------------------------------------------------");
                LOG.info("Restart process...");
                int totalTriggerCount = 0;
                do {
                    LOG.info("Delaying restart for " + settleDownMillis + " ms to wait for file changes to settle");
                    totalTriggerCount += this.triggerCount.getAndSet(0);

                    sleep();
                } while (this.triggerCount.get() != 0);
                LOG.info("Restarting dev mode (" + totalTriggerCount + " file change(s) detected)");
                LOG.info("------------------------------------------------------------------------");
                runner.restart();
            } catch (InterruptedException e) {
                if (!shutdown) {
                    LOG.error("Unexpected thread interrupt (maybe you are shutting down Maven?)", e);
                }
                break;
            }
        }
    }

    @SuppressWarnings("all")
    private void sleep() {
        try {
            Thread.sleep(settleDownMillis);
        } catch (InterruptedException e) {
            //intentionally left blank
        }
    }

    public void trigger() {
        this.restartLock.lock();
        try {
            triggerCount.incrementAndGet();
            this.restartRequested.signalAll();
        } finally {
            this.restartLock.unlock();
        }
    }
}