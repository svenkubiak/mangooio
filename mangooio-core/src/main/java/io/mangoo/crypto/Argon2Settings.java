package io.mangoo.crypto;

/**
 * The Argon2id parameters an application hashes with
 * <p>
 * <strong>Changing any of these values invalidates every hash that has already been
 * stored.</strong> Users whose password was hashed with different parameters can no
 * longer log in. A change is only feasible together with a rehash on the next
 * successful login, see {@link PasswordHasher}
 *
 * @param memoryKb Memory cost in kibibytes
 * @param iterations Number of iterations (time cost)
 * @param parallelism Number of lanes
 */
public record Argon2Settings(int memoryKb, int iterations, int parallelism) {
}
