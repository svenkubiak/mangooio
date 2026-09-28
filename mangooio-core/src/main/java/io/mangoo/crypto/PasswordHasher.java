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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Computes Argon2id hashes and limits how many of them may run at the same time
 * <p>
 * A single Argon2id computation allocates authentication.hashing.memory kibibytes of
 * heap and holds them for its entire duration. Undertow starts with eight worker
 * threads per core, so without a limit a handful of concurrent logins is enough to
 * request several gigabytes of heap at once. This class caps the number of concurrent
 * computations and rejects with a {@link MangooHashingException} once a caller waited
 * longer than authentication.hashing.timeout for a slot
 * <p>
 * The number of slots is taken from authentication.hashing.concurrency. The default 0
 * derives it from the configured memory cost and the heap available to the JVM,
 * clamped to [{@value #MIN_CONCURRENCY}, {@value #MAX_CONCURRENCY}]
 * <p>
 * <strong>Changing the Argon2 parameters invalidates every stored hash.</strong> See
 * {@link Argon2Settings}
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
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();

    private final Semaphore semaphore;
    private final Argon2Settings settings;
    private final long timeoutMillis;
    private final int concurrency;

    @Inject
    public PasswordHasher(Config config) {
        this(concurrency(config), timeout(config), settings(config));
    }

    /**
     * Creates a PasswordHasher with explicit values, bypassing the configuration
     * and the heuristic. Intended for tests
     *
     * @param concurrency The number of concurrent hash computations allowed
     * @param timeoutMillis The time in milliseconds a caller waits for a free slot
     * @param settings The Argon2 parameters to hash with
     */
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

    /**
     * Hashes a given clear text with a given salt using Argon2id, waiting for a free
     * slot if all slots are currently taken
     *
     * @param cleartext The clear text
     * @param salt The salt
     * @return A Base64 encoded String
     *
     * @throws MangooHashingException If no slot became available within the configured timeout
     */
    public String hash(String cleartext, String salt) {
        Argument.requireNonBlank(cleartext, Required.CLEARTEXT);
        Argument.requireNonBlank(salt, Required.SALT);

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
            return compute(cleartext, salt);
        } finally {
            semaphore.release();
        }
    }

    private String compute(String cleartext, String salt) {
        var argon2 = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withParallelism(settings.parallelism())
                .withMemoryAsKB(settings.memoryKb())
                .withSalt(salt.getBytes(StandardCharsets.UTF_8))
                .withIterations(settings.iterations())
                .build();

        var argon2Generator = new Argon2BytesGenerator();
        argon2Generator.init(argon2);

        var hash = new byte[HASH_LENGTH];
        argon2Generator.generateBytes(cleartext.getBytes(StandardCharsets.UTF_8), hash);

        return BASE64_ENCODER.encodeToString(hash);
    }

    /**
     * Resolves the number of concurrent hash computations that are allowed
     * <p>
     * A configured value greater than zero takes precedence. Otherwise the value is
     * derived from the memory a single hash occupies and half of the heap the JVM may
     * use, clamped to [{@value #MIN_CONCURRENCY}, {@value #MAX_CONCURRENCY}]
     *
     * @param configured The configured value, 0 to derive it
     * @param memoryKb The memory cost of a single hash in kibibytes
     * @return The number of concurrent hash computations allowed
     */
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

    private static Argon2Settings settings(Config config) {
        Objects.requireNonNull(config, Required.CONFIG);
        return new Argon2Settings(
                config.getAuthenticationHashingMemory(),
                config.getAuthenticationHashingIterations(),
                config.getAuthenticationHashingParallelism());
    }

    /**
     * @return The number of concurrent hash computations this instance allows
     */
    int getConcurrency() {
        return concurrency;
    }

    /**
     * @return The number of hashing slots that are currently free
     */
    int availablePermits() {
        return semaphore.availablePermits();
    }
}
