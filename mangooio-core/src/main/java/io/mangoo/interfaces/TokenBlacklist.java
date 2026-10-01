package io.mangoo.interfaces;

import com.google.inject.ImplementedBy;
import io.mangoo.cache.InMemoryTokenBlacklist;

import java.time.Instant;

/**
 * Revocation of authentication tokens before their natural expiry. Only consulted
 * when authentication.blacklist is enabled.
 *
 * The default implementation keeps all revocations in memory, local to the running
 * instance. Applications that need revocations to survive a restart or to be shared
 * between multiple instances can bind their own implementation in their Guice module,
 * e.g. bind(TokenBlacklist.class).to(RedisTokenBlacklist.class)
 */
@ImplementedBy(InMemoryTokenBlacklist.class)
public interface TokenBlacklist {

    /**
     * Revokes a single token until it expires
     *
     * @param jwtId The ID (jti) of the token
     * @param expiresAt The expiry (exp) of the token, after which the revocation is no longer needed
     */
    void revoke(String jwtId, Instant expiresAt);

    /**
     * Revokes all tokens of the given subject that have been issued before the given point in time.
     * Tokens issued afterwards, e.g. after a new login, stay valid.
     *
     * @param subject The subject of the tokens
     * @param since Tokens issued before this point in time are revoked
     */
    void revokeSubject(String subject, Instant since);

    /**
     * Revokes all tokens of the given subject that have been issued until now
     *
     * @param subject The subject of the tokens
     */
    default void revokeSubject(String subject) {
        revokeSubject(subject, Instant.now());
    }

    /**
     * Checks if a token is revoked, either by its ID or by its subject
     *
     * @param jwtId The ID (jti) of the token
     * @param subject The subject (sub) of the token
     * @param issuedAt The issue time (iat) of the token
     * @return True if the token is revoked, false otherwise
     */
    boolean isRevoked(String jwtId, String subject, Instant issuedAt);
}
