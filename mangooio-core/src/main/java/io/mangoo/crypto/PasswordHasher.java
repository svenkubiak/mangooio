package io.mangoo.crypto;

import io.mangoo.constants.Required;
import io.mangoo.core.Config;
import io.mangoo.exceptions.MangooHashingException;
import io.mangoo.utils.Argument;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.util.Arrays;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Limits concurrent Argon2id computations, as each holds authentication.hashing.memory KiB of heap for its whole duration.
 * Hashes are PHC encoded and verified with their own parameters, so configuration changes do not lock out users.
 */
@Singleton
public class PasswordHasher {
    private static final Logger LOG = LogManager.getLogger(PasswordHasher.class);
    private static final int MIN_CONCURRENCY = 2;
    private static final int MAX_CONCURRENCY = 8;
    private static final int HASH_LENGTH = 32;
    private static final int KB = 1024;
    private static final double HEADROOM_FACTOR = 1.2;
    private static final double BUDGET_FACTOR = 0.5;

    private final Semaphore semaphore;
    private final Argon2Settings settings;
    private final long timeoutMillis;
    private final int concurrency;

    @Inject
    public PasswordHasher(Config config) {
        this(concurrency(config), timeout(config), settings(config));
    }

    // Bypasses the configuration and the concurrency heuristic; intended for tests.
    PasswordHasher(int concurrency, long timeoutMillis, Argon2Settings settings) {
        Objects.requireNonNull(settings, Required.SETTINGS);
        Argument.check(concurrency > 0, "concurrency must be greater than zero");

        this.concurrency = concurrency;
        this.timeoutMillis = timeoutMillis;
        this.settings = settings;
        this.semaphore = new Semaphore(concurrency, true);

        LOG.info("Argon2 hashing is limited to {} concurrent computations with a timeout of {} ms (memory {} KB, iterations {}, parallelism {})",
                concurrency, timeoutMillis, settings.memoryKb(), settings.iterations(), settings.parallelism());
    }

    /** Throws {@link MangooHashingException} if no hashing slot becomes available within the configured timeout. */
    public String hash(String cleartext, String salt) {
        Argument.requireNonBlank(cleartext, Required.CLEARTEXT);
        Argument.requireNonBlank(salt, Required.SALT);

        byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);
        byte[] hash = throttled(() -> compute(cleartext, saltBytes, settings, HASH_LENGTH));

        return new Argon2Hash(settings, saltBytes, hash).encode();
    }

    /**
     * Verifies with the parameters of the stored hash (legacy non-PHC hashes with {@link Argon2Settings#LEGACY}); a salt mismatch fails.
     * Throws {@link MangooHashingException} if no hashing slot becomes available within the configured timeout.
     */
    public boolean matches(String cleartext, String salt, String stored) {
        Argument.requireNonBlank(cleartext, Required.CLEARTEXT);
        Argument.requireNonBlank(salt, Required.SALT);
        Argument.requireNonBlank(stored, Required.HASH);

        byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);

        Argon2Hash expected;
        try {
            expected = Argon2Hash.isPhcFormat(stored)
                    ? Argon2Hash.parse(stored)
                    : new Argon2Hash(Argon2Settings.LEGACY, saltBytes, Base64.getDecoder().decode(stored));
        } catch (IllegalArgumentException e) { //NOSONAR the message names the defect, the stack trace is always the same
            LOG.warn("Rejected a login, the stored hash is malformed: {}", e.getMessage());
            return false;
        }

        if (!Arrays.constantTimeAreEqual(expected.salt(), saltBytes)) {
            return false;
        }

        byte[] expectedHash = expected.hash();
        byte[] actual = throttled(() -> compute(cleartext, saltBytes, expected.settings(), expectedHash.length));

        return Arrays.constantTimeAreEqual(expectedHash, actual);
    }

    /** Only call after {@link #matches(String, String, String)} returned true, as a failed login must not trigger a rehash. */
    public boolean needsRehash(String stored) {
        Argument.requireNonBlank(stored, Required.HASH);

        if (!Argon2Hash.isPhcFormat(stored)) {
            return true;
        }

        try {
            return !settings.equals(Argon2Hash.parse(stored).settings());
        } catch (IllegalArgumentException e) { //NOSONAR a malformed hash is replaced, not reported
            return true;
        }
    }

    private <T> T throttled(Supplier<T> computation) {
        try {
            if (!semaphore.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS)) {
                LOG.warn("Rejected an Argon2 hashing request, all {} slots were taken for more than {} ms. Consider raising authentication.hashing.concurrency", concurrency, timeoutMillis);
                throw new MangooHashingException("No Argon2 hashing slot became available within " + timeoutMillis + " ms");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MangooHashingException("Interrupted while waiting for an Argon2 hashing slot", e);
        }

        try {
            return computation.get();
        } finally {
            semaphore.release();
        }
    }

    private static byte[] compute(String cleartext, byte[] salt, Argon2Settings settings, int length) {
        var argon2 = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withParallelism(settings.parallelism())
                .withMemoryAsKB(settings.memoryKb())
                .withSalt(salt)
                .withIterations(settings.iterations())
                .build();

        var argon2Generator = new Argon2BytesGenerator();
        argon2Generator.init(argon2);

        var hash = new byte[length];
        argon2Generator.generateBytes(cleartext.getBytes(StandardCharsets.UTF_8), hash);

        return hash;
    }

    // A configured value greater than 0 wins; otherwise it is derived from the per-hash memory and half the max heap, clamped to [2, 8].
    static int resolveConcurrency(int configured, int memoryKb) {
        if (configured > 0) {
            return configured;
        }

        long bytesPerHash = (long) (((long) memoryKb) * KB * HEADROOM_FACTOR);
        long budget = (long) (Runtime.getRuntime().maxMemory() * BUDGET_FACTOR);

        return Math.clamp(budget / bytesPerHash, MIN_CONCURRENCY, MAX_CONCURRENCY);
    }

    private static int concurrency(Config config) {
        Objects.requireNonNull(config, Required.CONFIG);
        return resolveConcurrency(config.getAuthenticationHashingConcurrency(), config.getAuthenticationHashingMemory());
    }

    private static long timeout(Config config) {
        Objects.requireNonNull(config, Required.CONFIG);
        return config.getAuthenticationHashingTimeout();
    }

    // Fails the startup rather than hashing weaker than the accepted minimum.
    private static Argon2Settings settings(Config config) {
        Objects.requireNonNull(config, Required.CONFIG);
        return new Argon2Settings(
                config.getAuthenticationHashingMemory(),
                config.getAuthenticationHashingIterations(),
                config.getAuthenticationHashingParallelism()).requireNotWeakerThanMinimum();
    }

    int getConcurrency() {
        return concurrency;
    }

    int availablePermits() {
        return semaphore.availablePermits();
    }
}
