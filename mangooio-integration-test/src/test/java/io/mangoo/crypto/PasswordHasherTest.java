package io.mangoo.crypto;

import io.mangoo.core.Config;
import io.mangoo.exceptions.MangooHashingException;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Execution(ExecutionMode.SAME_THREAD)
class PasswordHasherTest {
    private static final String SALT = "6IeDg6szfIfJKOsuhfZHtqWU4W7O6BpvNFhfI8Kjb64p9Pi";
    private static final String CLEARTEXT = "this is a secret password";
    private static final Argon2Settings SLOW = new Argon2Settings(65536, 12, 1);
    private static final Argon2Settings FAST = new Argon2Settings(1024, 1, 1);
    private static final Argon2Settings OTHER = new Argon2Settings(2048, 2, 1);

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
    void testHashIsPhcFormattedAndCarriesTheConfiguredParameters() {
        //given
        var passwordHasher = new PasswordHasher(2, 5000, FAST);

        //when
        String hash = passwordHasher.hash(CLEARTEXT, SALT);

        //then
        assertThat(hash, startsWith("$argon2id$v=19$m=1024,t=1,p=1$"));

        var parsed = Argon2Hash.parse(hash);
        assertThat(parsed.settings(), equalTo(FAST));
        assertThat(parsed.salt(), equalTo(SALT.getBytes(StandardCharsets.UTF_8)));
        assertThat(parsed.hash().length, equalTo(32));
        assertThat(parsed.encode(), equalTo(hash));
    }

    @Test
    void testMatchesRoundTrip() {
        //given
        var passwordHasher = new PasswordHasher(2, 5000, FAST);
        String hash = passwordHasher.hash(CLEARTEXT, SALT);

        //then
        assertThat(passwordHasher.matches(CLEARTEXT, SALT, hash), equalTo(true));
        assertThat(passwordHasher.matches("wrong password", SALT, hash), equalTo(false));
    }

    @Test
    void testFreshHashDoesNotNeedRehash() {
        //given
        var passwordHasher = new PasswordHasher(2, 5000, FAST);

        //when
        String hash = passwordHasher.hash(CLEARTEXT, SALT);

        //then
        assertThat(passwordHasher.needsRehash(hash), equalTo(false));
    }

    @Test
    void testLegacyHashStillVerifiesAndNeedsRehash() {
        //given a bare Base64 hash as earlier mangoo I/O versions stored it
        var passwordHasher = new PasswordHasher(2, 30000, FAST);
        String legacy = legacyHash(CLEARTEXT, SALT);

        //then
        assertThat(legacy, not(startsWith("$")));
        assertThat(passwordHasher.matches(CLEARTEXT, SALT, legacy), equalTo(true));
        assertThat(passwordHasher.matches("wrong password", SALT, legacy), equalTo(false));
        assertThat(passwordHasher.needsRehash(legacy), equalTo(true));
    }

    @Test
    void testEmbeddedParametersBeatTheConfiguredOnes() {
        //given a hash created with parameters this instance is not configured with
        String hash = new PasswordHasher(2, 5000, OTHER).hash(CLEARTEXT, SALT);
        var passwordHasher = new PasswordHasher(2, 5000, FAST);

        //then
        assertThat(passwordHasher.matches(CLEARTEXT, SALT, hash), equalTo(true));
        assertThat(passwordHasher.matches("wrong password", SALT, hash), equalTo(false));
        assertThat(passwordHasher.needsRehash(hash), equalTo(true));
    }

    @Test
    void testTheGivenSaltMustMatchTheEmbeddedOne() {
        //given the salt travels with the hash, but is not taken from there on verification
        var passwordHasher = new PasswordHasher(2, 5000, FAST);
        String hash = passwordHasher.hash(CLEARTEXT, SALT);

        //then
        assertThat(passwordHasher.matches(CLEARTEXT, SALT, hash), equalTo(true));
        assertThat(passwordHasher.matches(CLEARTEXT, "a completely different salt", hash), equalTo(false));
    }

    @Test
    void testATamperedEmbeddedSaltIsRejected() {
        //given an attacker replaced the embedded salt with one of their own
        var passwordHasher = new PasswordHasher(2, 5000, FAST);
        String hash = passwordHasher.hash(CLEARTEXT, SALT);
        String tampered = new Argon2Hash(
                FAST,
                "attacker".getBytes(StandardCharsets.UTF_8),
                Argon2Hash.parse(hash).hash()).encode();

        //then
        assertThat(passwordHasher.matches(CLEARTEXT, SALT, tampered), equalTo(false));
    }

    @Test
    void testMalformedHashIsRejectedInsteadOfThrowing() {
        //given
        var passwordHasher = new PasswordHasher(2, 5000, FAST);

        //then
        assertThat(passwordHasher.matches(CLEARTEXT, SALT, "$argon2id$v=19$m=1024,t=1$c2FsdA$aGFzaA"), equalTo(false));
        assertThat(passwordHasher.matches(CLEARTEXT, SALT, "not base64 at all !!!"), equalTo(false));
        assertThat(passwordHasher.needsRehash("$argon2id$broken"), equalTo(true));

        //parameters that no Argon2 implementation would accept are rejected instead of
        //reaching the generator
        String hash = passwordHasher.hash(CLEARTEXT, SALT);
        assertThat(passwordHasher.matches(CLEARTEXT, SALT, hash.replace("m=1024,t=1,p=1", "m=0,t=0,p=0")), equalTo(false));
        assertThat(passwordHasher.matches(CLEARTEXT, SALT, hash.replace("v=19", "v=16")), equalTo(false));
    }

    @Test
    void testConfiguredMemoryBelowMinimumFailsStartup() {
        //given
        var config = config(Argon2Settings.MIN_MEMORY_KB - 1, 3, 1);

        //then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new PasswordHasher(config));
        assertThat(exception.getMessage(), equalTo("authentication.hashing.memory must be at least 8192 KB, but is 8191"));
    }

    @Test
    void testConfiguredIterationsBelowMinimumFailsStartup() {
        //given
        var config = config(32768, Argon2Settings.MIN_ITERATIONS - 1, 1);

        //then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new PasswordHasher(config));
        assertThat(exception.getMessage(), equalTo("authentication.hashing.iterations must be at least 2, but is 1"));
    }

    @Test
    void testConfiguredParallelismBelowMinimumFailsStartup() {
        //given
        var config = config(32768, 3, Argon2Settings.MIN_PARALLELISM - 1);

        //then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new PasswordHasher(config));
        assertThat(exception.getMessage(), equalTo("authentication.hashing.parallelism must be at least 1, but is 0"));
    }

    @Test
    void testConfiguredMinimumStarts() {
        //given
        var config = config(Argon2Settings.MIN_MEMORY_KB, Argon2Settings.MIN_ITERATIONS, Argon2Settings.MIN_PARALLELISM);

        //then
        assertThat(new PasswordHasher(config).getConcurrency(), greaterThanOrEqualTo(2));
    }

    private static Config config(int memoryKb, int iterations, int parallelism) {
        var config = Mockito.mock(Config.class);
        Mockito.when(config.getAuthenticationHashingMemory()).thenReturn(memoryKb);
        Mockito.when(config.getAuthenticationHashingIterations()).thenReturn(iterations);
        Mockito.when(config.getAuthenticationHashingParallelism()).thenReturn(parallelism);
        Mockito.when(config.getAuthenticationHashingConcurrency()).thenReturn(4);
        Mockito.when(config.getAuthenticationHashingTimeout()).thenReturn(5000L);

        return config;
    }

    private static String legacyHash(String cleartext, String salt) {
        var parameters = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withParallelism(Argon2Settings.LEGACY.parallelism())
                .withMemoryAsKB(Argon2Settings.LEGACY.memoryKb())
                .withSalt(salt.getBytes(StandardCharsets.UTF_8))
                .withIterations(Argon2Settings.LEGACY.iterations())
                .build();

        var generator = new Argon2BytesGenerator();
        generator.init(parameters);

        var hash = new byte[32];
        generator.generateBytes(cleartext.getBytes(StandardCharsets.UTF_8), hash);

        return Base64.getEncoder().encodeToString(hash);
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
