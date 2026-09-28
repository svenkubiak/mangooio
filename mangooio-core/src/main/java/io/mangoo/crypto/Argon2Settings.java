package io.mangoo.crypto;

import io.mangoo.utils.Argument;

/**
 * The Argon2id parameters a hash was computed with
 * <p>
 * Hashes carry the parameters they were created with, see {@link Argon2Hash}. Changing
 * the configured parameters therefore does not invalidate stored hashes, verification
 * always uses the parameters of the stored hash. Use
 * {@link PasswordHasher#needsRehash(String)} to find hashes that were created with
 * something else than the current configuration
 *
 * @param memoryKb Memory cost in kibibytes
 * @param iterations Number of iterations (time cost)
 * @param parallelism Number of lanes
 */
public record Argon2Settings(int memoryKb, int iterations, int parallelism) {
    public static final int MIN_MEMORY_KB = 8192;
    public static final int MIN_ITERATIONS = 2;
    public static final int MIN_PARALLELISM = 1;

    /**
     * The parameters mangoo I/O hashed with before hashes carried their own parameters.
     * A stored hash that is not in PHC format was created with these
     */
    public static final Argon2Settings LEGACY = new Argon2Settings(80000, 6, 2);

    /**
     * Checks that these settings are not weaker than the lower bounds mangoo I/O
     * accepts. Only applied to the configured settings, parameters read from a stored
     * hash are used as they are so that an older hash can still be verified
     *
     * @return These settings
     * @throws IllegalArgumentException If one of the values is below its lower bound
     */
    public Argon2Settings requireNotWeakerThanMinimum() {
        Argument.check(memoryKb >= MIN_MEMORY_KB,
                "authentication.hashing.memory must be at least " + MIN_MEMORY_KB + " KB, but is " + memoryKb);
        Argument.check(iterations >= MIN_ITERATIONS,
                "authentication.hashing.iterations must be at least " + MIN_ITERATIONS + ", but is " + iterations);
        Argument.check(parallelism >= MIN_PARALLELISM,
                "authentication.hashing.parallelism must be at least " + MIN_PARALLELISM + ", but is " + parallelism);

        return this;
    }
}
