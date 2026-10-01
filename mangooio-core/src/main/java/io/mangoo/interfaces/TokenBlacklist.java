package io.mangoo.interfaces;

import com.google.inject.ImplementedBy;
import io.mangoo.cache.InMemoryTokenBlacklist;

import java.time.Instant;

/**
 * Revokes authentication tokens before their expiry; only consulted when authentication.blacklist is enabled.
 * The default in-memory implementation is local to the instance; bind your own implementation to share or persist revocations.
 */
@ImplementedBy(InMemoryTokenBlacklist.class)
public interface TokenBlacklist {

    /**
     * Revokes a single token; the revocation is no longer needed after expiresAt.
     */
    void revoke(String jwtId, Instant expiresAt);

    /**
     * Revokes all tokens of the subject issued before since; tokens issued afterwards (e.g. a new login) stay valid.
     */
    void revokeSubject(String subject, Instant since);

    default void revokeSubject(String subject) {
        revokeSubject(subject, Instant.now());
    }

    boolean isRevoked(String jwtId, String subject, Instant issuedAt);
}
