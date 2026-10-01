package io.mangoo.crypto;

import io.mangoo.utils.Argument;

public record Argon2Settings(int memoryKb, int iterations, int parallelism) {
    public static final int MIN_MEMORY_KB = 8192;
    public static final int MIN_ITERATIONS = 2;
    public static final int MIN_PARALLELISM = 1;

    /** Parameters used before hashes carried their own; applied to stored hashes that are not in PHC format. */
    public static final Argon2Settings LEGACY = new Argon2Settings(80000, 6, 2);

    /**
     * Only applied to the configured settings; parameters of a stored hash are used as is so older hashes still verify.
     * Throws IllegalArgumentException if a value is below its minimum.
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
