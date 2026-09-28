package io.mangoo.crypto;

import io.mangoo.exceptions.MangooHashingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Execution(ExecutionMode.SAME_THREAD)
class PasswordHasherTest {
    private static final String SALT = "6IeDg6szfIfJKOsuhfZHtqWU4W7O6BpvNFhfI8Kjb64p9Pi";
    private static final String CLEARTEXT = "this is a secret password";
    private static final Argon2Settings SLOW = new Argon2Settings(65536, 12, 1);
    private static final Argon2Settings FAST = new Argon2Settings(1024, 1, 1);

    @Test
    void testDerivedConcurrencyIsClampedToUpperBound() {
        //given a hash that occupies almost nothing, the heuristic would allow a lot
        //when
        int concurrency = PasswordHasher.resolveConcurrency(0, 1);

        //then
        assertThat(concurrency, equalTo(8));
    }

    @Test
    void testDerivedConcurrencyIsClampedToLowerBound() {
        //given a single hash that does not even fit into the heap
        int memoryKb = (int) Math.min(Integer.MAX_VALUE, Runtime.getRuntime().maxMemory() / 1024);

        //when
        int concurrency = PasswordHasher.resolveConcurrency(0, memoryKb);

        //then
        assertThat(concurrency, equalTo(2));
    }

    @Test
    void testDerivedConcurrencyStaysWithinBounds() {
        //when
        int concurrency = PasswordHasher.resolveConcurrency(0, 80000);

        //then
        assertThat(concurrency, greaterThanOrEqualTo(2));
        assertThat(concurrency, lessThanOrEqualTo(8));
    }

    @Test
    void testExplicitConcurrencyBeatsHeuristic() {
        //when
        int concurrency = PasswordHasher.resolveConcurrency(64, 80000);

        //then
        assertThat(concurrency, equalTo(64));
    }

    @Test
    void testExplicitConcurrencyIsUsedByInstance() {
        //given
        var passwordHasher = new PasswordHasher(3, 1000, FAST);

        //then
        assertThat(passwordHasher.getConcurrency(), equalTo(3));
        assertThat(passwordHasher.availablePermits(), equalTo(3));
    }

    @Test
    void testHashIsDeterministic() {
        //given
        var passwordHasher = new PasswordHasher(2, 5000, FAST);

        //when
        String first = passwordHasher.hash(CLEARTEXT, SALT);
        String second = passwordHasher.hash(CLEARTEXT, SALT);

        //then
        assertThat(first, equalTo(second));
        assertThat(first, not(equalTo(passwordHasher.hash(CLEARTEXT, SALT + "x"))));
    }

    @Test
    void testHashRejectsWhenAllSlotsAreTaken() throws InterruptedException {
        //given
        var passwordHasher = new PasswordHasher(2, 50, SLOW);
        var done = new CountDownLatch(2);
        List<Thread> threads = new ArrayList<>();

        //when
        for (var i = 0; i < 2; i++) {
            var thread = Thread.ofPlatform().start(() -> {
                try {
                    passwordHasher.hash(CLEARTEXT, SALT);
                } finally {
                    done.countDown();
                }
            });
            threads.add(thread);
        }

        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
        while (passwordHasher.availablePermits() > 0 && System.nanoTime() < deadline) {
            Thread.onSpinWait();
        }
        assertThat(passwordHasher.availablePermits(), equalTo(0));

        //then
        assertThrows(MangooHashingException.class, () -> passwordHasher.hash(CLEARTEXT, SALT));

        //when
        assertThat(done.await(60, TimeUnit.SECONDS), equalTo(true));
        for (Thread thread : threads) {
            thread.join();
        }

        //then no permit is leaked by the rejected call
        assertThat(passwordHasher.availablePermits(), equalTo(2));
    }
}
