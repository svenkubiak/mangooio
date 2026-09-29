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
 * Hashes are returned in the PHC string format, see {@link Argon2Hash}, so that they
 * carry the parameters they were computed with. Verification uses the parameters of the
 * stored hash, never the current configuration. Changing the configured parameters is
 * therefore safe, existing hashes keep verifying and {@link #needsRehash(String)} points
 * out which of them should be recomputed on the next successful login
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
     * @return An Argon2id hash in the PHC string format, see {@link Argon2Hash}
     *
     * @throws MangooHashingException If no slot became available within the configured timeout
     */
    public String hash(String cleartext, String salt) {
        Argument.requireNonBlank(cleartext, Required.CLEARTEXT);
        Argument.requireNonBlank(salt, Required.SALT);

        byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);
        byte[] hash = throttled(() -> compute(cleartext, saltBytes, settings, HASH_LENGTH));

        return new Argon2Hash(settings, saltBytes, hash).encode();
    }

    /**
     * Verifies a given clear text against an already hashed value
     * <p>
     * A hash in PHC format is verified with the parameters it carries, the current
     * configuration is deliberately ignored. The given salt must be the one the hash was
     * created with, a mismatch is a failed verification and not silently verified against
     * the embedded salt. A hash that is not in PHC format was created before mangoo I/O
     * embedded the parameters and is verified with {@link Argon2Settings#LEGACY} and the
     * given salt, so that an upgrade does not lock out existing users. Use
     * {@link #needsRehash(String)} after a successful verification to find out whether the
     * hash should be recomputed
     *
     * @param cleartext The clear text
     * @param salt The salt the hash was created with
     * @param stored The stored hash
     * @return True if the clear text matches the stored hash, false otherwise
     *
     * @throws MangooHashingException If no slot became available within the configured timeout
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

        byte[] actual = throttled(() -> compute(cleartext, saltBytes, expected.settings(), expected.hash().length));

        return Arrays.constantTimeAreEqual(expected.hash(), actual);
    }

    /**
     * Checks whether a stored hash was created with something else than the current
     * configuration and should be replaced
     * <p>
     * True for a hash that is not in PHC format and for a PHC hash whose parameters
     * differ from the configured ones. <strong>Only evaluate this after
     * {@link #matches(String, String, String)} returned true</strong>, the clear text is
     * needed to compute the replacement and a failed login must not trigger a rehash
     *
     * @param stored The stored hash
     * @return True if the hash should be recomputed with the current parameters, false otherwise
     */
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

    /**
     * Reads the configured Argon2id parameters and refuses anything below the lower
     * bounds mangoo I/O accepts. The application fails to start rather than hashing
     * weaker than intended
     */
    private static Argon2Settings settings(Config config) {
        Objects.requireNonNull(config, Required.CONFIG);
        return new Argon2Settings(
                config.getAuthenticationHashingMemory(),
                config.getAuthenticationHashingIterations(),
                config.getAuthenticationHashingParallelism()).requireNotWeakerThanMinimum();
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
