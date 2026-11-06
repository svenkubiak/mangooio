package io.mangoo.interfaces;

import java.time.Duration;

/**
 * Failed attempt budget per key, e.g. per login identifier. The MongoDB implementation is bound when persistence is
 * enabled and shares the budget between all instances; bind your own implementation in app.Module to replace it.
 */
public interface AttemptStore {

    /**
     * Reserves an attempt before it is verified and returns false if the key is locked. The attempt that uses up the
     * budget is still granted and sets the lock, which further attempts never extend.
     */
    boolean tryAcquire(String key, int maxAttempts, Duration lockDuration);

    /** Gives back an attempt that could not be verified, e.g. on a hashing timeout. */
    void release(String key, int maxAttempts);

    void reset(String key);

    boolean isLocked(String key);
}
